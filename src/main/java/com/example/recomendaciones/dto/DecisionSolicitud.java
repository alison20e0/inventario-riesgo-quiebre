package com.example.recomendaciones.dto;

import jakarta.validation.constraints.NotBlank;

public record DecisionSolicitud(
        @NotBlank(message = "el usuario es obligatorio") String usuario,
        String motivo) {
}
