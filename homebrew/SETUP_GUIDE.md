# Homebrew Tap Setup Guide

This guide explains how to set up and maintain the Homebrew tap for YANG Compiler.

## 📋 Prerequisites

- macOS with Homebrew installed
- GitHub account with access to `yang-central` organization
- Git configured

## 🚀 Quick Start

### For Users

Install YANG Compiler via Homebrew:

```bash
# Add the tap
brew tap yang-central/yang-compiler

# Install
brew install yang-compiler

# Verify installation
yangc --help
```

### For Maintainers

## 📁 Repository Structure

The Homebrew tap should be in a separate repository: `yang-central/homebrew-yang-compiler`

```
homebrew-yang-compiler/
├── Formula/
│   └── yang-compiler.rb    # The formula file
├── README.md               # This file
└── .github/
    └── workflows/
        └── ci.yml          # Optional: Test formula on PR
```

## 🔧 Setup Steps

### Step 1: Create the Tap Repository

1. Create a new public repository: `yang-central/homebrew-yang-compiler`
2. Clone it locally:
   ```bash
   git clone https://github.com/yang-central/homebrew-yang-compiler.git
   cd homebrew-yang-compiler
   ```

3. Create the directory structure:
   ```bash
   mkdir -p Formula
   mkdir -p .github/workflows
   ```

4. Copy the formula file from the main repo:
   ```bash
   cp /path/to/yang-compiler/homebrew/Formula/yang-compiler.rb Formula/
   cp /path/to/yang-compiler/homebrew/README.md .
   ```

5. Commit and push:
   ```bash
   git add .
   git commit -m "Initial Homebrew formula for yang-compiler"
   git push origin main
   ```

### Step 2: Configure GitHub Actions

The main repository already has a workflow file at `.github/workflows/homebrew-release.yml`.

You need to set up a secret in the main repository:

1. Go to `yang-central/yang-compiler` → Settings → Secrets and variables → Actions
2. Add a new secret:
   - Name: `BREW_TAP_TOKEN`
   - Value: A GitHub Personal Access Token with `repo` scope

To create the token:
1. Go to GitHub → Settings → Developer settings → Personal access tokens
2. Generate new token (classic)
3. Select scopes: `repo` (Full control of private repositories)
4. Copy the token and save it as `BREW_TAP_TOKEN`

### Step 3: Update Formula for New Release

#### Option A: Automatic (Recommended)

When you create a new release on GitHub:
1. Create a release with tag `v1.4.0` (for example)
2. Upload the source tarball as a release asset
3. The GitHub Actions workflow will automatically:
   - Download the tarball
   - Calculate SHA256
   - Update the formula in the tap repository
   - Create a commit

#### Option B: Manual

Use the helper script:

```bash
cd yang-compiler
./scripts/update-homebrew-formula.sh 1.4.0
```

This will:
1. Download the release tarball
2. Calculate SHA256
3. Generate an updated formula
4. Show you the updated formula

Then manually:
1. Copy the formula to the tap repository
2. Test it locally
3. Commit and push

### Step 4: Test the Formula

Before publishing, test the formula locally:

```bash
# Install from local formula
brew install --build-from-source /path/to/yang-compiler.rb

# Test the installation
yangc --help
yangc init

# Uninstall when done
brew uninstall yang-compiler
```

## 📝 Formula Maintenance

### Updating Dependencies

If YANG Compiler's dependencies change, update the formula:

```ruby
depends_on "maven" => :build
depends_on "openjdk@8"
# Add new dependencies here
```

### Version Bumping

For each new release:
1. Update the `url` to point to the new tarball
2. Update the `sha256` hash
3. Update the `head` branch if needed

### Testing Changes

Always test formula changes:

```bash
# Audit the formula
brew audit --strict yang-compiler

# Test installation
brew install --build-from-source yang-compiler

# Run tests
brew test yang-compiler
```

## 🐛 Troubleshooting

### Issue: Formula not found

```bash
# Make sure the tap is added
brew tap yang-central/yang-compiler

# Update tap
brew update
```

### Issue: Build fails

Check the build logs:
```bash
brew install --verbose --debug yang-compiler
```

Common issues:
- Java not installed: `brew install openjdk@8`
- Maven not installed: `brew install maven`
- Network issues: Check internet connection

### Issue: SHA256 mismatch

Recalculate the hash:
```bash
wget https://github.com/yang-central/yang-compiler/releases/download/v1.4.0/yang-compiler-1.4.0.tar.gz
sha256sum yang-compiler-1.4.0.tar.gz
```

Update the formula with the correct hash.

## 📊 Monitoring

### Check Installation Stats

Homebrew provides analytics:
```bash
# View install stats (if published to homebrew/core)
brew info --analytics yang-compiler
```

### Monitor Issues

Watch the tap repository for issues:
- https://github.com/yang-central/homebrew-yang-compiler/issues

## 🔄 Continuous Integration

Optional: Add CI to test the formula on pull requests.

Create `.github/workflows/ci.yml` in the tap repository:

```yaml
name: CI

on:
  pull_request:
    branches: [main]
  push:
    branches: [main]

jobs:
  test:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Install OpenJDK
        run: brew install openjdk@8
      
      - name: Install Maven
        run: brew install maven
      
      - name: Install formula
        run: brew install --build-from-source ./Formula/yang-compiler.rb
      
      - name: Test formula
        run: |
          yangc --help
          yangc init
```

## 📚 Resources

- [Homebrew Formula Cookbook](https://docs.brew.sh/Formula-Cookbook)
- [Homebrew Creating a Homebrew Formula](https://docs.brew.sh/How-to-Create-and-Maintain-a-Tap)
- [Ruby Style Guide](https://rubystyle.guide/)

## 🤝 Contributing

Contributions to improve the formula are welcome! Please:
1. Fork the tap repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📞 Support

- **Issues**: https://github.com/yang-central/homebrew-yang-compiler/issues
- **Discussions**: https://github.com/yang-central/yang-compiler/discussions
- **Email**: frank.fengchong@huawei.com

---

**Last Updated**: 2026-04-09
