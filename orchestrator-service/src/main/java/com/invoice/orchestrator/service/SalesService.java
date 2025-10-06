package com.invoice.orchestrator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoice.orchestrator.model.dto.SaleRequest;
import com.invoice.orchestrator.model.entity.Sale;
import com.invoice.orchestrator.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * Servicio principal que orquesta la creación de ventas.
 * Coordina las operaciones de:
 * 1. Persistencia en base de datos
 * 2. Generación de PDF (síncrona)
 * 3. Envío de email (asíncrona)
 */
@Service
public class SalesService {

    private final SaleRepository saleRepository;
    private final PdfServiceClient pdfServiceClient;
    private final EmailServiceClient emailServiceClient;
    private final ObjectMapper objectMapper;

    public SalesService(SaleRepository saleRepository,
                       PdfServiceClient pdfServiceClient,
                       EmailServiceClient emailServiceClient) {
        this.saleRepository = saleRepository;
        this.pdfServiceClient = pdfServiceClient;
        this.emailServiceClient = emailServiceClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Busca una venta por su ID.
     * 
     * @param id ID de la venta a buscar
     * @return Optional con la venta si existe, vacío si no
     */
    public Sale findSaleById(Long id) {
        return saleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    /**
     * Procesa una nueva venta siguiendo el flujo completo:
     * 1. Guarda la venta en base de datos
     * 2. Genera el PDF (comunicación síncrona)
     * 3. Envía email con la factura (comunicación asíncrona)
     * 
     * PATRON COMENTADO: Este método actúa como Saga Orchestrator, coordinando
     * múltiples operaciones. En producción se implementaría compensación
     * en caso de fallos parciales.
     * 
     * @param saleRequest Datos de la venta a procesar
     * @return Bytes del PDF generado
     * @throws RuntimeException Si hay error en el proceso
     */
    @Transactional
    public byte[] processSale(SaleRequest saleRequest) {
        System.out.println("🎯 Iniciando procesamiento de venta...");
        
        try {
            // Paso 1: Guardar en base de datos
            Sale sale = saveSale(saleRequest);
            System.out.println("💾 Venta guardada en BD con ID: " + sale.getId());
            
            // Paso 2: Generar PDF (SÍNCRONO - esperamos respuesta)
            byte[] pdfBytes = pdfServiceClient.generatePdf(saleRequest);
            
            // Paso 3: Enviar email (ASÍNCRONO - no esperamos que termine)
            // Si falla el email, no afecta el flujo principal
            boolean emailAccepted = emailServiceClient.sendInvoiceEmail(saleRequest, pdfBytes);
            if (emailAccepted) {
                System.out.println("📬 Email aceptado para envío en background");
            } else {
                System.out.println("⚠️ Email no pudo ser enviado (operación no crítica)");
            }
            
            System.out.println("✅ Venta procesada exitosamente");
            return pdfBytes;
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar venta: " + e.getMessage());
            // PATRON: Aquí se implementaría compensación (rollback de operaciones)
            throw new RuntimeException("Error al procesar la venta: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda una venta en la base de datos.
     * Serializa los productos a JSON para almacenamiento.
     * 
     * @param saleRequest Datos de la venta
     * @return Entidad Sale guardada
     * @throws RuntimeException Si hay error al guardar
     */
    private Sale saveSale(SaleRequest saleRequest) {
        try {
            // Calcular total
            BigDecimal totalAmount = saleRequest.getProducts().stream()
                .map(p -> p.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Serializar productos a JSON
            String productsJson = objectMapper.writeValueAsString(saleRequest.getProducts());
            
            // Crear entidad
            Sale sale = new Sale(
                saleRequest.getCustomer().getName(),
                saleRequest.getCustomer().getIdentification(),
                saleRequest.getCustomer().getEmail(),
                totalAmount,
                productsJson
            );
            
            // Guardar en BD
            Sale savedSale = saleRepository.save(sale);
            return savedSale;
            
        } catch (Exception e) {
            System.err.println("❌ Error al guardar venta: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al guardar venta: " + e.getMessage(), e);
        }
    }
}