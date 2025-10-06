package com.invoice.orchestrator.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una venta en la base de datos.
 * Almacena información de ventas realizadas con sus productos.
 */
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "products", nullable = false, columnDefinition = "TEXT")
    private String products; // JSON serializado de los productos

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Sale() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor con todos los campos.
     * 
     * @param customerName Nombre del cliente
     * @param customerId Identificación del cliente
     * @param customerEmail Email del cliente
     * @param totalAmount Monto total de la venta
     * @param products JSON con los productos
     */
    public Sale(String customerName, String customerId, String customerEmail, 
                BigDecimal totalAmount, String products) {
        this.customerName = customerName;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.products = products;
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}