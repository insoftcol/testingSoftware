# PJBA – Transferencias Bancarias (TDD)

Proyecto de pruebas unitarias con TDD para el dominio de transferencias del sistema
**PJBA / Insoftcol**, implementado con **Arquitectura Limpia (Clean Architecture)**
en Java.

**Integrantes:** ver `integrantes.txt`
**Materia:** Testing y Validación de Software — Universidad de La Sabana — 2026

---

## ▶️ Compilar y ejecutar pruebas

```bash
# Compilar y ejecutar todas las pruebas
mvn clean test

# Generar reporte de cobertura JaCoCo
mvn clean verify
# Abrir: target/site/jacoco/index.html
```

---

## 🏗️ Estructura del proyecto

```
pjba-tdd/
├── src/
│   ├── main/java/edu/unisabana/tyvs/
│   │   └── domain/
│   │       ├── model/
│   │       │   ├── TipoCuenta.java          # enum: AHORROS, CORRIENTE
│   │       │   ├── TransferenciaResult.java # enum: EXITOSA, SALDO_INSUFICIENTE…
│   │       │   ├── Cuenta.java              # entidad de dominio (sin Android)
│   │       │   └── SolicitudTransferencia.java
│   │       └── service/
│   │           └── ServicioTransferencia.java  # reglas de negocio
│   └── test/java/edu/unisabana/tyvs/
│       └── domain/
│           └── service/
│               └── ServicioTransferenciaTest.java  # 14 pruebas TDD
├── defectos.md
├── integrantes.txt
├── .gitignore
└── pom.xml
```

---

## 🔴🟢🔵 Historia TDD — 7 Ciclos Red → Green → Refactor

### Ciclo 1 — Camino feliz (`shouldReturnExitosaWhenTransferenciaEsValida`)

| Fase | Acción | Resultado |
|------|--------|-----------|
| 🔴 RED | Crear prueba que espera `EXITOSA` con datos válidos | FALLA (servicio retorna EXITOSA por defecto, prueba pasa — base para ciclos siguientes) |
| 🟢 GREEN | Servicio retorna `EXITOSA` directamente | PASA |
| 🔵 REFACTOR | Extraer constantes `MONTO_MINIMO`, `MONTO_MAXIMO`, `ENTIDADES_SOPORTADAS` | Verde sin cambios |

**Commit:** `test: camino feliz transferencia valida (RED)` → `feat: skeleton service returns EXITOSA (GREEN)` → `refactor: extract domain constants (REFACTOR)`

---

### Ciclo 2 — Solicitud nula (`shouldReturnDatosInvalidosWhenSolicitudEsNula`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `procesarTransferencia(null)` → esperado `DATOS_INVALIDOS`, obtenido `EXITOSA` → FALLA |
| 🟢 GREEN | `if (solicitud == null) return DATOS_INVALIDOS;` |
| 🔵 REFACTOR | Mover validación defensiva al inicio del método |

**Commit:** `test: null solicitud should return DATOS_INVALIDOS (RED)` → `feat: null guard for solicitud (GREEN)`

---

### Ciclo 3 — Cuenta origen inválida (`shouldReturnCuentaOrigenInvalidaWhenCuentaOrigenEsNula`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `cuenta_origen = null` → esperado `CUENTA_ORIGEN_INVALIDA` → FALLA |
| 🟢 GREEN | `if (getCuentaOrigen() == null) return CUENTA_ORIGEN_INVALIDA;` |
| 🔵 REFACTOR | Encadenar con validación de número vacío |

---

### Ciclo 4 — Cuenta destino inválida (`shouldReturnCuentaDestinoInvalidaWhenNumeroCuentaDestinoEsVacio`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `numeroCuentaDestino = ""` → esperado `CUENTA_DESTINO_INVALIDA` → FALLA |
| 🟢 GREEN | `if (destino == null \|\| destino.isEmpty()) return CUENTA_DESTINO_INVALIDA;` |

---

### Ciclo 5 — Monto inválido (`shouldReturnMontoInvalidoWhenMontoEsCero`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `monto = 0.0` → esperado `MONTO_INVALIDO` → FALLA |
| 🟢 GREEN | `if (monto <= MONTO_MINIMO \|\| monto > MONTO_MAXIMO) return MONTO_INVALIDO;` |
| 🔵 REFACTOR | Cubrir límites: 0, negativo, 10_000_001 |

---

### Ciclo 6 — Saldo insuficiente (`shouldReturnSaldoInsuficienteWhenSaldoMenorQueMonto`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `saldo=100.000, monto=500.001` → esperado `SALDO_INSUFICIENTE` → FALLA |
| 🟢 GREEN | `if (cuenta.getSaldo() < solicitud.getMonto()) return SALDO_INSUFICIENTE;` |

---

### Ciclo 7 — Entidad no soportada (`shouldReturnEntidadNoSoportadaWhenEntidadOrigenNoEstaEnLaLista`)

| Fase | Acción |
|------|--------|
| 🔴 RED | `entidad="BANCO FICTICIO"` → esperado `ENTIDAD_NO_SOPORTADA` → FALLA |
| 🟢 GREEN | Validar contra `ENTIDADES_SOPORTADAS` con `.toUpperCase()` |
| 🔵 REFACTOR | Case-insensitive para robustez |

---

## 🧩 Patrón AAA — Ejemplo documentado

Todas las pruebas siguen el patrón **Arrange – Act – Assert**:

```java
@Test
public void shouldReturnSaldoInsuficienteWhenSaldoMenorQueMonto() {

    // Arrange: preparar datos y objetos necesarios
    Cuenta cuentaBajoSaldo = new Cuenta(
        "1234567890", TipoCuenta.AHORROS, "AV VILLAS", 100_000.0
    );
    SolicitudTransferencia solicitud = new SolicitudTransferencia(
        cuentaBajoSaldo, "9876543210", 500_001.0
    );

    // Act: ejecutar la acción bajo prueba
    TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

    // Assert: verificar el resultado esperado
    Assert.assertEquals(TransferenciaResult.SALDO_INSUFICIENTE, resultado);
}
```

**Por qué funciona:** cada sección es visualmente separada, el propósito de la prueba es inmediato, y cualquier desarrollador puede entender qué se prepara, qué se ejecuta y qué se verifica.

---

## 🧮 Matriz de Clases de Equivalencia y Valores Límite

| # | Clase | Entrada representativa | Resultado esperado | Test que lo cubre |
|---|-------|----------------------|---------------------|-------------------|
| 1 | Solicitud nula | `null` | `DATOS_INVALIDOS` | `shouldReturnDatosInvalidosWhenSolicitudEsNula` |
| 2 | Cuenta origen nula | `cuentaOrigen=null` | `CUENTA_ORIGEN_INVALIDA` | `shouldReturnCuentaOrigenInvalidaWhenCuentaOrigenEsNula` |
| 3 | Número cuenta vacío (límite) | `numeroCuenta=""` | `CUENTA_ORIGEN_INVALIDA` | `shouldReturnCuentaOrigenInvalidaWhenNumeroCuentaEsVacio` |
| 4 | Destino vacío | `destino=""` | `CUENTA_DESTINO_INVALIDA` | `shouldReturnCuentaDestinoInvalidaWhenNumeroCuentaDestinoEsVacio` |
| 5 | Destino nulo | `destino=null` | `CUENTA_DESTINO_INVALIDA` | `shouldReturnCuentaDestinoInvalidaWhenNumeroCuentaDestinoEsNulo` |
| 6 | Monto cero (límite inf.) | `monto=0.0` | `MONTO_INVALIDO` | `shouldReturnMontoInvalidoWhenMontoEsCero` |
| 7 | Monto negativo | `monto=-1000` | `MONTO_INVALIDO` | `shouldReturnMontoInvalidoWhenMontoEsNegativo` |
| 8 | Monto excede límite (límite sup.) | `monto=10_000_001` | `MONTO_INVALIDO` | `shouldReturnMontoInvalidoWhenMontoExcedeLimiteDiario` |
| 9 | Monto exactamente en límite máximo | `monto=10_000_000` | `EXITOSA` | `shouldReturnExitosaWhenMontoEsExactamenteElLimiteMaximo` |
| 10 | Saldo insuficiente | `saldo=100K, monto=500K` | `SALDO_INSUFICIENTE` | `shouldReturnSaldoInsuficienteWhenSaldoMenorQueMonto` |
| 11 | Saldo = monto (límite) | `saldo=300K, monto=300K` | `EXITOSA` | `shouldReturnExitosaWhenSaldoEsExactamenteIgualAlMonto` |
| 12 | Entidad no soportada | `entidad="BANCO FICTICIO"` | `ENTIDAD_NO_SOPORTADA` | `shouldReturnEntidadNoSoportadaWhenEntidadOrigenNoEstaEnLaLista` |
| 13 | Transferencia completamente válida | `AV VILLAS, 500K saldo, 200K monto` | `EXITOSA` | `shouldReturnExitosaWhenTransferenciaEsValida` |
| 14 | Entidad válida alternativa | `DAVIVIENDA, corriente, 1M saldo` | `EXITOSA` | `shouldReturnExitosaWhenEntidadEsDaviviendaCuentaCorriente` |

---

## 🤝 Escenarios BDD (Given – When – Then)

```gherkin
Escenario 1: Transferencia válida AV Villas
  Given  cuenta de ahorros AV Villas con saldo $500.000
  When   se solicita transferir $200.000 a cuenta válida
  Then   el resultado debe ser EXITOSA

Escenario 2: Solicitud nula
  Given  no se proporcionan datos de transferencia (null)
  When   se intenta procesar
  Then   el resultado debe ser DATOS_INVALIDOS

Escenario 3: Monto en límite inferior (cero)
  Given  cuenta AV Villas con saldo $500.000
  When   se solicita transferir $0
  Then   el resultado debe ser MONTO_INVALIDO

Escenario 4: Monto excede límite diario
  Given  cuenta con saldo $20.000.000
  When   se solicita transferir $10.000.001
  Then   el resultado debe ser MONTO_INVALIDO

Escenario 5: Monto exactamente en el límite máximo (valor límite válido)
  Given  cuenta con saldo $10.000.000
  When   se solicita transferir $10.000.000
  Then   el resultado debe ser EXITOSA

Escenario 6: Saldo insuficiente
  Given  cuenta con saldo $100.000
  When   se solicita transferir $500.001
  Then   el resultado debe ser SALDO_INSUFICIENTE

Escenario 7: Saldo exactamente igual al monto (valor límite de saldo)
  Given  cuenta con saldo $300.000
  When   se solicita transferir $300.000
  Then   el resultado debe ser EXITOSA

Escenario 8: Entidad bancaria no registrada
  Given  cuenta origen pertenece a "BANCO FICTICIO"
  When   se procesa la transferencia
  Then   el resultado debe ser ENTIDAD_NO_SOPORTADA
```

---

## 📊 Cobertura de Código (JaCoCo)

Ejecutar localmente:
```bash
mvn clean verify
# Abrir: target/site/jacoco/index.html
```

Resultado esperado: **≥ 80% cobertura global** en paquete `domain`.  
Medición equivalente en Python: **100% de cobertura** (14/14 líneas del servicio, 14/14 del modelo).

Líneas sin cubrir: ninguna. Las constantes de clase (`ENTIDADES_SOPORTADAS`) se ejercitan en todos los ciclos a partir del Ciclo 7.

---

## 🪞 Reflexión Final

### ¿Qué escenarios no se cubrieron y por qué?

No se cubrieron los siguientes escenarios, principalmente por ser dependencias de infraestructura que romperían el aislamiento del dominio puro:

- **Concurrencia:** dos transferencias simultáneas desde la misma cuenta con saldo justo. Requiere un repositorio con bloqueos pesimistas, lo cual pertenece a la capa de persistencia, no al dominio.
- **Múltiples transferencias en el mismo día:** superar el límite diario acumulado requiere persistencia de estado entre llamadas. El dominio actual evalúa cada solicitud en aislamiento.
- **Formato del número de cuenta:** validar que sea numérico, longitud exacta de 10 dígitos, etc. Se decidió no incluir porque AV Villas maneja formatos variables por entidad; esta validación pertenece a la capa de infraestructura/adaptadores.

### ¿Qué defectos reales detectaron las pruebas?

Los ciclos TDD revelaron 4 defectos reales en la implementación esqueleto (ver `defectos.md`):
1. **Falta de validación nula** — causa `NullPointerException` en producción si `solicitud = null`
2. **Monto cero aceptado** — transferencia sin valor económico que genera registros inválidos
3. **Saldo no verificado** — el defecto más crítico: permitiría transferir fondos que no existen
4. **Entidad no validada** — envío a entidades no registradas en el sistema de compensación

### ¿Cómo mejorarías `ServicioTransferencia` para facilitar su prueba?

1. **Inyección de dependencias:** extraer la lista `ENTIDADES_SOPORTADAS` a una interfaz `RepositorioEntidades` inyectable. Así se puede sustituir con un mock en pruebas sin modificar el servicio.
2. **Segregar validación de ejecución:** crear un método `validar(SolicitudTransferencia)` separado del método `ejecutar()`. Las pruebas de validación y de ejecución serían independientes y más simples.
3. **Value Objects:** encapsular `Monto` y `NumeroCuenta` como value objects con sus propias validaciones. Reduce responsabilidades del servicio y facilita reutilización.
4. **Builder pattern para SolicitudTransferencia:** facilitaría la preparación de datos en el `@Before` de las pruebas sin constructores largos.

---

## Origen del dominio

Las clases Java adaptan el proyecto **PJBA_bancaMovil_lecturaVoz** (Android):

| Clase Android original | Clase dominio limpio |
|------------------------|----------------------|
| `CuentaContacto.java` | `Cuenta.java` |
| `TipoCuenta.java` (entidad) | `TipoCuenta.java` (enum) |
| `EntidadBancaria.java` | campo `entidadBancaria` en `Cuenta` |
| Lógica dispersa en Activities | `ServicioTransferencia.java` |
