-- ============================================================
-- V3: Stock Positions
-- ============================================================

CREATE TABLE stock_positions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    symbol          VARCHAR(10) NOT NULL,
    company_name    VARCHAR(200),
    quantity        NUMERIC(19,6) NOT NULL CHECK (quantity >= 0),
    average_cost    NUMERIC(19,4) NOT NULL CHECK (average_cost >= 0),
    current_price   NUMERIC(19,4),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, symbol)
);

-- Seed stock positions
INSERT INTO stock_positions (user_id, symbol, company_name, quantity, average_cost, current_price)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'AAPL', 'Apple Inc.', 50.000000, 175.5000, 192.3000),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'GOOGL', 'Alphabet Inc.', 25.000000, 140.2500, 155.8000),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MSFT', 'Microsoft Corp.', 30.000000, 380.0000, 415.2000),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'AMZN', 'Amazon.com Inc.', 15.000000, 155.0000, 185.5000),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TSLA', 'Tesla Inc.', 20.000000, 245.0000, 262.0000);
