package com.example.recomendaciones.web;

import com.example.recomendaciones.dto.RecomendacionResponse;
import com.example.recomendaciones.service.RecomendacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recomendaciones")
@RequiredArgsConstructor
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    @GetMapping
    public ResponseEntity<List<RecomendacionResponse>> listar() {
        return ResponseEntity.ok(recomendacionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecomendacionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(recomendacionService.obtener(id));
    }

    @PostMapping("/generar")
    public ResponseEntity<List<RecomendacionResponse>> generar() {
        return ResponseEntity.status(HttpStatus.CREATED).body(recomendacionService.generar());
    }

    @PostMapping
    public ResponseEntity<List<RecomendacionResponse>> generarEndpointAnterior() {
        return ResponseEntity.status(HttpStatus.CREATED).body(recomendacionService.generar());
    }
}
