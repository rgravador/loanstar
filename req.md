# Lending Mobile App Development

Create a comprehensive lending mobile app for Android using Supabase as the backend database and authentication service. The app should prioritize secure user session management, including token-based authentication with automatic session refresh and logout on token expiration. Implement robust network connectivity checking, displaying offline mode alerts, caching data for offline access where feasible (e.g., viewing cached account lists), and syncing changes upon reconnection.

## Android Native Requirements
- **Platform**: Android Native Application
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Backend**: Supabase for database, authentication, and real-time features
- **Development Environment**: Android Studio
- **Build Output**: Generate APK and AAB files for Android

## Development Framework Selection
Native Android Implementation:
- Use Kotlin as the primary programming language
- Follow Android's modern architecture patterns (MVVM, Navigation Components)
- Use ViewBinding for type-safe view access
- Implement Material Design 3 components for UI
- Use Room Database for offline caching and data persistence
- Navigation using Navigation Component with bottom navigation
- State management using LiveData and ViewModel from Android Architecture Components
- Coroutines and Flow for asynchronous operations

## Code Base Structure
- Standard Android project structure with Gradle Kotlin DSL
- Package organization by feature (home, dashboard, notifications, etc.)
- Separation of concerns: UI (Fragments), ViewModel (business logic), Repository (data layer)
- Comprehensive build instructions for Android Studio setup
- Environment configuration for Supabase integration using BuildConfig and local.properties

## User Roles and Assumptions
- There are two primary user roles: **Admin** (assumed to be a male user for any persona-based references or placeholders) and **Agent** (assumed to be a female user for any persona-based references or placeholders).
- All users (admins and agents) must register or be assigned roles during onboarding.
- Authentication: Use Supabase Auth for secure login with username and password. Include password reset functionality via email, two-factor authentication (2FA) as an optional enhancement, and role-based access control (RBAC) to restrict features based on user role.
- Database Schema: Design a Supabase schema with tables for:
  - Users (id, username, password_hash, role: 'admin' or 'agent', email, profile_details).
  - Accounts (borrower profiles: id, name, contact_info, address, assigned_agent_id, creation_date, etc.).
  - Loans (id, account_id, principal_amount, interest_rate, tenure_months, payment_frequency: 'bi-monthly'|'monthly'|'weekly', status: 'pending_approval'|'approved'|'active'|'closed', approval_date, amortization_schedule_json).
  - Payments (id, loan_id, amount, payment_date, type: 'regular'|'penalty', status: 'received').
  - Notifications (id, user_id, message, type: 'past_due'|'upcoming_due', timestamp).
  - Earnings (id, agent_id, total_earnings, collectible_earnings, cashed_out_amount, commission_percentage).
  - Cashout_Requests (id, agent_id, amount, status: 'pending'|'approved'|'rejected', request_date, approval_date).
  - Transactions (id, type: 'create_account'|'create_loan'|'receive_payment'|'cashout'|'etc.', user_id, details_json, timestamp).
- Use Supabase's real-time subscriptions for live updates on notifications, loan statuses, and balances.
- Security: Encrypt sensitive data (e.g., personal info), validate all inputs to prevent SQL injection or XSS, and implement rate limiting on API calls.

## Agent Features
- **Login**: Agents log in securely with username and password. Upon successful login, fetch and display a dashboard summarizing accounts, upcoming notifications, and earnings.
- **Account Management**:
  - Add new borrower accounts: Form to input details like name, contact, address, ID proof (optional upload), and automatically assign to the current agent.
  - View account profiles: Detailed view of any account assigned to the agent, including personal info, loan history, and status.
  - Edit accounts: Allow updates to account details (e.g., contact info) for assigned accounts, with audit logging.
  - List all accounts: Paginated list of accounts under the agent's name, with search/filter by name, status, or due dates. Include sortable columns for last activity, total loans, etc.
- **Notifications**:
  - Real-time push notifications (using Firebase Cloud Messaging integrated with Supabase triggers) for past-due payments on assigned loans.
  - Notifications when a payment due date is less than 5 days away.
  - In-app notification center to view history, mark as read, and link to relevant loan/account.
- **Loan Creation and Management**:
  - Create new loans from the list of assigned accounts: Select account, input principal amount, select interest rate (3-5% per month, with validation), payment frequency (bi-monthly, monthly, weekly), and tenure (min 2 months, max 12 months).
  - Automatically generate an amortization schedule (using a Kotlin library or custom function for calculations, stored as JSON in Supabase).
  - New loans are labeled as "pending approval" and require admin approval before activation.
  - View amortization schedule: Interactive table or list showing due dates, principal, interest, penalties (if any), and remaining balance for assigned loans.
  - View history: Timeline of all actions on a loan/account (e.g., creation, payments, edits).
  - Download amortization schedule: Export as PDF or CSV via device sharing.
  - Receive payments: Form to record payments (amount, date), update loan balance immediately in Supabase, apply to principal/interest/penalties, and trigger real-time updates.
- **Earnings and Cashout**:
  - Agents earn commissions as a percentage (set by admin) of the interest collected on payments.
  - View earnings dashboard: Display total earnings, collectible earnings (un-cashed), and total commission cashed out. Include breakdowns by loan or period.
  - Request cashout: Form to request withdrawal of collectible earnings, with validation (e.g., min amount threshold).
- **UI/UX for Agent**: Feminine-themed placeholders (e.g., icons or avatars) if assuming agent is a girl. Ensure intuitive navigation with bottom tabs for Dashboard, Accounts, Loans, Earnings, Notifications.

## Loan-Specific Rules
- **Interest**: 3-5% per month, compounded or simple as per standard lending practices (specify simple interest unless otherwise).
- **Payment Collection**: Options for bi-monthly (every 15 days), monthly, or weekly schedules. Auto-generate due dates based on start date and frequency.
- **Tenure**: Enforce minimum 2 months, maximum 12 months, with validation errors for invalid inputs.
- **Amortization Schedule**: Calculate using standard formulas (e.g., EMI = P * r * (1+r)^n / ((1+r)^n - 1) for monthly, adapt for others). Include columns for payment number, due date, principal due, interest due, total due, balance.
- **Penalties**: For past-due accounts, apply 3% of the due amortization amount per month, divided by 30, computed daily at end-of-day (use Supabase cron jobs or triggers for automation). Accumulate penalties until paid.
- **Approval Workflow**: After agent creation, notify admin via in-app/push. Admin approves/rejects with reasons.

## Admin Features
- **All Agent Functions**: Admins can perform all agent actions, including managing their own assigned accounts if any.
- **Cashout Management**: View pending cashout requests from agents, approve/reject, and deduct approved amounts from agent's collectible earnings immediately upon approval.
- **Transaction Oversight**: View a comprehensive audit log of all system transactions (e.g., account creation, loan creation, payments received, cashouts). Filter by user, date, type; export as report.
- **Commission Settings**: Set or update commission percentage for individual agents or globally, with immediate effect on future calculations.
- **Role Assignment**: Assign roles (agent or admin) to new or existing user accounts during registration or via admin panel.
- **Additional Admin Tools**: Global dashboard showing total loans, outstanding balances, agent performance metrics (e.g., accounts managed, commissions paid). Ability to deactivate users or loans.
- **UI/UX for Admin**: Masculine-themed placeholders (e.g., icons or avatars) if assuming admin is a man. Admin-specific sidebar or tabs for oversight features.

## General App Requirements
- **UI/UX Design**:
  - Implement a modern flat UI design with clean, minimalist aesthetics
  - Follow Material Design 3 principles with emphasis on simplicity and clarity
  - Ensure user-friendly navigation with intuitive interactions and clear visual hierarchy
  - Use consistent spacing, typography, and color schemes throughout the app
  - Implement smooth transitions and animations for better user experience
  - Prioritize readability with proper contrast ratios and font sizes
  - Design for accessibility (touch targets, screen readers, color blindness considerations)
  - Use meaningful icons and visual feedback for user actions
  - Implement responsive layouts that adapt to different screen sizes
- **Onboarding**: Splash screen, login/register flow, role selection or assignment.
- **Error Handling**: Graceful handling of network errors, validation failures, unauthorized access (redirect to login).
- **Performance**: Optimize for mobile with lazy loading, pagination for lists, and background syncing.
- **Testing**: Include unit tests for calculations (e.g., amortization, penalties), integration tests for Supabase APIs.
- **Compliance**: Ensure data privacy (e.g., GDPR-like handling), secure payment processing (integrate with a gateway if needed, but focus on recording for now).
- **Internationalization**: Support English as default; prepare for localization.
- **Deployment**: Guidelines for building APK and AAB (Android App Bundle) files, integrating Supabase environment variables securely using BuildConfig.

Generate the full app code structure, including main activities, fragments/composables, Supabase integration code, and any necessary helper functions. Provide explanations for key implementations, such as amortization calculation and real-time features.