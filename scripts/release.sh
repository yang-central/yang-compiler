#!/bin/bash
# Release script for YANG Compiler
# This script helps prepare and publish a new release

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}YANG Compiler Release Script${NC}"
echo -e "${GREEN}======================================${NC}"

# Check if version is provided
if [ -z "$1" ]; then
    echo -e "${RED}Error: Version number is required${NC}"
    echo "Usage: $0 <version> [snapshot|release]"
    echo "Example: $0 1.0.0 release"
    exit 1
fi

VERSION=$1
RELEASE_TYPE=${2:-release}

echo -e "${YELLOW}Version: ${VERSION}${NC}"
echo -e "${YELLOW}Release Type: ${RELEASE_TYPE}${NC}"

# Verify we're on the main branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$CURRENT_BRANCH" != "main" ] && [ "$CURRENT_BRANCH" != "master" ]; then
    echo -e "${RED}Warning: You are not on main/master branch${NC}"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${RED}Aborted${NC}"
        exit 1
    fi
fi

# Ensure working directory is clean
if [ -n "$(git status --porcelain)" ]; then
    echo -e "${RED}Error: Working directory is not clean${NC}"
    echo "Please commit or stash your changes first"
    exit 1
fi

echo ""
echo -e "${YELLOW}Step 1: Running tests...${NC}"
mvn clean test
if [ $? -ne 0 ]; then
    echo -e "${RED}Tests failed! Aborting release.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Tests passed${NC}"

echo ""
echo -e "${YELLOW}Step 2: Updating version in pom.xml...${NC}"
if [ "$RELEASE_TYPE" = "snapshot" ]; then
    SNAPSHOT_VERSION="${VERSION}-SNAPSHOT"
    mvn versions:set -DnewVersion=$SNAPSHOT_VERSION
else
    mvn versions:set -DnewVersion=$VERSION
fi
echo -e "${GREEN}✓ Version updated${NC}"

echo ""
echo -e "${YELLOW}Step 3: Building package...${NC}"
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed! Aborting release.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Build successful${NC}"

echo ""
echo -e "${YELLOW}Step 4: Creating git tag...${NC}"
git add pom.xml
git commit -m "Release version ${VERSION}"
git tag -a "v${VERSION}" -m "Release v${VERSION}"
echo -e "${GREEN}✓ Git tag created: v${VERSION}${NC}"

echo ""
echo -e "${YELLOW}Step 5: Preparing for Maven Central deployment...${NC}"
echo "To deploy to Maven Central, run:"
echo "  mvn clean deploy -P release"
echo ""
echo "Make sure you have configured:"
echo "  - ~/.m2/settings.xml with OSSRH credentials"
echo "  - GPG key for signing"
echo ""

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}Release preparation complete!${NC}"
echo -e "${GREEN}======================================${NC}"
echo ""
echo "Next steps:"
echo "1. Review the changes: git log -1"
echo "2. Push to remote: git push origin main --tags"
echo "3. Deploy to Maven Central: mvn clean deploy -P release"
echo "4. Create GitHub release from tag v${VERSION}"
