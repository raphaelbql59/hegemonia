-- ===========================
-- HEGEMONIA - Web Database Schema
-- Base de données pour le site web et l'API
-- ===========================

-- Créer la base web séparée
CREATE DATABASE hegemonia_web;

-- Se connecter à la base web
\c hegemonia_web;

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===========================
-- UTILISATEURS WEB
-- ===========================

CREATE TABLE web_users (
    id SERIAL PRIMARY KEY,
    minecraft_uuid UUID UNIQUE,  -- Liaison avec compte Minecraft
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),  -- Si auth par email/password
    display_name VARCHAR(100),
    avatar_url TEXT,

    -- OAuth
    microsoft_id VARCHAR(255) UNIQUE,  -- Microsoft/Mojang ID
    discord_id VARCHAR(255) UNIQUE,

    -- Statut
    email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_staff BOOLEAN DEFAULT FALSE,
    is_superuser BOOLEAN DEFAULT FALSE,

    -- Dates
    created_at TIMESTAMP DEFAULT NOW(),
    last_login TIMESTAMP,

    -- Préférences
    language VARCHAR(5) DEFAULT 'fr',
    timezone VARCHAR(50) DEFAULT 'Europe/Paris',
    theme VARCHAR(20) DEFAULT 'dark'
);

CREATE INDEX idx_web_users_minecraft ON web_users(minecraft_uuid);
CREATE INDEX idx_web_users_email ON web_users(email);
CREATE INDEX idx_web_users_active ON web_users(is_active);

-- ===========================
-- SESSIONS WEB
-- ===========================

CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id INTEGER REFERENCES web_users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    ip_address INET,
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_sessions_user ON sessions(user_id);
CREATE INDEX idx_sessions_token ON sessions(token);
CREATE INDEX idx_sessions_expires ON sessions(expires_at);

-- Auto-cleanup des sessions expirées
CREATE OR REPLACE FUNCTION cleanup_expired_sessions()
RETURNS void AS $$
BEGIN
    DELETE FROM sessions WHERE expires_at < NOW();
END;
$$ LANGUAGE plpgsql;

-- ===========================
-- API TOKENS
-- ===========================

CREATE TABLE api_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES web_users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,  -- Nom descriptif du token
    permissions JSONB DEFAULT '[]',  -- Array of permissions
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP
);

CREATE INDEX idx_api_tokens_user ON api_tokens(user_id);
CREATE INDEX idx_api_tokens_token ON api_tokens(token);

-- ===========================
-- FORUM - CATÉGORIES
-- ===========================

CREATE TABLE forum_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon VARCHAR(50),  -- Nom de l'icône
    color VARCHAR(7),  -- Couleur hex
    position INTEGER DEFAULT 0,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ===========================
-- FORUM - SUJETS
-- ===========================

CREATE TABLE forum_topics (
    id SERIAL PRIMARY KEY,
    category_id INTEGER REFERENCES forum_categories(id) ON DELETE CASCADE,
    author_id INTEGER REFERENCES web_users(id) ON DELETE SET NULL,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) UNIQUE NOT NULL,
    content TEXT NOT NULL,

    -- Statut
    is_pinned BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    is_hidden BOOLEAN DEFAULT FALSE,

    -- Statistiques
    view_count INTEGER DEFAULT 0,
    reply_count INTEGER DEFAULT 0,

    -- Dates
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_reply_at TIMESTAMP
);

CREATE INDEX idx_forum_topics_category ON forum_topics(category_id);
CREATE INDEX idx_forum_topics_author ON forum_topics(author_id);
CREATE INDEX idx_forum_topics_slug ON forum_topics(slug);
CREATE INDEX idx_forum_topics_pinned ON forum_topics(is_pinned);

-- ===========================
-- FORUM - RÉPONSES
-- ===========================

CREATE TABLE forum_replies (
    id SERIAL PRIMARY KEY,
    topic_id INTEGER REFERENCES forum_topics(id) ON DELETE CASCADE,
    author_id INTEGER REFERENCES web_users(id) ON DELETE SET NULL,
    parent_reply_id INTEGER REFERENCES forum_replies(id) ON DELETE CASCADE,  -- Pour les réponses aux réponses
    content TEXT NOT NULL,

    -- Statut
    is_hidden BOOLEAN DEFAULT FALSE,

    -- Dates
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_forum_replies_topic ON forum_replies(topic_id);
CREATE INDEX idx_forum_replies_author ON forum_replies(author_id);
CREATE INDEX idx_forum_replies_parent ON forum_replies(parent_reply_id);

-- ===========================
-- WIKI - PAGES
-- ===========================

CREATE TABLE wiki_pages (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) UNIQUE NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100),

    -- Authorship
    created_by INTEGER REFERENCES web_users(id) ON DELETE SET NULL,
    last_edited_by INTEGER REFERENCES web_users(id) ON DELETE SET NULL,

    -- Statut
    is_published BOOLEAN DEFAULT FALSE,
    is_protected BOOLEAN DEFAULT FALSE,  -- Éditable uniquement par staff

    -- SEO
    meta_description TEXT,
    keywords TEXT,

    -- Statistiques
    view_count INTEGER DEFAULT 0,
    revision_count INTEGER DEFAULT 0,

    -- Dates
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_wiki_pages_slug ON wiki_pages(slug);
CREATE INDEX idx_wiki_pages_category ON wiki_pages(category);
CREATE INDEX idx_wiki_pages_published ON wiki_pages(is_published);

-- ===========================
-- WIKI - RÉVISIONS
-- ===========================

CREATE TABLE wiki_revisions (
    id SERIAL PRIMARY KEY,
    page_id INTEGER REFERENCES wiki_pages(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    edited_by INTEGER REFERENCES web_users(id) ON DELETE SET NULL,
    edit_summary TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_wiki_revisions_page ON wiki_revisions(page_id);

-- ===========================
-- TICKETS SUPPORT
-- ===========================

CREATE TABLE support_tickets (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES web_users(id) ON DELETE SET NULL,
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN (
        'BUG', 'HELP', 'QUESTION', 'REPORT', 'OTHER'
    )),
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN (
        'LOW', 'NORMAL', 'HIGH', 'URGENT'
    )),
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN (
        'OPEN', 'IN_PROGRESS', 'WAITING', 'RESOLVED', 'CLOSED'
    )),

    assigned_to INTEGER REFERENCES web_users(id) ON DELETE SET NULL,

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    closed_at TIMESTAMP
);

CREATE INDEX idx_support_tickets_user ON support_tickets(user_id);
CREATE INDEX idx_support_tickets_status ON support_tickets(status);
CREATE INDEX idx_support_tickets_assigned ON support_tickets(assigned_to);

-- ===========================
-- RÉPONSES TICKETS
-- ===========================

CREATE TABLE support_replies (
    id SERIAL PRIMARY KEY,
    ticket_id INTEGER REFERENCES support_tickets(id) ON DELETE CASCADE,
    author_id INTEGER REFERENCES web_users(id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    is_staff_reply BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_support_replies_ticket ON support_replies(ticket_id);

-- ===========================
-- BOUTIQUE - PRODUITS
-- ===========================

CREATE TABLE shop_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    currency VARCHAR(3) DEFAULT 'EUR',

    -- Type de produit
    type VARCHAR(50) NOT NULL CHECK (type IN (
        'RANK', 'COSMETIC', 'BOOST', 'ITEM', 'OTHER'
    )),

    -- Minecraft data
    minecraft_command TEXT,  -- Commande à exécuter in-game

    -- Statut
    is_available BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,

    -- Médias
    image_url TEXT,

    -- Statistiques
    sales_count INTEGER DEFAULT 0,

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_shop_products_type ON shop_products(type);
CREATE INDEX idx_shop_products_available ON shop_products(is_available);

-- ===========================
-- BOUTIQUE - COMMANDES
-- ===========================

CREATE TABLE shop_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id INTEGER REFERENCES web_users(id) ON DELETE SET NULL,
    product_id INTEGER REFERENCES shop_products(id) ON DELETE SET NULL,

    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'EUR',

    -- Statut paiement
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN (
        'PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'
    )),

    -- Stripe
    stripe_payment_intent_id VARCHAR(255),
    stripe_charge_id VARCHAR(255),

    -- Delivery
    is_delivered BOOLEAN DEFAULT FALSE,
    delivered_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_shop_orders_user ON shop_orders(user_id);
CREATE INDEX idx_shop_orders_status ON shop_orders(status);

-- ===========================
-- NOTIFICATIONS
-- ===========================

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES web_users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    link TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);

-- ===========================
-- TRIGGERS
-- ===========================

CREATE TRIGGER update_forum_topics_updated_at BEFORE UPDATE ON forum_topics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wiki_pages_updated_at BEFORE UPDATE ON wiki_pages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_support_tickets_updated_at BEFORE UPDATE ON support_tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ===========================
-- DONNÉES INITIALES
-- ===========================

-- Catégories forum
INSERT INTO forum_categories (name, slug, description, position) VALUES
    ('Annonces', 'annonces', 'Annonces officielles du serveur', 1),
    ('Aide', 'aide', 'Besoin d''aide ? Posez vos questions ici', 2),
    ('Suggestions', 'suggestions', 'Vos idées pour améliorer le serveur', 3),
    ('Discussions', 'discussions', 'Discussions générales', 4),
    ('Nations', 'nations', 'Discussions entre nations', 5)
ON CONFLICT DO NOTHING;

-- ===========================
-- PERMISSIONS
-- ===========================

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO hegemonia;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO hegemonia;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO hegemonia;

-- ===========================
-- FIN DU SCHEMA WEB
-- ===========================
