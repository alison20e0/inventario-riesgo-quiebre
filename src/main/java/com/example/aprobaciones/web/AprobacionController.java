package com.example.aprobaciones.web;

import com.example.aprobaciones.service.AprobacionService;
import com.example.recomendaciones.dto.DecisionSolicitud;
import com.example.recomendaciones.dto.RecomendacionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recomendaciones")
@RequiredArgsConstructor
public class AprobacionController {

    private final AprobacionService aprobacionService;

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<RecomendacionResponse> aprobar(@PathVariable Long id,
                                                        @Valid @RequestBody DecisionSolicitud solicitud) {
        return ResponseEntity.ok(aprobacionService.aprobar(id, solicitud.usuario(), solicitud.motivo()));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<RecomendacionResponse> rechazar(@PathVariable Long id,
                                                         @Valid @RequestBody DecisionSolicitud solicitud) {
        return ResponseEntity.ok(aprobacionService.rechazar(id, solicitud.usuario(), solicitud.motivo()));
    }
}
