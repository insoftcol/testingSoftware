# Registraduría — TDD Pruebas Unitarias

Implementación del ejercicio de la **Registraduría Electoral** siguiendo la guía del taller
de pruebas unitarias con TDD (Red → Green → Refactor), patrón AAA y arquitectura limpia.

**Integrantes:** ver `integrantes.txt`  
**Materia:** Testing y Validación de Software — Universidad de La Sabana — 2026

---

## ▶️ Compilar, probar y generar cobertura

```bash
# Ejecutar pruebas
mvn clean test

# Generar reporte JaCoCo
mvn clean verify
open target/site/jacoco/index.html
```

---

## 🏗️ Estructura del proyecto

```
registraduria-tdd/
├── src/
│   ├── main/java/edu/unisabana/tyvs/domain/
│   │   ├── model/
│   │   │   ├── Gender.java          # enum: MALE, FEMALE, UNIDENTIFIED
│   │   │   ├── RegisterResult.java  # enum: VALID, DUPLICATED, INVALID, DEAD, INVALID_AGE, UNDERAGE
│   │   │   └── Person.java          # entidad: name, id, age, gender, alive
│   │   └── service/
│   │       └── Registry.java        # servicio: registerVoter(Person)
│   └── test/java/edu/unisabana/tyvs/domain/service/
│       └── RegistryTest.java        # 11 pruebas TDD (AAA + BDD)
├── defectos.md
├── integrantes.txt
├── .gitignore
└── pom.xml
```

---

## 🔴🟢🔵 Historia TDD — 8 Ciclos Red → Green → Refactor

### Ciclo 1 — Camino feliz (`shouldRegisterValidPerson`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `registerVoter(personaValida)` → prueba espera `VALID`; servicio solo tiene `return VALID` → pasa trivialmente (base para los siguientes) |
| 🟢 GREEN | Servicio retorna `VALID` directamente |
| 🔵 REFACTOR | Agregar constantes `MIN_AGE = 18`, `MAX_AGE = 120` |

**Commit:** `test: camino feliz persona valida (RED)` → `feat: skeleton registerVoter returns VALID (GREEN)`

---

### Ciclo 2 — Persona muerta (`shouldRejectDeadPerson`) — *Defecto 02*

| Fase | Acción |
|------|--------|
| 🔴 RED | `Person(alive=false)` → esperado `DEAD`, obtenido `VALID` → FALLA |
| 🟢 GREEN | `if (!p.isAlive()) return RegisterResult.DEAD;` |
| 🔵 REFACTOR | Mover validación al inicio del método |

**Commit:** `test: dead person should return DEAD (RED)` → `feat: check alive condition (GREEN)`

---

### Ciclo 3 — Persona nula (`shouldReturnInvalidWhenPersonIsNull`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `registerVoter(null)` → lanza `NullPointerException` → FALLA |
| 🟢 GREEN | `if (p == null) return RegisterResult.INVALID;` |

**Commit:** `test: null person returns INVALID (RED)` → `feat: null guard (GREEN)`

---

### Ciclo 4 — Id inválido (`shouldRejectWhenIdIsZero`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `Person(id=0)` → esperado `INVALID`, obtenido `VALID` → FALLA |
| 🟢 GREEN | `if (p.getId() <= 0) return RegisterResult.INVALID;` |
| 🔵 REFACTOR | Cubrir también `id = -5` en prueba adicional |

---

### Ciclo 5 — Edad negativa (`shouldRejectNegativeAge`) — *Defecto 01*

| Fase | Acción |
|------|--------|
| 🔴 RED | `Person(age=-1)` → esperado `INVALID_AGE`, obtenido `VALID` → FALLA |
| 🟢 GREEN | `if (p.getAge() < 0 \|\| p.getAge() > MAX_AGE) return RegisterResult.INVALID_AGE;` |
| 🔵 REFACTOR | Agregar `INVALID_AGE` al enum `RegisterResult` |

---

### Ciclo 6 — Menor de edad (`shouldRejectUnderageAt17`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `Person(age=17)` → esperado `UNDERAGE`, obtenido `VALID` → FALLA |
| 🟢 GREEN | `if (p.getAge() < MIN_AGE) return RegisterResult.UNDERAGE;` |
| 🔵 REFACTOR | Agregar `UNDERAGE` al enum; verificar límite `age=18` pasa |

---

### Ciclo 7 — Registro duplicado (`shouldRejectDuplicateId`) — *Defecto 03*

| Fase | Acción |
|------|--------|
| 🔴 RED | Segunda persona con `id=200` → esperado `DUPLICATED`, obtenido `VALID` → FALLA |
| 🟢 GREEN | Agregar `Set<Integer> registeredIds`; validar `contains` antes de `add` |
| 🔵 REFACTOR | Inicializar `registeredIds` en declaración con `new HashSet<>()` |

**Commit:** `test: duplicate id returns DUPLICATED (RED)` → `feat: add registeredIds Set for dedup (GREEN)`

---

### Ciclo 8 — Dos registros distintos (`shouldRegisterTwoDifferentPersonsWithDifferentIds`)

| Fase | Acción |
|------|--------|
| 🔴 RED | Verificar que ids diferentes no se bloquean entre sí → asegurar correctitud del `Set` |
| 🟢 GREEN | Ambas personas con ids distintos retornan `VALID` |

---

## 🧩 Patrón AAA — Ejemplo documentado

```java
@Test
public void shouldRejectDuplicateId() {

    // Arrange: dos personas con el mismo número de documento (id=200)
    Person persona1 = new Person("Carlos", 200, 30, Gender.MALE,   true);
    Person persona2 = new Person("Carla",  200, 25, Gender.FEMALE, true);

    // Act: registrar ambas en la misma sesión
    RegisterResult resultadoPrimera = registry.registerVoter(persona1);
    RegisterResult resultadoSegunda = registry.registerVoter(persona2);

    // Assert: primera pasa, segunda rechazada por duplicado
    Assert.assertEquals(RegisterResult.VALID,      resultadoPrimera);
    Assert.assertEquals(RegisterResult.DUPLICATED, resultadoSegunda);
}
```

---

## 🧮 Matriz de Clases de Equivalencia y Valores Límite

| # | Clase | Entrada representativa | Resultado esperado | Test que lo cubre |
|---|-------|----------------------|---------------------|-------------------|
| 1 | Persona nula | `null` | `INVALID` | `shouldReturnInvalidWhenPersonIsNull` |
| 2 | Id = 0 (límite inf. inválido) | `id=0` | `INVALID` | `shouldRejectWhenIdIsZero` |
| 3 | Id negativo | `id=-5` | `INVALID` | `shouldRejectWhenIdIsNegative` |
| 4 | Persona muerta | `alive=false` | `DEAD` | `shouldRejectDeadPerson` |
| 5 | Edad negativa | `age=-1` | `INVALID_AGE` | `shouldRejectNegativeAge` |
| 6 | Edad > 120 (límite sup. inválido) | `age=121` | `INVALID_AGE` | `shouldRejectInvalidAgeOver120` |
| 7 | Menor de edad (límite sup.) | `age=17` | `UNDERAGE` | `shouldRejectUnderageAt17` |
| 8 | Mayoría de edad exacta (límite inf. válido) | `age=18` | `VALID` | `shouldAcceptAdultAt18` |
| 9 | Edad máxima válida (límite sup. válido) | `age=120` | `VALID` | `shouldAcceptMaxAge120` |
| 10 | Id duplicado | `id=200` dos veces | `DUPLICATED` | `shouldRejectDuplicateId` |
| 11 | Persona válida completa | `age=30, alive=true, id único` | `VALID` | `shouldRegisterValidPerson` |

---

## 🤝 Escenarios BDD (Given – When – Then)

```gherkin
Escenario 1: Registro válido
  Given  persona viva, 30 años, id único
  When   intento registrarla
  Then   resultado es VALID

Escenario 2: Persona con edad inválida (Defecto 01)
  Given  persona viva con age=-1
  When   intento registrarla
  Then   resultado es INVALID_AGE

Escenario 3: Persona muerta (Defecto 02)
  Given  persona con alive=false
  When   intento registrarla
  Then   resultado es DEAD

Escenario 4: Registro duplicado (Defecto 03)
  Given  Carlos con id=200 ya está registrado
  When   Carla con id=200 intenta registrarse
  Then   resultado es DUPLICATED

Escenario 5: Valor límite de mayoría de edad
  Given  persona viva con age=17 (menor de edad)
  When   intento registrarla
  Then   resultado es UNDERAGE

Escenario 6: Valor límite inferior válido de edad
  Given  persona viva con age=18 (mayoría exacta)
  When   intento registrarla
  Then   resultado es VALID

Escenario 7: Valor límite superior válido de edad
  Given  persona viva con age=120 (máximo permitido)
  When   intento registrarla
  Then   resultado es VALID

Escenario 8: Valor límite superior inválido de edad
  Given  persona viva con age=121 (supera el máximo)
  When   intento registrarla
  Then   resultado es INVALID_AGE
```

---

## 📊 Cobertura de Código (JaCoCo)

```bash
mvn clean verify
open target/site/jacoco/index.html
```

La cobertura esperada es ≥ 80% global. Todas las ramas del método `registerVoter`
están cubiertas por los 11 tests, lo que garantiza una cobertura cercana al 100%
en el paquete de dominio.

---

## 🪞 Reflexión Final

### ¿Qué escenarios no se cubrieron y por qué?

- **Género**: el campo `gender` no tiene reglas de negocio definidas. Registrar con `UNIDENTIFIED` es válido para cualquier edad/estado — no hay una clase de equivalencia que lo valide de forma diferente.
- **Nombre vacío o nulo**: la guía del taller no especifica validación de nombre. En producción se debería validar, pero hacerlo aquí sería asumir requisitos no descritos.
- **Concurrencia en registros simultáneos**: el `HashSet` de ids no es thread-safe. En un sistema real se usaría `ConcurrentHashMap` o persistencia, que pertenecen a la capa de infraestructura.

### ¿Qué defectos reales detectaron los tests?

Los tres defectos del `defectos.md` son exactamente los detectados durante el ciclo TDD:
1. **Edad negativa aceptada** — crítico: en producción permitiría registrar personas con datos inconsistentes.
2. **Persona muerta aceptada** — lógica de negocio incumplida: la registraduría solo registra personas vivas.
3. **Id duplicado aceptado** — falla de integridad: permite doble inscripción del mismo documento.

### ¿Cómo mejorarías `Registry` para facilitar su prueba?

1. **Inyectar el repositorio de ids**: extraer `Set<Integer> registeredIds` a una interfaz `RepositorioVotantes`. En pruebas se pasa un mock vacío o pre-poblado según el escenario.
2. **Value Object `DocumentoIdentidad`**: encapsular `id` con su propia validación (`id > 0`). Reduce responsabilidades de `Registry`.
3. **Value Object `EdadVotante`**: encapsular `age` con rango `[0, 120]`. Las pruebas de edad serían unitarias sobre `EdadVotante`, no sobre `Registry`.
4. **Separar validación de persistencia**: método `validar(Person)` retorna resultado sin modificar estado; método `registrar(Person)` solo llama tras validar. Pruebas de validación no tienen efectos secundarios.
