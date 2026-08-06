# Form058 module

Package: `uz.uzinfocom.app.modules.form058`. Form No. 058 — the
infectious/parasitic disease notification sent between medical
organizations. It's the aggregate root the `card` module attaches to and the
trigger for `patient` registration.

## Layout

Fully layered — the most complete of the four modules:

```
form058/
├── application/
│   ├── command/           create, update, approve, cancel, delete
│   ├── query/
│   ├── security/
│   ├── shared/
│   ├── validator/
│   └── exception/
├── domain/
│   ├── model/               Form058, Form058Location
│   ├── model/embedded/       Form058ApprovalInfo, CancellationInfo, ClinicalInfo,
│   │                          DateInfo, DeleteInfo, DiagnosisInfo, EpidemicInfo, ReportInfo
│   ├── enums/                 FormStatus
│   └── exception/
├── infrastructure/persistence/  repository, specification
└── web/                          controller, dto/request, dto/response, mapper, resolvers
```

## Status lifecycle (`FormStatus`)

```
SENT ──accept (receiver)──► ACCEPTED ──linkCards()──► CARD_LINKED ──── approve (sender, finalIcd10 required) ──► APPROVED (final)
  │
  └──cancel (sender OR receiver, reason)──► CANCELED (final, locked —
        only while still SENT              only a super admin may act on
                                            it: reopen back to SENT, edit,
                                            or delete)
```

Three decisions on a form, three different actors:

1. **Cancel** — shared between both organizations, but only while the form
   is still `SENT`: the sender withdraws it, or the receiver rejects it —
   both call the same `cancel` endpoint and land on the same `CANCELED`
   status (`FormStatus.isCancellable()`, i.e. `SENT` only). See
   `Form058CancelValidator`. There is no separate "rejected" status and no
   separate reject endpoint — once the receiver has accepted a form, it can
   no longer be canceled by either side.
2. **Accept** — the receiver's call, the only other decision available on a
   freshly `SENT` form. `accept()` opens it up for processing (`ACCEPTED`)
   — see `Form058AcceptValidator`.
3. **Approve** — the sender's call (the institution that originally
   diagnosed and reported the case), reachable only once a card has been
   linked (`FormStatus.isApprovable()`, i.e. `CARD_LINKED`). A form can
   never be approved straight from `SENT`/`ACCEPTED`. See
   `Form058ApprovalValidator`.

Neither the sender nor the receiver organization can touch a `CANCELED`
form — `ensureEditable()`, `linkCards()` and `isCancellable()` all reject
it. Only `AdminAccessGuard.isSuperAdmin()` can `reopen()` it back to `SENT`,
edit it, or delete it (see `Form058ReopenValidator`, `Form058UpdateValidator`,
`Form058DeleteValidator`).

`linkCards()` (called by the `card` module's `assignCards` flow) requires
`ACCEPTED` or later — it throws `error.form058.card-link-not-allowed` from
`SENT` — and only advances the status forward from there: a form already at
`CARD_LINKED` stays there when another card is (re)assigned to it.

The receiver organization must have `Organization.medicalType ==
MedicalType.SANEPID_SERVICE` — enforced in `Form058CreateValidator` on
create, and again in `Form058UpdateValidator` whenever `receiverOrganizationId`
is being changed — otherwise a 400 (`error.form058.receiver-not-sanepid`) is
thrown.

The epidemiological report module (`modules/report/form1`, `form2`, `form4`,
`form6`) already excludes `CANCELED` cases from its `PRIMARY` bucket
(`status NOT IN ('APPROVED', 'CANCELED')`), so a receiver-rejected form058 is
automatically excluded too — there is nothing status-specific left to do
there now that rejection and cancellation share one status.

## Endpoints (`ApiPaths.Form058`, root `/v1/form-058`)

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| PATCH | `/{id}/accept` | Receiver accepts an incoming form (`SENT` → `ACCEPTED`) |
| PATCH | `/{id}/approve` | Sender's final approval with the definitive diagnosis (`CARD_LINKED` → `APPROVED`) |
| PATCH | `/{id}/cancel` | Sender withdraws or receiver rejects the form, only while `SENT` (`CANCELED`, final/locked) |
| PATCH | `/{id}/reopen` | Super-admin-only: puts a `CANCELED` form back to `SENT` |
| GET | `/` | List/filter |
| GET | `/by-nnuzb` | Lookup by national ID |
| GET | `/{id}` | Detail |

`/{id}/cards` and `/{id}/cards/assign` are also declared under
`ApiPaths.Form058` but are implemented in the **card** module
(`Form058CardQueryController` / `Form058CardCommandController`) — form058
itself has no card-related controller code, keeping the dependency direction
one-way (card depends on form058, not the reverse).

## Notable design decisions (from in-code javadoc)

- `Form058.patient` deliberately avoids `CascadeType.ALL` — form058 "must not
  control Patient lifecycle."
- `Form058Location`'s cascade is PERSIST/MERGE only; remove is intentionally
  not cascaded.
- `assignedCardId` is `@Deprecated`, kept only for existing API/DB
  compatibility — the `hasLinkedCards` boolean plus the `card` module's own
  join table is the current mechanism.
