package com.example.controller;

import com.example.model.Recomendacion;
import com.example.service.RecomendacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recomendaciones")
public class RecomendacionController {
    
    private final RecomendacionService recomendacionService;
    
    public RecomendacionController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }
    
    @GetMapping
    public ResponseEntity<List<Recomendacion>> listarRecomendaciones() {
        List<Recomendacion> recomendaciones = recomendacionService.listarTodas();
        return ResponseEntity.ok(recomendaciones);
    }
    
    @PostMapping
    public ResponseEntity<List<Recomendacion>> generarRecomendaciones() {
        List<Recomendacion> recomendaciones = recomendacionService.generarRecomendaciones();
        return ResponseEntity.status(HttpStatus.CREATED).body(recomendaciones);
    }
    
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Recomendacion> aprobarRecomendacion(@PathVariable Long id) {
        return recomendacionService.aprobarRecomendacion(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
