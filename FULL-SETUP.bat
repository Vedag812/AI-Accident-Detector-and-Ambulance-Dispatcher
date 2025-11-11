@echo off
cls
echo ================================================
echo   Accident Alert System - Complete Setup
echo ================================================
echo.

cd /d "%~dp0"

echo [Step 1/5] Checking MySQL Connection...
echo.

REM Test MySQL connection
mysql -u root -pVedant@039 -e "SELECT 1;" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Cannot connect to MySQL!
    echo.
    echo Please check:
    echo   1. MySQL Server is running
    echo   2. Password is correct: Vedant@039
    echo   3. MySQL is on localhost:3306
    echo.
    pause
    exit /b 1
)
echo [OK] MySQL connection successful!
echo.

echo [Step 2/5] Checking Database...
mysql -u root -pVedant@039 -e "USE accident_alert_system;" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Database 'accident_alert_system' not found!
    echo.
    set /p create="Create database now? (y/n): "
    if /i "%create%"=="y" (
        echo Creating database...
        mysql -u root -pVedant@039 < setup-database.sql
        if %ERRORLEVEL% EQU 0 (
            echo [OK] Database created successfully!
        ) else (
            echo [ERROR] Failed to create database
            echo Please run your SQL script manually in MySQL Workbench
            pause
            exit /b 1
        )
    ) else (
        echo Please create the database manually using your SQL script
        pause
        exit /b 1
    )
) else (
    echo [OK] Database found!
)
echo.

echo [Step 3/5] Checking JDBC Driver...
if not exist lib\mysql-connector-j-*.jar (
    echo [ERROR] MySQL JDBC driver not found!
    pause
    exit /b 1
)
echo [OK] JDBC driver found!
echo.

echo [Step 4/5] Compiling Application...
if not exist bin mkdir bin

javac -encoding UTF-8 -cp ".;lib\mysql-connector-j-9.4.0.jar" -d bin src\ConfigManager.java src\DatabaseManager.java src\RouteOptimizer.java src\WeatherService.java src\NotificationManager.java src\CommunicationPanel.java src\LoginDialog.java src\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
echo [OK] Compilation successful!
echo.

echo [Step 5/5] Starting Application...
echo.
echo ================================================
echo   Login Credentials
echo   Username: admin
echo   Password: admin123
echo ================================================
echo.
echo Starting in 3 seconds...
timeout /t 3 /nobreak >nul

java -cp ".;bin;lib\mysql-connector-j-9.4.0.jar" Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ================================================
    echo   Application Error
    echo ================================================
    echo.
    echo The application closed with an error.
    echo Please check the messages above.
    echo.
    pause
)
