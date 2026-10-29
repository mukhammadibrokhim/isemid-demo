# ISEMID backend guide

Single consolidated reference for everything under `docs/` **except**
[`integration/`](integration) (external-submitter API contracts) and the
legacy-migration docs ([`legacy-data-migration.md`](legacy-data-migration.md),
[`legacy-migration/`](legacy-migration)), which stay as separate files.

It merges the former per-module docs, frontend guides and the data
dictionary. Every statement was re-checked against the code on 2026-10-28;
the corrections made during that pass are listed in [§17](#17-corrections-made-in-this-consolidation).
For exact request/response field lists use Swagger UI — every DTO field has a
`@Schema` description. This guide covers workflows, rules and the
non-obvious parts. Architecture/stack/CI narrative is in the root
[README.md](../README.md); the file-finding map is [CLAUDE.md](../CLAUDE.md).

## Contents

1. [API conventions](#1-api-conventions)
2. [Platform and orchestration](#2-platform-and-orchestration)
3. [Sign-in (SSO / DHP login proxy)](#3-sign-in-sso--dhp-login-proxy)
4. [Form 058 and Form 058-1](#4-form-058-and-form-058-1)
5. [Form 129](#5-form-129)
6. [Patient](#6-patient)
7. [Cards](#7-cards)
8. [Acts and LIS](#8-acts-and-lis)
9. [Notifications](#9-notifications)
10. [DHP server-to-server integration](#10-dhp-server-to-server-integration)
11. [Reference data: population and manual reports](#11-reference-data-population-and-manual-reports)
12. [Reports](#12-reports)
13. [Excel export](#13-excel-export)
14. [Dev panel (`/v1/dev/**`)](#14-dev-panel-v1dev)
15. [Data dictionary: Form 058 / 058-1](#15-data-dictionary-form-058--058-1)
16. [Open questions](#16-open-questions)
17. [Corrections made in this consolidation](#17-corrections-made-in-this-consolidation)

---

## 1. API conventions

**Auth.** Everything except `/v1/auth/**` needs `Authorization: Bearer <SSO/DHP
JWT>`. `/v1/dev/**` is the exception: HTTP Basic against local `DevUser`
accounts (§14). Never use cookies or `credentials: "include"` — the API sets
`allowCredentials=false`.

**CORS.** Allowed origins come from `app.cors.allowed-origins` (dev default
`http://localhost:3000`, prod empty = deny all). A frontend on another origin
must be added by the backend team; otherwise the browser blocks the request
before it reaches the server (shows as a CORS error, not an HTTP error).

**Success envelope.**

```json
{ "success": true, "message": "…", "data": { } }
```

Paged lists add `meta` (`page` 1-based, `size`, `totalElements`, `totalPages`)
and `links`. Paging params everywhere: `page`, `size` (max 200), `sortBy`,
`sortDir`; unknown `sortBy` is ignored server-side, not an error.

**Error envelope.**

```json
{ "success": false, "code": "CONFLICT", "message": "…localized…",
  "traceId": "…", "path": "/v1/…", "timestamp": "…+05:00", "violations": [] }
```

| `code` | HTTP | Typical cause |
|---|---|---|
| `VALIDATION_FAILED` | 400 | bad input; `violations[]` names the fields |
| `UNAUTHORIZED` | 401 | missing/invalid token, rejected login code |
| `FORBIDDEN` / `SCOPE_VIOLATION` | 403 | wrong organization or missing permission/role |
| `NOT_FOUND` | 404 | id missing or not visible to caller |
| `CONFLICT` | 409 | action invalid for the record's current status |
| `UPSTREAM_ERROR` / `UPSTREAM_TIMEOUT` | 502 / 504 | external system (SSO, DHP, LIS, stat.uz) failed |

`message` is localized to the request locale and safe to show; match on
`code`, never on `message`.

**Locales.** `Accept-Language`: `uz` (default, Latin), `uz-Cyrl`, `kaa`, `ru`,
`en`. (`uz-Cyril` is **not** a valid tag — the historical bug.)

**Time.** The JVM is pinned to `Asia/Tashkent` (first line of
`Application.main()`), and every `Instant` is serialized with a fixed `+05:00`
offset by `JacksonTimeZoneConfig`.

**SSE gotcha (notifications, export progress).** The browser's native
`EventSource` cannot send `Authorization`, so it gets `401` (often shown as a
CORS failure). Use `fetch` + a streaming reader, e.g.
`@microsoft/fetch-event-source`. Server closes SSE streams after 15 minutes —
reconnect.

**Pagination in affiliated lists** wraps each row (`data[i].form.id`), see §4.4.

---

## 2. Platform and orchestration

Package `uz.uzinfocom.app.platform` — cross-cutting infrastructure. It never
depends on `modules/*` (ArchUnit `PlatformModuleBoundaryTest`); exceptions:
`modules.reference` and `modules.iam`. Code that must read across modules
lives in `orchestration/*`.

| Package | Role |
|---|---|
| `web.config` | `JacksonTimeZoneConfig` (see §1) |
| `security` | Resource server (multi-provider JWT). `SecurityConfig` disables CSRF/form login, enables CORS. Public routes are **DB-driven**: `DynamicRouteAuthorizationManager` + `settings.domain.RouteAccessPolicy` (table `security_route_policy`); the old static `SecurityRouteCatalog` is gone. `OrganizationContextFilter` resolves the caller's organization scope before authorization. Includes `ratelimit/` (per-IP/per-user 429 budgets on auth, integration token, citizen/legal-entity lookups). |
| `ssoproxy` | Login proxy (§3); formerly `auth`, renamed to stop colliding with `security.auth` |
| `observability` | `app.observability.*`: `X-Trace-Id`, request logging with sensitive-query redaction (`pinfl`, `nnuzb`, `passport`, `phone`, `patient_id`…), outbound HTTP logging (body off by default), async executor |
| `i18n` | `ResourceBundleMessageSource` over `i18n/messages*.properties` |
| `cache` | Caffeine `@Cacheable` setup |
| `exception` | global handlers |
| `persistence` | `AbsEntity`, `BaseEntity`, `AuditableEntity`, `UuidAuditableEntity`; `stats.AbstractCaseStatsRepository` (Criteria template for module stats repos) |
| `export` | shared Excel (SXSSF) + SSE progress; multi-sheet workbook support (§13) |
| `settings` | runtime-editable `system_settings`, route policies |
| `audit`, `integrationclient`, `devpanel`, `resilience`, `http`, `mapping`, `stats` | as named |

**Orchestration** (`uz.uzinfocom.app.orchestration`): `notification` (§9),
`webhook` (outbound status-change dispatch to integration clients),
`dashboard` (single-organization summary widgets), `scope`
(organization/region scope resolution, form-visibility scope from patient
affiliations via `FormAccessScopeResolver`), `devseed` (`dev` profile only).

---

## 3. Sign-in (SSO / DHP login proxy)

Package `platform.ssoproxy`. Two **public** endpoints exchange an
authorization code (Authorization Code + PKCE) or a refresh token for an
access token by calling the provider's token endpoint with a `client_id`
(/`client_secret`) kept on the backend. Users never type credentials on our
site (the earlier password grant was removed). Swagger group:
**«Аутентификация»**.

### Flow (identical for `sso-web` and `dhp-web`)

1. Frontend creates a PKCE pair (`S256`) and redirects the browser to the
   provider's authorization page (`client_id` is public).
2. User signs in at the provider; provider redirects back with `?code=…`.
3. Frontend calls:

```
POST /v1/auth/login/{provider}
{ "code": "…", "codeVerifier": "same verifier as step 1", "redirectUri": "exactly as in step 1" }
```

```
POST /v1/auth/refresh/{provider}
{ "refreshToken": "…" }
```

Response (both):

```json
{ "accessToken": "…", "refreshToken": "…|null", "tokenType": "Bearer",
  "expiresIn": 900, "scope": "openid" }
```

`accessToken` is the provider's own JWT — use it directly as `Bearer`.
`refreshToken` may be absent. **DHP and some SSO configs rotate the refresh
token on every use** — always overwrite the stored value with the latest
response; reusing the old one fails. `expiresIn` may be `null`; treat a `401`
from the API as the signal to re-authenticate.

### Errors

| HTTP | `code` | When | Frontend action |
|---|---|---|---|
| 400 | `VALIDATION_FAILED` | `code`/`codeVerifier`/`redirectUri` (login) or `refreshToken` (refresh) missing — checked inside the provider, not via `@NotBlank` | show field error |
| 401 | `UNAUTHORIZED` | provider rejected the code (expired/used/mismatch) or the refresh token; also the safe default for unparseable 400/401 bodies | login: "try again"; refresh: fall back to full redirect flow |
| 404 | `NOT_FOUND` | unknown/disabled `{provider}` | frontend bug |
| 502 | `UPSTREAM_ERROR` | provider 5xx/odd body; **also** `invalid_client`/`invalid_request`/`unsupported_grant_type`/`invalid_scope` (our own client misconfiguration — distinct exception + `log.warn`, same HTTP shape) | "temporarily unavailable", retry once |
| 504 | `UPSTREAM_TIMEOUT` | provider timed out | same |

Upstream error bodies are never assumed to be textbook OAuth2 (a gateway in
front of the SSO sometimes returns `{"success":false,"errors":[…]}`); parsing
never throws and falls back to the 401 default.

### Providers and configuration

`app.auth.login.providers.<key>.*`: `enabled`, `grant-type` (only
`AUTHORIZATION_CODE`), `token-url`, `client-id`, `client-secret`,
`credentials-in-basic-header`, `require-client-secret`, `extra-params.*`.

| Key | Notes |
|---|---|
| `sso-web` | public PKCE client, **no secret** (`require-client-secret=false`), `extra-params.claims=organization`. Verified live against the test SSO. |
| `dhp-web` | **does require** `client_secret` (in the form body) despite being called a public SPA. **Unverified:** `token-url` was switched back to `https://playground.dhp.uz/sso/oauth/token` on 2026-09-18, but the client id/secret were last confirmed working on `dev.dhp.uz` (they returned `invalid_client` on playground). Re-check live before relying on it. |

Key naming is deliberate: inbound JWT validation is `app.auth.providers.dhp.*`
(key hard-coded in `DhpIdentityClaimExtractor.PROVIDER_KEY`); login proxy is
`app.auth.login.providers.dhp-web.*`; M2M calls are `integration.dhp.*` (§10).
Dev profile carries literal fallbacks; prod uses env vars only.

### Architecture (Strategy + Factory)

`LoginProvider` (`login`, `refresh`) ← `AuthorizationCodeGrantLoginProvider`
(shared `OAuth2TokenExchangeClient` does validation, secret placement,
`extraParams`, POST, parsing, error classification). `LoginProviderRegistry`
collects `LoginProviderFactory` beans by grant type. New provider on an
existing grant = configuration only; new grant = new `LoginGrantType` value +
`LoginProvider` + `LoginProviderFactory`; registry/service/controller never
change. Tests: `AuthorizationCodeGrantLoginProviderTest` (MockRestServiceServer,
full error matrix incl. gateway reformatting), `LoginProviderRegistryTest`,
`LoginServiceTest`.

---

## 4. Form 058 and Form 058-1

Form №058 = infectious/parasitic disease notification
(`modules.form058`, `/v1/form-058`). Form №058-1 = suspected-rabies animal
bite/scratch/saliva notification (`modules.form0581`, `/v1/form-058-1`).
Independent entities/tables, **same workflow, actions and authorization
model** (differences are called out). Form 058 is the aggregate root the
`card` module attaches to and the trigger for `patient` registration.

### 4.1 Status workflow

`FormStatus` / `Form0581Status` (identical): `SENT`, `ACCEPTED`,
`CARD_LINKED`, `APPROVED`, `CANCELED`.

```
SENT ──accept (receiver)──► ACCEPTED ──cards linked──► CARD_LINKED ──approve (sender)──► APPROVED (final)
  │                            └────────── approve (Form 058 only) ───────────────────────┘
  └──cancel (sender OR receiver, only while SENT)──► CANCELED ──reopen (super admin)──► SENT
```

| Action | Who | Required status | Result |
|---|---|---|---|
| `accept` | receiver org only | `SENT` | `ACCEPTED` |
| `cancel` | sender **or** receiver org | `SENT` only | `CANCELED` (withdraw/reject share one status — no separate "rejected") |
| `approve` | **sender** org only | Form 058: `ACCEPTED` or `CARD_LINKED` · Form 058-1: `CARD_LINKED` only | `APPROVED` |
| `reopen` | `isemid_super_admin` only | `CANCELED` | `SENT` |

- `APPROVED` is final and locked — nobody (incl. super admin) can edit,
  cancel or reopen it.
- `CANCELED` is locked for both organizations; only a super admin may
  `reopen`, edit or delete it.
- Once the receiver accepted, neither side can cancel.
- `update`/`delete` are blocked for everyone once `APPROVED` or `CANCELED`
  (super admin may still edit/delete `CANCELED`). Physical delete is possible
  only from `SENT` (`FormStatus.physicallyDeletable`); otherwise soft delete.
- A super admin bypasses organization-scope checks but not status
  preconditions (except `reopen`/`update`/`delete` on `CANCELED`).
- Linking cards (`card` module) requires `ACCEPTED` or later
  (`error.form058.card-link-not-allowed` from `SENT`); a form already at
  `CARD_LINKED` stays there when another card is assigned.
- **Receiver constraint:** `Organization.medicalType == SANEPID_SERVICE` on
  create and whenever `receiverOrganizationId` changes, else
  `VALIDATION_FAILED` (`error.form058.receiver-not-sanepid` /
  `error.form0581.receiver-not-sanepid`). Filter the receiver picker to
  SANEPID_SERVICE organizations.
- Transition rules live as predicates on the enum (`isCancellable`,
  `isApprovable`, `isReopenable`, …) and in `Form058{Accept,Approval,Cancel,
  Reopen,Update,Delete,Create}Validator`.

### 4.2 Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/v1/form-058` (`/v1/form-058-1`) | create |
| PUT | `/{id}` | update |
| DELETE | `/{id}` | delete |
| PATCH | `/{id}/accept` | no body |
| PATCH | `/{id}/approve` | `{ "finalIcd10Code": "A00" (≤20), "finalIcd10Name": "Cholera" (≤512) }`, both required |
| PATCH | `/{id}/cancel` | `{ "reason": "…" }` required, ≤1000 |
| PATCH | `/{id}/reopen` | no body |
| GET | `/` | list/filter (`direction`, `status`, dates, `icd10Code`, `source`, …) |
| GET | `/by-nnuzb` (058) | lookup by national ID |
| GET | `/{id}` | detail |
| GET | `/affiliated` | forms visible through patient affiliation (§4.4) |
| POST | `/export` | background Excel journal (§13) |
| — | `/{id}/cards`, `/{id}/cards/assign` | declared under the form paths but implemented in the **card** module (dependency stays one-way: card → form) |

Design notes: `Form058.patient` has no `CascadeType.ALL` (form must not
control patient lifecycle); `Form058Location` cascades PERSIST/MERGE only;
`assignedCardId` is `@Deprecated` — use `hasLinkedCards` + the card module's
join table.

### 4.3 Responses and errors

Standard envelope (§1). Statuses: `409 CONFLICT` for wrong-status actions
(e.g. `Form058 accept is not allowed for status: APPROVED`), `403` for wrong
organization. Statistical reports exclude `CANCELED` from the PRIMARY bucket
(`status NOT IN ('APPROVED','CANCELED')`), so a receiver-rejected form is
excluded automatically.

If a form came through an external integration (`sourceIntegrationClientId`
set), every status change — including `accept` — triggers an outbound webhook
to that client; transparent to the frontend (contracts under
`docs/integration/*/webhook`).

### 4.4 Affiliated organizations

An organization can also see a form because the patient's **workplace or
place of study** (`PatientAffiliation` type `WORKPLACE`/`EDUCATIONAL`) is
that organization (e.g. the SES serving a factory or school). It is neither
sender nor receiver, so `GET /v1/form-058?direction=…` never shows it.

```
GET /v1/form-058/affiliated      GET /v1/form-058-1/affiliated
```

Same params as the main listing but **no** `direction`/`organizationId`/
`regionCode`/`districtCode`: `page,size,sortBy,sortDir,status,dateFrom,
dateTo,id,documentValue,icd10Code,source` (+ `hasLinkedCards` on 058 only —
Form 058-1 has no such field). Rows are wrapped:

```jsonc
{ "data": [ { "form": { "id": 123, "status": "CARD_LINKED", "icd10Code": "A09",
                         "senderOrganizationId": 10, "receiverOrganizationId": 20,
                         "patient": { "id": 456 } },
              "affiliationType": "WORKPLACE" } ],
  "meta": { "page": 1, "size": 20, "totalElements": 3, "totalPages": 1 } }
```

Detail needs no special endpoint: `patient.affiliations[]` in the normal
detail lists every affiliation; find the entry with your `organizationId`.

Once a form is `CARD_LINKED`, an affiliated organization may attach an act:
`POST /v1/cards/{cardId}/acts`. The backend allows sender, receiver **or**
affiliated organization; anyone else gets `403 SCOPE_VIOLATION` (same for
`POST …/cards/assign`). There is no "am I allowed" endpoint — attempt and
handle the 403. Notifications for this flow: `*_AFFILIATED_RECEIVED`,
`*_AFFILIATED_CARD_LINKED` (§9).

---

## 5. Form 129

Lab-to-SES notification of serology results (syphilis, hepatitis B,
brucellosis) from AKP/hospital/blood-center labs (`modules.form129`). A
**pure registry**: no card linking, no approval, no attachments, no update.
Surfaces: frontend `/v1/form-129`; integrations
`/integration/v1/{source}/form-129` (DMED uses the same generic path — no
DMED-specific contract).

```
SENT ──accept (receiver)──► ACCEPTED
  └──reject (receiver, reason required, only while SENT)──► CANCELED
```

| Action | Endpoint | Who | Notes |
|---|---|---|---|
| create | `POST /v1/form-129` | sender (caller's active org) | receiver must be `SANEPID_SERVICE` (`error.form129.receiver-not-sanepid`); also registers the patient |
| accept | `PATCH /{id}/accept` `{ "receiverFullName"?: ≤255 }` | receiver only | 409 unless `SENT` |
| reject | `PATCH /{id}/reject` `{ "reason": ≤1000 }` **required** | receiver only | 409 unless `SENT`; the sender cannot withdraw |
| soft delete | `DELETE /v1/form-129/{id}` `{ "reason" }` | `isemid_admin` / `isemid_super_admin` only | `deleteInfo.deleted`; hidden from all lists and selections afterwards |
| list | `GET /v1/form-129` | | `direction` `OUTGOING`/`INCOMING`/`ALL` (**ALL = super admin only**), `status`, `dateFrom/dateTo`, `id`, `documentValue`, `organizationId`, `source`, paging. No `hasLinkedCards`/`icd10Code` |
| detail | `GET /v1/form-129/{id}` | | patient, all results, orgs, cancellation info, audit |

There is no `update` and no `reopen`: once rejected a form stays closed. A
super admin bypasses the organization check on accept/reject, not the status
check.

**Lab result fields.** 13 tests use a pair: `*Outcome` (`NEGATIVE`/`POSITIVE`,
omit if not tested) + free-text `*ResultText` (e.g. a titre): `rw`, `rprVdrl`,
`rpga`, `elisa`, `tpha`, `westernBlot`, `hbsAg`, `hbeAg`, `antiHbcIgG`,
`antiHbcIgM`, `antiHbe`, `antiHbs`, `pcrQualitative`. The brucellosis
`wrightHeddelsonOutcome` is three-state (`NEGATIVE`/`DOUBTFUL`/`POSITIVE`).
All optional on create.

Outbound webhook fires on accept/reject for externally submitted forms.
In-app notifications: `FORM129_RECEIVED` (receiver org, on create),
`FORM129_ACKNOWLEDGED` (sender, on accept), `FORM129_REJECTED` (sender, on
reject).

---

## 6. Patient

`modules.patient` — patient/person record (demographics, national ID,
addresses, workplace/school affiliation). Created as a side effect of
registering a form (`PatientRegistrationService.create()` called from
`CreateForm058Service`). **No REST surface of its own** (no controller
package, no `ApiPaths.Patient`). Entities: `Patient`, `PatientAddress`
(`PERMANENT`/`TEMPORARY`), `PatientAffiliation` (`WORKPLACE`/`EDUCATIONAL`),
`PatientIdentifier`. Layers: `application`, `domain`,
`infrastructure/persistence`, `web` (mapper/request only). No class javadoc
exists — read `PatientRegistrationService` rather than assuming. The
external patient-case lookup contract is in `docs/integration/patient`.

---

## 7. Cards

`modules.card` — five epidemiological investigation card types, each owned by
exactly one case, a `Form058` **or** a `Form0581` (DB check
`chk_card_exactly_one_form`). Only `CARD174`/`CARD175`/`CARD205` may attach to
a `Form0581` (enforced in `CardCommandService.assignCardsToForm0581`); all
five attach to `Form058`.

| Type | Children | Notes |
|---|---|---|
| `CARD161` | 9 child entities + 1:1 `InfectionSourceDetail` (unique `uk_card161_infection_source_detail`) | largest; reference implementation of `ChildCollectionSync` |
| `CARD174` | `LaboratoryTest`, `InfectionMonitoring`, `OutbreakControlMeasure`, `AnimalMeasure` | `serialDocNumber` = card id (server); `lastDiseaseYear` is an integer year; `deratizationArea` in m²; `animalType` is an `ANIMAL_TYPE` catalog code (GET falls back to the 058-1 notification's animal type while empty); `laboratoryTests`/`animalMeasures` child tables; PINFL fields; `form.hospitalPlaceName` (org id); processedArea/eventConducted stay per-row |
| `CARD175` | none | simplest |
| `CARD205` | 3 | two share the `AdministrativeAddress` `@Embeddable` |
| `CARD_TUBE` | 4 | own `InfectionSource` (distinct entity name from CARD161's) |

### Layout and patterns

`domain` (abstract `Card`, JOINED inheritance; one package per type;
`CardType`, `CardStatus`) · `application` (flat `CardCommandService` /
`CardQueryService`, `handler/` with `CardTypeHandler<C,Q,R>` +
`CardTypeHandlerRegistry` + `ChildCollectionSync`) · `mapper` (MapStruct per
type + `CardCaseFieldMapperHelper` resolving formId/formType) · `web`
(`/v1/cards/*`, plus form-scoped paths for form058 and form0581; the 0581
ones are type-restricted).

Services are deliberately flat (all operations share one aggregate and status
rules). Adding a 6th type touches only: `CardType` value, entity package,
request/response DTOs (sealed `CardRequest`/`CardDetailResponse`), mapper,
one new `@Component` handler — registry fails fast at startup on a
missing/duplicate type (`CardTypeHandlerRegistryTest`).

### Status lifecycle (`CardStatus`: NEW, ACCEPTED_BY_USER, REJECTED_BY_USER, IN_PROGRESS, COMPLETED, APPROVED, REJECTED)

```
assignCards → NEW ─accept→ ACCEPTED_BY_USER ─update→ IN_PROGRESS ─complete→ COMPLETED ─supervisor→ APPROVED (final)
               └─reject→ REJECTED_BY_USER ─(supervisor reassign)→ NEW          └─supervisor→ REJECTED ─update/complete again→
```

Predicates on the enum (`canBeAcceptedByUser`, `canBeUpdated`,
`canBeDeleted`, `canBeReassigned`, `canBeApprovedBySupervisor`, `isFinal`…);
`CardStatusTest` pins the matrix. Rules:

- `PUT /cards/{id}` always lands on `IN_PROGRESS` ("Save" of a step-by-step
  fill-in).
- `complete` = "Save and Complete" → `COMPLETED`; there is no "pending
  approval" endpoint — supervisors list `status=COMPLETED&assignedById=<self>`.
- Delete only before real data exists (`NEW`/`ACCEPTED_BY_USER`/
  `REJECTED_BY_USER`).
- Reassign is supervisor-only (`PATCH /cards/{id}/supervisor/reassign`), only
  from `REJECTED_BY_USER`, and checks the caller is the card's `assignedById`.

### Child collections: `ChildCollectionSync`

Children are `@OneToMany(cascade = ALL, orphanRemoval = true)`. Updates are an
**id-matching upsert**: a request item whose `id` matches an existing child
updates that managed instance; no/unknown id inserts; an existing child
missing from the list is orphan-removed. **API contract:** to edit an
existing child, echo back the `id` from a previous GET/PUT; omitting it
creates a new row (and deletes the old one).

### Legacy fixes not carried over

`Card.users` no longer cascades ALL (deleting a card deleted `User`s);
`cardType` has no builder/setter; owner FK is nullable-pair guarded by the
check constraint; optional primitives became boxed types; entities carry no
Jackson annotations (polymorphism lives in the DTO sealed interfaces); the two
`InfectionSource` entities got explicit `@Entity(name=…)` (guard:
`EntityNameUniquenessTest`).

### Testing notes

Mapper impls are instantiated directly; because they `uses`
`CardCaseFieldMapperHelper`, tests must inject it via
`ReflectionTestUtils.setField(mapper, "cardCaseFieldMapperHelper", …)`.
Mockito cannot mock the sealed DTO interfaces — use `Card175Request`. No
Testcontainers/`@DataJpaTest`: schema issues only show when running against
Postgres.

---

## 8. Acts and LIS

`modules.act` — lab/procedure acts (далолатнома) attached to a card and sent
to the external LIS. Handler-per-type, mirrored from `card`. Outbound client:
`integration/lis`.

### 8.1 Types

| Type | Meaning (`Act.xlsx`) | Shared `EmployeeInfo` | Reaches LIS |
|---|---|---|---|
| `ACT153` | water sampling | yes | yes |
| `ACT154` | food sampling (absorbed Act155) | yes | yes |
| `ACT156` | external-environment swabs | no (own columns) | **no** |
| `ACT223` | soil sampling | yes | yes |
| `ACT224` | sanitary inspection | no | **no** |

`Act155` was removed 2026-08-24 (functionality merged into 154; migrations
`zzz-card-act/20260824-1000-drop-act155.xml`,
`reference/20260824-1000-deactivate-act155-catalog-entry.xml`). Sending 156/224
to LIS is a caller error (`LisUnsupportedActTypeException`). Only 153/154/223
share `EmployeeInfo` (`sampler`/`participant`, with
`identifierType`/`identifierValue` so a participant's name can be resolved from
the citizen registry); changes to it do not reach 156/224.

No standalone `POST /v1/acts`: acts are created only by
`POST /v1/cards/{id}/acts` (bulk, one blank act per type, all attached to the
same employees). Employees see them in `GET /acts/mine`, fill via
`PUT /acts/{id}`. Layout: `domain` (abstract `Act`, JOINED, `card_id` FK,
`act_users`; embeddables `ActSubject`, `LisInfo`, `EmployeeInfo`, …; one
`XxxDetail` per type) · `application` (`ActCommandService`,
`ActLisSendService`, `ActQueryService`, `ActStatsQueryService`, handlers +
fail-fast registry) · `mapper` · `web` (`/v1/acts/*`, `/v1/cards/{id}/acts/*`).

### 8.2 Status lifecycle (`ActStatus`)

`NEW`, `IN_PROGRESS`, `READY`, `SENT`, `SEND_FAILED`, `RETURNED_BY_LIS`,
`RESULT_RECEIVED`, `COMPLETED` (8 values; no accept/reject gate).

```
NEW → IN_PROGRESS → READY → SENT → RESULT_RECEIVED ──close──→ COMPLETED (terminal)
                      ↑       │ └──→ RETURNED_BY_LIS ──┐
      SEND_FAILED ←───┘ (send failed)                   │ edit → ready → send again
        ↑  └─────────────────────────────────────────────┴────── (also from RESULT_RECEIVED if the doctor disagrees)
```

| Status | Meaning | UI |
|---|---|---|
| `NEW`/`IN_PROGRESS` | being filled | edit; mark ready |
| `READY` | ready | edit (drops to `IN_PROGRESS`) or send |
| `SENT` | LIS accepted; result pending | read-only, not deletable |
| `SEND_FAILED` | the send itself failed (never reached LIS) | fix and resend; **deletable** |
| `RETURNED_BY_LIS` | LIS accepted, then sent it back for rework | edit → ready → send; **not deletable** |
| `RESULT_RECEIVED` | result in, awaiting the attached employee (doctor) | **Yopish** (`PATCH /acts/{id}/close`) or edit → ready → send again (lands in LIS as a new request); **not deletable** |
| `COMPLETED` | doctor closed it | read-only, terminal |

Operations:

- `update` (`PUT /acts/{id}`): re-saveable from any non-`SENT`/`COMPLETED`
  status, always → `IN_PROGRESS`.
- `markReady` (`PATCH …/ready`): from `IN_PROGRESS`, `SEND_FAILED`,
  `RETURNED_BY_LIS`, `RESULT_RECEIVED` → `READY`. Also the point where
  **completeness is checked**: `subject` must be complete (`type` required;
  `LEGAL_ENTITY` → `tin` + `name`; others → `actualAddress`) else `400
  error.act.subject-incomplete`. Drafts may be saved incomplete; no other
  content field is validated server-side (only `@Size`/format, `tin` = 9
  digits) — if LIS needs more, enforce client-side.
- `send-to-lis` (`POST …/send-to-lis`, body `{ labId, priority, paid?, force? }`):
  `READY`/`SEND_FAILED`/`RETURNED_BY_LIS`/`RESULT_RECEIVED` → `SENT`, stamps
  `lisInfo.attempt`/`sentDate`, clears `lastError`. Once LIS accepted the act
  before (`lisInfo.actId` set) the backend sends `force=true` itself, so a
  re-send is a fresh LIS request; client `force` still covers the case where
  LIS's id was never learned (timeout). `409` from LIS (duplicate
  `senderActNumber`) → retry with `force: true`.
- `lis/callback` (`POST /v1/acts/{id}/lis/callback`) — **called by LIS, never
  the frontend.** Stores the full body in `lisInfo.response`;
  `LisCallbackInterpreter` decides `SENT → RESULT_RECEIVED` (default) or
  `SENT → RETURNED_BY_LIS` (reason also in `lisInfo.lastError`).
  **TODO(LIS-spec):** the real "returned" signal is unknown; a broad heuristic
  is used (`status`/`state`/`decision` containing return/reject…, or
  `rejected`/`returned` booleans) so a genuine return outside the heuristic
  looks like a result (doctor can still re-send).
- `close` (`PATCH …/close`, no body, attached employee, `UPDATE` permission):
  only from `RESULT_RECEIVED` (else 409); stamps `closeInfo` →
  `lisInfo.closedById`/`closedAt` (null for acts completed before 2026-09-30).
- `delete`: soft delete, blocked from `SENT`, `RESULT_RECEIVED`, `COMPLETED`,
  `RETURNED_BY_LIS` (`ActAlreadySentToLisException`).

### 8.3 LIS correlation, auth and the two "lab" ids

- Correlation is by URL: `redirectUrl = {callbackBaseUrl}/v1/acts/{actId}/lis/callback`
  is placed in the push payload (`LisUrlFactory.callbackUrl`); LIS also echoes
  its own id (top-level `"id"` → `lisInfo.actId`), informational only.
  `senderActNumber` = our act id.
- **Auth of the callback is unconfirmed.** `/lis/callback` is an ordinary
  `/v1/acts/**` endpoint (`isAuthenticated()`, bearer token); there is no
  separate API-key/IP channel like `integration/inbound`. Confirm LIS can
  present a valid token before relying on it.
- Two unrelated lab ids: `lisOrganizationId` (act content; an id in our own
  `iam.Organization`; informational, not cross-checked) vs `labId` in
  `send-to-lis` (LIS's internal laboratory id, goes into
  `/create-act/{labId}`). Source the `labId` options from the **proxied**
  `GET /v1/lis-reference/organizations` (+ `/{id}/departments`). A direct call
  to `lis.sanepid.uz` returns `401` for everything; the single LIS API key
  (`integration.lis.api-key`) stays server-side.

### 8.4 LIS reference endpoints (proxied + cached)

All `isAuthenticated()`; failures surface as localized `502`/`504`
(`LisException`), never raw traces.

| Endpoint | LIS path | Notes |
|---|---|---|
| `GET /v1/lis-reference/organizations?name=` | `GET /sesorgs` | full list (48 in test env) |
| `GET /v1/lis-reference/organizations/{id}/departments` | `GET /departments/{orgId}` | |
| `GET /v1/lis-reference/conditions` | `GET /reference-dictionaries?type=CONDITIONS` | only `CONDITIONS` wired (7 rows) |
| `GET /v1/lis-reference/{professions,research-types,categories,item-types}?search=&page=0&limit=50` | `POST /professions` … | **paged**: `professions` alone is 12k+ rows and a full fetch takes minutes (exceeds the 15s read timeout); `limit` capped at 200 server-side → build search-as-you-type pickers. `professions` is wrapped/localized (`{list:[{id,code,name}], total}`, `name` follows `Accept-Language`); the other three return LIS's raw `{list,total}` |

Cache: Caffeine `sync=true` — 1 h for organizations/departments/conditions,
30 min per `(search,page,limit)` for the paged four. Frontend caching optional.
Not yet proxied: `PURPOSE`, `packageTypeInfo`, `conservationTypeInfo`,
`sampleTypeInfo` dictionaries (LIS naming unknown) — those four fields are
entered manually with empty `id`.

### 8.5 What reaches LIS (153/154/223)

`ActLisPayloadMapper` forwards saved values one-to-one; no defaults, no
rejection.

| LIS field | Source |
|---|---|
| `tin`, `organizationName`, `organizationAddress`, `organizationLegalAddress` | `subject.tin/name/actualAddress/legalAddress` |
| `purpose` | purpose name (uz) — `goal` is derived from the `PURPOSE` catalog name |
| `conditions`, `noteConditions`, `packageType`, `preservationMethod` (153) | `description.uz` only (Ru captured but not sent) |
| `sampleTakenDate`, `deliveryDateToLaboratory` | Tashkent local → UTC ISO-8601 |
| `involvedPersonName`, `involvedProfessionId` | `participant` |
| `fullNameOfDoctor` | the **currently signed-in sender**, not `act.sampler` |
| `collectorProfessionId` | `sampler.positionId` only (name never sent) |
| `manufacturer`, `manufactureDate`, `docNumber` (154) | act-level fields |
| `document` | sampling-basis name (uz) (154: + free text) |
| `redirectUrl`, `paid`, `priority` | callback URL / request body |
| `selectionActItems` | per-type sample rows; `sampleQtUnit` forwarded for **ACT154 only** (enum name as-is) |

Known mapping gaps (verify against LIS's real schema): `packageType` and
`manufacturer`/`manufactureDate` are sent at act level but the legacy client
nests them per `selectionActItems[]` (multi-sample acts with different values
lose data); ACT153's quantity pair (`sampleVolume`/`sampleVolumeUnit` vs
`sampleQt`/`sampleQtUnit`) is not sent — LIS must say which it wants;
`locationOwnership` and per-item `itemOrder` exist in legacy only.

### 8.6 Subject block, catalogs and DTO changes (2026-09-29/30)

- `institution` → **`subject: { type, tin, name, legalAddress, actualAddress,
  label }`** on all five types (`type`: `LEGAL_ENTITY`/`GEOGRAPHIC_POINT`/
  `PHYSICAL_PERSON`; for non-legal-entity `tin/name/legalAddress` are cleared
  server-side). The free-text `subject` column was dropped; `label` is the
  registry label. ACT156/224 flat tin/institution columns and 224's
  `nameOfInstitution`/`addressOfInstitution` were folded into it (data moved).
- `actNumber` = act id, server-generated, lives on base `act` (not in
  requests); equals the LIS `senderActNumber`.
- `purposeCode` (request) → `purpose: {code,name}` (response); catalog
  `type=PURPOSE`, **code = LIS purpose id** (`1–9, 44–47, 115`). Unknown code →
  `400 error.act.purpose-not-found`.
- `samplingBasisCode` (catalog `SAMPLING_BASIS`: `PLANNED`, `UNPLANNED`,
  `EPID_INDICATION`, `APPLICATION`, `CONTRACT`, `AUTHORITY_REQUEST`,
  `NORMATIVE_DOC`, `MONITORING`, `OTHER` — list is a proposal awaiting product
  confirmation) on 153/154/223; ACT154 also `samplingBasisText` (≤500). Unknown →
  `400 error.act.sampling-basis-not-found`. Legacy rows: `code=null`, `name` =
  stored text.
- Removed from requests: 153 `goal`, `lisOrganizationId`, `laboratoryAddress`,
  sample `sampleQtUnit`, `objectTypeId`, `objectCode`; 154 `title`,
  `documentConfirmSampling`, `lisOrganizationId`, `laboratoryAddress`,
  `sampleName`; 223 `lisOrganizationId`, `laboratoryAddress`; 156
  `lisOrganizationId`, `laboratoryAddress`. Legacy values stay readable in
  responses.
- `lisInfo` (always present on every `Act…DetailResponse`): `attempt` (0 =
  never sent), `sentDate`, `actId`, `lastError` (last send failure **or** return
  reason; cleared next attempt), `response` (full callback body once
  `RESULT_RECEIVED`/`COMPLETED`/`RETURNED_BY_LIS`), `closedById`, `closedAt`.
- `ActTableResponse`: `id, actType, actTypeName, status, subject (object),
  actNumber, cardId, cardType, assignedById, createdAt`. Filters: `actType`
  on `GET /v1/acts`, `/mine`, `/v1/cards/{id}/acts`; real `sortBy` values:
  `id`, `actType`, `status`, `createdAt`, `updatedAt`. `GET /acts/{id}/pdf` has
  the same payload as the detail minus `audit` (draw the blank from the loaded
  act).
- Migrations (all in `zzz-card-act/` unless noted): `20260902-1200` (act
  number to base), `20260929-1200…1800` (number backfill, subject, purpose
  code, sampling basis ×3, 224 duplicate drop), `20260930-1000-act-close-info`;
  `reference/20260929-1800-seed-act-purpose-sampling-basis-catalogs.xml`.

### 8.7 RBAC

Act controllers enforce `ATTACH_ACT` via `@PreAuthorize`:

| Action | Endpoints | Authority |
|---|---|---|
| `READ` | `GET /acts/mine`, `/acts/{id}`, `/pdf`, `/cards/{id}/acts` | `PERMISSION_ATTACH_ACT_READ` |
| `VIEW_ALL` | `GET /v1/acts` (org-wide) | `PERMISSION_ATTACH_ACT_VIEW_ALL` |
| `ASSIGN` | `POST /cards/{id}/acts` | `…_ASSIGN` |
| `UPDATE` | `PUT /acts/{id}`, `…/ready`, `…/send-to-lis`, `…/close` | `…_UPDATE` |
| `DELETE` | `DELETE /acts/{id}` | `…_DELETE` |

`/lis/callback` stays `isAuthenticated()`. Grants: `isemid_epidim_head` READ,
VIEW_ALL, ASSIGN, UPDATE; `isemid_epidemiologist` and
`isemid_assistant_epidemiologist` READ, UPDATE; `isemid_super_admin`/
`isemid_admin` full (admin except DELETE/MANAGE) via the runAlways
`zzz-rbac-grants` migration. Roles with no `ATTACH_ACT` grant (e.g. lab roles)
get `403` on `/v1/acts/**`. Seed: `iam/20260825-1200-seed-act-permission-subject.xml`,
`iam/20260902-1300-seed-act-view-all-action.xml`. Configure roles with the
`ATTACH_ACT` subject, not the card ones (`USER_INCOMINGS`/`ATTACHED_CARDS`).

### 8.8 Notifications

`ACT_ASSIGNED` on assignment; `ACT_LIS_RESPONSE` on `SENT → RESULT_RECEIVED`
and `SENT → RETURNED_BY_LIS` (toggle `notification.act-lis-response.enabled`).
The notification does not say which — `GET /v1/acts/{entityId}` and read
`status`/`lisInfo`. Closing sends none. A failed *send* has no push; it just
sits in `SEND_FAILED`.

### 8.9 Frontend sequence

```
PUT /acts/{id} (pick dictionary values from /v1/lis-reference/**)
PATCH /acts/{id}/ready
POST  /acts/{id}/send-to-lis { labId (from proxied orgs), priority, paid }   → SENT
… LIS works; callback arrives …                                              → RESULT_RECEIVED / RETURNED_BY_LIS
wait for ACT_LIS_RESPONSE (SSE/list), GET the act, show lisInfo
PATCH /acts/{id}/close                                                       → COMPLETED
```

The result never arrives on the send response — do not `await` it.

---

## 9. Notifications

`orchestration.notification` — **internal in-app notifications only**, for
users signed into this SPA. Integration clients are *not* informed here; they
get outbound **webhooks** (`OutboundWebhookEventListener` →
`OutboundWebhookDispatchService`). The receiver org's human users still get
`*_RECEIVED` regardless of where the form came from. No message broker
exists: events are produced and consumed in-process.

### 9.1 How notifications are created

No business service knows about notifications. `NotificationEventListener`
listens to the same `EntityCreatedEvent` / `StatusChangedEvent` that
`platform.audit` consumes (published `AFTER_COMMIT`), runs `@Async` on
`applicationTaskExecutor` in its own `REQUIRES_NEW` transaction, and **reloads**
the entity by id (never trusts event payload). So a slow/failing fan-out can't
delay or roll back the source change. The export-ready trigger uses its own
`ExportJobCompletedEvent` (single recipient = submitter, and the actor is *not*
excluded). The acting user is excluded from every other recipient list.

Gotchas already hit: the settings-enabled check must run **before** any
repository lookup (Java evaluates arguments eagerly);
`ExportJobService.completeJob` is self-invoked, so a plain `@Transactional`
would be ignored and `AFTER_COMMIT` would never fire — it publishes inside
`requiresNewTransactionTemplate.executeWithoutResult`.

### 9.2 Triggers and recipients

| Type | Trigger | Recipients |
|---|---|---|
| `FORM058_RECEIVED` / `FORM0581_RECEIVED` | form created | active users of `receiverOrganizationId` |
| `*_ACKNOWLEDGED` | `SENT → ACCEPTED` | sender org |
| `*_CARD_LINKED` | `ACCEPTED → CARD_LINKED` (exact pair, so a re-link no-op doesn't refire) | sender org |
| `*_APPROVED` | any `→ APPROVED` | receiver org |
| `*_CANCELED` | any `→ CANCELED` | **both** orgs (either side may cancel) |
| `*_REOPENED` | `CANCELED → SENT` | **both** orgs |
| `*_AFFILIATED_RECEIVED` | form created | every affiliated org (workplace/school) **excluding** sender/receiver |
| `*_AFFILIATED_CARD_LINKED` | `ACCEPTED → CARD_LINKED` | same — cue that cards exist and an act can be attached |
| `FORM129_RECEIVED` | form129 created | receiver org |
| `FORM129_ACKNOWLEDGED` / `FORM129_REJECTED` | accept / reject | sender org |
| `CARD_ASSIGNED` | `createBlankCards` (new per-card event) | `card.getUsers()` |
| `CARD_ACCEPTED_BY_USER` / `CARD_REJECTED_BY_USER` / `CARD_COMPLETED` | user actions | `Card.assignedById` |
| `CARD_APPROVED` / `CARD_REJECTED` | supervisor decision on `COMPLETED` | `card.getUsers()` |
| `ACT_ASSIGNED` | `assignActs` | `act.getUsers()` |
| `ACT_LIS_RESPONSE` | `SENT → RESULT_RECEIVED` or `→ RETURNED_BY_LIS` | `act.getUsers()` |
| `EXPORT_READY` | export job finished | submitter only |

(`*` = `FORM058` and `FORM0581`.) 28 types in total. A past bug: the
acknowledged handlers matched `SENT → RECEIVED`, a status that has never
existed, so `FORM0581_ACKNOWLEDGED` never fired and FORM058 wasn't routed at
all — fixed.

Each notification kind is gated by `notification.<kebab-type>.enabled`
(e.g. `notification.form058-received.enabled`,
`notification.form129-rejected.enabled`, `notification.export-ready.enabled`)
in `system_settings`, read via `SystemSettingResolver.resolveBoolean(key, true)`;
no row = enabled. Editable from `/v1/admin/settings` or the dev panel's
`/v1/dev/settings` (same table). Disabled types simply stop being created —
the API shape is unchanged.

**Model.** One row per (event, recipient) — "my unread" is a single indexed
query on `(recipient_user_id, read, occurred_at)`. `organization_id` is
best-effort (form's receiver org, walked up from card/act) and not used for
reads. The text is stored as `messageKey` + `messageParams` (JSON) and
resolved per viewer locale at read time via `MessageResolver`.

### 9.3 Delivery API (`/v1/notifications`, Swagger group "Notifications")

All require the bearer token and are scoped to the caller (`@CurrentUser`) —
no user-id parameter exists.

| Method | Path | Purpose |
|---|---|---|
| GET | `/v1/notifications` | paged, newest first; `page` (1-based), `size` (≤200), `unreadOnly` |
| GET | `/v1/notifications/unread-count` | badge (`data`: number) |
| GET | `/v1/notifications/unread-count/by-type` | map of **every** `NotificationType` → count (0 if none) |
| POST | `/v1/notifications/{id}/read` | mark one |
| POST | `/v1/notifications/read-all` | mark all |
| GET | `/v1/notifications/stream` | SSE `unread-count` event every second |

```jsonc
// GET /v1/notifications → data[i]
{ "id": 501, "type": "CARD_ASSIGNED", "entityType": "CARD", "entityId": 50,
  "organizationId": 12, "message": "…localized…", "read": false,
  "readAt": null, "occurredAt": "2026-08-11T09:12:03Z" }
```

Click-through: open `entityType`/`entityId` (`FORM058` → `GET /v1/form-058/{id}`,
`ACT` → `GET /v1/acts/{id}`, …). Affiliation types are handled identically —
only the *reason* differs; route into the `/affiliated` list or open the
detail rather than assuming you are sender/receiver.

**SSE.** Use `fetch` streaming (§1). `@microsoft/fetch-event-source`:

```ts
fetchEventSource(`${API}/v1/notifications/stream`, {
  headers: { Authorization: `Bearer ${token}` }, signal: controller.signal,
  onmessage(ev) { if (ev.event === "unread-count") setUnread(Number(ev.data)); },
});
```

The stream only carries the **count**; refetch the list when the panel opens.
The server polls once a second, so decrement the badge optimistically after
a read call. Stream service (`NotificationStreamService`) mirrors
`ExportJobService.streamProgress`; on client disconnect it completes the
emitter quietly (a JSON error body can't be written onto a committed
`text/event-stream` response).

Tests: `NotificationEventListenerTest` (fan-out, actor exclusion, disabled
short-circuit),
`CardCommandServiceAssignCardsTest.publishesAnEntityCreatedEventPerCreatedCard`.

---

## 10. DHP server-to-server integration

`integration/dhp` — backend-to-DHP lookups over a **client_credentials (M2M)**
client, mapped into our own shapes. Not the same as `app.auth.providers.dhp.*`
(validates inbound DHP tokens) or `app.auth.login.providers.dhp-web.*`
(browser login); this is `integration.dhp.*`.

| Endpoint (`GET`, `isAuthenticated()`, no `X-Organization`, role validation on) | Upstream | Result |
|---|---|---|
| `/v1/dhp/employment?ni=<14 digits>` | `GET https://egov.dhp.uz/mol/citizen/employment/by-ni?ni=` | `{nnuzb, items[{organizationName, organizationTin, position, startDate, endDate, current, raw}]}` |
| `/v1/dhp/immunization?ni=<14 digits>` | FHIR R5 `GET /Patient?identifier=` then `GET /Immunization?patient=Patient/<id>` (paged) | `{nnuzb, items[{fhirId, status, vaccineCode, vaccinationName, serialNumber, vaccinationDate, expirationDate, doseVolume, doseUnit, doseNumber, targetDiseases[], performerName}]}` |

No data (404, empty body, empty Bundle) = empty `items`, not an error. Invalid
`ni` = `400 VALIDATION_FAILED` (`validation.nnuzb.format`) before any upstream
call. Every `ImmunizationItem` field except `status` matches Card161's
`VaccinationRequest/Response`, so a DHP record can be dropped into a card's
`vaccinations[]`: `doseVolume` is fractional (`NUMERIC(10,3)`), `fhirId` links
back to the source (null for hand-entered), `targetDiseases` is stored
`"; "`-joined (migration `zzz-card-act/20260928-1000`). `entered-in-error`
records are dropped; newest first.

**Call mechanics** (`DhpHttpClient`): `DhpAccessTokenProvider` caches a bearer
token (`POST <token-url>`, `grant_type=client_credentials`, id/secret **in the
form body**; ~1 h, no scopes; refreshed 60 s early, single-flight) → GET inside
the shared `dhp` circuit breaker → `401` drops the token and retries once
(second `401` = `DhpAccessDeniedException`). Mapping: `400/422` →
`DhpRequestRejectedException` (400); `403` → 502 `dhp.error.access_denied`;
`404` → empty; other 4xx/5xx/transport → `DhpIntegrationException` (502/504).
Rejected/denied are breaker `ignore-exceptions`. Immunization follows Bundle
`next` links (max `fhir.max-pages`, size `fhir.page-size`) **only** while they
stay under `integration.dhp.fhir.base-url` — a server-supplied link elsewhere
would leak the bearer token.

**Configuration** (`integration.dhp.*`; prod has no credential defaults and
the app still boots — calls then fail with `dhp.error.not_configured`):
`token-url` [`DHP_M2M_TOKEN_URL`], `client-id`, `client-secret`,
`employment.base-url`/`by-ni-endpoint`, `fhir.base-url`
[`DHP_FHIR_BASE_DOMAIN`], `fhir.immunization-endpoint`, `fhir.patient-endpoint`,
`fhir.identifier-system` (when set the search value is `system|ni`),
`fhir.page-size`, `fhir.max-pages`, `connect-timeout`, `read-timeout`,
`token-expiry-skew`.

**Verified live (playground, 2026-09-21):** FHIR access is now granted
(`Patient`, `Immunization`, `metadata` → 200, FHIR 5.0.0). Immunization
cannot be filtered by patient identifier directly: chained
`patient.identifier=` is rejected as an unsupported parameter and **silently
returns every Immunization unfiltered** (a data-exposure risk); the
`patient:identifier=` modifier is accepted but never matches. Hence the
two-step search. Employment payload is a JSON-RPC envelope
(`result.positions[{org, org_tin, position, begin_date}]`, no `end_date`
sample seen yet); `DhpEmploymentMapper` accepts the real names plus synonyms,
and every item keeps the untouched source in `raw`.

---

## 11. Reference data: population and manual reports

Both live in `modules.reference` and are shown under **Ma'lumotnomalar**.
Envelope/errors as §1.

### 11.1 Population («Aholi soni», `ref_population`)

One row = permanent population of one MHOBT/SOATO territory in one calendar
year (stat.uz SDMX dataset 246). Per-capita denominator for reports (read
server-side by `PopulationLookupService`; consumed by Form 10/11 and the
Statistika `population` field, and Analitik hisobot). Seeded 2025–2026
(`base/population_reference.csv`), refreshable. **Every endpoint is
`isemid_super_admin` only** (`403` otherwise); show the menu item to super
admins only. Swagger tag "Reference - Population".

| Field | Notes |
|---|---|
| `id` | one territory in one **year** |
| `geoType` | `REPUBLIC`/`REGION`/`DISTRICT`/`OTHER` (`OTHER` = SOATO territory with no active region/district match, currently only Ko'kdala `1710240`) |
| `soatoId` | `1700` republic, 4-digit region, 7-digit district |
| `regionCode`/`districtCode` | resolved codes; `null` for `REPUBLIC`/`OTHER` |
| `year` | 1900–2100 |
| `population` | **absolute head-count** (feed publishes thousands; backend ×1000) — don't divide |
| `source` | `SDMX` or `MANUAL` (`/sync` never overwrites `MANUAL`) |
| `name` | response-only, localized from `ref_region`/`ref_district` |

Endpoints (base `/v1/references/populations`):

- `GET /years` → `[2026, 2025]` descending (year dropdown, default = first).
- `GET ?year=` → republic row first (the feed's own `1700` figure, not the
  sum of regions), then regions by localized name, then any `OTHER` rows
  (`hasChildren:false`, name falls back to the SOATO number). `year` omitted =
  latest. Node fields: `id, code, name, population, hasChildren, geoType,
  soatoId, source, year`.
- `GET ?year=&regionCode=UZ-AN` → that region's districts for the same year.
- `GET /{id}` → detail: the year-row plus `years[]` (the territory's whole time
  series, each with its own `id`), localized `regionName`/`districtName`,
  `audit` (`createdBy/updatedBy` are `null` for seed/sync rows; `{id,
  firstName, lastName, middleName}` after a manual change). No
  field-by-field change log in this response.
- `POST` create: `geoType, soatoId (>0), year, population (≥0)` required,
  `regionCode/districtCode` optional (set them so reports pick the row up);
  `(soatoId, year)` unique → `409`. Gets `source=MANUAL`.
- `PUT /{id}`: `geoType, regionCode, districtCode, population`; `soatoId` and
  `year` are identity (to move a figure to another year, delete and
  re-create). `409` on a soft-deleted row.
- `DELETE /{id}`: soft delete. A later sync can bring an SDMX row back —
  deleting is really meaningful only for `MANUAL`/stale `OTHER` rows.
- `POST /sync` (no body; "SDMX'dan yangilash"): upserts by `(soatoId, year)`
  for years ≥ `min-year` (2025) from `app.reference.population.sdmx-url`;
  returns `{processed, inserted, updated, skippedManual, unmatched, minYear,
  sdmxLastModified}`. `502 UPSTREAM_ERROR` = feed unreachable, table untouched.
  Takes seconds (≈440 rows) — disable the button while running, then refresh.

UI: year dropdown, expandable republic → region → district tree, row ✎ opens
a detail drawer (`years[]` series + `audit`), 🗑 only for `MANUAL`/stale
`OTHER` rows. (`Population extends AuditableEntity`, `AuditEntityType.POPULATION`.)

### 11.2 Manual reports («Qo'lda kiritiladigan hisobot ta'riflari», `ref_manual_report`)

One record = one nosological form: code, localized names, "include in total"
flag, a set of ICD-10 codes, and a set of **report-type tags** («Hisobot
turi») deciding which reports show the row (Form 12, 13, 28.1, 28.2). The
statistical reports read this server-side (`findByReportType("FORM_12")`),
so tagging a row here is the only wiring. Base `/v1/references/manual-reports`,
Swagger tags "Reference - Manual Reports" / "Reference - Catalogs". List/view:
any authenticated user; create/update/delete: `isemid_admin`/`isemid_super_admin`
(`@adminAccessGuard.isAdmin()`) — show those buttons to admins only.

| Field | Notes |
|---|---|
| `code` | unique, case-sensitive, ≤50, trimmed (not upper-cased); treat as immutable in the UI (API allows changing it on update) |
| `shortName` | ≤100, report header (usually an ICD range, e.g. `A15-A19`) |
| `nameUz/nameUzCyril/nameRu/nameKaa` | ≤255, all optional; fill at least uz + ru |
| `includeInTotal` | default `true` ("Umumiy yakunga qo'shilsin"); sums into the `Jami` line |
| `icd10Codes` | set, **≥1 required**, stored trimmed + UPPER-CASED; `+`/`*` suffixes (`A39.0+`, `J17.0*`) match `ref_icd10` — keep them |
| `reportTypes` | set, optional; codes from the `REPORT_TYPE` catalog: `FORM_12`, `FORM_13`, `FORM_28_1`, `FORM_28_2`; stored case-preserved, matched case-insensitively; **not validated against the catalog server-side** — keep the UI a closed select |
| `deleted` | soft delete; never returned by reads |

Errors: `400` (code blank/>50, name>255, `shortName`>100, `icd10Codes`
empty), `403`, `404`, `409` (duplicate `code` on create; update of a
soft-deleted row).

**«Hisobot turi» field (the form's multi-select).** Load options with
`GET /v1/references/catalogs/types/REPORT_TYPE` (optional `limit` ≤200, `name`
search) → `[{id, type, code, parentCode, name}]`; `name` is already localized.
Bind on `code`, display `name`, cache. Not required (empty = row appears in no
report), place it near «Umumiy yakunga qo'shilsin», helper text «Bu ta'rif
qaysi statistik hisobotlarda chiqishini belgilaydi». Send `reportTypes` as an
array of codes on POST/PUT (omit or `[]` for none; order irrelevant;
duplicates removed). `PUT` is a **full replace** — send the complete
`icd10Codes` and `reportTypes`, not a delta. On edit, prefill from
`GET /{id}`; a code no longer in the catalog still comes back — show the raw
code as a fallback.

Table: `GET ?page&size(1–200)&sortBy(id|code|shortName|nameUz|nameUzCyril|nameRu|nameKaa|createdAt|updatedAt)&sortDir&name&code`.
Row: `id, code, shortName, names, includeInTotal, deleted` — it does **not**
include `reportTypes`/`icd10Codes` (use `GET /{id}`; ask backend to add a
column rather than N+1). Other reads: `GET …/code/{code}` (lookup shape),
`GET …/icd10/{code}` (rows whose set contains that diagnosis).

Seed: rows + ICD-10 + type tags came from legacy `isemid` via an idempotent
Liquibase migration (2026-09-04).

---

## 12. Reports

`modules.report`, Swagger tags `Report — Form N`. All report endpoints need
`isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')`
(`403` otherwise). Drill-down hierarchy comes from
`report.shared.ReportHierarchyService`.

### 12.1 Shared rules

**Scope-dependent drill-down:**

| Caller scope | `root` returns | Levels below |
|---|---|---|
| Republican / ALL | regions + `Jami` | region → district → organization |
| Region | districts + `Jami` | district → organization |
| District | organizations + `Jami` | none |
| Single organization | its own row, **no** `Jami` | none (`hasChildren=false`); `children` → `[]` |

`children` params `regionCode`/`districtCode`: none = caller's own first
level; `regionCode` alone = its districts; `districtCode` = its organizations.
Out-of-scope codes → `403 organization.scope_violation`. `Jami`
(`code:"TOTAL"`) only on `root`. Organization is the lowest level the system
models.

**Diagnosis-code rule.** Confirmed/`APPROVED` counts match `diagnosisCode` on
`final_icd10_code` **only** (no fallback to initial `icd10_code`):
Form 1/4 CONFIRMED blocks and Form 8/10/11/12/13/28.1/28.2/Statistika
top-diseases; Form 12/13/28.x also require `final_icd10_code is not null`.
Only PRIMARY-notification blocks (Form 1/4 PRIMARY, Form 6, Form 9) match
"initial OR final". Forecast counts every live notification (`status <>
'CANCELED'`) and matches initial OR final.

Common conventions: counts are over `form058 ∪ form058_1`, `deleted=false`,
`sender_organization_id` inside the caller's scope (and the drilled node),
bucketed by `created_at` (Tashkent); age = complete years (or months) **at
`created_at`**, not today; empty periods return rows with zeros; all
read-only, no migrations.

**Catalog of reports:**

| Report | Base | Shape |
|---|---|---|
| Form 1, 4 | `/v1/reports/form-1`, `form-4` | aggregate drill-down; CONFIRMED + PRIMARY blocks (Form 4: social category bucketing) |
| Form 6 / 8 / 9 | `form-6`, `form-8`, `form-9` | year-over-year with a per-node breakdown: 6 by age group (primary/undecided), 8 by social category (`APPROVED` only), 9 by calendar month (two metrics: registered by primary notification + hospitalized) |
| Form 2 (manual) / 3-1 / 3-2 / 7 | `form-2/manual-entries`, `form-3-1/entries`, `form-3-2/entries`, `form-7/entries` | manual-entry CRUD (own `rp_*` table, scope-filtered listing, owner-only edit); Form 7 and Form 2 manual also offer a form058-derived prefill snapshot (age/gender + confirmed diagnoses) |
| Form 10 / 11 | `form-10`, `form-11` | §12.2 |
| Form 12 | `form-12` | disease-first (§12.3) |
| Form 13 | `form-13` | geography-first transpose of 12 (§12.3) |
| Form 28.1 / 28.2 | `form-28-1`, `form-28-2` | §12.4 |
| Statistika | `statistics` | §12.5 |
| Forecast | `forecast` | §12.6 |
| Analitik hisobot | `analytic` | ad-hoc builder: caller multi-selects regions + ICD-10 codes for `[from,to]`; `POST …/compute` previews per-region confirmed counts (`APPROVED`, final code only) vs current-year population; the client stitches the rate into a rich-text `content`, saved as `TEMPLATE` or `FINAL`; owner-only edit; `GET /{id}/docx` export |
| Map | `/v1/reports/map/points` | form058-only lat/lng points (058-1 has no coordinates) with region/district/diagnosis/status filters |

### 12.2 Form 10 and Form 11

Two structurally identical morbidity reports — geography-first, confirmed
(`status='APPROVED'`) only, a **"Joriy davr" / "Yig'ma"** two-block period
model, intensive rates per territory population.

| | Form 10 | Form 11 |
|---|---|---|
| Title | «Respublika bo'yicha ma'muriy hududlar kesimida yuqumli kasalliklar bilan kasallanish to'g'risidagi ma'lumotlar» | «Yuqumli va parazitar kasalliklar bilan kasallanish ko'rsatkichlari» |
| Base | `/v1/reports/form-10` | `/v1/reports/form-11` |
| Child cut | under **14** | under **18** |
| Extra slices | — | `city` (`CITY_RESIDENT`) / `rural` (`VILLAGE_RESIDENT`) |

**Period.** `year` (default current) + `period` (default current month):
`JANUARY…DECEMBER`, `Q1…Q4`, `HALF_YEAR`, `NINE_MONTHS`, `YEAR` — **not**
`from/to`. "Joriy davr" = the period's own month span; "Yig'ma" = January →
period end (so `Q2` current = Apr–Jun but cumulative = Jan–Jun). Each block has
previous year / current year / growth (four hierarchy walks zipped per row).

**Population is automatic — there is no `population` request parameter.**
Each row's population comes from `ref_population` by its own territory code and
the report year (republic row for root/`Jami`, region/district rows for their
codes; organization rows reuse the parent district's, or the root's for a
district-scope caller). Two lookups per row: `year` and `year-1`. No row →
population `0` → intensive `0` (no divide-by-zero; the API does not distinguish
this from genuine zero incidence — consider showing `—`). Fix wrong figures
once in the Aholi soni screen, not per report. Remove any old manual
"Aholi soni" input; only **Koeffitsiyent** (`koef`, default `100000`) remains.
`intensiveRate = caseCount * koef / territoryPopulation`, rounded to 2
decimals server-side.

`GET {base}/root?year&period&diagnosisCode&koef` and
`GET {base}/children?regionCode|districtCode&…` return rows:

```jsonc
{ "code": "1703", "name": "Andijon viloyati", "hasChildren": true,
  "current":    { "total": {…metric…}, "child": {…}, /* Form 11 also city, rural */ },
  "cumulative": { "total": {…}, "child": {…} } }
```

Metric (`Form10Metric`/`Form11Metric`): `absPreviousYear`, `absCurrentYear`,
`absGrowthPercent` (**not a percentage**: `|current − previous|`, render as a
count), `intensivePreviousYear`, `intensiveCurrentYear`, and
`intensiveGrowthPercent` — a **string**: signed percent with 2 decimals
(`"-12.22"`), switching to `"X marta"`/`"-X marta"` once either ratio reaches
2×. Render verbatim; don't `parseFloat` or append `%`. Use the numeric fields
for sorting/colouring.

UI: filter bar Yil + Davr + optional Tashxis kodi + Koeffitsiyent; changing
`koef` only rescales the intensive columns but must re-issue the calls
(population also varies by year); table groups "Joriy davr"/"Yig'ma" →
total (+city/rural) / child → prev/curr/growth → absolute/intensive, matching
the Excel export.

Export: `POST …/export` (`FORM10`/`FORM11`), params `year, period,
diagnosisCode, koef`.

### 12.3 Form 12 and Form 13

**Form 12** is *disease-first*: rows are the `ref_manual_report` entries
tagged `FORM_12`; each counts confirmed (`APPROVED`) cases whose
`final_icd10_code` is in the entry's ICD-10 set — total / under-14 / under-18,
year-over-year, plus a `Jami` row (only `includeInTotal` entries); geography is
the per-row drill-down. Per-request non-bean `Form12GeographyCountSource`.
**Form 13** is the transpose: geography-first rows (republic → region →
district → org), one column per `FORM_13`-tagged entry, same counting rule
(final code only, with `final_icd10_code is not null`), total/under-14/
under-18 year-over-year with no delta; every row carries a stable `diseases[]`
in entry-code order; `Form13GeographyCountSource` is per request. A case whose
final code sits in two tagged entries counts in both rows.

Exports (`§13`): Form 12 by disease and by territory (per-disease prev/curr
year, Form 13-style), a combined two-sheet file
(`POST …/form-12/export/combined`), Form 13 by territory and by disease.

### 12.4 Form 28.1 and Form 28.2

Clones of Form 12 (disease-first rows from `FORM_28_1`/`FORM_28_2` entries,
geography per-row drill-down, `Jami`) with a **single `[from,to]` period, no
year-over-year**. `from`/`to` both omitted = whole history; one omitted = the
other side defaults to today. Rows sorted by `code`.

| | Form 28.1 | Form 28.2 |
|---|---|---|
| Title | «Ayrim yuqumli va parazitar kasalliklar haqida ma'lumotlar» | «Kasalxona ichki infeksiyalari haqida ma'lumotlar» |
| Base | `/v1/reports/form-28-1` | `/v1/reports/form-28-2` |

Row fields: `code` (disease row = the manual-report **id**, pass it back as
`manualReportId`; geography rows = region/district code or organization id;
`"TOTAL"` for `Jami`), `name`, `rowCode` (manual-report `code`, e.g. `101`),
`icd10Display` (`shortName`), `hasChildren`.

Data columns — **28.1:** `total`, `female`, `under18` (<18), `under15` (<15),
`under1`, `age1to2`, `age3to5`, and the rural repeat (`VILLAGE_RESIDENT`, no
female repeat) `ruralTotal`, `ruralUnder18`, `ruralUnder15`, `ruralUnder1`,
`ruralAge1to2`, `ruralAge3to5`. Age cuts are nested (`under18 ⊇ under15 ⊇
under1`); `age1to2`/`age3to5` are disjoint bands. **28.2:** `total`,
`under18`, `underOneMonth`, `oneMonthToUnderOneYear` (together = all under 1
year).

- `GET {base}/root?from&to`; `GET {base}/children?manualReportId=<row.code>
  &regionCode|districtCode&from&to` (**`manualReportId` required**, keep it
  fixed while drilling; no `Jami`). Unknown/deleted id → `404
  reference.manual_report.not_found_by_id`. An entry with an empty ICD set is
  all zeros.
- **By territory** (the Form 13-style counterpart):
  `GET {base}/by-territory/root?from&to` and `…/by-territory/children?
  regionCode|districtCode&from&to` (no `manualReportId`). Rows are territories
  with `diseases[]` — one cell per tagged entry (`manualReportId, code, name,
  icd10Display`, plus the same data columns), identical order on every row
  incl. `Jami`, so safe as fixed columns (take the header from the first row).
- Excel: **server-side**, see §13 (`FORM281`, `FORM281_BY_TERRITORY`,
  `FORM282`, `FORM282_BY_TERRITORY`).

### 12.5 Statistika

Geography-first surveillance statistics; **no disease dimension** in the
table. Base `/v1/reports/statistics`. Distinguishing feature: it compares **two
independent, caller-chosen periods** — «Davr A» (`fromA`/`toA`, always
present; both omitted = whole history) and optional «Davr B»
(`fromB`/`toB`; all omitted → `periodB:null`; passing only one side counts as
requested and the missing side defaults to today) — by running the hierarchy
twice and zipping by `code`. It is not the fixed "this year vs a year ago" of
other reports.

**Meaning.** Per form (058, 058-1) every node splits into `confirmed`
(`status='APPROVED'`) and `primary` (`status not in ('APPROVED','CANCELED')`);
`CANCELED` is in neither bucket. Form 129 is a plain total (no
confirmed/primary split). Cards and acts are folded in per node by their own
`created_at`; the report reads `modules.card`/`modules.act` via org-id-list
repositories (`StatisticsCardRepository`/`StatisticsActRepository`).

**Node row** (`StatisticsNodeResponse`): `code`, `name`, `hasChildren`,
`parentCode` (region for a district; access-scope root `"UZ"` for a first-level
row; `null` for `Jami` — walk it up to restore an expanded branch from a deep
link like `?node=district:1703215`), `population` (nullable `ref_population`
for the **Davr A end year**, region/district rows only; one value shared by
both periods; compute rates client-side as `count*100000/population`, show `—`
when `null`), `periodA`, `periodB`.

**Each period is three separate blocks — never sum them**
(`StatisticsPeriodCountsResponse`):

- `form058`, `form0581` (`StatisticsFormBlockResponse`): `confirmedTotal`,
  `primaryTotal`, `ageBreakdown {confirmedUnder18, confirmedAdult,
  primaryUnder18, primaryAdult}`, `genderBreakdown {confirmedFemale,
  confirmedMale, primaryFemale, primaryMale}`, `categories[]` (one
  `{code, name, confirmedTotal, primaryTotal}` per `ref_catalog(type=CATEGORY)`
  entry, sorted by `nameUz`, identical order across rows and periods — safe as
  fixed columns; adding/renaming a catalog entry changes the columns with no
  deploy), `cardsTotal`, `cardsByStatus[]` (all 7 `CardStatus`, zero-filled),
  `cardsByType[]`, `actsTotal`, `actsByStatus[]` (all 8 `ActStatus`,
  zero-filled), `actsByType[]`. The cards/acts in a form block are those
  attached to **that form's** cases.
- `form129` (`StatisticsForm129BlockResponse`): `total`, `under18`, `adult`,
  `female`, `male`, `categories[]`, `byStatus[]` (`SENT`/`ACCEPTED`/
  `CANCELED`, zero-filled). No cards/acts.

Caveats: `categories` sums can be lower than the totals (patients with no
`category_code` count in totals and age/gender but in no category cell); age
cut is 18 at `created_at`; a soft-deleted catalog category disappears from the
next call for **both** periods (current catalog, even for past dates).

**Cross-filters** `genderCode` (`MALE`/`FEMALE`), `ageGroup`
(`UNDER_18`/`ADULT`), `categoryCode` (case-sensitive) on `/root`, `/children`,
`/export`: independent, AND-ed, narrow **only the case counts** of the three
forms — never the card/act sub-blocks (so `cardsTotal` doesn't drop when
`genderCode=FEMALE`; surface this in the UI).

Endpoints: `GET /root` (+ `Jami`), `GET /children` (`regionCode`,
`districtCode`), `GET /series`, `GET /top-diseases`, `POST /export`.

- `GET /series?regionCode&districtCode&from&to&bucket(DAY|WEEK|MONTH, default
  WEEK)` — `data` is a **single object** (not an array): `{code, name, bucket,
  from, to, points[{bucketStart, bucketEnd, form058Confirmed, form058Primary,
  form0581Confirmed, form0581Primary, form129Total}]}`. Default look-back 60
  days / 26 weeks / 12 months; empty buckets included; cap 400 buckets (most
  recent kept); **no cross-filters**; the three series are separate.
- `GET /top-diseases?regionCode&districtCode&from&to&limit(1–50, default 10)`
  — confirmed `APPROVED`, forms 058+058-1, ranked by `final_icd10_code` only
  (`[{diagnosisCode, diagnosisName (falls back to the code), confirmedCount}]`);
  won't reconcile with `confirmedTotal` (which counts uncoded cases too).
- `POST /export` — `fromA/toA/fromB/toB` + cross-filters; flattens the
  hierarchy for both periods; per-category and by-status breakdowns, `population`
  and series are not expanded into columns.

UI: Davr A (required) + Davr B (optional "solishtirish" toggle) pickers;
`root` then `children` on expand; widgets read straight from each form block
(summary, age, gender, category stacked bars, card/act funnels); clicking a
slice adds the matching cross-filter (show removable chips) and refetches
root + expanded children; trend panel from `/series`; diagnosis ranking from
`/top-diseases`; "Absolyut / 100 mingga" toggle plots `count*100000/population`
(grey out rows with `population:null`). For `periodB` in a different year the
intensive rate still uses Davr A's denominator.

### 12.6 Forecast («Kasallanish prognozi»)

Base `/v1/reports/forecast`: `GET /root`, `/children` (compact forecast row per
node + `Jami`), `GET /series` (full `history[]` + `forecast[]` + endemic
channel for one node — the chart), `GET /top-diseases` (risk-ranked "which
diseases might rise" for one node), `POST /export` (geography rows only, not
`/series` or `/top-diseases`).

**Cases:** every live notification (`status <> 'CANCELED'`, primary and
confirmed), ICD-10 filter on initial **or** final code. **Params** (shared by
all four): `bucket` (`DAY`/`WEEK` default/`MONTH`), `horizon` (1–120, default
8, **clamped** — not rejected — to 90/52/24 by bucket), `method` (`AUTO`
default, `NAIVE_MEAN`, `SES`, `HOLT`, `HOLT_WINTERS_ADDITIVE`), `from`/`to`
(training window; default look-back 180 days / 104 weeks / 36 months before
`to`; `to` default today; windows over 520 buckets are trimmed to the most
recent 520). `children`/`series`/`top-diseases` take `regionCode`/
`districtCode`; `root`/`children`/`series` take `diagnosisCode` (not
`top-diseases`, which forecasts *every* code); `top-diseases` adds `limit`
(1–50, default 10) and `minCases` (default 3; codes below it are dropped
before forecasting). History is gap-filled (`actual=0` for empty buckets).

**Node row** (`ForecastNodeResponse`): `code, name, hasChildren, method
(per node), trainingTotal, lastActual, nextPredicted, forecastTotal,
trendPerBucket (>0 rising), alertBuckets, peakPeriodStart`. `Jami` is the whole
scope's own series forecast as one — **not** the sum of region rows
(forecasting is non-linear; don't reconcile). **`series`**: `summary`
(`territoryCode/Name, diagnosisCode, bucket, trainingStart/End/Buckets/Total/
MeanPerBucket, method, trendPerBucket, forecastTotal, alertBuckets,
peakPeriodStart`), `history[{periodStart, periodEnd, actual}]`,
`forecast[{periodStart, periodEnd, predicted, lowerBound, upperBound,
endemicThreshold, alert}]`. **`top-diseases` row** (`ForecastDiseaseRiskResponse`):
node fields computed on that disease's own series + `diagnosisCode`,
`diagnosisName`, `riskLevel` — `HIGH` (`alertBuckets>0`), `MEDIUM`
(`trendPerBucket>0`), `LOW`. Already sorted: HIGH>MEDIUM>LOW, then
`alertBuckets`, `trendPerBucket`, `forecastTotal` desc — don't re-sort. Risk
labels are per series and not comparable across nodes.

Caveats: `created_at`-based so the last 1–2 buckets are undercounted by
reporting lag (set `to` a bucket or two before today); the ICD filter
re-evaluates the *current* code every call (history not frozen); organizations
onboarding mid-history depress early data; it is a decision-support baseline,
not a transmission model; `minCases` (3) can hide a first-ever rare case (offer
a 0 toggle on small nodes); a republic-wide `root` with `bucket=DAY` and a long
window is the heaviest call — prefer `WEEK`/`MONTH`.

**Screen:** filter bar (Hudud, MKB-10, Kun/Hafta/Oy, Gorizont, from–to) →
summary cards (model, trend arrow, `forecastTotal`, alert count/peak) → chart
→ top-diseases panel → territory table. On open: `/root` + `/series` +
`/top-diseases` (no geography params). Expand → `/children`; row click →
`/series` + `/top-diseases` for that node; row click in top-diseases sets
`diagnosisCode` and refetches `/series` only. Empty top-diseases state:
«Ushbu davrda xavf ostida kasallik aniqlanmadi». Chart.js: six line datasets
on one axis — band upper (dashed, `fill:'+1'`), band lower, observed (blue
`#2a78d6`), forecast (dashed orange `#eb6834`, `spanGaps`), endemic threshold
(grey `#898781`), alert dots (red `#e34948`); repeat the last history point
(`history[H-1].actual`) as a **seam** at the start of band/forecast datasets so
the lines join; `beginAtZero`, rounded tooltips, hard-coded hex colours.

#### Algorithm

Pure functions in `TimeSeriesForecaster` and `EndemicChannel`
(`modules/report/forecast/application/query/forecasting/`), unit-tested in
`TimeSeriesForecasterTest`. No library, no ML, deterministic, O(n).
Pipeline: query counts per bucket (SQL `date_trunc`) → gap-fill → forecast →
endemic threshold → assemble.

Smoothing constants are fixed (not fitted): **α=0.3, β=0.1, γ=0.3** — keeps
calls instant and exactly reproducible; trade-off: it can't adapt to a
series' volatility.

- `NAIVE_MEAN`: level = mean; flat forecast.
- `SES`: `level₀=y₀`; `levelᵢ = levelᵢ₋₁ + α(yᵢ − levelᵢ₋₁)`; flat at the final level.
- `HOLT`: `level₀=y₀`, `trend₀=y₁−y₀`; `pred = level+trend`;
  `level = pred + α·err`; `trend = trend + β·α·err`; forecast `level + h·trend`.
- `HOLT_WINTERS_ADDITIVE` (m = 7 / 52 / 12): initial level = mean of cycle 1,
  trend = (mean cycle 2 − mean cycle 1)/m, `season[s] = y[s] − level`; per step
  `pred = level + trend + season[s]`; `level = α(y−season[s]) + (1−α)(level+trend)`;
  `trend = β(newLevel−level) + (1−β)trend`;
  `season[s] = γ(y−newLevel) + (1−γ)season[s]`; forecast
  `level + h·trend + season[(n−1+h) mod m]`. Needs `n ≥ 2m`, else it silently
  falls back to `HOLT`.

**AUTO** (`resolveAuto`): `n<3` → `NAIVE_MEAN`; `seasonLength≥2 and n≥2m` →
`HOLT_WINTERS_ADDITIVE`; `n≥4` → `HOLT`; else `SES` — chosen independently per
node and per disease, and echoed in `method`.

**Band:** `σ` = sample SD of the model's own in-sample one-step errors
(needs ≥2; fallback `max(1, √mean)` Poisson-style so a flat history still gets
a visible band). For `h=1..horizon`: `point = max(0, forecast)`;
`band = 1.96·σ·√h`; `lower = max(0, point−band)`; `upper = point+band`. It is
a normal approximation, not a calibrated count-distribution interval.

**Endemic channel:** per seasonal phase (`DAY` = weekday, `WEEK` = ISO week,
`MONTH` = month; same key as Holt-Winters seasonality, `seasonIndex()` in
`ForecastBucketUnit`): `mean + 1.96·SD` of all historical buckets in that phase
if ≥2 values, else the global fallback `mean(all) + 1.96·√mean(all)`.
`alert = predicted > endemicThreshold` where `endemicThreshold =
ceil(threshold[phase])` — so an expected winter bump is normal and only
off-season-unusual buckets alert.

**Derived fields:** `predicted = round(point)` (≥0); `forecastTotal = Σ
predicted`; `alertBuckets` = count of alerts; `peakPeriodStart` = highest
`predicted`; `trendPerBucket = (point[last] − point[first])/(horizon−1)` — the
slope of the forecast line itself (a 1-bucket horizon uses `point[0] −
lastActual`).

**Worked example.** History `[10,12,11,15,14,18]`, `WEEK`, horizon 3 →
`n=6≥4` → HOLT. `level₀=10, trend₀=2`; steps give errors
`[0,−3.0,−0.01,−2.91,0.14]`, final `level≈17.90, trend≈1.836`. Forecast
`19.74 / 21.57 / 23.41` → predicted `20 / 22 / 23`. `σ≈1.66` → band at h=1
`≈±3.25` (16–23), at h=3 `≈±5.63` (18–29). One observation per ISO week →
global threshold `13.33 + 1.96·√13.33 ≈ 20.5 → 21`, so alerts at 22 and 23
(`alertBuckets=2`, risk HIGH), `trendPerBucket = (23−20)/2 = 1.5`.

Non-goals: no `R₀`/`Rt`/SEIR; reports reporting time series (delays, backlogs,
new organizations conflate with incidence); no per-series tuning.

---

## 13. Excel export

Every report (and the Form 058 / 058-1 journals) exports to `.xlsx` through a
**background job** — never streamed on the submit request. Submit endpoints
are per report (their own filters); progress, "my files" and download are the
same three endpoints for all. Shared surface `/v1/exports`, Swagger tag
`Exports`; `/v1/exports/**` needs only `isAuthenticated()` and only ever sees
**your own** jobs. Format `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.
Row cap **200 000** (`app.export.max-rows`), retention **7 days**
(`app.export.retention-days`; file and job row deleted).

```
POST …/export (report filters) → { id, status: PENDING|PROCESSING, progressPercent, … }
   (a) SSE  GET /v1/exports/{id}/progress   ← recommended  (b) poll GET /v1/exports?exportType=FORM1&size=5
   until COMPLETED →  GET /v1/exports/{id}/download  → .xlsx
```

Status `PENDING → PROCESSING → COMPLETED` or `FAILED`. Download on any other
status = `409 error.export.not-ready`; foreign/unknown id = `404`.

### 13.1 Submit endpoints (all `POST`, query-string params, empty body)

| Reports | `exportType` | Endpoint | Params |
|---|---|---|---|
| Shakl 1, 4, 6, 8, 9 | `FORM1/4/6/8/9` | `/v1/reports/form-N/export` | `from`, `to` (default today), `diagnosisCode` |
| Shakl 10 | `FORM10` | `/v1/reports/form-10/export` | `year`, `period`, `diagnosisCode`, `koef` (default 100000) |
| Shakl 11 | `FORM11` | `/v1/reports/form-11/export` | same as 10 (**no** `population`/`from`/`to`; population is automatic, §12.2) |
| 12 (by disease / by territory) | `FORM12` / `FORM12_BY_TERRITORY` | `/v1/reports/form-12/export`, `…/by-territory/export` (+ `…/export/combined`) | `from` (default whole history), `to` |
| 13 (by territory / by disease) | `FORM13` / `FORM13_BY_DISEASE` | `/v1/reports/form-13/export`, `…/by-disease/export` | `from`, `to` |
| 28.1, 28.2 (by disease / by territory) | `FORM281`, `FORM281_BY_TERRITORY`, `FORM282`, `FORM282_BY_TERRITORY` | `/v1/reports/form-28-N/export`, `…/by-territory/export` | `from`, `to` |
| Statistika | `STATISTICS` | `/v1/reports/statistics/export` | `fromA/toA`, `fromB/toB` (omitted → "Davr B" columns blank), cross-filters |
| Forecast | `FORECAST` | `/v1/reports/forecast/export` | `diagnosisCode`, `bucket`, `horizon`, `method`, `from`, `to` (geography rows only) |
| Manual reports | `FORM2_MANUAL`, `FORM31`, `FORM32`, `FORM7`, `ANALYTIC_REPORT` | `/v1/reports/form-2/manual-entries/export`, `form-3-1/entries/export`, `form-3-2/entries/export`, `form-7/entries/export`, `analytic/export` | the list endpoint's filter (`sortBy`, `sortDir`, `from`, `to`; analytic also `status`); `page`/`size` ignored — whole filtered set |
| Journals | `FORM058` / `FORM0581` | `POST /v1/form-058/export`, `/v1/form-058-1/export` | same params as the list endpoint; `isAuthenticated()` only (no `PERMISSION_REPORTS_*`) |

Submit permission: `PERMISSION_REPORTS_READ` for report modules. By-territory
column sets are **dynamic** (one group per tagged catalog entry, code-sorted) —
don't hard-code positions. There are no longer "client-side only" exports for
Form 28.x or Forecast.

### 13.2 Shared endpoints

- `GET /v1/exports?page&size(≤200)&exportType` — "Mening fayllarim", newest
  first, `PagedResponse<ExportJobResponse>`.
- `GET /v1/exports/{id}/progress` — SSE, one `progress` event per second with
  the full job JSON; closes itself on `COMPLETED`/`FAILED`. Same
  header-needs-`fetch` constraint (§1).
- `GET /v1/exports/{id}/download` — `.xlsx` with `Content-Disposition:
  attachment`. Needs the bearer header, so fetch as a blob and save.

`ExportJobResponse`: `id, exportType, status, progressPercent,
processedRows, totalRows (known at submit), fileName, fileSizeBytes,
errorMessage, createdAt, completedAt`.

### 13.3 Errors and headers

`400 VALIDATION_FAILED` + `error.export.too-large` (> 200 000 rows — narrow
dates / add `diagnosisCode`); other `400` for bad params; `401/403`; `5xx` if
the worker queue is saturated (job marked `FAILED`, retry). A mid-run failure
comes back as `status:"FAILED"` with `errorMessage`.

Column headers follow `Accept-Language` **of the submit request** (`uz`
default, `uz-Cyrl`, `kaa`, `ru`, `en`) — send the UI language; data columns
and order never change. Machine column keys (dev-panel setting
`export.excel.<type>.columns`) are language-independent. Exception: Form
058/058-1 journal headers are still fixed Cyrillic (official-blank layout).

### 13.4 Backend capability

`platform.export` also has an additive **multi-sheet** capability
(`ExcelWorkbookWriter` / `MultiSheetExcelExportSource` /
`ExcelExportSourceSheet`) for one file with several sheets (used by the
combined Form 12 export), alongside the single-sheet pipeline. Known issue
from runtime verification: export jobs showed connection-pool contention
under heavy concurrent load (an infrastructure/tuning matter, not a
per-module bug).

### 13.5 Client example

```ts
const auth = { Authorization: `Bearer ${token}` };
const submit = await fetch(submitUrl, { method: "POST", headers: { ...auth, "Accept-Language": lang } });
if (!submit.ok) throw new Error(await submit.text());      // e.g. error.export.too-large
const { data: job } = await submit.json();
await new Promise<void>((resolve, reject) =>
  fetchEventSource(`${API}/v1/exports/${job.id}/progress`, {
    headers: auth,
    onmessage(ev) { const j = JSON.parse(ev.data);
      if (j.status === "COMPLETED") resolve();
      if (j.status === "FAILED") reject(new Error(j.errorMessage)); },
    onerror: reject }));
const res = await fetch(`${API}/v1/exports/${job.id}/download`, { headers: auth });
const blob = await res.blob();       // then save via an <a download> + URL.createObjectURL
```

Polling alternative: `GET /v1/exports?size=50` every ~2 s until the job is
`COMPLETED`/`FAILED`.

---

## 14. Dev panel (`/v1/dev/**`)

Developer monitoring panel (errors, request/login history, CPU/RAM/HTTP
metrics, settings, route policies, integration clients, webhook dispatches) —
**not** part of the business API. Whole subtree authenticates via **HTTP Basic**
against local `DevUser` accounts: `Authorization: Basic base64(identifier:password)`
on **every** request (no session/token); `identifier` is the `username` **or**
`email` (one login field is enough). Swagger group **"Dev Panel"**. This section
covers account auth/self-service/admin, positions, RBAC management and the
organizations lookup; the rest of the panel is documented in Swagger.

### 14.1 Roles (one per account, `DevUserRole`)

| Capability | USER | ADMIN | SUPER_ADMIN |
|---|:-:|:-:|:-:|
| Read anything; update own profile; change own password | ✓ | ✓ | ✓ |
| Writes (settings, route policies, integration clients, positions, permissions, actions, roles, create dev-user), resolve an error, retry a webhook dispatch, revoke an integration client | – | ✓ | ✓ |
| Deletes (settings, route policies, positions, permissions, actions, roles), restores, block/unblock, reset another account's password | – | – | ✓ |

Exactly one `SUPER_ADMIN` exists (seeded); `POST /dev-users` rejects
`role:"SUPER_ADMIN"` (`409 dev-user.role.super-admin-not-creatable`). Rule of
thumb: reversible writes = ADMIN+, irreversible-from-the-API deletes =
SUPER_ADMIN; error-resolve / webhook-retry / client-revoke are ADMIN+
(`hasRole('DEV_ADMIN')`; Swagger descriptions don't yet say so — the
`@PreAuthorize` is authoritative). Authorization is `@PreAuthorize` per
controller. A `403` for an endpoint outside the caller's role is Spring's
standard insufficient-authority response.

### 14.2 First login — forced password change

Every new or reset account has `mustChangePassword:true`. While set,
`DevPasswordChangeGuardFilter` returns `403` (`code:
DEV_USER_PASSWORD_CHANGE_REQUIRED`, message key
`dev-user.password.change-required`) on **every** `/v1/dev/**` call except
`GET /v1/dev/dev-users/me` and `PATCH /v1/dev/dev-users/me/password` — even
lists and `GET /{id}`. `DevUserDetailResponse` (returned by `me`, list,
get-by-id, update) carries `mustChangePassword`, so read it from `me` rather
than inferring from a 403. Change password with
`PATCH …/me/password { currentPassword, newPassword (8–100) }` (wrong current
→ `409 dev-user.password.current.invalid`) and switch stored credentials to the
new password afterwards.

### 14.3 Endpoints

| Method | Path | Role | Notes |
|---|---|---|---|
| GET | `/v1/dev/dev-users`, `/{id}`, `/me` | any | paged list / one / calling account |
| POST | `/v1/dev/dev-users` | ADMIN+ | `{username, role? (USER default / ADMIN), email*, fullName?, phone?, positionId?}` → `DevUserCreateResponse` with a **one-time `password`** (never retrievable again; hand it to the owner) |
| PUT | `/v1/dev/dev-users/{id}` | ADMIN+ | `{email, fullName, phone, positionId}` (`positionId:null` clears); username/password/role/enabled not editable |
| PATCH | `…/{id}/revoke` / `…/unblock` | SUPER_ADMIN | block (`enabled:false`, Basic auth then fails before role checks; cannot block yourself `409 dev-user.revoke.self-forbidden`) / unblock (idempotent) |
| PATCH | `…/{id}/reset-password` | SUPER_ADMIN | no body; returns new one-time `password`, sets `mustChangePassword` again |
| GET/POST/PUT/DELETE | `/v1/dev/ref/positions[/{id}]` | any / ADMIN+ / ADMIN+ / SUPER_ADMIN | single-language lookup (`{name, enabled}`); hard delete → `409 dev-position.delete.in-use` if assigned |
| GET/POST/PUT/DELETE + `PATCH …/{id}/restore` | `/v1/dev/permissions`, `/v1/dev/actions`, `/v1/dev/roles` | any / ADMIN+ / ADMIN+ / SUPER_ADMIN (restore SUPER_ADMIN) | the same rows the main app's `/v1/permissions`, `/v1/actions`, `/v1/roles` manage, reachable without an SSO token; **delete is soft** (`active:false, deleted:true`), always succeeds |
| GET | `/v1/dev/roles/{id}/permissions` | any | grants |
| PATCH / PUT / PATCH | `…/roles/{id}/permissions`, `…/permissions` , `…/permissions/remove` | ADMIN+ | body `{permissions:[{permissionId, actionIds:[…]}]}`: PATCH merges, PUT replaces the whole set, `remove` drops actions (and the permission once empty); all return the full `RoleDetailResponse` |
| GET | `/v1/dev/ref/organizations?search=&limit=20` | any | lookup (name/TIN/phone/region/district code); `limit` ≤50; returns `{id, uuid, name, active, levelType, medicalType}` — submit the `uuid`; no lookup-by-uuid parameter |
| GET | `/v1/dev/settings?deleted=true|false` | any | the `deleted` filter finds soft-deleted settings so `PATCH …/{id}/restore` is reachable any time; `GET /{id}` and `/by-key/{key}` still 404 on a deleted row (restore first) |

Permission = a **subject** (a module/resource like `users`) paired with
actions (`READ`, `CREATE`, `MANAGE`, custom like `CARD_ATTACH`). Permission
request: `{subject, descriptionUz/Ru/UzCyril/Kaa, active}`; action: same with
`code`; role: `{name, descriptions…, active}` (`active` required on PUT).
List filters: `subject`/`code`/`name` partial match, `active` exact;
`sortBy` accepts `id`+`subject` (permissions), `id`+`code` (actions), `id,
name, description*` (roles). `PUT` on an already soft-deleted row → `409
{permission|action|role}.update.deleted_conflict` — restore first.
`dev-users` has no soft-delete problem (revoke only sets `enabled:false`;
list never filters on it unless `enabled=` is passed).

### 14.4 Errors

| HTTP | `code` | Message keys |
|---|---|---|
| 403 | `DEV_USER_PASSWORD_CHANGE_REQUIRED` | route to the password screen |
| 409 | `CONFLICT` | `dev-user.password.current.invalid`, `dev-user.username.already-exists`, `dev-user.email.already-exists`, `dev-user.role.super-admin-not-creatable`, `dev-user.revoke.self-forbidden`, `dev-position.name.already-exists`, `dev-position.delete.in-use`, `permission.subject.already_exists`, `permission.update.deleted_conflict`, `action.code.already_exists`, `action.update.deleted_conflict`, `role.name.already_exists`, `role.update.deleted_conflict` |
| 404 | `NOT_FOUND` | `dev-user.not-found`, `dev-position.not-found`, `permission.not_found_by_id`, `action.not_found_by_id`, `role.not_found` |

Performance history: panel auth was slow from per-request BCrypt + DB lookups;
it now caches authentication, and indexes/stat calls were reduced.

---

## 15. Data dictionary: Form 058 / 058-1

Source: `modules.form058.domain.model.*`, `modules.form0581.domain.model.*`.
"Req." = `nullable=false` in JPA.

### 15.1 Common audit fields (all aggregates via `AbsEntity`)

`id` (Long, IDENTITY), `version` (optimistic lock), `uuid` (UUID, set at first
save; used by integrations instead of `id`), `createdAt`/`updatedAt` (Instant),
`createdBy`/`updatedBy` (`*_by_id`), `createdOrgUuid` (req.) /
`updatedOrgUuid` (organization context for data isolation) and read-only
`createdOrg`/`updatedOrg` relations.

### 15.2 `Form058` (table `form058`)

| Field | Column | Type | Req. | Description |
|---|---|---|:-:|---|
| `status` | `status` | `FormStatus` | ✓ | `SENT`, `ACCEPTED`, `CARD_LINKED`, `APPROVED`, `CANCELED` (§4.1) |
| `source` | `source` | String(20) | ✓ | creation source (SSO/DHP portal or integration client) |
| `patient` | `patient_id` | FK | ✓ | no cascade delete |
| `senderOrganizationId` | `sender_organization_id` | Long | ✓ | reporting medical organization |
| `receiverOrganizationId` | `receiver_organization_id` | Long | ✓ | SES (SANEPID_SERVICE) |
| `sourceIntegrationClientId` | `source_integration_client_id` | Long | | submitting integration client; `null` for SSO/DHP; targets the outbound webhook |
| `location` | `location_id` | `Form058Location` | | PERSIST/MERGE cascade only |
| `hasLinkedCards` | `has_linked_cards` | boolean | ✓ | denormalized filter flag |
| `assignedCardId` | `assigned_card_id` | Long | | **deprecated** |

Embedded blocks:

- `diagnosisInfo`: `icd10Code` (≤20, req.), `icd10Name` (≤512, req.),
  `finalIcd10Code`/`finalIcd10Name` (set on `approve`), `icd10UsageLimit`.
- `clinicalInfo`: `labConfirmation` (Boolean), `hospitalPlaceId`.
- `dateInfo`: `admissionDate`, `diagnosisDate`; **required**: `diseaseDate`,
  `firstVisitDate`, `visitDate`, `initialReportDateTime` (all `LocalDateTime`).
- `epidemicInfo`: `diseasePlaceCode` (≤64), `diseaseCause` (≤2000),
  `epidemicMeasures` (≤2000).
- `reportInfo`: `journalFormCode` (≤64, req.), `comment` (`form_comment`,
  ≤2000), `notifierFullName` (req.), `cardByFullName`.
- `cancellationInfo`: `cancelReason` (≤1000), `canceledBy`, `canceledAt`,
  `notApprovedReason` (≤1000).
- `approvalInfo`: `approvedBy`, `approvedOrganizationId`, `approvedAt`,
  `approvedFullName`, `approvedOrgUuid`. For Form 058 the approver is the
  **sender** organization (§4.1).
- `deleteInfo` (soft delete): `deleted` (req.), `deletedAt`, `deletedBy`,
  `deleteReason` (≤1000).
- `Form058Location` (`fm058_location`): `latitude`, `longitude` (Double),
  `location` (text ≤1000).

### 15.3 `Form0581` (table `form058_1`)

Independent entity, `Form0581Status` (same five values), same lifecycle.
Fields: `status`, `source`, `patient` (the bitten/scratched person),
`senderOrganizationId`, `receiverOrganizationId` (validator-enforced
SANEPID_SERVICE), `sourceIntegrationClientId`, `otherPeopleInjured` (Boolean;
controls `otherInjuredPeople`), `otherInjuredPeople` (`Form0581OtherInjuredPerson`
list), `hasLinkedCards`.

Embedded blocks:

- `diagnosisInfo`: `icd10Code`/`icd10Name` (req.), `injuryLocalization`
  (≤500), `finalIcd10Code`/`finalIcd10Name`.
- `incidentInfo`: `injuryDateTime` (req.), `dpuVisitDateTime` (req., antirabic
  point visit), `injuryRegionCode` (req.), `injuryDistrictCode` (req.),
  `injuryAddress` (≤1000).
- `animalInfo`: `animalCategoryCode`, `animalColor`, `animalType`, `animalBreed`.
- `animalOwnerInfo`: `ownerLastName/FirstName/MiddleName`, `ownerAddress`
  (`Form0581Address`, columns prefixed `owner_*`).
- `hospitalizationInfo`: `hospitalizedAt`, `hospitalOrganizationId`.
- `reportInfo`: `antirabicAssistanceInfo` (≤2000), `notifierFullName` (req.),
  `receiverFullName`, `messageSentAt`.
- `cancellationInfo`, `approvalInfo` (decision by the **sender**), `deleteInfo`
  — same structure as Form 058.
- `Form0581Address` (embeddable): `regionCode`, `districtCode`,
  `neighborhoodCode`, `street`, `houseNumber`, `apartmentNumber`; used for the
  owner (`owner_*` columns) and each other injured person.
- `Form0581OtherInjuredPerson` (`form058_1_other_injured_person`, 0..N):
  `form0581` FK (req.), `lastName`, `firstName`, `middleName`, address.

---

## 16. Open questions

Carried over from the source docs; none are decided in code.

**LIS / acts** (owner: LIS team, product): the real "returned" signal in the
callback body; required fields (`Act.xlsx`) before `READY`/send (nothing
enforces them server-side beyond `subject`); ACT153 quantity pair and
`sampleQtUnit` enum names; `packageType`/`manufacturer`/`manufactureDate`
per-item nesting; names/behaviour of the four unproxied dictionaries
(`PURPOSE`, package, conservation, sample type — and whether an empty
`purposeId` with only a name is accepted); canonical institution fields (now
resolved by the `subject` block); whether `fullNameOfDoctor` should be the
sender or the recorded `sampler`, and whether Ru names should be sent;
whether `lisOrganizationId` and `labId` must agree; whether LIS authenticates
its callback with a valid bearer token; confirm the `SAMPLING_BASIS` code
list; ACT156 group number field (not requested yet); deferred: assigned
employee full name on `ActTableResponse`.

**Auth:** re-confirm `dhp-web` credentials against the current `token-url`
(§3).

**Analytic report:** `getById` has no organization-scope check (flagged,
unfixed); `content` is stored opaque.

**Quality:** no Testcontainers/`@DataJpaTest` — schema correctness is checked
by running the app; Statistika `genderBreakdown` showed 0/0 in the dev DB
(looks like a data-seed gap, not a query bug).

---

## 17. Corrections made in this consolidation

Statements in the old docs that contradicted the code or each other, and what
this guide now says:

| Old doc | Claim | Now |
|---|---|---|
| `form058-data-dictionary.md` | `FormStatus` = `NOT_APPROVED, SENT, RECEIVED, CARD_LINKED, APPROVED_PENDING, APPROVED, CANCELED`; 058-1 approval/`receive()` wording | Both enums are `SENT, ACCEPTED, CARD_LINKED, APPROVED, CANCELED`; approval by the sender; `cancellationInfo.notApprovedReason` kept as a column |
| `form058-form0581-frontend-guide.md` | `approve` returns 409 unless `CARD_LINKED` | Form 058 accepts `ACCEPTED` **or** `CARD_LINKED`; Form 058-1 `CARD_LINKED` only |
| `form129-frontend-guide.md` | no `delete` action | admin/super-admin soft delete `DELETE /v1/form-129/{id}` |
| `notification-module.md` / guide | no Form 129 types; listener filtered `SENT → COMPLETED` for acts; `by-type` sample incomplete | `FORM129_*` types; `ACT_LIS_RESPONSE` on `RESULT_RECEIVED`/`RETURNED_BY_LIS`; 28 types |
| `act-lis-frontend-guide.md` | sequence diagram `SENT → COMPLETED`; "labId from the direct lis.sanepid.uz call" (contradicted its own text); callback `SENT → COMPLETED` in the endpoint table | `RESULT_RECEIVED`/`RETURNED_BY_LIS` + doctor `close`; `labId` from the proxied lookup |
| `act-module.md` | `markReady` bullets omitted `subject` completeness; recent-change list mixed superseded text | consolidated lifecycle + subject block |
| `auth-login-module.md` | `/v1/auth/**` reserved in `SecurityRouteCatalog` | that class no longer exists; routes are DB-driven (`RouteAccessPolicy`) |
| `statistics-report-frontend-guide.md` | single flat period payload (`confirmedTotal`, `ageBreakdown`, `cardsByStatus` with 7 act statuses…) | per-period **three blocks** `form058` / `form0581` / `form129`; `cardsByType`/`actsByType` added; acts have 8 statuses |
| `form-28-1-28-2-report-frontend-guide.md`, `forecast-report-frontend-guide.md` | "no server export endpoint; build Excel client-side" | server-side exports exist (`FORM281…`, `FORM282…`, `FORECAST`) |
| `population-reference-frontend-guide.md` | reports "will use" the denominator | Form 10/11, Statistika `population` and Analitik hisobot already read it |
| `report-excel-export-frontend-guide.md` | reported "outdated §2.3" ambiguity for Form 11 | Form 11 documented as the Form 10 clone (year/period/koef) |
| `form058-module.md` | report-module list `form1/form2/form4/form6` | PRIMARY exclusion applies to every report using the primary bucket; see §12.1 |
| `dev-panel-frontend-guide.md` | "Stage 5/6" project-management remarks | removed (kept behaviour only) |

Not changed: [`integration/`](integration) and the legacy-migration docs.
