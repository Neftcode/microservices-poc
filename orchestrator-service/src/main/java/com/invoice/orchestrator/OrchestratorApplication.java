package com.invoice.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Clase principal del Microservicio Orquestador.
 * Punto de entrada de la aplicación Spring Boot.
 * 
 * @author Sistema de Facturación
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.invoice.orchestrator")
public class OrchestratorApplication {

    /**
     * Método main que inicia la aplicación Spring Boot.
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
        System.out.println("🚀 Orchestrator Service iniciado en puerto 8080");
    }

    /**
     * Bean de RestTemplate para realizar llamadas HTTP a otros microservicios.
     * 
     * PATRON COMENTADO: Aquí se podría configurar un Circuit Breaker (Resilience4j)
     * para manejar fallos en las llamadas a otros servicios.
     * 
     * PATRON COMENTADO: También se podría configurar un Retry Pattern para
     * reintentar llamadas fallidas automáticamente.
     * 
     * @return Instancia de RestTemplate configurada
     */
    @Bean
    public RestTemplate restTemplate() {
        // PATRON: Aquí se configurarían timeouts para evitar bloqueos indefinidos
        return new RestTemplate();
    }
    
    // CORS configuration moved to CorsConfig.java
}