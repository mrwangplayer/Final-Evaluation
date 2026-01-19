@echo off
rem Build script for Silent Convent
if not exist classes mkdir classes






javac -d classes src\silentconvent\*.java src\scenes\*.java
if %errorlevel% neq 0 (
    echo Build failed.
    pause
    exit /b %errorlevel%
)
echo Build succeeded.
pause
