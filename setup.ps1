# Enhanced Accident Alert System - Setup and Run Script
# PowerShell script for Windows

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Accident Alert System - Setup" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# Check Java installation
Write-Host "Checking Java installation..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Java is installed" -ForegroundColor Green
    java -version
} else {
    Write-Host "✗ Java is not installed!" -ForegroundColor Red
    Write-Host "Please install Java JDK 17 or higher from:" -ForegroundColor Yellow
    Write-Host "https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Check MySQL Connector
Write-Host "Checking MySQL JDBC Driver..." -ForegroundColor Yellow
if (Test-Path "lib\mysql-connector-j-*.jar") {
    Write-Host "✓ MySQL Connector found" -ForegroundColor Green
} else {
    Write-Host "✗ MySQL Connector not found!" -ForegroundColor Red
    Write-Host "Please download mysql-connector-j-8.2.0.jar and place it in the 'lib' folder" -ForegroundColor Yellow
    Write-Host "Download from: https://dev.mysql.com/downloads/connector/j/" -ForegroundColor Yellow
    Write-Host ""
    $response = Read-Host "Continue anyway? (y/n)"
    if ($response -ne 'y') {
        exit 1
    }
}

Write-Host ""

# Create bin directory if it doesn't exist
if (!(Test-Path "bin")) {
    Write-Host "Creating bin directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path "bin" | Out-Null
    Write-Host "✓ Created bin directory" -ForegroundColor Green
}

Write-Host ""

# Compile Java files
Write-Host "Compiling Java files..." -ForegroundColor Yellow
$jarFiles = Get-ChildItem -Path "lib" -Filter "*.jar" -ErrorAction SilentlyContinue
$classpath = "."
if ($jarFiles) {
    $classpath = ".;" + ($jarFiles | ForEach-Object { "lib\$($_.Name)" }) -join ";"
}

$javaFiles = Get-ChildItem -Path "src" -Filter "*.java"
$sourceFiles = ($javaFiles | ForEach-Object { "src\$($_.Name)" }) -join " "

$compileCmd = "javac -encoding UTF-8 -cp `"$classpath`" -d bin $sourceFiles"
Write-Host "Command: $compileCmd" -ForegroundColor Gray

Invoke-Expression $compileCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "✗ Compilation failed!" -ForegroundColor Red
    Write-Host "Please check the error messages above" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Check config.properties
Write-Host "Checking configuration..." -ForegroundColor Yellow
if (Test-Path "config.properties") {
    Write-Host "✓ Configuration file found" -ForegroundColor Green
} else {
    Write-Host "⚠ Configuration file not found, using defaults" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To run the application:" -ForegroundColor Yellow
Write-Host "  .\run.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "Or manually:" -ForegroundColor Yellow
Write-Host "  java -cp `".;bin;$classpath`" Main" -ForegroundColor Cyan
Write-Host ""
Write-Host "Default login credentials:" -ForegroundColor Yellow
Write-Host "  Username: admin" -ForegroundColor Cyan
Write-Host "  Password: admin123" -ForegroundColor Cyan
Write-Host ""

$runNow = Read-Host "Run the application now? (y/n)"
if ($runNow -eq 'y') {
    Write-Host ""
    Write-Host "Starting application..." -ForegroundColor Green
    Write-Host ""
    $runClasspath = ".;bin"
    if ($jarFiles) {
        $runClasspath += ";" + ($jarFiles | ForEach-Object { "lib\$($_.Name)" }) -join ";"
    }
    java -cp $runClasspath Main
}
