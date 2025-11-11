@echo off
echo ========================================
echo   Database Setup and Application Start
echo ========================================
echo.

cd /d "%~dp0"

echo [1/4] Creating all required database tables...
echo.
mysql -u root -pVedant@039 accident_alert_system < create-all-tables.sql
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to create tables!
    echo Please check MySQL is running
    pause
    exit /b 1
)
echo [OK] All tables created!
echo.

echo [2/4] Verifying database...
mysql -u root -pVedant@039 accident_alert_system -e "SHOW TABLES;"
echo.

echo [3/4] Compiling application...
if not exist bin mkdir bin
javac -encoding UTF-8 -cp ".;lib\mysql-connector-j-9.4.0.jar" -d bin src\*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
echo [OK] Compilation successful!
echo.

echo [4/4] Starting application...
echo.
echo ========================================
echo   Login: admin / admin123
echo ========================================
echo.
timeout /t 2 /nobreak >nul

java -cp ".;bin;lib\mysql-connector-j-9.4.0.jar" Main

pause
