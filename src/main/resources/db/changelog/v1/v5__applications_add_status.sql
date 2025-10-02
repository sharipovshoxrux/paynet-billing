ALTER TABLE applications
    ADD COLUMN status VARCHAR(16);

UPDATE applications SET status = 'ACTIVE' WHERE status IS NULL;

ALTER TABLE applications
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE applications
    ADD CONSTRAINT ck_app_status CHECK (status IN ('ACTIVE','CANCELLED'));

CREATE INDEX IF NOT EXISTS ix_app_status    ON applications(status);
