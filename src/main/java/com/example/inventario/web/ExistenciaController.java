package com.example.inventario.web;

import com.example.inventario.dto.ExistenciaResponse;
import com.example.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/existencias")
@RequiredArgsConstructor
public class ExistenciaController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<ExistenciaResponse>> listar() {
        return ResponseEntity.ok(inventarioService.listarExistencias());
    }
}
