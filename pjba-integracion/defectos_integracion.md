# Registro de Defectos — Pruebas de Integración y Sistema
## Proyecto: Transferencias Bancarias PJBA

Defectos detectados en pruebas unitarias, de integración (H2/Mockito) y de sistema (REST).

---

### Defecto 01 — Solicitud nula no persiste correctamente *(Integración H2)*

- **Capa afectada**: Aplicación (`ServicioTransferencia`)
- **Caso de prueba**: `procesarTransferencia(null)` / `cotizar(null)`
- **Entrada**: `null`
- **Resultado esperado**: resultado DATOS_INVALIDOS guardado en `transferencia_log`
- **Resultado obtenido**: `NullPointerException` — la tabla no recibía el registro
- **Causa probable**: El método `save()` en `TransferenciaRepository` no manejaba valores nulos en `solicitud`
- **Tipo de prueba**: Integración (H2)
- **Estado**: Resuelto — se agregaron comprobaciones de nulo en `TransferenciaRepository.save()`
- **Prioridad**: Alta

---

### Defecto 02 — Mock sin configurar lanzaba NullPointerException *(Integración Mock)*

- **Capa afectada**: Aplicación (`ServicioTransferencia`)
- **Caso de prueba**: Mock de repositorio sin método `initSchema()` configurado
- **Configuración**:
```java
repoMock = mock(TransferenciaRepositoryPort.class);
// faltaba stubbing de save() para verificar interacciones
```
- **Resultado esperado**: Verificación correcta de interacciones con el mock
- **Resultado obtenido**: Test pasaba incluso con lógica incorrecta (sin `verify()`)
- **Causa probable**: Ausencia de aserciones en el mock — los tests no eran significativos
- **Tipo de prueba**: Integración (Mockito)
- **Estado**: Resuelto — se agregaron `verify(repoMock, times(1)).save(...)` en todos los tests mock
- **Prioridad**: Media

---

### Defecto 03 — HTTP 500 por excepción no manejada *(Sistema REST)*

- **Capa afectada**: Delivery (`TransferenciaController`)
- **Caso de prueba**: POST con `tipoEnvio` con valor inválido
- **Entrada**:
```json
{"tipoEnvio": "INVALIDO", ...}
```
- **Resultado esperado**: `HTTP 400 Bad Request`
- **Resultado obtenido**: `HTTP 500 Internal Server Error` (`IllegalArgumentException` en `TipoEnvio.valueOf()`)
- **Causa probable**: Falta de manejo de excepción en el controlador al parsear el enum
- **Tipo de prueba**: Sistema (REST)
- **Estado**: Resuelto — se envolvió `TipoEnvio.valueOf()` en try-catch retornando HTTP 400
- **Prioridad**: Alta

---

## Tabla resumen

| ID | Caso | Capa | Esperado | Obtenido | Tipo | Estado | Prioridad |
|----|------|------|----------|----------|------|--------|-----------|
| 01 | Solicitud nula | Aplicación | Persistido DATOS_INVALIDOS | NullPointerException | Integración H2 | Resuelto | Alta |
| 02 | Mock sin verify | Aplicación | Test significativo | Test sin aserciones | Mock | Resuelto | Media |
| 03 | TipoEnvio inválido | Delivery | HTTP 400 | HTTP 500 | Sistema REST | Resuelto | Alta |

---

## Convenciones de Estado
- **Abierto** → Detectado, sin corrección
- **En progreso** → En análisis o corrección
- **Resuelto** → Corregido y validado con prueba verde
