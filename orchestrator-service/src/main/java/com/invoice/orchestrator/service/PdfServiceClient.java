package com.invoice.orchestrator.service;

import com.invoice.orchestrator.model.dto.SaleRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente para comunicarse con el Microservicio B (Generación de PDF).
 * Realiza llamadas HTTP síncronas.
 */
@Service
public class PdfServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${services.pdf.url}")
    private String pdfServiceUrl;
    
    @Value("${api.key.pdf-service}")
    private String pdfServiceApiKey;

    public PdfServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Genera un PDF invocando al microservicio de generación de PDF.
     * Comunicación SÍNCRONA: espera la respuesta completa.
     * 
     * PATRON COMENTADO: Aquí se implementaría un Circuit Breaker para manejar
     * fallos del servicio de PDF sin afectar toda la aplicación.
     * 
     * PATRON COMENTADO: Se podría implementar un Retry Pattern con backoff
     * exponencial para reintentar en caso de fallos transitorios.
     * 
     * @param saleRequest Datos de la venta
     * @return Bytes del PDF generado
     * @throws RuntimeException Si hay error al generar el PDF
     */
    public byte[] generatePdf(SaleRequest saleRequest) {
        try {
            String url = pdfServiceUrl + "/generate-pdf";
            
            // Configurar headers con API Key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", pdfServiceApiKey);
            
            HttpEntity<SaleRequest> request = new HttpEntity<>(saleRequest, headers);
            
            // PATRON: Aquí se configuraría un timeout para evitar esperas indefinidas
            System.out.println("📄 Llamando al servicio de PDF...");
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                byte[].class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("✅ PDF generado exitosamente");
                return response.getBody();
            } else {
                throw new RuntimeException("Error al generar PDF: respuesta vacía");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al comunicarse con servicio de PDF: " + e.getMessage());
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }
}