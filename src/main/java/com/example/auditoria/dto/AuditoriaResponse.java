package com.example.auditoria.dto;

import java.time.LocalDateTime;

public record AuditoriaResponse(
        Long id,
        String entidad,
        String accion,
        String usuario,
        String motivo,
        Long referenciaId,
        LocalDateTime fecha) {
}
