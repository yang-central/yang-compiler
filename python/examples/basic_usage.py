"""
Basic usage examples for YANG Compiler Python wrapper.
"""

from yang_compiler import YangCompiler


def example_basic_validation():
    """Example 1: Basic YANG file validation."""
    print("=" * 60)
    print("Example 1: Basic Validation")
    print("=" * 60)
    
    # Initialize compiler (auto-downloads JAR if needed)
    compiler = YangCompiler(auto_download=True)
    
    # Validate a YANG file
    result = compiler.validate(['my-model.yang'])
    
    if result.success:
        print("✓ Validation passed!")
    else:
        print(f"✗ Validation failed with {len(result.errors)} error(s):")
        for error in result.errors:
            print(f"  - {error}")
    
    print()


def example_compile_module():
    """Example 2: Compile a module by name."""
    print("=" * 60)
    print("Example 2: Compile Module by Name")
    print("=" * 60)
    
    compiler = YangCompiler(auto_download=True)
    
    # Compile ietf-interfaces module
    result = compiler.compile(
        modules=['ietf-interfaces'],
        plugins=[
            {'name': 'validator_plugin'}
        ]
    )
    
    print(f"Exit code: {result.exit_code}")
    print(f"Success: {result.success}")
    print()


def example_with_revision():
    """Example 3: Compile specific module revision."""
    print("=" * 60)
    print("Example 3: Compile Specific Revision")
    print("=" * 60)
    
    compiler = YangCompiler(auto_download=True)
    
    # Compile specific revision
    result = compiler.compile(
        modules=['ietf-interfaces@2018-02-20'],
        plugins=[
            {'name': 'validator_plugin'}
        ]
    )
    
    if result.success:
        print("✓ Successfully compiled ietf-interfaces@2018-02-20")
    print()


def example_multiple_plugins():
    """Example 4: Use multiple plugins."""
    print("=" * 60)
    print("Example 4: Multiple Plugins")
    print("=" * 60)
    
    compiler = YangCompiler(auto_download=True)
    
    # Compile with validator and tree generator
    result = compiler.compile(
        modules=['ietf-interfaces'],
        plugins=[
            {
                'name': 'validator_plugin',
                'parameter': [
                    {'name': 'output', 'value': 'validation-report.txt'}
                ]
            },
            {
                'name': 'yangtree_generator',
                'parameter': [
                    {'name': 'output', 'value': 'tree'},
                    {'name': 'expand-grouping', 'value': 'true'}
                ]
            }
        ],
        output_dir='./output'
    )
    
    print(f"Generated {len(result.output_files)} file(s)")
    for file in result.output_files:
        print(f"  - {file}")
    print()


def example_compile_directory():
    """Example 5: Compile all YANG files in a directory."""
    print("=" * 60)
    print("Example 5: Compile Directory")
    print("=" * 60)
    
    compiler = YangCompiler(auto_download=True)
    
    # Compile all YANG files in a directory
    result = compiler.compile(
        directories=['./yang'],
        plugins=[
            {'name': 'validator_plugin'}
        ]
    )
    
    if result.success:
        print("✓ All YANG files validated successfully")
    else:
        print(f"✗ Found {len(result.errors)} error(s)")
    print()


def example_error_handling():
    """Example 6: Error handling."""
    print("=" * 60)
    print("Example 6: Error Handling")
    print("=" * 60)
    
    from yang_compiler.exceptions import CompilationError, TimeoutError
    
    compiler = YangCompiler(auto_download=True)
    
    try:
        result = compiler.compile(
            modules=['nonexistent-module'],
            plugins=[{'name': 'validator_plugin'}]
        )
        
        if not result.success:
            print("Compilation failed:")
            for error in result.errors:
                print(f"  {error}")
    
    except TimeoutError as e:
        print(f"Compilation timed out: {e}")
    except CompilationError as e:
        print(f"Compilation error: {e}")
    except Exception as e:
        print(f"Unexpected error: {e}")
    
    print()


if __name__ == '__main__':
    print("\nYANG Compiler Python Wrapper - Basic Examples\n")
    print("Note: These examples require Java to be installed.\n")
    
    # Run examples
    try:
        example_basic_validation()
    except Exception as e:
        print(f"Example 1 failed: {e}\n")
    
    try:
        example_compile_module()
    except Exception as e:
        print(f"Example 2 failed: {e}\n")
    
    try:
        example_with_revision()
    except Exception as e:
        print(f"Example 3 failed: {e}\n")
    
    try:
        example_multiple_plugins()
    except Exception as e:
        print(f"Example 4 failed: {e}\n")
    
    try:
        example_compile_directory()
    except Exception as e:
        print(f"Example 5 failed: {e}\n")
    
    try:
        example_error_handling()
    except Exception as e:
        print(f"Example 6 failed: {e}\n")
    
    print("\nAll examples completed!")
