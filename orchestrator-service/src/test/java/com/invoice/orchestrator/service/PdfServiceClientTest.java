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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de PdfServiceClient usando Mockito.
 *
 * Se mockea RestTemplate para no realizar llamadas HTTP reales.
 * Los campos @Value (pdfServiceUrl, pdfServiceApiKey) se inyectan
 * con ReflectionTestUtils ya que fuera de un contexto Spring los
 * @Value no se procesan automáticamente.
 */
@ExtendWith(MockitoExtension.class)
class PdfServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PdfServiceClient pdfServiceClient;

    private SaleRequest saleRequest;
    private byte[] fakePdfBytes;

    @BeforeEach
    void setUp() {
        // Inyectar los @Value manualmente en el contexto de test
        ReflectionTestUtils.setField(pdfServiceClient, "pdfServiceUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(pdfServiceClient, "pdfServiceApiKey", "test-pdf-key");

        CustomerInfo customer = new CustomerInfo(
                "Ana García", "987654321", "ana@test.com");
        ProductInfo product = new ProductInfo(
                "Monitor", new BigDecimal("350.00"), 2, new BigDecimal("700.00"));

        saleRequest = new SaleRequest(customer, List.of(product));
        fakePdfBytes = "%PDF-1.4 fake content".getBytes();
    }

    // -----------------------------------------------------------------------
    // generatePdf — casos exitosos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("generatePdf: servicio responde 200 con bytes → retorna el PDF")
    void generatePdf_whenServiceReturns200_shouldReturnPdfBytes() {
        ResponseEntity<byte[]> mockResponse = ResponseEntity.ok(fakePdfBytes);

        when(restTemplate.exchange(
                eq("http://localhost:8081/generate-pdf"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(mockResponse);

        byte[] result = pdfServiceClient.generatePdf(saleRequest);

        assertThat(result).isEqualTo(fakePdfBytes);
    }

    // -----------------------------------------------------------------------
    // generatePdf — casos de error
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("generatePdf: servicio responde 200 pero body nulo → lanza RuntimeException")
    void generatePdf_whenBodyIsNull_shouldThrowRuntimeException() {
        ResponseEntity<byte[]> emptyResponse = ResponseEntity.ok(null);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(emptyResponse);

        assertThatThrownBy(() -> pdfServiceClient.generatePdf(saleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al generar PDF");
    }

    @Test
    @DisplayName("generatePdf: RestTemplate lanza excepción → propaga RuntimeException")
    void generatePdf_whenRestTemplateThrows_shouldThrowRuntimeException() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> pdfServiceClient.generatePdf(saleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Connection refused");
    }
}
