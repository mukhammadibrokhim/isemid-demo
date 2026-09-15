# Dev panel — frontend guide

`/v1/dev/**` is the developer monitoring panel — error/request/login history,
CPU/RAM/HTTP metrics, and management of the panel's own local accounts. It
is **not** part of the main business API: every other endpoint in this app
authenticates via an SSO/DHP `Authorization: Bearer` token, but the whole
`/v1/dev/**` subtree authenticates via **HTTP Basic** against its own local
`DevUser` accounts — a separate credential system, unrelated to end-user
login (see [`auth-login-frontend-guide.md`](./auth-login-frontend-guide.md)
for that one). This document only covers account auth/self-service/admin
management, the positions lookup, and RBAC permission/action/role management —
the rest of the panel (errors, requests, metrics, settings, route-policies,
...) is unchanged and documented in Swagger.

Available in Swagger UI as its own group, **"Dev Panel"**
(`/swagger-ui.html`, group dropdown → "Dev Panel").

| Method | Path | Role | Purpose |
|---|---|---|---|
| GET | `/v1/dev/dev-users` | any | Paged list of accounts |
| GET | `/v1/dev/dev-users/{id}` | any | One account by id |
| GET | `/v1/dev/dev-users/me` | any | The calling account — always reachable, even mid password-change block |
| PATCH | `/v1/dev/dev-users/me/password` | any | Change your own password — the other endpoint always reachable |
| POST | `/v1/dev/dev-users` | ADMIN+ | Create an account (USER/ADMIN only — see below) |
| PUT | `/v1/dev/dev-users/{id}` | ADMIN+ | Update email/full name/phone/position |
| PATCH | `/v1/dev/dev-users/{id}/revoke` | SUPER_ADMIN | Block an account |
| PATCH | `/v1/dev/dev-users/{id}/unblock` | SUPER_ADMIN | Reverse a block |
| PATCH | `/v1/dev/dev-users/{id}/reset-password` | SUPER_ADMIN | Force-reset another account's password |
| GET | `/v1/dev/ref/positions` | any | Paged list of positions/departments |
| GET/POST/PUT/DELETE | `/v1/dev/ref/positions/{id}` | any / ADMIN+ / ADMIN+ / SUPER_ADMIN | Position lookup CRUD |
| GET | `/v1/dev/permissions` | any | Paged list of RBAC permissions (subjects) |
| GET/POST/PUT/DELETE | `/v1/dev/permissions/{id}` | any / ADMIN+ / ADMIN+ / SUPER_ADMIN | Permission CRUD (soft delete) |
| PATCH | `/v1/dev/permissions/{id}/restore` | SUPER_ADMIN | Restore a soft-deleted permission |
| GET | `/v1/dev/actions` | any | Paged list of RBAC actions |
| GET/POST/PUT/DELETE | `/v1/dev/actions/{id}` | any / ADMIN+ / ADMIN+ / SUPER_ADMIN | Action CRUD (soft delete) |
| PATCH | `/v1/dev/actions/{id}/restore` | SUPER_ADMIN | Restore a soft-deleted action |
| GET | `/v1/dev/roles` | any | Paged list of RBAC roles |
| GET/POST/PUT/DELETE | `/v1/dev/roles/{id}` | any / ADMIN+ / ADMIN+ / SUPER_ADMIN | Role CRUD (soft delete) |
| PATCH | `/v1/dev/roles/{id}/restore` | SUPER_ADMIN | Restore a soft-deleted role |
| GET | `/v1/dev/roles/{id}/permissions` | any | Permissions/actions assigned to a role |
| PATCH/PUT | `/v1/dev/roles/{id}/permissions` | ADMIN+ | Add to / fully replace a role's permissions |
| PATCH | `/v1/dev/roles/{id}/permissions/remove` | ADMIN+ | Remove permissions/actions from a role |

## CORS

Same rule as every other endpoint in this API: allowed origins are
configured server-side (`app.cors.allowed-origins` — in dev that's
`http://localhost:3000` by default). If your frontend runs on a different
origin/port, ask the backend team to add it. This subtree authenticates via
`Authorization: Basic`, not cookies — don't set `credentials: "include"` /
`withCredentials: true`, the backend does not allow credentialed CORS.

---

## Authenticating

Every request needs `Authorization: Basic base64(identifier:password)`.
There is no session and no token — the header has to go on every request,
same as the browser's native Basic Auth prompt would send it.

```ts
function basicAuthHeader(identifier: string, password: string) {
  return { Authorization: `Basic ${btoa(`${identifier}:${password}`)}` };
}

const res = await fetch(`${API_BASE}/v1/dev/dev-users/me`, {
  headers: basicAuthHeader("dev-oncall", "S3cur3Pass"),
});
```

`identifier` can be **either** the account's `username` or its `email` —
`DevUserDetailsService` checks both columns (both are unique), so a single
login field on the form is enough; don't ask the user which one they're
using.

Every successful response uses the same envelope as the rest of the API:

```jsonc
// ApiResponse<T> — single-item endpoints
{ "success": true, "message": "Operation completed successfully.", "data": { /* ... */ } }

// PagedResponse<T> — list endpoints
{
  "success": true,
  "message": "Operation completed successfully.",
  "data": [ /* ... */ ],
  "meta": { "pagination": { "page": 1, "size": 20, "numberOfElements": 20, "totalElements": 42, "totalPages": 3, "first": true, "last": false } },
  "links": { "self": "...", "first": "...", "prev": null, "next": "...", "last": "..." }
}
```

---

## First login: forced password change

Every account — freshly created **or** reset by a SUPER_ADMIN — starts with
`mustChangePassword: true`. While that flag is set, `DevPasswordChangeGuardFilter`
blocks **every** `/v1/dev/**` request from that account with `403
dev-user.password.change-required`, except the two endpoints that exist to
get you out of that state:

| Method | Path | Why it's exempt |
|---|---|---|
| GET | `/v1/dev/dev-users/me` | So the frontend can tell who's logged in and that a change is required, before anything else works |
| PATCH | `/v1/dev/dev-users/me/password` | The only way to clear the flag |

Even `GET /v1/dev/dev-users/{id}` or the list endpoint return `403` in this
state — don't special-case just the write endpoints.

`DevUserDetailResponse` — the shape returned by `GET .../me` itself, as well
as the list/get-by-id/update-profile endpoints — carries a `mustChangePassword`
boolean, so the frontend learns the state directly from that one call. There's
no need to probe a second endpoint and infer it from a `403`.

```ts
async function login(identifier: string, password: string) {
  const res = await fetch(`${API_BASE}/v1/dev/dev-users/me`, {
    headers: basicAuthHeader(identifier, password),
  });
  if (!res.ok) throw new Error((await res.json()).message);

  const { data: me } = await res.json();
  if (me.mustChangePassword) {
    // route to a "set your password" screen — every other call will 403 until this is done
    return { me, mustChangePassword: true };
  }
  return { me, mustChangePassword: false };
}

async function changeOwnPassword(identifier: string, currentPassword: string, newPassword: string) {
  const res = await fetch(`${API_BASE}/v1/dev/dev-users/me/password`, {
    method: "PATCH",
    headers: { ...basicAuthHeader(identifier, currentPassword), "Content-Type": "application/json" },
    body: JSON.stringify({ currentPassword, newPassword }),
  });
  if (!res.ok) throw new Error((await res.json()).message);
  // switch stored credentials to (identifier, newPassword) from here on — the old password stops working
}
```

`newPassword` must be 8–100 characters. A wrong `currentPassword` gets `409
dev-user.password.current.invalid`.

---

## Roles

Every account has exactly one `role`. There is exactly one `SUPER_ADMIN` in
the whole panel, seeded once (Liquibase, on first DB startup) — `POST
/v1/dev/dev-users` rejects `role: "SUPER_ADMIN"` outright
(`409 dev-user.role.super-admin-not-creatable`), so there is no API path to
create a second one.

| Capability | USER | ADMIN | SUPER_ADMIN |
|---|:---:|:---:|:---:|
| Read anything (lists/details) | ✓ | ✓ | ✓ |
| Update own profile, change own password | ✓ | ✓ | ✓ |
| Write (settings, route-policies, integration clients, positions, permissions, actions, create dev-user) | – | ✓ | ✓ |
| Resolve an error (`PATCH .../errors/{id}`) | – | ✓ | ✓ |
| Retry a webhook dispatch (`POST .../webhook-dispatches/{id}/retry`) | – | ✓ | ✓ |
| Revoke an integration client (`POST .../integration-clients/{id}/revoke`) | – | ✓ | ✓ |
| Delete (settings, route-policies, positions), restore (settings, permissions, actions) | – | – | ✓ |
| Block / unblock another account | – | – | ✓ |
| Reset another account's password | – | – | ✓ |

`canDevWrite` (ADMIN+) was the right guess for all three of these — they're
plain `@PreAuthorize("hasRole('DEV_ADMIN')")`, same tier as every other write
in the panel. They aren't a new tier and don't need `ROLE_DEV_SUPER_ADMIN`:
none of them destroys anything permanently (an error just gets a resolved
flag, a webhook dispatch gets reset to `PENDING`, a client revoke is reversed
with `PUT .../{id}` and `active: true`) — that's the same reversibility test
that puts settings/route-policies/positions *writes* at ADMIN+ and reserves
SUPER_ADMIN only for their irreversible-from-the-API deletes. They're missing
from the Swagger operation descriptions themselves (tracked as a follow-up),
but the `@PreAuthorize` on `DevErrorController`, `DevOutboundWebhookDispatchController`
and `DevIntegrationClientController` is authoritative and matches this table.

A `403` from any endpoint not listed as reachable for the caller's role is
Spring Security's standard "insufficient authority" response — not a
dev-panel-specific error code.

---

## Account endpoints

### Create — `POST /v1/dev/dev-users` (ADMIN+)

```jsonc
// request
{
  "username": "dev-oncall",
  "role": "ADMIN",             // optional, defaults to USER; SUPER_ADMIN is rejected
  "email": "dev-oncall@example.uz",   // required
  "fullName": "Aziz Karimov",  // optional
  "phone": "+998901234567",    // optional
  "positionId": 3              // optional, from GET /v1/dev/ref/positions
}

// response — data: DevUserCreateResponse
{
  "id": 42,
  "username": "dev-oncall",
  "role": "ADMIN",
  "email": "dev-oncall@example.uz",
  "fullName": "Aziz Karimov",
  "phone": "+998901234567",
  "positionId": 3,
  "positionName": "Backend Developer",
  "password": "aB3-...",       // shown ONCE, never retrievable again — show it to the operator now
  "createdAt": "2026-08-19T09:12:00Z"
}
```

The new account has `mustChangePassword: true` — hand the one-time
`password` to whoever owns it and expect them to go through the
first-login flow above.

### Update profile — `PUT /v1/dev/dev-users/{id}` (ADMIN+)

```jsonc
// request — username/password/role/enabled are NOT editable here
{ "email": "dev-oncall@example.uz", "fullName": "Aziz Karimov", "phone": "+998901234567", "positionId": null }
```

`positionId: null` clears the assigned position. Response is a
`DevUserDetailResponse` (same shape as the list/get-by-id endpoints).

### Block / unblock — `PATCH /v1/dev/dev-users/{id}/revoke` / `.../unblock` (SUPER_ADMIN)

No body on either. `revoke` sets `enabled: false` — a blocked account can no
longer authenticate at all (its Basic Auth attempts fail before even
reaching role checks). It can't block itself
(`409 dev-user.revoke.self-forbidden`). `unblock` is idempotent — calling it
on an already-enabled account is a no-op, not an error.

### Reset another account's password — `PATCH /v1/dev/dev-users/{id}/reset-password` (SUPER_ADMIN)

No body, no current-password check — the caller is already trusted with
full control. Generates a new one-time password and sets
`mustChangePassword: true` again.

```jsonc
// response — data: DevUserResetPasswordResponse
{ "id": 42, "username": "dev-oncall", "password": "kL9-..." }
```

Hand the returned `password` to the account's owner directly — it cannot be
retrieved again, and the account is now forced through the first-login flow
with it.

---

## Positions lookup

`/v1/dev/ref/positions` — a small, single-language "position/department"
list assignable to a dev-user's profile (`positionId`/`positionName` above).
Separate from the org-facing, multi-language `modules.reference`
dictionaries used elsewhere in the app — this one only exists for the dev
panel's own account list.

```jsonc
// GET /v1/dev/ref/positions -> PagedResponse<DevPositionResponse>, data[0]:
{ "id": 3, "name": "Backend Developer", "enabled": true, "createdAt": "...", "updatedAt": "..." }

// POST (ADMIN+)
{ "name": "Backend Developer" }

// PUT /{id} (ADMIN+)
{ "name": "Backend Developer", "enabled": false }
```

`DELETE /{id}` (SUPER_ADMIN) fails with `409 dev-position.delete.in-use` if
any dev-user still has it assigned — reassign or clear those first.

---

## Permissions (subjects) and actions

`/v1/dev/permissions` and `/v1/dev/actions` are the dev-panel's own entry
point into the same RBAC `Permission`/`Action` tables the main app's
`/v1/permissions`/`/v1/actions` manage — they're the same rows, just reachable
without an SSO `isemid_super_admin`/`isemid_admin` JWT (which a `/v1/dev/**`
Basic-Auth account has no way to obtain). A "permission" here is really a
**subject** — a module/resource name like `users` or `form058-sender` — that
gets paired with one or more **actions** (`READ`, `CREATE`, `MANAGE`, or a
custom code like `CARD_ATTACH`) when building a role's grants elsewhere in
the app. Both follow the exact same request/response shape and role tiers as
`positions` above (list/get: any; create/update: ADMIN+; delete/restore:
SUPER_ADMIN), with one difference: **delete here is a soft delete**
(`active: false`, `deleted: true`), not the hard delete positions use — so
`restore` exists for these two but not for positions.

```jsonc
// GET /v1/dev/permissions -> PagedResponse<PermissionTableResponse>, data[0]:
{ "id": 1, "subject": "users", "descriptionUz": "...", "descriptionRu": "Управление пользователями", "descriptionUzCyril": "...", "descriptionKaa": "...", "active": true }

// GET /v1/dev/permissions/{id} -> ApiResponse<PermissionDetailResponse> — same fields plus `audit` (createdAt/updatedAt/etc.), no `active` in the detail shape

// POST /v1/dev/permissions (ADMIN+)
{ "subject": "users", "descriptionUz": "...", "descriptionRu": "...", "descriptionUzCyril": "...", "descriptionKaa": "...", "active": true }
// PUT /v1/dev/permissions/{id} (ADMIN+) — same body shape as POST

// GET /v1/dev/actions -> PagedResponse<ActionTableResponse>, data[0]:
{ "id": 1, "code": "CARD_ATTACH", "descriptionUz": "...", "descriptionRu": "...", "descriptionUzCyril": "...", "descriptionKaa": "...", "active": true }

// POST /v1/dev/actions (ADMIN+) — same shape as permissions, "code" instead of "subject"
{ "code": "CARD_ATTACH", "descriptionUz": "...", "descriptionRu": "...", "descriptionUzCyril": "...", "descriptionKaa": "...", "active": true }
```

Both list endpoints take the same paging params as everywhere else
(`page`, `size`, `sortBy`, `sortDir`) plus per-field filters — `subject` /
`code` do a partial match, `active` an exact match. `sortBy` only accepts
`id` or `subject` (permissions) / `id` or `code` (actions); anything else is
ignored server-side rather than erroring.

Unlike positions, `DELETE /v1/dev/permissions/{id}` (SUPER_ADMIN) is never
blocked by other rows referencing it — it always succeeds and just flips
`active: false, deleted: true`. The one delete-adjacent conflict is on
*update*: `409 permission.update.deleted_conflict` if you `PUT` a row that's
already soft-deleted — restore it first. `PATCH .../{id}/restore`
(SUPER_ADMIN) clears `deleted`/`active` back. Same pair of endpoints and
semantics for `actions` (`action.update.deleted_conflict`).

---

## Roles

`/v1/dev/roles` is the dev-panel's own entry point into the same RBAC `Role`
table the main app's `/v1/roles` manages — same rows, same tiers as
`permissions`/`actions` above (list/get: any; create/update/permission
changes: ADMIN+; delete/restore: SUPER_ADMIN), same soft-delete/restore
pair. A role bundles a `name` (e.g. `isemid_epidemiologist`) with a set of
**permission grants** — each grant pairs one permission (subject) with the
subset of its actions the role is allowed to perform.

```jsonc
// GET /v1/dev/roles -> PagedResponse<RoleTableResponse>, data[0]:
{ "id": 1, "name": "ROLE_ADMIN", "active": true, "descriptionUz": "...", "descriptionRu": "Роль администратора", "descriptionUzCyril": "...", "descriptionKaa": "..." }

// GET /v1/dev/roles/{id} -> ApiResponse<RoleDetailResponse> — same fields plus `audit` and `permissions`:
{
  "id": 1, "name": "isemid_epidemiologist", "active": true,
  "descriptionUz": "...", "descriptionRu": "...", "descriptionUzCyril": "...", "descriptionKaa": "...",
  "audit": { "createdAt": "...", "updatedAt": "..." },
  "permissions": [
    { "id": 22, "subject": "DISEASE_PLACES", "description": "Места расположения больного",
      "actions": [ { "id": 1, "code": "READ", "description": "..." } ] }
  ]
}

// POST /v1/dev/roles (ADMIN+)
{ "name": "ROLE_ADMIN", "descriptionUz": "...", "descriptionRu": "...", "descriptionUzCyril": "...", "descriptionKaa": "...", "active": true }
// PUT /v1/dev/roles/{id} (ADMIN+) — same body shape as POST, `active` is required

// GET /v1/dev/roles/{id}/permissions -> ApiResponse<List<RolePermissionResponse>> — same shape as the `permissions` array above

// PATCH/PUT /v1/dev/roles/{id}/permissions (ADMIN+), PATCH /v1/dev/roles/{id}/permissions/remove (ADMIN+):
{ "permissions": [ { "permissionId": 22, "actionIds": [1, 2] } ] }
```

`PATCH .../permissions` merges each listed permission's actions into what
the role already has; `PUT .../permissions` replaces the role's whole
permission set with exactly what's given; `PATCH .../permissions/remove`
removes the listed actions from each permission, dropping the permission
entirely once no actions remain on it. All three respond with the full
`RoleDetailResponse`, so the frontend can re-render the role's permission
list straight from the response instead of re-fetching.

List filtering/sorting follows the same convention as `permissions`/`actions`
(`name` does a partial match, `active` an exact match; `sortBy` accepts `id`,
`name`, `descriptionUz`, `descriptionUzCyril`, `descriptionRu`, `descriptionKaa`
— anything else is ignored server-side).

---

## Organizations lookup

`GET /v1/dev/ref/organizations` (any) — search/select organizations without
ever needing a business-session bearer token. This is the fix for "the panel
cannot name organizations at all": `/v1/organizations/**` sits on the main
SSO/DHP bearer-token security chain, which a `/v1/dev/**` Basic-Auth account
has no way to obtain, so it was never reachable from here. This endpoint
reuses the exact same query as `GET /v1/organizations/lookup`, just mounted
on the dev-panel's own auth chain.

```jsonc
// GET /v1/dev/ref/organizations?search=poliklinika&limit=20
// -> ApiResponse<List<OrganizationLookupResponse>>, data[0]:
{
  "id": 336,
  "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Городская поликлиника №1",
  "active": true,
  "levelType": "DISTRICT",
  "medicalType": "OUTPATIENT"
}
```

Use `search` (matches name/TIN/phone/region/district code) to turn the
free-typed-UUID field in the integration-client create form (see
[Integration clients: `organizationId` is a uuid](#integration-clients-organizationid-is-a-uuid)
below) into an actual select: search by name, submit the `uuid` of the row
the operator picked. `id` here filters by the organization's *internal* Long
id, not its `uuid` — there's no lookup-by-uuid parameter, because nothing in
this endpoint's callers needs one; resolving a uuid you already have back to
a name is what `IntegrationClientResponse.organizationName` already does
server-side once a client is registered. `limit` defaults to 20, max 50 —
same convention as `/integration-clients/source-keys`.

---

## Errors

Same `ErrorResponse` shape as the rest of the API — `code` is the stable,
machine-checkable field; `message` is localized free text for the request's
current locale, safe to show to the user directly but not safe to match on
in code (it changes per-locale). The codes specific to this module:

| HTTP | `code` | When |
|---|---|---|
| 403 | `DEV_USER_PASSWORD_CHANGE_REQUIRED` | Caller has `mustChangePassword: true` and hit anything besides `GET/PATCH .../me[/password]` |
| 409 | `CONFLICT` | `dev-user.password.current.invalid` (wrong `currentPassword`), `dev-user.username.already-exists`, `dev-user.email.already-exists`, `dev-user.role.super-admin-not-creatable`, `dev-user.revoke.self-forbidden`, `dev-position.name.already-exists`, `dev-position.delete.in-use`, `permission.subject.already_exists`, `permission.update.deleted_conflict`, `action.code.already_exists`, `action.update.deleted_conflict`, `role.name.already_exists`, `role.update.deleted_conflict` — see `message` for which one |
| 404 | `NOT_FOUND` | `dev-user.not-found` / `dev-position.not-found` / `permission.not_found_by_id` / `action.not_found_by_id` / `role.not_found` — bad id |

`dev-user.password.change-required` is carved out with its own `code`
(rather than the generic `FORBIDDEN` every other 403 in this app uses) so
the frontend can route straight to the password-change screen — rather than
a generic error toast — by checking `code === "DEV_USER_PASSWORD_CHANGE_REQUIRED"`,
a single condition that works regardless of locale. In practice you
shouldn't need to: the `mustChangePassword` field on `GET .../me` (above)
tells you this before you ever hit the 403.

---

## Integration clients: `organizationId` is a uuid

`IntegrationClientCreateRequest.organizationId` is a `UUID` on purpose — it's
not a typo and not the field that needs fixing. It's the organization's
*business* identity: the same identifier space as the `X-Organization-Id`
header every request from that client must present, and the one the org's
own SSO/HR-side systems know it by. `IntegrationClientResponse.organizationId`
(and every other place in this app that addresses an organization —
`GET /v1/organizations/{id}`, `Organization.parent`, etc.) is the *internal*
auto-generated `Long` id instead, because that's what's actually stored as
the foreign key on the `IntegrationClient` row once
`OrganizationIdResolver.resolveActiveId(uuid)` has run at create time — and
`organizationName` is resolved from that same internal id
(`OrganizationMappingHelper.activeOrganizationNameById`). So: submit the
uuid, read back the internal id + a name — two different identifier spaces
used consistently on each side of the boundary, not one field guessing wrong
about its own type. The two never round-trip directly against each other,
and were never meant to; what was actually missing was a way to get from a
human-meaningful search to a uuid worth submitting in the first place — see
[Organizations lookup](#organizations-lookup) above, which is the real fix
here.

---

## Settings: finding a deleted row again

`DELETE /v1/dev/settings/{id}` is a soft delete (`deleted: true` on the row,
nothing removed) and `PATCH .../{id}/restore` clears that flag — but until
now `GET /v1/dev/settings` had no way to ask for deleted rows: the query
always filtered `deleted = false` unconditionally, and both `GET .../{id}`
and `GET .../by-key/{key}` 404 on a deleted row too. So restore was only
ever reachable in the same session that just deleted the row, while the id
was still on hand — which is why Stage 5's UI keeps the delete toast up for
15 seconds instead of just showing a generic "deleted" confirmation.

That's now fixed: `SystemSettingFilterRequest` takes a `deleted` filter,
same shape as `active`.

```jsonc
// GET /v1/dev/settings?deleted=true  -> only deleted rows
// GET /v1/dev/settings?deleted=false -> only non-deleted rows (same as omitting it)
// GET /v1/dev/settings               -> non-deleted rows (default unchanged)
```

Point the "restore" affordance at `GET /v1/dev/settings?deleted=true` instead
of (or in addition to) the delete-toast workaround — a deleted row is
findable and restorable at any point afterward now, not just for 15 seconds.
`GET .../{id}` and `GET .../by-key/{key}` are unchanged and still 404 on a
deleted row (restore first, then fetch by id/key).

**`dev-users` doesn't have this problem** — it was never in the same
situation. `PATCH .../dev-users/{id}/revoke` only ever sets `enabled: false`;
`GET /v1/dev/dev-users` never filters by `enabled` unless you ask it to
(`DevUserSpecification` has no unconditional predicate, unlike settings'
`deleted = false`), so a revoked account stays visible in the default list
and filterable with `enabled=false` — nothing to fix for Stage 6.
