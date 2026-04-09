@echo off
REM Release script for YANG Compiler (Windows version)
REM This script helps prepare and publish a new release

setlocal enabledelayedexpansion

echo ======================================
echo YANG Compiler Release Script
echo ======================================

REM Check if version is provided
if "%1"=="" (
    echo Error: Version number is required
    echo Usage: %0 ^<version^> [snapshot^|release]
    echo Example: %0 1.0.0 release
    exit /b 1
)

set VERSION=%1
set RELEASE_TYPE=%2
if "%RELEASE_TYPE%"=="" set RELEASE_TYPE=release

echo Version: %VERSION%
echo Release Type: %RELEASE_TYPE%

REM Verify we're on the main branch
for /f "delims=" %%i in ('git rev-parse --abbrev-ref HEAD') do set CURRENT_BRANCH=%%i
if not "%CURRENT_BRANCH%"=="main" (
    if not "%CURRENT_BRANCH%"=="master" (
        echo Warning: You are not on main/master branch
        set /p CONTINUE="Continue anyway? (y/n) "
        if /i not "!CONTINUE!"=="y" (
            echo Aborted
            exit /b 1
        )
    )
)

REM Ensure working directory is clean
for /f "delims=" %%i in ('git status --porcelain') do set HAS_CHANGES=%%i
if defined HAS_CHANGES (
    echo Error: Working directory is not clean
    echo Please commit or stash your changes first
    exit /b 1
)

echo.
echo Step 1: Running tests...
call mvn clean test
if errorlevel 1 (
    echo Tests failed! Aborting release.
    exit /b 1
)
echo [OK] Tests passed

echo.
echo Step 2: Updating version in pom.xml...
if "%RELEASE_TYPE%"=="snapshot" (
    set SNAPSHOT_VERSION=%VERSION%-SNAPSHOT
    call mvn versions:set -DnewVersion=!SNAPSHOT_VERSION!
) else (
    call mvn versions:set -DnewVersion=%VERSION%
)
echo [OK] Version updated

echo.
echo Step 3: Building package...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo Build failed! Aborting release.
    exit /b 1
)
echo [OK] Build successful

echo.
echo Step 4: Creating git tag...
git add pom.xml
git commit -m "Release version %VERSION%"
git tag -a "v%VERSION%" -m "Release v%VERSION%"
echo [OK] Git tag created: v%VERSION%

echo.
echo Step 5: Preparing for Maven Central deployment...
echo To deploy to Maven Central, run:
echo   mvn clean deploy -P release
echo.
echo Make sure you have configured:
echo   - %%USERPROFILE%%\.m2\settings.xml with OSSRH credentials
echo   - GPG key for signing
echo.

echo ======================================
echo Release preparation complete!
echo ======================================
echo.
echo Next steps:
echo 1. Review the changes: git log -1
echo 2. Push to remote: git push origin main --tags
echo 3. Deploy to Maven Central: mvn clean deploy -P release
echo 4. Create GitHub release from tag v%VERSION%

endlocal
