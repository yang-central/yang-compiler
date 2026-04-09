"""
Core YANG Compiler API.

Provides the main YangCompiler class for compiling and processing YANG models.
"""

import json
import logging
import subprocess
import tempfile
import shutil
from pathlib import Path
from typing import List, Optional, Dict, Any, Union
from dataclasses import dataclass, field

from .downloader import JarDownloader
from .exceptions import (
    YangCompilerError,
    JarNotFoundError,
    CompilationError,
    TimeoutError,
)

logger = logging.getLogger(__name__)


@dataclass
class CompileResult:
    """Result of a YANG compilation."""
    
    success: bool
    """Whether compilation succeeded."""
    
    output_files: List[str] = field(default_factory=list)
    """List of generated output files."""
    
    errors: List[str] = field(default_factory=list)
    """List of error messages."""
    
    warnings: List[str] = field(default_factory=list)
    """List of warning messages."""
    
    stdout: str = ""
    """Standard output from the compiler."""
    
    stderr: str = ""
    """Standard error from the compiler."""
    
    exit_code: int = 0
    """Exit code from the compiler process."""


class YangCompiler:
    """
    Main interface for YANG Compiler.
    
    Provides methods to compile YANG models, validate them, and generate
    various outputs using plugins.
    
    Example:
        >>> compiler = YangCompiler(auto_download=True)
        >>> result = compiler.compile(modules=['ietf-interfaces'])
        >>> if result.success:
        ...     print("Compilation successful!")
        ... else:
        ...     print(f"Errors: {result.errors}")
    """
    
    def __init__(
        self,
        jar_path: Optional[str] = None,
        auto_download: bool = True,
        timeout: int = 300,
        cache_dir: Optional[str] = None
    ):
        """
        Initialize YANG Compiler.
        
        Args:
            jar_path: Path to yang-compiler JAR file. If None and auto_download
                     is True, will download automatically.
            auto_download: Whether to automatically download JAR if not found.
            timeout: Timeout in seconds for compilation processes.
            cache_dir: Directory for caching downloaded JARs.
        
        Raises:
            JarNotFoundError: If JAR not found and auto_download is False.
            DownloadError: If auto-download fails.
        """
        self.timeout = timeout
        self.jar_path = jar_path
        
        # Try to find or download JAR
        if not self.jar_path:
            if auto_download:
                downloader = JarDownloader(cache_dir=cache_dir)
                cached_version = downloader.get_cached_version()
                
                if cached_version:
                    logger.info(f"Found cached version: {cached_version}")
                    self.jar_path = downloader.download_latest(cached_version)
                else:
                    logger.info("No cached version found, downloading latest...")
                    self.jar_path = downloader.download_latest()
            else:
                raise JarNotFoundError()
        
        # Verify JAR exists
        if not Path(self.jar_path).exists():
            raise JarNotFoundError(self.jar_path)
        
        logger.info(f"Using JAR: {self.jar_path}")
    
    def compile(
        self,
        modules: Optional[List[str]] = None,
        directories: Optional[List[str]] = None,
        files: Optional[List[str]] = None,
        plugins: Optional[List[Dict[str, Any]]] = None,
        output_dir: Optional[str] = None,
        settings_path: Optional[str] = None,
        install: bool = False
    ) -> CompileResult:
        """
        Compile YANG modules with specified plugins.
        
        Args:
            modules: List of module names to compile (e.g., ['ietf-interfaces']).
                    Can include revision: 'ietf-interfaces@2018-02-20'
            directories: List of directories containing YANG files.
            files: List of individual YANG file paths.
            plugins: List of plugin configurations. Each plugin is a dict with
                    'name' and optional 'parameter' list.
            output_dir: Directory for output files.
            settings_path: Path to settings.json file.
            install: Whether to install compiled files to local repository.
        
        Returns:
            CompileResult with compilation results.
        
        Example:
            >>> result = compiler.compile(
            ...     modules=['ietf-interfaces'],
            ...     plugins=[
            ...         {'name': 'validator_plugin'},
            ...         {'name': 'yangtree_generator', 'parameter': [{'name': 'output', 'value': 'tree'}]}
            ...     ]
            ... )
        """
        # Build configuration
        build_config = self._build_config(
            modules=modules,
            directories=directories,
            files=files,
            plugins=plugins,
            settings_path=settings_path
        )
        
        # Write temporary build config
        with tempfile.NamedTemporaryFile(mode='w', suffix='.json', delete=False) as f:
            json.dump(build_config, f, indent=2)
            config_file = f.name
        
        try:
            # Build command
            cmd = ['java', '-jar', self.jar_path, f'option={config_file}']
            
            if install:
                cmd.append('install')
            
            logger.debug(f"Running command: {' '.join(cmd)}")
            
            # Execute compiler
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=self.timeout,
                cwd=output_dir
            )
            
            # Parse results
            return self._parse_result(result, output_dir)
            
        except subprocess.TimeoutExpired:
            raise TimeoutError(self.timeout)
        except FileNotFoundError:
            raise JarNotFoundError(self.jar_path)
        except Exception as e:
            raise CompilationError(f"Compilation failed: {str(e)}")
        finally:
            # Clean up temp file
            Path(config_file).unlink(missing_ok=True)
    
    def validate(self, yang_files: List[str]) -> CompileResult:
        """
        Quickly validate YANG files using the validator plugin.
        
        Args:
            yang_files: List of YANG file paths to validate.
        
        Returns:
            CompileResult with validation results.
        
        Example:
            >>> result = compiler.validate(['my-model.yang'])
            >>> if result.success:
            ...     print("Validation passed!")
        """
        return self.compile(
            files=yang_files,
            plugins=[{'name': 'validator_plugin'}]
        )
    
    def generate_tree(
        self,
        modules: List[str],
        output_dir: str = 'tree',
        line_length: int = 72,
        expand_grouping: bool = True
    ) -> CompileResult:
        """
        Generate RFC 8340 style tree diagrams for modules.
        
        Args:
            modules: List of module names.
            output_dir: Directory for tree output files.
            line_length: Maximum line length in tree diagram.
            expand_grouping: Whether to expand grouping references.
        
        Returns:
            CompileResult with tree generation results.
        
        Example:
            >>> result = compiler.generate_tree(['ietf-interfaces'], output_dir='trees')
        """
        return self.compile(
            modules=modules,
            plugins=[{
                'name': 'yangtree_generator',
                'parameter': [
                    {'name': 'output', 'value': output_dir},
                    {'name': 'line-length', 'value': str(line_length)},
                    {'name': 'expand-grouping', 'value': str(expand_grouping).lower()}
                ]
            }]
        )
    
    def _build_config(
        self,
        modules: Optional[List[str]] = None,
        directories: Optional[List[str]] = None,
        files: Optional[List[str]] = None,
        plugins: Optional[List[Dict[str, Any]]] = None,
        settings_path: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Build the JSON configuration for the compiler.
        
        Args:
            modules, directories, files, plugins, settings_path: Same as compile().
        
        Returns:
            Configuration dictionary.
        """
        config = {}
        
        # Build yang section
        yang_section = {}
        
        if modules:
            module_list = []
            for mod in modules:
                if '@' in mod:
                    name, revision = mod.split('@', 1)
                    module_list.append({'name': name, 'revision': revision})
                else:
                    module_list.append({'name': mod, 'revision': ''})
            yang_section['module'] = module_list
        
        if directories:
            yang_section['dir'] = directories
        
        if files:
            yang_section['file'] = files
        
        if yang_section:
            config['yang'] = yang_section
        
        # Add settings if provided
        if settings_path:
            config['settings'] = settings_path
        
        # Add plugins
        if plugins:
            config['plugin'] = plugins
        
        return config
    
    def _parse_result(
        self,
        process_result: subprocess.CompletedProcess,
        output_dir: Optional[str] = None
    ) -> CompileResult:
        """
        Parse the subprocess result into a CompileResult.
        
        Args:
            process_result: Result from subprocess.run()
            output_dir: Output directory to scan for generated files.
        
        Returns:
            Parsed CompileResult.
        """
        success = process_result.returncode == 0
        
        # Collect output files if output_dir is specified
        output_files = []
        if output_dir and Path(output_dir).exists():
            output_files = [
                str(f) for f in Path(output_dir).rglob('*')
                if f.is_file()
            ]
        
        # Parse errors and warnings from output
        errors = []
        warnings = []
        
        if process_result.stderr:
            for line in process_result.stderr.splitlines():
                if 'ERROR' in line.upper() or 'error:' in line.lower():
                    errors.append(line.strip())
                elif 'WARNING' in line.upper() or 'warning:' in line.lower():
                    warnings.append(line.strip())
        
        return CompileResult(
            success=success,
            output_files=output_files,
            errors=errors,
            warnings=warnings,
            stdout=process_result.stdout,
            stderr=process_result.stderr,
            exit_code=process_result.returncode
        )
