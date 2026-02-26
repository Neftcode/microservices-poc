package com.invoice.orchestrator.service;

import com.invoice.orchestrator.model.dto.CustomerInfo;
import com.invoice.orchestrator.model.dto.ProductInfo;
import com.invoice.orchestrator.model.dto.SaleRequest;
import com.invoice.orchestrator.model.entity.Sale;
import com.invoice.orchestrator.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de SalesService usando Mockito.
 *
 * Se mockean las tres dependencias externas del servicio:
 *   - SaleRepository      → persistencia (SQLite)
 *   - PdfServiceClient    → llamada HTTP síncrona al pdf-service
 *   - EmailServiceClient  → llamada HTTP asíncrona al notification-service
 *
 * De esta forma los tests corren sin levantar contexto de Spring ni
 * necesitar servicios externos disponibles.
 */
@ExtendWith(MockitoExtension.class)
class SalesServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PdfServiceClient pdfServiceClient;

    @Mock
    private EmailServiceClient emailServiceClient;

    @InjectMocks
    private SalesService salesService;

    // -----------------------------------------------------------------------
    // Datos de prueba reutilizables
    // -----------------------------------------------------------------------
    private SaleRequest saleRequest;
    private Sale savedSale;
    private byte[] fakePdfBytes;

    @BeforeEach
    void setUp() {
        CustomerInfo customer = new CustomerInfo(
                "Juan Pérez", "123456789", "juan@test.com");

        ProductInfo product = new ProductInfo(
                "Laptop", new BigDecimal("999.99"), 1, new BigDecimal("999.99"));

        saleRequest = new SaleRequest(customer, List.of(product));

        savedSale = new Sale(
                "Juan Pérez", "123456789", "juan@test.com",
                new BigDecimal("999.99"), "[{\"name\":\"Laptop\"}]");
        savedSale.setId(1L);

        fakePdfBytes = "PDF_CONTENT".getBytes();
    }

    // -----------------------------------------------------------------------
    // processSale — flujo completo exitoso
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("processSale: happy path → guarda venta, genera PDF y acepta email")
    void processSale_happyPath_shouldSaveGeneratePdfAndSendEmail() {
        when(saleRepository.save(any(Sale.class))).thenReturn(savedSale);
        when(pdfServiceClient.generatePdf(saleRequest)).thenReturn(fakePdfBytes);
        when(emailServiceClient.sendInvoiceEmail(saleRequest, fakePdfBytes)).thenReturn(true);

        byte[] result = salesService.processSale(saleRequest);

        assertThat(result).isEqualTo(fakePdfBytes);

        verify(saleRepository, times(1)).save(any(Sale.class));
        verify(pdfServiceClient, times(1)).generatePdf(saleRequest);
        verify(emailServiceClient, times(1)).sendInvoiceEmail(saleRequest, fakePdfBytes);
    }

    @Test
    @DisplayName("processSale: fallo no-crítico del email → aún retorna el PDF")
    void processSale_whenEmailFails_shouldStillReturnPdf() {
        when(saleRepository.save(any(Sale.class))).thenReturn(savedSale);
        when(pdfServiceClient.generatePdf(saleRequest)).thenReturn(fakePdfBytes);
        // El email client devuelve false (operación asíncrona fallida, no crítica)
        when(emailServiceClient.sendInvoiceEmail(saleRequest, fakePdfBytes)).thenReturn(false);

        byte[] result = salesService.processSale(saleRequest);

        // El flujo no se interrumpe; el PDF sigue siendo retornado
        assertThat(result).isEqualTo(fakePdfBytes);
        verify(pdfServiceClient, times(1)).generatePdf(saleRequest);
    }

    @Test
    @DisplayName("processSale: fallo en PDF Service → lanza RuntimeException")
    void processSale_whenPdfServiceFails_shouldThrowRuntimeException() {
        when(saleRepository.save(any(Sale.class))).thenReturn(savedSale);
        when(pdfServiceClient.generatePdf(saleRequest))
                .thenThrow(new RuntimeException("PDF service no disponible"));

        assertThatThrownBy(() -> salesService.processSale(saleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PDF service no disponible");

        // Si el PDF falla, nunca se intenta enviar el email
        verify(emailServiceClient, never()).sendInvoiceEmail(any(), any());
    }

    @Test
    @DisplayName("processSale: fallo al guardar en BD → lanza RuntimeException")
    void processSale_whenRepositoryFails_shouldThrowRuntimeException() {
        when(saleRepository.save(any(Sale.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        assertThatThrownBy(() -> salesService.processSale(saleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de base de datos");

        // Si la BD falla, no se llama a ningún servicio externo
        verify(pdfServiceClient, never()).generatePdf(any());
        verify(emailServiceClient, never()).sendInvoiceEmail(any(), any());
    }

    // -----------------------------------------------------------------------
    // findSaleById
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findSaleById: venta existente → retorna la entidad Sale")
    void findSaleById_whenExists_shouldReturnSale() {
        when(saleRepository.findById(1L)).thenReturn(Optional.of(savedSale));

        Sale result = salesService.findSaleById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerName()).isEqualTo("Juan Pérez");
    }

    @Test
    @DisplayName("findSaleById: venta no encontrada → lanza RuntimeException")
    void findSaleById_whenNotFound_shouldThrowRuntimeException() {
        when(saleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salesService.findSaleById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Venta no encontrada con ID: 99");
    }
}
