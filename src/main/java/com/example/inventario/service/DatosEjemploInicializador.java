package com.example.inventario.service;

import com.example.inventario.model.Bodega;
import com.example.inventario.model.Existencia;
import com.example.inventario.model.Producto;
import com.example.inventario.repository.BodegaRepository;
import com.example.inventario.repository.ExistenciaRepository;
import com.example.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.ejemplo", havingValue = "true", matchIfMissing = true)
public class DatosEjemploInicializador implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosEjemploInicializador.class);

    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final ExistenciaRepository existenciaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productoRepository.count() > 0) {
            log.info("Datos de ejemplo omitidos: el catalogo ya tiene informacion cargada.");
            return;
        }
        Bodega central = bodegaRepository.save(
                Bodega.builder().codigo("BOD-CEN").nombre("Bodega Central").activo(true).build());
        Bodega norte = bodegaRepository.save(
                Bodega.builder().codigo("BOD-NOR").nombre("Bodega Norte").activo(true).build());

        Producto aceite = productoRepository.save(Producto.builder()
                .sku("PRD-001").nombre("Aceite de girasol 900 ml").stockMinimo(40).build());
        Producto arroz = productoRepository.save(Producto.builder()
                .sku("PRD-002").nombre("Arroz grano largo 5 lb").stockMinimo(60).build());
        Producto pasta = productoRepository.save(Producto.builder()
                .sku("PRD-003").nombre("Pasta corta 500 g").stockMinimo(50).build());

        List<Existencia> existencias = List.of(
                existencia(central, aceite, 30, 5, "12"),
                existencia(central, arroz, 200, 20, "10"),
                existencia(central, pasta, 140, 10, "10"),
                existencia(norte, aceite, 18, 4, "15"),
                existencia(norte, arroz, 12, 3, "10"),
                existencia(norte, pasta, 45, 8, "8"));

        existenciaRepository.saveAll(existencias);
        log.info("Datos de ejemplo cargados: {} bodegas, {} productos, {} existencias.",
                bodegaRepository.count(), productoRepository.count(), existenciaRepository.count());
    }

    private Existencia existencia(Bodega bodega, Producto producto, int disponible, int reservado, String consumo) {
        return Existencia.builder()
                .bodega(bodega)
                .producto(producto)
                .disponible(disponible)
                .reservado(reservado)
                .consumoDiario(new BigDecimal(consumo))
                .build();
    }
}
