package com.invoice.orchestrator.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO que representa la petición de creación de una venta.
 * Contiene información del cliente y lista de productos.
 */
public class SaleRequest {

    @NotNull(message = "La información del cliente es obligatoria")
    @Valid
    private CustomerInfo customer;

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<ProductInfo> products;

    public SaleRequest() {}

    public SaleRequest(CustomerInfo customer, List<ProductInfo> products) {
        this.customer = customer;
        this.products = products;
    }

    public CustomerInfo getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerInfo customer) {
        this.customer = customer;
    }

    public List<ProductInfo> getProducts() {
        return products;
    }

    public void setProducts(List<ProductInfo> products) {
        this.products = products;
    }
}