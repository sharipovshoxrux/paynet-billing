UPDATE applications
SET status = UPPER(BTRIM(status))
WHERE status IS NOT NULL;

UPDATE applications SET status = 'CREATED'
WHERE status IS NULL OR status = '' OR status = 'ACTIVE';

UPDATE applications SET status = 'CANCELLED'
WHERE status = 'CANCELED';

UPDATE applications
SET status = 'CREATED'
WHERE status NOT IN ('CREATED','PAID','CANCELLED','COMPLETED','REFUNDED');

ALTER TABLE applications
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE applications
DROP CONSTRAINT IF EXISTS ck_app_status;

ALTER TABLE applications
    ADD CONSTRAINT ck_app_status
        CHECK (status IN ('CREATED','PAID','CANCELLED','COMPLETED','REFUNDED'))
    NOT VALID;

ALTER TABLE applications VALIDATE CONSTRAINT ck_app_status;

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_active
    ON applications(user_id)
    WHERE status IN ('CREATED','PAID');