package com.invoice.orchestrator.service;

import com.invoice.orchestrator.model.dto.SaleRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * Cliente para comunicarse con el Microservicio C (Envío de Email).
 * Realiza llamadas HTTP asíncronas.
 */
@Service
public class EmailServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${services.email.url}")
    private String emailServiceUrl;
    
    @Value("${api.key.email-service}")
    private String emailServiceApiKey;

    public EmailServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Envía la factura por email de forma ASÍNCRONA.
     * El servicio de email procesa en segundo plano y responde inmediatamente con 202.
     * 
     * PATRON COMENTADO: En un sistema real, aquí se usaría un Message Queue (RabbitMQ, Kafka)
     * para garantizar la entrega del mensaje incluso si el servicio está caído.
     * 
     * @param saleRequest Datos de la venta
     * @param pdfBytes Bytes del PDF generado
     * @return true si se aceptó la petición (202), false en caso contrario
     */
    public boolean sendInvoiceEmail(SaleRequest saleRequest, byte[] pdfBytes) {
        try {
            String url = emailServiceUrl + "/send-invoice";
            
            // Preparar el payload con datos y PDF en base64
            Map<String, Object> payload = new HashMap<>();
            payload.put("customer", saleRequest.getCustomer());
            payload.put("products", saleRequest.getProducts());
            payload.put("pdfBase64", java.util.Base64.getEncoder().encodeToString(pdfBytes));
            
            // Configurar headers con API Key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", emailServiceApiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            System.out.println("📧 Enviando petición asíncrona al servicio de email...");
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );
            
            // HTTP 202 = Aceptado, se procesará en segundo plano
            if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                System.out.println("✅ Email aceptado para envío (procesamiento asíncrono)");
                return true;
            } else {
                System.out.println("⚠️ Respuesta inesperada del servicio de email: " + response.getStatusCode());
                return false;
            }
            
        } catch (Exception e) {
            // En comunicación asíncrona, un error no debe detener el flujo principal
            System.err.println("⚠️ Error al comunicarse con servicio de email (no crítico): " + e.getMessage());
            // PATRON: Aquí se podría implementar un Dead Letter Queue para reintentos
            return false;
        }
    }
}