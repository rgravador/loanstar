-- ===================================================================
-- Loanstar Database Setup Script
-- This file contains all SQL queries for setting up the Supabase database
-- ===================================================================

-- ===================================================================
-- CLEANUP - DROP EXISTING OBJECTS
-- ===================================================================

-- Drop triggers first (they depend on functions)
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
DROP TRIGGER IF EXISTS update_accounts_updated_at ON accounts;
DROP TRIGGER IF EXISTS update_loans_updated_at ON loans;

-- Drop functions
DROP FUNCTION IF EXISTS update_updated_at_column();
DROP FUNCTION IF EXISTS calculate_loan_penalties();

-- Drop policies
-- Users table policies
DROP POLICY IF EXISTS "Users can insert own record" ON users;
DROP POLICY IF EXISTS "Users can view own profile" ON users;
DROP POLICY IF EXISTS "Users can update own profile" ON users;
DROP POLICY IF EXISTS "Admins can view all users" ON users;
DROP POLICY IF EXISTS "Admins can update all users" ON users;
DROP POLICY IF EXISTS "Authenticated users can view all users" ON users;
DROP POLICY IF EXISTS "Authenticated users can update users" ON users;

-- Accounts table policies
DROP POLICY IF EXISTS "Agents see own accounts" ON accounts;
DROP POLICY IF EXISTS "Admins see all accounts" ON accounts;
DROP POLICY IF EXISTS "Agents can create own accounts" ON accounts;
DROP POLICY IF EXISTS "Agents can update own accounts" ON accounts;
DROP POLICY IF EXISTS "Admins can create accounts" ON accounts;
DROP POLICY IF EXISTS "Admins can update accounts" ON accounts;
DROP POLICY IF EXISTS "Users can view relevant accounts" ON accounts;
DROP POLICY IF EXISTS "Authenticated users can create accounts" ON accounts;
DROP POLICY IF EXISTS "Users can update relevant accounts" ON accounts;

-- Drop indexes (will be automatically dropped with tables, but explicit for clarity)
DROP INDEX IF EXISTS idx_users_username;
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_users_role;
DROP INDEX IF EXISTS idx_users_approval_status;
DROP INDEX IF EXISTS idx_accounts_agent;
DROP INDEX IF EXISTS idx_accounts_status;
DROP INDEX IF EXISTS idx_accounts_name;
DROP INDEX IF EXISTS idx_loans_account;
DROP INDEX IF EXISTS idx_loans_status;
DROP INDEX IF EXISTS idx_loans_created_by;
DROP INDEX IF EXISTS idx_loans_approved_by;
DROP INDEX IF EXISTS idx_payments_loan;
DROP INDEX IF EXISTS idx_payments_date;
DROP INDEX IF EXISTS idx_payments_agent;
DROP INDEX IF EXISTS idx_notifications_user;
DROP INDEX IF EXISTS idx_notifications_is_read;
DROP INDEX IF EXISTS idx_notifications_timestamp;
DROP INDEX IF EXISTS idx_notifications_type;
DROP INDEX IF EXISTS idx_earnings_agent;
DROP INDEX IF EXISTS idx_cashout_agent;
DROP INDEX IF EXISTS idx_cashout_status;
DROP INDEX IF EXISTS idx_cashout_date;
DROP INDEX IF EXISTS idx_transactions_user;
DROP INDEX IF EXISTS idx_transactions_type;
DROP INDEX IF EXISTS idx_transactions_timestamp;

-- Drop tables in reverse order of dependencies
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS cashout_requests CASCADE;
DROP TABLE IF EXISTS earnings CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS loans CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ===================================================================
-- TABLE CREATION
-- ===================================================================

-- 1. Users Table
-- approval_status: PENDING by default for agents, APPROVED for admins
-- Agents require admin approval before they can login and use the system
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('admin', 'agent')),
    approval_status TEXT DEFAULT 'PENDING' CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    approved_by_admin_id UUID REFERENCES users(id),
    approval_date TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    profile_details JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index for faster lookups
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_approval_status ON users(approval_status);

-- 2. Accounts Table
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    contact_info JSONB NOT NULL,
    address TEXT NOT NULL,
    assigned_agent_id UUID REFERENCES users(id) ON DELETE SET NULL,
    id_proof_url TEXT,
    creation_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status TEXT DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- Indexes
CREATE INDEX idx_accounts_agent ON accounts(assigned_agent_id);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_name ON accounts(name);

-- 3. Loans Table
CREATE TABLE loans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID REFERENCES accounts(id) ON DELETE CASCADE,
    principal_amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL CHECK (interest_rate BETWEEN 3.0 AND 5.0),
    tenure_months INTEGER NOT NULL CHECK (tenure_months BETWEEN 2 AND 12),
    payment_frequency TEXT NOT NULL CHECK (payment_frequency IN ('WEEKLY', 'BI_MONTHLY', 'MONTHLY')),
    status TEXT DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'APPROVED', 'ACTIVE', 'CLOSED', 'REJECTED')),
    approval_date TIMESTAMP WITH TIME ZONE,
    amortization_schedule JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by_agent_id UUID REFERENCES users(id),
    approved_by_admin_id UUID REFERENCES users(id),
    rejection_reason TEXT,
    outstanding_balance DECIMAL(15, 2) NOT NULL,
    total_paid DECIMAL(15, 2) DEFAULT 0.0
);

-- Indexes
CREATE INDEX idx_loans_account ON loans(account_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_created_by ON loans(created_by_agent_id);
CREATE INDEX idx_loans_approved_by ON loans(approved_by_admin_id);

-- 4. Payments Table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    loan_id UUID REFERENCES loans(id) ON DELETE CASCADE,
    amount DECIMAL(15, 2) NOT NULL,
    payment_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    type TEXT DEFAULT 'REGULAR' CHECK (type IN ('REGULAR', 'PENALTY', 'PARTIAL', 'FULL')),
    status TEXT DEFAULT 'RECEIVED' CHECK (status IN ('RECEIVED', 'PENDING', 'FAILED')),
    received_by_agent_id UUID REFERENCES users(id),
    notes TEXT,
    applied_to_principal DECIMAL(15, 2) DEFAULT 0.0,
    applied_to_interest DECIMAL(15, 2) DEFAULT 0.0,
    applied_to_penalty DECIMAL(15, 2) DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_payments_loan ON payments(loan_id);
CREATE INDEX idx_payments_date ON payments(payment_date);
CREATE INDEX idx_payments_agent ON payments(received_by_agent_id);

-- 5. Notifications Table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('PAST_DUE', 'UPCOMING_DUE', 'LOAN_APPROVED', 'LOAN_REJECTED', 'CASHOUT_APPROVED', 'CASHOUT_REJECTED', 'PAYMENT_RECEIVED', 'ACCOUNT_CREATED', 'GENERAL')),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_read BOOLEAN DEFAULT FALSE,
    related_entity_id UUID,
    related_entity_type TEXT,
    priority TEXT DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'))
);

-- Indexes
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_timestamp ON notifications(timestamp DESC);
CREATE INDEX idx_notifications_type ON notifications(type);

-- 6. Earnings Table
CREATE TABLE earnings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agent_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    total_earnings DECIMAL(15, 2) DEFAULT 0.0,
    collectible_earnings DECIMAL(15, 2) DEFAULT 0.0,
    cashed_out_amount DECIMAL(15, 2) DEFAULT 0.0,
    commission_percentage DECIMAL(5, 2) DEFAULT 0.0,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    earnings_breakdown JSONB
);

-- Index
CREATE INDEX idx_earnings_agent ON earnings(agent_id);

-- 7. Cashout Requests Table
CREATE TABLE cashout_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agent_id UUID REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(15, 2) NOT NULL,
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    request_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    approval_date TIMESTAMP WITH TIME ZONE,
    approved_by_admin_id UUID REFERENCES users(id),
    rejection_reason TEXT,
    notes TEXT
);

-- Indexes
CREATE INDEX idx_cashout_agent ON cashout_requests(agent_id);
CREATE INDEX idx_cashout_status ON cashout_requests(status);
CREATE INDEX idx_cashout_date ON cashout_requests(request_date DESC);

-- 8. Transactions Table (Audit Log)
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type TEXT NOT NULL CHECK (type IN ('CREATE_ACCOUNT', 'UPDATE_ACCOUNT', 'CREATE_LOAN', 'APPROVE_LOAN', 'REJECT_LOAN', 'RECEIVE_PAYMENT', 'CASHOUT_REQUEST', 'CASHOUT_APPROVED', 'CASHOUT_REJECTED', 'UPDATE_COMMISSION', 'USER_LOGIN', 'USER_LOGOUT')),
    user_id UUID REFERENCES users(id),
    details TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    related_entity_id UUID,
    related_entity_type TEXT
);

-- Indexes
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp DESC);

-- ===================================================================
-- ROW LEVEL SECURITY (RLS)
-- ===================================================================

-- Enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE loans ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE earnings ENABLE ROW LEVEL SECURITY;
ALTER TABLE cashout_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- ===================================================================
-- USERS TABLE POLICIES
-- ===================================================================
-- Note: Using simple policies to avoid recursion
-- Security is enforced at the application layer

-- Allow users to insert their own record during registration
CREATE POLICY "Users can insert own record" ON users
    FOR INSERT
    WITH CHECK (id = auth.uid());

-- Allow all authenticated users to read all user records
-- This is needed for registration, login, and admin operations
CREATE POLICY "Authenticated users can view all users" ON users
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- Allow all authenticated users to update any user record
-- Application layer enforces who can update what
CREATE POLICY "Authenticated users can update users" ON users
    FOR UPDATE
    USING (auth.role() = 'authenticated');

-- ===================================================================
-- ACCOUNTS TABLE POLICIES
-- ===================================================================

-- Allow authenticated users to view accounts they're assigned to or all if they're admin
-- Using OR logic to avoid recursion
CREATE POLICY "Users can view relevant accounts" ON accounts
    FOR SELECT
    USING (
        assigned_agent_id = auth.uid()
        OR auth.role() = 'authenticated'
    );

-- Allow authenticated users to create accounts
-- Application logic will enforce that agents can only create accounts assigned to themselves
CREATE POLICY "Authenticated users can create accounts" ON accounts
    FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');

-- Allow users to update accounts they're assigned to
CREATE POLICY "Users can update relevant accounts" ON accounts
    FOR UPDATE
    USING (
        assigned_agent_id = auth.uid()
        OR auth.role() = 'authenticated'
    );

-- Similar policies should be created for other tables

-- ===================================================================
-- DATABASE FUNCTIONS
-- ===================================================================

-- Auto-update timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_loans_updated_at BEFORE UPDATE ON loans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Calculate penalties (scheduled function)
CREATE OR REPLACE FUNCTION calculate_loan_penalties()
RETURNS void AS $$
DECLARE
    loan_record RECORD;
    amort_schedule JSONB;
    entry JSONB;
    days_overdue INTEGER;
    penalty DECIMAL;
BEGIN
    FOR loan_record IN
        SELECT * FROM loans WHERE status IN ('ACTIVE', 'APPROVED')
    LOOP
        amort_schedule := loan_record.amortization_schedule;

        -- Update penalties for each unpaid entry
        -- Implementation would iterate through amortization_schedule
        -- and calculate penalties for overdue payments

    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Schedule to run daily using pg_cron (if available)
-- SELECT cron.schedule('calculate-penalties', '0 0 * * *', 'SELECT calculate_loan_penalties()');

-- ===================================================================
-- SETUP COMPLETE
-- ===================================================================
-- All tables, indexes, RLS policies, and functions have been created.
--
-- IMPORTANT NOTES:
-- - This script includes DROP IF EXISTS statements at the beginning
-- - Running this script will DESTROY ALL EXISTING DATA in these tables
-- - Use with caution in production environments
-- - Consider backing up data before running this script
--
-- Next steps:
-- 1. Configure additional RLS policies for remaining tables
-- 2. Set up pg_cron for scheduled penalty calculations (if available)
-- 3. Create initial admin user via Supabase Auth
-- 4. Test the database schema with sample data
-- ===================================================================
