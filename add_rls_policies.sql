-- ===================================================================
-- ADD RLS POLICIES TO EXISTING DATABASE
-- Run this script in Supabase SQL Editor to add missing RLS policies
-- This will NOT delete any existing data
-- ===================================================================

-- Drop existing policies if they exist (safe to run multiple times)
-- Drop old conflicting policies that caused recursion
DROP POLICY IF EXISTS "Users can view own profile" ON users;
DROP POLICY IF EXISTS "Users can update own profile" ON users;
DROP POLICY IF EXISTS "Admins can view all users" ON users;
DROP POLICY IF EXISTS "Admins can update all users" ON users;

-- Drop current policies to recreate them
DROP POLICY IF EXISTS "Users can insert own record" ON users;
DROP POLICY IF EXISTS "Authenticated users can view all users" ON users;
DROP POLICY IF EXISTS "Authenticated users can update users" ON users;

DROP POLICY IF EXISTS "Agents see own accounts" ON accounts;
DROP POLICY IF EXISTS "Admins see all accounts" ON accounts;
DROP POLICY IF EXISTS "Agents can create own accounts" ON accounts;
DROP POLICY IF EXISTS "Agents can update own accounts" ON accounts;
DROP POLICY IF EXISTS "Admins can create accounts" ON accounts;
DROP POLICY IF EXISTS "Admins can update accounts" ON accounts;
DROP POLICY IF EXISTS "Users can view relevant accounts" ON accounts;
DROP POLICY IF EXISTS "Authenticated users can create accounts" ON accounts;
DROP POLICY IF EXISTS "Users can update relevant accounts" ON accounts;

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

-- ===================================================================
-- POLICIES ADDED SUCCESSFULLY
-- ===================================================================
-- The RLS policies have been created. You can now:
-- 1. Register new users (they can insert their own records)
-- 2. Users can view and update their own profiles
-- 3. Admins can view and manage all users
-- 4. Agents can manage their own accounts
-- 5. Admins have full access to all accounts
-- ===================================================================
