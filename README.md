# Loanstar - Cross-Platform Lending Application

A comprehensive lending mobile application with both **Android** and **iOS** implementations, featuring secure loan management, role-based access control, and offline-first architecture.

## 📱 Platforms

### Android (Kotlin + Jetpack Compose)
**Location**: [`/android`](./android)

- **Language**: Kotlin 1.9.24
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: MVVM with Navigation Components
- **Backend**: Supabase, Firebase
- **Database**: Room (local), Supabase (remote)

**Quick Start:**
```bash
cd android
./gradlew assembleDebug
```

📚 [Android Documentation](./android/CLAUDE.md) | [Android README](./android/README.md)

### iOS (Swift + SwiftUI)
**Location**: [`/ios`](./ios)

- **Language**: Swift 5.9+
- **Min iOS**: 16.0+
- **Framework**: SwiftUI
- **Architecture**: MVVM with Combine
- **Backend**: Supabase, Firebase
- **Database**: Core Data (local), Supabase (remote)

**Quick Start:**
```bash
cd ios/LoanstariOS
open Package.swift
```

📚 [iOS Documentation](./ios/LoanstariOS/README.md) | [iOS Setup Guide](./docs/ios/XCODE_SETUP_GUIDE.md)

## 🎯 Features

### User Roles
- **Admin**: Full system access with oversight capabilities
- **Agent**: Borrower and loan management

### Core Functionality

#### ✅ Authentication
- User registration with role selection
- Secure login (username/password)
- Session management with auto-refresh
- Password reset
- Role-based access control

#### ✅ Account Management
- Create borrower accounts
- View and edit account details
- Search and filter accounts
- Upload ID proof documents
- Account status management

#### ✅ Loan Management
- Create loan applications
- Configurable interest rates (3-5% monthly)
- Multiple payment frequencies (bi-monthly, monthly, weekly)
- Tenure: 2-12 months
- Automatic amortization schedule generation
- Payment recording and tracking
- Penalty calculations (3% monthly, daily prorated)

#### ✅ Admin Features
- Loan approval/rejection workflow
- Cashout request management
- Transaction audit logs
- Commission settings
- User management
- Performance analytics

#### ✅ Agent Features
- Dashboard with statistics
- Assigned accounts overview
- Loan creation and management
- Payment recording
- Earnings tracking
- Cashout requests

#### ✅ Technical Features
- Offline support with local caching
- Background synchronization
- Push notifications (Firebase)
- Network status monitoring
- PDF export (amortization schedules)
- Real-time updates (Supabase)

## 🗄️ Database

**Backend**: Supabase (PostgreSQL)
**Location**: [`/docs/database`](./docs/database)

### Tables
- `users` - User accounts and authentication
- `accounts` - Borrower profiles
- `loans` - Loan applications and details
- `payments` - Payment records
- `notifications` - System notifications
- `earnings` - Agent commission tracking
- `cashout_requests` - Agent cashout requests
- `transactions` - Audit log

**Setup:**
```bash
# Run the setup script in your Supabase SQL editor
cat docs/database/setup.sql
```

📚 [Database Schema](./docs/database/setup.sql)

## 📐 Architecture

Both platforms follow clean architecture principles:

```
├── Presentation Layer
│   ├── Views/Screens (SwiftUI/Compose)
│   └── ViewModels (MVVM)
│
├── Domain Layer
│   ├── Use Cases
│   └── Business Logic
│
├── Data Layer
│   ├── Repositories
│   ├── Remote Data Sources (Supabase)
│   └── Local Data Sources (Room/Core Data)
│
└── Core
    ├── Network Monitoring
    ├── Utilities
    └── Extensions
```

## 🚀 Getting Started

### Prerequisites

**For Android:**
- JDK 11 or higher
- Android Studio Hedgehog or later
- Android SDK API 34

**For iOS:**
- macOS Ventura (13.0) or later
- Xcode 15.0 or later
- iOS 16.0+ device or simulator

**For Both:**
- Supabase account
- Firebase project

### Quick Setup

1. **Clone the repository**
```bash
cd /Users/rgravador/Desktop/trainings/Loanstar
```

2. **Configure Supabase**

**Android:**
```bash
cd android
cp local.properties.sample local.properties
# Edit local.properties and add your Supabase credentials
```

**iOS:**
```bash
cd ios/LoanstariOS
cp Config.xcconfig.template Config.xcconfig
# Edit Config.xcconfig and add your Supabase credentials
```

3. **Configure Firebase**

**Android:**
- Download `google-services.json` from Firebase Console
- Place in `android/app/google-services.json`

**iOS:**
- Download `GoogleService-Info.plist` from Firebase Console
- Add to Xcode project

4. **Set up database**
```bash
# Execute the setup script in your Supabase SQL editor
cat docs/database/setup.sql
```

5. **Build and run**

**Android:**
```bash
cd android
./gradlew installDebug
```

**iOS:**
```bash
cd ios/LoanstariOS
open Package.swift
# In Xcode: Cmd+R to run
```

## 📊 Project Structure

```
Loanstar/
├── android/                    # Android application
│   ├── app/
│   │   └── src/
│   │       └── main/
│   │           ├── java/com/example/loanstar/
│   │           └── res/
│   ├── build.gradle.kts
│   ├── CLAUDE.md              # Android development guide
│   ├── README.md              # Android README
│   └── local.properties.sample
│
├── ios/                       # iOS application
│   ├── LoanstariOS/           # Main iOS project
│   │   ├── Sources/
│   │   │   ├── Core/
│   │   │   ├── Data/
│   │   │   ├── UI/
│   │   │   └── Utils/
│   │   ├── Tests/
│   │   ├── Package.swift
│   │   ├── README.md          # iOS documentation
│   │   └── Config.xcconfig.template
│   └── LoanstarApp/           # Swift package (alternative)
│
├── docs/                      # Documentation
│   ├── android/              # Android-specific docs
│   │   ├── IMPLEMENTATION_PROGRESS.md
│   │   └── SETUP_GUIDE.md
│   ├── ios/                  # iOS-specific docs
│   │   ├── BUILD_STATUS.md
│   │   ├── CURRENT_STATUS.md
│   │   ├── iOS_TRANSFORMATION_SUMMARY.md
│   │   ├── LATEST_FIX.md
│   │   ├── XCODE_SETUP_GUIDE.md
│   │   └── create_ios_project.sh
│   └── database/             # Database documentation
│       ├── setup.sql
│       ├── add_rls_policies.sql
│       └── insert_test_agent.sql
│
├── README.md                 # This file
├── req.md                    # Requirements document
└── .gitignore
```

## 🧪 Testing

### Android
```bash
cd android
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

### iOS
```bash
cd ios/LoanstariOS
swift test                        # Command line
# Or in Xcode: Cmd+U
```

## 🔐 Security

- ✅ Token-based authentication
- ✅ Row Level Security (RLS) policies in Supabase
- ✅ Encrypted local storage
- ✅ HTTPS-only communication
- ✅ Input validation on client and server
- ✅ Secure credential storage
- ⚠️ API keys stored in `.gitignore`d files

## 🎓 Technology Stack

### Backend
- **Database**: PostgreSQL (via Supabase)
- **Authentication**: Supabase Auth
- **Real-time**: Supabase Realtime
- **Storage**: Supabase Storage
- **Push Notifications**: Firebase Cloud Messaging

### Android
- **Language**: Kotlin 1.9.24
- **UI**: Jetpack Compose / XML
- **Architecture**: MVVM
- **DI**: Hilt
- **Local DB**: Room
- **Networking**: Retrofit, OkHttp, Supabase SDK
- **Async**: Coroutines, Flow

### iOS
- **Language**: Swift 5.9+
- **UI**: SwiftUI
- **Architecture**: MVVM
- **DI**: Manual/Singletons
- **Local DB**: Core Data
- **Networking**: URLSession, Supabase SDK
- **Async**: async/await, Combine

## 📊 Statistics

- **Total Lines of Code**: 10,000+
  - Android: ~5,000 lines (Kotlin)
  - iOS: ~3,000 lines (Swift)
  - Documentation: ~2,000 lines
- **Files**: 100+ across both platforms
- **Features**: 30+ implemented
- **Test Coverage**: Growing

## 🗺️ Roadmap

### Phase 1: MVP ✅
- ✅ Authentication
- ✅ Account management
- ✅ Loan creation
- ✅ Payment recording
- ✅ Basic dashboard

### Phase 2: Enhanced Features (In Progress)
- 🚧 Biometric authentication
- 🚧 Advanced analytics
- ⏳ Batch operations
- ⏳ Export reports
- ⏳ Multi-language support

### Phase 3: Advanced (Planned)
- ⏳ Widgets (iOS/Android)
- ⏳ Apple Watch companion
- ⏳ iPad optimization
- ⏳ Dark mode
- ⏳ Accessibility improvements

## 📚 Documentation

### Android
- [CLAUDE.md](./android/CLAUDE.md) - Development guide
- [README.md](./android/README.md) - Android overview
- [Setup Guide](./docs/android/SETUP_GUIDE.md)
- [Implementation Progress](./docs/android/IMPLEMENTATION_PROGRESS.md)

### iOS
- [README.md](./ios/LoanstariOS/README.md) - Complete iOS documentation
- [Quick Start](./ios/LoanstariOS/QUICKSTART.md)
- [Migration Guide](./ios/LoanstariOS/MIGRATION_GUIDE.md) - For Android developers
- [Xcode Setup](./docs/ios/XCODE_SETUP_GUIDE.md)
- [Build Status](./docs/ios/BUILD_STATUS.md)
- [Transformation Summary](./docs/ios/iOS_TRANSFORMATION_SUMMARY.md)

### Database
- [Schema Setup](./docs/database/setup.sql)
- [RLS Policies](./docs/database/add_rls_policies.sql)
- [Test Data](./docs/database/insert_test_agent.sql)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

[Add your license here]

## 📞 Support

For issues, questions, or contributions:
- 📧 Email: [your-email@example.com]
- 🐛 Issues: [GitHub Issues](https://github.com/your-repo/issues)
- 📚 Documentation: See `docs/` folder

## 🏆 Acknowledgments

- Supabase team for excellent backend infrastructure
- Firebase for push notification services
- The Android and iOS developer communities

---

**Version**: 1.0.0  
**Last Updated**: October 28, 2025  
**Status**: Active Development

Made with ❤️ for financial inclusion
