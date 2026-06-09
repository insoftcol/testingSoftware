# Taller de Pruebas de Integración y Sistema — Registraduría Electoral

Implementación completa del taller de **pruebas de integración y sistema** sobre el caso de la
**Registraduría Electoral**, usando Spring Boot, H2, Mockito y TestRestTemplate.  
El proyecto muestra los tres niveles de prueba automatizada siguiendo **Arquitectura Limpia**.

**Integrantes:** Fredy Orlando Pulido Quintero  
**Materia:** Testing y Validación de Software — Universidad de La Sabana — 2026  
**Repositorio:** `github.com/insoftcol/testingSoftware` → carpeta `taller-integracion/`

---

## ▶️ Ejecución

```bash
# Ejecutar todas las pruebas (unitarias + integración)
mvn clean test

# Ejecutar pruebas de integración (*IT.java) + generar reporte JaCoCo
mvn clean verify

# Abrir reporte de cobertura
open target/site/jacoco/index.html
```

---

## 1. Descripción del Dominio

El sistema de la **Registraduría Electoral** permite registrar personas como votantes para las
elecciones. Una persona puede ser registrada únicamente si:

- No es `null` y su id es positivo.
- Está viva (`alive = true`).
- Tiene 18 años o más y no supera los 120.
- Su número de documento no ha sido registrado previamente.

El resultado de cada intento de registro se indica mediante el enum `RegisterResult`:
`VALID`, `DUPLICATED`, `INVALID`, `DEAD`, `UNDERAGE`.

---

## 2. Arquitectura del Sistema

```
┌─────────────────────────────────────────────────┐
│            DELIVERY (REST)                       │
│   POST /register  ←→  RegistryController         │
│   PersonDTO (entrada JSON)                        │
└───────────────────┬─────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────┐
│           APPLICATION (Caso de Uso)              │
│   Registry.registerVoter(Person)                 │
│   ↕ RegistryRepositoryPort (puerto de salida)    │
└───────────────────┬─────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────┐
│         INFRASTRUCTURE (Persistencia)            │
│   RegistryRepository → H2/JDBC (tests)           │
│   Tabla: registry(id, name, age, is_alive)        │
└─────────────────────────────────────────────────┘
```

```
src/
 ├─ main/java/edu/unisabana/tyvs/registry/
 │   ├─ domain/model/       # Person, Gender, RegisterResult
 │   ├─ application/
 │   │   ├─ usecase/        # Registry.java (reglas de negocio)
 │   │   └─ port/out/       # RegistryRepositoryPort.java (interfaz)
 │   ├─ infrastructure/
 │   │   └─ persistence/    # RegistryRepository (JDBC), RegistryRecord
 │   ├─ delivery/rest/      # RegistryController, PersonDTO
 │   └─ config/             # RegistryConfig (beans Spring)
 └─ test/java/edu/unisabana/tyvs/registry/
     ├─ AppTest.java
     ├─ application/usecase/
     │   ├─ RegistryTest.java            # Integración con H2
     │   └─ RegistryWithMockTest.java    # Mockito
     └─ delivery/rest/
         └─ RegistryControllerIT.java    # Sistema (TestRestTemplate)
```

---

## 3. Pruebas de Integración

### 3.1 Integración con Base de Datos H2 — `RegistryTest`

Valida la colaboración real entre **`Registry` (caso de uso)** y **`RegistryRepository` (H2)**,
sin levantar Spring. Los objetos se instancian directamente para máxima velocidad.

**Configuración del entorno (patrón AAA en `@Before`):**

```java
@Before
public void setup() throws Exception {
    // Arrange: H2 en memoria, esquema limpio
    String jdbc = "jdbc:h2:mem:regdb;DB_CLOSE_DELAY=-1";
    repo = new RegistryRepository(jdbc);
    repo.initSchema();   // CREATE TABLE IF NOT EXISTS registry(...)
    repo.deleteAll();    // Limpiar datos de tests anteriores
    registry = new Registry(repo); // Inyectar repositorio real
}
```

**Prueba 1 — Registro válido persiste en BD:**

```java
@Test
public void shouldRegisterValidPerson() throws Exception {
    // Arrange
    Person p1 = new Person("Ana", 100, 30, Gender.FEMALE, true);

    // Act
    RegisterResult result = registry.registerVoter(p1);

    // Assert — resultado de dominio
    assertEquals(RegisterResult.VALID, result);
    // Assert — verificar persistencia real en H2
    assertTrue(repo.existsById(100));
}
```

**Prueba 2 — Duplicado detectado y rechazado:**

```java
@Test
public void shouldPersistValidVoterAndRejectDuplicates() throws Exception {
    // Arrange
    Person p1 = new Person("Ana",    100, 30, Gender.FEMALE, true);
    Person p2 = new Person("AnaDos", 100, 40, Gender.FEMALE, true);

    // Act + Assert primer registro
    assertEquals(RegisterResult.VALID,      registry.registerVoter(p1));
    assertTrue(repo.existsById(100));

    // Act + Assert segundo registro (mismo id)
    assertEquals(RegisterResult.DUPLICATED, registry.registerVoter(p2));
}
```

**Diferencia clave con prueba unitaria:** en la prueba unitaria, el duplicado se detecta con un
`Set<Integer>` en memoria. Aquí se detecta con una **consulta SQL real a H2** (`SELECT 1 FROM
registry WHERE id = ?`), validando que la lógica de negocio y la persistencia se integran correctamente.

---

### 3.2 Integración con Mock (Mockito) — `RegistryWithMockTest`

Aísla el caso de uso del repositorio real para **verificar la lógica de interacción** entre
componentes sin base de datos.

**Configuración:**

```java
@Before
public void setUp() {
    repo     = mock(RegistryRepositoryPort.class); // Simular el repositorio
    registry = new Registry(repo);                  // Inyectar el mock
}
```

**Prueba — Verificar que `save()` NO se llama cuando hay duplicado:**

```java
@Test
public void shouldReturnDuplicatedWhenRepoSaysExists() throws Exception {
    // Arrange: configurar mock para simular id=7 ya registrado
    when(repo.existsById(7)).thenReturn(true);
    Person p = new Person("Ana", 7, 25, Gender.FEMALE, true);

    // Act
    RegisterResult result = registry.registerVoter(p);

    // Assert — resultado correcto
    assertEquals(RegisterResult.DUPLICATED, result);

    // Assert — verificar que save() NUNCA fue invocado (no duplicar en BD)
    verify(repo, never()).save(anyInt(), anyString(), anyInt(), anyBoolean());
}
```

**¿Por qué usar mocks?** Permiten probar escenarios de error de infraestructura (ej: BD caída,
timeout) sin levantar infraestructura real.

---

## 4. Pruebas de Sistema (REST) — `RegistryControllerIT`

Valida el comportamiento del sistema como **caja negra** a través del endpoint HTTP, sin conocer
la implementación interna.

**Configuración:**

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistryControllerIT {

    @Autowired TestRestTemplate rest; // Cliente HTTP real
    @Autowired RegistryRepositoryPort repo;

    @Before
    public void setUp() throws Exception {
        repo.deleteAll(); // Limpiar BD entre tests
    }
}
```

**Prueba 1 — Registro válido retorna HTTP 200 + "VALID":**

```java
@Test
public void shouldReturnValidWhenPersonaEsValida() {
    String json = "{\"name\":\"Ana\",\"id\":100,\"age\":30,\"gender\":\"FEMALE\",\"alive\":true}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> resp = rest.postForEntity(
        "/register", new HttpEntity<>(json, headers), String.class
    );

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals("VALID", resp.getBody());
}
```

**Prueba 2 — Menor de edad retorna HTTP 200 + "UNDERAGE":**

```java
@Test
public void shouldReturnUnderageWhenEdadEsMenorDe18() {
    String json = "{\"name\":\"Joven\",\"id\":101,\"age\":15,\"gender\":\"MALE\",\"alive\":true}";
    // ... TestRestTemplate POST /register
    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals("UNDERAGE", resp.getBody());
}
```

**Prueba 3 — Persona muerta retorna HTTP 200 + "DEAD"**

---

## 5. Comparativa de los Tres Tipos de Prueba

| Aspecto | Unitaria (`RegistryTest` puro) | Integración H2 | Integración Mock | Sistema IT |
|---------|-------------------------------|----------------|------------------|------------|
| **Velocidad** | Muy rápida (<1ms) | Rápida (~50ms) | Muy rápida (<5ms) | Lenta (~2s) |
| **Dependencias** | Ninguna | H2 en memoria | Mockito | Spring Boot completo |
| **Qué valida** | Lógica de dominio | Service + Repository | Interacciones | Stack HTTP completo |
| **Aislamiento** | Total | Parcial (BD real) | Total | Ninguno (caja negra) |
| **Confianza** | Dominio | Integración real | Contratos | End-to-end |

---

## 6. Cobertura de Código (JaCoCo)

```bash
mvn clean verify
open target/site/jacoco/index.html
```

El reporte se genera en `target/site/jacoco/index.html`. La cobertura esperada es **≥ 80% global**.

| Clase | Cobertura esperada |
|-------|-------------------|
| `Registry` (caso de uso) | ~100% |
| `RegistryRepository` | ~90% |
| `RegistryController` | ~85% |
| Global | ≥ 80% |

---

## 7. Registro de Defectos

Ver archivo `defectos.md` en la raíz del proyecto. Los 5 defectos documentados son:

| ID | Defecto | Tipo | Estado |
|----|---------|------|--------|
| 01 | Edad negativa aceptada | Unitaria (dominio) | Abierto |
| 02 | Persona muerta aceptada | Unitaria | En progreso |
| 03 | Duplicado no detectado | Integración H2 | Abierto |
| 04 | NPE en mock mal configurado | Integración Mock | En progreso |
| 05 | HTTP 500 en género inválido | Sistema REST | Abierto |

---

## 8. Reflexión Técnica

### ¿Qué diferencias hay entre pruebas unitarias y de integración?

Las pruebas unitarias validan el dominio en aislamiento total: `Registry` no conoce base de datos
ni HTTP. Las pruebas de integración validan que los contratos entre capas se cumplen correctamente:
que `RegistryRepository` realmente persiste en H2 y que `RegistryController` transforma
correctamente el JSON a `Person`. La confianza aumenta pero también el tiempo de ejecución.

### ¿Qué capa presentó más desafíos?

La capa de infraestructura (`RegistryRepository`). El manejo de transacciones JDBC manual
(commit/rollback) y el esquema de H2 en memoria requirieron configuración precisa en `initSchema()`
y `deleteAll()` para garantizar aislamiento entre pruebas.

### ¿Cómo se simularon los componentes externos?

Con dos estrategias complementarias: H2 en memoria (reemplaza PostgreSQL/Oracle en producción
sin cambiar el código de producción) y Mockito (simula el repositorio completo para pruebas de
interacción que no requieren persistencia real).

### ¿Qué defectos se detectaron en la capa REST?

El Defecto 05: enviar `gender: "OTHER"` causaba HTTP 500 por `IllegalArgumentException` en
`Gender.valueOf("OTHER")`, que no era capturada en el controlador. La prueba de sistema lo detectó
antes del despliegue.

### ¿Qué aprendiste al ejecutar pruebas de sistema completas?

Que el comportamiento HTTP del sistema (códigos de estado, formato de respuesta, Content-Type)
solo puede validarse con pruebas de sistema. Las pruebas unitarias y de integración no pueden
detectar errores en la serialización JSON ni en el mapeo de rutas Spring.

---

## Referencias

- Koskela, L. (2013). *Effective Unit Testing*. Manning Publications.
- Martin, R. C. (2017). *Clean Architecture*. Prentice Hall.
- Spring Boot Documentation: *Testing & TestRestTemplate*. https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- Fowler, M. (2006). *Continuous Integration*. https://martinfowler.com/articles/continuousIntegration.html
- ISO/IEC/IEEE 29119 – *Software Testing Standards*.
