@echo off
title Compilar Sistema Botica
color 0B
echo.
echo  ==========================================
echo   SISTEMA BOTICA - Compilando proyecto
echo  ==========================================
echo.

java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java no encontrado. Instale Java 17 desde https://adoptium.net/
    pause
    exit /b 1
)

echo [INFO] Descargando dependencias y compilando...
call mvnw.cmd clean package -DskipTests

if errorlevel 1 (
    echo.
    echo [ERROR] La compilacion fallo. Revise los errores arriba.
    pause
    exit /b 1
)

echo.
echo [OK] Compilacion exitosa!
echo [OK] JAR generado en: target\botica-system-1.0.0.jar
echo.
echo Para iniciar: ejecuta  iniciar-produccion.bat
echo.
pause
