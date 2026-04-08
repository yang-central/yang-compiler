# YANG Compiler

## Overview

YANG Compiler is a tool based on [YangKit](https://github.com/yang-central/yangkit) that solves the problem of YANG module compilation dependencies. When validating a YANG module, every missing dependency must be available on the path. Because dependencies are chained, this can become very tedious. YANG Compiler solves this by resolving and downloading dependencies automatically:

1. Search the YANG sources being compiled.
2. Search the local repository (`{user.home}/.yang` by default, or as configured in `settings.json`).
3. Use module information in `settings.json` (if provided) to download the missing file.
4. Search the remote repository ([yangcatalog](https://yangcatalog.org/api/) by default).

Downloaded dependencies are cached in the local repository for future use. YANG Compiler also provides a plugin system so developers can extend it with custom functionality.

## Installation

### Prerequisites

* JDK or JRE 1.8 or above

### Obtain and build

```bash
git clone https://github.com/yang-central/yang-compiler.git
cd yang-compiler
mvn clean install
```

This generates `yang-compiler-1.0-SNAPSHOT.jar` and a `libs` directory under `target/`.

## Quick Start

Wrapper scripts (`yangc` on Linux/macOS, `yangc.bat` on Windows) are provided so you never need to type the full `java -jar` command.

```bash
# Linux/macOS — make executable once
chmod +x yangc
```

### Scaffold a new project

```bash
./yangc init
```

Creates a `yang/` directory, a default `build.json`, and a default `settings.json`.

### Compile YANG sources (zero-config)

```bash
./yangc compile <inputs...> [--plugin <name>] [--param key=value ...]
```

Each input can be a directory, a `.yang` file, a module name, or a `module@revision` pair.
The default plugin is `validator_plugin`; results are written to `validator.txt`.

### Use a full build.json

```bash
./yangc                              # uses build.json in the current directory
./yangc option=my-build.json install # custom build file + install compiled files
```

## Examples

```bash
./yangc init
./yangc compile ./yang
./yangc compile ietf-interfaces
./yangc compile ietf-interfaces@2018-02-20 --plugin yangtree_generator --param output=tree
```

## Documentation

* [User Guide](docs/user-guide.md) — CLI reference, settings, build options, and more.
* [Developer Guide](docs/developer-guide.md) — how to develop built-in and external plugins.
