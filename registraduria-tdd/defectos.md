# Registro de Defectos

Este documento recopila los defectos encontrados durante la ejecución de pruebas
unitarias del proyecto **Registraduría**.
Cada defecto está documentado con su caso de prueba, análisis y estado de resolución.

---

## Formato 1: Lista detallada (narrativa)

### Defecto 01

- **Caso de prueba**: Persona con edad -1 (edad inválida).
- **Entrada**: `Person(name="Juan", id=101, age=-1, gender=MALE, alive=true)`
- **Resultado esperado**: `INVALID_AGE`
- **Resultado obtenido**: `VALID` (implementación esqueleto retornaba VALID siempre)
- **Causa probable**: Falta de validación de edad negativa en `Registry.registerVoter`. El servicio no evaluaba el rango `[0, MAX_AGE]` antes de retornar.
- **Estado**: Resuelto — se agregó `if (p.getAge() < 0 || p.getAge() > MAX_AGE) return INVALID_AGE`

---

### Defecto 02

- **Caso de prueba**: Persona muerta.
- **Entrada**: `Person(name="Ana", id=102, age=45, gender=FEMALE, alive=false)`
- **Resultado esperado**: `DEAD`
- **Resultado obtenido**: `VALID`
- **Causa probable**: No se evaluaba la condición `alive=false`. El servicio ignoraba el atributo `alive` de la persona.
- **Estado**: Resuelto — se agregó `if (!p.isAlive()) return DEAD`

---

### Defecto 03

- **Caso de prueba**: Registro duplicado con el mismo `id`.
- **Entradas**:
  - Persona 1: `Person(name="Carlos", id=200, age=30, gender=MALE, alive=true)`
  - Persona 2: `Person(name="Carla",  id=200, age=25, gender=FEMALE, alive=true)`
- **Resultado esperado**:
  - Persona 1 → `VALID`
  - Persona 2 → `DUPLICATED`
- **Resultado obtenido**:
  - Persona 1 → `VALID`
  - Persona 2 → `VALID`
- **Causa probable**: No había verificación de `id` previamente registrado. El servicio no mantenía estado de los ids ya inscritos.
- **Estado**: Resuelto — se agregó `Set<Integer> registeredIds` y la validación `if (registeredIds.contains(p.getId())) return DUPLICATED`

---

## Formato 2: Tabla de defectos (bug tracking)

| ID | Caso de Prueba | Entrada | Resultado Esperado | Resultado Obtenido | Causa Probable | Estado |
|-----|---------------------|---------|--------------------|--------------------|----------------|--------|
| 01 | Edad inválida (-1) | `Person(id=101, age=-1, alive=true)` | `INVALID_AGE` | `VALID` | No se valida edad negativa | Resuelto |
| 02 | Persona muerta | `Person(id=102, age=45, alive=false)` | `DEAD` | `VALID` | No se evalúa `alive=false` | Resuelto |
| 03 | Registro duplicado | `Person(id=200)` + `Person(id=200)` | 1º→`VALID` 2º→`DUPLICATED` | 1º→`VALID` 2º→`VALID` | Sin verificación de id duplicado | Resuelto |

---

## Convenciones de Estado

| Estado | Significado |
|---------|-------------|
| **Abierto** | El defecto fue detectado pero no corregido. |
| **En progreso** | El defecto se encuentra en análisis o corrección. |
| **Resuelto** | El defecto fue corregido y validado mediante pruebas. |

---

## Observaciones

- Los defectos 01, 02 y 03 son los defectos originales del proyecto de ejemplo del taller.
- Todos fueron detectados mediante TDD ciclo a ciclo y corregidos con la implementación mínima.
- El proceso TDD garantizó que la corrección de cada defecto no rompiera pruebas anteriores (refactor continuo).
