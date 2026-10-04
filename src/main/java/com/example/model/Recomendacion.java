package com.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recomendaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recomendacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    @Column(nullable = false)
    private String tipoRecomendacion;

    private String descripcion;

    private Integer cantidadSugerida;

    @Builder.Default
    @Column(nullable = false)
    private Boolean aprobada = false;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaAprobacion;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
    }
}
