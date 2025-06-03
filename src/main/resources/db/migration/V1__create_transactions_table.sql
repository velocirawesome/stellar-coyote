	CREATE TABLE ledger (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    account VARCHAR(255) NOT NULL,
    -- yeah don't store monetary amounts as floating point, it is a bad idea
    amount DOUBLE PRECISION NOT NULL,
    name VARCHAR(255) NOT NULL,
    running_total DOUBLE PRECISION NOT NULL
);


-- powers fetching all the transactions for a given account order by timestamp
CREATE INDEX idx_ledger_account_timestamp ON ledger (account, timestamp);
