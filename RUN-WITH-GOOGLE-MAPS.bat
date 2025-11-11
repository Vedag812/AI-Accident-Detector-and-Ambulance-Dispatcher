@echo off
echo ========================================
echo  ACCIDENT ALERT SYSTEM - QUICK START
echo ========================================
echo.
echo Features:
echo  - Auto-creates all database tables
echo  - Google Maps integration
echo  - Real-time updates
echo  - Modern tabbed UI with scroll
echo.
echo Step 1: Compiling Java files...
cd /d "%~dp0"
javac -encoding UTF-8 -cp ".;lib\mysql-connector-j-9.4.0.jar" -d bin src\*.java
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
echo [OK] Compilation successful!
echo.

echo Step 2: Launching application...
echo.
echo Login credentials:
echo   Username: admin
echo   Password: admin123
echo.
echo Tips:
echo  - All database tables are created automatically
echo  - Click "Google Maps" button to view map
echo  - Enable "Auto-Refresh" on the map for real-time updates
echo  - Use tabs to switch between Map View, Table View, and Communications
echo  - Click the up arrow button to scroll to top
echo.
pause
java -cp ".;bin;lib\mysql-connector-j-9.4.0.jar" Main
pause
