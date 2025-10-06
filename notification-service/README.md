# Microservicio C - Notificaciones por Email (Node.js)

## Navegación

- [⬅️ Volver al README principal](../README.md)
- [📐 Ver Arquitectura C4](../ARCHITECTURE.md)
- [🔧 Orchestrator Service](../orchestrator-service/README.md)
- [📄 PDF Service](../pdf-service/README.md)
- [🖥️ Frontend](../frontend/README.md)

---

## Descripción

Microservicio que realiza el envío de notificaciones por correo electrónico. Recibe facturas y las envía a los clientes utilizando Nodemailer con Gmail.

**Característica principal:** Procesamiento asíncrono mediante background tasks. El servicio acepta la petición inmediatamente (HTTP 202) y procesa el envío del email en segundo plano.

## Tecnologías

- Node.js 20
- Express 4.18
- Nodemailer 6.9

## Dependencias

```json
{
  "express": "^4.18.2",
  "nodemailer": "^6.9.7",
  "dotenv": "^16.3.1",
  "cors": "^2.8.5"
}
```

## Configuración

### Variables de Entorno Requeridas

```bash
EMAIL_SERVICE_API_KEY=tu-api-key-segura
GMAIL_USER=tu-correo@gmail.com
GMAIL_APP_PASSWORD=xxxx-xxxx-xxxx-xxxx
```

### Configurar Gmail

Para usar Nodemailer con Gmail, necesitas:

1. **Activar verificación en dos pasos** en tu cuenta de Google
2. **Generar una contraseña de aplicación:**
   - Ve a: https://myaccount.google.com/apppasswords
   - Selecciona "Correo" como la aplicación
   - Copia la contraseña de 16 caracteres generada
   - Úsala en la variable `GMAIL_APP_PASSWORD`

**Importante:** NO uses tu contraseña normal de Gmail, debe ser una contraseña de aplicación.

## Ejecución

### Con Docker

```bash
docker build -t notification-service .
docker run -p 8082:8082 \
  -e EMAIL_SERVICE_API_KEY=your-key \
  -e GMAIL_USER=tu@gmail.com \
  -e GMAIL_APP_PASSWORD=xxxx-xxxx-xxxx-xxxx \
  notification-service
```

### Local (sin Docker)

```bash
# Instalar dependencias
npm install

# Ejecutar en desarrollo
npm run dev

# Ejecutar en producción
npm start
```

## Endpoints

### POST /send-invoice
Envía factura por email de forma asíncrona.

**Comunicación ASÍNCRONA:** El servicio acepta la petición (202), la procesa en background y retorna inmediatamente sin esperar a que el email se envíe.

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
  ],
  "pdfBase64": "JVBERi0xLjQKJeLjz9MKMy..."
}
```

**Response (HTTP 202):**
```json
{
  "message": "Email aceptado para envío",
  "status": "processing"
}
```

**Status codes:**
- 202: Aceptado, se procesará en segundo plano
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
  "service": "notification-service",
  "emailConfigured": true
}
```

## Formato del email

El email enviado incluye:

**Asunto:** Factura Electrónica - [Nombre del Cliente]

**Cuerpo:**
- Saludo personalizado al cliente
- Tabla HTML con lista de productos comprados
- Totales por producto y total general
- Mensaje de agradecimiento

**Adjunto:**
- Factura en formato PDF

## Seguridad

- **API Key Validation**: Middleware que valida el header `X-API-Key`
- **Input Validation**: Validación de estructura de datos
- **Email Validation**: Verifica formato de email antes de enviar

## Procesamiento asíncrono

Este servicio demuestra comunicación asíncrona:

```javascript
// El endpoint acepta la petición inmediatamente
app.post('/send-invoice', (req, res) => {
  // Retorna 202 Accepted
  res.status(202).json({ message: "Aceptado" });
  
  // Procesa en background sin bloquear
  processEmailInBackground(data);
});
```

**Ventajas:**
- No bloquea el flujo principal
- Mejor experiencia de usuario
- Mayor throughput del sistema

**PATRON COMENTADO:** En producción se usaría una cola de mensajes (RabbitMQ, SQS) para garantizar entrega incluso si el servicio se reinicia.

## Probar el Servicio

### Con curl

```bash
# Health check
curl http://localhost:8082/health

# Enviar email (necesitas un PDF en base64)
curl -X POST http://localhost:8082/send-invoice \
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
    ],
    "pdfBase64": "..."
  }'
```

### Con JavaScript

```javascript
const axios = require('axios');

const data = {
  customer: {
    name: "Juan Pérez",
    identification: "1234567890",
    email: "juan@example.com"
  },
  products: [
    {
      name: "Producto A",
      price: 50000,
      quantity: 2,
      total: 100000
    }
  ],
  pdfBase64: "..."
};

axios.post('http://localhost:8082/send-invoice', data, {
  headers: {
    'X-API-Key': 'your-api-key',
    'Content-Type': 'application/json'
  }
})
.then(response => {
  console.log('Email aceptado:', response.data);
})
.catch(error => {
  console.error('Error:', error.response.data);
});
```

## Logs

El servicio registra:
- Peticiones recibidas
- Validación de API Key
- Inicio de envío de email
- Resultado del envío (éxito/error)

```bash
# Ver logs en Docker
docker logs notification-service -f
```

## Troubleshooting

### Error: "Invalid login: 535-5.7.8 Username and Password not accepted"

- Verifica que uses una **contraseña de aplicación**, no tu contraseña normal
- Asegúrate de tener activada la verificación en dos pasos
- Verifica que `GMAIL_USER` y `GMAIL_APP_PASSWORD` sean correctos

### Error: "getaddrinfo ENOTFOUND smtp.gmail.com"

- Verifica tu conexión a internet
- Asegúrate de que no haya firewall bloqueando puerto 587

### El email no llega

- Revisa la carpeta de spam del destinatario
- Verifica que el email del destinatario sea válido
- Revisa los logs del servicio para errores

### Gmail bloquea el envío

- Gmail tiene límites de envío: 500 emails/día
- Si envías muchos emails rápido, puede activarse protección
- Espera unos minutos e intenta de nuevo

## Patrones Comentados

1. **Message Queue**: Para garantizar entrega de emails
2. **Retry Pattern**: Reintentar envíos fallidos
3. **Dead Letter Queue**: Para emails que no se pudieron enviar
4. **Circuit Breaker**: Detener intentos si Gmail está caído