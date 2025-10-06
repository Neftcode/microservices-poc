package com.invoice.orchestrator.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Filtro de seguridad que valida el API Key en las peticiones entrantes.
 * 
 * Este filtro intercepta todas las peticiones y verifica que incluyan
 * un API Key válido en el header X-API-Key.
 * 
 * PATRON COMENTADO: En un sistema real, aquí se implementaría OAuth2/JWT
 * para autenticación más robusta. El API Key es solo para demostración.
 */
@Component
@Order(1)
public class ApiKeyFilter implements Filter {

    @Value("${api.key.orchestrator}")
    private String validApiKey;

    /**
     * Valida el API Key en cada petición.
     * 
     * Excepciones (no requieren API Key):
     * - /health (health check público)
     * - /swagger-ui/** (documentación)
     * - /v3/api-docs/** (OpenAPI spec)
     * 
     * @param request Petición HTTP
     * @param response Respuesta HTTP
     * @param chain Cadena de filtros
     * @throws IOException Si hay error de I/O
     * @throws ServletException Si hay error en el servlet
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        // Permitir peticiones OPTIONS para CORS
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Rutas públicas que no requieren API Key
        if (path.equals("/health") || 
            path.startsWith("/swagger-ui") || 
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-resources") ||
            path.equals("/favicon.ico")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Validar API Key
        String apiKey = httpRequest.getHeader("X-API-Key");
        
        if (apiKey == null || apiKey.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\": \"API Key requerido\", \"message\": \"Debe incluir el header X-API-Key\"}"
            );
            return;
        }
        
        if (!apiKey.equals(validApiKey)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\": \"API Key inválido\", \"message\": \"El API Key proporcionado no es válido\"}"
            );
            return;
        }
        
        // API Key válido, continuar con la petición
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inicialización del filtro (si fuera necesaria)
    }

    @Override
    public void destroy() {
        // Limpieza al destruir el filtro (si fuera necesaria)
    }
}