# YANG Compiler Python Wrapper

A Python interface for [YANG Compiler](https://github.com/yang-central/yang-compiler), providing easy-to-use APIs for compiling and processing YANG models with automatic dependency resolution.

## Features

- 🐍 **Pythonic API** - Clean, intuitive interface for YANG compilation
- 📦 **Auto-download** - Automatically downloads the latest JAR from GitHub Releases
- 🔌 **Plugin Support** - Use all built-in and custom plugins
- ⚡ **Type Hints** - Full type annotations for better IDE support
- 🛡️ **Error Handling** - Comprehensive exception hierarchy
- 💾 **Caching** - Caches downloaded JARs for offline use

## Installation

```bash
pip install yang-compiler
```

### Prerequisites

- Python 3.7 or higher
- Java Runtime Environment (JRE) 8 or higher

## Quick Start

### Basic Validation

```python
from yang_compiler import YangCompiler

# Initialize compiler (auto-downloads JAR if needed)
compiler = YangCompiler(auto_download=True)

# Validate a YANG file
result = compiler.validate(['my-model.yang'])

if result.success:
    print("✓ Validation passed!")
else:
    print(f"✗ Errors: {result.errors}")
```

### Compile Module

```python
# Compile a module by name
result = compiler.compile(
    modules=['ietf-interfaces'],
    plugins=[
        {'name': 'validator_plugin'},
        {'name': 'yangtree_generator', 'parameter': [{'name': 'output', 'value': 'tree'}]}
    ]
)

print(f"Generated files: {result.output_files}")
```

### Advanced Usage

```python
# Compile multiple sources with custom settings
result = compiler.compile(
    modules=['ietf-interfaces@2018-02-20', 'ietf-ip'],
    directories=['./yang/custom'],
    files=['./yang/specific.yang'],
    plugins=[
        {
            'name': 'validator_plugin',
            'parameter': [
                {'name': 'output', 'value': 'validation-report.txt'}
            ]
        },
        {
            'name': 'yang_statistics',
            'parameter': [
                {'name': 'output', 'value': 'stats.xlsx'}
            ]
        }
    ],
    output_dir='./output',
    settings_path='./settings.json'
)

if result.success:
    print(f"✓ Compiled successfully!")
    print(f"  Generated {len(result.output_files)} files")
else:
    print(f"✗ Compilation failed:")
    for error in result.errors:
        print(f"  - {error}")
```

## API Reference

### YangCompiler Class

#### Initialization

```python
compiler = YangCompiler(
    jar_path=None,          # Path to JAR file (auto-download if None)
    auto_download=True,     # Auto-download JAR if not found
    timeout=300,            # Timeout in seconds
    cache_dir=None          # Cache directory for downloaded JARs
)
```

#### Methods

##### `compile()`

Compile YANG modules with specified plugins.

```python
result = compiler.compile(
    modules=['ietf-interfaces'],      # Module names (with optional @revision)
    directories=['./yang'],           # Directories to scan
    files=['model.yang'],             # Individual files
    plugins=[...],                    # Plugin configurations
    output_dir='./output',            # Output directory
    settings_path='settings.json',    # Settings file path
    install=False                     # Install to local repo
)
```

##### `validate()`

Quick validation using validator_plugin.

```python
result = compiler.validate(['model.yang'])
```

##### `generate_tree()`

Generate RFC 8340 style tree diagrams.

```python
result = compiler.generate_tree(
    modules=['ietf-interfaces'],
    output_dir='tree',
    line_length=72,
    expand_grouping=True
)
```

### CompileResult

Dataclass containing compilation results:

```python
@dataclass
class CompileResult:
    success: bool              # Whether compilation succeeded
    output_files: List[str]    # Generated output files
    errors: List[str]          # Error messages
    warnings: List[str]        # Warning messages
    stdout: str                # Standard output
    stderr: str                # Standard error
    exit_code: int             # Process exit code
```

### Exceptions

- `YangCompilerError` - Base exception
- `JarNotFoundError` - JAR file not found
- `DownloadError` - Download failed
- `CompilationError` - Compilation failed
- `TimeoutError` - Compilation timed out
- `ConfigurationError` - Invalid configuration
- `PluginError` - Plugin execution failed

## Examples

See the [examples](examples/) directory for more usage examples:

- [basic_usage.py](examples/basic_usage.py) - Basic operations
- [advanced_usage.py](examples/advanced_usage.py) - Advanced features

## Development

### Setup Development Environment

```bash
# Clone repository
git clone https://github.com/yang-central/yang-compiler.git
cd yang-compiler/python

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install in development mode
pip install -e ".[dev]"
```

### Run Tests

```bash
pytest
```

### Code Formatting

```bash
black src/yang_compiler tests
flake8 src/yang_compiler tests
```

### Build Package

```bash
python -m build
```

## License

Apache License 2.0 - See [LICENSE](../LICENSE) for details.

## Contributing

We welcome contributions! Please see our [Contributing Guide](../CONTRIBUTING.md) for details.

## Support

- **Issues**: [GitHub Issues](https://github.com/yang-central/yang-compiler/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yang-central/yang-compiler/discussions)
- **Email**: frank.fengchong@huawei.com

## Acknowledgments

This package is a Python wrapper around [YANG Compiler](https://github.com/yang-central/yang-compiler), built on [YangKit](https://github.com/yang-central/yangkit).
