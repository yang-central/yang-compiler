@echo off
setlocal enabledelayedexpansion

set JAR=target\yang-compiler-1.0-SNAPSHOT.jar

rem Help command (no JAR needed)
if "%~1"=="help" goto :help
if "%~1"=="--help" goto :help

rem Init command (no JAR needed)
if "%~1"=="init" goto :init

rem Compile command (zero-config)
if "%~1"=="compile" goto :compile_start

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
echo   init                    Scaffold a new YANG compiler project in the current directory.
echo                           Creates a 'yang' directory, a default build.json, and a default
echo                           settings.json.
echo   compile ^<inputs...^>     Zero-config compilation. Each input can be:
echo                             - A directory path (e.g. .\yang)
echo                             - A .yang file path (e.g. my-model.yang)
echo                             - A module name (e.g. ietf-interfaces)
echo                             - A module name with revision (e.g. ietf-interfaces@2018-02-20)
echo                           Multiple inputs are allowed.
echo     --plugin ^<name^>       Plugin to use (default: validator_plugin).
echo                           Only one --plugin is allowed; for multiple plugins use build.json.
echo     --param key=value     Plugin parameter. May be repeated.
echo   help, --help            Show this help message.
echo   ^<other^>                 All other arguments are forwarded directly to the YANG compiler:
echo                             java -jar %JAR% [args...]
echo.
echo Examples:
echo   yangc.bat init
echo   yangc.bat compile .\yang
echo   yangc.bat compile ietf-interfaces
echo   yangc.bat compile ietf-interfaces@2018-02-20 --plugin yangtree_generator --param output=tree
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

:compile_start
rem Shift past 'compile'; remaining positional args are %1 %2 ...
shift /1

rem Initialise state
set PLUGIN_NAME=validator_plugin
set PLUGIN_COUNT=0
set PARAM_COUNT=0
set INPUT_COUNT=0

:compile_loop
if "%~1"=="" goto :compile_done

if "%~1"=="--plugin" (
    set /a PLUGIN_COUNT+=1
    if !PLUGIN_COUNT! GTR 1 (
        echo Error: Only one --plugin is allowed in quick CLI mode.
        echo For multiple plugins, use a build.json file and run: yangc.bat option=build.json
        exit /b 1
    )
    if "%~2"=="" (
        echo Error: --plugin requires a value.
        exit /b 1
    )
    set PLUGIN_NAME=%~2
    shift /1
    shift /1
    goto :compile_loop
)

if "%~1"=="--param" (
    if "%~2"=="" (
        echo Error: --param requires a key=value argument.
        exit /b 1
    )
    set /a PARAM_COUNT+=1
    set PARAM_!PARAM_COUNT!=%~2
    shift /1
    shift /1
    goto :compile_loop
)

rem Positional input
set /a INPUT_COUNT+=1
set INPUT_!INPUT_COUNT!=%~1
shift /1
goto :compile_loop

:compile_done
if %INPUT_COUNT% EQU 0 (
    echo Error: 'compile' requires at least one input ^(file, directory, or module name^).
    echo Usage: yangc.bat compile ^<inputs...^> [--plugin ^<name^>] [--param key=value ...]
    exit /b 1
)

if not exist "%JAR%" (
    echo Error: %JAR% not found.
    echo Please build the project first by running: mvn clean install
    exit /b 1
)

set TEMP_BUILD=%TEMP%\temp-build-%RANDOM%%RANDOM%.json

rem Categorise each input as directory, file, or module name
set DIR_JSON=
set FILE_JSON=
set MODULE_JSON=
set DIR_COUNT=0
set FILE_COUNT=0
set MODULE_COUNT=0

for /L %%i in (1,1,%INPUT_COUNT%) do (
    set CUR_INPUT=!INPUT_%%i!

    if exist "!CUR_INPUT!\" (
        rem It is a directory
        set /a DIR_COUNT+=1
        set ESC=!CUR_INPUT:\=\\!
        if !DIR_COUNT! EQU 1 (
            set DIR_JSON="!ESC!"
        ) else (
            set DIR_JSON=!DIR_JSON!,"!ESC!"
        )
    ) else if exist "!CUR_INPUT!" (
        rem It is a file
        set /a FILE_COUNT+=1
        set ESC=!CUR_INPUT:\=\\!
        if !FILE_COUNT! EQU 1 (
            set FILE_JSON="!ESC!"
        ) else (
            set FILE_JSON=!FILE_JSON!,"!ESC!"
        )
    ) else (
        rem Treat as module name, optionally with @revision
        set MOD_REV=
        for /f "tokens=1 delims=@" %%a in ("!CUR_INPUT!") do set MOD_NAME=%%a
        for /f "tokens=2 delims=@" %%a in ("!CUR_INPUT!") do set MOD_REV=%%a
        set /a MODULE_COUNT+=1
        if !MODULE_COUNT! EQU 1 (
            set MODULE_JSON={"name":"!MOD_NAME!","revision":"!MOD_REV!"}
        ) else (
            set MODULE_JSON=!MODULE_JSON!,{"name":"!MOD_NAME!","revision":"!MOD_REV!"}
        )
    )
)

rem Build the "yang" content string
set YANG_CONTENT=
set YANG_FIRST=1

if %DIR_COUNT% GTR 0 (
    if !YANG_FIRST! EQU 1 (
        set YANG_CONTENT="dir":[!DIR_JSON!]
        set YANG_FIRST=0
    ) else (
        set YANG_CONTENT=!YANG_CONTENT!,"dir":[!DIR_JSON!]
    )
)

if %FILE_COUNT% GTR 0 (
    if !YANG_FIRST! EQU 1 (
        set YANG_CONTENT="file":[!FILE_JSON!]
        set YANG_FIRST=0
    ) else (
        set YANG_CONTENT=!YANG_CONTENT!,"file":[!FILE_JSON!]
    )
)

if %MODULE_COUNT% GTR 0 (
    if !YANG_FIRST! EQU 1 (
        set YANG_CONTENT="module":[!MODULE_JSON!]
        set YANG_FIRST=0
    ) else (
        set YANG_CONTENT=!YANG_CONTENT!,"module":[!MODULE_JSON!]
    )
)

rem Build plugin parameter JSON
set PARAM_JSON=
if %PARAM_COUNT% GTR 0 (
    for /L %%i in (1,1,%PARAM_COUNT%) do (
        set PARAM_ITEM=!PARAM_%%i!
        for /f "tokens=1 delims==" %%a in ("!PARAM_ITEM!") do set PKEY=%%a
        for /f "tokens=2* delims==" %%a in ("!PARAM_ITEM!") do set PVAL=%%a
        if %%i EQU 1 (
            set PARAM_JSON={"name":"!PKEY!","value":"!PVAL!"}
        ) else (
            set PARAM_JSON=!PARAM_JSON!,{"name":"!PKEY!","value":"!PVAL!"}
        )
    )
)

rem Build plugin JSON entry
set DEFAULT_PARAM={"name":"output","value":"validator.txt"}
if "!PARAM_JSON!"=="" (
    if "!PLUGIN_NAME!"=="validator_plugin" (
        set PLUGIN_JSON={"name":"!PLUGIN_NAME!","parameter":[!DEFAULT_PARAM!]}
    ) else (
        set PLUGIN_JSON={"name":"!PLUGIN_NAME!"}
    )
) else (
    set PLUGIN_JSON={"name":"!PLUGIN_NAME!","parameter":[!PARAM_JSON!]}
)

rem Write the temporary build configuration
(
    echo {
    echo   "yang": {!YANG_CONTENT!},
    echo   "plugin": [!PLUGIN_JSON!]
    echo }
) > "!TEMP_BUILD!"

java -jar "%JAR%" "option=!TEMP_BUILD!"
set EXIT_CODE=%ERRORLEVEL%
del /f /q "!TEMP_BUILD!" 2>nul
exit /b %EXIT_CODE%
