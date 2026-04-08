# YANG Compiler — User Guide

## Table of Contents
1. [Make Application Package](#make-application-package)
2. [Specification of Settings](#specification-of-settings)
3. [Compile YANG Modules](#compile-yang-modules)

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
      "url": "http:proxy.mydomain.com:8080",
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
