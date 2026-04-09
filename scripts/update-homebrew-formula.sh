#!/bin/bash
# Script to help generate and validate Homebrew Formula

set -e

echo "======================================"
echo "Homebrew Formula Helper"
echo "======================================"
echo ""

# Function to print colored messages
print_info() {
    echo -e "\033[0;34mℹ\033[0m $1"
}

print_success() {
    echo -e "\033[0;32m✓\033[0m $1"
}

print_error() {
    echo -e "\033[0;31m✗\033[0m $1"
}

# Check if version is provided
if [ -z "$1" ]; then
    print_error "Version number is required"
    echo "Usage: $0 <version>"
    echo "Example: $0 1.3.1"
    exit 1
fi

VERSION=$1
TAG="v${VERSION}"

print_info "Generating Homebrew Formula for version ${VERSION}..."

# Download the release tarball
TARBALL_URL="https://github.com/yang-central/yang-compiler/releases/download/${TAG}/yang-compiler-${VERSION}.tar.gz"
print_info "Downloading tarball from: ${TARBALL_URL}"

TEMP_DIR=$(mktemp -d)
cd "${TEMP_DIR}"

if ! wget -q "${TARBALL_URL}" -O "yang-compiler-${VERSION}.tar.gz"; then
    print_error "Failed to download tarball"
    print_info "Make sure the release exists: https://github.com/yang-central/yang-compiler/releases/tag/${TAG}"
    rm -rf "${TEMP_DIR}"
    exit 1
fi

print_success "Tarball downloaded"

# Calculate SHA256
SHA256=$(sha256sum "yang-compiler-${VERSION}.tar.gz" | awk '{print $1}')
print_success "SHA256 calculated: ${SHA256}"

# Update the Formula file
FORMULA_FILE="$(dirname "$0")/../homebrew/Formula/yang-compiler.rb"

if [ ! -f "${FORMULA_FILE}" ]; then
    print_error "Formula file not found: ${FORMULA_FILE}"
    rm -rf "${TEMP_DIR}"
    exit 1
fi

# Create updated formula
UPDATED_FORMULA="${TEMP_DIR}/yang-compiler-updated.rb"

sed "s|PLACEHOLDER_SHA256_HASH|${SHA256}|g" "${FORMULA_FILE}" > "${UPDATED_FORMULA}"
sed -i.bak "s|v1.3.1|${TAG}|g" "${UPDATED_FORMULA}"
sed -i.bak "s|yang-compiler-1.3.1.tar.gz|yang-compiler-${VERSION}.tar.gz|g" "${UPDATED_FORMULA}"

print_success "Formula updated"

# Show the updated formula
echo ""
echo "======================================"
echo "Updated Formula:"
echo "======================================"
cat "${UPDATED_FORMULA}"

echo ""
echo "======================================"
echo "Next Steps:"
echo "======================================"
echo ""
echo "1. Review the formula above"
echo "2. Copy it to: homebrew/Formula/yang-compiler.rb"
echo "3. Test locally:"
echo "   brew install --build-from-source ./yang-compiler.rb"
echo "4. Commit and push to your homebrew tap repository"
echo ""
echo "Or use the automated GitHub Actions workflow:"
echo "  - Create a release with tag: ${TAG}"
echo "  - The workflow will automatically update the formula"
echo ""

# Cleanup
rm -rf "${TEMP_DIR}"

print_success "Done!"
