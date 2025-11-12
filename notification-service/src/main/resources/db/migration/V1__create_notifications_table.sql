CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS notifications (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(255) UNIQUE NOT NULL,
    event_type VARCHAR(255),
    template_name VARCHAR(255),
    payload TEXT,
    recipient_email VARCHAR(255),
    status VARCHAR(50),
    error TEXT,
    attempts INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    sent_at TIMESTAMP WITHOUT TIME ZONE
    );
