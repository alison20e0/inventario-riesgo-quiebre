package com.example.recomendaciones.service;

import com.example.inventario.model.Existencia;
import com.example.inventario.model.Producto;
import com.example.inventario.repository.ExistenciaRepository;
import com.example.pronostico.NivelRiesgo;
import com.example.pronostico.OrigenCalculo;
import com.example.pronostico.PronosticoResultado;
import com.example.pronostico.PronosticoService;
import com.example.recomendaciones.dto.RecomendacionResponse;
import com.example.recomendaciones.model.EstadoRecomendacion;
import com.example.recomendaciones.model.Recomendacion;
import com.example.recomendaciones.model.TipoRecomendacion;
import com.example.recomendaciones.repository.RecomendacionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecomendacionService {

    private static final Logger log = LoggerFactory.getLogger(RecomendacionService.class);

    private static final int MULTIPLICADOR_STOCK_OBJETIVO = 2;

    private final RecomendacionRepository recomendacionRepository;
    private final ExistenciaRepository existenciaRepository;
    private final PronosticoService pronosticoService;
    private final RecomendacionMapper mapper;

    @Transactional
    public List<RecomendacionResponse> generar() {
        List<Existencia> existencias = existenciaRepository.findAllByOrderByBodega_CodigoAscProducto_SkuAsc();
        List<Recomendacion> nuevas = new ArrayList<>();
        for (Existencia existencia : existencias) {
            PronosticoResultado pronostico = pronosticoService.evaluar(existencia);
            if (pronostico.riesgo() != NivelRiesgo.ALTO) {
                log.debug("Sin riesgo alto en {} / {}.", existencia.getBodega().getCodigo(),
                        existencia.getProducto().getSku());
                continue;
            }
            Recomendacion recomendacion = construir(existencia, pronostico);
            if (yaExistePendiente(recomendacion)) {
                log.info("Ya existe una recomendacion {} pendiente para {} en {}.",
                        recomendacion.getTipo(), recomendacion.getProducto().getSku(),
                        recomendacion.getBodegaDestino().getCodigo());
                continue;
            }
            nuevas.add(recomendacion);
        }
        List<RecomendacionResponse> generadas = recomendacionRepository.saveAll(nuevas).stream()
                .map(mapper::toResponse)
                .toList();
        log.info("Generacion de recomendaciones terminada: {} nuevas de {} existencias evaluadas.",
                generadas.size(), existencias.size());
        return generadas;
    }

    @Transactional(readOnly = true)
    public List<RecomendacionResponse> listar() {
        return recomendacionRepository.findAllByOrderByFechaCreacionDescIdDesc().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecomendacionResponse obtener(Long id) {
        return mapper.toResponse(cargarEntidad(id));
    }

    @Transactional(readOnly = true)
    public Recomendacion cargarEntidad(Long id) {
        return recomendacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe la recomendacion " + id));
    }

    @Transactional
    public Recomendacion actualizar(Recomendacion recomendacion) {
        return recomendacionRepository.save(recomendacion);
    }

    private Recomendacion construir(Existencia existencia, PronosticoResultado pronostico) {
        Optional<Existencia> conExcedente = buscarBodegaConExcedente(existencia);
        return conExcedente
                .map(origen -> transferencia(existencia, pronostico, origen))
                .orElseGet(() -> compra(existencia, pronostico));
    }

    private Optional<Existencia> buscarBodegaConExcedente(Existencia existencia) {
        return existenciaRepository
                .findByProducto_IdAndBodega_IdNotOrderByDisponibleDesc(
                        existencia.getProducto().getId(), existencia.getBodega().getId())
                .stream()
                .filter(candidata -> excedente(candidata) > 0)
                .findFirst();
    }

    private Recomendacion transferencia(Existencia destino, PronosticoResultado pronostico, Existencia origen) {
        int excedente = excedente(origen);
        int cantidad = Math.min(necesario(destino), excedente);
        return Recomendacion.builder()
                .tipo(TipoRecomendacion.TRANSFERENCIA)
                .estado(EstadoRecomendacion.PENDIENTE_APROBACION)
                .riesgo(pronostico.riesgo())
                .origenCalculo(pronostico.origenCalculo())
                .producto(destino.getProducto())
                .bodegaOrigen(origen.getBodega())
                .bodegaDestino(destino.getBodega())
                .cantidadSugerida(cantidad)
                .diasCobertura(pronostico.diasCobertura())
                .descripcion(String.format(
                        "Riesgo %s en %s (%s). Excedente de %d unidades en %s: transfiere %d unidades.",
                        pronostico.riesgo(), destino.getBodega().getCodigo(),
                        detalleCobertura(pronostico, destino.getProducto()),
                        excedente, origen.getBodega().getCodigo(), cantidad))
                .build();
    }

    private Recomendacion compra(Existencia destino, PronosticoResultado pronostico) {
        Producto producto = destino.getProducto();
        int cantidad = Math.max(1, producto.getStockMinimo() * MULTIPLICADOR_STOCK_OBJETIVO - destino.getDisponible());
        return Recomendacion.builder()
                .tipo(TipoRecomendacion.COMPRA)
                .estado(EstadoRecomendacion.PENDIENTE_APROBACION)
                .riesgo(pronostico.riesgo())
                .origenCalculo(pronostico.origenCalculo())
                .producto(producto)
                .bodegaOrigen(null)
                .bodegaDestino(destino.getBodega())
                .cantidadSugerida(cantidad)
                .diasCobertura(pronostico.diasCobertura())
                .descripcion(String.format(
                        "Riesgo %s en %s (%s). Ninguna bodega tiene excedente de %s: compra %d unidades.",
                        pronostico.riesgo(), destino.getBodega().getCodigo(),
                        detalleCobertura(pronostico, destino.getProducto()),
                        producto.getSku(), cantidad))
                .build();
    }

    private boolean yaExistePendiente(Recomendacion recomendacion) {
        return recomendacionRepository.existsByProducto_IdAndBodegaDestino_IdAndTipoAndEstado(
                recomendacion.getProducto().getId(),
                recomendacion.getBodegaDestino().getId(),
                recomendacion.getTipo(),
                EstadoRecomendacion.PENDIENTE_APROBACION);
    }

    private int excedente(Existencia existencia) {
        return existencia.getDisponible() - existencia.getProducto().getStockMinimo();
    }

    private int necesario(Existencia existencia) {
        return Math.max(1, existencia.getProducto().getStockMinimo() - existencia.getDisponible());
    }

    private String detalleCobertura(PronosticoResultado pronostico, Producto producto) {
        if (pronostico.origenCalculo() == OrigenCalculo.FALLBACK_MINIMO) {
            return String.format("pronostico no disponible, evaluado contra el stock minimo %d",
                    producto.getStockMinimo());
        }
        return String.format("%.2f dias de cobertura", pronostico.diasCobertura());
    }
}
