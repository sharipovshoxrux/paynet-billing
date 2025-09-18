CREATE TABLE IF NOT EXISTS applications (
    id              BIGSERIAL PRIMARY KEY,
    application_id  VARCHAR(64)   NOT NULL,
    user_id         UUID          NOT NULL,
    pinfl           VARCHAR(16)   NOT NULL,
    name            VARCHAR(160)  NOT NULL,
    amount          NUMERIC(18,2) NOT NULL,
    source          VARCHAR(16)   NOT NULL,   -- BANK | INSON | APP
    purpose         VARCHAR(16)   NOT NULL,   -- ISSUE | RE_ISSUE
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NULL,
    CONSTRAINT uq_app_application_id UNIQUE (application_id),
    CONSTRAINT ck_app_source  CHECK (source  IN ('BANK','INSON','APP')),
    CONSTRAINT ck_app_purpose CHECK (purpose IN ('ISSUE','RE_ISSUE'))
    );

CREATE INDEX IF NOT EXISTS ix_app_user_id ON applications (user_id);
CREATE INDEX IF NOT EXISTS ix_app_pinfl   ON applications (pinfl);
CREATE INDEX IF NOT EXISTS ix_app_source  ON applications (source);
CREATE INDEX IF NOT EXISTS ix_app_purpose ON applications (purpose);

CREATE TABLE IF NOT EXISTS payment_transactions (
    id               BIGSERIAL PRIMARY KEY,
    application_id   VARCHAR(64)   NOT NULL,
    amount           NUMERIC(18,2) NOT NULL,
    state            VARCHAR(16)   NOT NULL,  -- PAID | CANCELED
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NULL,
    CONSTRAINT ck_txn_state CHECK (state IN ('PAID','CANCELLED')),
    CONSTRAINT fk_txn_application
    FOREIGN KEY (application_id) REFERENCES applications (application_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT
    );

CREATE INDEX IF NOT EXISTS ix_txn_app_id
    ON payment_transactions (application_id);

CREATE INDEX IF NOT EXISTS ix_txn_state
    ON payment_transactions (state);