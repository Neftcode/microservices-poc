/**
 * Tests básicos del Notification Service.
 * Verifica que los endpoints principales responden correctamente.
 */

const request = require('supertest');

// Mock de las dependencias externas antes de cargar la app
jest.mock('nodemailer', () => ({
    createTransport: jest.fn().mockReturnValue({
        sendMail: jest.fn().mockResolvedValue({ messageId: 'test-id' }),
        verify: jest.fn().mockImplementation((cb) => cb(null, true))
    })
}));

// Cargar la app de forma aislada para tests
const express = require('express');
const cors = require('cors');
const emailRoutes = require('../routes/email.routes');
const { validateApiKey } = require('../middleware/apiKey.middleware');

const app = express();
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(validateApiKey);
app.use('/', emailRoutes);

describe('Notification Service - Health Check', () => {
    test('GET /health debería retornar status 200 con estado UP', async () => {
        const response = await request(app).get('/health');

        expect(response.status).toBe(200);
        expect(response.body.status).toBe('UP');
        expect(response.body.service).toBe('notification-service');
    });
});

describe('Notification Service - Seguridad', () => {
    test('POST /send-invoice sin API Key debería retornar 401', async () => {
        const response = await request(app)
            .post('/send-invoice')
            .send({});

        expect(response.status).toBe(401);
    });
});
