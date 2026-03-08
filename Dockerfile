# ==========================================
# Multi-stage build para optimizar imagen
# ==========================================

# ==========================================
# STAGE 1: Build
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar solo archivos de dependencias primero (para cache de Docker)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar aplicación (sin ejecutar tests para build más rápido)
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear directorio para uploads
RUN mkdir -p /app/uploads/invoices

# Copiar JAR desde stage de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto 8080
EXPOSE 8080

# Variables de entorno (se sobreescriben en docker-compose)
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# Ejecutar aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
