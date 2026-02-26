package com.invoice.orchestrator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Tests de integración del Orquestador Service.
 * Verifica que el contexto de Spring Boot se inicialice correctamente.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:sqlite:./data/test-sales.db",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrchestratorApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring Boot carga sin errores
    }
}
