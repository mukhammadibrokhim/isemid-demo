# «Statistika» report — frontend integration guide

Geography‑first surveillance **statistics** report — rows are territories
(republic→region→district→organization), same drill‑down shape as every
other report (`report.shared.ReportHierarchyService`). Unlike the
disease‑first reports (Form 12/13/28.1/28.2), there is **no** disease
dimension here at all — each row carries confirmed/primary case totals, an
age cut, a gender cut, and a **social‑category breakdown driven live off the
`ref_catalog(type = 'CATEGORY')` reference catalog** (`Ma'lumotnomalar →
Katalog`, type `CATEGORY`) — adding/renaming a category entry there changes
this report's columns with no backend deploy.

Unlike the dashboard's single‑snapshot widgets (`/v1/dashboard/**`), this is
the **broader, drill‑down + period‑comparison** version: full
republic→region→district→organization geography, and — the key difference
from every other report — it compares **two entirely independent,
freely‑chosen periods** ("Davr A" / "Davr B"), not just "this year vs a year
ago."

| | |
|---|---|
| Title | «Statistika» |
| Base path | `/v1/reports/statistics` |
| Swagger tag | `Report — Statistika` |

---

## 1. What the numbers mean

Every count is over `form058` + `form058_1` (unioned), `deleted = false`,
`sender_organization_id` inside the caller's access scope (and, when
drilling down, inside the requested region/district).

Every row splits into two independent buckets:

- **`confirmed*`** — «Tasdiqlangan»: `status = 'APPROVED'`
- **`primary*`** — «Tasdiqlanmagan» (birlamchi/hali qaror qilinmagan):
  `status not in ('APPROVED', 'CANCELED')`

`CANCELED` (bekor qilingan/rad etilgan) cases are excluded from **both**
buckets — same CONFIRMED/PRIMARY split "Form 1" uses.

Age is **complete years at `created_at`** (not "as of today"), cut at 18.

There is **no disease/ICD‑10** dimension — diagnosis plays no role in this
report at all.

### Cards and acts

Each period also carries **card** and **act** counts for the same
geography node — the only report that reads these two modules directly:

- **Cards** — every row of any of the 5 epidemiological card types whose
  owning `form058`/`form058_1` case is in scope, bucketed by the card's own
  `CardStatus` (`NEW`, `IN_PROGRESS`, `COMPLETED`, `ACCEPTED_BY_USER`,
  `REJECTED_BY_USER`, `APPROVED`, `REJECTED`). Filtered by the **card's own**
  `created_at` (not the case's).
- **Acts** — every act whose owning card is in scope, bucketed by
  `ActStatus` (`NEW`, `IN_PROGRESS`, `READY`, `SENT`, `SEND_FAILED`,
  `RETURNED_BY_LIS`, `COMPLETED`). Filtered by the **act's own** `created_at`.

Cards/acts are **not** split by confirmed/primary or by category — they get
one flat status breakdown per period, independent of the form058/form058_1
dimensions above.

### Two periods, not year‑over‑year

Every number is computed **twice** — once for **Davr A** (`fromA`/`toA`,
always resolved, defaults to whole history if both omitted) and, if
requested, once for **Davr B** (`fromB`/`toB`, entirely optional). The two
periods are **completely independent** — pick March vs April, this quarter
vs last quarter, or any two arbitrary ranges; this is *not* the fixed
"current year / same period a year ago" comparison the other reports
(Form 6/8/9/10/11) use.

If `fromB`/`toB` are both omitted, `periodB` is `null` in every row — a
plain single‑period report.

---

## 2. Access

```
isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')
```

Same as every other report. A caller without the permission gets `403`.

| Caller scope | `root` returns | drill‑down levels |
|---|---|---|
| Republican / ALL | regions + `Jami` | region → district → organization |
| Region | districts + `Jami` | district → organization |
| District | organizations + `Jami` | (none below organization) |
| Single organization | that org's own totals only, no `Jami` | none (`hasChildren = false`) |

**Organization is the lowest level** — every row's statistics (confirmed/primary
totals, age, gender, categories, cards, acts) drill all the way down to a single
organization (MTM); `hasChildren = false` at that level means there is nothing
finer‑grained to expand, not that the drill‑down stopped early.

---

## 3. Response envelope

```json
{ "success": true, "message": "...", "data": [ /* rows */ ] }
```

`data` is a flat array of row objects — one call already shows the first
level plus totals; the frontend renders the tree itself (see §6).

---

## 4. `GET /v1/reports/statistics/root`

| Query param | Type | Notes |
|---|---|---|
| `fromA` | `YYYY-MM-DD` | Davr A start, inclusive. Optional (defaults to whole history if `toA` also omitted). |
| `toA` | `YYYY-MM-DD` | Davr A end, inclusive. Optional. |
| `fromB` | `YYYY-MM-DD` | Davr B start, inclusive. Optional — omit both `fromB`/`toB` for no comparison. |
| `toB` | `YYYY-MM-DD` | Davr B end, inclusive. Optional. |

Returns the caller's whole access scope broken down **one level**, plus a
trailing **`Jami`** row summing the whole scope — for **both** periods.

### Row shape

| Field | Meaning |
|---|---|
| `code` | Region/district code, organization id, `"UZ"` for the republic root, or `"TOTAL"` for `Jami`. |
| `name` | Localized geography node name (`"Jami"` for the total row). |
| `hasChildren` | Show the expand arrow when `true`. `Jami` is always `false`. |
| `periodA` | Numbers for Davr A — see below. Always present. |
| `periodB` | Numbers for Davr B — same shape as `periodA`. **`null`** if `fromB`/`toB` were not passed. |

`periodA` / `periodB` shape (`MonitoringPeriodCountsResponse`):

| Field | Meaning |
|---|---|
| `confirmedTotal` | «Tasdiqlangan» — all confirmed cases in this node for this period, **regardless of patient category**. |
| `primaryTotal` | «Tasdiqlanmagan» — all primary/not‑yet‑decided cases, regardless of category. |
| `ageBreakdown` | `{ confirmedUnder18, confirmedAdult, primaryUnder18, primaryAdult }` |
| `genderBreakdown` | `{ confirmedFemale, confirmedMale, primaryFemale, primaryMale }` |
| `categories` | Array of category cells, **one per `ref_catalog(type = CATEGORY)` entry, in a stable order (sorted by `nameUz`) identical across every row and both periods** — safe to render as fixed columns/series. Each cell: `{ code, name, confirmedTotal, primaryTotal }`. |
| `cardsTotal` | Total cards (any of the 5 card types) in this node for this period. |
| `cardsByStatus` | One cell per `CardStatus` value, **always all 7, zero‑filled** — `{ status, count }`, same order every row/period. |
| `actsTotal` | Total acts in this node for this period. |
| `actsByStatus` | One cell per `ActStatus` value, **always all 7, zero‑filled** — `{ status, count }`. |

Note: `confirmedTotal`/`primaryTotal` at the **period level** count every
case in scope, including patients with no recognized category — summing
`categories[].confirmedTotal` can therefore be **less than** the period's
own `confirmedTotal` when some patients have no `category_code` set. This
is intentional: the period totals answer "how many cases," the category
cells answer "of the ones with a known category, which kind."

### Example — with comparison (Davr A vs Davr B)

```
GET /v1/reports/statistics/root?fromA=2026-08-01&toA=2026-08-31&fromB=2026-07-01&toB=2026-07-31
```
```json
{
  "success": true,
  "message": "Amal muvaffaqiyatli bajarildi.",
  "data": [
    {
      "code": "1703", "name": "Andijon viloyati", "hasChildren": true,
      "periodA": {
        "confirmedTotal": 210, "primaryTotal": 40,
        "ageBreakdown": { "confirmedUnder18": 60, "confirmedAdult": 150, "primaryUnder18": 12, "primaryAdult": 28 },
        "genderBreakdown": { "confirmedFemale": 100, "confirmedMale": 110, "primaryFemale": 18, "primaryMale": 22 },
        "categories": [
          { "code": "NO_ORGANIZED", "name": "Uyushmagan", "confirmedTotal": 20, "primaryTotal": 3 },
          { "code": "STUDENT_SCHOOL", "name": "Maktab o'quvchisi", "confirmedTotal": 35, "primaryTotal": 8 }
        ],
        "cardsTotal": 95, "cardsByStatus": [
          { "status": "NEW", "count": 10 }, { "status": "IN_PROGRESS", "count": 22 },
          { "status": "COMPLETED", "count": 18 }, { "status": "ACCEPTED_BY_USER", "count": 5 },
          { "status": "REJECTED_BY_USER", "count": 2 }, { "status": "APPROVED", "count": 35 },
          { "status": "REJECTED", "count": 3 }
        ],
        "actsTotal": 40, "actsByStatus": [
          { "status": "NEW", "count": 4 }, { "status": "IN_PROGRESS", "count": 6 },
          { "status": "READY", "count": 3 }, { "status": "SENT", "count": 20 },
          { "status": "SEND_FAILED", "count": 1 }, { "status": "RETURNED_BY_LIS", "count": 2 },
          { "status": "COMPLETED", "count": 4 }
        ]
      },
      "periodB": {
        "confirmedTotal": 185, "primaryTotal": 33,
        "ageBreakdown": { "confirmedUnder18": 50, "confirmedAdult": 135, "primaryUnder18": 9, "primaryAdult": 24 },
        "genderBreakdown": { "confirmedFemale": 90, "confirmedMale": 95, "primaryFemale": 15, "primaryMale": 18 },
        "categories": [
          { "code": "NO_ORGANIZED", "name": "Uyushmagan", "confirmedTotal": 17, "primaryTotal": 2 },
          { "code": "STUDENT_SCHOOL", "name": "Maktab o'quvchisi", "confirmedTotal": 30, "primaryTotal": 6 }
        ],
        "cardsTotal": 80, "cardsByStatus": [ "...": "same 7-status shape" ],
        "actsTotal": 34, "actsByStatus": [ "...": "same 7-status shape" ]
      }
    },
    {
      "code": "TOTAL", "name": "Jami", "hasChildren": false,
      "periodA": { "confirmedTotal": 1280, "primaryTotal": 340, "...": "..." },
      "periodB": { "confirmedTotal": 1145, "primaryTotal": 298, "...": "..." }
    }
  ]
}
```

### Example — no comparison (`fromB`/`toB` omitted)

```
GET /v1/reports/statistics/root?fromA=2026-01-01&toA=2026-09-04
```
```json
{
  "data": [
    { "code": "1703", "name": "Andijon viloyati", "hasChildren": true,
      "periodA": { "confirmedTotal": 210, "...": "..." },
      "periodB": null }
  ]
}
```

---

## 5. `GET /v1/reports/statistics/children`

| Query param | Type | Notes |
|---|---|---|
| `regionCode` | string | Optional. Omit for the caller's own first level. |
| `districtCode` | string | Optional. |
| `fromA` / `toA` | `YYYY-MM-DD` | Same as `root`. |
| `fromB` / `toB` | `YYYY-MM-DD` | Same as `root` — omit both for no comparison. |

| Call | Returns |
|---|---|
| *(no region/district params)* | The caller's own first level — same as `root` minus the `Jami` row. |
| `regionCode=1703` | Districts of that region. |
| `districtCode=1703215` | Organizations (MTM) of that district. |

- A region/district‑scoped caller skips the levels above them — just call
  `children` with no region/district params and you get your own first
  level.
- `regionCode` / `districtCode` outside the caller's scope → `403`
  (`organization.scope_violation`).
- Row shape is identical to §4 minus the `Jami` row (never present on
  `children`).

```
GET /v1/reports/statistics/children?regionCode=1703&fromA=2026-08-01&toA=2026-08-31&fromB=2026-07-01&toB=2026-07-31
```
```json
{
  "success": true, "message": "...",
  "data": [
    { "code": "1703215", "name": "Andijon shahri", "hasChildren": true,
      "periodA": { "confirmedTotal": 40, "primaryTotal": 9, "...": "..." },
      "periodB": { "confirmedTotal": 35, "primaryTotal": 7, "...": "..." } }
  ]
}
```

---

## 6. Suggested UI flow

1. Filter bar: **two date‑range pickers** — "Davr A" (required) and "Davr B"
   (optional, with a toggle/checkbox "solishtirish" to show/hide it — when
   hidden, just don't send `fromB`/`toB`). Region/district selectors are
   optional/cosmetic, feeding `regionCode`/`districtCode` into a `children`
   call exactly like expanding that row would.
2. On load / filter change → `GET /root?fromA&toA[&fromB&toB]`. Render every
   row with an expand chevron where `hasChildren`; pin `Jami` last. One call
   already shows the first level plus totals for both periods.
3. On expand of a region row → `GET /children?regionCode={row.code}&fromA&toA[&fromB&toB]`;
   on a district row → `+ &districtCode={row.code}`.
4. Build the widgets straight from each row, no extra client‑side
   aggregation needed — read from `periodA` (and `periodB` when present,
   e.g. side‑by‑side bars or a delta column computed client‑side as
   `periodB.x - periodA.x`):
   - **Tasdiqlangan / Tasdiqlanmagan** summary — `confirmedTotal` /
     `primaryTotal`.
   - **Yosh bo'yicha** bar/pie (with a Tasdiqlangan/Tasdiqlanmagan toggle) —
     `ageBreakdown.confirmed*` or `ageBreakdown.primary*`.
   - **Jinsi bo'yicha** — same, from `genderBreakdown`.
   - **Ijtimoiy toifa bo'yicha** stacked/grouped bar — one series/bar per
     `categories[]` entry, using the **first row's** `categories` order as
     the fixed legend (identical order in every row and both periods).
   - **Kartalar / Aktlar bo'yicha** — `cardsTotal`/`actsTotal` for a summary
     tile, or `cardsByStatus`/`actsByStatus` for a status‑breakdown bar
     (workflow funnel: NEW → IN_PROGRESS → ... → APPROVED/REJECTED for
     cards, NEW → ... → COMPLETED for acts).
5. A hudud‑taqqoslash chart (bar per region/district) reads straight off the
   array `root`/`children` returns — `name` as the category axis,
   `periodA.confirmedTotal` (and `periodB.confirmedTotal` as a second
   series, when comparing) as the value axis.
6. Excel export is built client‑side from the row array; there is no server
   export endpoint for this report.

---

## 7. Edge cases

- Empty period / no matching cases → rows still returned, all counts `0`.
- A patient with no `category_code` set contributes to the period's
  `confirmedTotal`/`primaryTotal` and to `ageBreakdown`/`genderBreakdown`,
  but to **no** `categories[]` cell — see the note in §4.
- A `ref_catalog` `CATEGORY` entry that is soft‑deleted stops appearing in
  `categories[]` on the **next** call (the column list is read live, not
  cached) — historical counts for that category simply disappear from the
  breakdown, though the period's own `confirmedTotal`/`primaryTotal` are
  unaffected (they never depended on the category list). This applies
  identically to `periodA` and `periodB` — both use the *current* catalog,
  even if `periodB` refers to a past date.
- Only `fromB` **or** only `toB` passed (not both) still counts as
  "comparison requested" — the missing side defaults to today, same as
  `fromA`/`toA`'s own defaulting rule.
- Single‑organization caller: `root` returns just one data row (their own
  totals) with `hasChildren = false`, no `Jami`, and `children` returns `[]`.
- `cardsByStatus`/`actsByStatus` always list **all** 7 statuses each, even
  when the count is `0` — no status is ever omitted, so a fixed‑column
  status chart never needs to guard for a missing entry.
- A node with cases but no cards yet (e.g. a case still `SENT`, not yet
  investigated) shows `cardsTotal: 0` and, consequently, `actsTotal: 0` —
  acts always require a card.
