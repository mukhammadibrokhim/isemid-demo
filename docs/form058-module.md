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
NOT_APPROVED → SENT → RECEIVED → CARD_LINKED → APPROVED_PENDING → APPROVED / CANCELED
```

`linkCards()` (called by the `card` module's `assignCards` flow) only
advances this status forward — a form already at `APPROVED_PENDING` or
later never regresses when cards are (re)assigned to it.

## Endpoints (`ApiPaths.Form058`, root `/v1/form-058`)

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Create |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| POST | `/{id}/approve` | Approve |
| POST | `/{id}/not-approve` | Reject approval |
| POST | `/{id}/cancel` | Cancel |
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
