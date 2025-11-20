CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS deals (
                                     id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                     title varchar(255) NOT NULL,
                                     description text,
                                     amount numeric(14,2),
                                     stage varchar(50) NOT NULL,
                                     pipeline_id uuid,
                                     company_id uuid NOT NULL,
                                     user_id uuid NOT NULL,
                                     created_at timestamptz NOT NULL DEFAULT now(),
                                     updated_at timestamptz,
                                     closed_at timestamptz,
                                     is_deleted boolean NOT NULL DEFAULT false
);

