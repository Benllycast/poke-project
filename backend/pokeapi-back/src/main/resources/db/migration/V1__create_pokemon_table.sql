CREATE TABLE pokemon (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sprite_url VARCHAR(500),
    category VARCHAR(100),
    weight INT,
    height INT,
    abilities_json CLOB,
    moves_json CLOB,
    stats_json CLOB,
    types_json CLOB,
    description CLOB,
    evolution_chain_json CLOB,
    localized_name VARCHAR(100),
    region VARCHAR(100),
    tags VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
