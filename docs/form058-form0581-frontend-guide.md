# Form 058 / Form 058-1 — frontend integration guide

This covers the status-workflow API for **Form №058** (infectious/parasitic
disease notification) and **Form №058-1** (suspected-rabies animal-bite
notification). The two modules are independent entities/tables but share an
**identical status workflow, action set, and authorization model** — this
guide documents both together; anywhere it says "the form" it applies
equally to either one.

For full request/response field lists for `create`/`update`/list-and-filter
endpoints (patient info, diagnosis fields, dates, etc.), use the generated
Swagger/OpenAPI UI — every DTO field already carries a `@Schema` description
there. This guide focuses on the status-transition actions, since those
carry authorization rules that aren't obvious from the schema alone.

## Base paths

| Form | Root |
|---|---|
| Form №058 | `/v1/form-058` |
| Form №058-1 | `/v1/form-058-1` |

## Response envelope

Every endpoint returns the same wrapper on success:

```json
{
  "success": true,
  "message": "Record accepted successfully.",
  "data": { "...": "..." }
}
```

On error, a different shape, with an HTTP status matching `code`:

```json
{
  "success": false,
  "code": "CONFLICT",
  "message": "Form058 accept is not allowed for status: APPROVED",
  "traceId": "9f1c2a3b4d",
  "path": "/v1/form-058/123/accept",
  "timestamp": "2026-08-06T12:00:00Z",
  "violations": []
}
```

| `code` | HTTP status | When |
|---|---|---|
| `VALIDATION_FAILED` | 400 | Bad input — missing required field, receiver org not a SANEPID_SERVICE institution, sender == receiver, etc. |
| `FORBIDDEN` / `SCOPE_VIOLATION` | 403 | Authenticated, but the wrong organization is calling (e.g. sender trying to `accept`), or not a super admin for a super-admin-only action. |
| `NOT_FOUND` | 404 | The form id doesn't exist (or isn't visible to the caller). |
| `CONFLICT` | 409 | The action isn't valid for the form's **current status** (e.g. calling `approve` before a card is linked, or `cancel` after the form was already accepted). |

## Status workflow

Both forms use the same 5-value status lifecycle:

```
SENT ──accept (receiver)──► ACCEPTED ──linkCards()──► CARD_LINKED ──approve (sender)──► APPROVED
  │
  └──cancel (sender OR receiver, only while SENT)──► CANCELED ──reopen (super admin only)──► SENT
```

- **`SENT`** — initial status right after creation.
- **`ACCEPTED`** — the receiver organization has accepted the incoming form. Cards can now be linked (via the `card` module's assign-cards endpoints).
- **`CARD_LINKED`** — at least one card has been linked to the form.
- **`APPROVED`** — final, locked. Set by the sender with the definitive diagnosis. Cannot be edited, canceled, or reopened by anyone (not even a super admin).
- **`CANCELED`** — final, locked for both organizations. Reached either by the sender withdrawing the form or the receiver rejecting it — **both use the same `cancel` action and land on the same status**; there is no separate "rejected" status. Only a super admin can act on a `CANCELED` form (`reopen`, edit, or delete).

**Who can call what:**

| Action | Who | Required current status | Result |
|---|---|---|---|
| `accept` | Receiver org only | `SENT` | → `ACCEPTED` |
| `cancel` | **Either** sender org **or** receiver org | `SENT` only | → `CANCELED` |
| `approve` | **Sender** org only (not the receiver!) | Form058: `ACCEPTED` or `CARD_LINKED`. Form0581: `CARD_LINKED` only | → `APPROVED` |
| `reopen` | **Super admin only** | `CANCELED` only | → `SENT` |

A super admin (`isemid_super_admin` authority) bypasses every organization-scope
check above, but not the status precondition — except for `reopen`, `update`,
and `delete`, where a super admin is additionally allowed to act on a
`CANCELED` form that a regular user could no longer touch.

## Endpoints

### Accept — receiver accepts an incoming form

```
PATCH /v1/form-058/{id}/accept
PATCH /v1/form-058-1/{id}/accept
```
No request body. Requires the caller's active organization to be the form's
`receiverOrganizationId`. 409 if the form isn't `SENT`.

### Approve — sender's final decision with the definitive diagnosis

```
PATCH /v1/form-058/{id}/approve
PATCH /v1/form-058-1/{id}/approve
```
```json
{
  "finalIcd10Code": "A00",
  "finalIcd10Name": "Cholera"
}
```
Both fields required (max length 20 / 512). Requires the caller's active
organization to be the form's `senderOrganizationId`. 409 if the form isn't
`CARD_LINKED` yet (i.e. no card has been linked).

### Cancel — sender withdraws, or receiver rejects

```
PATCH /v1/form-058/{id}/cancel
PATCH /v1/form-058-1/{id}/cancel
```
```json
{
  "reason": "Duplicate submission"
}
```
`reason` required (max length 1000). Requires the caller's active
organization to be **either** the sender **or** the receiver. 409 if the
form isn't `SENT` — once accepted, neither side can cancel anymore.

### Reopen — super-admin-only recovery of a closed form

```
PATCH /v1/form-058/{id}/reopen
PATCH /v1/form-058-1/{id}/reopen
```
No request body. Requires the caller to hold the `isemid_super_admin`
authority. 409 if the form isn't `CANCELED`. Puts the form back to `SENT` so
the workflow can run again from the top.

### Everything else

`POST /` (create), `PUT /{id}` (update), `DELETE /{id}` (delete), `GET /`
(list/filter), `GET /{id}` (detail) all exist per the usual REST shape and
are documented in full via Swagger. Note: `update` and `delete` are blocked
once a form is `APPROVED` or `CANCELED` for everyone except a super admin,
who may still edit/delete a `CANCELED` (but never an `APPROVED`) form.

## Receiver organization constraint

On `create` (and on `update` whenever `receiverOrganizationId` is being
changed), the receiver organization must have
`medicalType == SANEPID_SERVICE` — otherwise the API returns
`VALIDATION_FAILED` (400) with message key `error.form058.receiver-not-sanepid`
/ `error.form0581.receiver-not-sanepid`. Make sure any organization picker
in the "receiver" field is filtered to SANEPID_SERVICE institutions to avoid
a round-trip error.

## Affiliated organizations

Form №058 only (no Form0581 equivalent). Besides the sender/receiver
relationship, an organization can also see a form because the **patient's
workplace or place of study** (`PatientAffiliation`, type `WORKPLACE` or
`EDUCATIONAL`) is that organization — e.g. a санэпидстанция responsible for
a factory or a school. This "affiliated" organization is neither the
sender nor the receiver, and the normal `GET /v1/form-058?direction=...`
listing will never show it the form.

### Listing: a separate endpoint, not a filter flag

```
GET /v1/form-058/affiliated
```

Same query-parameter shape as the main listing (`page`, `size`, `sortBy`,
`sortDir`, `status`, `dateFrom`, `dateTo`, `id`, `documentValue`,
`icd10Code`, `source`, `hasLinkedCards`) — but **no `direction` and no
`organizationId`/`regionCode`/`districtCode`**. Those describe the
sender/receiver side and have no meaning here; the endpoint doesn't accept
them at all, on purpose, so there's nothing to send by mistake.

Each row is wrapped with the affiliation type that explains why it's
visible to you:

```jsonc
// GET /v1/form-058/affiliated  ->  PagedResponse<Form058AffiliatedTableResponse>
{
  "success": true,
  "message": "Успешный запрос.",
  "data": [
    {
      "form": {
        "id": 123,
        "uuid": "...",
        "createdAt": "2026-08-10T09:00:00Z",
        "status": "CARD_LINKED",
        "icd10Code": "A09",
        "senderOrganizationId": 10,
        "receiverOrganizationId": 20,
        "patient": { "id": 456, "firstName": "...", "lastName": "..." }
      },
      "affiliationType": "WORKPLACE"
    }
  ],
  "meta": { "page": 1, "size": 20, "totalElements": 3, "totalPages": 1 }
}
```

Note the extra nesting level — `data[i].form.id`, not `data[i].id`. This
differs from the plain `GET /v1/form-058` response shape on purpose: the
`affiliationType` field is meaningless for every other listing, so it isn't
bolted onto the shared row type.

For the full detail view (`GET /v1/form-058/{id}`), no separate endpoint is
needed — `patient.affiliations[]` in `Form058DetailResponse` already lists
every affiliation record (`type`, `organizationId`, ...); find the entry
where `organizationId` matches your own to get the same information.

### Attaching an Act — the reason this list exists

Once the form reaches `CARD_LINKED`, cards exist and an affiliated
organization may attach an `Act` to one of them, same as the sender/receiver
could:

```
POST /v1/cards/{cardId}/acts
```

The backend checks that the caller's active organization is the form's
sender, its receiver, **or** an affiliated organization for that form's
patient — anyone else gets `403` (`code: "SCOPE_VIOLATION"`). The same
check applies to `POST /v1/form-058/{id}/cards/assign` (linking cards in
the first place). There is no separate "am I allowed" endpoint — attempt
the action and handle a `403` the same way you already handle one on
`accept`/`approve`/`cancel`.

### Notifications

Two dedicated notification types exist for this flow — see
[notification-frontend-guide.md § Affiliation types](./notification-frontend-guide.md#affiliation-types--a-third-kind-of-recipient)
for the full contract: `FORM058_AFFILIATED_RECEIVED` (form just became
visible to you) and `FORM058_AFFILIATED_CARD_LINKED` (cards now exist, you
may attach an Act).

## Outbound notifications

If a form was originally submitted through an external integration channel
(e.g. DMED) rather than the SSO/DHP frontend, every status change — including
`accept` — automatically triggers an outbound webhook to that same external
system. This is transparent to the frontend; no extra call is needed to
"notify" anyone.
