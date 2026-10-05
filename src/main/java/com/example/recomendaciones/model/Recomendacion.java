package com.example.recomendaciones.model;

import com.example.inventario.model.Bodega;
import com.example.inventario.model.Producto;
import com.example.pronostico.NivelRiesgo;
import com.example.pronostico.OrigenCalculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recomendaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoRecomendacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 25)
    @Builder.Default
    private EstadoRecomendacion estado = EstadoRecomendacion.PENDIENTE_APROBACION;

    @Enumerated(EnumType.STRING)
    @Column(name = "riesgo", nullable = false, length = 10)
    private NivelRiesgo riesgo;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_calculo", nullable = false, length = 25)
    private OrigenCalculo origenCalculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_origen_id")
    private Bodega bodegaOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bodega_destino_id", nullable = false)
    private Bodega bodegaDestino;

    @Column(name = "cantidad_sugerida", nullable = false)
    private Integer cantidadSugerida;

    @Column(name = "dias_cobertura", precision = 12, scale = 2)
    private BigDecimal diasCobertura;

    @Column(length = 400)
    private String descripcion;

    @Column(name = "usuario_decision", length = 80)
    private String usuarioDecision;

    @Column(name = "motivo_decision", length = 400)
    private String motivoDecision;

    @Column(name = "fecha_decision")
    private LocalDateTime fechaDecision;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoRecomendacion.PENDIENTE_APROBACION;
        }
    }

    public boolean estaPendiente() {
        return estado == EstadoRecomendacion.PENDIENTE_APROBACION;
    }
}
