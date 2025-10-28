# iOS Implementation Guide - Loanstar App

## Overview
This document provides a comprehensive mapping from the Android implementation to iOS, detailing all features, data models, repositories, ViewModels, and UI components that need to be implemented in the iOS version of Loanstar.

**Source Analysis Date:** October 28, 2025
**Android Package:** `com.example.loanstar`
**iOS Bundle:** To be determined

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Data Models](#data-models)
4. [Repository Layer](#repository-layer)
5. [ViewModels / View State Management](#viewmodels--view-state-management)
6. [UI Components](#ui-components)
7. [Utilities](#utilities)
8. [Features Implementation](#features-implementation)
9. [Implementation Checklist](#implementation-checklist)

---

## Architecture Overview

### Android Architecture (Current)
- **Pattern:** MVVM (Model-View-ViewModel)
- **DI Framework:** Dagger Hilt
- **Navigation:** Android Navigation Component with Bottom Navigation
- **Database:** Room (local caching)
- **Backend:** Supabase (Auth, Postgrest, Realtime, Storage)
- **Concurrency:** Kotlin Coroutines and Flow
- **UI:** XML layouts with ViewBinding

### iOS Architecture (Recommended)
- **Pattern:** MVVM with SwiftUI
- **DI Framework:** Swift Dependency Injection (protocol-based) or Resolver
- **Navigation:** SwiftUI NavigationStack with TabView
- **Database:** SwiftData or Core Data (local caching)
- **Backend:** Supabase Swift SDK (Auth, Postgrest, Realtime, Storage)
- **Concurrency:** Swift Async/Await and Combine
- **UI:** SwiftUI

---

## Technology Stack

### Required Swift Packages
```swift
// Package.swift dependencies
dependencies: [
    .package(url: "https://github.com/supabase/supabase-swift", from: "2.0.0"),
    // For network monitoring
    .package(url: "https://github.com/ashleymills/Reachability.swift", from: "5.0.0"),
]
```

### Core Technologies
1. **SwiftUI** - Declarative UI framework
2. **Combine** - Reactive programming (alternative to Flow)
3. **Async/Await** - Asynchronous operations (alternative to Coroutines)
4. **SwiftData/Core Data** - Local persistence
5. **Supabase Swift SDK** - Backend services

---

## Data Models

### 1. User Models
**Android Location:** `data/model/User.kt`

```swift
// iOS Implementation: Models/User.swift

enum UserRole: String, Codable {
    case admin = "ADMIN"
    case agent = "AGENT"
}

enum ApprovalStatus: String, Codable {
    case pending = "PENDING"
    case approved = "APPROVED"
    case rejected = "REJECTED"
}

struct User: Identifiable, Codable {
    let id: String
    let username: String
    let email: String
    let role: UserRole
    let approvalStatus: ApprovalStatus
    let approvedByAdminId: String?
    let approvalDate: Date?
    let rejectionReason: String?
    let profileDetails: ProfileDetails?
    let createdAt: Date
    let updatedAt: Date
}

struct ProfileDetails: Codable {
    let fullName: String?
    let phone: String?
    let avatarUrl: String?
    let address: String?
    let dateOfBirth: String?
}
```

### 2. Account Models
**Android Location:** `data/model/Account.kt`

```swift
// iOS Implementation: Models/Account.swift

enum AccountStatus: String, Codable {
    case active = "ACTIVE"
    case inactive = "INACTIVE"
    case suspended = "SUSPENDED"
}

struct Account: Identifiable, Codable {
    let id: String
    let name: String
    let contactInfo: ContactInfo
    let address: String
    let assignedAgentId: String
    let idProofUrl: String?
    let creationDate: Date
    let updatedAt: Date
    let status: AccountStatus
}

struct ContactInfo: Codable {
    let phone: String
    let email: String?
    let alternatePhone: String?
}
```

### 3. Loan Models
**Android Location:** `data/model/Loan.kt`

```swift
// iOS Implementation: Models/Loan.swift

enum LoanStatus: String, Codable {
    case pendingApproval = "PENDING_APPROVAL"
    case approved = "APPROVED"
    case active = "ACTIVE"
    case closed = "CLOSED"
    case rejected = "REJECTED"
}

enum PaymentFrequency: String, Codable {
    case weekly = "WEEKLY"          // Every 7 days
    case biMonthly = "BI_MONTHLY"   // Every 15 days
    case monthly = "MONTHLY"        // Every 30 days
}

struct Loan: Identifiable, Codable {
    let id: String
    let accountId: String
    let principalAmount: Double
    let interestRate: Double        // 3-5% per month
    let tenureMonths: Int          // 2-12 months
    let paymentFrequency: PaymentFrequency
    let status: LoanStatus
    let approvalDate: Date?
    let amortizationSchedule: [AmortizationEntry]
    let createdAt: Date
    let updatedAt: Date
    let createdByAgentId: String
    let approvedByAdminId: String?
    let rejectionReason: String?
    let outstandingBalance: Double
    let totalPaid: Double
}

struct AmortizationEntry: Identifiable, Codable {
    var id: String { "\(paymentNumber)" }
    let paymentNumber: Int
    let dueDate: Date
    let principalDue: Double
    let interestDue: Double
    let totalDue: Double
    let remainingBalance: Double
    let penalty: Double
    let isPaid: Bool
    let paidAmount: Double
    let paidDate: Date?
}
```

### 4. Payment Models
**Android Location:** `data/model/Payment.kt`

```swift
// iOS Implementation: Models/Payment.swift

enum PaymentType: String, Codable {
    case regular = "REGULAR"
    case penalty = "PENALTY"
    case partial = "PARTIAL"
    case full = "FULL"
}

enum PaymentStatus: String, Codable {
    case received = "RECEIVED"
    case pending = "PENDING"
    case failed = "FAILED"
}

struct Payment: Identifiable, Codable {
    let id: String
    let loanId: String
    let amount: Double
    let paymentDate: Date
    let type: PaymentType
    let status: PaymentStatus
    let receivedByAgentId: String
    let notes: String?
    let appliedToPrincipal: Double
    let appliedToInterest: Double
    let appliedToPenalty: Double
    let createdAt: Date
}
```

### 5. Earnings Models
**Android Location:** `data/model/Earnings.kt`

```swift
// iOS Implementation: Models/Earnings.swift

struct Earnings: Identifiable, Codable {
    let id: String
    let agentId: String
    let totalEarnings: Double
    let collectibleEarnings: Double
    let cashedOutAmount: Double
    let commissionPercentage: Double
    let lastUpdated: Date
    let earningsBreakdown: [EarningEntry]
}

struct EarningEntry: Identifiable, Codable {
    let id: String
    let loanId: String
    let paymentId: String
    let interestAmount: Double
    let commissionAmount: Double
    let earnedDate: Date
    let isCashedOut: Bool
}
```

### 6. Notification Models
**Android Location:** `data/model/Notification.kt`

```swift
// iOS Implementation: Models/Notification.swift

enum NotificationType: String, Codable {
    case pastDue = "PAST_DUE"
    case upcomingDue = "UPCOMING_DUE"
    case loanApproved = "LOAN_APPROVED"
    case loanRejected = "LOAN_REJECTED"
    case cashoutApproved = "CASHOUT_APPROVED"
    case cashoutRejected = "CASHOUT_REJECTED"
    case paymentReceived = "PAYMENT_RECEIVED"
    case accountCreated = "ACCOUNT_CREATED"
    case general = "GENERAL"
}

enum NotificationPriority: String, Codable {
    case low = "LOW"
    case normal = "NORMAL"
    case high = "HIGH"
    case urgent = "URGENT"
}

struct Notification: Identifiable, Codable {
    let id: String
    let userId: String
    let message: String
    let type: NotificationType
    let timestamp: Date
    let isRead: Bool
    let relatedEntityId: String?
    let relatedEntityType: String?
    let priority: NotificationPriority
}
```

### 7. Cashout Request Models
**Android Location:** `data/model/CashoutRequest.kt`

```swift
// iOS Implementation: Models/CashoutRequest.swift

enum CashoutStatus: String, Codable {
    case pending = "PENDING"
    case approved = "APPROVED"
    case rejected = "REJECTED"
}

struct CashoutRequest: Identifiable, Codable {
    let id: String
    let agentId: String
    let amount: Double
    let status: CashoutStatus
    let requestDate: Date
    let approvalDate: Date?
    let approvedByAdminId: String?
    let rejectionReason: String?
    let notes: String?
}
```

### 8. Transaction Models (Audit Log)
**Android Location:** `data/model/Transaction.kt`

```swift
// iOS Implementation: Models/Transaction.swift

enum TransactionType: String, Codable {
    case createAccount = "CREATE_ACCOUNT"
    case updateAccount = "UPDATE_ACCOUNT"
    case createLoan = "CREATE_LOAN"
    case approveLoan = "APPROVE_LOAN"
    case rejectLoan = "REJECT_LOAN"
    case receivePayment = "RECEIVE_PAYMENT"
    case cashoutRequest = "CASHOUT_REQUEST"
    case cashoutApproved = "CASHOUT_APPROVED"
    case cashoutRejected = "CASHOUT_REJECTED"
    case updateCommission = "UPDATE_COMMISSION"
    case userLogin = "USER_LOGIN"
    case userLogout = "USER_LOGOUT"
}

struct Transaction: Identifiable, Codable {
    let id: String
    let type: TransactionType
    let userId: String
    let details: String
    let timestamp: Date
    let relatedEntityId: String?
    let relatedEntityType: String?
}
```

---

## Repository Layer

### 1. AuthRepository
**Android Location:** `data/repository/AuthRepository.kt`

**iOS Implementation: `Repositories/AuthRepository.swift`**

```swift
protocol AuthRepositoryProtocol {
    // Authentication
    func register(email: String, password: String, username: String, role: UserRole) async throws -> User
    func login(email: String, password: String) async throws -> User
    func logout() async throws
    func resetPassword(email: String) async throws
    func changePassword(newPassword: String) async throws
    func refreshSession() async throws

    // User Management
    func getCurrentUser() -> User?
    func getCurrentUserId() -> String?
    func isLoggedIn() -> Bool
    func getUserById(userId: String) async throws -> User
    func updateProfile(user: User) async throws -> User

    // Admin Functions
    func getUsersByRole(role: UserRole) async throws -> [User]
    func getUsersByRoleAndStatus(role: UserRole, status: ApprovalStatus) async throws -> [User]
    func getPendingAgents() async throws -> [User]
    func approveAgent(agentId: String, adminId: String) async throws -> User
    func rejectAgent(agentId: String, adminId: String, reason: String) async throws -> User

    // Observables
    var currentUserPublisher: AnyPublisher<User?, Never> { get }
}

class AuthRepository: AuthRepositoryProtocol {
    private let supabaseClient: SupabaseClient
    private let userDao: UserDAO // Local cache

    // Implementation details...
}
```

**Key Features:**
- Token-based authentication with Supabase Auth
- Role-based access control (Admin/Agent)
- Agent approval workflow (PENDING → APPROVED/REJECTED)
- Local caching with SwiftData/Core Data
- Session management and refresh
- Audit logging for auth events

### 2. AccountRepository
**Android Location:** `data/repository/AccountRepository.kt`

**iOS Implementation: `Repositories/AccountRepository.swift`**

```swift
protocol AccountRepositoryProtocol {
    // CRUD Operations
    func createAccount(account: Account) async throws -> Account
    func updateAccount(account: Account) async throws -> Account
    func deleteAccount(accountId: String) async throws
    func getAccountById(accountId: String) async throws -> Account

    // Queries
    func getAccountsByAgent(agentId: String, forceRefresh: Bool) async throws -> [Account]
    func getAccountsByStatus(status: AccountStatus) async throws -> [Account]
    func searchAccountsByName(query: String) async throws -> [Account]
    func getAllAccounts(forceRefresh: Bool) async throws -> [Account]

    // Sync
    func syncAccounts(agentId: String?) async throws

    // Observables
    func accountsByAgentPublisher(agentId: String) -> AnyPublisher<[Account], Never>
    func accountByIdPublisher(accountId: String) -> AnyPublisher<Account?, Never>
}

class AccountRepository: AccountRepositoryProtocol {
    private let supabaseClient: SupabaseClient
    private let accountDao: AccountDAO
    private let networkMonitor: NetworkMonitor

    // Implementation with offline-first architecture
}
```

**Key Features:**
- Offline-first architecture with local caching
- Network-aware operations (online/offline detection)
- Search and filter capabilities
- Automatic sync when network restored
- Audit logging for account operations

### 3. LoanRepository
**Android Location:** `data/repository/LoanRepository.kt`

**iOS Implementation: `Repositories/LoanRepository.swift`**

```swift
protocol LoanRepositoryProtocol {
    // CRUD Operations
    func createLoan(accountId: String, principalAmount: Double,
                   interestRate: Double, tenureMonths: Int,
                   paymentFrequency: PaymentFrequency) async throws -> Loan
    func approveLoan(loanId: String) async throws -> Loan
    func rejectLoan(loanId: String, reason: String) async throws -> Loan
    func updateLoanBalance(loanId: String, paidAmount: Double) async throws -> Loan

    // Queries
    func getLoansByAgent(agentId: String) async throws -> [Loan]
    func getLoansByAccount(accountId: String) async throws -> [Loan]
    func getLoansByAgentAndStatus(agentId: String, status: LoanStatus) async throws -> [Loan]
    func getTotalOutstandingBalance() async throws -> Double
    func getLoanCountByStatus(status: LoanStatus) async throws -> Int

    // Sync
    func syncLoans(agentId: String?) async throws

    // Observables
    func loanByIdPublisher(loanId: String) -> AnyPublisher<Loan?, Never>
    func loansByAccountPublisher(accountId: String) -> AnyPublisher<[Loan], Never>
    func loansByAgentPublisher(agentId: String) -> AnyPublisher<[Loan], Never>
    func loansByStatusPublisher(status: LoanStatus) -> AnyPublisher<[Loan], Never>
}

class LoanRepository: LoanRepositoryProtocol {
    private let supabaseClient: SupabaseClient
    private let loanDao: LoanDAO
    private let networkMonitor: NetworkMonitor
    private let loanCalculator: LoanCalculator

    // Implementation with amortization schedule generation
}
```

**Key Features:**
- Loan creation with validation (3-5% interest, 2-12 months tenure)
- Automatic amortization schedule generation
- Approval workflow (PENDING_APPROVAL → ACTIVE/REJECTED)
- Balance tracking and payment application
- Offline support with sync
- Audit logging

### 4. PaymentRepository
**Android Location:** `data/repository/PaymentRepository.kt`

**iOS Implementation: `Repositories/PaymentRepository.swift`**

```swift
protocol PaymentRepositoryProtocol {
    // CRUD Operations
    func recordPayment(payment: Payment) async throws -> Payment
    func getPaymentById(paymentId: String) async throws -> Payment
    func getPaymentsByLoan(loanId: String) async throws -> [Payment]
    func getPaymentsByAgent(agentId: String) async throws -> [Payment]
    func getPaymentsByDateRange(startDate: Date, endDate: Date) async throws -> [Payment]

    // Statistics
    func getTotalPaymentsForLoan(loanId: String) async throws -> Double
    func getTotalPaymentsForAgent(agentId: String, dateRange: (Date, Date)?) async throws -> Double

    // Sync
    func syncPayments(loanId: String?) async throws

    // Observables
    func paymentsByLoanPublisher(loanId: String) -> AnyPublisher<[Payment], Never>
}
```

### 5. EarningsRepository
**Android Location:** `data/repository/EarningsRepository.kt`

**iOS Implementation: `Repositories/EarningsRepository.swift`**

```swift
protocol EarningsRepositoryProtocol {
    // Earnings Management
    func getEarningsByAgent(agentId: String) async throws -> Earnings
    func updateEarnings(agentId: String, earnings: Earnings) async throws
    func calculateCommission(interestAmount: Double, commissionRate: Double) -> Double
    func updateCommissionRate(agentId: String, newRate: Double) async throws

    // Observables
    func earningsPublisher(agentId: String) -> AnyPublisher<Earnings, Never>
}
```

### 6. NotificationRepository
**Android Location:** `data/repository/NotificationRepository.kt`

**iOS Implementation: `Repositories/NotificationRepository.swift`**

```swift
protocol NotificationRepositoryProtocol {
    // CRUD Operations
    func createNotification(notification: Notification) async throws
    func markAsRead(notificationId: String) async throws
    func markAllAsRead(userId: String) async throws
    func deleteNotification(notificationId: String) async throws

    // Queries
    func getNotificationsByUser(userId: String) async throws -> [Notification]
    func getUnreadCount(userId: String) async throws -> Int

    // Observables
    func notificationsPublisher(userId: String) -> AnyPublisher<[Notification], Never>
    func unreadCountPublisher(userId: String) -> AnyPublisher<Int, Never>
}
```

---

## ViewModels / View State Management

### 1. AuthViewModel
**Android Location:** `ui/auth/AuthViewModel.kt`

**iOS Implementation: `ViewModels/AuthViewModel.swift`**

```swift
@MainActor
class AuthViewModel: ObservableObject {
    @Published var authState: AuthState = .unauthenticated
    @Published var currentUser: User?
    @Published var errorMessage: String?

    private let authRepository: AuthRepositoryProtocol
    private var cancellables = Set<AnyCancellable>()

    enum AuthState {
        case unauthenticated
        case loading
        case authenticated(User)
        case error(String)
        case passwordResetSent
    }

    // Methods
    func login(email: String, password: String) async
    func register(email: String, password: String, username: String, role: UserRole) async
    func logout() async
    func resetPassword(email: String) async
    func checkAuthStatus()
    func clearError()
}
```

**Features:**
- Input validation (email, password length)
- Loading states
- Error handling with user-friendly messages
- Auto-login on app launch if session exists

### 2. HomeViewModel (Admin Dashboard)
**Android Location:** `ui/home/HomeViewModel.kt`

**iOS Implementation: `ViewModels/HomeViewModel.swift`**

```swift
@MainActor
class HomeViewModel: ObservableObject {
    @Published var dashboardState: DashboardState = .loading
    @Published var statistics: DashboardStatistics?
    @Published var agents: [AgentItemData] = []

    private let authRepository: AuthRepositoryProtocol
    private let accountRepository: AccountRepositoryProtocol
    private let loanRepository: LoanRepositoryProtocol

    enum DashboardState {
        case loading
        case success
        case error(String)
    }

    struct DashboardStatistics {
        let agentsCount: Int
        let totalLoanReleases: Double
        let totalCollectables: Double
        let activeAccountsCount: Int
    }

    struct AgentItemData: Identifiable {
        let id: String
        let user: User
        let accountsCount: Int
        let totalEarnings: Double
    }

    // Methods
    func loadDashboardData() async
    func approveAgent(agentId: String) async
    func rejectAgent(agentId: String, reason: String) async
    func retry()
}
```

**Features:**
- Dashboard statistics calculation
- Agent list with approval status filtering
- Agent approval/rejection actions
- Real-time data updates

### 3. DashboardViewModel (Agent Management)
**Android Location:** `ui/dashboard/DashboardViewModel.kt`

**iOS Implementation: `ViewModels/DashboardViewModel.swift`**

```swift
@MainActor
class DashboardViewModel: ObservableObject {
    @Published var agents: [AgentItemData] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    @Published var currentFilter: StatusFilter = .pending

    private let authRepository: AuthRepositoryProtocol
    private var allAgents: [User] = []

    enum StatusFilter {
        case all, pending, approved, rejected
    }

    // Methods
    func loadAgents() async
    func setFilter(_ filter: StatusFilter)
    func approveAgent(agentId: String) async
    func rejectAgent(agentId: String, reason: String) async
}
```

### 4. LoansViewModel (Accounts/Loans Management)
**Android Location:** `ui/loans/LoansViewModel.kt`

**iOS Implementation: `ViewModels/LoansViewModel.swift`**

```swift
@MainActor
class LoansViewModel: ObservableObject {
    @Published var accounts: [Account] = []
    @Published var filteredAccounts: [Account] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    private let accountRepository: AccountRepositoryProtocol

    // Methods
    func loadAccounts() async
    func searchAccounts(query: String)
    func clearError()
}
```

### 5. AccountDetailsViewModel
**Android Location:** `ui/loans/AccountDetailsViewModel.kt`

**iOS Implementation: `ViewModels/AccountDetailsViewModel.swift`**

```swift
@MainActor
class AccountDetailsViewModel: ObservableObject {
    @Published var account: Account?
    @Published var loans: [Loan] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    private let accountRepository: AccountRepositoryProtocol
    private let loanRepository: LoanRepositoryProtocol

    // Methods
    func loadAccountDetails(accountId: String) async
    func loadLoans(accountId: String) async
    func createNewLoan(loanData: LoanCreationData) async
}
```

### 6. ProfileViewModel
**Android Location:** `ui/profile/ProfileViewModel.kt`

**iOS Implementation: `ViewModels/ProfileViewModel.swift`**

```swift
@MainActor
class ProfileViewModel: ObservableObject {
    @Published var user: User?
    @Published var stats: ProfileStats?
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    struct ProfileStats {
        let totalAccounts: Int
        let activeLoans: Int
    }

    private let authRepository: AuthRepositoryProtocol
    private let accountRepository: AccountRepositoryProtocol
    private let loanRepository: LoanRepositoryProtocol

    // Methods
    func loadUserProfile() async
    func loadProfileStats() async
    func updateProfile(fullName: String?, phone: String?, address: String?, dateOfBirth: String?) async
    func getUserInitials() -> String
    func getFormattedMemberSince() -> String
}
```

### 7. NotificationsViewModel
**Android Location:** `ui/notifications/NotificationsViewModel.kt`

**iOS Implementation: `ViewModels/NotificationsViewModel.swift`**

```swift
@MainActor
class NotificationsViewModel: ObservableObject {
    @Published var notifications: [Notification] = []
    @Published var unreadCount: Int = 0
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    private let notificationRepository: NotificationRepositoryProtocol

    // Methods
    func loadNotifications() async
    func markAsRead(notificationId: String) async
    func markAllAsRead() async
    func deleteNotification(notificationId: String) async
}
```

---

## UI Components

### SwiftUI Screen Structure

```
Loanstar (iOS)
├── App
│   └── LoanstarApp.swift              // App entry point
├── Views
│   ├── Auth
│   │   ├── LoginView.swift            // Login screen
│   │   └── RegisterView.swift         // Registration screen
│   ├── Dashboard
│   │   ├── DashboardView.swift        // Admin dashboard (agent management)
│   │   ├── AgentCardView.swift        // Agent card component
│   │   └── AgentApprovalDialog.swift  // Approval/rejection dialog
│   ├── Home
│   │   ├── HomeView.swift             // Admin home (statistics)
│   │   └── StatisticsCardView.swift   // Statistics display component
│   ├── Loans
│   │   ├── LoansView.swift            // Accounts list
│   │   ├── AccountCardView.swift      // Account card component
│   │   ├── AccountDetailsView.swift   // Account details with loans
│   │   └── LoanCreationView.swift     // Create new loan form
│   ├── Profile
│   │   ├── ProfileView.swift          // User profile
│   │   └── EditProfileView.swift      // Edit profile dialog
│   ├── Notifications
│   │   ├── NotificationsView.swift    // Notifications list
│   │   └── NotificationRowView.swift  // Notification item
│   └── Common
│       ├── LoadingView.swift          // Loading indicator
│       ├── ErrorView.swift            // Error display
│       ├── EmptyStateView.swift       // Empty state placeholder
│       └── GlassCardView.swift        // Glassmorphism card component
├── ViewModels
│   ├── AuthViewModel.swift
│   ├── HomeViewModel.swift
│   ├── DashboardViewModel.swift
│   ├── LoansViewModel.swift
│   ├── AccountDetailsViewModel.swift
│   ├── ProfileViewModel.swift
│   └── NotificationsViewModel.swift
├── Models
│   ├── User.swift
│   ├── Account.swift
│   ├── Loan.swift
│   ├── Payment.swift
│   ├── Earnings.swift
│   ├── Notification.swift
│   ├── CashoutRequest.swift
│   └── Transaction.swift
├── Repositories
│   ├── AuthRepository.swift
│   ├── AccountRepository.swift
│   ├── LoanRepository.swift
│   ├── PaymentRepository.swift
│   ├── EarningsRepository.swift
│   └── NotificationRepository.swift
├── Utilities
│   ├── LoanCalculator.swift
│   ├── DateUtils.swift
│   ├── NetworkMonitor.swift
│   ├── Result.swift
│   └── Extensions.swift
└── Services
    └── SupabaseClient.swift
```

### Main Navigation Structure

```swift
// MainTabView.swift

struct MainTabView: View {
    @StateObject private var authViewModel: AuthViewModel

    var body: some View {
        TabView {
            // Admin or Agent Home
            HomeView()
                .tabItem {
                    Label("Dashboard", systemImage: "chart.bar.fill")
                }

            // Admin: Agent Management / Agent: Accounts
            LoansView()
                .tabItem {
                    Label("Loans", systemImage: "person.3.fill")
                }

            // Profile
            ProfileView()
                .tabItem {
                    Label("Profile", systemImage: "person.circle.fill")
                }

            // Notifications
            NotificationsView()
                .tabItem {
                    Label("Notifications", systemImage: "bell.fill")
                }
        }
    }
}
```

---

## Utilities

### 1. LoanCalculator
**Android Location:** `util/LoanCalculator.kt`

**iOS Implementation: `Utilities/LoanCalculator.swift`**

```swift
class LoanCalculator {
    // EMI Calculation
    static func calculateEMI(principal: Double, monthlyInterestRate: Double, tenureMonths: Int) -> Double

    // Amortization Schedule Generation
    static func generateAmortizationSchedule(
        principal: Double,
        monthlyInterestRate: Double,
        tenureMonths: Int,
        paymentFrequency: PaymentFrequency,
        startDate: Date
    ) -> [AmortizationEntry]

    // Penalty Calculation
    static func calculatePenalty(dueAmount: Double, daysOverdue: Int) -> Double
    static func calculateDaysOverdue(dueDate: Date, currentDate: Date) -> Int
    static func calculateTotalDueWithPenalty(entry: AmortizationEntry, currentDate: Date) -> Double

    // Validation
    static func validateLoanParameters(
        principal: Double,
        monthlyInterestRate: Double,
        tenureMonths: Int
    ) -> [String]

    // Statistics
    static func calculateTotalInterest(
        principal: Double,
        monthlyInterestRate: Double,
        tenureMonths: Int,
        paymentFrequency: PaymentFrequency
    ) -> Double

    static func calculateTotalAmount(
        principal: Double,
        monthlyInterestRate: Double,
        tenureMonths: Int,
        paymentFrequency: PaymentFrequency
    ) -> Double
}
```

**Key Calculations:**
- **EMI Formula:** `EMI = P × r × (1+r)^n / ((1+r)^n - 1)`
- **Penalty:** 3% of due amount per month, calculated daily: `(3% / 30) × days overdue`
- **Interest Rate:** 3-5% per month (simple interest)
- **Payment Frequencies:** Weekly (7 days), Bi-monthly (15 days), Monthly (30 days)

### 2. DateUtils
**Android Location:** `util/DateUtils.kt`

**iOS Implementation: `Utilities/DateUtils.swift`**

```swift
struct DateUtils {
    static func getStartOfDay(_ date: Date = Date()) -> Date
    static func getEndOfDay(_ date: Date = Date()) -> Date
    static func getStartOfMonth(_ date: Date = Date()) -> Date
    static func getEndOfMonth(_ date: Date = Date()) -> Date
    static func getStartOfWeek(_ date: Date = Date()) -> Date
    static func getEndOfWeek(_ date: Date = Date()) -> Date

    static func addDays(to date: Date, days: Int) -> Date
    static func addMonths(to date: Date, months: Int) -> Date

    static func daysBetween(_ startDate: Date, _ endDate: Date) -> Int
    static func monthsBetween(_ startDate: Date, _ endDate: Date) -> Int

    static func isPast(_ date: Date) -> Bool
    static func isFuture(_ date: Date) -> Bool
    static func isToday(_ date: Date) -> Bool
    static func isWithinNextDays(_ date: Date, days: Int) -> Bool
    static func daysUntil(_ date: Date) -> Int

    static func formatRelativeTime(_ date: Date) -> String
    static func formatDateRange(_ startDate: Date, _ endDate: Date) -> String
    static func isSameDay(_ date1: Date, _ date2: Date) -> Bool
}
```

### 3. NetworkMonitor
**Android Location:** `util/NetworkMonitor.kt`

**iOS Implementation: `Utilities/NetworkMonitor.swift`**

```swift
import Network

class NetworkMonitor: ObservableObject {
    @Published var isConnected: Bool = true
    @Published var connectionType: ConnectionType = .wifi

    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "NetworkMonitor")

    enum ConnectionType {
        case wifi
        case cellular
        case ethernet
        case other
        case none
    }

    init() {
        monitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                self?.isConnected = path.status == .satisfied
                self?.connectionType = self?.getConnectionType(from: path) ?? .none
            }
        }
        monitor.start(queue: queue)
    }

    private func getConnectionType(from path: NWPath) -> ConnectionType {
        if path.usesInterfaceType(.wifi) {
            return .wifi
        } else if path.usesInterfaceType(.cellular) {
            return .cellular
        } else if path.usesInterfaceType(.wiredEthernet) {
            return .ethernet
        } else {
            return .other
        }
    }
}
```

### 4. Result Type
**Android Location:** `util/Result.kt`

**iOS Implementation: `Utilities/Result.swift`**

```swift
enum Result<T> {
    case success(T)
    case failure(Error)
    case loading

    var isSuccess: Bool {
        if case .success = self { return true }
        return false
    }

    var isFailure: Bool {
        if case .failure = self { return true }
        return false
    }

    var isLoading: Bool {
        if case .loading = self { return true }
        return false
    }

    func getOrNull() -> T? {
        if case .success(let data) = self { return data }
        return nil
    }

    func getOrThrow() throws -> T {
        switch self {
        case .success(let data):
            return data
        case .failure(let error):
            throw error
        case .loading:
            throw ResultError.stillLoading
        }
    }
}

enum ResultError: Error {
    case stillLoading
}

// Safe async call wrapper
func safeCall<T>(_ block: @escaping () async throws -> T) async -> Result<T> {
    do {
        let data = try await block()
        return .success(data)
    } catch {
        return .failure(error)
    }
}
```

### 5. Extensions
**Android Location:** `util/Extensions.kt`

**iOS Implementation: `Utilities/Extensions.swift`**

```swift
// Date Extensions
extension Date {
    func toDateString(format: String = "MMM dd, yyyy") -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = format
        return formatter.string(from: self)
    }

    func toTimeString(format: String = "hh:mm a") -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = format
        return formatter.string(from: self)
    }

    func toDateTimeString() -> String {
        "\(toDateString()) \(toTimeString())"
    }
}

// Double Extensions
extension Double {
    func toCurrencyString(currencySymbol: String = "$") -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        let formatted = formatter.string(from: NSNumber(value: self)) ?? "0.00"
        return "\(currencySymbol)\(formatted)"
    }

    func toPercentageString() -> String {
        String(format: "%.2f%%", self)
    }
}

// String Extensions
extension String {
    func toDate(format: String = "MMM dd, yyyy") -> Date? {
        let formatter = DateFormatter()
        formatter.dateFormat = format
        return formatter.date(from: self)
    }

    var isValidEmail: Bool {
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        let emailPredicate = NSPredicate(format:"SELF MATCHES %@", emailRegex)
        return emailPredicate.evaluate(with: self)
    }
}
```

---

## Features Implementation

### 1. Authentication & Authorization

**Android References:**
- `ui/auth/LoginFragment.kt`
- `ui/auth/RegisterFragment.kt`
- `ui/auth/AuthViewModel.kt`
- `data/repository/AuthRepository.kt`

**iOS Implementation Steps:**

1. **Login Screen (`Views/Auth/LoginView.swift`)**
   - Email and password input fields
   - Form validation
   - Error display
   - Loading state
   - Navigation to registration

2. **Registration Screen (`Views/Auth/RegisterView.swift`)**
   - Email, password, username, role selection
   - Password strength validation (min 6 characters)
   - Role selection (Admin/Agent)
   - Auto-approval for Admin, PENDING for Agent

3. **Session Management**
   - Token storage in Keychain
   - Automatic session refresh
   - Logout on token expiration
   - Check auth status on app launch

4. **Password Reset**
   - Email-based password reset via Supabase
   - Reset confirmation UI

### 2. Agent Approval Workflow

**Android References:**
- `ui/home/HomeFragment.kt`
- `ui/dashboard/DashboardFragment.kt`
- `data/repository/AuthRepository.kt` (approveAgent, rejectAgent)

**iOS Implementation:**

1. **Pending Agents View**
   - List of agents with PENDING status
   - Agent details (username, email, registration date)
   - Approve/Reject buttons

2. **Agent Approval Dialog**
   - Confirmation dialog for approval
   - Rejection reason input for rejection
   - Audit logging of approval/rejection

3. **Status Filtering**
   - Filter by: ALL, PENDING, APPROVED, REJECTED
   - Real-time status updates

### 3. Account Management (Borrower Profiles)

**Android References:**
- `ui/loans/LoansFragment.kt`
- `ui/loans/AccountDetailsFragment.kt`
- `data/repository/AccountRepository.kt`

**iOS Implementation:**

1. **Account List View**
   - Paginated list of accounts
   - Search functionality (name, phone, email, address)
   - Status indicators (ACTIVE, INACTIVE, SUSPENDED)
   - Sort options

2. **Account Details View**
   - Personal information display
   - Contact details
   - Associated loans list
   - Loan history
   - Action buttons (Edit, Create Loan)

3. **Create Account Form**
   - Name, phone, email (optional), alternate phone (optional)
   - Address
   - ID proof upload (future enhancement)
   - Assigned agent selection (Admin only)
   - Validation and error handling

4. **Edit Account**
   - Update account details
   - Audit logging

### 4. Loan Management

**Android References:**
- `ui/loans/AccountDetailsFragment.kt`
- `data/repository/LoanRepository.kt`
- `util/LoanCalculator.kt`

**iOS Implementation:**

1. **Loan Creation Form**
   - Account selection
   - Principal amount input
   - Interest rate selection (3-5% dropdown)
   - Tenure selection (2-12 months)
   - Payment frequency (Weekly/Bi-monthly/Monthly)
   - Amortization schedule preview
   - Validation and calculation

2. **Loan Approval (Admin)**
   - List of pending loans
   - Loan details review
   - Approve/Reject actions
   - Notification to agent

3. **Amortization Schedule View**
   - Interactive table/list display
   - Columns: Payment #, Due Date, Principal, Interest, Total, Balance, Penalty, Status
   - Mark payment as paid
   - Export as PDF/CSV

4. **Payment Recording**
   - Amount input
   - Payment date picker
   - Automatic balance update
   - Payment allocation (principal, interest, penalty)
   - Earnings calculation for agent

### 5. Notifications System

**Android References:**
- `ui/notifications/NotificationsFragment.kt`
- `data/model/Notification.kt`
- `data/repository/NotificationRepository.kt`

**iOS Implementation:**

1. **Notification Center View**
   - List of notifications sorted by timestamp
   - Badge for unread count
   - Mark as read functionality
   - Mark all as read
   - Delete notification

2. **Notification Types:**
   - Past-due payments
   - Upcoming due dates (< 5 days)
   - Loan approved/rejected
   - Cashout approved/rejected
   - Payment received
   - Account created

3. **Push Notifications**
   - Firebase Cloud Messaging integration
   - Real-time delivery
   - Deep linking to related entity

4. **Priority Levels:**
   - LOW, NORMAL, HIGH, URGENT
   - Visual indicators (color coding)

### 6. Earnings & Cashout

**Android References:**
- `data/model/Earnings.kt`
- `data/model/CashoutRequest.kt`
- `data/repository/EarningsRepository.kt`

**iOS Implementation:**

1. **Earnings Dashboard (Agent)**
   - Total earnings
   - Collectible earnings
   - Cashed out amount
   - Commission percentage display
   - Earnings breakdown by loan/period

2. **Cashout Request (Agent)**
   - Request cashout amount
   - Minimum threshold validation
   - Request status tracking
   - History of cashout requests

3. **Cashout Management (Admin)**
   - List of pending cashout requests
   - Approve/Reject actions
   - Automatic deduction from collectible earnings
   - Notification to agent

4. **Commission Settings (Admin)**
   - Set commission percentage per agent or globally
   - Immediate effect on future calculations

### 7. Dashboard & Statistics

**Android References:**
- `ui/home/HomeFragment.kt`
- `ui/dashboard/DashboardFragment.kt`

**iOS Implementation:**

1. **Agent Dashboard**
   - Summary of assigned accounts
   - Upcoming notifications
   - Earnings overview
   - Quick actions

2. **Admin Dashboard**
   - Total loans and outstanding balances
   - Agent performance metrics
   - Accounts managed by each agent
   - Commissions paid
   - Pending approvals count

3. **Statistics Cards**
   - Glassmorphism design
   - Real-time data updates
   - Interactive elements

### 8. Profile Management

**Android References:**
- `ui/profile/ProfileFragment.kt`
- `ui/profile/ProfileViewModel.kt`

**iOS Implementation:**

1. **Profile View**
   - User information display
   - Profile picture/avatar
   - Role badge
   - Member since date
   - Statistics (total accounts, active loans)

2. **Edit Profile**
   - Full name
   - Phone number
   - Address
   - Date of birth
   - Validation and update

3. **Settings**
   - Change password
   - Logout

### 9. Offline Support & Sync

**Android References:**
- `util/NetworkMonitor.kt`
- All repositories (offline-first architecture)

**iOS Implementation:**

1. **Network Monitoring**
   - Detect connectivity changes
   - Display offline banner

2. **Local Caching**
   - Cache all entities in SwiftData/Core Data
   - Read from cache when offline
   - Queue operations for sync

3. **Sync Strategy**
   - Automatic sync on network restore
   - Manual sync button
   - Conflict resolution (server wins)
   - Sync status indicators

4. **Offline Capabilities:**
   - View cached accounts
   - View cached loans and amortization schedules
   - View cached notifications
   - Queue payment recordings
   - Queue account/loan creation

### 10. Audit Logging

**Android References:**
- `data/model/Transaction.kt`
- All repositories (logTransaction methods)

**iOS Implementation:**

1. **Transaction Logging**
   - Log all CRUD operations
   - User ID, timestamp, details
   - Related entity tracking

2. **Transaction Types:**
   - CREATE_ACCOUNT, UPDATE_ACCOUNT
   - CREATE_LOAN, APPROVE_LOAN, REJECT_LOAN
   - RECEIVE_PAYMENT
   - CASHOUT_REQUEST, CASHOUT_APPROVED, CASHOUT_REJECTED
   - UPDATE_COMMISSION
   - USER_LOGIN, USER_LOGOUT

3. **Admin Transaction View**
   - Comprehensive audit log
   - Filter by user, date, type
   - Export reports
   - Search functionality

---

## Implementation Checklist

### Phase 1: Foundation (Week 1-2)

- [ ] **Setup Project**
  - [ ] Create iOS project in Xcode
  - [ ] Add Supabase Swift SDK dependency
  - [ ] Add Reachability dependency
  - [ ] Configure SwiftData/Core Data
  - [ ] Setup folder structure

- [ ] **Data Models**
  - [ ] User, ProfileDetails, UserRole, ApprovalStatus
  - [ ] Account, ContactInfo, AccountStatus
  - [ ] Loan, AmortizationEntry, LoanStatus, PaymentFrequency
  - [ ] Payment, PaymentType, PaymentStatus
  - [ ] Earnings, EarningEntry
  - [ ] Notification, NotificationType, NotificationPriority
  - [ ] CashoutRequest, CashoutStatus
  - [ ] Transaction, TransactionType

- [ ] **Utilities**
  - [ ] LoanCalculator (EMI, amortization, penalties)
  - [ ] DateUtils (all date operations)
  - [ ] NetworkMonitor (connectivity detection)
  - [ ] Result type
  - [ ] Extensions (Date, Double, String)

- [ ] **Services**
  - [ ] SupabaseClient initialization
  - [ ] Authentication configuration
  - [ ] Database configuration
  - [ ] Realtime configuration
  - [ ] Storage configuration

### Phase 2: Repository Layer (Week 2-3)

- [ ] **AuthRepository**
  - [ ] Register, Login, Logout
  - [ ] Password reset and change
  - [ ] Session management and refresh
  - [ ] User CRUD operations
  - [ ] Agent approval/rejection
  - [ ] Local caching with DAO

- [ ] **AccountRepository**
  - [ ] CRUD operations
  - [ ] Search and filter
  - [ ] Offline-first architecture
  - [ ] Sync mechanism
  - [ ] Local caching

- [ ] **LoanRepository**
  - [ ] Loan creation with validation
  - [ ] Amortization schedule generation
  - [ ] Approval/rejection workflow
  - [ ] Balance updates
  - [ ] Offline support

- [ ] **PaymentRepository**
  - [ ] Payment recording
  - [ ] Payment queries
  - [ ] Statistics calculation

- [ ] **EarningsRepository**
  - [ ] Earnings tracking
  - [ ] Commission calculation
  - [ ] Commission rate updates

- [ ] **NotificationRepository**
  - [ ] CRUD operations
  - [ ] Unread count tracking
  - [ ] Real-time updates

### Phase 3: ViewModels (Week 3-4)

- [ ] **AuthViewModel**
  - [ ] Login logic
  - [ ] Registration logic
  - [ ] Session check
  - [ ] Error handling

- [ ] **HomeViewModel**
  - [ ] Dashboard statistics
  - [ ] Agents list
  - [ ] Agent approval/rejection

- [ ] **DashboardViewModel**
  - [ ] Agent filtering
  - [ ] Status management

- [ ] **LoansViewModel**
  - [ ] Accounts list
  - [ ] Search functionality

- [ ] **AccountDetailsViewModel**
  - [ ] Account details
  - [ ] Loans list
  - [ ] Loan creation

- [ ] **ProfileViewModel**
  - [ ] User profile
  - [ ] Statistics
  - [ ] Profile update

- [ ] **NotificationsViewModel**
  - [ ] Notifications list
  - [ ] Mark as read
  - [ ] Delete notifications

### Phase 4: UI Components (Week 4-6)

- [ ] **Authentication**
  - [ ] LoginView
  - [ ] RegisterView
  - [ ] Password reset view

- [ ] **Dashboard/Home**
  - [ ] HomeView (Admin statistics)
  - [ ] DashboardView (Agent management)
  - [ ] StatisticsCardView
  - [ ] AgentCardView
  - [ ] AgentApprovalDialog

- [ ] **Loans/Accounts**
  - [ ] LoansView (Accounts list)
  - [ ] AccountCardView
  - [ ] AccountDetailsView
  - [ ] LoanCreationView
  - [ ] AmortizationScheduleView

- [ ] **Profile**
  - [ ] ProfileView
  - [ ] EditProfileView

- [ ] **Notifications**
  - [ ] NotificationsView
  - [ ] NotificationRowView

- [ ] **Common Components**
  - [ ] LoadingView
  - [ ] ErrorView
  - [ ] EmptyStateView
  - [ ] GlassCardView (glassmorphism)

- [ ] **Navigation**
  - [ ] MainTabView
  - [ ] Navigation routing

### Phase 5: Advanced Features (Week 6-7)

- [ ] **Offline Support**
  - [ ] Network detection
  - [ ] Offline banner
  - [ ] Data caching
  - [ ] Sync mechanism
  - [ ] Conflict resolution

- [ ] **Push Notifications**
  - [ ] Firebase Cloud Messaging integration
  - [ ] Notification handling
  - [ ] Deep linking

- [ ] **PDF Export**
  - [ ] Amortization schedule PDF generation
  - [ ] Sharing functionality

- [ ] **Real-time Updates**
  - [ ] Supabase Realtime subscriptions
  - [ ] Live data updates
  - [ ] Proper cleanup

### Phase 6: Testing & Polish (Week 7-8)

- [ ] **Unit Tests**
  - [ ] LoanCalculator tests
  - [ ] DateUtils tests
  - [ ] Repository tests (with mocks)
  - [ ] ViewModel tests

- [ ] **UI Tests**
  - [ ] Authentication flow
  - [ ] Navigation flow
  - [ ] CRUD operations

- [ ] **Integration Tests**
  - [ ] Supabase API integration
  - [ ] Offline/online transitions
  - [ ] Sync functionality

- [ ] **Performance Optimization**
  - [ ] Lazy loading
  - [ ] Image caching
  - [ ] Memory leak detection
  - [ ] Pagination

- [ ] **UI/UX Polish**
  - [ ] Animations and transitions
  - [ ] Loading states
  - [ ] Error states
  - [ ] Empty states
  - [ ] Accessibility (VoiceOver support)
  - [ ] Dark mode support
  - [ ] Responsive layouts

### Phase 7: Deployment (Week 8)

- [ ] **App Store Preparation**
  - [ ] App icon and launch screen
  - [ ] Screenshots
  - [ ] App description
  - [ ] Privacy policy

- [ ] **Configuration**
  - [ ] Environment variables
  - [ ] API keys (Supabase)
  - [ ] Build configurations (Debug/Release)

- [ ] **TestFlight**
  - [ ] Internal testing
  - [ ] External testing
  - [ ] Bug fixes

- [ ] **App Store Submission**
  - [ ] App review submission
  - [ ] Address review feedback

---

## Key Implementation Notes

### 1. Supabase Configuration

```swift
// Services/SupabaseClient.swift

import Supabase

class SupabaseClientProvider {
    static let shared = SupabaseClientProvider()

    let client: SupabaseClient

    private init() {
        // Get from environment or configuration
        let supabaseURL = URL(string: "https://eypsjkstzvfjsgpojjuf.supabase.co")!
        let supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." // Your anon key

        client = SupabaseClient(
            supabaseURL: supabaseURL,
            supabaseKey: supabaseKey
        )
    }
}
```

### 2. Dependency Injection

```swift
// Use protocol-based dependency injection

protocol Dependency {
    var authRepository: AuthRepositoryProtocol { get }
    var accountRepository: AccountRepositoryProtocol { get }
    var loanRepository: LoanRepositoryProtocol { get }
    // ... other repositories
}

class AppDependency: Dependency {
    lazy var supabaseClient = SupabaseClientProvider.shared.client

    lazy var authRepository: AuthRepositoryProtocol = AuthRepository(
        supabaseClient: supabaseClient,
        userDao: userDao
    )

    // ... initialize other repositories
}

// Use @EnvironmentObject to inject into views
@main
struct LoanstarApp: App {
    @StateObject private var dependency = AppDependency()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(dependency)
        }
    }
}
```

### 3. Error Handling

```swift
enum AppError: LocalizedError {
    case networkError(String)
    case validationError(String)
    case authenticationError(String)
    case notFound(String)
    case serverError(String)

    var errorDescription: String? {
        switch self {
        case .networkError(let message):
            return "Network Error: \(message)"
        case .validationError(let message):
            return "Validation Error: \(message)"
        case .authenticationError(let message):
            return "Authentication Error: \(message)"
        case .notFound(let message):
            return "Not Found: \(message)"
        case .serverError(let message):
            return "Server Error: \(message)"
        }
    }
}
```

### 4. Design Patterns

- **MVVM:** Clear separation between Views and ViewModels
- **Repository Pattern:** Abstraction layer for data sources
- **Singleton:** SupabaseClient, NetworkMonitor
- **Observer Pattern:** Combine publishers for reactive updates
- **Factory Pattern:** For creating instances with dependencies

### 5. Testing Strategy

```swift
// Example ViewModel test

import XCTest
@testable import Loanstar

class AuthViewModelTests: XCTestCase {
    var sut: AuthViewModel!
    var mockAuthRepository: MockAuthRepository!

    override func setUp() {
        super.setUp()
        mockAuthRepository = MockAuthRepository()
        sut = AuthViewModel(authRepository: mockAuthRepository)
    }

    func testLogin_WithValidCredentials_ShouldAuthenticate() async {
        // Given
        let email = "test@example.com"
        let password = "password123"

        // When
        await sut.login(email: email, password: password)

        // Then
        XCTAssertEqual(sut.authState, .authenticated)
        XCTAssertNotNil(sut.currentUser)
    }
}
```

---

## Additional Resources

### Android to iOS Mapping Reference

| Android Component | iOS Equivalent |
|-------------------|----------------|
| Fragment | SwiftUI View |
| ViewModel | ObservableObject |
| LiveData | @Published property |
| Kotlin Coroutines | async/await |
| Flow | Combine Publisher |
| Room Database | SwiftData / Core Data |
| Hilt/Dagger | Protocol-based DI |
| Navigation Component | NavigationStack |
| ViewBinding | SwiftUI (no binding needed) |
| BottomNavigationView | TabView |

### Best Practices

1. **SwiftUI State Management**
   - Use `@State` for view-local state
   - Use `@StateObject` for view-owned objects
   - Use `@ObservedObject` for passed objects
   - Use `@EnvironmentObject` for shared dependencies

2. **Async/Await Usage**
   - Mark async functions with `async`
   - Use `await` for async calls
   - Handle errors with `do-catch`
   - Use `Task` for launching async work from sync context

3. **Combine Publishers**
   - Use for reactive data flows
   - Clean up with `cancellables`
   - Prefer async/await for simple cases

4. **SwiftData/Core Data**
   - Use for local persistence
   - Define @Model classes
   - Use @Query for fetching
   - Handle migrations properly

5. **Security**
   - Store sensitive data in Keychain
   - Never commit API keys
   - Use HTTPS only
   - Implement proper session management

---

## Conclusion

This comprehensive guide provides a complete roadmap for implementing the iOS version of the Loanstar app based on the Android implementation. All features, data models, repositories, ViewModels, utilities, and UI components have been documented with their Android counterparts for reference.

The implementation should follow the phases outlined in the checklist, ensuring proper testing and quality assurance at each stage.

**Estimated Timeline:** 8 weeks for full implementation with a single developer, or 4-6 weeks with a team of 2-3 developers.

**Priority Features:**
1. Authentication and user management
2. Account management
3. Loan creation and management
4. Payment recording
5. Notifications
6. Offline support
7. Advanced features (PDF export, real-time updates)

For any questions or clarifications, refer to the Android source code in the corresponding packages and files referenced throughout this document.
