# Loanstar - Lending Mobile Application

![Android](https://img.shields.io/badge/Android-24+-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue.svg)
![License](https://img.shields.io/badge/License-Private-red.svg)

Loanstar is a comprehensive Android lending mobile application designed for financial loan management with two user roles: **Admin** and **Agent**. Built with modern Android architecture patterns, Supabase backend, and Material Design 3.

## Features

### Agent Features
- **Account Management**: Create, view, edit, and manage borrower accounts
- **Loan Management**: Create loan applications, view amortization schedules, record payments
- **Real-time Notifications**: Push notifications for past-due and upcoming payments
- **Earnings Tracking**: View commissions earned and request cashouts
- **Offline Support**: Cache data for offline access with automatic sync

### Admin Features
- **All Agent Capabilities**: Admins can perform all agent functions
- **Cashout Management**: Approve or reject agent cashout requests
- **Transaction Oversight**: Comprehensive audit log of all system activities
- **Commission Settings**: Set and update commission percentages for agents
- **Role Management**: Assign and manage user roles
- **Analytics Dashboard**: View system-wide metrics and agent performance

### Technical Features
- **Secure Authentication**: Token-based auth with automatic session refresh
- **Network Monitoring**: Robust connectivity checking with offline mode alerts
- **Data Caching**: Room database for offline access
- **Background Sync**: Automatic data synchronization when reconnected
- **Material Design 3**: Modern, clean, and intuitive UI
- **Real-time Updates**: Live data updates using Supabase Realtime
- **PDF Export**: Generate and share amortization schedules

## Architecture

- **Pattern**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt/Dagger
- **Database**: Supabase (remote) + Room (local cache)
- **Authentication**: Supabase Auth
- **Notifications**: Firebase Cloud Messaging
- **Navigation**: Navigation Component with bottom navigation
- **UI**: XML layouts with ViewBinding

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin 1.9.24 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Build System | Gradle with Kotlin DSL |
| Backend | Supabase (PostgREST, Auth, Realtime, Storage) |
| Local DB | Room Database |
| DI | Hilt/Dagger |
| Async | Kotlin Coroutines & Flow |
| Notifications | Firebase Cloud Messaging |
| PDF Generation | iText |
| Security | AndroidX Security Crypto |

## Project Structure

```
app/src/main/java/com/example/loanstar/
├── LoanstarApp.kt                 # Application class
├── MainActivity.kt                # Main activity with navigation
├── data/
│   ├── model/                     # Domain models
│   │   ├── User.kt
│   │   ├── Account.kt
│   │   ├── Loan.kt
│   │   ├── Payment.kt
│   │   ├── Notification.kt
│   │   ├── Earnings.kt
│   │   ├── CashoutRequest.kt
│   │   └── Transaction.kt
│   ├── local/                     # Room database (pending)
│   ├── remote/                    # Supabase client
│   │   └── SupabaseClient.kt
│   └── repository/                # Data repositories (pending)
├── di/
│   └── AppModule.kt              # Hilt dependency injection module
├── ui/                           # UI components (pending)
│   ├── auth/
│   ├── agent/
│   ├── admin/
│   └── common/
└── util/
    ├── LoanCalculator.kt         # Loan calculations
    ├── NetworkMonitor.kt         # Network connectivity
    └── Extensions.kt             # Utility extensions
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK with API 24-34
- Supabase account
- Firebase account

### Installation

1. **Clone the repository**

```bash
git clone <repository-url>
cd Loanstar
```

2. **Configure Supabase**

   - Create a Supabase project at https://supabase.com
   - Set up database tables (see [SETUP_GUIDE.md](SETUP_GUIDE.md))
   - Copy your project credentials

3. **Configure local.properties**

```bash
cp local.properties.sample local.properties
```

Edit `local.properties`:

```properties
sdk.dir=/path/to/android/sdk
supabase.url=https://YOUR_PROJECT_ID.supabase.co
supabase.anon.key=YOUR_ANON_KEY_HERE
```

4. **Configure Firebase**

   - Create a Firebase project
   - Add Android app with package name: `com.example.loanstar`
   - Download `google-services.json` to `app/` directory
   - Enable Cloud Messaging

5. **Sync and Build**

```bash
./gradlew build
```

### Running the App

**Install debug build:**

```bash
./gradlew installDebug
```

**Generate APK:**

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

**Run tests:**

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Loan Calculation Rules

### Interest Rates
- 3-5% per month (simple interest)
- Validation enforced on input

### Payment Frequencies
- **Weekly**: Every 7 days
- **Bi-monthly**: Every 15 days
- **Monthly**: Every 30 days

### Tenure
- Minimum: 2 months
- Maximum: 12 months

### Amortization Formula
EMI = P × r × (1+r)^n / ((1+r)^n - 1)

Where:
- P = Principal amount
- r = Interest rate per period
- n = Number of periods

### Penalties
- 3% of due amortization amount per month
- Calculated daily: (3% / 30) × days overdue
- Accumulates until paid

### Approval Workflow
1. Agent creates loan (status: PENDING_APPROVAL)
2. Admin receives notification
3. Admin approves or rejects with reason
4. Upon approval, loan becomes ACTIVE

## Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused

### Git Workflow
1. Create feature branch from `main`
2. Make changes and commit
3. Create pull request
4. Code review required
5. Merge after approval

### Commit Messages
```
feat: Add loan approval workflow
fix: Correct penalty calculation
docs: Update setup guide
refactor: Simplify amortization logic
test: Add unit tests for LoanCalculator
```

### Testing
- Write unit tests for business logic
- Test ViewModels with mock data
- UI tests for critical user flows
- Aim for 80%+ code coverage on utilities

## Configuration

### Supabase Tables

See [SETUP_GUIDE.md](SETUP_GUIDE.md) for complete SQL schema including:
- users
- accounts
- loans
- payments
- notifications
- earnings
- cashout_requests
- transactions

### Row Level Security

RLS policies must be configured to:
- Allow agents to access only their assigned accounts
- Allow admins to access all data
- Restrict modifications based on role

### Firebase Cloud Messaging

FCM is used for:
- Past-due payment notifications
- Upcoming payment reminders (< 5 days)
- Loan approval/rejection notifications
- Cashout status updates

## Security

- Supabase credentials stored in `local.properties` (never committed)
- BuildConfig used for secure credential access
- Row Level Security (RLS) on all Supabase tables
- Encrypted local storage with AndroidX Security Crypto
- Token-based authentication with auto-refresh
- Input validation on client and server side

## Implementation Status

### ✅ Completed
- Project setup and dependencies
- Supabase integration
- Data models for all entities
- Loan calculation utilities
- Network monitoring
- Hilt dependency injection
- Application class configuration

### ⏳ In Progress
- Room database setup
- Repository implementations
- UI components
- Authentication flows

### 📋 Pending
- Agent UI screens
- Admin UI screens
- FCM service
- Offline sync
- PDF export
- Unit tests
- UI tests

See [IMPLEMENTATION_PROGRESS.md](IMPLEMENTATION_PROGRESS.md) for detailed status.

## Documentation

- [CLAUDE.md](CLAUDE.md) - Project guidelines and coding standards
- [SETUP_GUIDE.md](SETUP_GUIDE.md) - Comprehensive setup instructions
- [IMPLEMENTATION_PROGRESS.md](IMPLEMENTATION_PROGRESS.md) - Development progress tracker
- [req.md](req.md) - Original requirements document

## Build Commands

| Command | Description |
|---------|-------------|
| `./gradlew build` | Build the project |
| `./gradlew clean` | Clean build artifacts |
| `./gradlew assembleDebug` | Generate debug APK |
| `./gradlew assembleRelease` | Generate release APK |
| `./gradlew installDebug` | Install debug build on device |
| `./gradlew test` | Run unit tests |
| `./gradlew connectedAndroidTest` | Run instrumented tests |
| `./gradlew lint` | Run lint checks |

## Troubleshooting

### Build Issues

**Problem**: Supabase credentials not found
**Solution**: Ensure `local.properties` exists with correct credentials

**Problem**: Hilt annotation processor errors
**Solution**: Clean and rebuild: `./gradlew clean build`

**Problem**: Room schema errors
**Solution**: Clear app data and reinstall

### Runtime Issues

**Problem**: Network connection failures
**Solution**: Verify Supabase URL and check internet connection

**Problem**: Authentication failures
**Solution**: Verify Supabase Auth is enabled and RLS policies are correct

**Problem**: Firebase notifications not working
**Solution**: Ensure `google-services.json` is in `app/` directory and FCM is enabled

## Performance Optimization

- Lazy loading for large lists
- Pagination for database queries
- Image caching and optimization
- Background syncing with WorkManager
- Efficient Room queries with indexes
- Minimize re-compositions in UI

## Contributing

1. Read [CLAUDE.md](CLAUDE.md) for coding standards
2. Create a feature branch
3. Write tests for new features
4. Submit pull request with clear description
5. Address review comments

## License

This project is proprietary and confidential.

## Support

For issues and questions:
1. Check documentation in this repository
2. Review [SETUP_GUIDE.md](SETUP_GUIDE.md)
3. Consult the development team

## Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Material Design 3](https://m3.material.io/)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Room Database](https://developer.android.com/training/data-storage/room)

---

**Loanstar** - Modern lending management for the mobile era.
