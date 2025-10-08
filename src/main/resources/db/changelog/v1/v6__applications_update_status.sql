ALTER TABLE applications DROP CONSTRAINT IF EXISTS ck_app_status;
ALTER TABLE applications
    ADD CONSTRAINT ck_app_status
        CHECK (status IN ('CREATED','PAID','CANCELLED','COMPLETED','REFUNDED'));

UPDATE applications SET status = 'CREATED'  WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_active
    ON applications(user_id)
    WHERE status IN ('CREATED','PAID');
