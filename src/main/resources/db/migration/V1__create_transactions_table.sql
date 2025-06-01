	CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    account VARCHAR(255) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    name VARCHAR(255) NOT NULL
);


-- powers fetching all the transactions for a given account order by timestamp
CREATE INDEX idx_transactions_account_timestamp ON transactions (account, timestamp);
