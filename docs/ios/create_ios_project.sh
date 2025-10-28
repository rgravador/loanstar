#!/bin/bash

# Loanstar iOS Project Setup Script
# This script creates a proper iOS app project in Xcode

set -e

echo "🚀 Creating Loanstar iOS App Project..."

PROJECT_DIR="/Users/rgravador/Desktop/trainings/Loanstar"
APP_NAME="LoanstarIOS"
APP_PATH="$PROJECT_DIR/$APP_NAME"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}Step 1: Creating project directory...${NC}"
mkdir -p "$APP_PATH"
cd "$APP_PATH"

echo -e "${BLUE}Step 2: Creating Info.plist...${NC}"
cat > Info.plist << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>en</string>
    <key>CFBundleDisplayName</key>
    <string>Loanstar</string>
    <key>CFBundleExecutable</key>
    <string>$(EXECUTABLE_NAME)</string>
    <key>CFBundleIdentifier</key>
    <string>com.loanstar.app</string>
    <key>CFBundleName</key>
    <string>Loanstar</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>LSRequiresIPhoneOS</key>
    <true/>
    <key>UIApplicationSupportsIndirectInputEvents</key>
    <true/>
    <key>UILaunchScreen</key>
    <dict/>
    <key>UISupportedInterfaceOrientations</key>
    <array>
        <string>UIInterfaceOrientationPortrait</string>
    </array>
</dict>
</plist>
EOF

echo -e "${BLUE}Step 3: Copying source files from LoanstariOS...${NC}"
if [ -d "$PROJECT_DIR/LoanstariOS/Sources" ]; then
    cp -r "$PROJECT_DIR/LoanstariOS/Sources" "$APP_PATH/"
    echo -e "${GREEN}✓ Source files copied${NC}"
else
    echo -e "${YELLOW}⚠ Source directory not found, will need to add files manually${NC}"
fi

echo -e "${BLUE}Step 4: Creating .gitignore...${NC}"
cat > .gitignore << 'EOF'
# Xcode
*.xcodeproj
*.xcworkspace
xcuserdata/
DerivedData/
.DS_Store

# Swift Package Manager
.build/
.swiftpm/

# Configuration
Config.xcconfig
GoogleService-Info.plist
EOF

echo ""
echo -e "${GREEN}✅ Project structure created!${NC}"
echo ""
echo -e "${YELLOW}📝 Next Steps:${NC}"
echo "1. Open Xcode"
echo "2. File → New → Project"
echo "3. Choose: iOS → App"
echo "4. Configure:"
echo "   - Product Name: LoanstarIOS"
echo "   - Team: Select your team"
echo "   - Organization ID: com.loanstar"
echo "   - Interface: SwiftUI"
echo "   - Language: Swift"
echo "   - Storage: None (we'll add Core Data later)"
echo "5. Save to: $APP_PATH"
echo "6. Once created, the source files are already in place!"
echo ""
echo -e "${BLUE}Alternative - Open existing Swift Package:${NC}"
echo "You can also open the Swift Package version:"
echo "cd $PROJECT_DIR/LoanstariOS"
echo "open Package.swift"
echo ""
echo -e "${GREEN}For now, opening Xcode...${NC}"

# Open Xcode
open -a Xcode

echo ""
echo -e "${GREEN}✨ Setup complete! Follow the steps above in Xcode.${NC}"
