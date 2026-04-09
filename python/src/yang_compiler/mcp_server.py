"""
MCP server for YANG Compiler.

Exposes YANG validation and compilation tools to GitHub Copilot Chat and
other MCP-compatible clients via the stdio transport.

Usage:
    python -m yang_compiler.mcp_server

Or, after installing the package with the 'mcp' extra:
    yang-compiler-mcp
"""

from __future__ import annotations

import logging
import sys
from pathlib import Path
from typing import List, Optional

logger = logging.getLogger(__name__)

try:
    from mcp.server.fastmcp import FastMCP
except ImportError:
    print(
        "Error: 'mcp' package is not installed.\n"
        "Install it with: pip install 'mcp>=1.23.0'\n"
        "Or install this package with the mcp extra: pip install 'yang-compiler[mcp]'",
        file=sys.stderr,
    )
    sys.exit(1)

from .compiler import YangCompiler
from .exceptions import JarNotFoundError, YangCompilerError

mcp = FastMCP("yang-compiler")


def _find_local_jar() -> Optional[str]:
    """Look for a pre-built JAR in the project's target/ directory."""
    project_root = Path(__file__).resolve().parent.parent.parent.parent
    target_jars = sorted(project_root.glob("target/yang-compiler-*.jar"))
    if target_jars:
        return str(target_jars[-1])
    return None


def _get_compiler(auto_download: bool = True) -> YangCompiler:
    """Return a YangCompiler instance, preferring a locally-built JAR."""
    jar_path = _find_local_jar()
    return YangCompiler(
        jar_path=jar_path,
        auto_download=(auto_download and jar_path is None),
    )


@mcp.tool()
def validate_yang(files: List[str]) -> str:
    """Validate YANG files for correctness using the YANG compiler.

    Args:
        files: Paths to the .yang files to validate.

    Returns:
        A human-readable validation report.
    """
    try:
        compiler = _get_compiler()
        result = compiler.validate(files)
        if result.success:
            return "Validation passed successfully."
        lines = ["Validation failed:"]
        for error in result.errors:
            lines.append(f"  ERROR: {error}")
        for warning in result.warnings:
            lines.append(f"  WARNING: {warning}")
        if result.stdout:
            lines.append(f"\nCompiler output:\n{result.stdout}")
        return "\n".join(lines)
    except JarNotFoundError:
        return (
            "YANG Compiler JAR not found. "
            "Build the project first with: mvn clean package\n"
            "Or enable auto-download by not specifying a jar_path."
        )
    except YangCompilerError as exc:
        return f"Compiler error: {exc.message}"


@mcp.tool()
def generate_tree(
    modules: List[str],
    output_dir: str = "tree",
    expand_grouping: bool = True,
) -> str:
    """Generate RFC 8340 YANG tree diagrams for one or more YANG modules.

    Args:
        modules: Module names to process, e.g. ['ietf-interfaces'] or
                 ['ietf-interfaces@2018-02-20'].
        output_dir: Directory where tree files will be written (default: 'tree').
        expand_grouping: Whether to inline grouping references (default: True).

    Returns:
        Status message listing the generated files.
    """
    try:
        compiler = _get_compiler()
        result = compiler.generate_tree(
            modules=modules,
            output_dir=output_dir,
            expand_grouping=expand_grouping,
        )
        if result.success:
            if result.output_files:
                file_list = "\n".join(f"  - {f}" for f in result.output_files)
                return f"Tree generated successfully:\n{file_list}"
            return "Tree generated successfully."
        lines = ["Tree generation failed:"]
        for error in result.errors:
            lines.append(f"  ERROR: {error}")
        return "\n".join(lines)
    except JarNotFoundError:
        return (
            "YANG Compiler JAR not found. "
            "Build the project first with: mvn clean package"
        )
    except YangCompilerError as exc:
        return f"Compiler error: {exc.message}"


@mcp.tool()
def compile_yang(
    modules: Optional[List[str]] = None,
    directories: Optional[List[str]] = None,
    files: Optional[List[str]] = None,
    plugin: str = "validator_plugin",
    output: str = "validator.txt",
) -> str:
    """Compile YANG sources using a specified plugin.

    Args:
        modules: Module names to compile (e.g. ['ietf-interfaces']).
        directories: Directories that contain .yang source files.
        files: Individual .yang file paths.
        plugin: Plugin name to invoke (default: 'validator_plugin').
        output: Output file or directory path for the plugin result.

    Returns:
        Compilation result summary.
    """
    try:
        compiler = _get_compiler()
        result = compiler.compile(
            modules=modules,
            directories=directories,
            files=files,
            plugins=[
                {
                    "name": plugin,
                    "parameter": [{"name": "output", "value": output}],
                }
            ],
        )
        if result.success:
            if result.output_files:
                file_list = "\n".join(f"  - {f}" for f in result.output_files)
                return f"Compilation succeeded:\n{file_list}"
            return "Compilation succeeded."
        lines = ["Compilation failed:"]
        for error in result.errors:
            lines.append(f"  ERROR: {error}")
        return "\n".join(lines)
    except JarNotFoundError:
        return (
            "YANG Compiler JAR not found. "
            "Build the project first with: mvn clean package"
        )
    except YangCompilerError as exc:
        return f"Compiler error: {exc.message}"


def main() -> None:
    """Entry point for the YANG Compiler MCP server."""
    mcp.run(transport="stdio")


if __name__ == "__main__":
    main()
