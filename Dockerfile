# =============================================
# Dockerfile - Sistema Botica
# Usa imagen Maven oficial — no requiere mvnw
# =============================================

# Etapa 1: Compilar con Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Descargar dependencias primero (aprovecha cache de capas Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# Etapa 2: Imagen de ejecucion (solo JRE, mas liviana ~200MB)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S botica && adduser -S botica -G botica && \
    mkdir -p /tmp/botica/uploads/productos && \
    chown -R botica:botica /tmp/botica /app

COPY --from=builder /app/target/botica-system-1.0.0.jar app.jar
RUN chown botica:botica app.jar

USER botica

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-8080}/login || exit 1

ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-Dfile.encoding=UTF-8", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
