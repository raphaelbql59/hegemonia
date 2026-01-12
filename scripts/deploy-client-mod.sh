#!/bin/bash
# ============================================================================
# Hegemonia Client Mod - Auto Deploy Script
# ============================================================================
# This script builds and deploys the client mod to:
# 1. Launcher API modpack directory (for auto-download)
# 2. Project root (for manual testing)
# ============================================================================

set -e

PROJECT_ROOT="/home/hegemonia/hegemonia-project"
CLIENT_MOD_DIR="$PROJECT_ROOT/client-mod"
API_MODS_DIR="$PROJECT_ROOT/launcher/api/modpack/mods"
OUTPUT_FILE="hegemonia-client-1.0.0.jar"

echo "╔══════════════════════════════════════════╗"
echo "║   HEGEMONIA CLIENT MOD - Auto Deploy     ║"
echo "╚══════════════════════════════════════════╝"
echo ""

# Build the client mod
echo "📦 Building client mod..."
cd "$CLIENT_MOD_DIR"
./gradlew build --quiet

# Check if build succeeded
if [ ! -f "$CLIENT_MOD_DIR/build/libs/$OUTPUT_FILE" ]; then
    echo "❌ Build failed - JAR not found"
    exit 1
fi

# Get file size
SIZE=$(stat -c %s "$CLIENT_MOD_DIR/build/libs/$OUTPUT_FILE")
echo "✓ Build successful (size: $SIZE bytes)"

# Deploy to API modpack directory
echo ""
echo "🚀 Deploying to launcher API..."
mkdir -p "$API_MODS_DIR"
cp "$CLIENT_MOD_DIR/build/libs/$OUTPUT_FILE" "$API_MODS_DIR/"
echo "✓ Deployed to $API_MODS_DIR/$OUTPUT_FILE"

# Deploy to project root for easy access
echo ""
echo "📁 Copying to project root..."
cp "$CLIENT_MOD_DIR/build/libs/$OUTPUT_FILE" "$PROJECT_ROOT/"
echo "✓ Copied to $PROJECT_ROOT/$OUTPUT_FILE"

# Update manifest with new size if needed
echo ""
echo "📝 Updating manifests..."
# Update TypeScript API manifest
sed -i "s/size: [0-9]*, \/\/ hegemonia-client/size: $SIZE, \/\/ hegemonia-client/g" \
    "$PROJECT_ROOT/launcher/api/src/routes/modpack.ts" 2>/dev/null || true

echo ""
echo "════════════════════════════════════════════"
echo "✅ Deployment complete!"
echo ""
echo "The mod is now available at:"
echo "  • API: http://localhost:3001/api/modpack/mods/$OUTPUT_FILE"
echo "  • File: $PROJECT_ROOT/$OUTPUT_FILE"
echo ""
echo "Launchers will auto-download on next install."
echo "════════════════════════════════════════════"
