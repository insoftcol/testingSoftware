# Logística — Pruebas de Integración y Sistema: Cotizador de Envíos

Proyecto de **pruebas de integración y sistema** para el dominio de cotización de envíos logísticos
entre ciudades colombianas. Extiende `logistica-tdd` (pruebas unitarias) agregando capa de
persistencia de cotizaciones (H2/JDBC), API REST (Spring Boot) y los tres niveles de prueba
automatizada: H2, Mockito y TestRestTemplate.

**Integrantes:** Fredy Orlando Pulido Quintero - Myriam Andrea Martinez Fontecha  
**Materia:** Testing y Validación de Software — Universidad de La Sabana — 2026  
**Repositorio:** `github.com/insoftcol/testingSoftware` → carpeta `logistica-integracion/`

---

## ▶️ Ejecución

```bash
# Pruebas unitarias + integración
mvn clean test

# Integración (*IT.java) + reporte JaCoCo
mvn clean verify

# Abrir reporte de cobertura
open target/site/jacoco/index.html

# Levantar la API REST localmente
mvn spring-boot:run
# → POST http://localhost:8080/cotizacion
```

---

## 1. Descripción del Dominio

El cotizador logístico calcula el precio de un envío de paquetes entre ciudades colombianas según
su distancia, peso y modalidad. Cada solicitud de cotización genera un registro en el historial
(`cotizacion_log`) para análisis y auditoría.

### Fórmula de tarifa

```
valorBase  = TARIFA_BASE_ZONA  +  COSTO_KG_ZONA × pesoKg
valorTotal = valorBase × multiplicadorTipoEnvio

Zonas:
  CORTA  (< 200 km)  → base $15.000 + $500/kg
  MEDIA  (200–500 km) → base $30.000 + $800/kg
  LARGA  (> 500 km)  → base $50.000 + $1.200/kg

Multiplicadores:
  NORMAL      × 1.0
  EXPRESO     × 1.5
  REFRIGERADO × 2.0
```

### Ciudades soportadas

`BOGOTA`, `MEDELLIN`, `CALI`, `BARRANQUILLA`, `CARTAGENA`, `BUCARAMANGA`, `MANIZALES`, `PEREIRA`

### Estados de cotización

| Estado | Significado |
|--------|-------------|
| `EXITOSA` | Cotización calculada, persistida en BD |
| `PESO_INVALIDO` | Peso ≤ 0 o > 500 kg |
| `CIUDAD_ORIGEN_INVALIDA` | Origen null, vacío o no registrado |
| `CIUDAD_DESTINO_INVALIDA` | Destino null, vacío o no registrado |
| `MISMA_CIUDAD` | Origen == Destino |
| `RUTA_NO_DISPONIBLE` | Par de ciudades sin distancia configurada |
| `DATOS_INVALIDOS` | Solicitud null |

---

## 2. Arquitectura del Sistema

```
┌──────────────────────────────────────────────────────┐
│                DELIVERY (REST)                        │
│   POST /cotizacion → CotizacionController             │
│   CotizacionRequestDTO  → CotizacionResponseDTO       │
│   HTTP 200: EXITOSA + valor  |  HTTP 400: error       │
└──────────────────────┬───────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────┐
│           APPLICATION (Caso de Uso)                   │
│   ServicioTarifa.cotizar(SolicitudEnvio)              │
│   ↕ CotizacionRepositoryPort (puerto de salida)       │
└──────────────────────┬───────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────┐
│          INFRASTRUCTURE (Persistencia)                │
│   CotizacionRepository → H2/JDBC                     │
│   Tabla: cotizacion_log                              │
│   (origen, destino, peso, tipo, estado, valor_total) │
└──────────────────────────────────────────────────────┘
```

```
src/
 ├─ main/java/edu/unisabana/tyvs/logistica/
 │   ├─ domain/model/       # SolicitudEnvio, Cotizacion,
 │   │                      # CotizacionEstado, TipoEnvio
 │   ├─ application/
 │   │   ├─ usecase/        # ServicioTarifa.java
 │   │   └─ port/out/       # CotizacionRepositoryPort.java
 │   ├─ infrastructure/
 │   │   └─ persistence/    # CotizacionRepository, CotizacionRecord
 │   ├─ delivery/rest/      # CotizacionController, Request/ResponseDTO
 │   ├─ config/             # LogisticaConfig (beans Spring)
 │   └─ LogisticaApplication.java
 └─ test/java/edu/unisabana/tyvs/logistica/
     ├─ integration/
     │   ├─ ServicioTarifaH2Test.java    # Integración H2
     │   └─ ServicioTarifaMockTest.java  # Mockito
     └─ system/
         └─ CotizacionControllerIT.java  # Sistema (TestRestTemplate)
```

---

## 3. API REST

### Endpoint: `POST /cotizacion`

**Request Body (JSON):**

```json
{
  "ciudadOrigen":  "BOGOTA",
  "ciudadDestino": "MEDELLIN",
  "pesoKg":        10.0,
  "tipoEnvio":     "NORMAL"
}
```

**Response Body (JSON):**

```json
{
  "estado":     "EXITOSA",
  "valorTotal": 38000.0
}
```

**Tabla de respuestas:**

| Request | HTTP | Respuesta |
|---------|------|-----------|
| BOGOTA→MEDELLIN, 10kg, NORMAL | `200 OK` | `{ estado:"EXITOSA", valorTotal:38000.0 }` |
| CALI→CALI, 10kg, NORMAL | `400` | `{ estado:"MISMA_CIUDAD", valorTotal:0.0 }` |
| BOGOTA→MEDELLIN, 0kg, NORMAL | `400` | `{ estado:"PESO_INVALIDO", valorTotal:0.0 }` |
| tipoEnvio:"INVALIDO" | `400` | `{ estado:"DATOS_INVALIDOS", valorTotal:0.0 }` |

### Ejemplos con cURL

```bash
# Cotización exitosa zona MEDIA (415 km)
curl -X POST http://localhost:8080/cotizacion \
  -H "Content-Type: application/json" \
  -d '{"ciudadOrigen":"BOGOTA","ciudadDestino":"MEDELLIN","pesoKg":10.0,"tipoEnvio":"NORMAL"}'
# Respuesta: {"estado":"EXITOSA","valorTotal":38000.0}

# Cotización zona CORTA Manizales→Pereira (43 km), 5kg
curl -X POST http://localhost:8080/cotizacion \
  -H "Content-Type: application/json" \
  -d '{"ciudadOrigen":"MANIZALES","ciudadDestino":"PEREIRA","pesoKg":5.0,"tipoEnvio":"NORMAL"}'
# Respuesta: {"estado":"EXITOSA","valorTotal":17500.0}

# Envío EXPRESO (×1.5)
curl -X POST http://localhost:8080/cotizacion \
  -H "Content-Type: application/json" \
  -d '{"ciudadOrigen":"BOGOTA","ciudadDestino":"MEDELLIN","pesoKg":10.0,"tipoEnvio":"EXPRESO"}'
# Respuesta: {"estado":"EXITOSA","valorTotal":57000.0}
```

---

## 4. Pruebas de Integración

### 4.1 Integración con H2 — `ServicioTarifaH2Test`

Valida la colaboración real entre `ServicioTarifa` y `CotizacionRepository` sin Spring.

**Configuración (`@Before`):**

```java
@Before
public void setUp() throws Exception {
    String jdbc = "jdbc:h2:mem:logistica_test;DB_CLOSE_DELAY=-1";
    repo     = new CotizacionRepository(jdbc);
    repo.initSchema();
    repo.deleteAll();
    servicio = new ServicioTarifa(repo);
}
```

**Prueba 1 — Cotización exitosa persiste valor correcto:**

```java
@Test
public void shouldPersistirCotizacionExitosaConValorCorrecto() throws Exception {
    // Arrange: Bogotá→Medellín 415km (MEDIA), 10kg, NORMAL
    SolicitudEnvio sol =
        new SolicitudEnvio("BOGOTA", "MEDELLIN", 10.0, TipoEnvio.NORMAL);

    // Act
    Cotizacion cotizacion = servicio.cotizar(sol);

    // Assert — dominio: 30.000 + 800×10 = 38.000
    assertEquals(CotizacionEstado.EXITOSA, cotizacion.getEstado());
    assertEquals(38_000.0, cotizacion.getValorTotal(), 0.01);

    // Assert — persistencia en H2
    List<CotizacionRecord> registros = repo.findAll();
    assertEquals(1, registros.size());
    assertEquals("EXITOSA",  registros.get(0).getEstado());
    assertEquals("BOGOTA",   registros.get(0).getCiudadOrigen());
    assertEquals("MEDELLIN", registros.get(0).getCiudadDestino());
    assertEquals(38_000.0,   registros.get(0).getValorTotal(), 0.01);
}
```

**Prueba 2 — Error persiste con estado correcto (trazabilidad):**

```java
@Test
public void shouldPersistirCotizacionInvalidaPorPesoInvalido() throws Exception {
    SolicitudEnvio sol =
        new SolicitudEnvio("BOGOTA", "MEDELLIN", 0.0, TipoEnvio.NORMAL);

    Cotizacion cotizacion = servicio.cotizar(sol);

    assertEquals(CotizacionEstado.PESO_INVALIDO, cotizacion.getEstado());
    assertEquals(0.0, cotizacion.getValorTotal(), 0.01);

    // El intento inválido también queda registrado
    List<CotizacionRecord> registros = repo.findAll();
    assertEquals(1, registros.size());
    assertEquals("PESO_INVALIDO", registros.get(0).getEstado());
}
```

**Prueba 3 — Contador de exitosas en historial:**

```java
@Test
public void shouldContarCotizacionesExitosasCorrectamente() throws Exception {
    // 2 exitosas + 1 misma ciudad
    servicio.cotizar(new SolicitudEnvio("BOGOTA","MEDELLIN",10.0,TipoEnvio.NORMAL));
    servicio.cotizar(new SolicitudEnvio("MANIZALES","PEREIRA",5.0,TipoEnvio.EXPRESO));
    servicio.cotizar(new SolicitudEnvio("BOGOTA","BOGOTA",10.0,TipoEnvio.NORMAL));

    assertEquals(3, repo.findAll().size());
    assertEquals(2, repo.countExitosas());
}
```

---

### 4.2 Integración con Mock (Mockito) — `ServicioTarifaMockTest`

Verifica los contratos de interacción sin base de datos.

```java
@Before
public void setUp() {
    repoMock = mock(CotizacionRepositoryPort.class);
    servicio = new ServicioTarifa(repoMock);
}
```

**Prueba 1 — Cotización exitosa: `save()` invocado con cotización exitosa:**

```java
@Test
public void shouldLlamarSaveWhenCotizacionEsExitosa() throws Exception {
    SolicitudEnvio sol =
        new SolicitudEnvio("BOGOTA", "MEDELLIN", 10.0, TipoEnvio.NORMAL);

    Cotizacion cotizacion = servicio.cotizar(sol);

    assertEquals(CotizacionEstado.EXITOSA, cotizacion.getEstado());
    // Verificar que save() fue llamado con una cotización exitosa
    verify(repoMock, times(1))
        .save(eq(sol), argThat(c -> c.isExitosa()));
}
```

**Prueba 2 — Misma ciudad: `save()` con estado MISMA_CIUDAD, nunca con EXITOSA:**

```java
@Test
public void shouldLlamarSaveConMismaCiudadWhenOrigenIgualDestino() throws Exception {
    SolicitudEnvio sol =
        new SolicitudEnvio("CALI", "CALI", 10.0, TipoEnvio.NORMAL);

    Cotizacion cotizacion = servicio.cotizar(sol);

    assertEquals(CotizacionEstado.MISMA_CIUDAD, cotizacion.getEstado());
    verify(repoMock, times(1))
        .save(eq(sol), argThat(c -> c.getEstado() == CotizacionEstado.MISMA_CIUDAD));
    verify(repoMock, never())
        .save(any(), argThat(c -> c.isExitosa()));
}
```

---

## 5. Pruebas de Sistema (REST) — `CotizacionControllerIT`

Valida el sistema completo como caja negra vía HTTP.

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CotizacionControllerIT {
    @Autowired TestRestTemplate rest;
    @Autowired CotizacionRepositoryPort repo;

    @Before public void setUp() throws Exception { repo.deleteAll(); }
}
```

| Prueba | Request | HTTP | Respuesta |
|--------|---------|------|-----------|
| Ruta válida MEDIA | BOGOTA→MEDELLIN, 10kg, NORMAL | `200 OK` | `EXITOSA, 38000.0` |
| Misma ciudad | CALI→CALI, 10kg, NORMAL | `400` | `MISMA_CIUDAD, 0.0` |
| Peso cero | BOGOTA→MEDELLIN, 0kg | `400` | `PESO_INVALIDO, 0.0` |

**Fragmento de prueba:**

```java
@Test
public void shouldReturn200ExitosaWhenRutaValidaBogotaMedellin() {
    String json = "{\"ciudadOrigen\":\"BOGOTA\",\"ciudadDestino\":\"MEDELLIN\"," +
                  "\"pesoKg\":10.0,\"tipoEnvio\":\"NORMAL\"}";

    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<CotizacionResponseDTO> resp =
        rest.postForEntity("/cotizacion", new HttpEntity<>(json, h),
                           CotizacionResponseDTO.class);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals("EXITOSA", resp.getBody().getEstado());
    assertEquals(38_000.0,  resp.getBody().getValorTotal(), 0.01);
}
```

---

## 6. Matriz de Verificación por Nivel

| Escenario | H2 Test | Mock Test | IT (Sistema) |
|-----------|---------|-----------|--------------|
| Bogotá→Medellín exitosa ($38.000) | ✅ | ✅ | ✅ |
| Misma ciudad (MISMA_CIUDAD) | ✅ | ✅ | ✅ |
| Peso = 0 (PESO_INVALIDO) | ✅ | — | ✅ |
| 2 exitosas + 1 fallida → countExitosas=2 | ✅ | — | — |
| Persistencia: campos correctos en BD | ✅ | — | — |
| `save()` invocado exactamente 1 vez | — | ✅ | — |
| `save()` nunca con EXITOSA en error | — | ✅ | — |

---

## 7. Cobertura (JaCoCo)

```bash
mvn clean verify
open target/site/jacoco/index.html
```

| Paquete | Cobertura esperada |
|---------|-------------------|
| `domain.model` | ~100% |
| `application.usecase` | ~95% |
| `infrastructure.persistence` | ~90% |
| `delivery.rest` | ~85% |
| **Global** | **≥ 80%** |

**Líneas sin cubrir:** el manejo de excepción en `cotizacion_log` si la conexión falla
(caso de error de infraestructura crítico que no se simula en pruebas normales).

---

## 8. Registro de Defectos

Ver `defectos_integracion.md`. Resumen:

| ID | Defecto | Tipo | Estado |
|----|---------|------|--------|
| 01 | NPE en `CotizacionRepository.save()` con solicitud null | Integración H2 | Resuelto |
| 02 | Tests mock sin `verify()` → sin valor | Integración Mock | Resuelto |
| 03 | `tipoEnvio` inválido → HTTP 500 en lugar de 400 | Sistema REST | Resuelto |

---

## 9. Reflexión Técnica

### ¿Diferencias entre pruebas unitarias y de integración?

El proyecto `logistica-tdd` valida que la fórmula de tarifa calcula correctamente
(38.000 para Bogotá→Medellín, 10kg, NORMAL). Las pruebas de integración agregan la pregunta:
**¿se persiste ese cálculo correctamente en la BD?** La primera solo valida lógica;
la segunda valida el flujo completo incluyendo serialización SQL y transacción.

### ¿Qué capa presentó más desafíos?

La capa de persistencia al manejar los estados de error. Decidir persistir **todos** los
intentos (no solo los exitosos) requirió manejar los campos opcionales en `save()` cuando
la `SolicitudEnvio` era parcialmente nula, derivando en el Defecto 01.

### ¿Cómo se simularon los componentes externos?

H2 en memoria reemplaza la base de datos de producción (PostgreSQL/Oracle) sin modificar
el código de producción — solo cambia la URL JDBC en `LogisticaConfig`. Mockito aísla el
repositorio para probar contratos de interacción (cuántas veces se llama `save()`, con qué argumentos).

### ¿Qué defectos se detectaron en la capa REST?

El Defecto 03: `tipoEnvio: "VUELO"` (no existe en el enum) causaba HTTP 500 por
`IllegalArgumentException` no capturada. El controlador solo era robusto si el cliente enviaba
valores exactos del enum. La prueba de sistema lo detectó; las pruebas de dominio e integración
no cubren el parsing HTTP.

### ¿Qué aprendiste al ejecutar pruebas de sistema completas?

Que el sistema logístico no es solo la fórmula de tarifa — incluye el **protocolo HTTP**:
cómo se serializa el JSON de respuesta, qué código HTTP corresponde a cada resultado, y cómo
se comporta el sistema ante entradas malformadas. Solo el nivel de sistema prueba esto end-to-end.

---

## Referencias

- Koskela, L. (2013). *Effective Unit Testing*. Manning Publications.
- Martin, R. C. (2017). *Clean Architecture*. Prentice Hall.
- Humble, J., & Farley, D. (2010). *Continuous Delivery*. Addison-Wesley.
- Spring Boot: *Testing*. https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- Fowler, M. (2006). *Continuous Integration*. https://martinfowler.com/articles/continuousIntegration.html
- ISO/IEC/IEEE 29119 – *Software Testing Standards*.
