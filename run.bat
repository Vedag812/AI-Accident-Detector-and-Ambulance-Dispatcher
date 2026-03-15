@echo off
echo Cleaning old builds...
if exist out rmdir /s /q out
mkdir out

echo Compiling project...
javac -cp "lib/*" -d out src/*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)

echo Starting AI Accident Detector...
java -cp "out;lib/*" Main
