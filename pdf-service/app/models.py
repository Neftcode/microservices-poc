"""
Modelos Pydantic para validación de datos.
Define las estructuras de datos esperadas en las peticiones.
"""

from pydantic import BaseModel, Field, EmailStr
from typing import List
from decimal import Decimal


class CustomerInfo(BaseModel):
    """
    Modelo que representa la información del cliente.
    
    Attributes:
        name: Nombre completo del cliente
        identification: Número de identificación del cliente
        email: Correo electrónico válido del cliente
    """
    name: str = Field(..., min_length=1, description="Nombre del cliente")
    identification: str = Field(..., min_length=1, description="Identificación del cliente")
    email: str = Field(..., pattern=r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$", 
                      description="Email válido del cliente")


class ProductInfo(BaseModel):
    """
    Modelo que representa un producto en la factura.
    
    Attributes:
        name: Nombre del producto
        price: Precio unitario del producto
        quantity: Cantidad de unidades
        total: Precio total (price * quantity)
    """
    name: str = Field(..., min_length=1, description="Nombre del producto")
    price: Decimal = Field(..., gt=0, description="Precio unitario")
    quantity: int = Field(..., gt=0, description="Cantidad")
    total: Decimal = Field(..., gt=0, description="Total")


class InvoiceRequest(BaseModel):
    """
    Modelo principal para la petición de generación de factura.
    
    Attributes:
        customer: Información del cliente
        products: Lista de productos en la factura
    """
    customer: CustomerInfo = Field(..., description="Información del cliente")
    products: List[ProductInfo] = Field(..., min_length=1, description="Lista de productos")

    class Config:
        json_schema_extra = {
            "example": {
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
                    },
                    {
                        "name": "Producto B",
                        "price": 30000.00,
                        "quantity": 1,
                        "total": 30000.00
                    }
                ]
            }
        }