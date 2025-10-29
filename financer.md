# Financer Web Application - Development Prompt

## Project Overview
Create a comprehensive lending management web application with mobile-first responsive design using Nuxt 3 with TypeScript, Vuetify 3, Pinia, Supabase, and tRPC. The application supports two user roles (Admin and Agent) and provides complete loan lifecycle management including account creation, loan processing, payment tracking, earnings management, and commission-based cashout functionality.

## Technology Stack Requirements

### Core Framework
- **Nuxt 3** (latest stable version) with TypeScript
- **Vue 3** with Composition API and `<script setup>` syntax
- **TypeScript** with strict mode enabled
- Mobile-first responsive design approach

### UI Framework
- **Vuetify 3** for Material Design components
- Custom theme configuration with primary/secondary colors
- Responsive breakpoints following mobile-first principles
- Dark mode support

### State Management
- **Pinia** for centralized state management
- Separate stores for:
  - User authentication state
  - Financial data (transactions, accounts, budgets)
  - UI state (theme, notifications, loading states)
- Persistent state using `pinia-plugin-persistedstate` where appropriate

### Backend & Database
- **Supabase** for:
  - PostgreSQL database
  - Authentication (email/password, OAuth providers)
  - Real-time subscriptions
  - Row Level Security (RLS) policies
  - Storage for receipts/documents

### API Layer
- **tRPC** for end-to-end type-safe API
- Server-side procedures in `/server/trpc/`
- Client-side integration with Nuxt
- Input validation using Zod schemas
- Error handling middleware

## Project Structure

```
/
├── assets/
│   ├── styles/
│   │   ├── variables.scss
│   │   └── main.scss
├── components/
│   ├── common/
│   ├── finance/
│   ├── auth/
│   └── layout/
├── composables/
│   ├── useAuth.ts
│   ├── useFinance.ts
│   └── useNotification.ts
├── layouts/
│   ├── default.vue
│   └── auth.vue
├── pages/
│   ├── index.vue
│   ├── dashboard.vue
│   ├── transactions.vue
│   ├── budgets.vue
│   ├── accounts.vue
│   └── auth/
│       ├── login.vue
│       └── signup.vue
├── plugins/
│   ├── vuetify.ts
│   └── supabase.ts
├── server/
│   ├── trpc/
│   │   ├── routers/
│   │   │   ├── auth.ts
│   │   │   ├── transactions.ts
│   │   │   ├── accounts.ts
│   │   │   └── budgets.ts
│   │   ├── context.ts
│   │   ├── trpc.ts
│   │   └── index.ts
│   └── middleware/
├── stores/
│   ├── auth.ts
│   ├── transactions.ts
│   ├── accounts.ts
│   └── ui.ts
├── types/
│   ├── database.ts
│   ├── finance.ts
│   └── index.ts
├── utils/
│   ├── validators.ts
│   ├── formatters.ts
│   └── constants.ts
├── nuxt.config.ts
├── tsconfig.json
└── package.json
```

## Detailed Implementation Requirements

### 1. Initial Setup & Configuration

#### package.json dependencies
```json
{
  "dependencies": {
    "nuxt": "latest",
    "vue": "^3.x",
    "vuetify": "^3.x",
    "pinia": "^2.x",
    "@pinia/nuxt": "latest",
    "pinia-plugin-persistedstate": "latest",
    "@supabase/supabase-js": "latest",
    "@trpc/server": "^10.x",
    "@trpc/client": "^10.x",
    "trpc-nuxt": "latest",
    "zod": "latest",
    "date-fns": "latest"
  },
  "devDependencies": {
    "@nuxtjs/supabase": "latest",
    "sass": "latest",
    "typescript": "latest"
  }
}
```

#### nuxt.config.ts
- Configure Vuetify module with custom theme
- Set up Supabase module with environment variables
- Configure tRPC integration
- Enable TypeScript strict mode
- Set up auto-imports for components and composables
- Configure build optimizations
- Set mobile viewport meta tags

#### Environment Variables (.env)
```
SUPABASE_URL=
SUPABASE_KEY=
SUPABASE_SERVICE_KEY=
```

### 2. Supabase Database Schema

Create tables with proper TypeScript types for the lending management system:

#### User Roles
- **Admin**: Full system access, manages cashouts, approves loans, oversees all transactions
- **Agent**: Creates accounts, manages loans, receives payments, requests cashouts

#### IMPORTANT: Table Naming Convention
- **DO NOT** use table names 'users' or 'profiles'
- **USE** 'users_profile' for user data table
- This avoids conflicts with Supabase's built-in auth.users table

#### Tables:
1. **users_profile** (extended user data from Supabase auth.users)
   - id (uuid, primary key, references auth.users(id))
   - username (text, unique)
   - email (text, unique)
   - role (enum: 'admin', 'agent')
   - full_name (text)
   - avatar_url (text, nullable)
   - is_active (boolean, default true)
   - created_at (timestamp)
   - updated_at (timestamp)

2. **accounts** (borrower profiles)
   - id (uuid, primary key)
   - assigned_agent_id (uuid, references users_profile.id)
   - name (text)
   - contact_info (text)
   - address (text)
   - id_proof_url (text, nullable) - stored in Supabase Storage
   - status (enum: 'active', 'inactive', 'suspended')
   - created_at (timestamp)
   - updated_at (timestamp)
   - created_by (uuid, references users_profile.id)

3. **loans**
   - id (uuid, primary key)
   - account_id (uuid, references accounts.id)
   - principal_amount (numeric)
   - interest_rate (numeric) - 3-5% per month
   - tenure_months (integer) - min 2, max 12
   - payment_frequency (enum: 'bi-monthly', 'monthly', 'weekly')
   - status (enum: 'pending_approval', 'approved', 'active', 'closed', 'rejected')
   - amortization_schedule (jsonb) - complete schedule with due dates, principal, interest
   - current_balance (numeric)
   - total_paid (numeric, default 0)
   - total_penalties (numeric, default 0)
   - approval_date (timestamp, nullable)
   - rejection_reason (text, nullable)
   - created_by (uuid, references users_profile.id)
   - approved_by (uuid, references users_profile.id, nullable)
   - start_date (date)
   - end_date (date)
   - created_at (timestamp)
   - updated_at (timestamp)

4. **payments**
   - id (uuid, primary key)
   - loan_id (uuid, references loans.id)
   - amount (numeric)
   - payment_date (date)
   - type (enum: 'regular', 'penalty', 'partial')
   - status (enum: 'received', 'pending', 'cancelled')
   - applied_to_principal (numeric)
   - applied_to_interest (numeric)
   - applied_to_penalty (numeric)
   - received_by (uuid, references users_profile.id)
   - notes (text, nullable)
   - created_at (timestamp)

5. **notifications**
   - id (uuid, primary key)
   - user_id (uuid, references users_profile.id)
   - loan_id (uuid, references loans.id, nullable)
   - account_id (uuid, references accounts.id, nullable)
   - message (text)
   - type (enum: 'past_due', 'upcoming_due', 'loan_approval', 'loan_rejection', 'cashout_approved', 'cashout_rejected', 'payment_received')
   - is_read (boolean, default false)
   - timestamp (timestamp)
   - created_at (timestamp)

6. **earnings**
   - id (uuid, primary key)
   - agent_id (uuid, references users_profile.id)
   - total_earnings (numeric, default 0)
   - collectible_earnings (numeric, default 0)
   - cashed_out_amount (numeric, default 0)
   - commission_percentage (numeric) - set by admin
   - created_at (timestamp)
   - updated_at (timestamp)

7. **cashout_requests**
   - id (uuid, primary key)
   - agent_id (uuid, references users_profile.id)
   - amount (numeric)
   - status (enum: 'pending', 'approved', 'rejected')
   - request_date (timestamp)
   - approval_date (timestamp, nullable)
   - approved_by (uuid, references users_profile.id, nullable)
   - rejection_reason (text, nullable)
   - created_at (timestamp)
   - updated_at (timestamp)

8. **transactions** (audit log)
   - id (uuid, primary key)
   - type (enum: 'create_account', 'update_account', 'create_loan', 'approve_loan', 'reject_loan', 'receive_payment', 'cashout_request', 'cashout_approved', 'cashout_rejected', 'commission_update')
   - user_id (uuid, references users_profile.id)
   - account_id (uuid, references accounts.id, nullable)
   - loan_id (uuid, references loans.id, nullable)
   - payment_id (uuid, references payments.id, nullable)
   - details (jsonb) - flexible details storage
   - timestamp (timestamp)
   - created_at (timestamp)

9. **penalties**
   - id (uuid, primary key)
   - loan_id (uuid, references loans.id)
   - amount (numeric)
   - reason (text) - e.g., "Late payment - 5 days overdue"
   - penalty_date (date)
   - is_paid (boolean, default false)
   - created_at (timestamp)

#### Row Level Security (RLS) Policies:
- Enable RLS on all tables
- **Agents**:
  - Can only view/edit accounts assigned to them (assigned_agent_id)
  - Can only create/view loans for their assigned accounts
  - Can only view their own earnings and cashout requests
  - Can view/update their own profile
- **Admins**:
  - Full access to all tables (SELECT, INSERT, UPDATE, DELETE)
  - Can view all accounts, loans, payments across all agents
  - Can manage cashout requests and approve/reject loans
  - Can update commission percentages
- **General**:
  - Users can only view notifications addressed to them
  - Audit logs (transactions table) are read-only for all users, write-only via triggers

### 3. tRPC Server Setup

#### server/trpc/trpc.ts
- Initialize tRPC with proper context
- Create authenticated and public procedures
- Set up error formatting
- Configure middleware for logging

#### server/trpc/context.ts
- Create context with Supabase client
- Extract user from session
- Type-safe context interface

#### server/trpc/routers/
Create routers for the lending management system:
- **auth.ts**: Login, signup, logout, session management, password reset, role-based access control
- **accounts.ts**: CRUD operations for borrower accounts, assignment to agents, search/filter
- **loans.ts**:
  - Create loan with validation (interest rate 3-5%, tenure 2-12 months)
  - Generate amortization schedule (bi-monthly, monthly, weekly frequencies)
  - Approve/reject loans (admin only)
  - View loan details and history
  - Calculate penalties (3% per month, daily computation)
- **payments.ts**:
  - Record payments
  - Apply to principal/interest/penalties
  - Update loan balance and earnings
  - Payment history
- **earnings.ts**:
  - Calculate agent commissions (percentage of interest collected)
  - View earnings dashboard (total, collectible, cashed out)
  - Update commission percentages (admin only)
- **cashouts.ts**:
  - Request cashout (agents)
  - Approve/reject cashout requests (admin)
  - Update collectible earnings on approval
- **notifications.ts**:
  - Real-time notifications for past-due (overdue) and upcoming-due (< 5 days)
  - Mark as read/unread
  - Notification history
- **transactions.ts**: Audit log viewing, filtering by type/user/date, export functionality
- **admin.ts**:
  - Global dashboard (total loans, outstanding balances, agent performance)
  - User management (activate/deactivate)
  - Role assignment
  - System-wide reports

Each router should:
- Use Zod for input validation with specific business rules
- Implement role-based authorization (admin vs agent)
- Implement proper error handling with descriptive messages
- Return type-safe responses
- Include pagination for list endpoints
- Log all mutations to transactions (audit) table

### 4. Pinia Stores

#### stores/auth.ts
```typescript
- State: user, session, role ('admin' | 'agent'), loading
- Actions: login, signup, logout, refreshSession, resetPassword
- Getters: isAuthenticated, userProfile, isAdmin, isAgent
- Persist: session info for auto-login
```

#### stores/accounts.ts (borrower accounts)
```typescript
- State: accounts[], selectedAccount, loading, filters
- Actions:
  - fetchAccounts (filtered by agent if not admin)
  - createAccount (auto-assign to current agent)
  - updateAccount
  - searchAccounts (by name, contact, status)
- Getters:
  - accountsByAgent
  - activeAccounts
  - accountsWithOverdueLoans
```

#### stores/loans.ts
```typescript
- State: loans[], selectedLoan, amortizationSchedule[], loading
- Actions:
  - createLoan (with amortization generation)
  - fetchLoans (by account, by status)
  - approveLoan (admin only)
  - rejectLoan (admin only)
  - updateLoanStatus
  - generateAmortizationSchedule
  - calculatePenalties
  - exportSchedule (PDF/CSV)
- Getters:
  - pendingApprovalLoans
  - activeLoans
  - overdueLoans
  - loansByAccount
  - totalOutstanding
```

#### stores/payments.ts
```typescript
- State: payments[], loading
- Actions:
  - recordPayment (update loan, earnings, notify)
  - fetchPaymentHistory
  - applyPaymentToLoan (principal, interest, penalties)
- Getters:
  - paymentsByLoan
  - recentPayments
  - totalPaymentsToday
```

#### stores/earnings.ts
```typescript
- State: earnings, loading
- Actions:
  - fetchEarnings
  - calculateCommission (on payment)
  - updateCommissionPercentage (admin only)
- Getters:
  - collectibleBalance
  - totalEarnings
  - cashedOutTotal
  - earningsByPeriod
```

#### stores/cashouts.ts
```typescript
- State: cashoutRequests[], loading
- Actions:
  - requestCashout (validation: min amount)
  - fetchCashoutRequests
  - approveCashout (admin only, update earnings)
  - rejectCashout (admin only, with reason)
- Getters:
  - pendingCashouts
  - cashoutHistory
  - agentCashouts (filtered by agent)
```

#### stores/notifications.ts
```typescript
- State: notifications[], unreadCount, loading
- Actions:
  - fetchNotifications
  - markAsRead
  - markAllAsRead
  - subscribeToRealtime (Supabase realtime)
- Getters:
  - unreadNotifications
  - notificationsByType
  - recentNotifications
```

#### stores/ui.ts
```typescript
- State: theme, sidebarOpen, loading states, snackbar messages
- Actions: setTheme, toggleSidebar, showSnackbar, hideSnackbar
- Persist: theme preference
```

#### stores/admin.ts
```typescript
- State: dashboardStats, agentPerformance[], loading
- Actions:
  - fetchDashboardStats (total loans, balances, agent metrics)
  - fetchAgentPerformance
  - deactivateUser
  - assignRole
  - generateReport
- Getters:
  - totalSystemBalance
  - totalActiveLoans
  - topPerformingAgents
```

### 5. Vuetify Configuration

#### plugins/vuetify.ts
- Custom theme with brand colors
- Mobile-first breakpoints
- Icon configuration (MDI)
- Default component props
- Dark mode configuration

Color scheme suggestion:
```typescript
primary: '#1976D2'    // Blue
secondary: '#424242'  // Grey
accent: '#82B1FF'     // Light Blue
error: '#FF5252'
info: '#2196F3'
success: '#4CAF50'
warning: '#FFC107'
```

### 6. Core Features Implementation

#### Authentication System
- Login page with username/password or email/password
- Sign up page with role selection (Admin/Agent) and validation
- Password reset flow via email
- Optional: Two-factor authentication (2FA)
- Role-based access control (RBAC) middleware
- Protected routes based on user role
- Session management with token refresh
- Auto-logout on token expiration
- Auto-redirect based on authentication state

#### Agent Dashboard Page
- **Overview Cards**:
  - Total assigned accounts
  - Active loans count
  - Total collectible earnings
  - Upcoming payments (next 7 days)
- **Recent Notifications** (past-due, upcoming-due)
- **Recent Payments** received
- **Quick Actions**: Add Account, Create Loan, Record Payment
- **Earnings Summary**: Chart showing earnings over time
- Responsive grid layout (12-column on desktop, stacked on mobile)

#### Admin Dashboard Page
- **System-Wide Overview Cards**:
  - Total loans outstanding
  - Total system balance
  - Number of agents
  - Pending approvals (loans, cashouts)
- **Agent Performance Metrics** (accounts managed, commissions earned)
- **Recent Transactions** (audit log)
- **Pending Actions**: Loans awaiting approval, cashout requests
- **Charts**: Loans by status, payments over time, agent performance
- Quick actions: Approve Loans, Manage Cashouts, View Reports

#### Accounts Page (Borrower Management)
- **For Agents**:
  - List of assigned accounts with search/filter
  - Add new borrower account form (name, contact, address, ID proof upload)
  - View account profile with loan history
  - Edit account details (with audit logging)
  - Account status indicators (active, inactive, suspended)
- **For Admins**:
  - View all accounts across all agents
  - Reassign accounts to different agents
  - Deactivate accounts
- Paginated table with sortable columns
- Mobile-optimized card view

#### Loans Page
- **Create Loan Form** (for assigned accounts):
  - Select account from dropdown
  - Input principal amount (validation)
  - Select interest rate (3-5% slider with validation)
  - Select payment frequency (bi-monthly, monthly, weekly)
  - Select tenure (2-12 months)
  - Auto-generate amortization schedule preview
  - Submit for approval (status: pending_approval)
- **Loans List**:
  - Filter by status (pending, approved, active, closed, rejected)
  - Search by account name or loan ID
  - Sortable columns (amount, start date, status, balance)
- **Loan Details View**:
  - Full amortization schedule table (payment #, due date, principal, interest, total due, balance)
  - Payment history
  - Penalties accumulated
  - Timeline of actions (creation, approval, payments)
  - Download schedule (PDF/CSV export)
- **Admin Loan Approval**:
  - Review pending loans
  - Approve or reject with reason
  - Notification sent to agent on decision

#### Payments Page
- **Record Payment Form**:
  - Select loan from dropdown (or from loan details)
  - Input amount received
  - Input payment date
  - Auto-calculate application to principal/interest/penalties
  - Add optional notes
  - Update loan balance immediately
  - Calculate and credit commission to agent earnings
- **Payment History**:
  - List of all payments with filters (by loan, date range, agent)
  - Payment details (amount, date, applied breakdown)
  - Export to CSV

#### Earnings Page (Agent Only)
- **Earnings Dashboard**:
  - Total earnings (lifetime)
  - Collectible earnings (available for cashout)
  - Total cashed out
  - Commission percentage (set by admin)
- **Earnings Breakdown**:
  - By loan
  - By period (daily, weekly, monthly)
  - Chart visualization
- **Cashout Section**:
  - Request cashout button (with minimum amount validation)
  - Cashout history (pending, approved, rejected)
  - Approval/rejection reasons displayed

#### Cashouts Page (Admin Only)
- **Pending Cashout Requests**:
  - List of pending requests from agents
  - Agent details, requested amount, date
  - Approve/Reject actions with reason input
  - Update collectible earnings on approval
- **Cashout History**:
  - All approved/rejected cashouts
  - Filter by agent, date range, status
  - Export report

#### Notifications Page
- **Notification Center**:
  - List of all notifications with filters (type, read/unread)
  - Real-time updates via Supabase subscriptions
  - Mark individual or all as read
  - Link to related loan/account
- **Notification Types**:
  - Past-due payment alerts (overdue loans)
  - Upcoming-due alerts (< 5 days to due date)
  - Loan approval/rejection
  - Cashout approval/rejection
  - Payment received confirmations
- Badge showing unread count in navigation

#### Transactions/Audit Log Page (Admin Only)
- **Complete Audit Log**:
  - All system transactions (create account, loan, payment, cashout, etc.)
  - Filter by user, type, date range
  - Search functionality
  - Detailed view showing full transaction details (JSON)
  - Export to CSV/PDF for reporting
- Timeline view for better visualization

#### Commission Settings Page (Admin Only)
- Set global commission percentage
- Set individual agent commission percentages
- View commission history changes
- Immediate effect on future earnings calculations

#### User Management Page (Admin Only)
- List of all users (admins and agents)
- Activate/deactivate users
- Assign/change roles
- View user activity and performance
- Create new user accounts

### 7. Components Architecture

#### Common Components
- **AppBar.vue**: Top navigation with user menu, role badge, theme toggle, notification bell with badge
- **NavigationDrawer.vue**: Side navigation with role-based menu items
- **BottomNavigation.vue**: Mobile bottom nav (Dashboard, Accounts, Loans, Earnings for Agent)
- **LoadingOverlay.vue**: Global loading state
- **ConfirmDialog.vue**: Reusable confirmation dialog
- **NotificationSnackbar.vue**: Toast notifications
- **StatCard.vue**: Reusable stat display card (used in dashboards)
- **DataTable.vue**: Reusable table with sorting, filtering, pagination
- **RoleBadge.vue**: Display user role with color coding

#### Lending-Specific Components
- **AccountCard.vue**: Display borrower account summary with loan count, status
- **AccountForm.vue**: Add/Edit borrower account with validation
- **LoanCard.vue**: Display loan summary (principal, balance, status, next due date)
- **LoanForm.vue**: Create loan with amortization preview
- **AmortizationTable.vue**: Display complete amortization schedule
- **PaymentForm.vue**: Record payment with breakdown calculation
- **PaymentCard.vue**: Display payment details
- **EarningsCard.vue**: Display earnings summary
- **CashoutRequestForm.vue**: Request cashout with validation
- **CashoutCard.vue**: Display cashout request with approval actions
- **NotificationCard.vue**: Display notification with action button
- **LoanApprovalCard.vue**: Admin loan approval interface
- **PenaltyCalculator.vue**: Display penalty calculations
- **CommissionSettings.vue**: Admin commission configuration
- **AgentPerformanceCard.vue**: Display agent metrics
- **AmountInput.vue**: Formatted currency input
- **DatePicker.vue**: Date selection component
- **InterestRateSlider.vue**: 3-5% slider with validation
- **PaymentFrequencySelector.vue**: Radio buttons for bi-monthly/monthly/weekly
- **ChartCard.vue**: Wrapper for charts (Chart.js for earnings, payments over time)

#### Form Validation
- Use Zod schemas shared with tRPC
- Real-time validation feedback
- Error message display
- Disabled submit until valid

### 8. Composables

#### useAuth.ts
```typescript
- Access auth store
- Provide login/logout/signup functions
- Check authentication status (isAuthenticated, isAdmin, isAgent)
- Handle session refresh and token expiration
- Role-based route guards
```

#### useAmortization.ts
```typescript
- Generate amortization schedule
- Calculate EMI/payment amounts
- Support bi-monthly, monthly, weekly frequencies
- Calculate remaining balance after payment
- Formulas:
  - Monthly EMI = P * r * (1+r)^n / ((1+r)^n - 1)
  - Adjust for bi-monthly (15 days) and weekly
```

#### usePenalties.ts
```typescript
- Calculate penalties for overdue payments
- 3% per month, daily computation (3% / 30 days)
- Accumulate penalties from due date to current date
- Apply to amortization amount
```

#### useCommissions.ts
```typescript
- Calculate agent commission on interest collected
- Apply commission percentage
- Update earnings on payment
```

#### useCurrency.ts
```typescript
- Currency formatting (configurable currency symbol)
- Parse currency input
- Format large numbers (K, M notation)
```

#### useNotification.ts
```typescript
- Show success/error/info/warning messages
- Queue notifications
- Auto-dismiss functionality
- Integrate with Vuetify snackbar
```

#### useRealtime.ts
```typescript
- Subscribe to Supabase realtime channels
- Handle notifications, loan updates, payment updates
- Reconnection logic
- Update stores on realtime events
```

#### useDateUtils.ts
```typescript
- Calculate due dates based on frequency
- Generate date range for amortization
- Check if payment is overdue
- Calculate days overdue
- Format dates for display
```

#### useExport.ts
```typescript
- Export amortization schedule to PDF
- Export data to CSV
- Export reports for admin
```

#### useTrpc.ts
```typescript
- Wrapper around tRPC client
- Global error handling
- Loading states
- Automatic retry on network errors
```

### 9. Mobile-First Responsive Design

#### Breakpoints Strategy
- **xs (< 600px)**: Single column, bottom navigation, compact cards
- **sm (600-960px)**: 2 columns for cards, side drawer
- **md (960-1264px)**: 3 columns, expanded details
- **lg (1264-1904px)**: 4 columns, full features
- **xl (> 1904px)**: Maximum width container

#### Mobile Optimizations
- Touch-friendly button sizes (min 48x48px)
- Swipe gestures for actions
- Bottom sheet for forms on mobile
- Sticky headers for lists
- Pull-to-refresh functionality
- Optimized images and lazy loading
- Virtual scrolling for long lists

#### Layout Components
- Use `v-container`, `v-row`, `v-col` for responsive grid
- Conditional rendering based on breakpoint
- Mobile-specific navigation
- Responsive typography scale

### 10. Real-time Features (Supabase)

#### Subscriptions
- **Notifications**: Real-time push for past-due, upcoming-due, loan approvals, cashout status
- **Loan Status Changes**: Updates when admin approves/rejects loans
- **Payment Updates**: Real-time balance updates when payments are recorded
- **Cashout Status**: Agents notified immediately on cashout approval/rejection
- **Multi-device Sync**: Keep data consistent across devices

#### Implementation
```typescript
- Set up Supabase realtime channels for each table (notifications, loans, payments, cashouts)
- Subscribe based on user role and ID (agents see only their data)
- Update Pinia stores on INSERT/UPDATE/DELETE events
- Handle connection status (show offline indicator)
- Automatic reconnection logic
- Unsubscribe on logout or component unmount
```

#### Automated Background Jobs (Supabase Edge Functions or Cron)
- **Daily Penalty Calculation**:
  - Run at end of day (e.g., 11:59 PM)
  - Calculate penalties for all overdue loans
  - 3% per month / 30 days = 0.1% per day on due amortization amount
  - Insert penalty records
  - Update loan total_penalties
- **Upcoming Due Notifications**:
  - Check for payments due within 5 days
  - Create notifications for assigned agents
- **Past-Due Notifications**:
  - Check for overdue payments
  - Create notifications for assigned agents

### 11. Business Rules & Loan Calculations

#### Loan Creation Rules
- **Interest Rate**: 3-5% per month (validate range)
- **Tenure**: Minimum 2 months, Maximum 12 months
- **Payment Frequency Options**:
  - **Bi-monthly**: Every 15 days (2 payments per month)
  - **Monthly**: Once per month
  - **Weekly**: Every 7 days
- **Start Date**: Default to loan approval date or custom date
- **Amortization**: Calculate using standard EMI formula, adapt for frequency
- **Initial Status**: All new loans start as "pending_approval"

#### Amortization Calculation
```typescript
// Monthly EMI Formula
EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)

Where:
- P = Principal amount
- r = Monthly interest rate (annual rate / 12 / 100)
- n = Number of payments (tenure * frequency)

// For bi-monthly: Calculate with adjusted rate and periods
// For weekly: Calculate with weekly rate (monthly rate / 4.33)

// Each payment breakdown:
- Interest for period = Remaining balance * period rate
- Principal for period = EMI - Interest for period
- Remaining balance = Previous balance - Principal for period
```

#### Penalty Calculation
```typescript
// Penalties for overdue payments
Penalty per day = (Due amortization amount * 3%) / 30

// Accumulate daily from due date until payment
Total penalty = Penalty per day * Days overdue

// Add to loan's total_penalties field
```

#### Commission Calculation
```typescript
// Agent earns commission on interest collected
Commission = Interest portion of payment * Commission percentage

// Update earnings:
- total_earnings += Commission
- collectible_earnings += Commission
```

#### Payment Application Logic
```typescript
// When payment is received, apply in order:
1. First apply to accumulated penalties
2. Then apply to interest due
3. Finally apply to principal

// Update loan balance immediately
// Credit commission to agent's earnings
```

### 12. Error Handling & Validation

#### Client-side Validation (Zod Schemas)
- **Loan Creation**:
  - Principal amount > 0
  - Interest rate between 3-5%
  - Tenure between 2-12 months
  - Valid payment frequency
- **Account Creation**:
  - Required fields: name, contact, address
  - Valid contact format (phone/email)
- **Payment Recording**:
  - Amount > 0
  - Amount <= loan balance + penalties
  - Valid date (not future date)
- **Cashout Request**:
  - Amount > minimum threshold (e.g., $10)
  - Amount <= collectible earnings
- User-friendly error messages
- Real-time validation feedback
- Disabled submit until valid

#### Server-side Validation & Error Handling
- tRPC error formatting with descriptive messages
- Database constraint errors (foreign keys, unique constraints)
- Authentication errors (401 Unauthorized)
- Authorization errors (403 Forbidden - wrong role)
- Business logic validation:
  - Cannot approve already approved loan
  - Cannot record payment for closed loan
  - Cannot cashout more than collectible earnings
  - Agents cannot access other agents' data
- Rate limiting on sensitive endpoints (login, cashout requests)
- Network error handling with retry logic

### 13. Performance Optimization

- Lazy load routes (separate chunks for admin/agent pages)
- Component code splitting
- Image optimization (ID proof uploads)
- Debounced search inputs (account search, loan search)
- Cached queries with tRPC
- Optimistic updates for better UX (update UI before server confirms)
- Virtual scrolling for long lists (accounts, loans, payments)
- Pagination for large datasets
- Compress and minify assets
- Service worker for offline support (cache static assets, show offline mode)

### 14. Testing Strategy

- **Unit Tests** (Vitest):
  - Amortization calculation functions
  - Penalty calculation logic
  - Commission calculation
  - Currency formatting
  - Date utilities
- **Component Tests**:
  - Form validation behavior
  - Role-based rendering
  - Button interactions
- **Integration Tests**:
  - tRPC endpoints
  - Supabase queries
  - Authentication flows
- **E2E Tests** (Playwright):
  - Complete loan creation workflow (create account -> create loan -> admin approval)
  - Payment recording workflow
  - Cashout request workflow
  - Login/logout flows
- **Type Checking**: TypeScript strict mode
- **Linting**: ESLint + Prettier

### 15. Security Best Practices

- **Environment Variables**: Store Supabase credentials securely (never commit to git)
- **HTTPS Only**: Enforce in production
- **Row Level Security (RLS)**: Strict policies on all Supabase tables
- **Input Sanitization**: Sanitize all user inputs to prevent SQL injection, XSS
- **Password Security**:
  - Use Supabase Auth with bcrypt hashing
  - Enforce strong password requirements
  - Password reset via email
- **Session Management**:
  - JWT tokens with expiration
  - Automatic refresh before expiration
  - Logout on token expiry
- **Role-Based Access Control (RBAC)**:
  - Middleware to check user role before rendering pages
  - Server-side validation of role permissions
- **Rate Limiting**: Limit login attempts, cashout requests, API calls
- **Audit Logging**: Log all critical actions to transactions table
- **File Upload Security**: Validate file types and sizes for ID proof uploads
- **CSRF Protection**: Use Nuxt's built-in CSRF tokens

### 16. Accessibility (a11y)

- Semantic HTML (proper heading hierarchy, landmarks)
- ARIA labels where needed (especially for icon buttons)
- Full keyboard navigation support
- Focus management (trap focus in dialogs)
- Color contrast ratios meeting WCAG AA standards
- Screen reader friendly (test with NVDA/JAWS)
- Proper form field labels and error announcements
- Skip to main content link
- Responsive text sizing

### 17. Development Workflow

#### Commands
```bash
npm run dev          # Development server with hot reload
npm run build        # Production build
npm run generate     # Static site generation (if applicable)
npm run preview      # Preview production build
npm run lint         # Lint code with ESLint
npm run lint:fix     # Auto-fix linting issues
npm run type-check   # TypeScript type checking
npm run test         # Run unit tests
npm run test:e2e     # Run E2E tests
```

#### Git Workflow
- Feature branches (feature/loan-creation, feature/cashout-management)
- Conventional commit messages (feat:, fix:, docs:, refactor:)
- Pull request templates with testing checklist
- Code review before merge

## Implementation Steps

### Phase 1: Project Foundation (Week 1)
1. **Project Initialization**
   - Create Nuxt 3 project with TypeScript template
   - Install all dependencies (Vuetify, Pinia, tRPC, Supabase, date-fns, etc.)
   - Configure nuxt.config.ts with modules and plugins
   - Set up environment variables (.env file)
   - Configure TypeScript (tsconfig.json)
   - Set up ESLint and Prettier

2. **Supabase Setup**
   - Create Supabase project
   - Design and create all database tables with proper relationships
   - Set up enums for role, loan status, payment type, etc.
   - Configure Row Level Security (RLS) policies for all tables
   - Generate TypeScript types from Supabase schema
   - Set up Supabase Storage bucket for ID proof uploads

3. **tRPC Configuration**
   - Set up tRPC server in /server/trpc/
   - Create context with Supabase client and user session
   - Build middleware for authentication and role checking
   - Create base routers (auth, accounts, loans, etc.)
   - Test endpoints with Postman or tRPC playground

### Phase 2: Authentication & Layout (Week 1-2)
4. **Vuetify & UI Setup**
   - Configure Vuetify plugin with custom theme
   - Create color scheme (primary, secondary, error, etc.)
   - Build layout components (AppBar, NavigationDrawer, BottomNavigation)
   - Set up role-based navigation menus
   - Create reusable components (StatCard, DataTable, ConfirmDialog)

5. **Authentication System**
   - Implement login page with form validation
   - Implement signup page with role selection
   - Set up auth store in Pinia
   - Configure middleware for protected routes
   - Implement password reset flow
   - Test complete auth flow (login, logout, session refresh)

### Phase 3: Agent Features (Week 2-3)
6. **Account Management (Borrower Accounts)**
   - Create account list page with search/filter
   - Build AccountForm component for add/edit
   - Implement file upload for ID proof (Supabase Storage)
   - Set up account store in Pinia
   - Add pagination and sorting
   - Test CRUD operations

7. **Loan Creation & Management**
   - Build LoanForm component with validation
   - Implement amortization calculation composable
   - Create AmortizationTable component
   - Build loan list page with filters
   - Implement loan details view
   - Add PDF/CSV export functionality
   - Test amortization calculations for all frequencies

8. **Payment Recording**
   - Build PaymentForm component
   - Implement payment application logic (penalties, interest, principal)
   - Create payment history view
   - Update loan balance in real-time
   - Calculate and credit commissions to agent
   - Test payment workflows

9. **Earnings & Cashouts (Agent)**
   - Build earnings dashboard
   - Create CashoutRequestForm
   - Display cashout history
   - Implement commission calculation
   - Test earnings updates on payments

### Phase 4: Admin Features (Week 3-4)
10. **Admin Dashboard**
    - Build system-wide overview cards
    - Create agent performance metrics
    - Display pending approvals
    - Add charts for visualizations (Chart.js)

11. **Loan Approval System**
    - Build loan approval interface
    - Implement approve/reject actions
    - Send notifications on approval/rejection
    - Test approval workflow

12. **Cashout Management**
    - Build cashout approval interface
    - Implement approve/reject with earnings update
    - Create cashout history view
    - Test cashout workflow

13. **Admin Tools**
    - Build transaction audit log page
    - Create commission settings interface
    - Implement user management (activate/deactivate, role assignment)
    - Add report generation and export

### Phase 5: Real-time & Notifications (Week 4)
14. **Real-time Features**
    - Set up Supabase realtime subscriptions
    - Implement notification system
    - Create notification center UI
    - Add real-time updates for loans, payments, cashouts
    - Test multi-device sync

15. **Automated Jobs**
    - Create Supabase Edge Function for daily penalty calculation
    - Set up cron job for upcoming-due notifications
    - Set up cron job for past-due notifications
    - Test automated notifications

### Phase 6: Polish & Testing (Week 5)
16. **Mobile Responsiveness**
    - Test on various screen sizes
    - Optimize for mobile (bottom nav, card views)
    - Ensure touch-friendly interactions
    - Test offline mode

17. **Performance Optimization**
    - Implement lazy loading
    - Add pagination where needed
    - Optimize images
    - Enable caching
    - Test load times

18. **Testing**
    - Write unit tests for calculations
    - Write component tests
    - Write E2E tests for critical flows
    - Fix bugs discovered during testing

19. **Security Audit**
    - Review RLS policies
    - Test role-based access control
    - Validate input sanitization
    - Test rate limiting

### Phase 7: Deployment (Week 5-6)
20. **Deployment Preparation**
    - Set up production Supabase project
    - Configure environment variables for production
    - Build and test production bundle
    - Set up CI/CD pipeline

21. **Deployment**
    - Deploy to Vercel or Netlify
    - Configure custom domain (if applicable)
    - Set up monitoring and error tracking (Sentry)
    - Monitor performance and fix issues

22. **Documentation**
    - Write user guide for agents
    - Write admin guide
    - Document API endpoints
    - Create deployment guide

## Expected Deliverables

1. **Fully Functional Lending Management Web Application**
   - Mobile-first responsive design
   - Role-based access (Admin and Agent)
   - Complete loan lifecycle management

2. **Core Features**
   - User authentication with role-based access control
   - Borrower account management
   - Loan creation with amortization schedule generation
   - Loan approval workflow (admin)
   - Payment recording with automatic balance updates
   - Penalty calculation (automated)
   - Commission tracking and earnings management
   - Cashout request system
   - Real-time notifications
   - Comprehensive audit logging

3. **Technical Implementation**
   - Type-safe API with tRPC and Zod validation
   - Supabase backend with RLS policies
   - Pinia state management
   - Vuetify 3 Material Design UI
   - Real-time data synchronization
   - Offline support (basic caching)

4. **Documentation**
   - User guides (Agent and Admin)
   - API documentation
   - Deployment guide
   - Database schema documentation

5. **Testing**
   - Unit tests for business logic
   - Component tests
   - E2E tests for critical workflows

6. **Production Deployment**
   - Deployed to Vercel/Netlify
   - Environment variables configured
   - Performance monitoring enabled

## Code Quality Standards

- **TypeScript Strict Mode**: No `any` types, full type safety
- **ESLint + Prettier**: Consistent code formatting
- **Naming Conventions**:
  - PascalCase for components (LoanForm.vue)
  - camelCase for functions, variables (calculateAmortization)
  - UPPER_SNAKE_CASE for constants (MAX_TENURE_MONTHS)
- **Comments**: Document complex business logic (especially calculations)
- **Reusable Code**: DRY principles, shared composables and components
- **SOLID Principles**: Single responsibility, separation of concerns
- **Comprehensive Error Handling**: Try-catch blocks, user-friendly error messages
- **Accessibility**: WCAG AA compliance
- **Security**: Input validation, sanitization, RLS policies
- **Performance**: Lazy loading, pagination, optimized queries

## Additional Notes

### Prioritization
1. **User Experience**: Intuitive navigation, clear feedback, smooth interactions
2. **Security**: Protect user data, enforce role-based access
3. **Performance**: Fast load times, responsive interactions
4. **Mobile-First**: Ensure excellent mobile experience
5. **Maintainability**: Clean, documented, testable code

### Best Practices
- Follow Vue 3 Composition API patterns with `<script setup>`
- Use Vuetify 3 components for consistent Material Design
- Leverage Supabase realtime for live updates
- Implement optimistic UI updates for better UX
- Use Pinia for centralized state management
- Keep components small and focused (single responsibility)
- Optimize bundle size with code splitting
- Use semantic versioning for releases

### Business Logic Validation
- Always validate loan parameters (interest rate 3-5%, tenure 2-12 months)
- Ensure amortization calculations are accurate for all frequencies
- Verify penalty calculations match specification (3% per month, daily)
- Confirm commission calculations are correct
- Test payment application logic thoroughly (penalties, then interest, then principal)

### Loan-Specific Requirements Summary
- **Interest Rate**: 3-5% per month (simple interest)
- **Tenure**: 2-12 months
- **Payment Frequencies**: Bi-monthly (15 days), Monthly, Weekly (7 days)
- **Penalties**: 3% per month of due amortization, calculated daily (3% / 30)
- **Commission**: Percentage of interest collected (set by admin per agent)
- **Approval Workflow**: All loans start as "pending_approval", require admin action
- **Payment Priority**: Penalties → Interest → Principal

---

## CRITICAL REMINDERS

**Important**: Generate production-ready code with:
1. **Proper error handling** at all levels (client, server, database)
2. **Full type safety** with TypeScript strict mode
3. **Security first** approach (RLS, validation, sanitization)
4. **Mobile-first** responsive design
5. **Accurate business logic** (amortization, penalties, commissions)
6. **Real-time updates** via Supabase subscriptions
7. **Role-based access control** enforced on both client and server
8. **Comprehensive audit logging** for all critical actions
9. **User-friendly error messages** and loading states
10. **Best practices** for Nuxt 3, Vue 3, TypeScript, Vuetify 3, Pinia, Supabase, and tRPC

**Testing is Critical**: Especially for:
- Amortization calculations (all frequencies)
- Penalty calculations (daily accumulation)
- Commission calculations
- Payment application logic
- Role-based access restrictions
- Real-time synchronization

**UI/UX Focus**:
- Agent interface should be intuitive and efficient for daily use
- Admin interface should provide clear oversight and control
- Mobile experience should be smooth and touch-friendly
- Notifications should be timely and actionable
- Loading states and error messages should be clear

---

This comprehensive specification should enable Claude Code to build a complete, production-ready lending management web application with all the features and business logic from the Android requirements adapted for web.
