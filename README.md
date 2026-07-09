# ISEMID Platform

Production-grade modular backend for **ISEMID** — Uzbekistan's epidemiological
surveillance and case-management system. It digitizes Form No. 058
(infectious/parasitic disease notification), the five epidemiological
investigation card types issued against a form, patient registration, and the
lab/procedure ("act") workflow attached to a card.

Rebuilt from a legacy codebase (`uz.uzinfocom.isemid`) module-by-module, using
the legacy source purely as a domain/field specification — architecture and
known legacy defects (unbounded cascades, nullable required FKs, primitive
optional fields, entity/DTO mixing) are deliberately **not** carried over.

## Tech stack

| Layer | Choice | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 4.0.6 |
| Modularity | Spring Modulith | 2.0.6 |
| Persistence | Spring Data JPA / Hibernate ORM | (Spring Boot managed) |
| Database | PostgreSQL | runtime |
| Schema management | `hibernate.ddl-auto=update` | — no Flyway/Liquibase in this project |
| Object mapping | MapStruct | 1.6.3 |
| API docs | springdoc-openapi (Swagger UI) | 3.0.3 |
| Caching | Caffeine (Spring Cache abstraction) | — |
| Security | Spring Security, OAuth2 resource server + client (JWT) | — |
| Observability | Micrometer, OpenTelemetry tracing, Prometheus, structured JSON logs (Logstash encoder) | 8.0 (encoder) |
| Module boundary enforcement | ArchUnit | 1.3.0 |
| Build | Maven (wrapper included: `./mvnw`) | — |

Root artifact: `uz.uzinfocom:isemid-platform:1.0.0-SNAPSHOT` (single Maven
module — `pom.xml` at the repo root, no multi-module reactor).

## Architecture

Package root: `uz.uzinfocom.app`. Business capabilities live under
`modules/*`, each following the same layered shape:

```
modules/<name>/
├── domain/          entities, enums, domain-only exceptions
├── application/     command/query services, mappers, validators, handlers
├── infrastructure/   JPA repositories, specifications  (where present)
└── web/              controllers, request/response DTOs
```

Shared, cross-cutting code lives under `platform/*` (security, i18n,
observability, caching, persistence base classes, scope resolution) and
`shared/*` (API path constants, common DTOs). `integration/api2` holds the
outbound HTTP client integration with the upstream API2 system.

Module boundaries are enforced at test time by ArchUnit
(`CardModuleBoundaryTest`, `EntityNameUniquenessTest`), not just by
convention.

### Modules

| Module | What it represents | REST root | Docs |
|---|---|---|---|
| `form058` | The epidemiological notification form itself — the aggregate root the other modules attach to. Approval/cancellation workflow (`NOT_APPROVED → SENT → RECEIVED → CARD_LINKED → APPROVED_PENDING → APPROVED/CANCELED`). | `/v1/form-058` | [docs/form058-module.md](docs/form058-module.md) |
| `card` | The five epidemiological investigation card types (CARD161, CARD174, CARD175, CARD205, CARD_TUBE), each attached to one `form058`. Full accept/reject/complete/supervisor-approval lifecycle. | `/v1/cards`, plus `/v1/form-058/{id}/cards*` | [docs/card-module.md](docs/card-module.md) |
| `patient` | Patient/person record (demographics, national ID, addresses, workplace/school affiliation), created as a side effect of registering a `form058`. No REST surface of its own. | — | [docs/patient-module.md](docs/patient-module.md) |
| `act` | Lab/procedure order attached to a card. Deliberately a minimal placeholder — the legacy system's 6 act subtypes are out of scope for this build. | — | [docs/act-module.md](docs/act-module.md) |

Shared platform infrastructure (security, i18n, observability, ...) is
documented in [docs/platform.md](docs/platform.md).

## Running locally

Requires JDK 21 and a PostgreSQL instance. Environment variables (all
optional, sensible defaults in `application.properties`):

```
SERVER_PORT              default 8081
DB_POOL_MAX / DB_POOL_MIN_IDLE
ASYNC_CORE_POOL_SIZE / ASYNC_MAX_POOL_SIZE / ASYNC_QUEUE_CAPACITY
TRACING_SAMPLING_PROBABILITY
```

Datasource credentials are supplied via the standard Spring Boot
`spring.datasource.*` properties (see `application-dev.properties` /
`application-prod.properties` for the active profile's overrides).

```bash
./mvnw spring-boot:run                 # dev profile
./mvnw clean package                    # produces target/app.jar (finalName=app)
```

Once running:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api-docs`
- Actuator: `http://localhost:8081/v1/actuator` (health/info/prometheus/metrics
  are public; `loggers` requires authentication)

## Testing

```bash
./mvnw test                              # full suite
./mvnw test -Dtest="uz.uzinfocom.app.modules.card.**"   # a single module
```

No Testcontainers/`@DataJpaTest` setup exists yet — tests are unit-level
(handlers, services, mappers exercised directly against real MapStruct
implementations, not mocks, wherever practical) plus ArchUnit boundary
checks. Schema correctness against a real Postgres has only been verified by
running the application, not by an automated integration test.

## Internationalization

Message bundles: `src/main/resources/i18n/messages*.properties`. Supported
locales: Uzbek Latin (`uz`, default), Uzbek Cyrillic (`uz-Cyrl`),
Karakalpak (`kaa`), Russian (`ru`), English (`en`).

## Conventions

- No Lombok `@Builder`/`@SuperBuilder` on entity hierarchies where a builder
  would let a caller set an invariant field (e.g. a discriminator) that must
  otherwise be fixed per subclass.
- Entities carry no Jackson annotations — polymorphic JSON (sealed
  request/response interfaces with `@JsonTypeInfo`) lives entirely in the DTO
  layer.
- New enum-driven business rules are expressed as methods on the enum itself
  (`canBeUpdated()`, `isFinal()`, ...) rather than scattered `if` conditions
  in services.
