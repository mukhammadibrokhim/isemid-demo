# Patient module

Package: `uz.uzinfocom.app.modules.patient`. A patient/person record —
demographics, national ID, addresses, workplace/school affiliation. Created
as a side effect of registering a `form058`, via
`PatientRegistrationService.create()` called from form058's
`CreateForm058Service`.

**No REST surface of its own** — there is no `web/controller` package and no
`ApiPaths.Patient`. The only way to create or touch a patient is indirectly,
through the form058 creation flow.

## Layout

```
patient/
├── application/    command, mapper, query/dto, query/mapper, service
├── domain/          model, enums, repository
├── infrastructure/persistence/  repository
└── web/              mapper, request   (no controller package)
```

## Entities

- `Patient`
- `PatientAddress` — `AddressType` enum: `PERMANENT`, `TEMPORARY`
- `PatientAffiliation` — `AffiliationType` enum: `WORKPLACE`, `EDUCATIONAL`
- `PatientIdentifier`

No class-level javadoc exists on these yet — behavior should be read
directly from `PatientRegistrationService` and the entities themselves
rather than assumed from this doc.
