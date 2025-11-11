@echo off
echo ====================================
echo Accident Alert System - Compile
echo ====================================
echo.

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Check for JDBC driver
if not exist lib\*.jar (
    echo WARNING: MySQL JDBC driver not found in lib folder!
    echo Please download mysql-connector-j-8.2.0.jar
    echo From: https://dev.mysql.com/downloads/connector/j/
    echo.
    pause
)

echo Compiling Java files...
echo.

REM Set classpath
set CLASSPATH=.
if exist lib\*.jar (
    for %%i in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%i
)

REM Compile all Java files
javac -encoding UTF-8 -cp "%CLASSPATH%" -d bin src\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================
    echo Compilation successful!
    echo ====================================
    echo.
    echo To run the application:
    echo   run.bat
    echo.
) else (
    echo.
    echo ====================================
    echo Compilation failed!
    echo ====================================
    echo Please check the errors above.
    echo.
)

pause
