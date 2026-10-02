# Form 129 — frontend integration guide

Covers **Form №129** — a lab-to-sanitary-epidemiological-committee (SES)
notification of infectious-disease serology results (syphilis, hepatitis B,
brucellosis), submitted by AKP/hospital/blood-transfusion-center
laboratories. Unlike Form №058/058-1, Form 129 is a **pure registry with no
card-linking, no approval step, and no attachments** — its lifecycle is just
create → receiver accepts or rejects.

For full request/response field lists, use the generated Swagger/OpenAPI UI
("Form 129" and "Integration - Form 129" tags) — every DTO field carries a
`@Schema` description there. This guide focuses on the workflow and the
lab-result field shape, since those aren't obvious from the schema alone.

## Base paths

| Surface | Root |
|---|---|
| Frontend (SSO/DHP) | `/v1/form-129` |
| External integration (any source, DMED included) | `/integration/v1/{source}/form-129` |

Unlike Form058/Form0581, there is **no separate DMED-specific fixed-contract
endpoint** — every source, DMED included, submits through the same generic
`/integration/v1/{source}/form-129` path.

## Response envelope

Same wrapper as every other module. On success:

```json
{
  "success": true,
  "message": "Record created successfully.",
  "data": { "...": "..." }
}
```

On error:

```json
{
  "success": false,
  "code": "CONFLICT",
  "message": "Form129 accept is not allowed for status: CANCELED",
  "traceId": "9f1c2a3b4d",
  "path": "/v1/form-129/123/accept",
  "timestamp": "2026-08-21T12:00:00Z",
  "violations": []
}
```

| `code` | HTTP status | When |
|---|---|---|
| `VALIDATION_FAILED` | 400 | Bad input — missing required field, receiver org not a SANEPID_SERVICE institution, sender == receiver, empty reject reason, etc. |
| `FORBIDDEN` / `SCOPE_VIOLATION` | 403 | Authenticated, but the wrong organization is calling (e.g. sender trying to `accept`/`reject`, only the receiver may). |
| `NOT_FOUND` | 404 | The form id doesn't exist (or isn't visible to the caller). |
| `CONFLICT` | 409 | The action isn't valid for the form's current status — `accept`/`reject` only work while `SENT`. |

## Status workflow

Three statuses only — simpler than Form058/Form0581, since there is no card
linkage or approval step:

```
SENT ──accept (receiver only)──► ACCEPTED
  │
  └──reject (receiver only, reason required, only while SENT)──► CANCELED
```

- **`SENT`** — initial status right after creation.
- **`ACCEPTED`** — the receiver (SES) organization confirmed receipt.
- **`CANCELED`** — the receiver rejected the form, with a mandatory reason.

**Who can call what:**

| Action | Who | Required current status | Result |
|---|---|---|---|
| `accept` | Receiver org only | `SENT` | → `ACCEPTED` |
| `reject` | Receiver org only | `SENT` | → `CANCELED` |

Unlike Form058/Form0581's `cancel`, **the sender cannot withdraw a Form129**
— only the receiving SES organization decides accept/reject. There is also
no `update`, `delete`, or `reopen` action, and no super-admin escape hatch
out of `CANCELED` — once rejected, a Form129 stays closed.

A super admin (`isemid_super_admin` authority) bypasses the organization-scope
check on `accept`/`reject`, but not the status precondition.

## Endpoints

### Create

```
POST /v1/form-129
```
Registers a new Form129 (and the patient, if not already registered).
Initial status is `SENT`. `senderOrganizationId` must be the caller's active
organization; `receiverOrganizationId` must be a `SANEPID_SERVICE`
institution (see below). No file/attachment fields exist on this form.

### Accept — receiver confirms receipt

```
PATCH /v1/form-129/{id}/accept
```
```json
{
  "receiverFullName": "Aliyev A.A."
}
```
`receiverFullName` optional (max 255 chars) — the name of the person at the
receiving SES institution who accepted it. Requires the caller's active
organization to be the form's `receiverOrganizationId`. 409 if the form
isn't `SENT`.

### Reject — receiver declines, with a reason

```
PATCH /v1/form-129/{id}/reject
```
```json
{
  "reason": "Duplicate submission"
}
```
`reason` **required** (max 1000 chars) — 400 `VALIDATION_FAILED` if blank.
Requires the caller's active organization to be the form's
`receiverOrganizationId`. 409 if the form isn't `SENT`.

### List — outgoing/incoming split

```
GET /v1/form-129?direction=INCOMING&page=1&size=20
```
Same `direction` convention as every other form module:

| `direction` | Meaning |
|---|---|
| `OUTGOING` | Forms sent by the caller's active organization (`senderOrganizationId` match). |
| `INCOMING` | Forms received by the caller's active organization (`receiverOrganizationId` match). |
| `ALL` | Every form visible to the org's scope — **super-admin only**. |

Other filters: `status`, `dateFrom`/`dateTo` (created-date range), `id`,
`documentValue` (patient PINFL/passport/etc.), `organizationId`, `source`,
`page`, `size`, `sortBy`, `sortDir`. No `hasLinkedCards` or `icd10Code` —
those don't apply to this form.

### Detail

```
GET /v1/form-129/{id}
```
Returns the full record: patient, all lab-test results, sender/receiver
org ids, cancellation info (if rejected), and audit stamps.

## Receiver organization constraint

On `create`, the receiver organization must have
`medicalType == SANEPID_SERVICE` — otherwise the API returns
`VALIDATION_FAILED` (400) with message key `error.form129.receiver-not-sanepid`.
Filter any organization picker in the "receiver" field to SANEPID_SERVICE
institutions to avoid a round-trip error.

## Lab result fields

Each of the 13 boolean tests uses the same two-field pair — an `*Outcome`
enum (`NEGATIVE` / `POSITIVE`, omit if not tested) and an editable free-text
`*ResultText` (populated for a positive result, e.g. a titre):

| Test | Outcome field | Text field |
|---|---|---|
| Реакция Вассермана (RW) | `rwOutcome` | `rwResultText` |
| RPR / VDRL | `rprVdrlOutcome` | `rprVdrlResultText` |
| РПГА (RPGA) | `rpgaOutcome` | `rpgaResultText` |
| ИФА (ELISA) | `elisaOutcome` | `elisaResultText` |
| TPHA | `tphaOutcome` | `tphaResultText` |
| Иммуноблот (Western blot) | `westernBlotOutcome` | `westernBlotResultText` |
| HBsAg | `hbsAgOutcome` | `hbsAgResultText` |
| HBeAg | `hbeAgOutcome` | `hbeAgResultText` |
| Anti-HBc IgG | `antiHbcIgGOutcome` | `antiHbcIgGResultText` |
| Anti-HBc IgM | `antiHbcIgMOutcome` | `antiHbcIgMResultText` |
| Anti-HBe | `antiHbeOutcome` | `antiHbeResultText` |
| Anti-HBs | `antiHbsOutcome` | `antiHbsResultText` |
| Метод ПЦР (качественный) | `pcrQualitativeOutcome` | `pcrQualitativeResultText` |

The Wright-Heddelson brucellosis test is the one **three-state** exception
(`NEGATIVE` / `DOUBTFUL` / `POSITIVE`): `wrightHeddelsonOutcome` +
`wrightHeddelsonResultText`.

All fields are optional on `create` — send only the tests actually
performed.

## Outbound notifications

If a form was submitted through an external integration channel rather than
the SSO/DHP frontend, every status change (`accept`/`reject`) automatically
triggers an outbound webhook back to that same external system, carrying the
new status and (for `reject`) the reason. This is transparent to the
frontend — no extra call is needed.

## In-app notifications

Three notification types, following the same pattern as every other form
module — see
[notification-frontend-guide.md](./notification-frontend-guide.md) for the
generic contract:

| Type | Recipient | When |
|---|---|---|
| `FORM129_RECEIVED` | Receiver org | On `create` |
| `FORM129_ACKNOWLEDGED` | Sender org | On `accept` |
| `FORM129_REJECTED` | Sender org | On `reject` |
