@echo off
echo Starting Accident Alert System...
echo.

REM Check if compiled
if not exist bin\Main.class (
    echo Application not compiled. Compiling now...
    echo.
    call compile.bat
    if %ERRORLEVEL% NEQ 0 (
        echo Compilation failed. Please fix errors and try again.
        pause
        exit /b 1
    )
)

REM Set classpath
set CLASSPATH=.;bin
if exist lib\*.jar (
    for %%i in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%i
)

REM Run application
java -cp "%CLASSPATH%" Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error.
    pause
)
