-- ===========================
-- HEGEMONIA - PostgreSQL Schema
-- Base de données principale
-- ===========================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- Pour recherche texte

-- ===========================
-- NATIONS
-- ===========================

CREATE TABLE nations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) UNIQUE NOT NULL,
    tag VARCHAR(5) UNIQUE NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('MINOR', 'REGIONAL', 'MAJOR', 'SUPERPOWER')),
    government_type VARCHAR(50) NOT NULL,
    capital_region_id UUID,
    president_uuid UUID,
    treasury DECIMAL(20, 2) DEFAULT 0 CHECK (treasury >= 0),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),

    -- Métadonnées additionnelles
    description TEXT,
    flag_url TEXT,
    color VARCHAR(7),  -- Couleur hex (#RRGGBB)
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_nations_type ON nations(type);
CREATE INDEX idx_nations_active ON nations(is_active);
CREATE INDEX idx_nations_name_trgm ON nations USING gin(name gin_trgm_ops);

-- ===========================
-- CITOYENS DES NATIONS
-- ===========================

CREATE TABLE nation_citizens (
    nation_id UUID REFERENCES nations(id) ON DELETE CASCADE,
    player_uuid UUID NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CITIZEN',
    minister_role VARCHAR(50),  -- Si ministre
    joined_at TIMESTAMP DEFAULT NOW(),

    PRIMARY KEY (nation_id, player_uuid)
);

CREATE INDEX idx_nation_citizens_player ON nation_citizens(player_uuid);
CREATE INDEX idx_nation_citizens_role ON nation_citizens(role);

-- ===========================
-- RÉGIONS (TERRITOIRES)
-- ===========================

CREATE TABLE regions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('NATION', 'STATE', 'PROVINCE', 'TERRITORY')),
    parent_region_id UUID REFERENCES regions(id) ON DELETE SET NULL,
    owner_nation_id UUID REFERENCES nations(id) ON DELETE SET NULL,

    -- Géographie
    capital_coords JSONB,  -- {x, y, z}
    bounds JSONB NOT NULL,  -- [[x1,z1], [x2,z2], ...] Polygon

    -- Ressources
    resources JSONB,  -- {resource: abundance_level}

    -- Métadonnées
    population INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),

    UNIQUE (name, owner_nation_id)
);

CREATE INDEX idx_regions_owner ON regions(owner_nation_id);
CREATE INDEX idx_regions_parent ON regions(parent_region_id);
CREATE INDEX idx_regions_type ON regions(type);

-- ===========================
-- JOUEURS
-- ===========================

CREATE TABLE players (
    uuid UUID PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    current_nation_id UUID REFERENCES nations(id) ON DELETE SET NULL,

    -- Économie
    balance DECIMAL(20, 2) DEFAULT 0 CHECK (balance >= 0),

    -- Réputation (JSONB pour flexibilité)
    reputation_individual JSONB DEFAULT '{}',  -- {category: score}

    -- Statistiques
    first_join TIMESTAMP DEFAULT NOW(),
    last_seen TIMESTAMP DEFAULT NOW(),
    playtime_minutes INTEGER DEFAULT 0,

    -- Métadonnées
    language VARCHAR(5) DEFAULT 'fr',
    discord_id VARCHAR(20),

    UNIQUE (username)
);

CREATE INDEX idx_players_nation ON players(current_nation_id);
CREATE INDEX idx_players_username ON players(username);
CREATE INDEX idx_players_last_seen ON players(last_seen);

-- ===========================
-- ENTREPRISES
-- ===========================

CREATE TABLE enterprises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN (
        'MINING', 'MANUFACTURING', 'AGRICULTURE', 'ENERGY',
        'ARMS', 'TECH', 'TRADE', 'FINANCE'
    )),

    -- Propriété
    owner_uuid UUID,
    owner_nation_id UUID REFERENCES nations(id) ON DELETE SET NULL,  -- Si nationalisée
    location_nation_id UUID REFERENCES nations(id) ON DELETE RESTRICT NOT NULL,

    -- Finances
    capital DECIMAL(20, 2) DEFAULT 0 CHECK (capital >= 0),

    -- Opérations
    employees INTEGER DEFAULT 0,
    efficiency DECIMAL(5, 2) DEFAULT 1.0 CHECK (efficiency >= 0),
    is_nationalized BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_enterprises_owner ON enterprises(owner_uuid);
CREATE INDEX idx_enterprises_nation ON enterprises(owner_nation_id);
CREATE INDEX idx_enterprises_location ON enterprises(location_nation_id);
CREATE INDEX idx_enterprises_type ON enterprises(type);

-- ===========================
-- ÉCONOMIE - PRIX MARCHÉ
-- ===========================

CREATE TABLE market_prices (
    resource VARCHAR(50) PRIMARY KEY,
    current_price DECIMAL(10, 2) NOT NULL CHECK (current_price >= 0),
    base_price DECIMAL(10, 2) NOT NULL,  -- Prix de référence
    supply INTEGER DEFAULT 0,
    demand INTEGER DEFAULT 0,
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ===========================
-- ÉCONOMIE - HISTORIQUE PRIX
-- ===========================

CREATE TABLE market_price_history (
    id BIGSERIAL PRIMARY KEY,
    resource VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    volume INTEGER DEFAULT 0,
    recorded_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_market_history_resource ON market_price_history(resource);
CREATE INDEX idx_market_history_date ON market_price_history(recorded_at);

-- ===========================
-- TRANSACTIONS
-- ===========================

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    from_uuid UUID,  -- NULL si système
    to_uuid UUID,    -- NULL si système
    amount DECIMAL(20, 2) NOT NULL CHECK (amount > 0),
    type VARCHAR(50) NOT NULL,  -- TRADE, TAX, SALARY, TRANSFER, etc.
    description TEXT,
    metadata JSONB,  -- Informations additionnelles
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_transactions_from ON transactions(from_uuid);
CREATE INDEX idx_transactions_to ON transactions(to_uuid);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_date ON transactions(created_at);

-- ===========================
-- DIPLOMATIE - TRAITÉS
-- ===========================

CREATE TABLE treaties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200),
    type VARCHAR(50) NOT NULL CHECK (type IN (
        'PEACE', 'ALLIANCE', 'TRADE', 'NON_AGGRESSION',
        'DEFENSE', 'UNION', 'VASSALIZATION'
    )),

    nations JSONB NOT NULL,  -- Array of nation UUIDs
    terms JSONB,  -- Conditions du traité

    signed_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'BROKEN', 'SUSPENDED'))
);

CREATE INDEX idx_treaties_status ON treaties(status);
CREATE INDEX idx_treaties_type ON treaties(type);

-- ===========================
-- DIPLOMATIE - RELATIONS
-- ===========================

CREATE TABLE relations (
    nation_a_id UUID REFERENCES nations(id) ON DELETE CASCADE,
    nation_b_id UUID REFERENCES nations(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'NEUTRAL',
    value INTEGER DEFAULT 0 CHECK (value >= -100 AND value <= 100),  -- -100 à +100
    updated_at TIMESTAMP DEFAULT NOW(),

    PRIMARY KEY (nation_a_id, nation_b_id),
    CHECK (nation_a_id < nation_b_id)  -- Éviter doublons (A-B et B-A)
);

CREATE INDEX idx_relations_status ON relations(status);

-- ===========================
-- GUERRES
-- ===========================

CREATE TABLE wars (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN (
        'CONQUEST', 'LIBERATION', 'INDEPENDENCE', 'PUNITIVE',
        'RELIGIOUS', 'ECONOMIC', 'WORLD_WAR', 'CIVIL_WAR', 'PROXY_WAR'
    )),

    attackers JSONB NOT NULL,  -- Array of nation UUIDs
    defenders JSONB NOT NULL,

    war_goals JSONB NOT NULL,  -- Objectifs des deux côtés
    war_score INTEGER DEFAULT 0 CHECK (war_score >= -100 AND war_score <= 100),

    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PEACE_TALKS', 'ENDED')),

    started_at TIMESTAMP DEFAULT NOW(),
    ended_at TIMESTAMP
);

CREATE INDEX idx_wars_status ON wars(status);
CREATE INDEX idx_wars_type ON wars(type);

-- ===========================
-- BATAILLES
-- ===========================

CREATE TABLE battles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    war_id UUID REFERENCES wars(id) ON DELETE CASCADE,
    location JSONB NOT NULL,  -- {x, y, z, world, region_name}

    attacker_nation_id UUID REFERENCES nations(id) ON DELETE SET NULL,
    defender_nation_id UUID REFERENCES nations(id) ON DELETE SET NULL,
    winner_nation_id UUID REFERENCES nations(id) ON DELETE SET NULL,

    participants JSONB,  -- Players involved with stats
    casualties JSONB,  -- {kills, deaths, by_nation}
    objectives JSONB,  -- Objectifs capturés/défendus

    duration_seconds INTEGER,
    fought_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_battles_war ON battles(war_id);
CREATE INDEX idx_battles_date ON battles(fought_at);

-- ===========================
-- SIÈGES
-- ===========================

CREATE TABLE sieges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    war_id UUID REFERENCES wars(id) ON DELETE CASCADE,
    region_id UUID REFERENCES regions(id) ON DELETE CASCADE,
    strategic_point VARCHAR(100) NOT NULL,

    attacker_nation_id UUID REFERENCES nations(id) ON DELETE SET NULL,
    defender_nation_id UUID REFERENCES nations(id) ON DELETE SET NULL,

    progress INTEGER DEFAULT 0 CHECK (progress >= 0 AND progress <= 100),
    attacker_troops INTEGER DEFAULT 0,
    defender_troops INTEGER DEFAULT 0,

    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUCCESS', 'FAILED', 'ABANDONED')),

    started_at TIMESTAMP DEFAULT NOW(),
    ended_at TIMESTAMP
);

CREATE INDEX idx_sieges_war ON sieges(war_id);
CREATE INDEX idx_sieges_region ON sieges(region_id);
CREATE INDEX idx_sieges_status ON sieges(status);

-- ===========================
-- JOBS - MÉTIERS JOUEURS
-- ===========================

CREATE TABLE player_jobs (
    player_uuid UUID REFERENCES players(uuid) ON DELETE CASCADE,
    job_type VARCHAR(50) NOT NULL,
    level INTEGER DEFAULT 1 CHECK (level >= 1 AND level <= 100),
    xp INTEGER DEFAULT 0 CHECK (xp >= 0),
    specialization VARCHAR(50),

    PRIMARY KEY (player_uuid, job_type)
);

CREATE INDEX idx_player_jobs_type ON player_jobs(job_type);
CREATE INDEX idx_player_jobs_level ON player_jobs(level);

-- ===========================
-- TECHNOLOGIES
-- ===========================

CREATE TABLE nation_technologies (
    nation_id UUID REFERENCES nations(id) ON DELETE CASCADE,
    tech_id VARCHAR(100) NOT NULL,
    unlocked_at TIMESTAMP DEFAULT NOW(),

    PRIMARY KEY (nation_id, tech_id)
);

-- Technologies en recherche
CREATE TABLE nation_research (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nation_id UUID REFERENCES nations(id) ON DELETE CASCADE,
    tech_id VARCHAR(100) NOT NULL,
    progress INTEGER DEFAULT 0,  -- Points de recherche accumulés
    required_points INTEGER NOT NULL,
    started_at TIMESTAMP DEFAULT NOW(),
    estimated_completion TIMESTAMP,

    UNIQUE (nation_id, tech_id)
);

-- ===========================
-- LOGS ET AUDIT
-- ===========================

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    actor_uuid UUID,
    target_uuid UUID,
    nation_id UUID REFERENCES nations(id) ON DELETE SET NULL,
    details JSONB,
    ip_address INET,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_event ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_uuid);
CREATE INDEX idx_audit_logs_nation ON audit_logs(nation_id);
CREATE INDEX idx_audit_logs_date ON audit_logs(created_at);

-- ===========================
-- FONCTIONS UTILITAIRES
-- ===========================

-- Fonction pour mettre à jour updated_at automatiquement
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers pour updated_at
CREATE TRIGGER update_nations_updated_at BEFORE UPDATE ON nations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_enterprises_updated_at BEFORE UPDATE ON enterprises
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ===========================
-- VUES UTILES
-- ===========================

-- Vue pour les nations avec leurs statistiques
CREATE VIEW nations_stats AS
SELECT
    n.id,
    n.name,
    n.tag,
    n.type,
    n.government_type,
    n.treasury,
    COUNT(DISTINCT nc.player_uuid) as citizen_count,
    COUNT(DISTINCT r.id) as region_count,
    n.created_at
FROM nations n
LEFT JOIN nation_citizens nc ON n.id = nc.nation_id
LEFT JOIN regions r ON n.id = r.owner_nation_id
WHERE n.is_active = true
GROUP BY n.id;

-- Vue pour le leaderboard des joueurs les plus riches
CREATE VIEW wealthy_players AS
SELECT
    p.uuid,
    p.username,
    p.balance,
    n.name as nation_name,
    p.playtime_minutes
FROM players p
LEFT JOIN nations n ON p.current_nation_id = n.id
ORDER BY p.balance DESC
LIMIT 100;

-- ===========================
-- DONNÉES INITIALES
-- ===========================

-- Ressources de base avec prix
INSERT INTO market_prices (resource, current_price, base_price, supply, demand) VALUES
    ('iron_ingot', 10.00, 10.00, 0, 0),
    ('gold_ingot', 50.00, 50.00, 0, 0),
    ('diamond', 100.00, 100.00, 0, 0),
    ('emerald', 75.00, 75.00, 0, 0),
    ('coal', 5.00, 5.00, 0, 0),
    ('copper_ingot', 8.00, 8.00, 0, 0),
    ('wheat', 2.00, 2.00, 0, 0),
    ('wood', 3.00, 3.00, 0, 0),
    ('stone', 1.00, 1.00, 0, 0),
    ('oil_barrel', 200.00, 200.00, 0, 0),
    ('uranium', 500.00, 500.00, 0, 0),
    ('lithium', 150.00, 150.00, 0, 0),
    ('rare_earth', 300.00, 300.00, 0, 0)
ON CONFLICT (resource) DO NOTHING;

-- ===========================
-- PERMISSIONS
-- ===========================

-- Grant permissions à l'utilisateur hegemonia
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO hegemonia;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO hegemonia;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO hegemonia;

-- ===========================
-- COMMENTAIRES
-- ===========================

COMMENT ON TABLE nations IS 'Nations joueurs avec gouvernement et économie';
COMMENT ON TABLE regions IS 'Régions géographiques et territoires';
COMMENT ON TABLE wars IS 'Guerres actives et historique';
COMMENT ON TABLE market_prices IS 'Prix actuels du marché';
COMMENT ON TABLE audit_logs IS 'Logs de toutes les actions importantes';

-- ===========================
-- FIN DU SCHEMA
-- ===========================
