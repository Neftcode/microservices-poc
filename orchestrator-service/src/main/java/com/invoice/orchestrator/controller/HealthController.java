package com.invoice.orchestrator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para health checks del servicio.
 * Permite verificar que el servicio está funcionando correctamente.
 * 
 * PATRON COMENTADO: En producción, este endpoint se usaría con
 * herramientas de monitoreo y orquestadores (Kubernetes, Docker Swarm)
 * para verificar la salud del servicio automáticamente.
 */
@RestController
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    /**
     * Endpoint de health check.
     * Retorna el estado del servicio.
     * 
     * @return Estado del servicio
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Verifica que el servicio está funcionando correctamente"
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "orchestrator-service");
        health.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(health);
    }
}