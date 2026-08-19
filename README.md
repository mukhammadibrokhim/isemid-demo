# ISEMID Platform

**ISEMID** (Infectious/parasitic disease Surveillance and Epidemiological
Monitoring Information Database) is the backend platform for Uzbekistan's
national epidemiological surveillance and case-management system. It
digitizes the full lifecycle of an infectious-disease case: the initial
notification (Form No. 058 and Form No. 058-1), the five epidemiological
investigation card types issued against it, patient registration, the
lab/procedure ("act") workflow, and the statistical reporting built on top of
all of it — for medical organizations, sanitary-epidemiological ("sanepid")
services, and their supervising administration, nationwide.

Rebuilt from the ground up from a legacy codebase (`uz.uzinfocom.isemid`),
module by module — the legacy source was used purely as a domain/field
specification. Architecture and known legacy defects (unbounded cascades,
nullable required foreign keys, primitive optional fields, entity/DTO
mixing, god-entities) are deliberately **not** carried over; see each
module's "legacy fixes" notes in [`docs/`](docs) for specifics.

## Contents

- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Modules](#modules)
- [Platform capabilities](#platform-capabilities)
- [Integrations](#integrations)
- [API surface](#api-surface)
- [Getting started](#getting-started)
- [Database & migrations](#database--migrations)
- [Testing](#testing)
- [Internationalization](#internationalization)
- [Security](#security)
- [Observability](#observability)
- [CI/CD](#cicd)
- [Conventions](#conventions)
- [Documentation index](#documentation-index)

## Tech stack

| Layer | Choice | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 4.0.6 |
| Modularity | Spring Modulith | 2.0.6 |
| Persistence | Spring Data JPA / Hibernate ORM | Spring Boot managed |
| Database | PostgreSQL | runtime |
| Schema management | Liquibase (`spring-boot-starter-liquibase`, XML changelogs, `<includeAll>`) | — |
| Object mapping | MapStruct | 1.6.3 |
| API docs | springdoc-openapi (Swagger UI) | 3.0.3 |
| Caching | Caffeine (Spring Cache abstraction) | — |
| Security | Spring Security, OAuth2 resource server + client (JWT), custom login-proxy | — |
| Resilience | Resilience4j (circuit breakers on every outbound integration) | 2.3.0 |
| Excel export | Apache POI (streaming SXSSF writer) | 5.5.1 |
| Observability | Micrometer, Prometheus, structured JSON console logs (Logstash encoder) | 8.0 (encoder) |
| Module boundary enforcement | ArchUnit | 1.3.0 |
| Build | Maven (wrapper included: `./mvnw`) | — |

Root artifact: `uz.uzinfocom:isemid-platform:1.0.0-SNAPSHOT` — a single
Maven module (`pom.xml` at the repo root, no multi-module reactor).

## Architecture

A modular monolith. Package root: `uz.uzinfocom.app`. Business capabilities
live under `modules/*`, each following the same layered shape:

```
modules/<name>/
├── domain/          entities, enums, domain-only exceptions
├── application/     command/query services, mappers, validators, handlers
├── infrastructure/  JPA repositories, specifications  (where present)
└── web/             controllers, request/response DTOs
```

Shared, cross-cutting code lives under `platform/*` (security, ssoproxy
login-proxy, observability, caching, audit, export, settings, i18n,
persistence base classes, IAM) and `shared/*` (API path constants, common
DTOs, pagination, validation). Code that legitimately needs to read across
module boundaries — not infrastructure, but not a business capability of
its own either — lives under `orchestration/*` (dashboard, notification,
webhook, scope resolution). `integration/*` holds the outbound clients to
external systems (API2, LIS) and the inbound API surface for external
systems submitting data into ISEMID.

`platform` must never depend on `modules` — enforced at test time by
ArchUnit (`PlatformModuleBoundaryTest`), with one narrow, deliberate
exception: `modules.reference` is a dependency-free, business-logic-free
reference-data lookup module (the same shape as `platform.iam` itself),
so `platform.iam`'s organization-table mapper is allowed to resolve
region/district display names through it. `CardModuleBoundaryTest` and
`EntityNameUniquenessTest` enforce two further rules (no dependency on the
legacy `uz.uzinfocom.isemid` codebase; no Hibernate entity-name
collisions) — all three fail the build instead of surfacing at runtime.

## Modules

| Module | What it represents | REST root | Docs |
|---|---|---|---|
| `form058` | Form No. 058 — the infectious/parasitic disease notification sent between medical organizations. The aggregate root the `card` module attaches to and the trigger for `patient` registration. Full accept/approve/cancel/reopen workflow (`SENT → ACCEPTED → CARD_LINKED → APPROVED`, or `→ CANCELED`). | `/v1/form-058` | [docs/form058-module.md](docs/form058-module.md) |
| `form0581` | Form No. 058-1 — the counterpart notification for zoonotic/animal-bite cases, with its own accept/approve/cancel/reopen workflow and stats. Only `CARD174`/`CARD175`/`CARD205` may attach to it. | `/v1/form-058-1` | [docs/form058-data-dictionary.md](docs/form058-data-dictionary.md) |
| `card` | Five epidemiological investigation card types (`CARD161`, `CARD174`, `CARD175`, `CARD205`, `CARD_TUBE`), each attached to exactly one owning case — a `form058` or a `form0581`, never both. Full accept/reject/complete/supervisor-approval lifecycle, per-type handler registry, generic child-collection upsert (`ChildCollectionSync`). | `/v1/cards`, plus `/v1/form-058/{id}/cards*` and `/v1/form-058-1/{id}/cards*` | [docs/card-module.md](docs/card-module.md) |
| `patient` | Patient/person record (demographics, national ID, addresses, workplace/school affiliation), created as a side effect of registering a `form058`. No REST surface of its own. | — | [docs/patient-module.md](docs/patient-module.md) |
| `act` | Lab/procedure order attached to a card, integrated with the external LIS (Laboratory Information System). Deliberately a minimal placeholder — the legacy system's 6 act subtypes are out of scope for this build. | `/v1/acts` | [docs/act-module.md](docs/act-module.md) |
| `report` | Cross-form, organization-hierarchy statistical reports (Forms 1, 2, 3-1, 3-2, 4, 6) built on `form058`/`form0581` data plus hand-entered counts that have no other source of truth. Every drill-down report shares one hierarchy pattern (republic → region → district → organization, one level per call) via `report.shared.ReportHierarchyService`. | `/v1/reports/**` | — |
| `reference` | Reference data: countries, regions, districts, neighborhoods, ICD-10 codes, generic catalogs. Dependency-free — every other module (and, narrowly, `platform.iam`) may read from it, it reads from nothing. | `/v1/references` | — |

## Platform capabilities

Cross-cutting infrastructure under `platform/*`, each with its own
admin/API surface where applicable. Never depends on `modules/*` (see
Architecture above):

| Package | Responsibility |
|---|---|
| `security` | Inbound JWT validation (OAuth2 resource server, multi-provider), organization-scope resolution, dynamic runtime-editable route-access policies. |
| `ssoproxy` | Login-proxy: exchanges an authorization code (PKCE) or refresh token for an access token by calling an external SSO/DHP provider on the caller's behalf — user credentials never reach this backend. |
| `iam` | `User`, `Organization`, `Role`, `Permission`, `Action` domain and admin CRUD, synced against the upstream SSO/DHP identity provider. |
| `audit` | Read-only audit trail of business events (creation, status change, reassignment) across `form058`/`form0581`/`card`/`act`. |
| `export` | Generic background Excel export (streaming SXSSF) shared by every exportable module — submission stays per-module, "my files"/progress/download is one shared surface with SSE progress. |
| `settings` | Runtime-editable system settings and route-access policies (HTTP client tuning, circuit-breaker thresholds, webhook backoff, ...) — no redeploy needed to change them. |
| `integrationclient` | Provisioning and lifecycle (revoke, allowed IPs, webhook config) for machine clients that call the inbound integration API. |
| `devpanel` | A separate, HTTP-Basic-authenticated ops panel (`/v1/dev/**`) — error/login/request history, live metrics via SSE, dev-only user/settings management — deliberately outside the SSO admin model. |
| `i18n` | Message bundles for `uz` (default), `uz-Cyrl`, `kaa`, `ru`, `en`. |
| `observability` | Trace-ID propagation, inbound/outbound HTTP logging with sensitive-field redaction, a dedicated async executor pool. |
| `resilience` | Resilience4j circuit-breaker configuration for every outbound integration (API2, LIS, SSO/DHP login, RSA public key, outbound webhooks), tunable at runtime from `settings`. |
| `cache` | Caffeine-backed `@Cacheable` setup for security/user/organization lookups. |

## Orchestration capabilities

Cross-module read/fan-out code under `orchestration/*` — deliberately kept
out of `platform/*` because it reads across module boundaries by nature
(status-change fan-out, cross-module dashboards), which infrastructure
proper should never do:

| Package | Responsibility |
|---|---|
| `notification` | In-app notifications (form received/acknowledged/canceled, card/act assigned, LIS response, export ready), fanned out in-process from the same domain events the audit trail consumes — list, unread badge, mark-read, SSE stream. |
| `webhook` | Outbound status-change webhooks to registered integration clients, with retry/backoff scheduling and dispatch-history visibility. |
| `dashboard` | Single-organization summary widgets aggregating across `act`/`card`/`form058`/`form0581` (as opposed to `report`'s drill-down tables). |
| `scope` | Organization/region scope resolution, including form-visibility scope derived from `patient` affiliations. |

## Integrations

| Direction | System | Purpose |
|---|---|---|
| Outbound | **API2** | Citizen and legal-entity (TIN) lookups by national ID, used to prefill patient/organization data. |
| Outbound | **LIS** (Laboratory Information System) | Sends lab/procedure orders (`act`) out and receives results back via callback. |
| Outbound | **SSO / DHP** | Identity providers — inbound JWT validation plus the outbound login-proxy exchange. |
| Inbound | **Integration API** (`/integration/v1/**`) | A fully separate, non-`/v1` namespace where external systems (labs, HIS, other registries, DMED) submit `form058`/`form058-1` data directly, authenticated as a registered integration client (API key / Basic / IP allowlist), or pull back a patient's latest case data. |
| Outbound | **Webhooks** | Registered integration clients can subscribe to status-change events on the records they submitted, with signed payloads and automatic retry. |

## API surface

All frontend-facing endpoints are versioned under `/v1`. Highlights:

| Area | Root |
|---|---|
| Form No. 058 | `/v1/form-058` |
| Form No. 058-1 | `/v1/form-058-1` |
| Cards | `/v1/cards` |
| Acts | `/v1/acts` |
| Reports (Forms 1, 2, 3-1, 3-2, 4, 6) | `/v1/reports/**` |
| Dashboard | `/v1/dashboard` |
| Notifications | `/v1/notifications` |
| Exports | `/v1/exports` |
| Reference data | `/v1/references` |
| Auth (login-proxy) | `/v1/auth` |
| Users / Organizations / Roles / Permissions / Actions / Scopes | `/v1/users`, `/v1/organizations`, `/v1/roles`, `/v1/permissions`, `/v1/actions`, `/v1/scopes` |
| Admin (settings, route policies, integration clients, audit, admin-scoped stats) | `/v1/admin/**` |
| Dev/ops monitoring panel (separate auth model) | `/v1/dev/**` |
| Inbound integration API (external system submissions) | `/integration/v1/**` |

The full, authoritative contract is `ApiPaths`
([`src/main/java/uz/uzinfocom/app/shared/constants/api/ApiPaths.java`](src/main/java/uz/uzinfocom/app/shared/constants/api/ApiPaths.java))
and the live Swagger UI once the app is running.

## Getting started

### Prerequisites

- JDK 21
- PostgreSQL (any recent version reachable at `spring.datasource.*`)

### Environments

Three Spring profiles, each with its own `application-{profile}.properties`
and `.env.{profile}.example` template:

| Profile | Config file | Purpose | Committed? |
|---|---|---|---|
| `local` | `application-local.properties` | Your own machine — a dedicated `isemid_local` database on whatever Postgres you already have running, verbose logging, permissive CORS. Activated together with `dev` (`dev,local`) so the SSO/DHP/API2/LIS test-environment defaults still apply. | No — gitignored, personal (see below) |
| `dev` | `application-dev.properties` | Shared dev/test target (used both for a plain local run and by CI's `deploy_dev` on the dev server) — points at `test-sso.ssv.uz`, `test-api2.ssv.uz`, `test-lis.sanepid.uz`, etc. | Yes |
| `prod` | `application-prod.properties` | Production. No built-in fallbacks for secrets/URLs — everything required must come from real env vars. | Yes |

All three load `.env` (if present, via `springboot4-dotenv`) into the Spring
`Environment` on startup — a real OS/container env var of the same name
always wins over `.env`. Copy the matching template and fill in as needed:

```bash
cp .env.local.example .env      # local profile (see "Local setup" below)
# or
cp .env.example .env            # dev profile
# or
cp .env.prod.example .env       # prod profile
```

`application-local.properties` itself is **not** committed (it's in
`.gitignore`, alongside `application-local.yml`/`.yaml`) — it's meant to be
your own per-developer file. This repo ships one on first checkout for
convenience; if it's missing, recreate it with your local DB/port/CORS
overrides (everything else — SSO/DHP/API2/LIS — is inherited from the `dev`
profile since `SPRING_PROFILES_ACTIVE=dev,local` activates both).

#### Local setup

No container needed — just an empty database on your existing PostgreSQL
instance (Liquibase creates the schema on first run):

```bash
createdb isemid_local            # or: psql -c "CREATE DATABASE isemid_local"
cp .env.local.example .env       # adjust DB_URL/DB_USERNAME/DB_PASSWORD if your
                                  # local Postgres isn't on localhost:5432/postgres
./mvnw spring-boot:run
```

### Configuration

Environment variables (all optional — defaults live in
`application.properties` / `application-dev.properties` /
`application-prod.properties` / `application-local.properties`):

```
SERVER_PORT                                  default 8081
DB_URL / DB_USERNAME / DB_PASSWORD
DB_POOL_MAX / DB_POOL_MIN_IDLE
ASYNC_CORE_POOL_SIZE / ASYNC_MAX_POOL_SIZE / ASYNC_QUEUE_CAPACITY
APP_CORS_ALLOWED_ORIGINS
SSO_* / DHP_*                                identity-provider endpoints and client credentials
API2_* / LIS_*                               outbound integration endpoints, timeouts, credentials
WEBHOOK_SECRET_KEY / WEBHOOK_*                outbound webhook signing key and retry tuning
EXPORT_STORAGE_DIR / EXPORT_MAX_ROWS / EXPORT_RETENTION_DAYS / EXPORT_CLEANUP_CRON
DOCS_DIR
```

Secrets (client secrets, API keys, webhook signing keys) are supplied via
environment variables per deployment — the values checked into
`application-dev.properties` are dev/test-environment placeholders only.

### Run

```bash
./mvnw spring-boot:run                                          # SPRING_PROFILES_ACTIVE from .env (defaults to whatever you copied above)
SPRING_PROFILES_ACTIVE=dev,local ./mvnw spring-boot:run          # explicit override, no .env needed
./mvnw clean package                                             # produces target/app.jar (finalName=app)
java -jar target/app.jar
```

Once running:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api-docs`
- Actuator: `http://localhost:8081/v1/actuator`
  (`health`/`info`/`prometheus`/`metrics` are public; `loggers` requires
  authentication)

## Database & migrations

Schema is managed entirely by **Liquibase**
(`spring.jpa.hibernate.ddl-auto=validate` — Hibernate never writes DDL).
Changelogs live under
[`src/main/resources/db.migration/changelog/`](src/main/resources/db.migration/changelog),
organized one directory per owning module/package (`form058`, `form0581`,
`card`, `iam`, `audit`, `export`, `settings`, `webhook`,
`integrationclient`, `devmonitoring`, ...), each wired into the master
changelog via `<includeAll>`. A dated baseline snapshot
(`changelog/baseline/`) anchors schema history before per-feature
changelogs took over.

## Testing

```bash
./mvnw test                                              # full suite
./mvnw test -Dtest="uz.uzinfocom.app.modules.card.**"    # a single module
```

~90 test classes, unit-level throughout — handlers, services, mappers and
validators exercised directly against real MapStruct implementations (not
mocks, wherever practical), plus ArchUnit module-boundary checks and a
handful of Spring context/security-focused tests (`OrganizationContextFilterTest`,
`DynamicRouteAuthorizationManagerTest`, ...). No Testcontainers/`@DataJpaTest`
setup exists yet — schema correctness against a real Postgres instance is
verified by running the application, not by an automated integration test.

## Internationalization

Message bundles: `src/main/resources/i18n/messages*.properties`. Supported
locales: Uzbek Latin (`uz`, default), Uzbek Cyrillic (`uz-Cyrl`),
Karakalpak (`kaa`), Russian (`ru`), English (`en`).

## Security

- OAuth2 resource server validating bearer JWTs from multiple identity
  providers (SSO, DHP) simultaneously, resolved per-request.
- CSRF, HTTP Basic and form login are disabled; CORS is explicit
  (`app.cors.allowed-origins`).
- Public routes are resolved dynamically by `DynamicRouteAuthorizationManager`
  against a runtime-editable `RouteAccessPolicy` store, not `permitAll()`
  scattered across config — editable without a redeploy.
- The dev/ops monitoring panel (`/v1/dev/**`) authenticates independently
  via local HTTP Basic credentials, entirely outside the SSO/DHP bearer-JWT
  model used by every other endpoint.
- The inbound integration API (`/integration/v1/**`) authenticates machine
  clients via API key, Basic auth, or IP allowlisting — never human SSO
  tokens.
- Sensitive fields (national ID, passport, phone, tokens, passwords, patient
  identifiers, ...) are redacted from HTTP request/response logging by
  default.

## Observability

- Structured JSON console logs in production (Logstash encoder).
- Micrometer + Prometheus metrics at `/v1/actuator/prometheus`, including
  circuit-breaker state for every outbound integration.
- Trace-ID propagation via a configurable header (`X-Trace-Id`), correlated
  through MDC.
- Slow-request logging thresholds, separate for inbound and outbound HTTP.

## CI/CD

`.gitlab-ci.yml` runs a SonarQube quality-gate analysis on every pipeline
and builds/deploys the packaged jar to environment-specific servers on
pushes to `dev` and `main`, with a manual rollback stage for the `dev`
environment.

## Conventions

- No Lombok `@Builder`/`@SuperBuilder` on entity hierarchies where a builder
  would let a caller set an invariant field (e.g. a discriminator) that must
  otherwise be fixed per subclass.
- Entities carry no Jackson annotations — polymorphic JSON (sealed
  request/response interfaces with `@JsonTypeInfo`) lives entirely in the
  DTO layer.
- New enum-driven business rules are expressed as methods on the enum itself
  (`canBeUpdated()`, `isFinal()`, ...) rather than scattered `if` conditions
  in services.
- Every card type follows a handler-per-type pattern
  (`CardTypeHandler<C, Q, R>` + a registry that fails fast at startup on a
  missing or duplicated type) so adding a new type never touches shared
  service/controller code.

## Documentation index

Deeper, module-specific documentation lives under [`docs/`](docs):

| Doc | Covers |
|---|---|
| [form058-module.md](docs/form058-module.md) | Form No. 058 status lifecycle, endpoints, design decisions |
| [form058-data-dictionary.md](docs/form058-data-dictionary.md) | Full field-by-field data dictionary shared by Form 058 / 058-1 |
| [form058-form0581-frontend-guide.md](docs/form058-form0581-frontend-guide.md) | Frontend integration guide for the two notification forms |
| [card-module.md](docs/card-module.md) | Card types, handler pattern, status lifecycle, child-collection sync |
| [patient-module.md](docs/patient-module.md) | Patient entity and registration flow |
| [act-module.md](docs/act-module.md) | Act placeholder scope and LIS integration touchpoints |
| [auth-login-module.md](docs/auth-login-module.md) | Login-proxy architecture, providers, grant flow |
| [auth-login-frontend-guide.md](docs/auth-login-frontend-guide.md) | Frontend integration guide for login/refresh |
| [notification-module.md](docs/notification-module.md) | Notification event fan-out model and triggers |
| [notification-frontend-guide.md](docs/notification-frontend-guide.md) | Frontend integration guide for notifications/SSE |
| [platform.md](docs/platform.md) | Shared platform infrastructure overview |
