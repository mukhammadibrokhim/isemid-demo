# Card module

Package: `uz.uzinfocom.app.modules.card`. Five epidemiological investigation
card types, each attached to exactly one `Form058`. Rebuilt from the legacy
`uz.uzinfocom.isemid.features.card` module, used purely as a field/domain
specification — legacy architecture defects were not carried over (see
"Legacy fixes" below).

## Card types

| Type | Child entities | Notes |
|---|---|---|
| `CARD161` | 9 (`Vaccination`, `Card161RiskFactor`, `InfectionSource`, `EnvironmentalSource`, `EnvironmentalLabTest`, `ContactPerson`, `ScreenedGroup`, `HomePreventiveMeasure`, `OutbreakDisinfectionMeasure`) + 1:1 `InfectionSourceDetail` | Largest type; also the reference implementation for the child-collection sync pattern (see below). |
| `CARD174` | 2 (`InfectionMonitoring`, `OutbreakControlMeasure`) | |
| `CARD175` | none (flat) | Simplest type — no child collections at all. |
| `CARD205` | 3 (`InformationOtherBittenPeople`, `InformationOtherBittenAnimals`, `InformationAboutAnimaBittenPeople`) | Two of the three children share an identical 6-field address breakdown, extracted into the `AdministrativeAddress` `@Embeddable`. |
| `CARD_TUBE` | 4 (`ContactMonitoring`, `InfectionSource`, `TBHistory`, `XRay`) | Its own `InfectionSource`, distinct from CARD161's — both entities are given explicit `@Entity(name = ...)` values since Hibernate's default (bare class name) would otherwise collide across packages. |

## Layout

```
card/
├── domain/
│   ├── model/            Card (abstract, JOINED inheritance) + one package per type
│   └── enums/             CardType, CardStatus
├── application/
│   ├── command/            CardCommandService (single flat service — see below)
│   ├── query/               CardQueryService, CardFilterRequest, response DTOs
│   └── handler/              CardTypeHandler<C,Q,R> + CardTypeHandlerRegistry
│                              + ChildCollectionSync (shared child-upsert utility)
│                              + one XxxHandler per card type
├── mapper/                  one MapStruct XxxMapper per card type
└── web/
    ├── controller/           CardCommandController, CardQueryController (/v1/cards/*)
    │                          + Form058CardCommandController/QueryController (Form058-scoped paths)
    └── dto/request, response
```

`CardCommandService`/`CardQueryService` are deliberately **flat**, not split
into one class per operation — all operations act on the same aggregate and
share the same status-transition rules; splitting into 6+ single-method
service classes would have been premature abstraction at this scale.

## Handler-per-type pattern

```java
public interface CardTypeHandler<C extends Card, Q extends CardRequest, R extends CardDetailResponse> {
    CardType getType();
    C createBlank();
    void update(C card, Q request);
    void validate(C card);
    R toResponse(C card);
}
```

`CardTypeHandlerRegistry` collects all `CardTypeHandler` beans into an
`EnumMap<CardType, ...>` at startup and fails fast if a type is missing or
duplicated (`CardTypeHandlerRegistryTest`). Adding a 6th card type requires
touching only: `CardType` enum value, the entity package, the request/response
DTOs (added to the `CardRequest`/`CardDetailResponse` sealed interfaces), the
MapStruct mapper, and a new `@Component` handler — no changes to the service,
controllers, or registry.

## Status lifecycle (`CardStatus`)

```
assignCards ──► NEW ──accept──► ACCEPTED_BY_USER ──update──► IN_PROGRESS ──complete──► COMPLETED
                 │                     │                         │(repeatable save)         │
                 └──reject──► REJECTED_BY_USER            (update keeps saving)     supervisor approve/reject
                        │                                                                 │        │
                        └──(supervisor reassign)──► NEW                              APPROVED   REJECTED
                                                                                       (final)   │
                                                                                          update/complete again ──┘
```

Every transition rule is a predicate method on the enum itself
(`canBeAcceptedByUser()`, `canBeRejectedByUser()`, `canBeUpdated()`,
`canBeDeleted()`, `canBeReassigned()`, `canBeApprovedBySupervisor()`,
`canBeRejectedBySupervisor()`, `isFinal()`) — never ad-hoc `if` chains in the
service. `CardStatusTest` pins the full transition matrix as a regression
test.

Key rules worth calling out:

- `PUT /cards/{id}` (`update`) always moves the card to `IN_PROGRESS` on a
  successful save — it's the "Save" step of a step-by-step fill-in flow, not
  a one-shot edit.
- `complete` is "Save and Complete" — moves `IN_PROGRESS`/`REJECTED` to
  `COMPLETED`, which is the only signal a supervisor needs (there is no
  separate "pending approval" endpoint — clients filter the generic list by
  `status=COMPLETED&assignedById=<self>`).
- A card can only be deleted before any real data exists
  (`NEW`/`ACCEPTED_BY_USER`/`REJECTED_BY_USER`) — never once work is in
  progress or later.
- Reassigning a card's attached users is **supervisor-only**
  (`PATCH /cards/{id}/supervisor/reassign`) and only valid from
  `REJECTED_BY_USER` — the endpoint checks the caller is the card's actual
  `assignedById`, not just "some authenticated user."

## Child-collection updates: `ChildCollectionSync`

Every card type's children (`vaccinations`, `riskFactors`, ...) are
`@OneToMany(cascade = ALL, orphanRemoval = true)` collections. An earlier
version replaced the whole collection on every `update()` — always building
brand-new entities from the request and calling `clear()` on the managed
list. Since a fresh entity's id is always `null`, Hibernate saw this as
"every existing row deleted, unrelated new rows inserted" on **every** save,
so a child's id kept climbing even when nothing had actually changed.

The fix (`application/handler/ChildCollectionSync.java`) is a generic,
id-matching upsert shared by all four handlers with child collections:

- Every child request DTO implements `ChildRequest` (`Long id()`).
- A request whose `id` matches an existing child updates **that exact
  managed instance** in place (its row survives the save).
- A request with no id (or an id matching nothing) is inserted as new.
- An existing child whose id is missing from the request list is dropped,
  letting `orphanRemoval` delete it.

**API contract implication**: to edit an existing child item, the client
must echo back the `id` it received from a prior GET/PUT response inside
that item. Omitting `id` (or sending `null`) always creates a new row.

## `@Embeddable` usage

Only introduced where two or more entities had a **genuinely identical**
field group (same names, types, column lengths) — not applied
speculatively. `AdministrativeAddress` (region/district/neighborhood/street/
houseNumber/apartmentNumber) is shared by CARD205's
`InformationOtherBittenPeople` and `InformationAboutAnimaBittenPeople`; it
lives in the `card205` package since those are its only two users today.
The request/response DTOs remain flat — MapStruct's nested `@Mapping(target
= "location.region", source = "region")` bridges the flat API shape to the
nested entity shape without changing the JSON contract.

## Legacy fixes (not carried over from `isemid`)

- `Card.users` had `cascade = CascadeType.ALL` — deleting a card deleted the
  assigned `User` rows. Removed.
- `Card.cardType` was freely settable and no builder exists on the
  hierarchy — a builder would have let a caller set the discriminator
  independently of the concrete subclass.
- `Card.form058` was nullable; now `optional = false` at both the Java and
  DB level.
- Every primitive `boolean`/`int` optional field (`hasLice`,
  `doseVolume`, ...) is now a boxed `Boolean`/`Integer`.
- `InfectionSourceDetail.card161_id` had no unique constraint despite being
  conceptually 1:1 — now enforced with `uk_card161_infection_source_detail`.
- Entities carry no Jackson annotations at all; polymorphism (`type`
  discriminator) lives only in `CardRequest`/`CardDetailResponse`.
- `card161.InfectionSource` and `card_tube.InfectionSource` collided on
  Hibernate's default (bare-class-name) entity name — both now have an
  explicit, distinct `@Entity(name = ...)`. Regression-guarded by
  `EntityNameUniquenessTest` (ArchUnit, no DB required).

## Testing notes

- MapStruct-generated mapper implementations (`Card161MapperImpl`, ...) are
  plain, dependency-free classes — tests instantiate and use them directly
  for real round trips instead of mocking them.
- Mockito's inline mock maker cannot mock the sealed `CardRequest`/
  `CardDetailResponse` interfaces. Tests that need a concrete instance
  construct a real permitted subclass (`Card175Request`, chosen for having
  the fewest fields and no child DTOs) rather than mocking.
- No Testcontainers/`@DataJpaTest` setup exists in this project — schema
  issues that only surface against a real Hibernate `SessionFactory` (like
  the entity-name collision above) won't be caught by the unit test suite;
  they were only found by running the application against Postgres.
