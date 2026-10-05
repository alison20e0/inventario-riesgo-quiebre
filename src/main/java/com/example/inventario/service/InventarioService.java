package com.example.inventario.service;

import com.example.inventario.dto.ExistenciaResponse;
import com.example.inventario.model.Existencia;
import com.example.inventario.repository.ExistenciaRepository;
import com.example.pronostico.PronosticoResultado;
import com.example.pronostico.PronosticoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ExistenciaRepository existenciaRepository;
    private final PronosticoService pronosticoService;

    @Transactional(readOnly = true)
    public List<ExistenciaResponse> listarExistencias() {
        return existenciaRepository.findAllByOrderByBodega_CodigoAscProducto_SkuAsc()
                .stream()
                .map(this::mapear)
                .toList();
    }

    private ExistenciaResponse mapear(Existencia existencia) {
        PronosticoResultado pronostico = pronosticoService.evaluar(existencia);
        return new ExistenciaResponse(
                existencia.getId(),
                existencia.getBodega().getId(),
                existencia.getBodega().getCodigo(),
                existencia.getBodega().getNombre(),
                existencia.getProducto().getId(),
                existencia.getProducto().getSku(),
                existencia.getProducto().getNombre(),
                existencia.getProducto().getStockMinimo(),
                existencia.getDisponible(),
                existencia.getReservado(),
                existencia.disponibleLibre(),
                pronostico.consumoDiario(),
                pronostico.diasCobertura(),
                pronostico.riesgo(),
                pronostico.origenCalculo(),
                existencia.getVersion());
    }
}
