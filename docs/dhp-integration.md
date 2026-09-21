# DHP server-to-server integration (`integration/dhp`)

Outbound lookups from DHP-hosted systems over a **client_credentials (M2M)**
client, mapped into ISEMID's own response shapes. Package:
`uz.uzinfocom.app.integration.dhp`.

Not to be confused with the two other DHP configs:

| Config | What it is |
|---|---|
| `app.auth.providers.dhp.*` | Validates **inbound** DHP-issued bearer tokens |
| `app.auth.login.providers.dhp-web.*` | Browser login (authorization_code + PKCE) |
| `integration.dhp.*` | **This module** - backend-to-DHP calls, no end user |

## Endpoints

Both are `GET`, `@PreAuthorize("isAuthenticated()")`, routed by the
`/v1/dhp/**` row in `security_route_policy` (no `X-Organization` header,
role validation on - same as `/v1/citizen/**`).

| Endpoint | Upstream | Result |
|---|---|---|
| `/v1/dhp/employment?ni=<14 digits>` | `GET https://egov.dhp.uz/mol/citizen/employment/by-ni?ni=` | `{nnuzb, items[{organizationName, organizationTin, position, startDate, endDate, current, raw}]}` |
| `/v1/dhp/immunization?ni=<14 digits>` | DHP FHIR R5 `GET /Patient?identifier=` then `GET /Immunization?patient=Patient/<id>` (paged) | `{nnuzb, items[{fhirId, status, vaccineCode, vaccinationName, serialNumber, vaccinationDate, expirationDate, doseVolume, doseUnit, doseNumber, targetDiseases[], performerName}]}` |

No data (upstream 404, empty body, empty Bundle) is an empty `items`, not an
error. An invalid `ni` is `400 VALIDATION_FAILED` (`validation.nnuzb.format`)
before any upstream call.

`vaccinationName`, `serialNumber`, `vaccinationDate`, `doseVolume` intentionally
match `VaccinationResponse` (Card161) so a vaccination row can be prefilled
directly. `entered-in-error` records are dropped; items are newest first.

## How a call works

`DhpHttpClient` is the single GET path:

1. `DhpAccessTokenProvider` returns a cached bearer token - `POST
   <token-url>` with `grant_type=client_credentials`, `client_id`,
   `client_secret` **in the form body** (confirmed live; token lives ~1h, no
   scopes). Refreshed `token-expiry-skew` (60s) early, one refresh at a time.
2. GET with `Authorization: Bearer`, inside the shared `dhp` circuit breaker.
3. `401` -> drop that token, fetch a new one, retry once; a second `401` is
   `DhpAccessDeniedException`.
4. Status mapping: `400/422` -> `DhpRequestRejectedException` (400),
   `403` -> `DhpAccessDeniedException` (502 `dhp.error.access_denied`),
   `404` -> empty, other 4xx/5xx/transport -> `DhpIntegrationException`
   (502/504). Rejected/denied are `ignore-exceptions` on the breaker.

The immunization client resolves the citizen's `Patient` id by NI first (`GET
<fhir-base>/Patient?identifier=[<identifier-system>|]<NNUZB>`), then searches
`Immunization?patient=Patient/<id>` - a direct patient-identifier search on
Immunization does **not** work on this server (see Known gaps). It follows
the Immunization Bundle's `next` links (max `fhir.max-pages`, page size
`fhir.page-size`) **only** while they stay under `integration.dhp.fhir.base-url`
- a next-link is server-supplied, so following one elsewhere would leak the
bearer token.

## Configuration

`integration.dhp.*` (env in brackets; prod has no credential defaults and the
app still boots without them - the DHP calls fail with
`dhp.error.not_configured` instead):

- `token-url` [`DHP_M2M_TOKEN_URL`], `client-id` [`DHP_M2M_CLIENT_ID`],
  `client-secret` [`DHP_M2M_CLIENT_SECRET`]
- `employment.base-url` [`DHP_EMPLOYMENT_BASE_URL`], `employment.by-ni-endpoint`
- `fhir.base-url` [`DHP_FHIR_BASE_DOMAIN`], `fhir.immunization-endpoint`,
  `fhir.patient-endpoint` [`DHP_IMMUNIZATION_PATIENT_ENDPOINT`; resolves the
  Patient id searched by NI before the Immunization call],
  `fhir.identifier-system` [`DHP_IMMUNIZATION_IDENTIFIER_SYSTEM`; when set the
  search value becomes `system|ni`], `fhir.page-size`, `fhir.max-pages`
- `connect-timeout`, `read-timeout`, `token-expiry-skew`

## Known gaps / verified behavior (live against playground, 2026-09-21)

- **FHIR access is now granted to the M2M client** (previously every FHIR
  resource answered `403`). `Patient`, `Immunization` and `metadata` all
  return `200`; the server reports `fhirVersion 5.0.0`.
- **Immunization cannot be filtered by patient identifier directly - fixed by
  a two-step search.** Both standard FHIR forms fail on this server:
  the chained `patient.identifier=<ni>` is rejected as an unsupported
  parameter type and silently returns **every** Immunization in the system
  unfiltered (entries from unrelated patients mixed in - a real data-exposure
  risk if used as-is); the `patient:identifier=<system>|<ni>` reference
  modifier is syntactically accepted but never matches, even against an
  identifier confirmed to exist on a real `Patient` resource. A plain
  reference search - `Patient?identifier=<ni>` to resolve an id, then
  `Immunization?patient=Patient/<id>` - is the only form that actually
  filters correctly, and is what `DhpImmunizationClient` now does.
- **The employment payload shape is confirmed**: a JSON-RPC envelope,
  `{"result":{"name":...,"surname":...,"positions":[{"org":...,"org_tin":...,
  "position":...,"begin_date":...}, ...]}}` (no `end_date` sample seen yet -
  nothing in the captured response had ended). `DhpEmploymentMapper` matches
  these real names plus several likely synonyms (prod may differ from the
  playground) and every item still carries the untouched source record in
  `raw`.
