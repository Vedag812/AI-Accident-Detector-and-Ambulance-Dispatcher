@echo off
echo ========================================
echo Starting Accident Alert System
echo ========================================
echo.

cd /d "%~dp0"

REM Check if compiled
if not exist bin\Main.class (
    echo [ERROR] Application not compiled!
    echo Please run: compile-and-run.bat
    pause
    exit /b 1
)

REM Check for JDBC driver
if not exist lib\mysql-connector-j-*.jar (
    echo [ERROR] MySQL JDBC driver not found!
    echo Please download mysql-connector-j JAR file
    echo From: https://dev.mysql.com/downloads/connector/j/
    echo And place it in the 'lib' folder
    pause
    exit /b 1
)

echo Starting application...
echo Login: admin / admin123
echo.

REM Run with classpath including JDBC driver
java -cp ".;bin;lib\mysql-connector-j-9.4.0.jar" Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application exited with an error
    echo.
    echo Common issues:
    echo 1. MySQL server not running
    echo 2. Wrong password in config.properties
    echo 3. Database 'accident_alert_system' not created
    echo.
    echo Please check:
    echo - MySQL is running
    echo - config.properties has correct password
    echo - Database exists (run SQL script)
    echo.
    pause
)
