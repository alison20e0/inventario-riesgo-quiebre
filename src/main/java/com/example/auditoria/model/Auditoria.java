package com.example.auditoria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entidad", nullable = false, length = 60)
    private String entidad;

    @Column(name = "accion", nullable = false, length = 60)
    private String accion;

    @Column(nullable = false, length = 80)
    private String usuario;

    @Column(length = 400)
    private String motivo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        throw new UnsupportedOperationException("La auditoria es de solo insercion");
    }

    @PreRemove
    public void preRemove() {
        throw new UnsupportedOperationException("La auditoria es de solo insercion");
    }
}
