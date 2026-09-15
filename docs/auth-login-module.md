# Auth login-proxy module (`uz.uzinfocom.app.platform.ssoproxy`)

Package: `uz.uzinfocom.app.platform.ssoproxy`. Implements a **login-proxy** —
two endpoints that exchange either an authorization code (Authorization
Code + PKCE) or a refresh token for an access token, by calling an
external authentication provider's (SSO, DHP) token endpoint on the
caller's behalf, using a `client_id`/(optionally) `client_secret` kept on
the backend.

> **History:** the module originally supported two grants - **password**
> (`sso`, username/password posted straight to the backend) and
> **authorization_code + PKCE** (`sso-web`, `dhp-web`, redirect to the
> provider's own login page). Once redirect-based login covered every
> provider that was needed, the password grant was removed entirely (the
> `sso` provider, the `PasswordGrantLoginProvider` class, its factory, and
> the related DTO/config fields). The architecture (Strategy + Factory, see
> below) was deliberately designed so this works in both directions - a
> grant can be added or removed without touching the controller, service,
> or registry.

## Why this is a separate module, not part of `platform.security`

`uz.uzinfocom.app.platform.security` already exists in the codebase — but
it solves a **different** problem: validating **inbound** bearer JWTs a
user already obtained from SSO/DHP and presents in an
`Authorization: Bearer <token>` header (the resource-server side,
`AuthProvidersProperties` + `ProviderAuthenticationManagerRegistry` +
`IdentityClaimExtractor`).

`platform.ssoproxy` solves the reverse problem — **obtaining** that token: our
backend itself calls the provider and exchanges an authorization code for
a token, to hand back to the frontend. That's a separate, outbound
operation — hence its own configuration (`app.auth.login.*`, not
`app.auth.providers.*`) and its own set of classes.

**Important note on key naming**: `app.auth.providers.dhp.*` (inbound, JWT
validation) and `app.auth.login.providers.dhp-web.*` (outbound,
login-proxy) are **deliberately different keys** (`dhp` vs `dhp-web`), not
a typo. The `dhp` key on the inbound side is hard-coded into
`DhpIdentityClaimExtractor.PROVIDER_KEY` and must match whatever actually
arrives in a validated JWT - it cannot be changed. The `dhp-web` key on the
login-proxy side is a separate OAuth2 client registration
(authorization_code + PKCE), named that way for consistency with
`sso-web` (the `-web` suffix marks a redirect-based web flow).

## Endpoints

### `POST /v1/auth/login/{provider}` - initial exchange

```
POST /v1/auth/login/{provider}
Content-Type: application/json

{
  "code": "...",
  "codeVerifier": "...",
  "redirectUri": "..."
}
```

The flow (identical for `sso-web` and `dhp-web`) - the user's credentials
**never reach our backend at all**:

1. The frontend generates a PKCE pair (`code_verifier` + `code_challenge`)
   and redirects the user's browser directly to the provider's login page
   (`authorization_endpoint` + `client_id` + `redirect_uri` +
   `code_challenge`).
2. The provider authenticates the user on its own side, then redirects
   back to `redirect_uri` with `?code=...`.
3. The frontend sends that `code` (+ the same `code_verifier` and
   `redirect_uri`) to `POST /v1/auth/login/{provider}`.
4. The backend exchanges `code` for a token via
   `grant_type=authorization_code`, adding the secret `client_secret` if
   the provider has one.

If the resolved provider is missing `code`/`codeVerifier`/`redirectUri` -
`400 VALIDATION_FAILED` with the same i18n message a regular `@NotBlank`
validation would produce (`auth.login.code.required`,
`auth.login.redirect-uri.required`, etc.) - only the check happens not via
Bean Validation on the DTO, but inside the resolved `LoginProvider`
(`OAuth2TokenExchangeClient.requireField(...)`). This is deliberate, not a
missed `@NotBlank`: it's what let the password grant coexist with
authorization_code before it was removed, and keeps the door open for a
future second grant with a different field set (see "Architecture" below).

### `POST /v1/auth/refresh/{provider}` - refresh an existing token

```
POST /v1/auth/refresh/{provider}
Content-Type: application/json

{
  "refreshToken": "..."
}
```

Exchanges a previously-issued `refreshToken` for a new token via
`grant_type=refresh_token`, using the **same** `client_id`/`client_secret`/
`tokenUrl` already configured for that provider's `POST /v1/auth/login/
{provider}` - it's the same OAuth2 client, just a different `grant_type`
value, so `AuthorizationCodeGrantLoginProvider` (see "Architecture" below)
handles both without needing a separate class. Some providers (DHP,
confirmed via its own OpenAPI spec) **rotate** the refresh token on every
use - the response's `refreshToken` may differ from the one that was sent,
and the caller must persist the new value, not the one it sent.

Both endpoints return the same response shape and go through the same
error classification (see below); a missing `refreshToken` gets `400
VALIDATION_FAILED` (`auth.login.refresh-token.required`) exactly like a
missing `code` does for `/login`.

### Response (both endpoints)

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "scope": "openid"
}
```

`accessToken` is the same JWT the provider itself issues, so it can be used
straight away as `Authorization: Bearer` on subsequent calls to the rest of
the API — they already validate `sso`/`dhp` tokens via
`AuthProvidersProperties` (see `platform.security`).

The `/v1/auth/**` path was reserved ahead of time in
`SecurityRouteCatalog.OPEN_PATTERNS` and `POLICY_RULES` (public access, no
organization header, no role check) before this module even existed — no
changes to `SecurityConfig`/`SecurityRouteCatalog` were needed.

## Architecture: Strategy + Factory

Key principle — **adding a new provider that speaks an already-supported
grant never requires changing the controller, service, or registry**, only
configuration. Adding (or removing) a **grant** requires one factory class
(plus a `LoginProvider` implementation), but still doesn't touch
`LoginProviderRegistry`/`LoginService`/`LoginController` - already proven
in practice: that's exactly how the password grant was removed, without
leaving a trace in those three classes.

### Components

| Class | Role |
|---|---|
| `properties/LoginGrantType.java` | Enum - which grant a given config entry speaks. Only `AUTHORIZATION_CODE` today; a `PASSWORD` value existed and was removed along with the password grant. |
| `properties/LoginProvidersProperties.java` | `@ConfigurationProperties(prefix = "app.auth.login")` — a `providers.<key>.*` map: `enabled`, `grantType` (defaults to `AUTHORIZATION_CODE`), `tokenUrl`, `clientId`, `clientSecret`, `credentialsInBasicHeader`, `requireClientSecret` (false for a public PKCE client with no secret at all), `extraParams` (arbitrary static form fields, e.g. SSO's `claims=organization`). |
| `web/dto/LoginRequest.java` | Record: `code`, `codeVerifier`, `redirectUri`. No `@NotBlank` (see explanation above) - left this way in case a second grant with a different field set shows up. |
| `application/LoginProvider.java` | Extension interface: `providerKey()`, `grantType()`, `login(LoginRequest request)`, `refresh(String refreshToken)`. `refresh` lives on the same interface, not a separate capability - every provider already has what a `refresh_token` grant needs (same client_id/secret/tokenUrl as `login`). Each implementation checks its own fields' presence itself (`OAuth2TokenExchangeClient.requireField(...)`). |
| `application/OAuth2TokenExchangeClient.java` | Package-private helper - the part of the exchange that's identical for any grant: required-field validation, assembling `client_id`/`client_secret` (form body or Basic header, secret omitted entirely for `requireClientSecret=false`), merging `extraParams` into the form, POSTing to `tokenUrl`, parsing the response into a private record via Jackson, and classifying **every** failure mode - see "Error classification" below. Shared as-is by both `login` and `refresh` - only the grant-specific form fields differ. |
| `application/AuthorizationCodeGrantLoginProvider.java` | The only `LoginProvider` today - `login()` reads `code`/`codeVerifier`/`redirectUri`, builds a `grant_type=authorization_code` form; `refresh()` reads `refreshToken`, builds a `grant_type=refresh_token` form. One implementation serves any number of providers on this grant (`sso-web`, `dhp-web`). |
| `application/LoginProviderFactory.java` | **Factory** interface: `supportedGrantType()`, `create(providerKey, properties, restClient, jsonMapper)` → `LoginProvider`. **The extension point for a new grant.** |
| `application/AuthorizationCodeGrantLoginProviderFactory.java` | `@Component` factory implementation for `AUTHORIZATION_CODE`. |
| `application/LoginProviderRegistry.java` | Collects `List<LoginProviderFactory>` (keyed by `supportedGrantType()`) and `List<LoginProvider>` (hand-written, take priority over configuration) via Spring injection - the same pattern `IdentityClaimExtractorRegistry` uses for `List<IdentityClaimExtractor>`. For each enabled config entry it finds the factory matching its `grantType` and builds the provider. **The class itself never changes** when a grant is added or removed - only the factories. |
| `application/LoginService.java` | Resolves the provider via the registry, calls `provider.login(request)` or `provider.refresh(refreshToken)`, maps `LoginResult` → `LoginResponse` via a shared private `toResponse(...)`. |
| `web/LoginController.java` | Two methods: `login` (`POST /v1/auth/login/{provider}`) and `refresh` (`POST /v1/auth/refresh/{provider}`). |
| `web/dto/RefreshTokenRequest.java` | Record: `refreshToken`. Same "no `@NotBlank`, validated inside the provider" reasoning as `LoginRequest`. |
| `application/exception/{UnknownLoginProviderException, InvalidLoginCredentialsException, InvalidLoginRequestException, LoginProviderMisconfiguredException, LoginUpstreamException}.java` | Extend `AppException`, rendered by the existing `GlobalExceptionHandler`. |

## Error classification - every failure mode must return the right `AppException`

No failure mode of the token endpoint should ever result in a `500` or an
unhandled exception - `OAuth2TokenExchangeClient` classifies each one
explicitly:

| Situation | Exception | HTTP | Note |
|---|---|---|---|
| Missing `code`/`codeVerifier`/`redirectUri` | `InvalidLoginRequestException` | `400` | Checked **before** any network call - `requireField(...)`. |
| Upstream returned `400`/`401` with an OAuth2 body `{"error": "invalid_grant"}` | `InvalidLoginCredentialsException` | `401` | The only case that's genuinely "the caller's fault" (an expired, already-used, or invalid code). |
| Upstream returned `400`/`401` with `{"error": "invalid_client" \| "unauthorized_client" \| "invalid_request" \| "unsupported_grant_type" \| "invalid_scope"}` | `LoginProviderMisconfiguredException` | `502` (the same `UPSTREAM_ERROR` as a regular upstream failure) | **Not the caller's fault** - this is our own misconfigured client_id/client_secret/grant-type/scope. The HTTP response to the frontend reads no differently from a generic "service unavailable", but a distinct exception type + `log.warn` with the specific upstream error code makes the problem immediately recognizable in logs as a config bug, not a routine user error. This is exactly how the DHP bug (see below) was found and confirmed - `invalid_client` instead of `invalid_grant`. |
| Upstream returned `400`/`401` but the body is empty, isn't JSON, or has no `error` field (e.g. a WAF/gateway in front of the real server sometimes reformats the error into its own shape rather than standard OAuth2) | `InvalidLoginCredentialsException` (safe default) | `401` | Observed live: a gateway in front of `test-sso.ssv.uz` sometimes responds `{"success":false,"errors":[...]}` instead of `{"error":"invalid_request",...}` to essentially the same request. Body parsing **never throws** further - any parse failure falls back to this default instead of failing the request. |
| Upstream returned any other `4xx`/`5xx` (`429`, `500`, `503`...) | `LoginUpstreamException` | `502` | The upstream error code (if recognized) goes into `log.warn` for diagnostics, but doesn't affect classification. |
| Network failure before a response was received (connection refused, DNS, dropped connection) | `LoginUpstreamException` | `502` | `RestClientException` is caught as a whole; a `SocketTimeoutException` in the cause chain produces `504` instead of `502` (see below). |
| Connect/read timeout | `LoginUpstreamException` | `504` (`UPSTREAM_TIMEOUT`) | The only network case that gets its own code - a timeout is worth retrying, other network errors aren't necessarily. |
| `200 OK`, but the body doesn't parse or `access_token` is blank/missing | `LoginUpstreamException` | `502` | The upstream "succeeded" with something useless - still its problem, not ours or the caller's. |

Important lesson from live testing: **never assume the upstream will
always return a textbook error shape.** A direct `curl` against
`test-sso.ssv.uz` and a call through our own `RestClient` to the same URL
with essentially the same parameters got noticeably different response
bodies (standard OAuth2 `{"error": ...}` manually vs
`{"success": false, "errors": [...]}` through the app) - most likely a
WAF/gateway in front of the real server. Body parsing
(`upstreamErrorCode`) is therefore wrapped in `try/catch`, treats
"unrecognized" as `null`, and **never throws** - this exact caution was
uncovered and closed by the unit test
`fallsBackSafelyWhenAGatewayInFrontOfTheProviderReturnsANonOAuthErrorShape`,
after `Set.of(...).contains(null)` initially threw a
`NullPointerException` on an empty body (a real bug found by live testing,
fixed, and covered by a test).

## Configuration

```properties
# grant-type omitted - AUTHORIZATION_CODE is the default and only supported
# grant today (LoginProvidersProperties.ProviderProperties).

# SSO's public-SPA client registration - no client_secret at all (this
# client authenticates with code_verifier alone), plus an extra static
# "claims" field this client's flow requires.
app.auth.login.providers.sso-web.enabled=true
app.auth.login.providers.sso-web.token-url=${SSO_WEB_LOGIN_TOKEN_URL:...}
app.auth.login.providers.sso-web.client-id=${SSO_WEB_LOGIN_CLIENT_ID:...}
app.auth.login.providers.sso-web.require-client-secret=false
app.auth.login.providers.sso-web.extra-params.claims=organization

# DHP - does require a client_secret (see the verification note below).
# Key "dhp-web", not "dhp" - see the key-naming note above for why.
app.auth.login.providers.dhp-web.enabled=true
app.auth.login.providers.dhp-web.token-url=${DHP_WEB_LOGIN_TOKEN_URL:https://dev.dhp.uz/sso/oauth/token}
app.auth.login.providers.dhp-web.client-id=${DHP_WEB_LOGIN_CLIENT_ID:...}
app.auth.login.providers.dhp-web.client-secret=${DHP_WEB_LOGIN_CLIENT_SECRET:...}
app.auth.login.providers.dhp-web.credentials-in-basic-header=false
```

The dev profile carries real values (the test SSO at `test-sso.ssv.uz`,
DHP dev environment) as literal fallback values - following an existing
precedent in the project (`integration.lis.api-key` in
`application-dev.properties` is also a literal for dev convenience). The
prod profile uses environment variables only, no literals.

## CORS

Frontend calls to this endpoint (and to any other endpoint) are
browser-based cross-origin requests - the browser enforces CORS itself,
before this app ever sees the request. `SecurityConfig` already had
`.cors(Customizer.withDefaults())` in the filter chain, but with no
`CorsConfigurationSource` bean registered anywhere, that call had nothing
to consult - CORS handling was effectively absent, and every cross-origin
browser request was silently rejected by the browser.

Fixed with a new `CorsConfigurationSource` bean in `SecurityConfig`, backed
by `CorsProperties` (`app.cors.*`, prefix `app.cors`):

```properties
# dev
app.cors.allowed-origins=${APP_CORS_ALLOWED_ORIGINS:http://localhost:3000}

# prod - no default; left empty (deny all cross-origin) until the real
# frontend origin(s) are supplied.
app.cors.allowed-origins=${APP_CORS_ALLOWED_ORIGINS:}
```

`allowCredentials` stays `false` (the default): this API authenticates via
`Authorization: Bearer`, never cookies, so the frontend never needs
`credentials: "include"` - and the CORS spec forbids combining
`allowCredentials=true` with a wildcard origin anyway. Verified live:
preflight (`OPTIONS`) and the actual response both carry
`Access-Control-Allow-Origin` for an allowed origin, and a disallowed
origin gets no CORS headers at all (browser blocks it client-side).

## Verified live (dev profile)

- **Both endpoints, both providers**: `POST /v1/auth/login/sso-web` and
  `POST /v1/auth/login/dhp-web` with `{code, codeVerifier, redirectUri}`
  both resolve the right provider and both reach the right upstream
  endpoint. The old routes (`/v1/auth/login/sso` - password grant,
  `/v1/auth/callback/dhp` - an earlier naming) now correctly return `404`
  instead of some stale stub.
- **Refresh**: `POST /v1/auth/refresh/sso-web` and `POST /v1/auth/refresh/
  dhp-web` with a (fake, for the test) `{refreshToken}` both actually
  reached `test-sso.ssv.uz` and `dev.dhp.uz` respectively (confirmed via
  the `OUTBOUND_HTTP` log line, not just the app's own response) and got
  back a clean `401`/`400` OAuth2 error - not a `500` - for the fake token,
  exactly the expected outcome.
- **SSO-web** (`test-sso.ssv.uz/oauth/token`, authorization_code + PKCE,
  public client with no secret, `claims=organization`): a direct curl with
  a fake `code`/`code_verifier` returned `invalid_request` /
  `"Cannot decrypt the authorization code"` - **not** `invalid_client`,
  meaning `client_id` is recognized by the server, no secret is required,
  and `claims` is accepted without complaint.
- **DHP - resolved.** Initially configured against
  `playground.dhp.uz/sso/oauth/token` (URL confirmed valid via its own
  `.well-known/openid-configuration`), the given `client_id: emid.conf.web`
  /`client_secret` returned **`invalid_client`** there - correctly
  classified as `LoginProviderMisconfiguredException` (not a routine user
  error) and logged with the upstream error code, which is exactly what
  made this diagnosable without a manual curl session. Root cause turned
  out to be the wrong server: the client is registered on
  **`dev.dhp.uz`**, not `playground.dhp.uz` (confirmed via
  `dev.dhp.uz/sso/.well-known/openid-configuration` and a direct token
  request - the same client_id/secret there returns `invalid_grant`, i.e.
  the client is accepted and only the test code is rejected, as expected).
  `app.auth.login.providers.dhp-web.token-url` now points at
  `https://dev.dhp.uz/sso/oauth/token`. Also confirmed live: despite being
  described as a "public spa" client, it does require `client_secret` in
  the request body (omitting it - the public-PKCE-only shape used for
  `sso-web` - returns `invalid_client` again) - `require-client-secret` is
  left at its default (`true`) for `dhp-web`, unlike `sso-web`.
- **Separately verified and documented** (while the password grant still
  existed): SSO issues two independent clients with different allowed
  grant_type sets - the password-grant client isn't accepted for
  authorization_code (the server responded `invalid_client`), and vice
  versa. That's exactly why `sso-web` and (at the time) `sso` had to be
  separate config keys rather than one key with a different `grant-type`.

## How to add a new provider

**If the provider uses an already-supported grant** (authorization_code,
like `sso-web`/`dhp-web`) — configuration only: a new
`app.auth.login.providers.<key>.*` section. No Java code changes.

**If the provider uses a grant that isn't supported yet** (e.g.
`client_credentials` for something else, or `password` again):

1. Add a value to `LoginGrantType`.
2. Add new (optional) fields to `LoginRequest` if the grant needs fields
   that don't exist yet.
3. Write a `LoginProvider` implementation for that grant (following
   `AuthorizationCodeGrantLoginProvider` as a template), reusing
   `OAuth2TokenExchangeClient`.
4. Write a `@Component implements LoginProviderFactory` for it.

`LoginProviderRegistry`, `LoginService`, `LoginController`, and
`ApiPaths.Auth` **don't change** either way - the factory is picked up
automatically via `List<LoginProviderFactory>`. Removing a grant is the
mirror operation (see the password-grant history above).

## Tests

- `AuthorizationCodeGrantLoginProviderTest` — via `MockRestServiceServer`:
  a successful exchange, credentials in the form body vs a Basic header, a
  public client with no `client_secret` at all + arbitrary `extraParams` in
  the form, a failure (`IllegalStateException`) when `requireClientSecret
  =true` and no secret is configured, a missing required field →
  `InvalidLoginRequestException` (no upstream call at all), and the full
  upstream error-classification matrix: `invalid_grant` →
  `InvalidLoginCredentialsException`; `invalid_client`/`invalid_request` →
  `LoginProviderMisconfiguredException`; `500` → `LoginUpstreamException`;
  a body with no `error` field at all (including the observed live
  gateway reformatting `{"success":false,"errors":[...]}`) → the safe
  default (`InvalidLoginCredentialsException`), without crashing. Plus
  `refresh(...)`: a successful exchange asserting `grant_type=refresh_token`
  is sent (and `grant_type=authorization_code` is not), a revoked/expired
  refresh token → `InvalidLoginCredentialsException`, a missing
  `refreshToken` → `InvalidLoginRequestException` without an upstream call.
- `LoginProviderRegistryTest` — registering enabled entries through their
  factory, skipping disabled ones, skipping an entry whose grant has no
  registered factory, a hand-written bean taking priority over
  configuration.
- `LoginServiceTest` — mapping `LoginResult` → `LoginResponse` through a
  mocked registry, for both `login` and `refresh`.
