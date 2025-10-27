CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE activities (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            type VARCHAR(20) NOT NULL,
                            title VARCHAR(150) NOT NULL,
                            description VARCHAR(500),
                            user_id UUID,
                            company_id UUID,
                            deal_id UUID,
                            status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                            due_date TIMESTAMP,
                            created_at TIMESTAMP NOT NULL DEFAULT now()
);
