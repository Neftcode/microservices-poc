package com.invoice.orchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración general de la aplicación.
 * Define beans de infraestructura usados por los clientes HTTP.
 */
@Configuration
public class AppConfig {

    /**
     * Bean de RestTemplate para comunicación HTTP entre microservicios.
     * Usado por PdfServiceClient y EmailServiceClient.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
