# Act module

Package: `uz.uzinfocom.app.modules.act`. Represents a lab/procedure order
attached to a `Card` — **deliberately a minimal placeholder**, not a full
rebuild of the legacy Act feature.

## Layout

Only two subpackages exist:

```
act/
└── domain/
    ├── enums/    ActStatus
    └── model/    Act
```

No `application`, `infrastructure`, or `web` layer exists. `ApiPaths.Act.ROOT`
(`/v1/acts`) is declared in the shared constants but **unused** — there is no
`ActController` anywhere in the codebase. The only way an `Act` row gets
created today is the `card` module's `assignAct` operation, which inserts a
bare `Act` row with just an `actType` string.

## Entity

`Act` fields: `actType` (`String`), `actStatus` (`ActStatus`), `card` /
`cardId` (shadow FK, `insertable/updatable = false`, matching the legacy
FK-shadowing pattern).

`ActStatus`: `NEW`, `IN_PROGRESS`, `COMPLETED`, `NOT_VIEWED`, `ACT_ATTACHED`.

## Why it's a stub

In-code javadoc on `Act` states this explicitly: the legacy system has 6 act
subtypes (`ACT153`/`154`/`155`/`156`/`223`/`224`), and rebuilding that whole
feature was out of scope for the card module work. This entity exists only
so `Card.acts` and the assign-act flow have somewhere to attach to.

**Do not add subtype-specific fields to `Act` without first designing the
Act module properly** — extending this placeholder ad hoc would reintroduce
the same god-entity problem the rest of this rebuild has been avoiding.
