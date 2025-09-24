ALTER TABLE payment_transactions
    ADD COLUMN transaction_id BIGINT;

CREATE INDEX ix_txn_transaction_id ON payment_transactions(transaction_id);