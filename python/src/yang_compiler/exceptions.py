"""
Custom exceptions for YANG Compiler Python wrapper.
"""

from typing import List, Optional


class YangCompilerError(Exception):
    """Base exception for YANG Compiler errors."""
    
    def __init__(self, message: str):
        super().__init__(message)
        self.message = message


class JarNotFoundError(YangCompilerError):
    """Raised when JAR file is not found."""
    
    def __init__(self, jar_path: Optional[str] = None):
        if jar_path:
            message = f"JAR file not found: {jar_path}"
        else:
            message = "No JAR file found. Please build the project or enable auto-download."
        super().__init__(message)
        self.jar_path = jar_path


class DownloadError(YangCompilerError):
    """Raised when downloading JAR file fails."""
    
    def __init__(self, message: str, url: Optional[str] = None):
        super().__init__(message)
        self.url = url


class CompilationError(YangCompilerError):
    """Raised when YANG compilation fails."""
    
    def __init__(self, message: str, errors: Optional[List[str]] = None, exit_code: Optional[int] = None):
        super().__init__(message)
        self.errors = errors or []
        self.exit_code = exit_code


class TimeoutError(YangCompilerError):  # noqa: A001 (shadowing built-in)
    """Raised when compilation times out."""
    
    def __init__(self, timeout_seconds: int):
        message = f"Compilation timed out after {timeout_seconds} seconds"
        super().__init__(message)
        self.timeout_seconds = timeout_seconds


class ConfigurationError(YangCompilerError):
    """Raised when configuration is invalid."""
    
    def __init__(self, message: str):
        super().__init__(message)


class PluginError(YangCompilerError):
    """Raised when plugin execution fails."""
    
    def __init__(self, plugin_name: str, message: str):
        super().__init__(f"Plugin '{plugin_name}' failed: {message}")
        self.plugin_name = plugin_name
