CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE activity_type AS ENUM ('TASK', 'CALL', 'MEETING');
CREATE TYPE activity_status AS ENUM ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED');

CREATE TABLE IF NOT EXISTS activities (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(512) NOT NULL,
    description TEXT,
    type activity_type NOT NULL,
    status activity_status NOT NULL,
    user_id UUID,
    deal_id UUID,
    company_id UUID,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    completed_at TIMESTAMP WITH TIME ZONE
                             );


