CREATE TABLE IF NOT EXISTS payments (
  id SERIAL PRIMARY KEY,
  payment_id VARCHAR(255) UNIQUE NOT NULL,
  amount BIGINT,
  currency VARCHAR(16),
  payment_method VARCHAR(32),
  merchant_reference VARCHAR(255),
  status VARCHAR(32),
  attempts INTEGER DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE,
  updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS attempts (
  id SERIAL PRIMARY KEY,
  payment_id VARCHAR(255),
  provider VARCHAR(255),
  attempt_no INTEGER,
  status VARCHAR(32),
  response TEXT,
  created_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS idempotency (
  idempotency_key VARCHAR(255) PRIMARY KEY,
  payment_id VARCHAR(255),
  response_code INTEGER,
  response_body TEXT,
  created_at TIMESTAMP WITH TIME ZONE
);

