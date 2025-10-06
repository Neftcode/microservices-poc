# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear directorio para la base de datos
RUN mkdir -p /app/data

# Copiar JAR compilado desde etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno con valores por defecto
ENV ORCHESTRATOR_API_KEY=default-orchestrator-key
ENV PDF_SERVICE_URL=http://pdf-service:8081
ENV PDF_SERVICE_API_KEY=default-pdf-key
ENV EMAIL_SERVICE_URL=http://notification-service:8082
ENV EMAIL_SERVICE_API_KEY=default-email-key
ENV DB_PATH=/app/data/sales.db

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]