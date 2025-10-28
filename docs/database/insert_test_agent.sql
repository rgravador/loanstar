-- ===================================================================
-- INSERT TEST PENDING AGENT
-- Run this script in Supabase SQL Editor to create a test pending agent
-- ===================================================================

-- Insert a pending agent for testing admin approval flow
-- Note: This creates a user in the database only, not in Supabase Auth
-- The user must register through the app first, or you need to create them in Auth

-- Option 1: Insert directly into users table (for testing dashboard display)
-- This will show in the dashboard but won't be able to login until created in Auth
INSERT INTO users (
    id,
    username,
    email,
    role,
    approval_status,
    approved_by_admin_id,
    approval_date,
    rejection_reason,
    profile_details,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'jane_doe',
    'jane.doe@example.com',
    'agent',
    'PENDING',
    NULL,
    NULL,
    NULL,
    '{"fullName": "Jane Doe", "phone": "+639123456789", "address": "123 Main St, Manila"}'::jsonb,
    NOW(),
    NOW()
);

-- Insert another pending agent
INSERT INTO users (
    id,
    username,
    email,
    role,
    approval_status,
    approved_by_admin_id,
    approval_date,
    rejection_reason,
    profile_details,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'maria_santos',
    'maria.santos@example.com',
    'agent',
    'PENDING',
    NULL,
    NULL,
    NULL,
    '{"fullName": "Maria Santos", "phone": "+639987654321", "address": "456 Rizal Ave, Quezon City"}'::jsonb,
    NOW(),
    NOW()
);

-- Insert an approved agent (for comparison)
INSERT INTO users (
    id,
    username,
    email,
    role,
    approval_status,
    approved_by_admin_id,
    approval_date,
    rejection_reason,
    profile_details,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'john_reyes',
    'john.reyes@example.com',
    'agent',
    'APPROVED',
    NULL,
    NOW(),
    NULL,
    '{"fullName": "John Reyes", "phone": "+639111222333", "address": "789 Bonifacio St, Makati"}'::jsonb,
    NOW(),
    NOW()
);

-- Verify the inserted agents
SELECT
    id,
    username,
    email,
    role,
    approval_status,
    created_at
FROM users
WHERE role = 'agent'
ORDER BY created_at DESC;
