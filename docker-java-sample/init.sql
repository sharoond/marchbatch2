-- Create schema
CREATE SCHEMA IF NOT EXISTS "cdf";

-- Create users table
CREATE TABLE IF NOT EXISTS "cdf"."users" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user table (singular)
CREATE TABLE IF NOT EXISTS "cdf"."user" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- (Optional) Insert sample data here if desired.
-- INSERT INTO "cdf"."users" (name, email) VALUES ('John Doe', 'john@example.com');
-- INSERT INTO "cdf"."users" (name, email) VALUES ('Jane Smith', 'jane@example.com');

-- (Optional) sample row for singular "user" table
-- INSERT INTO "cdf"."user" (name, email) VALUES ('Single User', 'single@example.com');
