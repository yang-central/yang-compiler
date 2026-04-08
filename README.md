# YANG Compiler

## Overview

YANG Compiler is a tool based on [YangKit](https://github.com/yang-central/yangkit). It is designed to solve the problem of YANG file compilation dependencies.

When validating a YANG module, every missing dependency must be available on the path. Because dependencies are chained, this can become very tedious. YANG Compiler solves this by resolving and downloading dependencies automatically:

1. Search the YANG sources being compiled.
2. Search the local repository (`{user.home}/.yang` by default, or as configured in `settings.json`).
3. Use module information in `settings.json` (if provided) to download the missing file.
4. Search the remote repository ([yangcatalog](https://yangcatalog.org/api/) by default).

Downloaded dependencies are cached in the local repository for future use. YANG Compiler also provides a plugin system so developers can extend it with custom functionality.

## Architecture

![yang-compiler](src/main/resources/yang-compiler.png)

## Specification

* Automatically searches and downloads dependencies.
* Customisable local repository (`{user.home}/.yang` is the default).
* Customisable remote repository ([yangcatalog](https://yangcatalog.org/api/) is the default).
* Proxy support.
* Ability to specify module information directly for modules not in yangcatalog.
* Optional installation of successfully compiled YANG files into the local repository.
* Extensible plugin system.

## Installation

### Prerequisites

* JDK or JRE 1.8 or above

### Obtain code

```bash
git clone https://github.com/yang-central/yang-compiler.git
```

### Build code

```bash
cd yang-compiler
mvn clean install
```

This generates `yang-compiler-1.0-SNAPSHOT.jar` and a `libs` directory under `target/`.

## Quick Start (CLI)

Wrapper scripts are provided so you never need to type the full `java -jar` command.

### Scaffold a new project

Run `init` once inside an empty project directory to generate boilerplate configuration files:

```bash
# Linux/macOS
chmod +x yangc        # only needed once
./yangc init

# Windows
.\yangc.bat init
```

This creates:
* `yang/` — place your YANG source files here.
* `build.json` — minimal compilation configuration.
* `settings.json` — default settings pointing to `~/.yang` and yangcatalog.

### Run the compiler

```bash
# Linux/macOS
./yangc                                 # uses build.json in the current directory
./yangc option=my-build.json install    # custom build file + install compiled files

# Windows
.\yangc.bat
.\yangc.bat option=my-build.json install
```

All arguments are forwarded to the underlying Java process unchanged.

> **Note:** The wrapper requires `target/yang-compiler-1.0-SNAPSHOT.jar`. Run `mvn clean install` first if you see a "not found" error.

## Documentation

* [User Guide](docs/user-guide.md) — settings reference, build options, application packaging, and more.
* [Developer Guide](docs/developer-guide.md) — how to develop built-in and external plugins.
