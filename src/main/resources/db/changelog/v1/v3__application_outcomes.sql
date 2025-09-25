CREATE TABLE IF NOT EXISTS application_outcomes (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    application_id VARCHAR(64) NOT NULL,
    purpose VARCHAR(16) NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_outcome_appid_purpose UNIQUE (application_id, purpose),
    CONSTRAINT ck_outcome_purpose CHECK (purpose IN ('ISSUE','RE_ISSUE'))
    );

CREATE INDEX IF NOT EXISTS ix_outcome_app_id
    ON application_outcomes (application_id);