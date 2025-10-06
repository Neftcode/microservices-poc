"""
Aplicación principal del Microservicio B - Generador de PDF.
FastAPI application que expone endpoints para generación de facturas en PDF.
"""

from fastapi import FastAPI, HTTPException, status
from fastapi.responses import Response
from fastapi.middleware.cors import CORSMiddleware
from datetime import datetime
from .models import InvoiceRequest
from .pdf_generator import PDFGenerator
from .middleware import ApiKeyMiddleware

# Importar OpenAPI utils al inicio
from fastapi.openapi.utils import get_openapi

# Crear instancia de FastAPI
app = FastAPI(
    title="PDF Service",
    description="Microservicio para generación de facturas en PDF",
    version="1.0.0",
    openapi_tags=[
        {
            "name": "PDF Generation",
            "description": "Endpoints para generación de PDFs"
        }
    ]
)

# Configuración de seguridad
security_scheme = {
    "type": "apiKey",
    "in": "header",
    "name": "X-API-Key",
    "description": "API Key para autenticación"
}

def custom_openapi():
    if app.openapi_schema:
        return app.openapi_schema

    openapi_schema = get_openapi(
        title=app.title,
        version=app.version,
        description=app.description,
        routes=app.routes,
    )
    
    # Establecer versión de OpenAPI
    openapi_schema["openapi"] = "3.0.2"
    
    # Agregar componentes de seguridad
    if "components" not in openapi_schema:
        openapi_schema["components"] = {}
    openapi_schema["components"]["securitySchemes"] = {
        "ApiKeyAuth": security_scheme
    }
    # Aplicar seguridad globalmente
    openapi_schema["security"] = [
        {"ApiKeyAuth": []}
    ]
    
    app.openapi_schema = openapi_schema
    return app.openapi_schema

app.openapi = custom_openapi

# Configurar middlewares
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # En producción, especificar orígenes exactos
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Agregar middleware de API Key
app.add_middleware(ApiKeyMiddleware)

# Inicializar generador de PDF
pdf_generator = PDFGenerator()


@app.post(
    "/generate-pdf",
    response_class=Response,
    status_code=status.HTTP_200_OK,
    summary="Generar PDF de factura",
    description="Genera un documento PDF con la información de la factura proporcionada",
    tags=["PDF Generation"],
    responses={
        200: {
            "description": "PDF generado exitosamente",
            "content": {"application/pdf": {}}
        },
        400: {"description": "Datos inválidos"},
        401: {"description": "API Key faltante"},
        403: {"description": "API Key inválido"},
        500: {"description": "Error al generar PDF"}
    }
)
async def generate_pdf(invoice_data: InvoiceRequest):
    """
    Endpoint para generar PDF de factura.
    Comunicación SÍNCRONA: Genera el PDF y lo retorna inmediatamente.
    
    PATRON COMENTADO: Se podría implementar caching para PDFs idénticos
    y reducir carga de procesamiento en peticiones repetidas.
    
    Args:
        invoice_data: Datos de la factura (validados por Pydantic)
    
    Returns:
        Response: PDF binary con headers apropiados
    
    Raises:
        HTTPException: Si hay error al generar el PDF
    """
    try:
        print(f"📥 Recibida petición para generar PDF")
        print(f"   Cliente: {invoice_data.customer.name}")
        print(f"   Productos: {len(invoice_data.products)}")
        
        # Generar PDF
        pdf_bytes = pdf_generator.generate_invoice_pdf(invoice_data)
        
        # PATRON: Aquí se podría guardar el PDF en almacenamiento (S3, etc.)
        # para tener un histórico de facturas generadas
        
        # Retornar PDF con headers apropiados
        return Response(
            content=pdf_bytes,
            media_type="application/pdf",
            headers={
                "Content-Disposition": "attachment; filename=factura.pdf",
                "Cache-Control": "no-cache"
            }
        )
        
    except Exception as e:
        print(f"❌ Error al generar PDF: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error al generar PDF: {str(e)}"
        )


@app.get(
    "/health",
    summary="Health check",
    description="Verifica que el servicio está funcionando correctamente",
    responses={
        200: {"description": "Servicio funcionando correctamente"}
    }
)
async def health_check():
    """
    Endpoint de health check.
    Permite verificar que el servicio está activo y respondiendo.
    
    PATRON COMENTADO: En producción, este endpoint sería usado por
    herramientas de orquestación (Kubernetes, Docker Swarm) para
    verificar la salud del servicio y reiniciarlo si es necesario.
    
    Returns:
        dict: Estado del servicio
    """
    return {
        "status": "UP",
        "service": "pdf-service",
        "timestamp": datetime.now().isoformat()
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8081)