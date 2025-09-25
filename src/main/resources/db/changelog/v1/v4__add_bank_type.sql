ALTER TABLE applications
    ADD COLUMN bank_type VARCHAR(16) NOT NULL;

ALTER TABLE applications
    ADD CONSTRAINT ck_app_bank_type
        CHECK (bank_type IN ('ALOQA','XALQ'));

CREATE INDEX IF NOT EXISTS ix_app_bank_type
    ON applications (bank_type);

CREATE INDEX IF NOT EXISTS ix_app_user_bank_type
    ON applications (user_id, bank_type);