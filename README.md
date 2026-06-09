# Testing y Validación de Software — INSOFTCOL S.A.

**Autores:** Fredy Orlando Pulido Quintero · Myriam Andrea Martinez Fontecha  
**Materia:** Testing y Validación de Software  
**Programa:** Maestría en Arquitectura de Software — Universidad de La Sabana  
**Año:** 2026

---

## 📦 Unidad 3 — Pruebas Unitarias con TDD

Aplicación del ciclo **Red → Green → Refactor** sobre dominios reales,
usando **JUnit 4**, **AAA**, **BDD**, clases de equivalencia y valores límite.
Java puro, sin frameworks externos.

| Carpeta | Dominio | Descripción |
|---------|---------|-------------|
| [`pjba-tdd/`](./pjba-tdd) | Transferencias Bancarias AV Villas | **Proyecto** — 14 pruebas unitarias, 7 ciclos TDD |
| [`registraduria-tdd/`](./registraduria-tdd) | Registraduría Electoral | **Taller** — 11 pruebas, ciclos TDD documentados |

```bash
# Ejecutar cualquier proyecto Unidad 3
cd pjba-tdd
mvn clean verify
open target/site/jacoco/index.html
```

---

## 🔗 Unidad 4 — Pruebas de Integración y Sistema con CI/CD

Extensión de los dominios de la Unidad 3 con **Spring Boot**,
capa de persistencia **H2/JDBC**, endpoints **REST** y pipeline **CI/CD**.
Pruebas en tres niveles: H2 (integración real), Mockito (contratos) y
TestRestTemplate (sistema/caja negra).

| Carpeta | Dominio | Descripción |
|---------|---------|-------------|
| [`taller-integracion/`](./taller-integracion) | Registraduría Electoral | **Taller** — Spring Boot + H2 + Mockito + TestRestTemplate |
| [`pjba-integracion/`](./pjba-integracion) | Transferencias Bancarias | **Proyecto** — audit log, 9 pruebas integración + sistema |
| [`logistica-integracion/`](./logistica-integracion) | Cotizador Logístico | **Proyecto** — cotización entre ciudades, 8 pruebas |

```bash
# Ejecutar cualquier proyecto Unidad 4
cd pjba-integracion
mvn clean verify
open target/site/jacoco/index.html

# Levantar API REST
mvn spring-boot:run
```

### Endpoints REST disponibles

| Proyecto | Endpoint | JSON de prueba |
|----------|----------|----------------|
| `taller-integracion` | `POST /register` | `{"name":"Ana","id":100,"age":30,"gender":"FEMALE","alive":true}` |
| `pjba-integracion` | `POST /transferencia` | `{"numeroCuentaOrigen":"1234567890","tipoCuenta":"AHORROS","entidadBancaria":"AV VILLAS","saldoOrigen":500000,"numeroCuentaDestino":"9876543210","monto":200000}` |
| `logistica-integracion` | `POST /cotizacion` | `{"ciudadOrigen":"BOGOTA","ciudadDestino":"MEDELLIN","pesoKg":10.0,"tipoEnvio":"NORMAL"}` |

---

## ⚙️ Pipeline CI/CD

El archivo [`.github/workflows/ci.yml`](./.github/workflows/ci.yml) ejecuta
`mvn clean verify` en los tres proyectos de Unidad 4 en paralelo.

```
Push / PR → main o develop
     ↓
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│ Taller Registraduría │  │ PJBA Transferencias  │  │ Logística Cotizador  │
│  mvn clean verify    │  │  mvn clean verify    │  │  mvn clean verify    │
│  JaCoCo artifact     │  │  JaCoCo artifact     │  │  JaCoCo artifact     │
└──────────┬───────────┘  └──────────┬───────────┘  └──────────┬───────────┘
           └──────────────────────────┼──────────────────────────┘
                                      ↓
                     ┌─────────────────────────────────┐
                     │  Pipeline completo — OK para merge  │
                     │  (Required check — bloquea merge)   │
                     └─────────────────────────────────┘
```

**Restricción de integración:** el Ruleset `main` requiere que
`Pipeline completo — OK para merge` sea verde antes de permitir cualquier merge.

---

## 📋 Registro de Defectos

| Proyecto | Archivo | Tipo |
|----------|---------|------|
| `pjba-tdd` | [`pjba-tdd/defectos.md`](./pjba-tdd/defectos.md) | Unitarias |
| `registraduria-tdd` | [`registraduria-tdd/defectos.md`](./registraduria-tdd/defectos.md) | Unitarias |
| `taller-integracion` | [`taller-integracion/defectos.md`](./taller-integracion/defectos.md) | Integración |
| `pjba-integracion` | [`pjba-integracion/defectos_integracion.md`](./pjba-integracion/defectos_integracion.md) | Integración + Sistema |
| `logistica-integracion` | [`logistica-integracion/defectos_integracion.md`](./logistica-integracion/defectos_integracion.md) | Integración + Sistema |

---

## 📊 Métricas de Calidad

| Métrica | Unidad 3 | Unidad 4 |
|---------|----------|----------|
| Cobertura JaCoCo | ≥ 80% | ≥ 80% |
| Pruebas unitarias | 14 (pjba) + 11 (registraduría) | — |
| Pruebas integración (H2 + Mock) | — | 6 por proyecto |
| Pruebas sistema (IT/REST) | — | 3 por proyecto |
| Pipeline CI/CD | — | ✅ GitHub Actions |
| Restricción merge | — | ✅ Branch Protection |
| Errores HTTP 500 sin manejar | — | 0 |
