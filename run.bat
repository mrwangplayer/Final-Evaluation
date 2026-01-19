@echo off
rem Run script for Silent Convent
if not exist classes (
    echo No classes directory found. Run build.bat first.
    pause
    exit /b 1
)
java -cp classes silentconvent.Main
pause
