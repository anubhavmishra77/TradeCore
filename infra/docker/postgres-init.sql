-- Runs once on first container start, before Flyway migrations.
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Flyway owns the schema from V1 onward; keep this file for extensions only.
