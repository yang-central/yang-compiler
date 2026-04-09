"""
JAR file downloader for YANG Compiler.

Automatically downloads the latest or specified version of yang-compiler JAR
from GitHub Releases.
"""

import os
import hashlib
import logging
from pathlib import Path
from typing import Optional
from urllib.request import urlopen, Request
from urllib.error import URLError

try:
    from tqdm import tqdm
except ImportError:
    tqdm = None

from .exceptions import DownloadError

logger = logging.getLogger(__name__)


class JarDownloader:
    """Downloads and manages YANG Compiler JAR files."""
    
    GITHUB_REPO = "yang-central/yang-compiler"
    BASE_URL = f"https://github.com/{GITHUB_REPO}/releases/download"
    
    def __init__(self, cache_dir: Optional[str] = None):
        """
        Initialize JAR downloader.
        
        Args:
            cache_dir: Directory to cache downloaded JARs.
                      Defaults to ~/.yang-compiler
        """
        if cache_dir:
            self.cache_dir = Path(cache_dir)
        else:
            home = Path.home()
            self.cache_dir = home / ".yang-compiler"
        
        self.cache_dir.mkdir(parents=True, exist_ok=True)
        logger.debug(f"Cache directory: {self.cache_dir}")
    
    def download_latest(self, version: Optional[str] = None) -> str:
        """
        Download the latest or specified version of YANG Compiler JAR.
        
        Args:
            version: Version to download (e.g., 'v1.3.1'). 
                    If None, downloads the latest release.
        
        Returns:
            Path to the downloaded JAR file.
        
        Raises:
            DownloadError: If download fails.
        """
        if version is None:
            version = self._get_latest_version()
            logger.info(f"Latest version: {version}")
        
        jar_filename = f"yang-compiler-{version.lstrip('v')}.jar"
        jar_path = self.cache_dir / jar_filename
        
        # Check if already cached
        if jar_path.exists():
            logger.info(f"Using cached JAR: {jar_path}")
            return str(jar_path)
        
        # Download JAR
        url = f"{self.BASE_URL}/{version}/{jar_filename}"
        logger.info(f"Downloading JAR from: {url}")
        
        try:
            self._download_file(url, jar_path)
            logger.info(f"Successfully downloaded: {jar_path}")
            return str(jar_path)
        except Exception as e:
            # Clean up partial download
            if jar_path.exists():
                jar_path.unlink()
            raise DownloadError(f"Failed to download JAR: {str(e)}", url=url)
    
    def get_cached_version(self) -> Optional[str]:
        """
        Get the version of the cached JAR file.
        
        Returns:
            Version string if cached JAR exists, None otherwise.
        """
        jar_files = list(self.cache_dir.glob("yang-compiler-*.jar"))
        if not jar_files:
            return None
        
        # Return the latest version (sorted by filename)
        latest_jar = sorted(jar_files)[-1]
        # Extract version from filename: yang-compiler-1.3.1.jar -> v1.3.1
        version = latest_jar.stem.replace("yang-compiler-", "v")
        return version
    
    def clear_cache(self, keep_latest: int = 0):
        """
        Clear cached JAR files.
        
        Args:
            keep_latest: Number of latest versions to keep.
        """
        jar_files = sorted(self.cache_dir.glob("yang-compiler-*.jar"))
        
        if keep_latest > 0 and len(jar_files) > keep_latest:
            # Keep the latest N versions
            jars_to_delete = jar_files[:-keep_latest]
        else:
            jars_to_delete = jar_files
        
        for jar_file in jars_to_delete:
            jar_file.unlink()
            logger.info(f"Deleted cached JAR: {jar_file}")
    
    def _get_latest_version(self) -> str:
        """
        Get the latest release version from GitHub.
        
        Returns:
            Version tag (e.g., 'v1.3.1')
        
        Raises:
            DownloadError: If unable to fetch latest version.
        """
        api_url = f"https://api.github.com/repos/{self.GITHUB_REPO}/releases/latest"
        
        try:
            import json
            req = Request(api_url)
            req.add_header('Accept', 'application/vnd.github.v3+json')
            
            with urlopen(req, timeout=10) as response:
                data = json.loads(response.read().decode('utf-8'))
                return data['tag_name']
        except Exception as e:
            raise DownloadError(f"Failed to get latest version: {str(e)}")
    
    def _download_file(self, url: str, destination: Path):
        """
        Download a file from URL to destination with progress bar.
        
        Args:
            url: URL to download from.
            destination: Local path to save the file.
        """
        req = Request(url)
        req.add_header('User-Agent', 'yang-compiler-python/0.1.0')
        
        try:
            with urlopen(req, timeout=300) as response:
                total_size = int(response.getheader('Content-Length', 0))
                block_size = 8192
                
                if tqdm:
                    progress_bar = tqdm(total=total_size, unit='iB', unit_scale=True, 
                                       desc="Downloading JAR")
                else:
                    progress_bar = None
                
                with open(destination, 'wb') as f:
                    while True:
                        buffer = response.read(block_size)
                        if not buffer:
                            break
                        
                        f.write(buffer)
                        if progress_bar:
                            progress_bar.update(len(buffer))
                
                if progress_bar:
                    progress_bar.close()
                    
        except URLError as e:
            raise DownloadError(f"Network error: {str(e.reason)}", url=url)
        except Exception as e:
            raise DownloadError(f"Download failed: {str(e)}", url=url)
