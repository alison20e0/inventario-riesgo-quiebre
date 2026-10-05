package com.example.auditoria.service;

import com.example.auditoria.dto.AuditoriaResponse;
import com.example.auditoria.model.Auditoria;
import com.example.auditoria.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaRepository auditoriaRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Auditoria registrar(String entidad, String accion, String usuario, String motivo, Long referenciaId) {
        Auditoria auditoria = auditoriaRepository.save(Auditoria.builder()
                .entidad(entidad)
                .accion(accion)
                .usuario(usuario)
                .motivo(motivo)
                .referenciaId(referenciaId)
                .build());
        log.info("Auditoria registrada: {} {} por {} (referencia {}).", accion, entidad, usuario, referenciaId);
        return auditoria;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listar() {
        return auditoriaRepository.findAllByOrderByFechaDescIdDesc().stream()
                .map(this::mapear)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listarPorReferencia(String entidad, Long referenciaId) {
        return auditoriaRepository.findByEntidadAndReferenciaIdOrderByFechaDescIdDesc(entidad, referenciaId)
                .stream()
                .map(this::mapear)
                .toList();
    }

    private AuditoriaResponse mapear(Auditoria auditoria) {
        return new AuditoriaResponse(auditoria.getId(), auditoria.getEntidad(), auditoria.getAccion(),
                auditoria.getUsuario(), auditoria.getMotivo(), auditoria.getReferenciaId(), auditoria.getFecha());
    }
}
