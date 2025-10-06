# Frontend - Sistema de Facturación (React + Vite)

## Navegación

- [⬅️ Volver al README principal](../README.md)
- [📐 Ver Arquitectura C4](../ARCHITECTURE.md)
- [🔧 Orchestrator Service](../orchestrator-service/README.md)
- [📄 PDF Service](../pdf-service/README.md)
- [📧 Notification Service](../notification-service/README.md)

---

## Descripción

Aplicación web SPA (Single Page Application) construida con React y Vite que permite crear ventas y generar facturas electrónicas.

## Tecnologías

- React 18
- Vite 5
- JavaScript (ES6+)
- CSS Modules

## Dependencias

```json
{
  "react": "^18.2.0",
  "react-dom": "^18.2.0"
}
```

## Estructura del proyecto

```
frontend/
├── src/
│   ├── App.jsx              # Componente principal
│   ├── App.css              # Estilos globales
│   ├── main.jsx             # Punto de entrada
│   └── index.css            # Estilos base
├── public/
├── index.html
├── package.json
├── vite.config.js
├── Dockerfile
└── README.md
```

## Configuración

### Variables de entorno (Opcional)

Si quieres cambiar la URL del backend:

```bash
VITE_API_URL=http://localhost:8080
```

Por defecto usa: `http://localhost:8080`

## Ejecución

### Con Docker

```bash
docker build -t frontend .
docker run -p 5173:5173 frontend
```

### Local (sin Docker)

```bash
# Instalar dependencias
npm install

# Ejecutar en desarrollo
npm run dev

# Compilar para producción
npm run build

# Vista previa de producción
npm run preview
```

## Características

### Panel izquierdo - Entrada de datos

1. **Información del cliente:**
   - Nombre completo
   - Número de identificación
   - Email (con validación de patrón)

2. **Gestión de productos:**
   - Agregar productos dinámicamente
   - Campos por producto:
     - Nombre del producto
     - Precio unitario (en COP $)
     - Cantidad de unidades
   - Cálculo automático del total por producto
   - Cálculo automático del total general
   - Eliminar productos individuales

3. **Validaciones:**
   - Email debe tener formato válido: `usuario@dominio.tld`
   - Todos los campos son obligatorios
   - Precios y cantidades deben ser mayores a 0

### Panel derecho - Visualización

- **Antes de generar:** Mensaje de instrucciones
- **Después de generar:** Visualización del PDF generado
- **En caso de error:** Mensaje de error detallado

## Integración con Backend

El frontend se comunica con el Microservicio A (Orquestador):

```javascript
POST /api/sales
Headers: {
  "X-API-Key": "orchestrator-secret-key-123456789",
  "Content-Type": "application/json"
}
Body: {
  "customer": { ... },
  "products": [ ... ]
}
```

## Flujo de Usuario

1. Usuario completa datos del cliente
2. Usuario agrega uno o más productos
3. Sistema calcula totales automáticamente
4. Usuario hace clic en "Realizar Venta"
5. Frontend envía petición al backend
6. Backend procesa (guarda BD, genera PDF, envía email)
7. Frontend recibe y muestra el PDF
8. Usuario puede descargar el PDF

## Probar la Aplicación

1. Accede a: http://localhost:5173
2. Completa el formulario:
   ```
   Nombre: Juan Pérez
   Identificación: 1234567890
   Email: juan.perez@example.com
   
   Producto 1: Laptop
   Precio: 2500000
   Cantidad: 1
   
   Producto 2: Mouse
   Precio: 50000
   Cantidad: 2
   ```
3. Click en "Realizar Venta"
4. Espera el PDF en el panel derecho

## Personalización de Estilos

Los estilos están en `App.css` y puedes personalizarlos fácilmente:

- Colores principales
- Tamaños de fuente
- Espaciados
- Diseño responsive

## Responsive Design

La aplicación es responsive y se adapta a:
- Desktop (1024px+)
- Tablet (768px - 1023px)
- Mobile (< 768px)

## Troubleshooting

### Error: "Failed to fetch"

- Verifica que el backend esté corriendo en puerto 8080
- Asegúrate de que CORS esté configurado en el backend
- Revisa la consola del navegador para más detalles

### PDF no se muestra

- Verifica que el backend esté retornando `application/pdf`
- Revisa la consola del navegador para errores
- Asegúrate de que el navegador soporte PDFs

### Email no válido

- El patrón validado es: `texto@dominio.ext`
- Debe tener una parte local, @, y un dominio con TLD
- Ejemplos válidos: `user@example.com`, `test@domain.co`

## Scripts Disponibles

```bash
# Desarrollo
npm run dev

# Build de producción
npm run build

# Preview del build
npm run preview

# Linting
npm run lint
```

## Seguridad

- La API Key se envía en el header `X-API-Key`
- Por seguridad, en producción la API Key debería estar en variables de entorno del servidor, no del cliente
- CORS está habilitado en el backend para permitir peticiones del frontend