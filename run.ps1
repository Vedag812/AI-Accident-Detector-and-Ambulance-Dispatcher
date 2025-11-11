# Run Script for Accident Alert System
# PowerShell script for Windows

Write-Host "Starting Accident Alert System..." -ForegroundColor Cyan
Write-Host ""

# Check if compiled
if (!(Test-Path "bin\Main.class")) {
    Write-Host "Application not compiled. Running setup..." -ForegroundColor Yellow
    .\setup.ps1
    exit
}

# Build classpath
$jarFiles = Get-ChildItem -Path "lib" -Filter "*.jar" -ErrorAction SilentlyContinue
$classpath = ".;bin"
if ($jarFiles) {
    $classpath += ";" + ($jarFiles | ForEach-Object { "lib\$($_.Name)" }) -join ";"
}

# Run application
java -cp $classpath Main
