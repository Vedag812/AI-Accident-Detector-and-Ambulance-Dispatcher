@echo off
echo ==========================================
echo  AI Accident Detector - Web Dashboard
echo ==========================================
echo.

cd /d "%~dp0"

echo Installing dependencies...
pip install -r requirements.txt -q

echo.
echo Starting web server...
echo Dashboard: http://localhost:5000
echo Login: admin / admin123
echo.
python app.py
pause
