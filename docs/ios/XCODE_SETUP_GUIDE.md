# Xcode Project Setup Guide

## Current Status

✅ Xcode has opened with `Package.swift`
⚠️ Need to create proper iOS App target to run on simulator/device

## Option 1: Create New iOS App Project in Xcode (Recommended)

Since we have all the source code ready, the fastest way is to create a new project in Xcode:

### Steps:

1. **In Xcode (should be open now):**
   - File → New → Project (or press `Shift + Cmd + N`)

2. **Choose Template:**
   - Select **iOS** tab
   - Choose **App**
   - Click **Next**

3. **Configure Project:**
   ```
   Product Name: LoanstarIOS
   Team: [Select your team]
   Organization Identifier: com.loanstar
   Bundle Identifier: com.loanstar.LoanstarIOS (auto-filled)
   Interface: SwiftUI
   Language: Swift
   Storage: None
   Include Tests: ✓ (checked)
   ```
   - Click **Next**

4. **Save Location:**
   - Navigate to: `/Users/rgravador/Desktop/trainings/Loanstar/`
   - Create new folder: `LoanstarIOSApp`
   - Click **Create**

5. **Add Our Source Files:**
   Once project is created:

   a. **Delete the default files** Xcode created:
      - Right-click on `LoanstarIOSApp.swift` → Delete → Move to Trash
      - Right-click on `ContentView.swift` → Delete → Move to Trash

   b. **Add our source files:**
      - Right-click on `LoanstarIOSApp` folder in project navigator
      - Add Files to "LoanstarIOSApp"...
      - Navigate to: `/Users/rgravador/Desktop/trainings/Loanstar/LoanstariOS/Sources`
      - Select all folders (Core, Data, UI, Utils, LoanstariOSApp.swift, ContentView.swift)
      - ✓ Check "Copy items if needed"
      - ✓ Check "Create groups"
      - ✓ Add to targets: LoanstarIOSApp
      - Click **Add**

6. **Add Dependencies:**
   - File → Add Package Dependencies...
   - Enter: `https://github.com/supabase-community/supabase-swift.git`
   - Dependency Rule: Up to Next Major Version, 2.0.0
   - Click **Add Package**
   - Select: Supabase, Auth, PostgREST, Realtime, Storage
   - Click **Add Package**

   - Repeat for Firebase:
   - Enter: `https://github.com/firebase/firebase-ios-sdk.git`
   - Version: 10.20.0
   - Select: FirebaseMessaging, FirebaseAnalytics
   - Click **Add Package**

7. **Configure Info.plist:**
   - Select `Info.plist` in project navigator
   - Add the following keys (if not present):
     ```
     UIApplicationSceneManifest → UIApplicationSupportsMultipleScenes: YES
     UIBackgroundModes → Array → remote-notification
     ```

8. **Build and Run:**
   - Select a simulator (e.g., iPhone 15)
   - Press `Cmd + R` or click the Play button
   - ✅ App should build and launch!

## Option 2: Use Swift Playgrounds (Quick Test)

If you just want to test SwiftUI views quickly:

1. In Xcode:
   - File → New → Playground
   - Choose iOS → Blank
   - Save as "LoanstarPlayground"

2. Paste this code:
```swift
import SwiftUI
import PlaygroundSupport

// Paste any View from our source here
struct LoginView: View {
    @State private var username = ""
    @State private var password = ""

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.purple.opacity(0.6), Color.blue.opacity(0.4)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            VStack(spacing: 30) {
                Image(systemName: "star.circle.fill")
                    .resizable()
                    .frame(width: 100, height: 100)
                    .foregroundStyle(.white)

                Text("Loanstar")
                    .font(.system(size: 40, weight: .bold, design: .rounded))
                    .foregroundColor(.white)
            }
        }
    }
}

// Show in live view
PlaygroundPage.current.setLiveView(LoginView())
```

3. Show live view: Editor → Live View (or Cmd + Option + Enter)

## Option 3: Command Line Build (Swift Package)

The Package.swift is configured, but it's for a library/executable, not an iOS app. To use it:

```bash
cd /Users/rgravador/Desktop/trainings/Loanstar/LoanstariOS

# Resolve dependencies
swift package resolve

# Build (won't create runnable iOS app, just compiles)
swift build
```

**Note:** This won't give you a runnable iOS app - use Option 1 for that.

## Troubleshooting

### "No such module 'Supabase'"
**Solution:** Add package dependencies (Step 6 above)

### "Cannot find 'SupabaseManager' in scope"
**Solution:** Make sure all source files are added to the target (Step 5b above)

### Build errors with Firebase
**Solution:**
1. Download `GoogleService-Info.plist` from Firebase Console
2. Add to project (drag into Xcode, check "Copy items if needed")

### "The app couldn't be launched"
**Solution:**
1. Product → Clean Build Folder (Shift + Cmd + K)
2. Try running again

## Quick Visual Guide

```
Xcode Window
├── Navigator (left sidebar)
│   ├── Project Navigator (folder icon)
│   │   └── LoanstarIOSApp
│   │       ├── Core/
│   │       ├── Data/
│   │       ├── UI/
│   │       ├── Utils/
│   │       ├── LoanstariOSApp.swift
│   │       ├── ContentView.swift
│   │       ├── Assets.xcassets
│   │       └── Info.plist
│   └── Package Dependencies
│       ├── supabase-swift
│       └── firebase-ios-sdk
│
├── Editor (center)
│   └── [Your selected file content]
│
├── Inspector (right sidebar)
│   └── File Inspector, Identity Inspector, etc.
│
└── Debug Area (bottom)
    ├── Console
    └── Variables View
```

## What Happens When You Run

1. ✅ Xcode builds the project
2. ✅ Simulator launches
3. ✅ App installs on simulator
4. ✅ You see the login screen with:
   - Purple/blue gradient background
   - Star icon
   - "Loanstar" title
   - Username field
   - Password field
   - Sign In button
   - Sign Up link

## Next Steps After Running

1. **Configure Supabase:**
   - Create `Config.xcconfig` file
   - Add your Supabase URL and key

2. **Test Authentication:**
   - Try creating an account
   - Try logging in

3. **Explore the App:**
   - Navigate through tabs
   - Create accounts
   - Add loans
   - View dashboard

## Need Help?

- See `README.md` for comprehensive documentation
- See `QUICKSTART.md` for detailed setup
- See `MIGRATION_GUIDE.md` if coming from Android

---

**Current Status:** Xcode is open with Package.swift
**Recommended:** Follow Option 1 above to create a proper iOS App project
