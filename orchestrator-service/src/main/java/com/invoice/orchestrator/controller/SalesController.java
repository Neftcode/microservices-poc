package com.invoice.orchestrator.controller;

import com.invoice.orchestrator.model.dto.SaleRequest;
import com.invoice.orchestrator.service.SalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoice.orchestrator.model.entity.Sale;
import java.util.Map;

/**
 * Controlador REST para gestión de ventas.
 * Expone endpoints para crear ventas y generar facturas.
 */
@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales", description = "API para gestión de ventas y facturas")
public class SalesController {

    private final SalesService salesService;
    private final ObjectMapper objectMapper;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Endpoint principal para crear una venta y generar factura.
     * 
     * Flujo:
     * 1. Valida los datos recibidos
     * 2. Guarda la venta en base de datos
     * 3. Genera PDF (síncrono)
     * 4. Envía email (asíncrono)
     * 5. Retorna el PDF generado
     * 
     * @param saleRequest Datos de la venta (validados)
     * @return ResponseEntity con el PDF como bytes
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, 
                 produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
        summary = "Crear venta y generar factura",
        description = "Procesa una nueva venta, genera la factura en PDF y envía email al cliente"
    )
    @ApiResponse(
        responseCode = "200",
        description = "PDF generado exitosamente",
        content = @Content(mediaType = "application/pdf")
    )
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public ResponseEntity<byte[]> createSale(@Valid @RequestBody SaleRequest saleRequest) {
        
        System.out.println("📥 Recibida petición de venta para: " + 
                          saleRequest.getCustomer().getEmail());
        
        // Procesar la venta (orquestación)
        byte[] pdfBytes = salesService.processSale(saleRequest);
        
        // Configurar headers de respuesta
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura.pdf\"");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        headers.setContentLength(pdfBytes.length);
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Endpoint para consultar una venta por su ID.
     * 
     * @param id ID de la venta a consultar
     * @return ResponseEntity con los datos de la venta
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Consultar venta por ID",
        description = "Obtiene los detalles de una venta específica por su ID"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Venta encontrada",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
    )
    @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id) {
        try {
            Sale sale = salesService.findSaleById(id);
            return ResponseEntity.ok(sale);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}