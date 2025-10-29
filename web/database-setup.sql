-- LoanStar Database Setup
-- Execute this SQL in your Supabase SQL Editor

-- Create user roles enum
CREATE TYPE user_role AS ENUM ('admin', 'agent');
CREATE TYPE account_status AS ENUM ('active', 'inactive', 'suspended');
CREATE TYPE loan_status AS ENUM ('pending_approval', 'approved', 'active', 'closed', 'rejected');
CREATE TYPE payment_frequency AS ENUM ('bi-monthly', 'monthly', 'weekly');
CREATE TYPE payment_type AS ENUM ('regular', 'penalty', 'partial');
CREATE TYPE payment_status AS ENUM ('received', 'pending', 'cancelled');
CREATE TYPE notification_type AS ENUM ('past_due', 'upcoming_due', 'loan_approval', 'loan_rejection', 'cashout_approved', 'cashout_rejected', 'payment_received');
CREATE TYPE cashout_status AS ENUM ('pending', 'approved', 'rejected');
CREATE TYPE transaction_type AS ENUM ('create_account', 'update_account', 'create_loan', 'approve_loan', 'reject_loan', 'receive_payment', 'cashout_request', 'cashout_approved', 'cashout_rejected', 'commission_update');

-- 1. users_profile table
CREATE TABLE users_profile (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  username TEXT UNIQUE NOT NULL,
  email TEXT UNIQUE NOT NULL,
  role user_role NOT NULL DEFAULT 'agent',
  full_name TEXT NOT NULL,
  avatar_url TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. accounts table (borrower profiles)
CREATE TABLE accounts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  assigned_agent_id UUID REFERENCES users_profile(id) NOT NULL,
  name TEXT NOT NULL,
  contact_info TEXT NOT NULL,
  address TEXT NOT NULL,
  id_proof_url TEXT,
  status account_status DEFAULT 'active',
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  created_by UUID REFERENCES users_profile(id) NOT NULL
);

-- 3. loans table
CREATE TABLE loans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  account_id UUID REFERENCES accounts(id) NOT NULL,
  principal_amount NUMERIC(12, 2) NOT NULL,
  interest_rate NUMERIC(5, 2) NOT NULL CHECK (interest_rate >= 3 AND interest_rate <= 5),
  tenure_months INTEGER NOT NULL CHECK (tenure_months >= 2 AND tenure_months <= 12),
  payment_frequency payment_frequency NOT NULL,
  status loan_status DEFAULT 'pending_approval',
  amortization_schedule JSONB NOT NULL,
  current_balance NUMERIC(12, 2) NOT NULL,
  total_paid NUMERIC(12, 2) DEFAULT 0,
  total_penalties NUMERIC(12, 2) DEFAULT 0,
  approval_date TIMESTAMPTZ,
  rejection_reason TEXT,
  created_by UUID REFERENCES users_profile(id) NOT NULL,
  approved_by UUID REFERENCES users_profile(id),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. payments table
CREATE TABLE payments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  loan_id UUID REFERENCES loans(id) NOT NULL,
  amount NUMERIC(12, 2) NOT NULL,
  payment_date DATE NOT NULL,
  type payment_type DEFAULT 'regular',
  status payment_status DEFAULT 'received',
  applied_to_principal NUMERIC(12, 2) NOT NULL,
  applied_to_interest NUMERIC(12, 2) NOT NULL,
  applied_to_penalty NUMERIC(12, 2) DEFAULT 0,
  received_by UUID REFERENCES users_profile(id) NOT NULL,
  notes TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. notifications table
CREATE TABLE notifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users_profile(id) NOT NULL,
  loan_id UUID REFERENCES loans(id),
  account_id UUID REFERENCES accounts(id),
  message TEXT NOT NULL,
  type notification_type NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  timestamp TIMESTAMPTZ DEFAULT NOW(),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. earnings table
CREATE TABLE earnings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  agent_id UUID REFERENCES users_profile(id) NOT NULL UNIQUE,
  total_earnings NUMERIC(12, 2) DEFAULT 0,
  collectible_earnings NUMERIC(12, 2) DEFAULT 0,
  cashed_out_amount NUMERIC(12, 2) DEFAULT 0,
  commission_percentage NUMERIC(5, 2) NOT NULL DEFAULT 5.0,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. cashout_requests table
CREATE TABLE cashout_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  agent_id UUID REFERENCES users_profile(id) NOT NULL,
  amount NUMERIC(12, 2) NOT NULL,
  status cashout_status DEFAULT 'pending',
  request_date TIMESTAMPTZ DEFAULT NOW(),
  approval_date TIMESTAMPTZ,
  approved_by UUID REFERENCES users_profile(id),
  rejection_reason TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. transactions table (audit log)
CREATE TABLE transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  type transaction_type NOT NULL,
  user_id UUID REFERENCES users_profile(id) NOT NULL,
  account_id UUID REFERENCES accounts(id),
  loan_id UUID REFERENCES loans(id),
  payment_id UUID REFERENCES payments(id),
  details JSONB,
  timestamp TIMESTAMPTZ DEFAULT NOW(),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. penalties table
CREATE TABLE penalties (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  loan_id UUID REFERENCES loans(id) NOT NULL,
  amount NUMERIC(12, 2) NOT NULL,
  reason TEXT NOT NULL,
  penalty_date DATE NOT NULL,
  is_paid BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE users_profile ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE loans ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE earnings ENABLE ROW LEVEL SECURITY;
ALTER TABLE cashout_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE penalties ENABLE ROW LEVEL SECURITY;

-- RLS Policies for users_profile
CREATE POLICY "Users can view own profile" ON users_profile FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON users_profile FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Admins can view all profiles" ON users_profile FOR SELECT USING (
  EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin')
);

-- RLS Policies for accounts
CREATE POLICY "Agents can view assigned accounts" ON accounts FOR SELECT USING (
  assigned_agent_id = auth.uid() OR
  EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin')
);
CREATE POLICY "Agents can create accounts" ON accounts FOR INSERT WITH CHECK (
  assigned_agent_id = auth.uid()
);
CREATE POLICY "Agents can update assigned accounts" ON accounts FOR UPDATE USING (
  assigned_agent_id = auth.uid() OR
  EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin')
);

-- RLS Policies for loans
CREATE POLICY "Users can view loans for their accounts" ON loans FOR SELECT USING (
  EXISTS (
    SELECT 1 FROM accounts
    WHERE accounts.id = loans.account_id
    AND (accounts.assigned_agent_id = auth.uid() OR
         EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin'))
  )
);
CREATE POLICY "Agents can create loans for assigned accounts" ON loans FOR INSERT WITH CHECK (
  EXISTS (SELECT 1 FROM accounts WHERE id = account_id AND assigned_agent_id = auth.uid())
);
CREATE POLICY "Admins can update loans" ON loans FOR UPDATE USING (
  EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin')
);

-- RLS Policies for payments
CREATE POLICY "Users can view payments for their loans" ON payments FOR SELECT USING (
  EXISTS (
    SELECT 1 FROM loans
    JOIN accounts ON loans.account_id = accounts.id
    WHERE loans.id = payments.loan_id
    AND (accounts.assigned_agent_id = auth.uid() OR
         EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin'))
  )
);
CREATE POLICY "Agents can create payments" ON payments FOR INSERT WITH CHECK (
  EXISTS (
    SELECT 1 FROM loans
    JOIN accounts ON loans.account_id = accounts.id
    WHERE loans.id = loan_id AND accounts.assigned_agent_id = auth.uid()
  )
);

-- RLS Policies for notifications
CREATE POLICY "Users can view own notifications" ON notifications FOR SELECT USING (user_id = auth.uid());
CREATE POLICY "Users can update own notifications" ON notifications FOR UPDATE USING (user_id = auth.uid());

-- RLS Policies for earnings
CREATE POLICY "Agents can view own earnings" ON earnings FOR SELECT USING (
  agent_id = auth.uid() OR
  EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin')
);
CREATE POLICY "Admins can update earnings" ON earnings FOR UPDATE USING (
  EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin')
);

-- RLS Policies for cashout_requests
CREATE POLICY "Agents can view own cashout requests" ON cashout_requests FOR SELECT USING (
  agent_id = auth.uid() OR
  EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin')
);
CREATE POLICY "Agents can create cashout requests" ON cashout_requests FOR INSERT WITH CHECK (agent_id = auth.uid());
CREATE POLICY "Admins can update cashout requests" ON cashout_requests FOR UPDATE USING (
  EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin')
);

-- RLS Policies for transactions (read-only for users)
CREATE POLICY "Users can view relevant transactions" ON transactions FOR SELECT USING (
  user_id = auth.uid() OR
  EXISTS (SELECT 1 FROM users_profile WHERE id = auth.uid() AND role = 'admin')
);

-- Create indexes for better performance
CREATE INDEX idx_accounts_agent ON accounts(assigned_agent_id);
CREATE INDEX idx_loans_account ON loans(account_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_payments_loan ON payments(loan_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_earnings_agent ON earnings(agent_id);
CREATE INDEX idx_cashout_agent ON cashout_requests(agent_id);
CREATE INDEX idx_cashout_status ON cashout_requests(status);
CREATE INDEX idx_transactions_user ON transactions(user_id);

-- Function to automatically create earnings record for new agents
CREATE OR REPLACE FUNCTION create_agent_earnings()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.role = 'agent' THEN
    INSERT INTO earnings (agent_id, commission_percentage)
    VALUES (NEW.id, 5.0);
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_create_agent_earnings
AFTER INSERT ON users_profile
FOR EACH ROW
EXECUTE FUNCTION create_agent_earnings();

-- Function to update timestamps
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_profile_updated_at BEFORE UPDATE ON users_profile
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_accounts_updated_at BEFORE UPDATE ON accounts
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_loans_updated_at BEFORE UPDATE ON loans
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_earnings_updated_at BEFORE UPDATE ON earnings
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_cashout_requests_updated_at BEFORE UPDATE ON cashout_requests
FOR EACH ROW EXECUTE FUNCTION update_updated_at();
