package com.invoice.orchestrator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para documentación de la API.
 * 
 * Swagger UI estará disponible en: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON en: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configura la documentación OpenAPI de la aplicación.
     * 
     * @return Objeto OpenAPI configurado
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orchestrator Service API")
                        .version("1.0.0")
                        .description("API del microservicio orquestador para sistema de facturación electrónica")
                        .contact(new Contact()
                                .name("Sistema de Facturación")
                                .email("support@invoice-system.com")))
                .components(new Components()
                        .addSecuritySchemes("ApiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")))
                .addSecurityItem(new SecurityRequirement().addList("ApiKey"));
    }
}