"""
Middleware de seguridad para validación de API Key.
Intercepta las peticiones y valida la autenticación.
"""

import os
from pathlib import Path
from dotenv import load_dotenv
from fastapi import Request, HTTPException, status
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware

# Cargar variables de entorno desde .env
env_path = Path(__file__).parent.parent / '.env'
load_dotenv(dotenv_path=env_path)


class ApiKeyMiddleware(BaseHTTPMiddleware):
    """
    Middleware que valida el API Key en cada petición.
    
    PATRON COMENTADO: En producción se usaría OAuth2/JWT para
    autenticación más robusta. El API Key es para demostración.
    """
    
    def __init__(self, app):
        """
        Inicializa el middleware.
        
        Args:
            app: Instancia de la aplicación FastAPI
        """
        super().__init__(app)
        self.api_key = os.getenv("PDF_SERVICE_API_KEY", "default-pdf-key")
        
        # Rutas que no requieren autenticación
        self.public_paths = ["/health", "/docs", "/redoc", "/openapi.json"]
    
    async def dispatch(self, request: Request, call_next):
        """
        Procesa cada petición y valida el API Key.
        
        Args:
            request: Petición HTTP entrante
            call_next: Siguiente middleware/handler en la cadena
        
        Returns:
            Response: Respuesta HTTP
        """
        # Permitir acceso a rutas públicas
        if request.url.path in self.public_paths:
            return await call_next(request)
        
        # Obtener API Key del header
        api_key = request.headers.get("X-API-Key")
        
        # Validar presencia del API Key
        if not api_key:
            return JSONResponse(
                status_code=status.HTTP_401_UNAUTHORIZED,
                content={
                    "error": "API Key requerido",
                    "message": "Debe incluir el header X-API-Key"
                }
            )
        
        # Validar API Key correcto
        if api_key != self.api_key:
            return JSONResponse(
                status_code=status.HTTP_403_FORBIDDEN,
                content={
                    "error": "API Key inválido",
                    "message": "El API Key proporcionado no es válido"
                }
            )
        
        # API Key válido, continuar con la petición
        response = await call_next(request)
        return response