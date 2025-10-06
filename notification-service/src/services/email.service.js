/**
 * Servicio de envío de emails.
 * Contiene la lógica para generar y enviar emails con facturas.
 */

const { createEmailTransporter } = require('../config/email.config');

// Crear transporter único (reutilizable)
const transporter = createEmailTransporter();

/**
 * Genera el HTML del email con la información de la factura.
 * 
 * @param {Object} customer - Información del cliente
 * @param {Array} products - Lista de productos
 * @returns {string} HTML del email
 */
function generateEmailHTML(customer, products) {
    // Calcular total
    const total = products.reduce((sum, product) => sum + parseFloat(product.total), 0);

    // Generar filas de productos para la tabla
    const productRows = products.map(product => `
        <tr>
            <td style="padding: 12px; border: 1px solid #ddd;">${product.name}</td>
            <td style="padding: 12px; border: 1px solid #ddd; text-align: right;">$${parseFloat(product.price).toLocaleString('es-CO', { minimumFractionDigits: 2 })}</td>
            <td style="padding: 12px; border: 1px solid #ddd; text-align: center;">${product.quantity}</td>
            <td style="padding: 12px; border: 1px solid #ddd; text-align: right;">$${parseFloat(product.total).toLocaleString('es-CO', { minimumFractionDigits: 2 })}</td>
        </tr>
    `).join('');

    // HTML del email con estilo
    return `
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Factura Electrónica</title>
        </head>
        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
            <div style="background-color: #3498db; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;">
                <h1 style="margin: 0;">Factura Electrónica</h1>
            </div>
            
            <div style="background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; border-top: none;">
                <h2 style="color: #3498db; margin-top: 0;">Estimado/a ${customer.name},</h2>
                
                <p>Gracias por su compra. A continuación encontrará el detalle de su factura:</p>
                
                <div style="margin: 20px 0;">
                    <h3 style="color: #2c3e50;">Datos del Cliente:</h3>
                    <p style="margin: 5px 0;"><strong>Nombre:</strong> ${customer.name}</p>
                    <p style="margin: 5px 0;"><strong>Identificación:</strong> ${customer.identification}</p>
                    <p style="margin: 5px 0;"><strong>Email:</strong> ${customer.email}</p>
                </div>
                
                <h3 style="color: #2c3e50;">Detalle de Productos:</h3>
                <table style="width: 100%; border-collapse: collapse; margin: 15px 0;">
                    <thead>
                        <tr style="background-color: #3498db; color: white;">
                            <th style="padding: 12px; border: 1px solid #ddd; text-align: left;">Producto</th>
                            <th style="padding: 12px; border: 1px solid #ddd; text-align: right;">Precio Unit.</th>
                            <th style="padding: 12px; border: 1px solid #ddd; text-align: center;">Cantidad</th>
                            <th style="padding: 12px; border: 1px solid #ddd; text-align: right;">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${productRows}
                    </tbody>
                    <tfoot>
                        <tr style="background-color: #ecf0f1; font-weight: bold;">
                            <td colspan="3" style="padding: 12px; border: 1px solid #ddd; text-align: right;">TOTAL:</td>
                            <td style="padding: 12px; border: 1px solid #ddd; text-align: right; color: #2c3e50; font-size: 1.2em;">$${total.toLocaleString('es-CO', { minimumFractionDigits: 2 })}</td>
                        </tr>
                    </tfoot>
                </table>
                
                <div style="margin-top: 30px; padding: 15px; background-color: #e8f4f8; border-left: 4px solid #3498db;">
                    <p style="margin: 0;"><strong>Nota:</strong> Encontrará adjunto el PDF de su factura para sus registros.</p>
                </div>
                
                <p style="margin-top: 30px; text-align: center; color: #7f8c8d; font-size: 0.9em;">
                    Gracias por confiar en nosotros.<br>
                    <em>Este es un email automático, por favor no responder.</em>
                </p>
            </div>
            
            <div style="background-color: #2c3e50; color: white; padding: 15px; text-align: center; border-radius: 0 0 5px 5px; font-size: 0.8em;">
                <p style="margin: 0;">© ${new Date().getFullYear()} Sistema de Facturación Electrónica</p>
            </div>
        </body>
        </html>
    `;
}

/**
 * Envía un email con la factura al cliente.
 * Procesa el envío de forma asíncrona sin bloquear.
 * 
 * PATRON COMENTADO: En producción se implementaría:
 * - Retry Pattern con backoff exponencial
 * - Dead Letter Queue para emails fallidos
 * - Logging en base de datos del estado de cada envío
 * 
 * @param {Object} customer - Información del cliente
 * @param {Array} products - Lista de productos
 * @param {string} pdfBase64 - PDF en formato base64
 * @returns {Promise<Object>} Resultado del envío
 */
async function sendInvoiceEmail(customer, products, pdfBase64) {
    try {
        console.log(`📧 Preparando email para: ${customer.email}`);

        // Convertir base64 a buffer
        const pdfBuffer = Buffer.from(pdfBase64, 'base64');

        // Generar HTML del email
        const htmlContent = generateEmailHTML(customer, products);

        // Configurar el email
        // Validar si se recibió adjunto pdfBase64, sino no adjuntar
        const attachments = [];
        if (pdfBase64) {
            attachments.push({
                filename: 'factura.pdf',
                content: pdfBuffer,
                contentType: 'application/pdf'
            });
        }
        const mailOptions = {
            from: {
                name: 'Facturación Electrónica',
                address: process.env.GMAIL_USER
            },
            to: customer.email,
            subject: `Factura Electrónica - ${customer.name}`,
            html: htmlContent,
            attachments: attachments
        };

        // Enviar email
        console.log(`📤 Enviando email a ${customer.email}...`);
        const info = await transporter.sendMail(mailOptions);

        console.log(`✅ Email enviado exitosamente a ${customer.email}`);
        console.log(`   Message ID: ${info.messageId}`);

        return {
            success: true,
            messageId: info.messageId,
            recipient: customer.email
        };

    } catch (error) {
        console.error(`❌ Error al enviar email a ${customer.email}:`, error.message);
        
        // PATRON: Aquí se enviaría a una Dead Letter Queue para retry posterior
        return {
            success: false,
            error: error.message,
            recipient: customer.email
        };
    }
}

/**
 * Procesa el envío de email en segundo plano.
 * Esta función se ejecuta sin bloquear el hilo principal.
 * 
 * @param {Object} data - Datos completos para el email
 */
function processEmailInBackground(data) {
    // Ejecutar de forma asíncrona sin esperar resultado
    setImmediate(async () => {
        try {
            await sendInvoiceEmail(
                data.customer,
                data.products,
                data.pdfBase64
            );
        } catch (error) {
            console.error('❌ Error en procesamiento background:', error.message);
        }
    });
}

module.exports = {
    sendInvoiceEmail,
    processEmailInBackground
};