# Loanstar iOS App

A modern iOS lending application for loan management with Admin and Agent roles.

## Overview

Loanstar iOS is built using SwiftUI and follows modern iOS architecture patterns. The app connects to Supabase for backend services and includes features like:

- User authentication with role-based access (Admin/Agent)
- Account (borrower) management
- Loan creation with automatic amortization schedules
- Payment tracking and recording
- Earnings tracking for agents
- Cashout requests
- Real-time notifications
- Offline support with local caching

## Requirements

- iOS 17.0+
- Xcode 15.0+
- Swift 5.9+
- Active Supabase project

## Project Structure

```
Loanstar/
├── Models/                 # Data models
│   ├── User.swift
│   ├── Account.swift
│   ├── Loan.swift
│   ├── Payment.swift
│   ├── Earnings.swift
│   ├── Notification.swift
│   ├── CashoutRequest.swift
│   └── Transaction.swift
├── Repositories/           # Data layer (to be implemented)
│   ├── AuthRepository.swift
│   ├── AccountRepository.swift
│   ├── LoanRepository.swift
│   └── ...
├── ViewModels/            # Business logic (to be implemented)
│   ├── AuthViewModel.swift
│   ├── HomeViewModel.swift
│   └── ...
├── Views/                 # UI components (to be implemented)
│   ├── Auth/
│   ├── Dashboard/
│   ├── Home/
│   ├── Loans/
│   ├── Profile/
│   └── Notifications/
├── Utilities/             # Helper classes
│   ├── LoanCalculator.swift
│   ├── DateUtils.swift
│   ├── NetworkMonitor.swift
│   ├── Result.swift
│   └── Extensions.swift
├── Services/              # External services
│   └── SupabaseClient.swift
└── Config/                # Configuration
    └── AppConfig.swift
```

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Loanstar/ios
```

### 2. Install Dependencies

The project uses Swift Package Manager for dependencies. Dependencies will be automatically resolved when you open the project in Xcode.

Main dependencies:
- Supabase Swift SDK (2.0.0+)

### 3. Configure Supabase

Update `Loanstar/Config/AppConfig.swift` with your Supabase credentials:

```swift
struct AppConfig {
    static let supabaseURL = "YOUR_SUPABASE_URL"
    static let supabaseAnonKey = "YOUR_SUPABASE_ANON_KEY"
}
```

### 4. Database Setup

Ensure your Supabase database has the required tables. Use the SQL script located at:
```
../docs/database/setup.sql
```

Required tables:
- users
- accounts
- loans
- payments
- earnings
- notifications
- cashout_requests
- transactions

### 5. Open in Xcode

```bash
open Loanstar.xcodeproj
```

or use:
```bash
xed .
```

### 6. Build and Run

1. Select a simulator or connected device
2. Press Cmd+R to build and run
3. The app will launch on your selected device

## Architecture

### MVVM Pattern

The app follows the Model-View-ViewModel (MVVM) pattern:

- **Models**: Data structures (User, Loan, Account, etc.)
- **Views**: SwiftUI views for UI
- **ViewModels**: Business logic and state management using `@ObservableObject`

### Data Flow

```
View → ViewModel → Repository → Supabase/Local Storage
                                      ↓
View ← ViewModel ← Repository ← Response Data
```

### Key Features Implementation

#### 1. Loan Calculator

The `LoanCalculator` utility handles all loan-related calculations:

```swift
// Generate amortization schedule
let schedule = LoanCalculator.generateAmortizationSchedule(
    principal: 10000,
    monthlyInterestRate: 4.0,
    tenureMonths: 6,
    paymentFrequency: .monthly
)

// Calculate penalty
let penalty = LoanCalculator.calculatePenalty(
    dueAmount: 1500,
    daysOverdue: 10
)
```

**Key Calculations:**
- EMI Formula: `EMI = P × r × (1+r)^n / ((1+r)^n - 1)`
- Penalty: 3% per month, calculated daily as `(3% / 30) × days overdue`
- Interest Rates: 3-5% per month (simple interest)
- Tenure: 2-12 months

#### 2. Date Utilities

Extensive date manipulation functions:

```swift
// Get date boundaries
let startOfMonth = DateUtils.getStartOfMonth()
let endOfWeek = DateUtils.getEndOfWeek()

// Date arithmetic
let futureDate = DateUtils.addDays(to: Date(), days: 30)
let daysBetween = DateUtils.daysBetween(startDate, endDate)

// Formatting
let relativeTime = DateUtils.formatRelativeTime(dueDate) // "In 3 days"
```

#### 3. Network Monitoring

Monitor connectivity for offline support:

```swift
class MyView: View {
    @StateObject private var networkMonitor = NetworkMonitor()

    var body: some View {
        VStack {
            if !networkMonitor.isConnected {
                Text("Offline Mode")
            }
        }
    }
}
```

#### 4. Data Models

All models support Codable for JSON serialization and include helpful extensions:

```swift
let user = User(
    id: UUID().uuidString,
    username: "john_doe",
    email: "john@example.com",
    role: .agent,
    approvalStatus: .pending
)

print(user.displayName)  // Uses full name or username
print(user.initials)     // "JD"
print(user.isApproved)   // false
```

## Business Rules

### Loan Parameters

- **Interest Rate**: 3-5% per month (simple interest)
- **Tenure**: 2-12 months
- **Payment Frequencies**:
  - Weekly: Every 7 days
  - Bi-Monthly: Every 15 days
  - Monthly: Every 30 days

### Penalties

- **Rate**: 3% of due amount per month
- **Calculation**: Daily basis = (3% / 30) × days overdue
- **Application**: Automatically calculated for overdue payments

### User Roles

#### Agent
- Create and manage borrower accounts
- Create loan requests (requires admin approval)
- Record payments
- View earnings and request cashouts
- Receive notifications

#### Admin
- All agent capabilities
- Approve/reject loan requests
- Approve/reject agent registrations
- Approve/reject cashout requests
- View global statistics and reports
- Manage commission rates

### Approval Workflows

#### Agent Registration
1. User registers with role=AGENT
2. Status set to PENDING
3. Admin reviews and approves/rejects
4. Agent notified of decision

#### Loan Creation
1. Agent creates loan (status=PENDING_APPROVAL)
2. Admin receives notification
3. Admin reviews loan details
4. Admin approves (status=ACTIVE) or rejects
5. Agent notified of decision

## Development

### Running Tests

```bash
# Run all tests
xcodebuild test -scheme Loanstar -destination 'platform=iOS Simulator,name=iPhone 15 Pro'

# Or use Xcode
Cmd+U
```

### Code Style

- Follow Swift API Design Guidelines
- Use meaningful variable and function names
- Add documentation comments for public APIs
- Use SwiftLint for code quality (optional)

### Git Workflow

```bash
# Create a feature branch
git checkout -b feature/loan-approval

# Make changes and commit
git add .
git commit -m "feat: implement loan approval workflow"

# Push to remote
git push origin feature/loan-approval
```

## Troubleshooting

### Build Errors

**Issue**: Package dependencies not resolved
```bash
# Solution: Reset package cache
File → Packages → Reset Package Caches
```

**Issue**: Code signing errors
```bash
# Solution: Select your development team in project settings
```

### Runtime Errors

**Issue**: Supabase connection fails
```bash
# Solution: Check your AppConfig.swift credentials
# Verify Supabase project is running
```

**Issue**: Date parsing errors
```bash
# Solution: Ensure date formats match between client and server
# Use ISO 8601 format for consistency
```

## Next Steps

### Phase 1: Repository Layer (In Progress)
- [ ] Implement AuthRepository
- [ ] Implement AccountRepository
- [ ] Implement LoanRepository
- [ ] Implement PaymentRepository
- [ ] Implement EarningsRepository
- [ ] Implement NotificationRepository

### Phase 2: ViewModels
- [ ] Implement AuthViewModel
- [ ] Implement HomeViewModel
- [ ] Implement DashboardViewModel
- [ ] Implement LoansViewModel
- [ ] Implement ProfileViewModel
- [ ] Implement NotificationsViewModel

### Phase 3: UI Components
- [ ] Authentication screens
- [ ] Dashboard screens
- [ ] Loan management screens
- [ ] Profile screens
- [ ] Notification screens

### Phase 4: Advanced Features
- [ ] Offline support with local caching
- [ ] Push notifications
- [ ] PDF export for amortization schedules
- [ ] Real-time updates

## Resources

- [Implementation Guide](../docs/iOS_IMPLEMENTATION_GUIDE.md) - Comprehensive implementation documentation
- [Supabase Swift Docs](https://supabase.com/docs/reference/swift/introduction)
- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)
- [Swift Package Manager](https://www.swift.org/package-manager/)

## License

Copyright © 2025 Loanstar. All rights reserved.

## Support

For issues and questions:
- Create an issue in the repository
- Contact the development team
- Check the implementation guide for detailed documentation
