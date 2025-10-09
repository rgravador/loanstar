# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Loanstar** is an Android lending mobile application designed for financial loan management with two user roles: Admin and Agent. The app is built using Kotlin and follows Android's modern architecture patterns with Navigation Components, ViewBinding, and MVVM architecture.

The application focuses on:
- Secure user session management with token-based authentication and automatic session refresh
- Robust network connectivity checking with offline mode support
- Data caching for offline access (e.g., viewing cached account lists)
- Background syncing when reconnection occurs
- Modern flat UI design following Material Design 3 principles
- User-friendly navigation with intuitive interactions

## Tech Stack

- **Language**: Kotlin 1.9.24
- **Min SDK**: 24 (Android 7.0)
- **Target/Compile SDK**: 34 (Android 14)
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: MVVM with Navigation Components
- **UI**: XML layouts with ViewBinding
- **Navigation**: Navigation Component with bottom navigation

## Key Dependencies

### Current Dependencies
- AndroidX Core KTX
- AndroidX Lifecycle (LiveData & ViewModel)
- Navigation Component (Fragment & UI)
- Material Design 3 Components
- ConstraintLayout

### Planned Dependencies
When implementing features, add these dependencies:
- Supabase Android SDK for backend integration
- Kotlin Coroutines and Flow for async operations
- Hilt/Dagger for dependency injection
- Room Database for offline caching and data persistence
- Firebase Cloud Messaging for push notifications
- PDF generation library (e.g., iText or Android PdfDocument) for amortization schedule exports
- Retrofit/OkHttp for network operations (if not using Supabase SDK directly)

## Build & Run Commands

### Build the project
```bash
./gradlew build
```

### Run tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest
```

### Clean build
```bash
./gradlew clean build
```

### Install debug APK
```bash
./gradlew installDebug
```

### Generate APK
```bash
# Debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Release APK
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

## Code Architecture

### Package Structure

```
com.example.loanstar/
├── MainActivity.kt              # Entry point with bottom navigation setup
└── ui/
    ├── home/
    │   ├── HomeFragment.kt
    │   └── HomeViewModel.kt
    ├── dashboard/
    │   ├── DashboardFragment.kt
    │   └── DashboardViewModel.kt
    └── notifications/
        ├── NotificationsFragment.kt
        └── NotificationsViewModel.kt
```

### Navigation Architecture

The app uses a single-Activity architecture with the Navigation Component:
- **MainActivity**: Hosts the NavHostFragment and BottomNavigationView
- **Bottom navigation**: Three top-level destinations (Home, Dashboard, Notifications)
- **Navigation graph**: Defined in `res/navigation/mobile_navigation.xml`
- **Top-level destinations**: Home, Dashboard, and Notifications are configured as top-level to prevent back stack issues

### MVVM Pattern

Each feature follows MVVM:
- **Fragment**: UI layer, observes ViewModel LiveData
- **ViewModel**: Business logic, exposes LiveData to the UI
- **Repository**: Data layer, handles Supabase API calls and Room database operations
- **ViewBinding**: Type-safe view access, binding lifecycle managed in fragments (nulled in `onDestroyView()`)

### Planned Package Structure

```
com.example.loanstar/
├── MainActivity.kt
├── data/
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── AccountRepository.kt
│   │   ├── LoanRepository.kt
│   │   ├── PaymentRepository.kt
│   │   └── EarningsRepository.kt
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   └── entities/
│   ├── remote/
│   │   ├── SupabaseClient.kt
│   │   └── dto/
│   └── model/
├── domain/
│   ├── usecase/
│   └── model/
├── ui/
│   ├── auth/
│   │   ├── LoginFragment.kt
│   │   ├── RegisterFragment.kt
│   │   └── AuthViewModel.kt
│   ├── agent/
│   │   ├── dashboard/
│   │   ├── accounts/
│   │   ├── loans/
│   │   ├── earnings/
│   │   └── notifications/
│   ├── admin/
│   │   ├── cashout/
│   │   ├── transactions/
│   │   ├── settings/
│   │   └── oversight/
│   └── common/
├── util/
│   ├── LoanCalculator.kt
│   ├── NetworkMonitor.kt
│   ├── DateUtils.kt
│   └── Extensions.kt
└── di/
    └── AppModule.kt
```

## Feature Requirements

### User Roles
- **Admin**: Male user persona, has all agent capabilities plus administrative functions
- **Agent**: Female user persona, manages borrower accounts and loans
- Both roles require registration/assignment during onboarding

### Database Schema (Supabase)

Design tables for:

1. **Users**
   - id, username, password_hash, role ('admin' or 'agent'), email, profile_details

2. **Accounts** (Borrower Profiles)
   - id, name, contact_info, address, assigned_agent_id, creation_date

3. **Loans**
   - id, account_id, principal_amount, interest_rate, tenure_months
   - payment_frequency ('bi-monthly'|'monthly'|'weekly')
   - status ('pending_approval'|'approved'|'active'|'closed')
   - approval_date, amortization_schedule_json

4. **Payments**
   - id, loan_id, amount, payment_date, type ('regular'|'penalty'), status ('received')

5. **Notifications**
   - id, user_id, message, type ('past_due'|'upcoming_due'), timestamp

6. **Earnings**
   - id, agent_id, total_earnings, collectible_earnings, cashed_out_amount, commission_percentage

7. **Cashout_Requests**
   - id, agent_id, amount, status ('pending'|'approved'|'rejected'), request_date, approval_date

8. **Transactions** (Audit Log)
   - id, type ('create_account'|'create_loan'|'receive_payment'|'cashout'), user_id, details_json, timestamp

### Authentication & Authorization
- Supabase Auth with username/password login
- Password reset functionality via email
- Optional 2FA enhancement
- Role-based access control (RBAC)
- Token-based session management with automatic refresh
- Logout on token expiration

### Agent Features

#### Dashboard
- Summary of assigned accounts
- Upcoming notifications
- Earnings overview

#### Account Management
- **Create**: Add borrower accounts with name, contact, address, optional ID proof upload
- **View**: Detailed profile with personal info, loan history, and status
- **Edit**: Update account details with audit logging
- **List**: Paginated list with search/filter by name, status, due dates; sortable columns

#### Loan Management
- **Create**: Select account, input principal, interest rate (3-5%), frequency (bi-monthly/monthly/weekly), tenure (2-12 months)
- **Approval**: New loans require admin approval
- **Amortization**: Auto-generate schedule showing due dates, principal, interest, penalties, remaining balance
- **View**: Interactive table/list of amortization schedule
- **Export**: Download as PDF or CSV
- **Payments**: Record payments with amount and date, update balance in real-time

#### Notifications
- Real-time push notifications via Firebase Cloud Messaging
- Alerts for past-due payments on assigned loans
- Notifications when due date is < 5 days away
- In-app notification center with history, mark as read, link to loan/account

#### Earnings & Cashout
- Commission calculation as percentage of interest collected (set by admin)
- Dashboard showing total earnings, collectible earnings, cashed out amount
- Breakdowns by loan or period
- Request cashout with validation (minimum threshold)

#### UI/UX for Agent
- Feminine-themed placeholders (icons/avatars)
- Bottom navigation: Dashboard, Accounts, Loans, Earnings, Notifications

### Admin Features

#### All Agent Functions
- Admins can perform all agent actions

#### Cashout Management
- View pending cashout requests
- Approve/reject requests with reasons
- Deduct approved amounts from collectible earnings

#### Transaction Oversight
- Comprehensive audit log of all transactions
- Filter by user, date, type
- Export reports

#### Commission Settings
- Set/update commission percentage per agent or globally
- Immediate effect on future calculations

#### Role Assignment
- Assign admin/agent roles during registration or via admin panel

#### Global Dashboard
- Total loans and outstanding balances
- Agent performance metrics (accounts managed, commissions paid)
- Ability to deactivate users or loans

#### UI/UX for Admin
- Masculine-themed placeholders (icons/avatars)
- Admin-specific sidebar or tabs for oversight

### Loan Calculation Rules

#### Interest
- 3-5% per month (simple interest)
- Rate selection with validation

#### Payment Frequencies
- **Bi-monthly**: Every 15 days
- **Monthly**: Every 30 days
- **Weekly**: Every 7 days
- Auto-generate due dates based on start date and frequency

#### Tenure
- Minimum: 2 months
- Maximum: 12 months
- Validation errors for invalid inputs

#### Amortization Formula
- EMI = P × r × (1+r)^n / ((1+r)^n - 1) for monthly payments
- Adapt formula for bi-monthly and weekly frequencies
- Columns: payment number, due date, principal due, interest due, total due, balance

#### Penalties
- 3% of due amortization amount per month
- Daily computation: (3% / 30) × days overdue
- Accumulate until paid
- Use Supabase cron jobs or triggers for automation

#### Approval Workflow
- Agent creates loan → marked as "pending approval"
- Notify admin via in-app/push notification
- Admin approves/rejects with reasons
- Only approved loans become active

## UI/UX Design Guidelines

### Modern Flat UI Requirements
- Implement clean, minimalist aesthetics
- Follow Material Design 3 principles with emphasis on simplicity and clarity
- User-friendly navigation with intuitive interactions
- Clear visual hierarchy with consistent spacing, typography, and color schemes
- Smooth transitions and animations
- Prioritize readability with proper contrast ratios and font sizes
- Design for accessibility:
  - Appropriate touch target sizes (minimum 48dp)
  - Screen reader support with content descriptions
  - Color blindness considerations
- Use meaningful icons and visual feedback for user actions
- Implement responsive layouts that adapt to different screen sizes

### Navigation Structure
- Bottom navigation for primary destinations (Agent view)
- Sidebar or tabs for admin-specific features
- Single-Activity architecture with Navigation Component

## Development Guidelines

### Database Schema Management

**CRITICAL: Always check and update `setup.sql` when working with database-related code.**

The `setup.sql` file in the project root is the single source of truth for the database schema. Follow these rules strictly:

1. **Before Creating/Updating Database Code**:
   - ALWAYS read `setup.sql` first to understand the current schema
   - Check table structures, column names, data types, constraints, and indexes
   - Verify foreign key relationships and RLS policies
   - Ensure your code matches the actual database schema

2. **When Adding New Database Objects**:
   - Update `setup.sql` immediately when adding:
     - New tables
     - New columns to existing tables
     - New indexes
     - New database functions
     - New triggers
     - New RLS policies
     - New views or stored procedures
   - Add clear comments explaining the purpose of new objects
   - Maintain the file's organization and structure

3. **When Modifying Database Objects**:
   - Update the corresponding section in `setup.sql`
   - Add ALTER statements if needed for migration purposes
   - Document breaking changes clearly
   - Update any dependent objects (views, functions, etc.)

4. **Synchronization**:
   - Keep Kotlin data models synchronized with `setup.sql`
   - Keep Room entities synchronized with `setup.sql`
   - Keep Supabase DTOs synchronized with `setup.sql`
   - Verify column names, types, and nullability match exactly

5. **Before Implementing Repositories**:
   - Reference `setup.sql` for accurate table and column names
   - Check constraints and validation rules
   - Understand relationships between tables
   - Review RLS policies to ensure proper data access

**Example Workflow**:
```
User requests: "Add a 'phone_number' field to accounts"
1. Read setup.sql to check accounts table structure
2. Update Kotlin Account data model
3. Update Room AccountEntity (if exists)
4. Update AccountRepository queries
5. Update setup.sql with the new column:
   ALTER TABLE accounts ADD COLUMN phone_number TEXT;
```

### ViewBinding Usage
Always initialize ViewBinding in fragments:
```kotlin
private var _binding: FragmentNameBinding? = null
private val binding get() = _binding!!

override fun onCreateView(...): View {
    _binding = FragmentNameBinding.inflate(inflater, container, false)
    return binding.root
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null  // Prevent memory leaks
}
```

### ViewModel Creation
Use `ViewModelProvider` for fragment-scoped ViewModels:
```kotlin
val viewModel = ViewModelProvider(this).get(YourViewModel::class.java)
```

### LiveData Observation
Observe LiveData with `viewLifecycleOwner` in fragments:
```kotlin
viewModel.data.observe(viewLifecycleOwner) { value ->
    // Update UI
}
```

### Dependency Management
All dependencies are managed in `gradle/libs.versions.toml` using version catalogs. When adding new dependencies:
1. Add version in `[versions]` section
2. Add library in `[libraries]` section
3. Reference in `app/build.gradle.kts` using `implementation(libs.library.name)`

### Network Connectivity
- Implement robust network connectivity checking
- Display offline mode alerts when connection is lost
- Cache data for offline access where feasible
- Sync changes upon reconnection
- Show loading states and retry mechanisms

### Error Handling
- Graceful handling of network errors
- Validation failures with clear user feedback
- Unauthorized access handling (redirect to login)
- Proper exception handling in coroutines

### Performance Optimization
- Implement lazy loading for lists
- Use pagination for large datasets
- Background syncing for offline changes
- Optimize image loading and caching
- Minimize memory leaks (proper lifecycle management)

## Testing

### Unit Tests
- Located in `app/src/test/`
- Test ViewModels and business logic
- Critical: Test all calculation logic thoroughly
  - Amortization calculations
  - Penalty calculations (daily computation)
  - Commission calculations
  - EMI formulas for different payment frequencies

### Instrumented Tests
- Located in `app/src/androidTest/`
- UI and integration tests
- Test Supabase API integration
- Test offline mode and sync functionality

### Test Coverage Requirements
- Loan calculation functions (100% coverage)
- Repository layer with mock Supabase responses
- ViewModel state management
- Navigation flows
- Form validation logic

## Important Notes

### Security Considerations

#### API Key Management
- Store Supabase API keys in `local.properties`, NEVER in version control
- Use BuildConfig for secure key access in code
- Add `local.properties` to `.gitignore`

#### Input Validation
- Validate all user inputs on client side
- Server-side validation via Supabase RLS policies
- Prevent SQL injection and XSS attacks
- Implement rate limiting on API calls

#### Data Protection
- Encrypt sensitive data before storing locally (Room encryption)
- Use Supabase Row Level Security (RLS) policies for database access control
- Secure token storage using Android KeyStore or EncryptedSharedPreferences
- Implement proper session timeout and auto-logout

#### Compliance
- GDPR-like data privacy handling
- Secure payment processing (currently focused on recording)
- Audit logging for all sensitive operations
- User consent for data collection

### General App Requirements

#### Onboarding
- Splash screen with branding
- Login/register flow with validation
- Role selection or assignment
- First-time user tutorial (optional)

#### Internationalization
- English as default language
- Prepare for localization (use string resources)
- Date/currency formatting based on locale

#### Deployment
- Generate APK for testing: `./gradlew assembleDebug`
- Generate AAB for Play Store: `./gradlew bundleRelease`
- Use Supabase environment variables via BuildConfig
- Implement proper versioning (versionCode, versionName)

### Real-time Features
- Use Supabase real-time subscriptions for:
  - Live notification updates
  - Loan status changes
  - Balance updates
  - Cashout request status changes
- Implement proper cleanup of subscriptions in lifecycle methods

### Offline Support
- Cache critical data in Room database:
  - User profile
  - Assigned accounts list
  - Active loans
  - Recent notifications
- Queue offline actions for sync:
  - Payment recordings
  - Account updates
  - Cashout requests
- Display sync status and pending changes to user
