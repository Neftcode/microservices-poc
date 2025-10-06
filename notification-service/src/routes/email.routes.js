/**
 * Rutas para el servicio de email.
 * Define los endpoints de la API.
 */

const express = require('express');
const { processEmailInBackground } = require('../services/email.service');

const router = express.Router();

/**
 * POST /send-invoice
 * Endpoint para enviar factura por email (asíncrono).
 * 
 * Comunicación ASÍNCRONA: Acepta la petición inmediatamente (HTTP 202)
 * y procesa el envío en segundo plano sin bloquear.
 * 
 * PATRON COMENTADO: En producción, esto se haría con una cola de mensajes
 * (RabbitMQ, AWS SQS, Redis Queue) para garantizar entrega y permitir
 * escalabilidad horizontal.
 * 
 * @route POST /send-invoice
 * @param {Object} req.body.customer - Información del cliente
 * @param {Array} req.body.products - Lista de productos
 * @param {string} req.body.pdfBase64 - PDF en base64
 * @returns {Object} Respuesta con HTTP 202 Accepted
 */
router.post('/send-invoice', (req, res) => {
    try {
        console.log('📥 Recibida petición de envío de email');

        const { customer, products, pdfBase64 } = req.body;

        // Validar datos requeridos
        if (!customer || !products) {
            return res.status(400).json({
                error: 'Datos incompletos',
                message: 'Se requieren customer y products'
            });
        }

        // Validar estructura del customer
        if (!customer.name || !customer.identification || !customer.email) {
            return res.status(400).json({
                error: 'Datos del cliente incompletos',
                message: 'Se requieren name, identification y email'
            });
        }

        // Validar formato de email
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(customer.email)) {
            return res.status(400).json({
                error: 'Email inválido',
                message: 'El formato del email no es válido'
            });
        }

        // Validar que haya productos
        if (!Array.isArray(products) || products.length === 0) {
            return res.status(400).json({
                error: 'Productos inválidos',
                message: 'Debe incluir al menos un producto'
            });
        }

        console.log(`   Cliente: ${customer.name}`);
        console.log(`   Email: ${customer.email}`);
        console.log(`   Productos: ${products.length}`);

        // Procesar en segundo plano (NO BLOQUEANTE)
        processEmailInBackground({
            customer,
            products,
            pdfBase64
        });

        // Responder inmediatamente con HTTP 202 Accepted
        // El cliente no espera a que el email se envíe
        console.log('✅ Petición aceptada, procesando en background');
        
        res.status(202).json({
            message: 'Email aceptado para envío',
            status: 'processing',
            recipient: customer.email
        });

    } catch (error) {
        console.error('❌ Error en endpoint send-invoice:', error.message);
        res.status(500).json({
            error: 'Error interno del servidor',
            message: error.message
        });
    }
});

/**
 * GET /health
 * Health check del servicio.
 * 
 * PATRON COMENTADO: En producción, este endpoint sería usado por
 * herramientas de orquestación (Kubernetes, Docker Swarm) para
 * verificar la salud del servicio y reiniciarlo si es necesario.
 * 
 * @route GET /health
 * @returns {Object} Estado del servicio
 */
router.get('/health', (req, res) => {
    const health = {
        status: 'UP',
        service: 'notification-service',
        timestamp: new Date().toISOString(),
        emailConfigured: !!(process.env.GMAIL_USER && process.env.GMAIL_APP_PASSWORD)
    };

    res.status(200).json(health);
});

module.exports = router;