/**
 * Configuración de Nodemailer para envío de emails.
 * Configura el transporter de Gmail con credenciales seguras.
 */

const nodemailer = require('nodemailer');

/**
 * Crea y configura el transporter de Nodemailer para Gmail.
 * 
 * PATRON COMENTADO: Se podría implementar un pool de conexiones SMTP
 * para manejar mayor volumen de emails concurrentes.
 * 
 * @returns {nodemailer.Transporter} Transporter configurado
 */
function createEmailTransporter() {
    const config = {
        service: 'gmail',
        auth: {
            user: process.env.GMAIL_USER,
            pass: process.env.GMAIL_APP_PASSWORD
        },
        // Configuración adicional para mayor confiabilidad
        pool: true, // Usar pool de conexiones
        maxConnections: 5, // Máximo de conexiones simultáneas
        maxMessages: 100 // Máximo de mensajes por conexión
    };

    const transporter = nodemailer.createTransport(config);

    // Verificar configuración al iniciar
    transporter.verify((error, success) => {
        if (error) {
            console.error('❌ Error en configuración de email:', error.message);
            console.error('   Verifica GMAIL_USER y GMAIL_APP_PASSWORD');
        } else {
            console.log('✅ Servidor de email configurado correctamente');
        }
    });

    return transporter;
}

module.exports = { createEmailTransporter };