@echo off
setlocal

set JAR=target\yang-compiler-1.0-SNAPSHOT.jar

rem Help command (no JAR needed)
if "%~1"=="help" goto :help
if "%~1"=="--help" goto :help

rem Init command (no JAR needed)
if "%~1"=="init" goto :init

rem All other commands require the JAR
if not exist "%JAR%" (
    echo Error: %JAR% not found.
    echo Please build the project first by running: mvn clean install
    exit /b 1
)

rem Default: forward all arguments to the YANG compiler
java -jar "%JAR%" %*
exit /b %ERRORLEVEL%

:help
echo Usage: yangc.bat ^<command^> [options]
echo.
echo Commands:
echo   init          Scaffold a new YANG compiler project in the current directory.
echo                 Creates a 'yang' directory, a default build.json, and a default
echo                 settings.json.
echo   help, --help  Show this help message.
echo   ^<other^>       All other arguments are forwarded directly to the YANG compiler:
echo                   java -jar %JAR% [args...]
echo.
echo Examples:
echo   yangc.bat init
echo   yangc.bat
echo   yangc.bat option=build.json install
exit /b 0

:init
echo Initializing YANG compiler project...

rem Create yang directory
if not exist "yang" (
    mkdir yang
    echo   Created directory: yang\
) else (
    echo   Directory yang\ already exists, skipping.
)

rem Generate default build.json
if exist "build.json" (
    echo   build.json already exists, skipping.
) else (
    (
        echo {
        echo   "yang": {
        echo     "dir": [
        echo       "yang"
        echo     ]
        echo   },
        echo   "plugin": [
        echo     {
        echo       "name": "validator_plugin",
        echo       "parameter": [
        echo         {
        echo           "name": "output",
        echo           "value": "yang/validator.txt"
        echo         }
        echo       ]
        echo     }
        echo   ]
        echo }
    ) > build.json
    echo   Created: build.json
)

rem Generate default settings.json
if exist "settings.json" (
    echo   settings.json already exists, skipping.
) else (
    (
        echo {
        echo   "settings": {
        echo     "local-repository": "%USERPROFILE%\.yang",
        echo     "remote-repository": "https://yangcatalog.org/api/"
        echo   }
        echo }
    ) > settings.json
    echo   Created: settings.json
)

echo Done. Place your YANG files in the yang\ directory and run yangc.bat to compile.
exit /b 0
