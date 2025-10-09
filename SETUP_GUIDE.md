# Loanstar Setup & Implementation Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Project Setup](#project-setup)
3. [Supabase Database Setup](#supabase-database-setup)
4. [Firebase Setup](#firebase-setup)
5. [Building the App](#building-the-app)
6. [Implementation Status](#implementation-status)
7. [Next Steps](#next-steps)

## Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK with API 24-34
- Supabase account
- Firebase account

## Project Setup

### 1. Clone and Configure

1. Open the project in Android Studio
2. Copy `local.properties.sample` to `local.properties`
3. Update `local.properties` with your credentials:

```properties
sdk.dir=/path/to/your/android/sdk
supabase.url=https://your-project-id.supabase.co
supabase.anon.key=your-anon-key-here
```

### 2. Sync Gradle

```bash
./gradlew build
```

## Supabase Database Setup

### Create Tables

Run these SQL commands in your Supabase SQL editor:

#### 1. Users Table

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('admin', 'agent')),
    profile_details JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index for faster lookups
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

#### 2. Accounts Table

```sql
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
```

#### 3. Loans Table

```sql
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
```

#### 4. Payments Table

```sql
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
```

#### 5. Notifications Table

```sql
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
```

#### 6. Earnings Table

```sql
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
```

#### 7. Cashout Requests Table

```sql
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
```

#### 8. Transactions Table (Audit Log)

```sql
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
```

### Set Up Row Level Security (RLS)

Enable RLS on all tables and create policies:

```sql
-- Enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE loans ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE earnings ENABLE ROW LEVEL SECURITY;
ALTER TABLE cashout_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- Example policies for accounts table
-- Agents can only see their assigned accounts
CREATE POLICY "Agents see own accounts" ON accounts
    FOR SELECT
    USING (assigned_agent_id = auth.uid());

-- Admins can see all accounts
CREATE POLICY "Admins see all accounts" ON accounts
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM users
            WHERE users.id = auth.uid()
            AND users.role = 'admin'
        )
    );

-- Similar policies should be created for other tables
```

### Create Database Functions

#### Auto-update timestamps

```sql
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
```

#### Calculate penalties (scheduled function)

```sql
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
```

## Firebase Setup

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project
3. Add an Android app
4. Register with package name: `com.example.loanstar`

### 2. Download google-services.json

1. Download the `google-services.json` file
2. Place it in `app/` directory

### 3. Enable Cloud Messaging

1. In Firebase Console, go to Cloud Messaging
2. Enable the Cloud Messaging API

### 4. Configure FCM Tokens

The app will handle FCM token registration automatically.

## Building the App

### Debug Build

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Install on Device

```bash
./gradlew installDebug
```

### Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Implementation Status

### ✅ Completed Components

1. **Project Dependencies**
   - All Gradle dependencies configured
   - Version catalog set up
   - Plugins configured (Hilt, KSP, Serialization)

2. **Supabase Integration**
   - SupabaseClientProvider created
   - Auth, Database, Realtime, Storage modules configured
   - BuildConfig integration for secure credential storage

3. **Data Models**
   - User, Account, Loan, Payment models
   - Notification, Earnings, CashoutRequest models
   - Transaction model for audit logging
   - All enums and supporting classes

4. **Utilities**
   - LoanCalculator with EMI, amortization, penalty calculations
   - NetworkMonitor for connectivity tracking
   - Extension functions for formatting and validation

5. **Dependency Injection**
   - Hilt setup with Application class
   - AppModule for providing dependencies

### ⏳ Pending Components

1. **Room Database**
   - Entity classes
   - DAOs for each entity
   - AppDatabase configuration
   - Type converters

2. **Repositories**
   - AuthRepository
   - AccountRepository
   - LoanRepository
   - PaymentRepository
   - NotificationRepository
   - EarningsRepository
   - CashoutRepository
   - TransactionRepository

3. **UI Components**
   - Authentication screens (Login, Register)
   - Agent screens (Dashboard, Accounts, Loans, Earnings)
   - Admin screens (Dashboard, Cashout Management, Transactions)
   - Common components (Dialogs, Lists, Forms)

4. **ViewModels**
   - Auth ViewModels
   - Agent ViewModels
   - Admin ViewModels

5. **Services**
   - FCM Service for push notifications
   - Sync Service for offline data

6. **Testing**
   - Unit tests for LoanCalculator
   - Repository tests
   - ViewModel tests
   - UI tests

## Next Steps

### For Developers

1. **Complete Room Database Setup**
   - Create entity classes with Room annotations
   - Implement DAOs with suspend functions
   - Set up type converters for complex types
   - Configure AppDatabase with migrations

2. **Implement Repositories**
   - Create repository interfaces
   - Implement with Supabase integration
   - Add Room caching layer
   - Implement offline sync logic

3. **Build UI Screens**
   - Create fragment layouts
   - Implement ViewModels
   - Set up navigation graphs
   - Add bottom navigation for Agent
   - Add drawer/tabs for Admin

4. **Implement Authentication**
   - Login screen with validation
   - Register screen with role assignment
   - Password reset functionality
   - Session management

5. **Add Business Logic**
   - Loan approval workflow
   - Payment processing
   - Earnings calculation
   - Cashout approval process

6. **Testing**
   - Write unit tests for calculations
   - Test repositories with mock data
   - UI tests for critical flows

### For Designers

1. Create Material Design 3 themes
2. Design icons and assets
3. Create user personas (feminine for Agent, masculine for Admin)
4. Design empty states and error states
5. Create loading animations

### For Project Managers

1. Set up Supabase project and database
2. Configure Firebase project
3. Set up CI/CD pipeline
4. Plan feature releases
5. Coordinate testing phases

## Troubleshooting

### Build Errors

1. **Supabase credentials not found**
   - Ensure `local.properties` exists
   - Verify credentials are correct
   - Clean and rebuild project

2. **Hilt annotation errors**
   - Ensure all modules are properly annotated
   - Check that Application class is annotated with @HiltAndroidApp
   - Rebuild project

3. **Room schema errors**
   - Export Room schema to verify structure
   - Check for missing migrations
   - Clear app data and reinstall

### Runtime Errors

1. **Network connection issues**
   - Verify Supabase URL is correct
   - Check API key validity
   - Ensure device has internet connection

2. **Authentication failures**
   - Verify Supabase Auth is enabled
   - Check email confirmation settings
   - Verify RLS policies

## Resources

- [Supabase Kotlin Documentation](https://supabase.com/docs/reference/kotlin/introduction)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Material Design 3](https://m3.material.io/)

## Support

For issues and questions:
1. Check this guide first
2. Review CLAUDE.md for code guidelines
3. Check IMPLEMENTATION_PROGRESS.md for current status
4. Consult the development team
