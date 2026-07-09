# Platform (shared infrastructure)

Package: `uz.uzinfocom.app.platform`. Cross-cutting code every module
depends on. No `package-info.java` files exist yet — this doc is the closest
thing to one.

## `config`

Only `TimezoneConfig` — pins the JVM default timezone to `Asia/Tashkent` via
`@PostConstruct`, so date/time handling is consistent regardless of the host
environment's system timezone.

## `security`

Subpackages: `annotation`, `auth`, `authorization`, `claims`, `config`,
`context`, `filter`, `handler`, `jwt`, `principal`, `properties`, `resolver`,
`route`, `whitelist`.

`SecurityConfig` disables CSRF/HTTP-Basic/form-login, enables CORS, and uses
a multi-provider `AuthenticationManagerResolver` (OAuth2 resource server —
JWT bearer tokens). Public routes are declared in
`SecurityRouteCatalog.OPEN_PATTERNS` (a whitelist, not an inline
`permitAll()` scattered across config). An `OrganizationContextFilter` runs
before Spring Security's `AuthorizationFilter`, resolving the caller's
organization scope ahead of any authorization decision.

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
`Api2ExceptionHandler` and friends), `http`, `iam` (`User`, `Organization`
domain), `mapping` (shared MapStruct config), `persistence` (`AbsEntity`,
`BaseEntity`, `AuditableEntity`, `UuidAuditableEntity` base classes used
across every module's entities), `scope` (organization/region scoping
helpers, e.g. `app.scope.tashkent-region-code`).
