@echo off
title Sistema Botica - Produccion
color 0A
echo.
echo  ==========================================
echo   SISTEMA BOTICA - Iniciando en produccion
echo  ==========================================
echo.

:: Verificar Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java no encontrado. Instale Java 17 desde https://adoptium.net/
    pause
    exit /b 1
)

:: Compilar si no existe el JAR
if not exist "target\botica-system-1.0.0.jar" (
    echo [INFO] Compilando el proyecto...
    call mvnw.cmd clean package -DskipTests
    if errorlevel 1 (
        echo [ERROR] Fallo la compilacion. Revise los errores.
        pause
        exit /b 1
    )
    echo [OK] Compilacion exitosa.
)

:: Crear directorios necesarios
if not exist "uploads\productos" mkdir "uploads\productos"
if not exist "logs" mkdir "logs"

echo [INFO] Iniciando la aplicacion...
echo [INFO] Accede desde el navegador: http://localhost:8080
echo [INFO] Presiona Ctrl+C para detener.
echo.

java -Xms256m -Xmx512m ^
     -Dfile.encoding=UTF-8 ^
     -Dspring.profiles.active=prod ^
     -Dapp.upload.dir=uploads/productos ^
     -jar target\botica-system-1.0.0.jar

pause
