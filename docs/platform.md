# Platform (shared infrastructure)

Package: `uz.uzinfocom.app.platform`. Cross-cutting code every module
depends on, and which must never depend back on `modules/*` (enforced by
`PlatformModuleBoundaryTest`). No `package-info.java` files exist yet —
this doc is the closest thing to one.

Code that legitimately needs to read *across* module boundaries —
dashboards, notifications, webhooks, form-visibility scope resolution —
lives in the sibling `orchestration/*` package instead, precisely because
it isn't allowed to live here. See [orchestration section](#orchestration)
below.

## `web.config`

`JacksonTimeZoneConfig` — overrides Jackson's default `Instant` serializer,
which always writes UTC ("...Z") regardless of `spring.jackson.time-zone`
(that property only affects `Date`/`Calendar` formatting), so every `Instant`
field returned by the API is rendered with a fixed `+05:00` (Asia/Tashkent)
offset instead.

The JVM default timezone itself is pinned to `Asia/Tashkent` as the first
line of `Application.main()` (not a `@PostConstruct` bean — that would run
too late for beans that initialize early in context refresh, e.g. the
DataSource/Liquibase), so date/time handling is consistent regardless of the
host environment's system timezone.

## `security`

Subpackages: `annotation`, `auth`, `claims`, `config`, `context`, `filter`,
`handler`, `jwt`, `principal`, `properties`, `resolver`.

`SecurityConfig` disables CSRF/HTTP-Basic/form-login, enables CORS, and uses
a multi-provider `AuthenticationManagerResolver` (OAuth2 resource server —
JWT bearer tokens). Public routes are resolved dynamically by
`DynamicRouteAuthorizationManager` against the DB-backed
`settings.domain.RouteAccessPolicy` table (the old static
`SecurityRouteCatalog` whitelist was removed in favor of this). An
`OrganizationContextFilter` runs before Spring Security's
`AuthorizationFilter`, resolving the caller's organization scope ahead of
any authorization decision.

## `observability`

`ObservabilityProperties` (`@ConfigurationProperties(prefix =
"app.observability")`) covers:

- Trace-ID header handling (`X-Trace-Id`, accept-incoming toggle, min/max
  length, allowed separators).
- Inbound HTTP request logging — slow-request threshold, a sensitive
  query-parameter redaction list (`pinfl`, `nnuzb`, `passport`, `phone`,
  `patient_id`, ...), path masking, excluded-path prefixes.
- Outbound HTTP client logging (separate on/off switches, body logging
  disabled by default, max body size cap).
- A dedicated async executor pool (core/max size, queue capacity, keep-alive,
  thread-name prefix) used for anything offloaded from the request thread.

See `application.properties` for the full set of tunable keys and their
production defaults.

## `i18n`

`I18nConfig` wires a `ResourceBundleMessageSource`
(`src/main/resources/i18n/messages*.properties`) and a locale resolver.
Supported locales: Uzbek Latin (`uz`, default), Uzbek Cyrillic (`uz-Cyrl`),
Karakalpak (`kaa`), Russian (`ru`), English (`en`). A code comment flags a
historical bug worth remembering: `"uz-Cyril"` is **not** a valid BCP-47 tag
— the correct form is `"uz-Cyrl"`.

## Other packages

`cache` (Caffeine-backed `@Cacheable` setup — see
`SecurityUserCacheService`/`SelectedOrganizationSecurityCacheService` for
usage examples), `exception` (global exception handling —
`Api2ExceptionHandler` and friends), `http`, `ssoproxy`
(login-proxy, formerly `auth` — renamed to stop colliding with
`security.auth` in search), `mapping` (shared MapStruct config),
`persistence` (`AbsEntity`, `BaseEntity`, `AuditableEntity`,
`UuidAuditableEntity` base classes used across every module's entities),
`stats` (`AbstractCaseStatsRepository`, the Criteria-API template every
module's own `*StatsRepository` extends).

## Orchestration

Sibling package `uz.uzinfocom.app.orchestration`, not `platform` — see the
note at the top of this doc for why. Four packages, moved out of `platform`
unchanged (same classes, same logic, only the package declaration and
imports changed):

- `notification` — in-app notification fan-out (form058/form0581/card/act
  domain events → `Notification` rows); reads `Act`/`Card`/`Form058`/
  `Form0581`/`PatientAffiliation` directly by design.
- `webhook` — outbound status-change webhook dispatch; same
  cross-module-read shape as `notification`.
- `dashboard` — single-organization summary widgets aggregating query
  results from `act`/`card`/`form058`/`form0581`.
- `scope` — organization/region scope resolution (`app.scope.tashkent-region-code`,
  etc.), including form-visibility scope derived from `patient`
  affiliations (`FormAccessScopeResolver`).
