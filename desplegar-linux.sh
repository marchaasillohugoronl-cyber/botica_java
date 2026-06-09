#!/bin/bash
# Script de despliegue para servidor Linux (VPS/Ubuntu/Debian)
set -e

APP_DIR="/opt/botica"
APP_USER="botica"
SERVICE_NAME="botica"
JAR_PATH="$APP_DIR/botica-system-1.0.0.jar"

echo "=== Despliegue Sistema Botica ==="

# 1. Crear usuario del sistema
if ! id "$APP_USER" &>/dev/null; then
    useradd -r -s /bin/false $APP_USER
    echo "[OK] Usuario $APP_USER creado."
fi

# 2. Crear directorios
mkdir -p $APP_DIR/uploads/productos
mkdir -p $APP_DIR/logs
mkdir -p $APP_DIR/data

# 3. Copiar JAR (debe haberse compilado antes)
if [ ! -f "target/botica-system-1.0.0.jar" ]; then
    echo "[ERROR] No se encontro el JAR. Ejecute primero: ./mvnw clean package -DskipTests"
    exit 1
fi
cp target/botica-system-1.0.0.jar $JAR_PATH
cp src/main/resources/application-prod.properties $APP_DIR/

chown -R $APP_USER:$APP_USER $APP_DIR
chmod 750 $APP_DIR

# 4. Crear servicio systemd
cat > /etc/systemd/system/$SERVICE_NAME.service << EOF
[Unit]
Description=Sistema de Gestion de Botica
After=network.target mysql.service

[Service]
Type=simple
User=$APP_USER
WorkingDirectory=$APP_DIR
ExecStart=/usr/bin/java \\
    -Xms256m -Xmx512m \\
    -Dfile.encoding=UTF-8 \\
    -Dspring.profiles.active=prod \\
    -Dapp.upload.dir=$APP_DIR/uploads/productos \\
    -Dlogging.file.name=$APP_DIR/logs/botica.log \\
    -jar $JAR_PATH
SuccessExitStatus=143
StandardOutput=journal
StandardError=journal
SyslogIdentifier=botica
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 5. Habilitar e iniciar servicio
systemctl daemon-reload
systemctl enable $SERVICE_NAME
systemctl restart $SERVICE_NAME

echo ""
echo "[OK] Despliegue completado."
echo "[OK] Estado: systemctl status $SERVICE_NAME"
echo "[OK] Logs:   journalctl -u $SERVICE_NAME -f"
echo "[OK] URL:    http://$(hostname -I | awk '{print $1}'):8080"
