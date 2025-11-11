@echo off
echo ========================================
echo Compile and Run - Accident Alert System
echo ========================================
echo.

cd /d "%~dp0"

REM Create bin directory
if not exist bin mkdir bin

echo [1/3] Compiling Java files...
javac -encoding UTF-8 -cp ".;lib\mysql-connector-j-9.4.0.jar" -d bin src\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    echo Please check the errors above
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
echo [2/3] Checking database configuration...

REM Check config file
if exist config.properties (
    echo [OK] Configuration file found
) else (
    echo [WARNING] config.properties not found, using defaults
)

echo.
echo [3/3] Starting application...
echo.
echo ========================================
echo Login credentials: admin / admin123
echo ========================================
echo.

java -cp ".;bin;lib\mysql-connector-j-9.4.0.jar" Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo Application Error
    echo ========================================
    echo.
    echo Troubleshooting:
    echo 1. Make sure MySQL server is running
    echo 2. Check config.properties password: Vedant@039
    echo 3. Create database with your SQL script
    echo 4. Verify JDBC driver in lib folder
    echo.
)

pause
