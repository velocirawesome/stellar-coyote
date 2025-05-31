CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    account VARCHAR(255) NOT NULL,
    amount DOUBLE PRECISION NOT NULL
);
