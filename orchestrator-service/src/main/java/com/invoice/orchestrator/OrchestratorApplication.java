package com.invoice.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
}