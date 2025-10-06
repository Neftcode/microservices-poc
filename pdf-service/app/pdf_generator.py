"""
Módulo para generación de PDFs de facturas.
Utiliza ReportLab para crear documentos PDF profesionales.
"""

from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_RIGHT
from io import BytesIO
from datetime import datetime
from decimal import Decimal
from .models import InvoiceRequest


class PDFGenerator:
    """
    Clase para generar PDFs de facturas electrónicas.
    Utiliza ReportLab para crear documentos con estilo profesional.
    """
    
    def __init__(self):
        """
        Inicializa el generador de PDF con estilos predefinidos.
        """
        self.styles = getSampleStyleSheet()
        self._setup_custom_styles()
    
    def _setup_custom_styles(self):
        """
        Configura estilos personalizados para el PDF.
        Define estilos para títulos, texto normal y alineaciones.
        """
        # Estilo para el título principal
        self.title_style = ParagraphStyle(
            'CustomTitle',
            parent=self.styles['Heading1'],
            fontSize=24,
            textColor=colors.HexColor('#2c3e50'),
            spaceAfter=30,
            alignment=TA_CENTER,
            fontName='Helvetica-Bold'
        )
        
        # Estilo para subtítulos
        self.subtitle_style = ParagraphStyle(
            'CustomSubtitle',
            parent=self.styles['Heading2'],
            fontSize=14,
            textColor=colors.HexColor('#34495e'),
            spaceAfter=12,
            fontName='Helvetica-Bold'
        )
        
        # Estilo para texto normal
        self.normal_style = ParagraphStyle(
            'CustomNormal',
            parent=self.styles['Normal'],
            fontSize=10,
            textColor=colors.black
        )
        # Estilo específico para celdas de producto que puede hacer wrap
        self.product_style = ParagraphStyle(
            'ProductStyle',
            parent=self.normal_style,
            fontSize=10,
            leading=12,
            # 'CJK' permite cortar palabras largas si es necesario; en la práctica
            # usar Paragraph con colWidths ya provoca wrapping.
            wordWrap='CJK'
        )
    
    def generate_invoice_pdf(self, invoice_data: InvoiceRequest) -> bytes:
        """
        Genera un PDF de factura a partir de los datos proporcionados.
        
        Args:
            invoice_data: Objeto InvoiceRequest con datos del cliente y productos
        
        Returns:
            bytes: Contenido del PDF generado
        
        Raises:
            Exception: Si hay error al generar el PDF
        """
        try:
            print(f"📄 Generando PDF para cliente: {invoice_data.customer.name}")
            
            # Crear buffer en memoria para el PDF
            buffer = BytesIO()
            
            # Crear documento PDF
            doc = SimpleDocTemplate(
                buffer,
                pagesize=letter,
                rightMargin=72,
                leftMargin=72,
                topMargin=72,
                bottomMargin=18
            )
            
            # Elementos que conformarán el PDF
            elements = []
            
            # Título de la factura
            title = Paragraph("FACTURA ELECTRÓNICA", self.title_style)
            elements.append(title)
            elements.append(Spacer(1, 0.3 * inch))
            
            # Información del cliente
            elements.append(Paragraph("DATOS DEL CLIENTE", self.subtitle_style))
            customer_info = [
                ["Nombre:", invoice_data.customer.name],
                ["Identificación:", invoice_data.customer.identification],
                ["Email:", invoice_data.customer.email],
                ["Fecha:", datetime.now().strftime("%d/%m/%Y %H:%M:%S")]
            ]
            
            customer_table = Table(customer_info, colWidths=[2*inch, 4*inch])
            customer_table.setStyle(TableStyle([
                ('FONTNAME', (0, 0), (0, -1), 'Helvetica-Bold'),
                ('FONTSIZE', (0, 0), (-1, -1), 10),
                ('TEXTCOLOR', (0, 0), (0, -1), colors.HexColor('#2c3e50')),
                ('ALIGN', (0, 0), (0, -1), 'RIGHT'),
                ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
                ('BOTTOMPADDING', (0, 0), (-1, -1), 8),
            ]))
            elements.append(customer_table)
            elements.append(Spacer(1, 0.4 * inch))
            
            # Tabla de productos
            elements.append(Paragraph("DETALLE DE PRODUCTOS", self.subtitle_style))
            
            # Encabezados de la tabla
            products_data = [["Producto", "Precio Unit.", "Cantidad", "Total"]]
            
            # Agregar productos
            total_general = Decimal('0')
            for product in invoice_data.products:
                # Usar Paragraph para que ReportLab haga wrap automáticamente
                product_name_par = Paragraph(product.name, self.product_style)
                products_data.append([
                    product_name_par,
                    f"${product.price:,.2f}",
                    str(product.quantity),
                    f"${product.total:,.2f}"
                ])
                total_general += product.total
            
            # Fila de total
            products_data.append(["", "", "TOTAL:", f"${total_general:,.2f}"])
            
            # Crear tabla de productos
            products_table = Table(products_data, colWidths=[3*inch, 1.5*inch, 1*inch, 1.5*inch])
            products_table.setStyle(TableStyle([
                # Estilo del encabezado
                ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#3498db')),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                ('FONTSIZE', (0, 0), (-1, 0), 11),
                ('ALIGN', (0, 0), (-1, 0), 'CENTER'),
                
                # Estilo del cuerpo
                ('FONTNAME', (0, 1), (-1, -2), 'Helvetica'),
                ('FONTSIZE', (0, 1), (-1, -2), 10),
                ('ALIGN', (1, 1), (-1, -1), 'RIGHT'),
                # Alineamos arriba para que las celdas con varias líneas se vean bien
                ('VALIGN', (0, 0), (-1, -1), 'TOP'),
                
                # Estilo de la fila de total
                ('BACKGROUND', (0, -1), (-1, -1), colors.HexColor('#ecf0f1')),
                ('FONTNAME', (0, -1), (-1, -1), 'Helvetica-Bold'),
                ('FONTSIZE', (0, -1), (-1, -1), 12),
                ('TEXTCOLOR', (0, -1), (-1, -1), colors.HexColor('#2c3e50')),
                
                # Bordes y líneas
                ('GRID', (0, 0), (-1, -2), 1, colors.HexColor('#bdc3c7')),
                ('LINEABOVE', (0, -1), (-1, -1), 2, colors.HexColor('#2c3e50')),
                ('LINEBELOW', (0, -1), (-1, -1), 2, colors.HexColor('#2c3e50')),
                
                # Padding
                # Reducir padding vertical para dar más espacio al texto envuelto
                ('TOPPADDING', (0, 0), (-1, -1), 8),
                ('BOTTOMPADDING', (0, 0), (-1, -1), 8),
                ('LEFTPADDING', (0, 0), (-1, -1), 10),
                ('RIGHTPADDING', (0, 0), (-1, -1), 10),
            ]))
            elements.append(products_table)
            elements.append(Spacer(1, 0.5 * inch))
            
            # Nota final
            note = Paragraph(
                "<i>Gracias por su compra. Factura generada electrónicamente.</i>",
                self.normal_style
            )
            elements.append(note)
            
            # Construir PDF
            doc.build(elements)
            
            # Obtener contenido del buffer
            pdf_content = buffer.getvalue()
            buffer.close()
            
            print(f"✅ PDF generado exitosamente ({len(pdf_content)} bytes)")
            return pdf_content
            
        except Exception as e:
            print(f"❌ Error al generar PDF: {str(e)}")
            raise Exception(f"Error al generar PDF: {str(e)}")