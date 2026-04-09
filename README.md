# YANG Compiler

## Overview

YANG Compiler is a plugin-extensible tool for compiling and processing YANG models. Built on [YangKit](https://github.com/yang-central/yangkit), it helps developers validate models, resolve dependencies, retrieve missing modules, and build custom workflows through built-in and external plugins.

## Architecture

The compiler resolves input modules and their dependencies through configurable sources, then executes processing steps through a plugin-based workflow.

![YANG Compiler Architecture](src/main/resources/yang-compiler.png)

## Key Capabilities

- Compile and process YANG models from files, directories, and module references
- Resolve dependencies automatically: local repository → `settings.json` module info → remote repository ([yangcatalog](https://yangcatalog.org/api/) by default)
- Cache retrieved modules in the local repository (`{user.home}/.yang`) for future use
- Validate YANG models and write results to a report file via the built-in `validator_plugin`
- Extend processing through built-in or external plugins, each configurable with custom parameters
- Configure global behavior through `settings.json` (repository paths, proxy, authentication)
- Define reproducible build workflows through `build.json` (inputs, plugins, parameters)
- Install compiled YANG files into the local repository with the `install` flag

## Installation

### Prerequisites

* JDK or JRE 1.8 or above

### Installation on macOS

#### Option 1: Manual Installation (Current)
```bash
# Install JDK 8+ using Homebrew
brew install openjdk@8
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)

# Clone and build
git clone https://github.com/yang-central/yang-compiler.git
cd yang-compiler
mvn clean package

# Make script executable
chmod +x yangc

# Verify installation
./yangc --help
```

#### Option 2: Docker (Recommended for Quick Start)
```bash
# Pull and run with Docker
docker pull yangcentral/yang-compiler:latest
docker run -v $(pwd)/yang:/opt/yang-compiler/yang yang-compiler:latest

# Or use Docker Compose
docker-compose up
```

#### Option 3: Homebrew (Recommended)
```bash
# Add the tap
brew tap yang-central/yang-compiler

# Install
brew install yang-compiler

# Verify installation
yangc --help
```

**Benefits of Homebrew installation:**
- ✅ One-command installation
- ✅ Automatic dependency management (Java, Maven)
- ✅ Easy updates: `brew upgrade yang-compiler`
- ✅ Clean uninstall: `brew uninstall yang-compiler`

### Installation on Linux

```bash
# Install JDK 8+
sudo apt-get install openjdk-8-jdk  # Debian/Ubuntu
sudo yum install java-1.8.0-openjdk  # CentOS/RHEL

# Clone and build
git clone https://github.com/yang-central/yang-compiler.git
cd yang-compiler
mvn clean package

# Make script executable
chmod +x yangc
```

### Installation on Windows

```powershell
# Install JDK 8+ from https://adoptium.net/
# Ensure Java is in your PATH

# Clone and build
git clone https://github.com/yang-central/yang-compiler.git
cd yang-compiler
mvn clean package

# Use the batch script
yangc.bat --help
```

### Obtain and build (Generic)

```bash
git clone https://github.com/yang-central/yang-compiler.git
cd yang-compiler
mvn clean install
```

This generates `yang-compiler-*.jar` and a `libs` directory under `target/`.

## Quick Start

Wrapper scripts (`yangc` on Linux/macOS, `yangc.bat` on Windows) are provided so you never need to type the full `java -jar` command.

```bash
# Linux/macOS — make executable once
chmod +x yangc
```

```bash
# Scaffold a new project (creates yang/, build.json, settings.json)
./yangc init

# Compile all YANG files in a directory (default: validator_plugin)
./yangc compile ./yang

# Resolve and compile a module by name
./yangc compile ietf-interfaces

# Compile a specific revision with a custom plugin and parameter
./yangc compile ietf-interfaces@2018-02-20 --plugin yangtree_generator --param output=tree

# Run a full build from build.json
./yangc

# Run a full build and install compiled files into the local repository
./yangc option=my-build.json install
```

## Documentation

* [User Guide](docs/user-guide.md) — CLI reference, settings, build options, and more.
* [Developer Guide](docs/developer-guide.md) — how to develop built-in and external plugins.
