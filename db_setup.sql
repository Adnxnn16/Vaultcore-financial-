-- VaultCore Financial — DB Setup Script
-- Run as: psql -U postgres -f db_setup.sql

-- Create role if not exists
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'vaultcore') THEN
    CREATE USER vaultcore WITH PASSWORD 'vaultcore_secret';
    RAISE NOTICE 'Role vaultcore created.';
  ELSE
    RAISE NOTICE 'Role vaultcore already exists.';
  END IF;
END
$$;

-- Create database if not exists
SELECT 'CREATE DATABASE vaultcore_app OWNER vaultcore'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'vaultcore_app')\gexec

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE vaultcore_app TO vaultcore;
