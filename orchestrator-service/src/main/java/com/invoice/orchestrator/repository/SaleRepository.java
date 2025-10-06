package com.invoice.orchestrator.repository;

import com.invoice.orchestrator.model.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad Sale.
 * Proporciona operaciones CRUD sobre la tabla de ventas.
 * 
 * Spring Data JPA genera automáticamente la implementación.
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    // Spring Data JPA proporciona métodos como:
    // - save(Sale sale): Guarda una venta
    // - findById(Long id): Busca por ID
    // - findAll(): Obtiene todas las ventas
    // - delete(Sale sale): Elimina una venta
    
    // Aquí se podrían agregar queries personalizadas si fueran necesarias
    // Ejemplo: List<Sale> findByCustomerEmail(String email);
}