# Microservicio A - Orquestador (Java Spring Boot)

## Navegación

- [⬅️ Volver al README principal](../README.md)
- [📐 Ver Arquitectura C4](../ARCHITECTURE.md)
- [📄 PDF Service](../pdf-service/README.md)
- [📧 Notification Service](../notification-service/README.md)
- [🖥️ Frontend](../frontend/README.md)

---

## Descripción

Microservicio principal que actúa como orquestador. Responsable de:
- Recibir peticiones del frontend
- Validar datos de entrada
- Persistir información en base de datos SQLite
- Coordinar llamadas a otros microservicios
- Gestionar comunicación síncrona y asíncrona

## Tecnologías

- Java 21
- Spring Boot 3.2.0
- Maven 3.8+
- SQLite 3
- Hibernate ORM
- SpringDoc OpenAPI (Swagger)

## Dependencias principales

```xml
- spring-boot-starter-web (REST API)
- spring-boot-starter-data-jpa (ORM)
- spring-boot-starter-validation (Validaciones)
- sqlite-jdbc (Driver SQLite)
- hibernate-community-dialects (Soporte SQLite)
- springdoc-openapi-starter-webmvc-ui (Swagger UI)
```

## Configuración

### Variables de entorno requeridas

```bash
ORCHESTRATOR_API_KEY=tu-api-key-segura
PDF_SERVICE_URL=http://pdf-service:8081
PDF_SERVICE_API_KEY=pdf-service-key
EMAIL_SERVICE_URL=http://notification-service:8082
EMAIL_SERVICE_API_KEY=email-service-key
DB_PATH=/app/data/sales.db
```

### application.properties

Las propiedades se configuran mediante variables de entorno en el contenedor Docker.

## Ejecución

### Con Docker

```bash
docker build -t orchestrator-service .
docker run -p 8080:8080 \
  -e ORCHESTRATOR_API_KEY=your-key \
  -e PDF_SERVICE_URL=http://pdf-service:8081 \
  -e PDF_SERVICE_API_KEY=pdf-key \
  -e EMAIL_SERVICE_URL=http://notification-service:8082 \
  -e EMAIL_SERVICE_API_KEY=email-key \
  orchestrator-service
```

### Local (sin Docker)

```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

## Endpoints

### POST /api/sales
Crear nueva venta y generar factura.

**Request Body:**
```json
{
  "customer": {
    "name": "Juan Pérez",
    "identification": "1234567890",
    "email": "juan.perez@example.com"
  },
  "products": [
    {
      "name": "Producto A",
      "price": 50000.00,
      "quantity": 2,
      "total": 100000.00
    }
  ]
}
```

**Response:** PDF Binary (application/pdf)

**Status Codes:**
- 200: PDF generado exitosamente
- 400: Datos inválidos
- 500: Error interno
- 502: Error comunicándose con otros servicios

### GET /health
Health check del servicio.

**Response:**
```json
{
  "status": "UP",
  "service": "orchestrator-service",
  "timestamp": "2025-10-05T10:30:00"
}
```

## Base de Datos

### Tabla: sales

```sql
CREATE TABLE sales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name TEXT NOT NULL,
    customer_id TEXT NOT NULL,
    customer_email TEXT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    products TEXT NOT NULL,  -- JSON serializado
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Seguridad

- **API Key Validation**: Todas las peticiones deben incluir header `X-API-Key`
- **Input Validation**: Validación de datos con Bean Validation
- **Email Validation**: Patrón regex para emails válidos

## Patrones comentados en el código

Estos patrones NO están implementados, pero se comentan en el código donde serían aplicables:

1. **Circuit Breaker**: En `PdfServiceClient` y `EmailServiceClient`
2. **Retry Pattern**: En llamadas HTTP a otros servicios
3. **Timeout Management**: En RestTemplate
4. **Service Discovery**: Para encontrar servicios dinámicamente
5. **API Gateway Pattern**: Este servicio actúa como uno simplificado

## Documentación API

Swagger UI disponible en: http://localhost:8080/swagger-ui.html

OpenAPI JSON: http://localhost:8080/v3/api-docs

## Probar el servicio

### Con curl

```bash
# Health check
curl http://localhost:8080/health

# Crear venta
curl -X POST http://localhost:8080/api/sales \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "customer": {
      "name": "Juan Pérez",
      "identification": "1234567890",
      "email": "juan@example.com"
    },
    "products": [
      {
        "name": "Producto A",
        "price": 50000,
        "quantity": 2,
        "total": 100000
      }
    ]
  }' \
  --output factura.pdf
```

## Logs

Los logs se configuran en diferentes niveles:
- INFO: Flujo normal de la aplicación
- WARN: Situaciones anómalas pero recuperables
- ERROR: Errores que requieren atención

```bash
# Ver logs en Docker
docker logs orchestrator-service -f
```

## Troubleshooting

### Error: "Could not create connection to database"
- Verifica que el directorio `/app/data` tenga permisos de escritura
- Asegúrate de que SQLite esté instalado

### Error: "Connection refused" al llamar otros servicios
- Verifica que PDF y Email services estén corriendo
- Confirma las URLs en variables de entorno

### Error: "Invalid API Key"
- Verifica que el header `X-API-Key` esté presente
- Confirma que el valor coincida con `ORCHESTRATOR_API_KEY`