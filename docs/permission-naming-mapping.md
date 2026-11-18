# Permission naming — standard, target model and old → new mapping

Status: **proposal, nothing renamed yet.** Purpose: agree the target names with the frontend
before any migration is written. Mechanics of the current system:
[backend-guide.md §8.7a](backend-guide.md).

An authority is `PERMISSION_<SUBJECT>_<ACTION>` (built in SQL from `permission.subject` +
`action.code`; a role holding `MANAGE` is expanded to every action). Subject and action are both
visible to the frontend (`/v1/permissions`, role detail, permissions-me), so every rename below
is a **frontend contract change**.

## 1. Standard

| Rule | Example |
|---|---|
| Subject = one singular resource noun, `UPPER_SNAKE` | `REPORT`, `ACT`, `CARD`, `COUNTRY` |
| One resource ⇒ one subject. Variants are actions, never new subjects | `FORM058` + `CANCEL`, not `FORM058_CANCEL` |
| Never encode an action or a side in a subject | no `REFERENCE_COUNTRY_READ`, no `FORM058_SENDER` |
| **Sender-side and receiver-side rights are separate actions on the same subject** | `FORM058` `CREATE` vs `ACCEPT` |
| Form subjects keep the official form code (the integration bridge derives them from scope claims: `form058:submit` → `FORM058_SUBMIT`) | `FORM058`, `FORM0581`, `FORM129` |
| Actions are verbs, `UPPER_SNAKE`; the only qualified forms are `READ_SENT` and `READ_RECEIVED` | |

Legend — **Kind**: `rename` 1:1 · `merge` several old rows become one (action sets are unioned per
role) · `split` one old row becomes subject + action · `keep` · `decide` · `retire`.
**Enforced** = checked by `@PreAuthorize` today (a rename changes real access).

## 2. Two permission layers (important)

1. **Permission** (this document): may this *user* perform this *kind* of action at all.
2. **Instance rule** (already in code, unchanged by this work): is this *particular record*
   theirs — decided by the record's own data, e.g. `senderOrganizationId` /
   `receiverOrganizationId` on a form, the attached employee on a card/act, the assigning
   supervisor on a card. Validators such as `Form058AcceptValidator`,
   `CardCommandService#requireAttachedUserCard` do this.

A permission never replaces the instance rule: `ACCEPT` does not let an organization accept a
form it is not the receiver of.

### Super-admin and admin need no extra seed

`isemid_super_admin` holds every subject × every action (including `MANAGE`), and the `runAlways`
`zzz-rbac-grants` sync re-applies that on each start, so every new subject/action in this document
is granted to it automatically. `isemid_admin` gets the same **minus `DELETE` and `MANAGE`** (existing
rule, unchanged) — so admin will not hold `DELETE` on the new subjects either.
Super-admin is also open at the instance level: the form validators (058, 0581, 129: accept,
approve, cancel/reject, update, delete, reopen) and the card/act services (supervisor steps,
assign, **and** the attached-employee steps `requireAttachedUserCard` / `requireAttachedUserAct`,
which got the bypass with this work) all let a super-admin act on behalf of any side. Admin
(non-super) has no instance bypass: it still needs to be the right organization / attached user.

## 3. Forms 058 / 058-1 / 129 — sender and receiver are separate

Side is decided by the form (sender org / receiver org); the permission says whether the user may
act in that role.

| Side | Action | Form058 / Form0581 endpoint | Form129 endpoint |
|---|---|---|---|
| **Sender** | `CREATE` | `POST` | `POST` |
| | `UPDATE` | `PUT /{id}` | — (no update) |
| | `DELETE` | `DELETE /{id}` | stays admin-only (`isAdmin`), not a permission |
| | `APPROVE` | `PATCH /{id}/approve` (sender's final decision, after a card is linked) | — |
| | `CANCEL` | `PATCH /{id}/cancel` **as sender** (withdraw while `SENT`) | — (no sender withdraw) |
| | `READ_SENT` | list `direction=OUTGOING`; open/pdf of a form the user's org sent | same |
| **Receiver** | `ACCEPT` | `PATCH /{id}/accept` | `PATCH /{id}/accept` |
| | `REJECT` | `PATCH /{id}/cancel` **as receiver** (same endpoint as sender cancel) | `PATCH /{id}/reject` |
| | `READ_RECEIVED` | list `direction=INCOMING`; open/pdf of a form received | same |
| either | `READ_SENT` + `READ_RECEIVED` | `direction=ALL` needs both | same |
| neither | — | `reopen` is super-admin only (`requireSuperAdmin`), not a permission | — |

Notes:
- `PATCH /cancel` is one endpoint with two meanings; the service decides which permission applies
  from the form: caller's org == sender ⇒ needs `CANCEL`, == receiver ⇒ needs `REJECT`. This check
  lives in `Form058CancelValidator` / the 0581 equivalent, not in `@PreAuthorize` (side depends on
  the record).
- `export` needs `EXPORT` plus the direction's `READ_*`.
- `/affiliated` (form visible because the patient's workplace/study matches the caller's org,
  independent of sender/receiver) is covered by `READ_RECEIVED` — **decided**, no separate action.
- Same model for all three forms (Form129 has no `UPDATE`/`APPROVE`/`CANCEL`).

| Old | New |
|---|---|
| `FORM058_SENDER` (CREATE/UPDATE/MANAGE) | `FORM058` `CREATE`, `UPDATE`, `READ_SENT` |
| `FORM058_RECEIVER` (UPDATE/MANAGE) | `FORM058` `ACCEPT`, `REJECT`, `READ_RECEIVED` |
| `FORM0581` (CREATE/UPDATE/MANAGE, one table) | `FORM0581` split the same way as `FORM058` |
| `FORM129` (menu only) | `FORM129` per table above |
| `FORM058_COMPLETE_BY_SUPERVISOR` | `FORM058` `APPROVE` (approval belongs to the **sender**) |
| `RECEIVED` | `FORM058` `READ_RECEIVED` |
| `FORM058_CHANGE_ORGANIZATION` | `FORM058` `CHANGE_ORG` |
| `FORM058_ATTACHED_CARDS` | `CARD` `ASSIGN` (assign cards and employees to a form) |

Existing grants are migrated per role: a role that had `FORM058_SENDER` keeps the sender actions,
`FORM058_RECEIVER` the receiver ones, so nobody gains or loses a side.

## 4. Cards and acts — own vs everything

Verified against the code and your description. Both already behave as described:

| Who | Cards | Acts |
|---|---|---|
| Attached employee | `GET /cards/mine` — own cards only | `GET /acts/mine` — own acts only |
| Admin, super-admin, department head (zaveduyushiy) | `GET /cards` — every card in the caller's organization scope | `GET /acts` — every act in scope |

"See all" is the `VIEW_ALL` action, granted to `isemid_epidim_head` and (via the runAlways
sync) `isemid_admin` / `isemid_super_admin`. `mine` never widens, not even for region/republic
organizations.

### CARD (`ATTACHED_CARDS` → `CARD`)

Today **only `GET /cards` is permission-gated** (`ATTACHED_CARDS_VIEW_ALL`). Every other card
endpoint is `isAuthenticated()`; who may do what is enforced by instance rules
(attached employee / assigning supervisor / super-admin bypass) in `CardCommandService`.

| Action | Endpoints | Side |
|---|---|---|
| `VIEW_ALL` | `GET /cards` | head / admin |
| `READ` | `GET /cards/mine`, `/cards/{id}`, `/cards/{id}/pdf`, `GET /form-058/{id}/cards` | employee |
| `ASSIGN` | `POST /form-058/{id}/cards/assign`, `PATCH …/supervisor-reassign` | supervisor |
| `UPDATE` | `PUT /cards/{id}` | attached employee |
| `DELETE` | `DELETE /cards/{id}` | |
| `ACCEPT`, `REJECT` | `PATCH …/accept`, `…/reject` | attached employee |
| `COMPLETE` | `PATCH …/complete` | attached employee |
| `APPROVE` | `PATCH …/supervisor-approve` | assigning supervisor |
| `RETURN` | `PATCH …/supervisor-reject` (sends back for rework) | assigning supervisor |

(`RETURN` rather than a second `REJECT`, so employee `REJECT` and supervisor send-back stay
distinct — **decided**.) `Api2LookupAccessGuard` (citizen / legal-entity lookups while
filling a card) moves from `ATTACHED_CARDS` `CREATE/UPDATE` to `CARD` `UPDATE`/`ASSIGN`.

**Behaviour change to schedule deliberately:** gating the currently open card endpoints means roles
need grants first. The migration seeds them to mirror today's behaviour (employees: `READ`,
`UPDATE`, `ACCEPT`, `REJECT`, `COMPLETE`; heads: + `ASSIGN`, `APPROVE`, `RETURN`, `VIEW_ALL`);
the gates should be switched on only after that seed has run.

### ACT (`ATTACH_ACT` → `ACT`)

Already fully gated; actions keep their names, only the subject is renamed.

| Action | Endpoints |
|---|---|
| `VIEW_ALL` | `GET /acts` |
| `READ` | `GET /acts/mine`, `/acts/{id}`, `/acts/{id}/pdf`, `GET /cards/{id}/acts` |
| `ASSIGN` | `POST /cards/{id}/acts` |
| `UPDATE` | `PUT /acts/{id}`, `…/ready`, `…/send-to-lis`, `…/close` |
| `DELETE` | `DELETE /acts/{id}` |

`/acts/lis/callback` stays `isAuthenticated()` (machine call). `ACTS` (menu-only) merges into `ACT`.
Optional later: split `UPDATE` into `UPDATE` / `SEND` / `CLOSE` if the lab and the doctor must be
granted separately.

**Cross-check finding (act) — decided: keep as is.** `GET /acts/mine` lists only attached acts, but
`GET /acts/{id}` (`ActQueryService#visibleById`) opens *any act in the caller's organization scope
**or** one the caller is attached to*. So an employee who is not attached can open another act of
their own organization by id even though it is not in their list. This stays; it is documented so
the frontend does not assume "by-id = mine".

## 5. Organization and User

| Subject | Action | Endpoint |
|---|---|---|
| `ORGANIZATION` | `READ` | get, hierarchy, lookup, users-by-organization |
| | `SYNC` | `POST …/sync/{uuid}` (today super-admin only) |
| `USER` | `READ`, `UPDATE`, `SYNC` | user list/detail, edit, sync |

`STAFF` and `EMPLOYEES` (both "Xodimlar") merge into `USER`; `ORGANIZATIONS` → `ORGANIZATION`.
`SYNC` is its own action so "may read, may not sync" can be granted.

## 6. Reports — 3 groups (decided: only because it keeps things simple)

One subject per form (~20) is unmanageable and one subject for everything cannot tell reading
from entering data, so three subjects, each with an obvious meaning for the user:

| Subject | What the user sees in the role editor | Contains | Actions |
|---|---|---|---|
| `REPORT` | "view reports" | all statistical forms (1, 4, 6, 8, 9, 10, 11, 12, 13, 28.1, 28.2, statistics) **and** forecast, air-health, map, air-quality | `READ`, `EXPORT` |
| `REPORT_ENTRY` | "enter reports manually" | manually entered: form 2 (manual), 31, 32, 7 | `READ`, `CREATE`, `UPDATE`, `DELETE` |
| `ANALYTIC_REPORT` | "build own report" | the caller's own report builder | `READ`, `CREATE`, `UPDATE`, `DELETE`, `EXPORT` |

- `EXPORT` is **separate from `READ`** (decided): export is bulk data out. Migration grants
  `EXPORT` to every role that has `READ` today, so nobody notices a change.
- The analytic builder currently enforces `REPORTS_*`, not `ANALYTICAL_REPORT`; after this it
  enforces `ANALYTIC_REPORT` — the subject that always existed for it.
- Migration from `REPORTS`: `READ` → `REPORT` `READ`+`EXPORT`; `CREATE/UPDATE/DELETE` → `REPORT_ENTRY`
  and `ANALYTIC_REPORT`.
- A per-form right (`REPORT_FORM12`) is added only when a real requirement appears. Legacy `FORM1`,
  `FORM12`, `GRAPH1`, `MAP_GRAPHIC` fold into `REPORT`.

## 7. Actions

| Old | New | Kind |
|---|---|---|
| `READ` `CREATE` `UPDATE` `DELETE` `MANAGE` `EXPORT` `APPROVE` `ASSIGN` `VIEW_ALL` | same | keep |
| `CARD_ATTACH` | `ASSIGN` (on `CARD`) | merge — attaching cards/employees to a received form is `CARD` `ASSIGN` |
| `ORG_CHANGE` | `CHANGE_ORG` | rename |
| `FORM_CANCEL` | `CANCEL` | rename |
| `IMPORT` | — | retire (never used) |
| — | `ACCEPT`, `REJECT`, `COMPLETE`, `RETURN`, `SYNC`, `READ_SENT`, `READ_RECEIVED` | new |
| — | `SUBMIT`, `CONFIRM` | new — the inbound-integration bridge (`IntegrationScope#permissionAuthority`) needs `FORM058_SUBMIT` etc.; they are not seeded today |

## 8. Everything else (menu-only subjects, backend does not check them)

| Old | New | Kind |
|---|---|---|
| `ALL` | `ALL` | keep (wildcard; front reads it as "show everything") |
| `ROLES` / `PERMISSIONS` | `ROLE` / `PERMISSION` | rename |
| `MKB_LIST` | `ICD10` | rename (matches `Icd10Controller`) |
| *(reference resources — see the table below)* | | |
| `CATALOGS`, `FORM_CATEGORIES`, `SICK_PATIENT_CATEGORIES`, `DISEASE_PLACES`, `POPULATION_TYPES` | `CATALOG` | merge (all are `ref_catalog` types: `FORM_CATEGORY`, `SICK_PATIENT_CATEGORY`, `DISEASE_PLACE`, `POPULATION_TYPE` = urban/rural) |
| `MANUAL_REPORT` | `MANUAL_REPORT` | keep |
| `USER_INCOMINGS` | `CARD` `READ` | merge (employee's own cards) |
| `CARD_FULL_INFO` | `CARD` `READ` | merge — referenced nowhere in the backend (legacy menu subject, "additional card information"); `GET /cards/{id}` already returns the full card to anyone who can read it, so hiding that block was front-side only |
| `SPEECH_TO_TEXT` | `SPEECH_TO_TEXT` | keep |

### 8.1 Reference data ("Ma'lumotnomalar")

The backend has these reference resources (`modules/reference/web`): country, region, district,
neighborhood, population, catalog, icd10, manual-report. The legacy address subjects point at them:

| Legacy subject(s) | New subject | Notes |
|---|---|---|
| `COUNTRIES`, `REFERENCE_COUNTRY_READ/MANAGE` | `COUNTRY` | merge + split |
| `STATES`, `REFERENCE_REGION_READ/MANAGE` | `REGION` | merge + split |
| `CITIES`, `REFERENCE_DISTRICT_READ/MANAGE` | `DISTRICT` | merge + split ("tuman/shahar") |
| `STREETS`, `RESIDENCES` | `DISTRICT` | merge — the backend has no street/residence catalog; they are address levels under region/district. Reference reads are open to every authenticated user and writes are admin-only, so folding them in grants nothing new. |
| `NEIGHBORHOODS`, `REFERENCE_NEIGHBORHOOD_READ/MANAGE` | `NEIGHBORHOOD` | merge + split |
| *(none today — `PopulationController` is super-admin-only)* | `POPULATION` | **new**: the permanent-population reference (`ref_population`); `READ`, `UPDATE`, `DELETE`, `SYNC` |
| `CATALOGS` + the four catalog-type subjects above | `CATALOG` | per §8 |
| `MKB_LIST` | `ICD10` | rename |
| `MANUAL_REPORT` | `MANUAL_REPORT` | keep |

Actions for every reference subject: `READ`, `CREATE`, `UPDATE`, `DELETE`, plus `SYNC` where an
external sync exists (`DISTRICT`, `NEIGHBORHOOD` geosync from cs.egov.uz, `POPULATION` from stat.uz).
`MANAGE` = all of them (expanded in SQL). Today these writes are `isAdmin()` /
`isSuperAdmin()`; moving them to permissions is the enforcement-gap item in §9, not part of the
rename.

### 8.2 Default grants per role

Roles in the seed: `isemid_general_practitioner` (UASH), `isemid_epidemiologist`,
`isemid_assistant_epidemiologist`, `isemid_epidim_head` (department head / zaveduyushiy),
`isemid_head_doctor`, `isemid_qr_user`; `isemid_admin` / `isemid_super_admin` are covered under
"Super-admin and admin need no extra seed" above.

**Workflow (confirmed):** the UASH creates and sends a form → the **department head** receives and
accepts it, attaches the cards and assigns the epidemiologist / assistant epidemiologist → the
cards then appear in those employees' `mine` lists and they work them → the head supervises
(approve / return).

**Translation rule applied to existing grants** (so a role keeps exactly the side it had):

| Old | New |
|---|---|
| `FORM058_SENDER` / `FORM0581`: `READ` / `CREATE` / `UPDATE` | `READ_SENT` / `CREATE` / `UPDATE` |
| `FORM058_RECEIVER`: `READ` / `UPDATE` / `FORM_CANCEL` / `ORG_CHANGE` | `READ_RECEIVED` / `ACCEPT` / `REJECT` / `CHANGE_ORG` |
| `FORM058_RECEIVER`: `CARD_ATTACH` | `CARD` `ASSIGN` |
| `ATTACHED_CARDS` `READ`/`UPDATE`/`APPROVE`/`VIEW_ALL` | `CARD` same (`CREATE` is copied as-is by the migration but nothing checks it any more; cards are created via form `assign-cards`, gated by `CARD` `ASSIGN`) |
| `ATTACH_ACT` | `ACT` same actions |
| `REPORTS` `READ` + `EXPORT` | `REPORT` `READ` + `EXPORT` |
| `REPORTS` `CREATE`/`UPDATE`/`DELETE` | `REPORT_ENTRY` + `ANALYTIC_REPORT` same actions |
| any `MANAGE` | every action of that subject/side |

**Default matrix** (what a fresh DB seeds; the live DB is migrated with the rule above instead).
`+` = not in today's seed but needed once the currently open endpoints are gated (§4, §10), because
today the endpoint works for everyone and gating must not remove it.

| Role | Forms 058 / 058-1 | Card | Act | Reports | Other |
|---|---|---|---|---|---|
| **General practitioner (UASH)** — creates and sends | `CREATE`, `READ_SENT`, `UPDATE`, `DELETE`, `CANCEL`, `APPROVE` (when each is allowed is decided by the form status in code — see below) | — | — | — | `SPEECH_TO_TEXT` `CREATE` |
| **Epidemiologist** | `READ_RECEIVED` | `READ`, `UPDATE`, `ACCEPT`+, `REJECT`+, `COMPLETE`+ | `READ`, `UPDATE`, `ASSIGN` | `REPORT` `READ`,`EXPORT`; `REPORT_ENTRY` `READ`,`CREATE`,`UPDATE` | `SPEECH_TO_TEXT` `CREATE` |
| **Assistant epidemiologist** | `READ_RECEIVED` | `READ`, `UPDATE`, `ACCEPT`+, `REJECT`+, `COMPLETE`+ | `READ`, `UPDATE`, `ASSIGN` | — | `SPEECH_TO_TEXT` `CREATE` |
| **Department head (epidim_head)** — receives, assigns, supervises | `READ_RECEIVED`, `ACCEPT`+, `REJECT`, `CHANGE_ORG` | `READ`, `UPDATE`, `DELETE`+, `APPROVE`, `VIEW_ALL`, `ASSIGN`, `RETURN`+ | `READ`, `VIEW_ALL`, `ASSIGN`, `UPDATE` | `REPORT` `READ`,`EXPORT`; `REPORT_ENTRY` `READ`,`UPDATE` | `SPEECH_TO_TEXT` `CREATE` |
| **Head doctor** — view only | `READ_SENT`, `READ_RECEIVED` | — | — | `REPORT` `READ`,`EXPORT` | — |
| **QR user** | — | — | — | — | not used for now (no grants) |

Note the change against the old seed: the assistant no longer holds the receiver `UPDATE`
(= `ACCEPT`); acceptance belongs to the department head.

**UASH and the form status (decided: the status rules in code stay exactly as they are).** The
permission only says the UASH may act as sender; *when* is the form status, already enforced by the
validators:

| Sender action | Allowed while | Source |
|---|---|---|
| `CANCEL` (withdraw) | `SENT` only | `FormStatus#isCancellable` |
| `UPDATE` | until `APPROVED` / `CANCELED` (so also `ACCEPTED`, `CARD_LINKED`) | `Form058#ensureEditable` |
| `DELETE` | `SENT`, `ACCEPTED` (removes a duplicate the receiver already accepted); locked from `CARD_LINKED` | `FormStatus#physicallyDeletable` |
| `APPROVE` (final decision) | `ACCEPTED` or `CARD_LINKED` — i.e. after the head accepts / a card is linked | `FormStatus#isApprovable` |

So the UASH keeps `APPROVE` as the sender's right; approval does not move to the department head.

## 9. Decisions taken and what is left

Taken: sender/receiver separate on all three forms; `CANCEL`/`REJECT` split; `/affiliated` under
`READ_RECEIVED`; card `RETURN`; act by-id unchanged; 3 report groups with `EXPORT` split;
`UASH` keeps `UPDATE`/`DELETE`/`CANCEL`/`APPROVE` with the existing status rules unchanged;
`CARD_FULL_INFO` → `CARD` `READ`; `CITIES`/`STREETS`/`RESIDENCES` map to the reference
`DISTRICT` (§8.1); population gets its own `POPULATION` subject.

To confirm:
1. Frontend: confirm `CARD_FULL_INFO`, `CITIES`/`STREETS`/`RESIDENCES` and `POPULATION_TYPES` mappings
   (§8, §8.1) and that the population reference is the new `POPULATION` subject.
2. Enforcement (done 2026-11-18): card and form058/0581/129 endpoints are gated; reference writes accept
   `isAdmin()` OR `CREATE`/`UPDATE`/`DELETE`; `SYNC` (district, neighborhood, population, organization)
   accepts `isSuperAdmin()` OR `SYNC`; population all actions `isSuperAdmin()` OR the permission.
   Reference/organization/user reads stay open (lookups). The runAlways sync also hands `SYNC` to `isemid_admin`.
3. Before migrating: dump the real `role_permissions` from the live DB and diff it against §8.2.

## 10. Safe rollout (no big bang)

1. **Expand** — Liquibase adds the new subjects/actions and copies every `role_permissions` row
   (actions unioned where several old rows merge; unique key is `(role_id, permission_id)`). Old
   rows stay active. Role seeds for newly gated endpoints (cards, forms) run here.
2. **Dual-emit** — the authority SQL emits old and new names (small old→new `VALUES` map), so
   guards and the frontend move independently. `Permissions` constants switch to the new names;
   `PermissionCatalogValidator` confirms they exist.
3. **Gate** — turn on the new `@PreAuthorize` / validator checks (cards, forms) once the seed is in.
4. **Frontend migrates** (checklist below).
5. **Contract** — soft-delete old subjects/actions, drop the old→new map, update the runAlways
   `zzz-rbac-grants` file (it re-creates `REPORTS`/`FORM129`/`ACTS`/`CATALOGS`/`ANALYTICAL_REPORT`
   every start, so it **must** change in the same release or old names return as duplicates).

The live DB differs from the seeds (≈40 subjects, hand-edited descriptions, admin-created rows):
every statement must be `WHERE subject = '<old>'`. Never edit an existing changeset — add new ones
(Liquibase checksums).

## 11. Frontend checklist

- Collect every place subject/action strings are read (menus, route guards, buttons); use §3–§8 to
  produce the exact replace list.
- Keep names in one constants file.
- Treat `MANAGE` as "all actions" (the backend already expands it).
- Sender screens: `CREATE/UPDATE/DELETE/APPROVE/CANCEL/READ_SENT`; receiver screens:
  `ACCEPT/REJECT/READ_RECEIVED`. A user may hold either side or both.
- Card/act: "my" tab needs `READ`; "all" tab needs `VIEW_ALL`. The card "full info" block is shown with `READ`.
- A subject is only enforced if listed in §3–§6; anything in §8 changes menus only.
- Inactive/deleted subjects produce no authority — do not rely on their presence.
