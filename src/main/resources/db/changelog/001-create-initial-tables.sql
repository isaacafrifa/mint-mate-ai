CREATE TABLE category (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE transaction(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    merchant VARCHAR(150) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    category_id BIGINT NOT NULL,
    -- Restrict transaction type to supported values.
    CONSTRAINT chk_transaction_type
        CHECK (transaction_type IN ('INCOME', 'EXPENSE')),
    -- Enforce amount sign based on transaction type.
    CONSTRAINT chk_transaction_amount
        CHECK (
            (transaction_type = 'INCOME' AND amount > 0)
            OR
            (transaction_type = 'EXPENSE' AND amount < 0)
        ),
    CONSTRAINT fk_transaction_category
        FOREIGN KEY (category_id)
        REFERENCES category (id)
);
