-- ============================================================================
-- Launcher Tables - Hegemonia
-- ============================================================================

-- Table des utilisateurs du launcher
CREATE TABLE IF NOT EXISTS launcher_users (
    id SERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    username VARCHAR(16) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(32),
    avatar_url TEXT,
    role VARCHAR(20) DEFAULT 'user' CHECK (role IN ('user', 'admin', 'moderator')),
    minecraft_uuid UUID REFERENCES hegemonia_players(uuid) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    last_login TIMESTAMP,
    settings JSONB DEFAULT '{}'::jsonb,
    CONSTRAINT username_length CHECK (char_length(username) >= 3 AND char_length(username) <= 16),
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Index pour performances
CREATE INDEX IF NOT EXISTS idx_launcher_users_email ON launcher_users(email);
CREATE INDEX IF NOT EXISTS idx_launcher_users_username ON launcher_users(username);
CREATE INDEX IF NOT EXISTS idx_launcher_users_minecraft_uuid ON launcher_users(minecraft_uuid);

-- Table des actualités
CREATE TABLE IF NOT EXISTS launcher_news (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id INTEGER REFERENCES launcher_users(id) ON DELETE SET NULL,
    thumbnail_url TEXT,
    published_at TIMESTAMP DEFAULT NOW(),
    is_published BOOLEAN DEFAULT true,
    tags TEXT[] DEFAULT '{}',
    views_count INTEGER DEFAULT 0
);

-- Index pour performances
CREATE INDEX IF NOT EXISTS idx_launcher_news_published ON launcher_news(published_at DESC) WHERE is_published = true;
CREATE INDEX IF NOT EXISTS idx_launcher_news_author ON launcher_news(author_id);

-- Table des sessions (pour JWT refresh tokens)
CREATE TABLE IF NOT EXISTS launcher_sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES launcher_users(id) ON DELETE CASCADE,
    refresh_token VARCHAR(500) UNIQUE NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true
);

-- Index pour performances
CREATE INDEX IF NOT EXISTS idx_launcher_sessions_user ON launcher_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_launcher_sessions_token ON launcher_sessions(refresh_token);
CREATE INDEX IF NOT EXISTS idx_launcher_sessions_expires ON launcher_sessions(expires_at);

-- Commentaires
COMMENT ON TABLE launcher_users IS 'Comptes utilisateurs du launcher Hegemonia';
COMMENT ON TABLE launcher_news IS 'Actualités affichées dans le launcher';
COMMENT ON TABLE launcher_sessions IS 'Sessions utilisateurs pour JWT refresh tokens';
