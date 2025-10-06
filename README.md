# Sistema de Facturación Electrónica - Arquitectura de Microservicios

## Grupo #3: Integrantes

- Álvaro Jesús Muñoz Martínez
- Julián Camilo Corredor Rojas
- Luis Alfredo González Mercado
- Luis Eduardo González Mejía
- Carlos Alberto Arevalo Martinez

## Descripción del proyecto

Prueba de concepto que demuestra una arquitectura de microservicios para un sistema de generación y envío de facturas electrónicas. El proyecto utiliza tres lenguajes de programación diferentes (Java, Python y Node.js) para ilustrar la interoperabilidad y comunicación entre servicios.

## Arquitectura

```
┌─────────────────┐
│    Frontend     │
│   React + Vite  │ :5173
└────────┬────────┘
         │ HTTP REST
         ▼
┌─────────────────────────────────┐
│   Microservicio A               │
│   Orquestador (Java Spring)     │ :8080
│   - Validación de datos         │
│   - Base de datos SQLite        │
│   - Coordinación de servicios   │
└────┬──────────────────────┬─────┘
     │ Síncrona             │ Asíncrona
     │ (API-Key)            │ (API-Key)
     ▼                      ▼
┌──────────────┐      ┌──────────────┐
│  Micro B     │      │  Micro C     │
│  Python      │      │  Node.js     │
│  FastAPI     │:8081 │  Express     │:8082
│  Gen. PDF    │      │  Email       │
└──────────────┘      └──────────────┘
```

## Tecnologías Utilizadas

| Componente | Tecnología | Puerto |
|------------|-----------|--------|
| Frontend | React 18 + Vite | 5173 |
| Microservicio A (Orquestador) | Java 21 + Spring Boot 3 + Maven | 8080 |
| Microservicio B (PDF) | Python 3.11 + FastAPI | 8081 |
| Microservicio C (Email) | Node.js 20 + Express | 8082 |
| Base de Datos | SQLite 3 | - |

## Estructura del Proyecto

```
invoice-microservices/
├── frontend/                      # React SPA
│   ├── src/
│   ├── Dockerfile
│   ├── package.json
│   └── README.md
├── orchestrator-service/          # Microservicio A (Java)
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── pdf-service/                   # Microservicio B (Python)
│   ├── app/
│   ├── requirements.txt
│   ├── Dockerfile
│   └── README.md
├── notification-service/          # Microservicio C (Node.js)
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── README.md
├── docker-compose.yml
├── .env.example
└── README.md
```

## Conceptos de Microservicios Demostrados

### 1. **Comunicación síncrona**
- El Orquestador llama al servicio de PDF y espera la respuesta
- Utiliza HTTP REST con timeouts

### 2. **Comunicación asíncrona**
- El Orquestador envía la petición al servicio de Email
- Recibe respuesta inmediata (HTTP 202 Accepted)
- El procesamiento ocurre en segundo plano (Background Tasks)

### 3. **Seguridad entre servicios**
- Validación de API-Key mediante header `X-API-Key`
- Cada microservicio valida la autenticidad de las peticiones

### 4. **Independencia de lenguajes**
- Cada servicio puede ser desarrollado en diferente tecnología
- Comunicación mediante estándares (HTTP/JSON)

### 5. **Separación de responsabilidades**
- Cada microservicio tiene una única responsabilidad
- Base de datos solo en el Orquestador

### 6. **Patrones no implementados**
- Circuit breaker
- Retry pattern
- Timeout management
- Health checks
- Service discovery

## Ejecución con Docker Compose

### Opción 1: Ejecutar todo el sistema

```bash
# Construir y ejecutar todos los servicios
docker-compose up --build

# Ejecutar en segundo plano
docker-compose up -d --build

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

### Opción 2: Ejecutar servicios individualmente

```bash
# Solo el orquestador
docker-compose up orchestrator-service

# Solo el servicio de PDF
docker-compose up pdf-service

# Solo el servicio de email
docker-compose up notification-service

# Solo el frontend
docker-compose up frontend
```

## Ejecución Local (Sin Docker)

### Prerequisitos
- Java 21+
- Python 3.11+
- Node.js 20+
- Maven 3.8+

### Microservicio A (Orquestador - Java)

```bash
cd orchestrator-service
mvn clean install
mvn spring-boot:run
```

### Microservicio B (PDF - Python)

```bash
cd pdf-service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8081
```

### Microservicio C (Email - Node.js)

```bash
cd notification-service
npm install
npm start
```

### Frontend (React)

```bash
cd frontend
npm install
npm run dev
```

## 📡 Endpoints de la API

### Microservicio A - Orquestador (Puerto 8080)

- `POST /api/sales` - Crear nueva venta y generar factura
- `GET /health` - Health check
- `GET /swagger-ui.html` - Documentación Swagger

### Microservicio B - PDF (Puerto 8081)

- `POST /generate-pdf` - Generar PDF de factura
- `GET /health` - Health check
- `GET /docs` - Documentación Swagger (automática de FastAPI)

### Microservicio C - Email (Puerto 8082)

- `POST /send-invoice` - Enviar factura por email (asíncrono)
- `GET /health` - Health check

## Probar la Aplicación

1. Accede al frontend: http://localhost:5173
2. Completa el formulario:
   - Agrega productos con precio y cantidad
   - Ingresa datos del comprador (nombre, ID, email válido)
3. Haz clic en "Realizar Venta"
4. El PDF se generará y mostrará
5. Revisa tu email para ver la factura enviada

## Base de Datos

El Microservicio A utiliza SQLite con la siguiente estructura:

**Tabla: sales**
```sql
CREATE TABLE sales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name TEXT NOT NULL,
    customer_id TEXT NOT NULL,
    customer_email TEXT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    products TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Ubicación: `orchestrator-service/data/sales.db`

## Documentación de APIs

- **Orquestador (Java)**: http://localhost:8080/swagger-ui.html
- **PDF Service (Python)**: http://localhost:8081/docs
- **Email Service (Node.js)**: No tiene Swagger (simplificado)

## Troubleshooting

### Error: "Connection refused" entre servicios

- Verifica que todos los servicios estén corriendo
- Revisa los logs: `docker-compose logs [service-name]`
- Asegúrate de que los puertos no estén ocupados

### Error: "Invalid API Key"

- Verifica que las API Keys en `.env` coincidan en todos los servicios
- Reinicia los contenedores después de cambiar `.env`

### Error al enviar email con Gmail

- Verifica que la verificación en dos pasos esté activada
- Usa una contraseña de aplicación, no tu contraseña normal
- Revisa que `GMAIL_USER` y `GMAIL_APP_PASSWORD` estén correctos

### Puerto ya en uso

```bash
# Verificar qué está usando el puerto
lsof -i :8080  # Mac/Linux
netstat -ano | findstr :8080  # Windows

# Cambiar puerto en docker-compose.yml si es necesario
```