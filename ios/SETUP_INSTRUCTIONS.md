# iOS Project Setup Instructions

## Issue: Supabase Module Not Found

The error occurs because the Supabase Swift package hasn't been added to your Xcode project yet.

## Solution: Add Supabase Package to Xcode

Follow these steps to properly configure your Xcode project:

### Step 1: Open Your Project in Xcode

```bash
cd /Users/rgravador/Desktop/trainings/Loanstar/ios/Loanstar
open Loanstar.xcodeproj
```

If you don't have an `.xcodeproj` file yet, you need to create a new iOS project first.

### Step 2: Create New iOS Project (If Needed)

If you don't have an Xcode project yet:

1. Open Xcode
2. Click "Create New Project"
3. Select "iOS" → "App"
4. Fill in:
   - Product Name: **Loanstar**
   - Team: (Select your team)
   - Organization Identifier: **com.loanstar** (or your preference)
   - Interface: **SwiftUI**
   - Language: **Swift**
   - Storage: **None** (we'll add SwiftData later)
5. Click "Next" and choose the `ios/Loanstar` directory
6. Click "Create"

### Step 3: Add Supabase Swift Package

Once you have the Xcode project open:

1. **Click on your project** in the Project Navigator (top-left)
2. **Select the project** (not the target) under "PROJECT"
3. **Click the "Package Dependencies" tab** (in Xcode 15+) or "Swift Packages" tab (older versions)
4. **Click the "+" button** at the bottom of the package list
5. **Enter the package URL:**
   ```
   https://github.com/supabase/supabase-swift.git
   ```
6. **Click "Add Package"**
7. **In the version selection:**
   - Choose "Up to Next Major Version"
   - Minimum: **2.0.0**
8. **Select the products to add:**
   - ✅ Check **Supabase**
   - ✅ Check **PostgREST** (optional, already included)
   - ✅ Check **GoTrue** (optional, for auth)
   - ✅ Check **Realtime** (optional, for realtime features)
   - ✅ Check **Storage** (optional, for file storage)
9. **Click "Add Package"**

Wait for Xcode to download and resolve the package dependencies.

### Step 4: Add Source Files to Project

Now add all the Swift files we created:

1. **Right-click** on the "Loanstar" folder in Project Navigator
2. **Select "Add Files to Loanstar"**
3. **Navigate to** `/Users/rgravador/Desktop/trainings/Loanstar/ios/Loanstar/Loanstar`
4. **Select these folders:**
   - Models
   - Utilities
   - Services
   - Config
5. **Make sure to check:**
   - ✅ "Copy items if needed"
   - ✅ "Create groups" (not folder references)
   - ✅ Add to target: "Loanstar"
6. **Click "Add"**

### Step 5: Update Build Settings (If Needed)

If you still have issues:

1. Select your project in Project Navigator
2. Select the "Loanstar" target
3. Go to "Build Settings"
4. Search for "Swift Language Version"
5. Ensure it's set to **Swift 5** or **Swift 6**

### Step 6: Clean and Build

1. **Clean Build Folder:**
   - Menu: Product → Clean Build Folder
   - Or: Cmd+Shift+K

2. **Build Project:**
   - Menu: Product → Build
   - Or: Cmd+B

The project should now build successfully!

---

## Alternative: Temporary Fix Without Supabase

If you want to proceed without Supabase for now (for testing/development), follow these steps:

### Option 1: Comment Out Supabase Import

I'll create a modified version of SupabaseClient.swift that uses a protocol instead.

### Option 2: Use Mock Implementation

See below for a mock implementation that doesn't require Supabase.

---

## Troubleshooting

### Error: "Unable to find module dependency: 'Supabase'"

**Solution:** Follow Step 3 above to add the package dependency

### Error: "No such module 'Supabase'"

**Solution:**
1. Clean Build Folder (Cmd+Shift+K)
2. Delete DerivedData folder:
   ```bash
   rm -rf ~/Library/Developer/Xcode/DerivedData
   ```
3. Restart Xcode
4. Build again (Cmd+B)

### Error: Package Resolution Failed

**Solution:**
1. Remove the package dependency
2. Clean Build Folder
3. Re-add the package dependency
4. Make sure you have internet connection

### Error: "Could not resolve package dependencies"

**Solution:**
1. Check your network connection
2. Try resetting package caches:
   - File → Packages → Reset Package Caches
3. Try updating packages:
   - File → Packages → Update to Latest Package Versions

---

## Next Steps After Successful Build

Once the project builds successfully:

1. ✅ Verify all files are in the project
2. ✅ Update `AppConfig.swift` with your Supabase credentials (if not already done)
3. ✅ Run the app on simulator (Cmd+R)
4. ✅ Start implementing repositories (Phase 2)

---

## Quick Start Commands

```bash
# Navigate to project
cd /Users/rgravador/Desktop/trainings/Loanstar/ios/Loanstar

# Open in Xcode
open Loanstar.xcodeproj

# Or use xed command
xed .

# Clean build folder (from command line)
xcodebuild clean -project Loanstar.xcodeproj -scheme Loanstar

# Build project (from command line)
xcodebuild build -project Loanstar.xcodeproj -scheme Loanstar
```

---

## Package.swift vs Xcode Project

**Note:** The `Package.swift` file I created is for Swift Package Manager (SPM) projects, not Xcode projects. For an Xcode project (`.xcodeproj`), you need to:

1. Add dependencies through Xcode's UI (as described in Step 3)
2. OR convert to a Swift Package (advanced)

For this project, **use the Xcode UI method** (Step 3 above).

---

## Need Help?

If you continue to have issues:

1. Check Xcode version: **Xcode 15.0+** recommended
2. Check macOS version: **macOS 14.0+** recommended
3. Check Swift version: **Swift 5.9+** required
4. Verify internet connection for package downloads

For more details, see:
- [Supabase Swift Documentation](https://supabase.com/docs/reference/swift/introduction)
- [Swift Package Manager Guide](https://developer.apple.com/documentation/xcode/adding-package-dependencies-to-your-app)
