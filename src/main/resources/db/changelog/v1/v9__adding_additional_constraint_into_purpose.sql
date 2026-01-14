ALTER TABLE applications
DROP CONSTRAINT IF EXISTS ck_app_purpose;

ALTER TABLE applications
    ADD CONSTRAINT ck_app_purpose
        CHECK (purpose IN ('ISSUE', 'RE_ISSUE', 'ISSUE_AND_PRINT', 'PRINT'));