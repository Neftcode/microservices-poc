package com.invoice.orchestrator.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO que representa la información del cliente.
 */
public class CustomerInfo {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String name;

    @NotBlank(message = "La identificación del cliente es obligatoria")
    private String identification;

    @NotBlank(message = "El email del cliente es obligatorio")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "El email debe tener un formato válido"
    )
    private String email;

    public CustomerInfo() {}

    public CustomerInfo(String name, String identification, String email) {
        this.name = name;
        this.identification = identification;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}