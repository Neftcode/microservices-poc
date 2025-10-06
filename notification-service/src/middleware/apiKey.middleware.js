/**
 * Middleware de seguridad para validación de API Key.
 * Intercepta las peticiones y valida la autenticación.
 */

/**
 * Middleware que valida el API Key en las peticiones.
 * 
 * PATRON COMENTADO: En producción se usaría OAuth2/JWT para
 * autenticación más robusta. El API Key es para demostración.
 * 
 * @param {Object} req - Request de Express
 * @param {Object} res - Response de Express
 * @param {Function} next - Siguiente middleware
 */
function validateApiKey(req, res, next) {
    // Rutas públicas que no requieren API Key
    const publicPaths = ['/health'];
    
    if (publicPaths.includes(req.path)) {
        return next();
    }

    // Obtener API Key del header
    const apiKey = req.headers['x-api-key'];
    const validApiKey = process.env.EMAIL_SERVICE_API_KEY || 'default-email-key';

    // Validar presencia del API Key
    if (!apiKey) {
        return res.status(401).json({
            error: 'API Key requerido',
            message: 'Debe incluir el header X-API-Key'
        });
    }

    // Validar API Key correcto
    if (apiKey !== validApiKey) {
        return res.status(403).json({
            error: 'API Key inválido',
            message: 'El API Key proporcionado no es válido'
        });
    }

    // API Key válido, continuar
    next();
}

module.exports = { validateApiKey };