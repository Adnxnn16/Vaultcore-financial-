-- Optional display name for accounts (PRD: POST /accounts body includes nickname)
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS nickname VARCHAR(100);
