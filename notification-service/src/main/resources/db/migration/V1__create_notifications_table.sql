CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE notifications (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               recipient_user_id UUID,
                               type VARCHAR(20),
                               message TEXT,
                               status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                               created_at TIMESTAMP NOT NULL DEFAULT now()
);
