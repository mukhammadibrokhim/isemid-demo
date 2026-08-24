# Act module

Package: `uz.uzinfocom.app.modules.act`. Represents a lab/procedure act
(далолатнома) attached to a `Card` and sent to an external Laboratory
Information System (LIS) for a result. Handler-per-type pattern, mirrored
directly off the `card` module's.

## Act types

Legacy had 6 subtypes (`ACT153`/`154`/`155`/`156`/`223`/`224`). **Act155 was
removed** (2026-08-24) — its functionality was absorbed into Act154, leaving
5:

| Type | Meaning (per `Act.xlsx` LIS field spec) | Uses shared `EmployeeInfo` embeddable |
|---|---|---|
| `ACT153` | Water sampling | yes |
| `ACT154` | Food-product sampling (absorbed ACT155's pesticide-residue scope) | yes |
| `ACT156` | External-environment swabs | no — own flat columns (`full_name_of_sampler`, etc.) |
| `ACT223` | Soil sampling | yes |
| `ACT224` | Sanitary inspection | no — own flat columns (`full_name_of_epid_staff`, etc.) |

Only 153/154/223 share the `sampler`/`participant` `EmployeeInfo`
`@Embeddable` (with `@AttributeOverrides`); a change to `EmployeeInfo` (e.g.
the `identifierType`/`identifierValue` columns added 2026-08-24, so a
facility participant's name can be resolved from the citizen registry by
PINFL/passport) only reaches those three automatically — 156/224 need
separate work.

## Layout

```
act/
├── domain/
│   ├── enums/          ActType, ActStatus, SubjectType, LengthUnit, SampleVolumeUnit, SampleQtUnit
│   └── model/
│       ├── Act.java     abstract root, JOINED inheritance, card_id FK, act_users join table
│       ├── embedded/     Institution, LisInfo, ActDeleteInfo, EmployeeInfo, ConditionInfo,
│       │                 ConservationTypeInfo, PackageTypeInfo, Purpose, ResearchItemTypeInfo,
│       │                 SampleTypeInfo, LocalizedText
│       └── act153/, act154/, act156/, act223/, act224/   one XxxDetail child entity per type
├── application/
│   ├── command/          ActCommandService (create/update/delete/status transitions),
│   │                       ActLisSendService (the LIS-send call itself)
│   ├── query/              ActQueryService, ActStatsQueryService, ActFilterRequest, response DTOs
│   ├── handler/             ActTypeHandler<C,Q,R> + ActTypeHandlerRegistry (fails fast at
│   │                          startup if any ActType lacks a handler) + one XxxHandler per type
│   └── exception/           UnsupportedActTypeException, InvalidActStatusException,
│                              ActAlreadySentToLisException, ActScopeViolationException, etc.
├── mapper/                  one MapStruct XxxMapper per act type + ActEmbeddedMapper
└── web/
    ├── controller/           ActCommandController/ActQueryController (/v1/acts/*, pure "Act" tag)
    │                          + CardActCommandController/CardActQueryController
    │                            (/v1/cards/{id}/acts/*, "Card" tag — act creation is
    │                            card-triggered only, mirrors Form058CardCommandController)
    └── dto/request, response
```

No standalone `POST /v1/acts` exists — the only way to create acts is
`CardActCommandController.assignActs` (`POST /v1/cards/{id}/acts`), which
bulk-creates one blank act per requested type, all attached to the same set
of employees. Employees then see them via `GET /acts/mine` and fill them in
via `PUT /acts/{id}`.

## Lifecycle

One `ActStatus` for every type, no accept/reject/supervisor-approval gate
(unlike `Card`/`Form058`):

```
NEW → IN_PROGRESS → READY → SENT → COMPLETED
                       ↑        ↓
                       └── SEND_FAILED
```

- `update` (`PUT /acts/{id}`): freely re-saveable from NEW/IN_PROGRESS/READY/
  SEND_FAILED, always lands on IN_PROGRESS. Blocked once SENT/COMPLETED.
- `markReady`: IN_PROGRESS or SEND_FAILED → READY.
- `markSendingToLis` / `ActLisSendService.send`: READY or SEND_FAILED → SENT
  (records `LisInfo.attempt`/`sentDate`, clears any previous `lastError`).
- `recordLisSendSuccess` / `recordLisSendFailure`: called after the actual
  HTTP call — success attaches the LIS-side act id; failure moves SENT →
  SEND_FAILED with a reason, staying editable/re-sendable.
- `receiveLisResponse` (`POST /acts/{id}/lis-callback`): SENT → COMPLETED
  only, stores the full LIS response JSON.
- `delete`: soft-delete, blocked once SENT or COMPLETED
  (`ActAlreadySentToLisException`).

## LIS integration

Outbound client lives in `integration/lis/` (see top-level `CLAUDE.md`).
`LisResearchCode` (`integration/lis/client/dto/`) maps each `ActType` to the
research code LIS expects — updated when Act155 was removed.
`LisUnsupportedActTypeException` fires if a type has no mapping.

## Recent changes

### Act155 removal (2026-08-24)

Act155 (pesticide-residue act) was deleted from the codebase entirely — the
user confirmed its functionality was absorbed into Act154 and it is no
longer a distinct type. Removed:

- `domain/model/act155/` (`Act155`, `Act155Detail`)
- `application/handler/act155/Act155Handler`
- `application/query/dto/detail/Act155DetailResponse`,
  `application/query/dto/detail/act155/Act155SampleResponse`
- `mapper/act155/Act155Mapper`
- `web/dto/request/Act155Request`, `web/dto/request/act155/Act155SampleRequest`
- the `ACT155` value from `ActType`, and its dispatch branches in
  `ActDetailMapper` / `LisResearchCode` / `LisUnsupportedActTypeException`
- i18n keys across all 6 locale bundles

Two new Liquibase changesets (both wired in automatically via the master
changelog's `includeAll` — see the migrations section of the root
`CLAUDE.md`):

- `db.migration/changelog/zzz-card-act/20260824-1000-drop-act155.xml` —
  drops the `act155_detail` then `act155` tables outright (no dead schema
  kept around).
- `db.migration/changelog/reference/20260824-1000-deactivate-act155-catalog-entry.xml`
  — soft-deletes the `ACT_TYPE`/`ACT155` row in `ref_catalog` for
  already-seeded environments (editing `catalog_reference.csv` alone has no
  effect once the one-time seed has run).

Same session also added `db.migration/changelog/zzz-card-act/20260824-1100-add-employee-identifier-columns.xml`,
adding `sampler_identifier_type/value` and `participant_identifier_type/value`
columns to `act153`/`act154`/`act223` (the three tables that embed
`EmployeeInfo`) — support for resolving a facility participant's name from
the citizen registry by identifier, since unlike the sampler (a system
user) a participant has no login of their own.

Verified via `./mvnw compile`, the act module's unit test suite
(`ActCommandServiceStatusTransitionTest`, `ActCommandServiceAssignActsTest`,
`ActQueryServiceTest`, `ActStatsQueryServiceTest`), and the ArchUnit boundary
suite (`CardModuleBoundaryTest`, `PlatformModuleBoundaryTest`,
`EntityNameUniquenessTest`) — all green, no leftover `Act155`/`act155`
references outside the two drop/deactivate migrations themselves.
