package com.example.service;

import com.example.model.Inventario;
import com.example.model.Recomendacion;
import com.example.repository.InventarioRepository;
import com.example.repository.RecomendacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecomendacionService {
    
    private final InventarioRepository inventarioRepository;
    private final RecomendacionRepository recomendacionRepository;
    
    public RecomendacionService(InventarioRepository inventarioRepository, 
                              RecomendacionRepository recomendacionRepository) {
        this.inventarioRepository = inventarioRepository;
        this.recomendacionRepository = recomendacionRepository;
    }
    
    @Transactional
    public List<Recomendacion> generarRecomendaciones() {
        List<Inventario> inventarios = inventarioRepository.findAll();
        List<Recomendacion> recomendacionesGeneradas = inventarios.stream()
                .filter(this::tieneRiesgoQuiebre)
                .map(this::crearRecomendacion)
                .toList();
        
        return recomendacionRepository.saveAll(recomendacionesGeneradas);
    }
    
    private boolean tieneRiesgoQuiebre(Inventario inventario) {
        return inventario.getCantidadActual() <= inventario.getStockMinimo();
    }
    
    private Recomendacion crearRecomendacion(Inventario inventario) {
        Integer cantidadSugerida = 0;
        if (inventario.getStockMaximo() != null && inventario.getStockMinimo() != null) {
            cantidadSugerida = inventario.getStockMaximo() - inventario.getCantidadActual();
        }
        if (cantidadSugerida <= 0 && inventario.getStockMinimo() != null) {
            cantidadSugerida = inventario.getStockMinimo() * 2;
        }
        
        return Recomendacion.builder()
                .inventario(inventario)
                .tipoRecomendacion("REABASTECIMIENTO")
                .descripcion(String.format("Riesgo de quiebre detectado. Stock actual: %d, Stock mínimo: %d",
                        inventario.getCantidadActual(), inventario.getStockMinimo()))
                .cantidadSugerida(cantidadSugerida)
                .aprobada(false)
                .build();
    }
    
    @Transactional
    public Optional<Recomendacion> aprobarRecomendacion(Long id) {
        return recomendacionRepository.findById(id)
                .map(recomendacion -> {
                    recomendacion.setAprobada(true);
                    recomendacion.setFechaAprobacion(java.time.LocalDateTime.now());
                    return recomendacionRepository.save(recomendacion);
                });
    }
    
    public List<Recomendacion> listarTodas() {
        return recomendacionRepository.findAll();
    }
    
    public Optional<Recomendacion> obtenerPorId(Long id) {
        return recomendacionRepository.findById(id);
    }
}
