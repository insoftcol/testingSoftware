# PJBA — Pruebas de Integración y Sistema: Transferencias Bancarias

Proyecto de **pruebas de integración y sistema** para el dominio de transferencias bancarias
del sistema **PJBA / Insoftcol**. Extiende el proyecto `pjba-tdd` (pruebas unitarias)
agregando la capa de persistencia (H2/JDBC), la capa REST (Spring Boot) y los tres niveles
de prueba automatizada: H2, Mockito y TestRestTemplate.

**Integrantes:** Fredy Orlando Pulido Quintero  
**Materia:** Testing y Validación de Software — Universidad de La Sabana — 2026  
**Repositorio:** `github.com/insoftcol/testingSoftware` → carpeta `pjba-integracion/`

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
# → POST http://localhost:8080/transferencia
```

---

## 1. Descripción del Dominio

El dominio de transferencias bancarias PJBA permite a los clientes del Banco realizar
transferencias entre cuentas. Una transferencia es válida cuando:

| Regla | Validación |
|-------|-----------|
| Solicitud no nula | `DATOS_INVALIDOS` si es null |
| Cuenta origen válida | `CUENTA_ORIGEN_INVALIDA` si es null o número vacío |
| Cuenta destino válida | `CUENTA_DESTINO_INVALIDA` si es null o vacío |
| Monto en rango | `MONTO_INVALIDO` si ≤ 0 o > $10.000.000 |
| Saldo suficiente | `SALDO_INSUFICIENTE` si saldo < monto |
| Entidad soportada | `ENTIDAD_NO_SOPORTADA` si no está en la lista |
| Todo válido | `EXITOSA` — se persiste en audit log |

La innovación respecto a `pjba-tdd`: cada intento de transferencia (exitoso o fallido)
se persiste en un **audit log** (`transferencia_log`) para trazabilidad regulatoria.

---

## 2. Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────┐
│               DELIVERY (REST)                        │
│   POST /transferencia → TransferenciaController      │
│   TransferenciaDTO (JSON de entrada)                 │
│   HTTP 200: EXITOSA  |  HTTP 400: cualquier fallo    │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│          APPLICATION (Caso de Uso)                   │
│   ServicioTransferencia.procesarTransferencia()      │
│   ↕ TransferenciaRepositoryPort (puerto de salida)   │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│          INFRASTRUCTURE (Persistencia)               │
│   TransferenciaRepository → H2/JDBC                  │
│   Tabla: transferencia_log                           │
│   (cuenta_origen, cuenta_destino, monto, resultado)  │
└─────────────────────────────────────────────────────┘
```

```
src/
 ├─ main/java/edu/unisabana/tyvs/pjba/
 │   ├─ domain/model/         # Cuenta, SolicitudTransferencia,
 │   │                        # TipoCuenta, TransferenciaResult
 │   ├─ application/
 │   │   ├─ usecase/          # ServicioTransferencia.java
 │   │   └─ port/out/         # TransferenciaRepositoryPort.java
 │   ├─ infrastructure/
 │   │   └─ persistence/      # TransferenciaRepository, TransferenciaRecord
 │   ├─ delivery/rest/        # TransferenciaController, TransferenciaDTO
 │   ├─ config/               # PjbaConfig (beans Spring)
 │   └─ PjbaApplication.java
 └─ test/java/edu/unisabana/tyvs/pjba/
     ├─ integration/
     │   ├─ ServicioTransferenciaH2Test.java    # Integración H2
     │   └─ ServicioTransferenciaMockTest.java  # Mockito
     └─ system/
         └─ TransferenciaControllerIT.java      # Sistema (TestRestTemplate)
```

---

## 3. API REST

### Endpoint: `POST /transferencia`

**Request Body (JSON):**

```json
{
  "numeroCuentaOrigen":  "1234567890",
  "tipoCuenta":          "AHORROS",
  "entidadBancaria":     "AV VILLAS",
  "saldoOrigen":         500000.0,
  "numeroCuentaDestino": "9876543210",
  "monto":               200000.0
}
```

**Responses:**

| Código HTTP | Body | Significado |
|-------------|------|-------------|
| `200 OK` | `"EXITOSA"` | Transferencia procesada y persistida |
| `400 BAD REQUEST` | `"MONTO_INVALIDO"` | Monto fuera de rango |
| `400 BAD REQUEST` | `"SALDO_INSUFICIENTE"` | Fondos insuficientes |
| `400 BAD REQUEST` | `"CUENTA_ORIGEN_INVALIDA"` | Datos de origen incorrectos |
| `400 BAD REQUEST` | `"ENTIDAD_NO_SOPORTADA"` | Entidad bancaria no registrada |
| `400 BAD REQUEST` | `"DATOS_INVALIDOS"` | JSON inválido o tipo de cuenta desconocido |

---

## 4. Pruebas de Integración

### 4.1 Integración con H2 — `ServicioTransferenciaH2Test`

Verifica la **colaboración real** entre `ServicioTransferencia` y `TransferenciaRepository`
usando H2 en memoria, sin Spring (prueba rápida y aislada).

**Configuración (`@Before`):**

```java
@Before
public void setUp() throws Exception {
    String jdbc = "jdbc:h2:mem:pjba_test;DB_CLOSE_DELAY=-1";
    repo     = new TransferenciaRepository(jdbc);
    repo.initSchema();   // CREATE TABLE transferencia_log(...)
    repo.deleteAll();    // Limpiar entre pruebas
    servicio = new ServicioTransferencia(repo); // Inyectar repositorio H2 real
}
```

**Prueba 1 — Transferencia exitosa se persiste:**

```java
@Test
public void shouldPersistirTransferenciaExitosaEnBaseDeDatos() throws Exception {
    // Arrange
    Cuenta origen = new Cuenta("1234567890", TipoCuenta.AHORROS, "AV VILLAS", 500_000.0);
    SolicitudTransferencia solicitud =
        new SolicitudTransferencia(origen, "9876543210", 200_000.0);

    // Act
    TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

    // Assert — dominio
    assertEquals(TransferenciaResult.EXITOSA, resultado);

    // Assert — persistencia en H2
    List<TransferenciaRecord> registros = repo.findAll();
    assertEquals(1, registros.size());
    assertEquals("EXITOSA",    registros.get(0).getResultado());
    assertEquals("1234567890", registros.get(0).getCuentaOrigen());
    assertEquals(200_000.0,    registros.get(0).getMonto(), 0.01);
}
```

**Prueba 2 — Intento fallido también persiste (audit log):**

```java
@Test
public void shouldPersistirIntentoFallidoPorSaldoInsuficiente() throws Exception {
    // Arrange: saldo=100K, monto=500K → SALDO_INSUFICIENTE
    Cuenta origen = new Cuenta("1234567890", TipoCuenta.AHORROS, "AV VILLAS", 100_000.0);
    SolicitudTransferencia solicitud =
        new SolicitudTransferencia(origen, "9876543210", 500_001.0);

    // Act
    TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

    // Assert — dominio
    assertEquals(TransferenciaResult.SALDO_INSUFICIENTE, resultado);

    // Assert — el intento fallido se registra igualmente (trazabilidad regulatoria)
    List<TransferenciaRecord> registros = repo.findAll();
    assertEquals(1, registros.size());
    assertEquals("SALDO_INSUFICIENTE", registros.get(0).getResultado());
}
```

**Prueba 3 — Contador de exitosas:**

```java
@Test
public void shouldContarCorrectamenteLasTransferenciasExitosas() throws Exception {
    // 2 exitosas + 1 fallida
    // ... (ver código fuente)
    assertEquals(3, repo.findAll().size());
    assertEquals(2, repo.countExitosas());
}
```

---

### 4.2 Integración con Mock (Mockito) — `ServicioTransferenciaMockTest`

Verifica los **contratos de interacción** entre `ServicioTransferencia` y el repositorio,
sin usar base de datos real.

```java
@Before
public void setUp() {
    repoMock = mock(TransferenciaRepositoryPort.class);
    servicio = new ServicioTransferencia(repoMock);
}
```

**Prueba 1 — Transferencia exitosa invoca `save()` exactamente una vez:**

```java
@Test
public void shouldLlamarSaveWhenTransferenciaEsExitosa() throws Exception {
    Cuenta origen = new Cuenta("1234567890", TipoCuenta.AHORROS, "AV VILLAS", 500_000.0);
    SolicitudTransferencia solicitud =
        new SolicitudTransferencia(origen, "9876543210", 200_000.0);

    TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

    assertEquals(TransferenciaResult.EXITOSA, resultado);
    // Verificar interacción: save() llamado 1 vez con los argumentos correctos
    verify(repoMock, times(1))
        .save(eq(solicitud), eq(TransferenciaResult.EXITOSA));
}
```

**Prueba 2 — Solicitud nula: `save()` se llama con DATOS_INVALIDOS:**

```java
@Test
public void shouldLlamarSaveConDatosInvalidosWhenSolicitudEsNula() throws Exception {
    TransferenciaResult resultado = servicio.procesarTransferencia(null);

    assertEquals(TransferenciaResult.DATOS_INVALIDOS, resultado);
    verify(repoMock, times(1))
        .save(isNull(), eq(TransferenciaResult.DATOS_INVALIDOS));
}
```

**Prueba 3 — Saldo insuficiente: nunca se llama con EXITOSA:**

```java
verify(repoMock, never()).save(any(), eq(TransferenciaResult.EXITOSA));
```

---

## 5. Pruebas de Sistema (REST) — `TransferenciaControllerIT`

Valida el stack completo como caja negra. Spring Boot inicia en puerto aleatorio.

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransferenciaControllerIT {
    @Autowired TestRestTemplate rest;
    @Autowired TransferenciaRepositoryPort repo;

    @Before public void setUp() throws Exception { repo.deleteAll(); }
}
```

| Prueba | Request JSON | HTTP esperado | Body esperado |
|--------|-------------|---------------|---------------|
| Transferencia válida AV Villas | `saldo=500K, monto=200K, entidad=AV VILLAS` | `200 OK` | `"EXITOSA"` |
| Monto cero | `monto=0.0` | `400 BAD REQUEST` | `"MONTO_INVALIDO"` |
| Saldo insuficiente | `saldo=100K, monto=500K` | `400 BAD REQUEST` | `"SALDO_INSUFICIENTE"` |

---

## 6. Tabla de Pruebas por Nivel

| Tipo | Clase | # Tests | Herramienta | Tiempo |
|------|-------|---------|-------------|--------|
| Integración H2 | `ServicioTransferenciaH2Test` | 3 | H2/JDBC directo | ~100ms |
| Integración Mock | `ServicioTransferenciaMockTest` | 3 | Mockito | ~10ms |
| Sistema REST | `TransferenciaControllerIT` | 3 | TestRestTemplate | ~3s |

---

## 7. Cobertura (JaCoCo)

```bash
mvn clean verify
open target/site/jacoco/index.html
```

| Paquete | Cobertura esperada |
|---------|-------------------|
| `domain.model` | ~100% |
| `application.usecase` | ~100% |
| `infrastructure.persistence` | ~90% |
| `delivery.rest` | ~85% |
| **Global** | **≥ 80%** |

---

## 8. Registro de Defectos

Ver `defectos_integracion.md`. Resumen:

| ID | Defecto | Tipo | Estado |
|----|---------|------|--------|
| 01 | Solicitud nula no se persistía (NPE en `save()`) | Integración H2 | Resuelto |
| 02 | Mock sin `verify()` → tests no significativos | Integración Mock | Resuelto |
| 03 | Tipo de cuenta inválido → HTTP 500 en lugar de 400 | Sistema REST | Resuelto |

---

## 9. Reflexión Técnica

### ¿Diferencias entre pruebas unitarias y de integración?

El proyecto `pjba-tdd` valida **solo las reglas de dominio** en total aislamiento: `ServicioTransferencia`
sin repositorio no puede detectar si H2 persiste correctamente. Las pruebas de integración agregan
confianza sobre el flujo completo: validación + persistencia + respuesta HTTP. La contrapartida es
mayor tiempo de ejecución (~30x más lento que unitarias).

### ¿Qué capa presentó más desafíos?

La capa de persistencia (`TransferenciaRepository`). La decisión de persistir **todos** los intentos
(exitosos y fallidos) como audit log requirió adaptar el `save()` para manejar solicitudes nulas
correctamente, lo que derivó en el Defecto 01.

### ¿Cómo se simularon los componentes externos?

H2 en memoria reemplaza la base de datos Oracle de producción sin cambiar ningún código de producción
(inversión de dependencias vía `TransferenciaRepositoryPort`). Mockito simula el repositorio completo
para probar interacciones sin infraestructura.

### ¿Qué defectos se detectaron en la capa REST?

El Defecto 03: `tipoCuenta: "INVALIDO"` causaba HTTP 500 por `IllegalArgumentException` en
`TipoCuenta.valueOf()`. Solo fue detectable con la prueba de sistema; las pruebas de integración
no cubrían el controlador.

### ¿Qué aprendiste al ejecutar pruebas de sistema completas?

Que el **contrato HTTP** (códigos de estado, formato de respuesta, Content-Type) es parte del
comportamiento observable del sistema y solo puede validarse en este nivel. Los errores de
serialización/deserialización JSON son invisibles para pruebas unitarias y de integración.

---

## Referencias

- Koskela, L. (2013). *Effective Unit Testing*. Manning Publications.
- Martin, R. C. (2017). *Clean Architecture*. Prentice Hall.
- Humble, J., & Farley, D. (2010). *Continuous Delivery*. Addison-Wesley.
- Spring Boot: *Testing*. https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- ISO/IEC/IEEE 29119 – *Software Testing Standards*.
