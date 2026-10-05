package com.example.auditoria.web;

import com.example.auditoria.dto.AuditoriaResponse;
import com.example.auditoria.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<AuditoriaResponse>> listar() {
        return ResponseEntity.ok(auditoriaService.listar());
    }

    @GetMapping("/{referenciaId}")
    public ResponseEntity<List<AuditoriaResponse>> listarPorReferencia(@PathVariable Long referenciaId) {
        return ResponseEntity.ok(auditoriaService.listarPorReferencia("RECOMENDACION", referenciaId));
    }
}
