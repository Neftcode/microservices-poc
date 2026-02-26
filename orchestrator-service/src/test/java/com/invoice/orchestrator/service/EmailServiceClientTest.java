package com.invoice.orchestrator.service;

import com.invoice.orchestrator.model.dto.CustomerInfo;
import com.invoice.orchestrator.model.dto.ProductInfo;
import com.invoice.orchestrator.model.dto.SaleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de EmailServiceClient usando Mockito.
 *
 * Se mockea RestTemplate para no realizar llamadas HTTP reales.
 * El cliente usa comunicación ASÍNCRONA (HTTP 202 Accepted):
 *   - Un fallo NO lanza excepción, solo retorna false.
 *   - Un HTTP 202 retorna true.
 *   - Cualquier otro código retorna false.
 *
 * Los campos @Value se inyectan con ReflectionTestUtils.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmailServiceClient emailServiceClient;

    private SaleRequest saleRequest;
    private byte[] fakePdfBytes;

    @BeforeEach
    void setUp() {
        // Inyectar los @Value manualmente en el contexto de test
        ReflectionTestUtils.setField(emailServiceClient, "emailServiceUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(emailServiceClient, "emailServiceApiKey", "test-email-key");

        CustomerInfo customer = new CustomerInfo(
                "Pedro López", "111222333", "pedro@test.com");
        ProductInfo product = new ProductInfo(
                "Teclado", new BigDecimal("75.00"), 3, new BigDecimal("225.00"));

        saleRequest = new SaleRequest(customer, List.of(product));
        fakePdfBytes = "%PDF-1.4 fake invoice".getBytes();
    }

    // -----------------------------------------------------------------------
    // sendInvoiceEmail — casos exitosos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("sendInvoiceEmail: servicio responde 202 Accepted → retorna true")
    void sendInvoiceEmail_when202Accepted_shouldReturnTrue() {
        ResponseEntity<String> accepted = ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body("{\"message\":\"procesando\"}");

        when(restTemplate.exchange(
                eq("http://localhost:8082/send-invoice"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(accepted);

        boolean result = emailServiceClient.sendInvoiceEmail(saleRequest, fakePdfBytes);

        assertThat(result).isTrue();
    }

    // -----------------------------------------------------------------------
    // sendInvoiceEmail — casos donde retorna false (sin lanzar excepción)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("sendInvoiceEmail: servicio responde 200 OK (no 202) → retorna false")
    void sendInvoiceEmail_whenUnexpectedStatus200_shouldReturnFalse() {
        ResponseEntity<String> okResponse = ResponseEntity.ok("ok");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(okResponse);

        boolean result = emailServiceClient.sendInvoiceEmail(saleRequest, fakePdfBytes);

        // El contrato asíncrono exige 202; cualquier otro status es inesperado
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("sendInvoiceEmail: RestTemplate lanza excepción → retorna false (no propaga error)")
    void sendInvoiceEmail_whenRestTemplateThrows_shouldReturnFalseNotThrow() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RestClientException("notification-service caído"));

        // El fallo de email es no-crítico: nunca debe interrumpir el flujo principal
        boolean result = emailServiceClient.sendInvoiceEmail(saleRequest, fakePdfBytes);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("sendInvoiceEmail: servicio responde 500 → retorna false")
    void sendInvoiceEmail_when500InternalServerError_shouldReturnFalse() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RestClientException("500 Internal Server Error"));

        boolean result = emailServiceClient.sendInvoiceEmail(saleRequest, fakePdfBytes);

        assertThat(result).isFalse();
    }
}
