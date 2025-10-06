# Microservicio B - Generador de PDF (Python FastAPI)

## Navegación

- [⬅️ Volver al README principal](../README.md)
- [📐 Ver Arquitectura C4](../ARCHITECTURE.md)
- [🔧 Orchestrator Service](../orchestrator-service/README.md)
- [📧 Notification Service](../notification-service/README.md)
- [🖥️ Frontend](../frontend/README.md)

---

## Descripción

Microservicio que realiza la generación de facturas en formato PDF. Recibe información de ventas y genera un documento PDF con los detalles de la transacción.

## Tecnologías

- Python 3.11
- FastAPI 0.104.1
- Uvicorn (servidor ASGI)
- ReportLab (generación de PDF)
- Pydantic (validación de datos)

## Dependencias

```txt
fastapi==0.104.1
uvicorn==0.24.0
reportlab==4.0.7
pydantic==2.5.0
```

## Estructura del Proyecto

```
pdf-service/
├── app/
│   ├── __init__.py
│   ├── main.py                # Aplicación FastAPI principal
│   ├── models.py              # Modelos Pydantic
│   ├── pdf_generator.py       # Lógica de generación de PDF
│   └── middleware.py          # Middleware de seguridad (API Key)
├── requirements.txt
├── Dockerfile
└── README.md
```

## Configuración

### Variables de entorno requeridas

```bash
PDF_SERVICE_API_KEY=tu-api-key-segura
```

## Ejecución

### Con Docker

```bash
docker build -t pdf-service .
docker run -p 8081:8081 \
  -e PDF_SERVICE_API_KEY=your-key \
  pdf-service
```

### Local (sin Docker)

```bash
# Crear entorno virtual
python -m venv venv
source venv/bin/activate  # Windows: venv/Scripts/activate

# Instalar dependencias
pip install -r requirements.txt

# Ejecutar servidor
uvicorn app.main:app --host 0.0.0.0 --port 8081 --reload
```

## Endpoints

### POST /generate-pdf
Genera un PDF de factura a partir de los datos de venta.

**Headers:**
```
X-API-Key: your-api-key
Content-Type: application/json
```

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
- 401: API Key faltante
- 403: API Key inválido
- 500: Error interno

### GET /health
Health check del servicio.

**Response:**
```json
{
  "status": "UP",
  "service": "pdf-service"
}
```

## Características del PDF

El PDF generado incluye:
- Logo/encabezado de la factura
- Información del cliente
- Tabla detallada de productos con:
  - Nombre del producto
  - Precio unitario
  - Cantidad
  - Total por producto
- Total general de la factura
- Fecha de generación
- Formato profesional con colores y estilos

## Seguridad

- **API Key Validation**: Middleware que valida el header `X-API-Key`
- **Input Validation**: Validación automática con Pydantic models
- **CORS**: Configurado para aceptar peticiones del orquestador

## Documentación API

FastAPI genera documentación automática:

- **Swagger UI**: http://localhost:8081/docs
- **ReDoc**: http://localhost:8081/redoc
- **OpenAPI JSON**: http://localhost:8081/openapi.json

## Probar el servicio

### Con curl

```bash
# Health check
curl http://localhost:8081/health

# Generar PDF
curl -X POST http://localhost:8081/generate-pdf \
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

### Con Python

```python
import requests

url = "http://localhost:8081/generate-pdf"
headers = {
    "X-API-Key": "your-api-key",
    "Content-Type": "application/json"
}
data = {
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
}

response = requests.post(url, json=data, headers=headers)
with open("factura.pdf", "wb") as f:
    f.write(response.content)
```

## Logs

Los logs incluyen:
- Peticiones recibidas
- Validación de API Key
- Generación de PDF
- Errores y excepciones

```bash
# Ver logs en Docker
docker logs pdf-service -f
```

## Patrones comentados en el código

Estos patrones NO están implementados, pero se comentan donde serían aplicables:

1. **Caching**: Cache de PDFs generados para peticiones repetidas
2. **Rate Limiting**: Limitar número de peticiones por cliente
3. **Async Processing**: Procesamiento asíncrono para PDFs muy grandes

## Troubleshooting

### Error: "ReportLab not found"
```bash
pip install --upgrade reportlab
```

### Error: "Port 8081 already in use"
```bash
# Cambiar puerto en el comando uvicorn
uvicorn app.main:app --host 0.0.0.0 --port 8082
```

### PDF no se genera correctamente
- Verifica que los datos de entrada sean válidos
- Revisa los logs para errores específicos
- Asegúrate de tener permisos de escritura