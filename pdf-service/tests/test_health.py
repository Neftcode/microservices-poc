"""
Tests básicos del PDF Service.
Verifica que los endpoints principales responden correctamente.
"""
import os
import pytest
from httpx import AsyncClient, ASGITransport

# Configurar variable de entorno antes de importar la app
os.environ.setdefault("PDF_SERVICE_API_KEY", "test-api-key")

from app.main import app


@pytest.mark.asyncio
async def test_health_endpoint():
    """Verifica que el endpoint /health responde con status UP."""
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as client:
        response = await client.get("/health")

    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert data["service"] == "pdf-service"


@pytest.mark.asyncio
async def test_generate_pdf_requires_api_key():
    """Verifica que el endpoint /generate-pdf requiere API Key."""
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as client:
        response = await client.post("/generate-pdf", json={})

    # Sin API Key debe retornar 401 o 403
    assert response.status_code in (401, 403)
