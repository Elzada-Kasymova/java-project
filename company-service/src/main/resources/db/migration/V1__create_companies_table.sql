CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS companies (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         name VARCHAR(100) NOT NULL UNIQUE,
                                         budget DOUBLE PRECISION,
                                         industry VARCHAR(100),
                                         address VARCHAR(255),
                                         country VARCHAR(100),
                                         user_id UUID,
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
