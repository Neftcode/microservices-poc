/**
 * Aplicación principal del Microservicio C - Notificaciones por Email.
 * Express application que expone endpoints para envío de emails.
 */

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const emailRoutes = require('./routes/email.routes');
const { validateApiKey } = require('./middleware/apiKey.middleware');

// Crear aplicación Express
const app = express();
const PORT = process.env.PORT || 8082;

// Middlewares globales
app.use(cors()); // Permitir CORS
app.use(express.json({ limit: '50mb' })); // Parser JSON con límite amplio para PDFs
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// Logging middleware para debug
app.use((req, res, next) => {
    console.log('Request Body:', req.body);
    next();
});

// Middleware de API Key (se aplica a todas las rutas excepto públicas)
app.use(validateApiKey);

// Rutas
app.use('/', emailRoutes);

// Middleware de manejo de errores
app.use((err, req, res) => {
    console.error('❌ Error no manejado:', err.stack);
    res.status(500).json({
        error: 'Error interno del servidor',
        message: err.message
    });
});

// Manejo de rutas no encontradas
app.use((req, res) => {
    res.status(404).json({
        error: 'Ruta no encontrada',
        message: `La ruta ${req.method} ${req.path} no existe`
    });
});

// Iniciar servidor
app.listen(PORT, () => {
    console.log('='.repeat(50));
    console.log('🚀 Notification Service iniciado');
    console.log(`📧 Puerto: ${PORT}`);
    console.log(`📬 Gmail configurado: ${process.env.GMAIL_USER}`);
    console.log('='.repeat(50));
});

// Manejo de errores no capturados
process.on('unhandledRejection', (reason) => {
    console.error('❌ Unhandled Rejection:', reason);
});

process.on('uncaughtException', (error) => {
    console.error('❌ Uncaught Exception:', error);
    process.exit(1);
});