@echo off
echo ========================================
echo QUICK SETUP - Accident Alert System
echo ========================================
echo.

echo Step 1: Checking Java...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found!
    echo Please install Java JDK 17 or higher
    pause
    exit /b 1
)
echo [OK] Java is installed
echo.

echo Step 2: Checking MySQL JDBC Driver...
if not exist lib\mysql-connector-j-*.jar (
    echo [WARNING] MySQL JDBC driver not found in lib folder
    echo.
    echo Please download mysql-connector-j-8.2.0.jar from:
    echo https://dev.mysql.com/downloads/connector/j/
    echo.
    echo Then place it in the 'lib' folder
    echo.
    set /p continue="Continue anyway? (y/n): "
    if /i not "%continue%"=="y" exit /b 1
)
echo [OK] JDBC driver found or skipped
echo.

echo Step 3: Creating bin directory...
if not exist bin mkdir bin
echo [OK] Bin directory ready
echo.

echo Step 4: Compiling Java files...
echo This may take a moment...
echo.

REM Build classpath
set CLASSPATH=.
for %%i in (lib\*.jar) do call :append_classpath %%i
goto :continue_compile

:append_classpath
set CLASSPATH=%CLASSPATH%;%1
goto :eof

:continue_compile
javac -encoding UTF-8 -cp "%CLASSPATH%" -d bin src\ConfigManager.java src\DatabaseManager.java src\RouteOptimizer.java src\WeatherService.java src\NotificationManager.java src\CommunicationPanel.java src\LoginDialog.java src\Main.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo [SUCCESS] Compilation Complete!
    echo ========================================
    echo.
    echo Next steps:
    echo 1. Update config.properties with your MySQL password
    echo 2. Run your SQL script in MySQL Workbench
    echo 3. Run: run.bat
    echo.
    echo Default login: admin / admin123
    echo.
    set /p run="Run now? (y/n): "
    if /i "%run%"=="y" (
        echo.
        echo Starting application...
        echo.
        set RUNCP=.;bin
        for %%i in (lib\*.jar) do call :append_runcp %%i
        goto :run_app
    )
) else (
    echo.
    echo ========================================
    echo [ERROR] Compilation Failed!
    echo ========================================
    echo.
    echo Common issues:
    echo 1. Missing JDBC driver in lib folder
    echo 2. Java version too old (need JDK 17+)
    echo 3. File encoding issues
    echo.
    echo Check the error messages above
    echo.
)
pause
exit /b

:append_runcp
set RUNCP=%RUNCP%;%1
goto :eof

:run_app
java -cp "%RUNCP%" Main
pause
