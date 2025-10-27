CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE deals (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       title VARCHAR(150) NOT NULL,
                       company_id UUID NOT NULL,
                       user_id UUID NOT NULL,
                       stage VARCHAR(30) NOT NULL,
                       amount NUMERIC NOT NULL,
                       currency VARCHAR(10),
                       created_at TIMESTAMP NOT NULL DEFAULT now(),
                       updated_at TIMESTAMP
);
