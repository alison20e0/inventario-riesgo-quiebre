# Inventario y riesgo de quiebre

Ejercicio de Arquitectura de Sistemas: inventario de una cadena de restaurantes con varias bodegas.
El sistema **consulta existencias**, **estima el riesgo de quiebre** con un pronóstico de consumo y
**recomienda una transferencia o una compra** que una persona **aprueba o rechaza** antes de ejecutarse.
Nada se ejecuta sin aprobación: al aprobar se publica un evento en RabbitMQ que consume el "sistema de compras".

## Arquitectura

```
com.example
├── inventario/        Bodega, Producto, Existencia (bodega + producto, disponible, reservado, consumo diario, @Version)
├── pronostico/        dias de cobertura = disponible / consumo diario + fallback a stock mínimo
├── recomendaciones/   genera TRANSFERENCIA o COMPRA, estado PENDIENTE_APROBACION
├── aprobaciones/      aprobar / rechazar: guarda usuario, motivo y registra auditoría
├── auditoria/         Auditoria de solo inserción (entidad, acción, usuario, motivo, fecha)
├── messaging/         evento RecomendacionAprobada, reintentos, cola de errores y consumidor de compras
└── common/            manejo de errores HTTP
```

Flujo:

1. `GET /api/v1/existencias` devuelve disponible, reservado, consumo diario, días de cobertura, riesgo y
   `origenCalculo` de cada combinación bodega/producto.
2. `POST /api/v1/recomendaciones/generar` crea recomendaciones `PENDIENTE_APROBACION`
   (`POST /api/v1/recomendaciones` sigue funcionando como alias).
3. `PUT /api/v1/recomendaciones/{id}/aprobar` o `/rechazar` reciben `usuario` y `motivo`, guardan la decisión,
   registran la auditoría y, solo al aprobar, publican el evento en RabbitMQ.

### Decisiones

- **Riesgo**: días de cobertura = `disponible / consumo_diario`. `ALTO` ≤ 2 días, `MEDIO` ≤ 5 días, `BAJO` el resto.
- **Fallback**: si el pronóstico no está disponible (`PRONOSTICO_SIMULAR_FALLO=true` o producto sin consumo diario)
  el riesgo se evalúa contra el `stock_minimo` del producto y la recomendación se marca `FALLBACK_MINIMO`.
  La consulta de existencias nunca falla por un pronóstico no disponible.
- **Transferencia vs compra**: si el riesgo es `ALTO` y otra bodega tiene excedente
  (`disponible - stock_minimo > 0`) se propone `TRANSFERENCIA` desde esa bodega; si no, `COMPRA`.
- **Nada automático**: `POST /generar` solo crea recomendaciones pendientes. No mueve unidades ni lanza órdenes.
- **Aprobación humana**: aprobar guarda `usuario_decision`, `motivo_decision` y `fecha_decision`, escribe la
  auditoría y publica `RecomendacionAprobada` **después del commit** de la transacción. Rechazar exige motivo.
- **Sin doble pendiente**: no se repite una recomendación `PENDIENTE_APROBACION` del mismo tipo para el mismo
  producto y bodega.
- **DTOs, no entidades**: los endpoints devuelven DTOs con `spring.jpa.open-in-view=false`, así ninguna relación
  `LAZY` se serializa fuera de la transacción.
- **Auditoría inmutable**: el repositorio de auditoría solo expone `save` y lecturas; la entidad lanza excepción
  si se intenta actualizar o borrar.

## Requisitos

- Docker y Docker Compose v2.
- `curl` para los ejemplos.

## Cómo levantar

```bash
cp .env.example .env          # credenciales: edita .env (está en .gitignore)
docker compose up -d --build  # compila el jar dentro de Docker (multi-etapa)
docker compose ps             # espera a que backend, postgres y rabbitmq estén healthy
```

El backend solo arranca cuando `postgres` y `rabbitmq` están `healthy` (`depends_on: service_healthy`) y su
propio healthcheck consulta `http://localhost:8080/actuator/health`.

```bash
curl -s http://localhost:8080/actuator/health
docker compose logs -f backend
docker compose down            # detiene
docker compose down -v         # detiene y borra los volúmenes (base de datos limpia)
```

## Puertos

| Servicio                | Host     | Interno | Uso                                     |
|-------------------------|----------|---------|-----------------------------------------|
| API                     | `8080`   | 8080    | REST + `/actuator/health`               |
| PostgreSQL              | `5441`   | 5432    | base de datos                           |
| RabbitMQ (AMQP)         | `5671`   | 5672    | evento `RecomendacionAprobada`          |
| RabbitMQ (consola web)  | `15671`  | 15672   | http://localhost:15671                  |

Los puertos 5432 y 5672 del host no se usan.

## Ejemplos

Los datos de ejemplo se cargan solos si la base está vacía: 2 bodegas (`BOD-CEN` Bodega Central,
`BOD-NOR` Bodega Norte), 3 productos (`PRD-001` aceite, `PRD-002` arroz, `PRD-003` pasta) y 6 existencias.
`BOD-NOR` está en riesgo de quiebre y `BOD-CEN` tiene excedente.

> En PowerShell, `curl.exe` no preserva las comillas simples: guarda el cuerpo JSON en un archivo y usa
> `--data-binary "@body.json"`, o invoca `Invoke-RestMethod`. En bash los ejemplos funcionan tal cual.

### 1. Existencias con riesgo de quiebre

```bash
curl -s http://localhost:8080/api/v1/existencias | python -m json.tool
```

`BOD-NOR` + `PRD-002` tiene 12 unidades con un consumo de 10/día (1.20 días de cobertura) → riesgo `ALTO`.

### 2. Generar recomendaciones

```bash
curl -s -X POST http://localhost:8080/api/v1/recomendaciones/generar | python -m json.tool
```

Con los datos de ejemplo devuelve dos recomendaciones `PENDIENTE_APROBACION`, en el orden en que se evalúan
(bodega por código y luego producto por SKU):

| id | tipo      | producto | bodega   | cantidad | motivo                                    |
|----|-----------|----------|----------|----------|-------------------------------------------|
| 1  | `COMPRA`  | `PRD-001`| `BOD-NOR`| 62       | ninguna bodega tiene excedente de aceite  |
| 2  | `TRANSFERENCIA` | `PRD-002` | `BOD-NOR` → `BOD-CEN` | 48 | `BOD-CEN` tiene excedente de arroz |

Repetir el `POST` no duplica: ya existe una pendiente del mismo tipo para ese producto y bodega.

### 3. Listar recomendaciones

```bash
curl -s http://localhost:8080/api/v1/recomendaciones | python -m json.tool
curl -s http://localhost:8080/api/v1/recomendaciones/1 | python -m json.tool
```

### 4. Aprobar (id 2: transferencia de arroz)

```bash
curl -s -X PUT http://localhost:8080/api/v1/recomendaciones/2/aprobar \
  -H "Content-Type: application/json" \
  -d '{"usuario":"alison.estrella","motivo":"Bodega Norte con 1.2 dias de cobertura"}' | python -m json.tool
```

La respuesta trae `estado: APROBADA` con `usuarioDecision`, `motivoDecision` y `fechaDecision`.
En los logs del backend:

```
Evento RecomendacionAprobada publicado en inventario.eventos con routing key recomendacion.aprobada (recomendacion 2).
[SISTEMA-COMPRAS] Transferencia 2 registrada: 48 unidades de PRD-002 desde BOD-CEN hacia BOD-NOR. ...
```

### 5. Rechazar (id 1: compra de aceite)

```bash
curl -s -X PUT http://localhost:8080/api/v1/recomendaciones/1/rechazar \
  -H "Content-Type: application/json" \
  -d '{"usuario":"juan.perez","motivo":"Compra postergada hasta revisar el pedido del proveedor"}' | python -m json.tool
```

El motivo es obligatorio al rechazar y **no** se publica ningún evento.
Una recomendación ya decidida devuelve `409`:

```bash
curl -s -i -X PUT http://localhost:8080/api/v1/recomendaciones/2/aprobar \
  -H "Content-Type: application/json" -d '{"usuario":"otro","motivo":"reintento"}'
```

### 6. Auditoría

```bash
curl -s http://localhost:8080/api/v1/auditorias | python -m json.tool
curl -s http://localhost:8080/api/v1/auditorias/1 | python -m json.tool
```

## Prueba del fallback del pronóstico

Con `PRONOSTICO_SIMULAR_FALLO=true` el pronóstico falla y todo se evalúa contra el `stock_minimo` del
producto, marcando `origenCalculo: FALLBACK_MINIMO`. La consulta de existencias sigue respondiendo.

```bash
docker compose down -v
PRONOSTICO_SIMULAR_FALLO=true docker compose up -d
# esperar a que backend esté healthy
curl -s http://localhost:8080/api/v1/existencias | python -m json.tool
curl -s -X POST http://localhost:8080/api/v1/recomendaciones/generar | python -m json.tool
```

También se puede cambiar solo la propiedad y reiniciar el backend:

```bash
docker compose stop backend
PRONOSTICO_SIMULAR_FALLO=true docker compose up -d backend
docker compose logs -f backend    # WARNING: Pronostico no disponible ... Se usa el stock minimo como fallback.
```

Volver al modo normal: `docker compose down -v && docker compose up -d --build`.

## Reintentos y cola de errores

El consumidor reintenta el evento 3 veces con espera exponencial. Si vuelve a fallar, el mensaje se publica
en el exchange de errores `inventario.dlx` y queda en la cola `compras.solicitudes.errores`.
Para verlo:

```bash
docker compose down -v
COMPRAS_SIMULAR_FALLO=true docker compose up -d --build
curl -s -X POST http://localhost:8080/api/v1/recomendaciones/generar
curl -s -X PUT http://localhost:8080/api/v1/recomendaciones/1/aprobar \
  -H "Content-Type: application/json" -d '{"usuario":"alison.estrella","motivo":"prueba de reintentos"}'
docker compose logs backend | grep -i "rechaz\|recuper\|SISTEMA-COMPRAS"
curl -s -u <RABBITMQ_USER>:<RABBITMQ_PASSWORD> http://localhost:15671/api/queues | python -m json.tool
```

La cola `compras.solicitudes.errores` queda con 1 mensaje; se reencola desde la consola web (pestaña Queues,
botón *Requeue*) o se purga para volver al estado inicial.

## Estructura

| Tabla              | Contenido                                                            |
|--------------------|----------------------------------------------------------------------|
| `bodegas`          | código, nombre, activo                                               |
| `productos`        | sku, nombre, `stock_minimo`                                          |
| `existencias`      | bodega + producto (único), disponible, reservado, `consumo_diario`, `version` |
| `recomendaciones`  | tipo, estado, riesgo, `origen_calculo`, bodega origen/destino, cantidad, decisión |
| `auditorias`       | entidad, acción, usuario, motivo, referencia, fecha (solo inserción) |

## Variables de entorno

Ver `.env.example`. `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `SERVER_PORT`,
`PRONOSTICO_SIMULAR_FALLO`, `COMPRAS_SIMULAR_FALLO`, `APP_SEED_EJEMPLO`. El archivo `.env` está en `.gitignore`.

## Desarrollo local sin Docker

`src/main/resources/application.properties` usa H2 en memoria y `localhost:5672` para RabbitMQ. El proyecto no
incluye el Maven Wrapper, así que usa un contenedor de Maven con el código montado:

```bash
docker run --rm -it -v "%CD%:/build" -v inventariom2:/root/.m2 -w /build maven:3.9-eclipse-temurin-17 mvn spring-boot:run
```

En Linux/macOS cambia `%CD%` por `$(pwd)`. Requiere RabbitMQ levantado en `localhost:5672` (por ejemplo,
`docker compose up -d rabbitmq` con el compose de este repo, que publica `5671`; en ese caso exporta
`RABBITMQ_PORT=5671`).

Las tablas se crean con `ddl-auto` (`create-drop` en local, `update` en el perfil `docker`).
La forma recomendada de trabajar sigue siendo `docker compose up -d --build`.
