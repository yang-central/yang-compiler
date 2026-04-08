# YANG Compiler — Developer Guide

## Table of Contents
1. [Plugin Overview](#plugin-overview)
2. [Specification of Plugin Information](#specification-of-plugin-information)
3. [Develop a Built-in Plugin](#develop-a-built-in-plugin)
4. [Develop an External Plugin](#develop-an-external-plugin)

---

## Plugin Overview

The plugin system of YANG Compiler supports two kinds of plugins:

* **Built-in plugin** — lives inside the `yang-compiler` project itself and is packaged with the compiler JAR.
* **External plugin** — can reside anywhere on the file system; it is loaded at runtime from the `plugins/` directory of the application package.

Plugins allow developers to extend the compiler with customised functions, such as YANG validation, tree diagram generation, statistics reports, or schema comparisons.

---

## Specification of Plugin Information

Each plugin entry in `plugins.json` supports the following fields:

| Field | Description |
|---|---|
| `name` | Unique identifier for the plugin. **Must be unique across all plugins.** |
| `class-path` | Path to the plugin JAR. **Required for external plugins only.** May be a relative path (relative to the `plugins/` directory of the application) or an absolute path. |
| `class` | Fully-qualified class name that implements `org.yangcentral.yangkit.compiler.plugin.YangCompilerPlugin`. |
| `description` | Human-readable description of what the plugin does. |
| `parameter` | JSON array of parameter descriptors. Each entry **must** provide `name` and `description`. |

---

## Develop a Built-in Plugin

Built-in plugins are compiled and packaged together with the compiler.

### Steps

1. Choose a unique plugin name (e.g., `yang-tree-generator`).
2. Write a Java class that implements `YangCompilerPlugin`.
   See [YangTreeGenerator](../src/main/java/org/yangcentral/yangkit/plugin/yangtree/YangTreeGenerator.java) for a reference implementation.
3. Register the plugin by adding an entry to `plugins.json` located in `src/main/resources/`.

### Example `plugins.json` Entry (built-in)

```json
{
  "plugins": {
    "plugin": [
      {
        "name": "validator_plugin",
        "class": "org.yangcentral.yangkit.compiler.plugin.validator.YangValidator",
        "description": "a plugin for validating yang files",
        "parameter": [
          {
            "name": "output",
            "description": "the output directory."
          }
        ]
      }
    ]
  }
}
```

---

## Develop an External Plugin

External plugins are developed in a separate Java project and loaded by the compiler at runtime.

### Steps

1. Choose a unique plugin name (e.g., `yang-comparator`).
2. Create a Java project and write a class that implements `YangCompilerPlugin`.
3. Build the project to produce a JAR file.
4. Copy the JAR into the `plugins/` directory of the compiler application package (or any accessible path).
5. Add a plugin entry to `{application-directory}/plugins/plugins.json`, making sure `class-path` points to the JAR produced in step 3.

### Example `plugins.json` Entry (external)

```json
{
  "plugins": {
    "plugin": [
      {
        "name": "yang_comparator",
        "class-path": "yang-comparator/yang-comparator-1.0-SNAPSHOT.jar",
        "class": "com.huawei.yang.comparator.YangComparatorPlugin",
        "description": "a plugin for comparing two yang schema.",
        "parameter": [
          {
            "name": "old-yang",
            "description": "mandatory, the old version yang directory."
          },
          {
            "name": "settings",
            "description": "optional, the settings file path."
          },
          {
            "name": "compare-type",
            "description": "mandatory, specify compare-type: one of stmt, tree, or compatible-check."
          },
          {
            "name": "rule",
            "description": "optional, specify the path of the compatible-rule file."
          },
          {
            "name": "result",
            "description": "mandatory, specify the compare result file path (XML format)."
          }
        ]
      }
    ]
  }
}
```
