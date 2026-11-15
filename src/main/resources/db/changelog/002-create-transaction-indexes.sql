-- Speeds up category/date lookups for transactions.
CREATE INDEX idx_transaction_category_date
    ON "transaction" (category_id, transaction_date);
