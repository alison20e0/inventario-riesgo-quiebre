package com.example.aprobaciones.service;

import com.example.auditoria.service.AuditoriaService;
import com.example.messaging.RecomendacionAprobada;
import com.example.recomendaciones.dto.RecomendacionResponse;
import com.example.recomendaciones.model.EstadoRecomendacion;
import com.example.recomendaciones.model.Recomendacion;
import com.example.recomendaciones.service.RecomendacionMapper;
import com.example.recomendaciones.service.RecomendacionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AprobacionService {

    private static final Logger log = LoggerFactory.getLogger(AprobacionService.class);

    private static final String ENTIDAD = "RECOMENDACION";
    private static final String ACCION_APROBAR = "APROBAR";
    private static final String ACCION_RECHAZAR = "RECHAZAR";

    private final RecomendacionService recomendacionService;
    private final RecomendacionMapper mapper;
    private final AuditoriaService auditoriaService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RecomendacionResponse aprobar(Long id, String usuario, String motivo) {
        Recomendacion recomendacion = recomendacionService.cargarEntidad(id);
        asegurarPendiente(recomendacion);
        LocalDateTime fechaDecision = LocalDateTime.now();

        recomendacion.setEstado(EstadoRecomendacion.APROBADA);
        recomendacion.setUsuarioDecision(usuario);
        recomendacion.setMotivoDecision(motivo);
        recomendacion.setFechaDecision(fechaDecision);
        Recomendacion guardada = recomendacionService.actualizar(recomendacion);

        auditoriaService.registrar(ENTIDAD, ACCION_APROBAR, usuario, motivo, guardada.getId());
        log.info("Recomendacion {} aprobada por {}.", guardada.getId(), usuario);
        eventPublisher.publishEvent(toEvento(guardada, fechaDecision));
        return mapper.toResponse(guardada);
    }

    @Transactional
    public RecomendacionResponse rechazar(Long id, String usuario, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El motivo es obligatorio para rechazar una recomendacion");
        }
        Recomendacion recomendacion = recomendacionService.cargarEntidad(id);
        asegurarPendiente(recomendacion);

        recomendacion.setEstado(EstadoRecomendacion.RECHAZADA);
        recomendacion.setUsuarioDecision(usuario);
        recomendacion.setMotivoDecision(motivo);
        recomendacion.setFechaDecision(LocalDateTime.now());
        Recomendacion guardada = recomendacionService.actualizar(recomendacion);

        auditoriaService.registrar(ENTIDAD, ACCION_RECHAZAR, usuario, motivo, guardada.getId());
        log.info("Recomendacion {} rechazada por {}. Motivo: {}", guardada.getId(), usuario, motivo);
        return mapper.toResponse(guardada);
    }

    private void asegurarPendiente(Recomendacion recomendacion) {
        if (!recomendacion.estaPendiente()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La recomendacion " + recomendacion.getId() + " ya esta en estado " + recomendacion.getEstado());
        }
    }

    private RecomendacionAprobada toEvento(Recomendacion recomendacion, LocalDateTime fechaDecision) {
        return new RecomendacionAprobada(
                recomendacion.getId(),
                recomendacion.getTipo(),
                recomendacion.getRiesgo(),
                recomendacion.getOrigenCalculo(),
                recomendacion.getProducto().getSku(),
                recomendacion.getProducto().getNombre(),
                recomendacion.getBodegaOrigen() == null ? null : recomendacion.getBodegaOrigen().getId(),
                recomendacion.getBodegaOrigen() == null ? null : recomendacion.getBodegaOrigen().getCodigo(),
                recomendacion.getBodegaDestino().getId(),
                recomendacion.getBodegaDestino().getCodigo(),
                recomendacion.getCantidadSugerida(),
                recomendacion.getDiasCobertura(),
                recomendacion.getUsuarioDecision(),
                recomendacion.getMotivoDecision(),
                fechaDecision);
    }
}
