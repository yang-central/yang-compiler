# YANG Compiler — User Guide

## New Features (v1.4.0+)

### Python Wrapper
A Python interface is now available for programmatic access to YANG Compiler:

```python
from yang_compiler import YangCompiler

compiler = YangCompiler(auto_download=True)
result = compiler.compile(modules=['ietf-interfaces'])
```

See [Python Package Documentation](../python/README.md) for details.

### Homebrew Installation (macOS)
macOS users can now install via Homebrew:

```bash
brew tap yang-central/yang-compiler
brew install yang-compiler
```

---

## Table of Contents
1. [Quick CLI Reference](#quick-cli-reference)
2. [Make Application Package](#make-application-package)
3. [Specification of Settings](#specification-of-settings)
4. [Compile YANG Modules](#compile-yang-modules)

---

## Quick CLI Reference

The wrapper scripts (`yangc` on Linux/macOS, `yangc.bat` on Windows) expose three modes of operation:

| Invocation | Description |
|---|---|
| `./yangc init` | Scaffold a new project. |
| `./yangc compile <inputs...> [options]` | Zero-config compilation (this section). |
| `./yangc [args...]` | Forward arguments directly to the compiler JAR. |

---

### `yangc compile` — Zero-Config Compilation

Use the `compile` subcommand when you want to validate or process YANG sources without writing a `build.json` first. The CLI wrapper generates a temporary build configuration on the fly, runs the compiler, and discards the temporary file after the run.

#### Syntax

```bash
# Linux/macOS
./yangc compile <inputs...> [--plugin <name>] [--param key=value ...]

# Windows
.\yangc.bat compile <inputs...> [--plugin <name>] [--param key=value ...]
```

#### Input Forms

Each positional argument is an **input**. You can supply one or more inputs in a single command. The following forms are supported:

| Form | Example | Description |
|---|---|---|
| Directory | `./yang` | All `.yang` files in the directory are compiled. Maps to the `dir` key in build.json. |
| `.yang` file | `my-model.yang` | A single YANG source file. Maps to the `file` key in build.json. |
| Module name | `ietf-interfaces` | A module resolved from the local or remote repository. Maps to the `module` key in build.json with an empty revision. |
| Module with revision | `ietf-interfaces@2018-02-20` | A specific revision of a module. Maps to the `module` key in build.json with the given revision date. |

Multiple inputs of different forms may be combined in a single command, for example:

```bash
./yangc compile ./yang ietf-interfaces@2018-02-20
```

#### Options

| Option | Description |
|---|---|
| `--plugin <name>` | Name of the plugin to run. **Default:** `validator_plugin`. Only one `--plugin` is allowed per invocation. |
| `--param key=value` | Pass a parameter to the plugin. May be repeated for multiple parameters. |

#### Default Plugin Behaviour

When `--plugin` is not specified, `validator_plugin` is used automatically. The validation result is written to `validator.txt` in the current working directory.

If you specify `--param`, only the parameters you provide are passed to the plugin (no implicit defaults are added).

#### Single-Plugin Restriction

The `compile` subcommand is designed for quick, single-plugin workflows. Supplying `--plugin` more than once is an error:

```
Error: Only one --plugin is allowed in quick CLI mode.
For multiple plugins, use a build.json file and run: ./yangc option=build.json
```

When you need to run multiple plugins in one pass, create a `build.json` and use the standard invocation (see [Running the Compiler](#running-the-compiler)).

#### When to Use `build.json` Instead

Use a `build.json` file when you need any of the following:

* Multiple plugins in a single compilation run.
* Complex plugin parameters (e.g., list-valued parameters like `tag`).
* Reproducible CI/CD builds that should not depend on CLI flags.
* Custom `settings.json` path.
* The `install` flag (copy compiled files into the local repository).

Run `./yangc init` to generate a starter `build.json` and `settings.json`.

#### Examples

```bash
# Validate all YANG files in a directory (default plugin)
./yangc compile ./yang

# Validate two individual YANG files
./yangc compile a.yang b.yang

# Resolve and validate a module by name
./yangc compile ietf-interfaces

# Resolve a specific revision and run a custom plugin with a parameter
./yangc compile ietf-interfaces@2018-02-20 --plugin yangtree_generator --param output=tree

# Mix directory and module inputs
./yangc compile ./yang ietf-interfaces@2018-02-20

# Windows equivalents
.\yangc.bat compile .\yang
.\yangc.bat compile ietf-interfaces@2018-02-20 --plugin yangtree_generator --param output=tree
```

---

## Make Application Package

After building the project with `mvn clean install`, you need to assemble an application directory before running the compiler.

### Steps

1. Create a directory anywhere on your computer. The recommended name is `yang-compiler-x.y.z` (e.g., `yang-compiler-1.0.0`).
2. Copy `yang-compiler-1.0-SNAPSHOT.jar` and the `libs` directory (both generated under `target/`) into the application directory.
3. *(Optional)* Place `settings.json` into the application directory if you need custom settings.
4. *(Optional)* If external plugins are needed, create a sub-directory named `plugins` under the application directory, then place `plugins.json` inside it.

### Example Application Package Layout

```
|--yang-compiler-1.0.0
   |--libs
   |--plugins
   |----plugins.json
   |--settings.json
   |--yang-compiler-1.0.0.jar
```

---

## Specification of Settings

The `settings.json` file controls global compiler behaviour such as the local repository path, remote repository URL, proxy settings, and manually specified module locations.

### Fields

| Field | Description |
|---|---|
| `local-repository` | Local repo directory used to find missing YANG module dependencies. Defaults to `{user.home}/.yang`. |
| `remote-repository` | Remote URL from which YANG module dependencies are fetched. Defaults to [yangcatalog](https://yangcatalog.org/api/). |
| `proxy.url` | Proxy URL including port number. Required if you cannot access the internet directly. |
| `proxy.authentication.username` | Proxy username (if required by the proxy). |
| `proxy.authentication.password` | Proxy password (if required by the proxy). |
| `token` | Authentication token for the remote repository (if required). |
| `module-info[].name` | Module name. **Mandatory.** |
| `module-info[].revision` | Revision date. **Mandatory.** |
| `module-info[].schema` | URL where the YANG schema is stored. Used when the module is not available in the local or remote repository. |

### Example `settings.json`

```json
{
  "settings": {
    "local-repository": "/Users/llly/yang",
    "remote-repository": "https://yangcatalog.org/api/",
    "proxy": {
      "url": "http://proxy.mydomain.com:8080",
      "authentication": {
        "username": "foo",
        "password": "bar"
      }
    },
    "module-info": [
      {
        "name": "openconfig-acl",
        "revision": "2022-01-14",
        "schema": "https://raw.githubusercontent.com/openconfig/public/master/release/models/acl/openconfig-acl.yang"
      },
      {
        "name": "openconfig-packet-match-types",
        "revision": "2021-07-14",
        "schema": "https://raw.githubusercontent.com/openconfig/public/master/release/models/acl/openconfig-packet-match-types.yang"
      }
    ]
  }
}
```

---

## Compile YANG Modules

### Project Layout

1. Create a project directory anywhere on your computer.
2. Create a sub-directory (e.g., `yang/`) to hold the YANG source files you want to compile.
3. Place a `build.json` file in the project root to specify compilation options.
4. Run the compiler.

#### Example Project Layout

```
|--yang-test (project name)
   |--yang   (YANG modules to be compiled)
   |--build.json
```

### Specification of Compilation Options (`build.json`)

| Field | Description |
|---|---|
| `yang` | Source YANG information. Supports `dir` (directory list), `file` (file list), `module` (module info list), or any combination. |
| `settings` | Path to `settings.json`. Optional — defaults to `{user.home}/.yang/settings.json`. |
| `plugin` | JSON array of plugin invocations. Each entry specifies a `name` and an optional `parameter` array (each with `name` and `value`). |

### Example `build.json`

```json
{
  "yang": {
    "module": [
      {
        "name": "ietf-interfaces",
        "revision": ""
      },
      {
        "name": "huawei-ifm",
        "revision": "2022-08-06"
      },
      {
        "name": "huawei-bgp",
        "revision": ""
      },
      {
        "name": "huawei-network-instance",
        "revision": ""
      }
    ],
    "dir": [
      "yang/ietf",
      "yang/huawei"
    ],
    "file": [
      "yang/ietf/ietf-interfaces.yang",
      "yang/huawei/huawei-bgp.yang"
    ]
  },
  "settings": "settings.json",
  "plugin": [
    {
      "name": "validator_plugin",
      "parameter": [
        {
          "name": "output",
          "value": "yang/validator.txt"
        }
      ]
    },
    {
      "name": "yangtree_generator",
      "parameter": [
        {
          "name": "output",
          "value": "tree"
        },
        {
          "name": "expand-grouping",
          "value": "true"
        }
      ]
    },
    {
      "name": "yang_statistics",
      "parameter": [
        {
          "name": "output",
          "value": "statistics/node_description.xlsx"
        },
        {
          "name": "tag",
          "value": [
            {
              "name": "operation-exclude",
              "keyword": "huawei-extension:operation-exclude"
            }
          ]
        }
      ]
    }
  ]
}
```

### Running the Compiler

#### Using the CLI wrapper (recommended)

```bash
# Linux/macOS
./yangc                              # uses build.json in the current directory
./yangc option=my-build.json install # custom build file + install compiled files

# Windows
.\yangc.bat
.\yangc.bat option=my-build.json install
```

#### Using `java -jar` directly

```bash
java -jar yang-compiler-1.0-SNAPSHOT.jar [option=<build.json>] [install]
```

#### Parameters

| Parameter | Description |
|---|---|
| `option` | *(Optional)* Path to the build configuration file. Defaults to `build.json` in the current directory. |
| `install` | *(Optional)* If present, all successfully compiled YANG files are copied into the local repository. |
