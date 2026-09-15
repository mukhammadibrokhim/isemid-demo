# Population reference — frontend integration guide

Covers the **«Aholi soni» / «Численность постоянного населения»** reference
(`ref_population`) under **Ma'lumotnomalar**. One record = permanent
population of one MHOBT/SOATO territory for one calendar year, sourced from
the stat.uz SDMX feed (dataset 246) and refreshable from it with one button.
It is the per-capita denominator the statistical reports will use.

For exact request/response field lists use the generated Swagger UI
(**"Reference - Population"** tag) — every DTO field carries a `@Schema`
description. This guide covers the screen flow and the parts that aren't
obvious from the schema.

## Access — super admin only

**Every** endpoint below requires the `isemid_super_admin` authority. A
plain `isemid_admin` or a regular user gets `403 FORBIDDEN`. Show the
"Aholi soni" menu item only to super admins.

(Reports read this data server-side through the repository, not through this
API, so locking the screen to super admin does not affect report figures.)

## Base path

```
/v1/references/populations
```

## Response envelope

Standard wrapper, same as every module. Success:

```json
{ "success": true, "message": "Amal muvaffaqiyatli bajarildi.", "data": { } }
```

Error:

```json
{
  "success": false,
  "code": "CONFLICT",
  "message": "1703 hududi va 2025 yil uchun aholi soni yozuvi allaqachon mavjud.",
  "traceId": "9f1c2a3b4d",
  "path": "/v1/references/populations",
  "timestamp": "2026-09-03T12:00:00Z",
  "violations": []
}
```

| `code` | HTTP | When |
|---|---|---|
| `VALIDATION_FAILED` | 400 | Bad body — missing `geoType`/`soatoId`/`year`/`population`, negative population, year out of 1900–2100. `violations[]` lists the fields. |
| `FORBIDDEN` | 403 | Caller is not `isemid_super_admin`. |
| `NOT_FOUND` | 404 | No record with that `id` (or it is soft-deleted). |
| `CONFLICT` | 409 | `create` with a `(soatoId, year)` that already exists; `update` on a soft-deleted record. |
| `UPSTREAM_ERROR` | 502 | `/sync` — the stat.uz feed could not be fetched or parsed. The table is left untouched; let the user retry. |

## Data model — what a record means

| Field | Notes |
|---|---|
| `id` | Row id. Identifies **one territory in one year** — not the territory as a whole. |
| `geoType` | `REPUBLIC` \| `REGION` \| `DISTRICT` \| `OTHER`. `OTHER` = a SOATO territory in the feed with no active `ref_region`/`ref_district` match yet (currently only Ko'kdala tumani, `1710240`). |
| `soatoId` | MHOBT/SOATO code. `1700` = republic, 4-digit = region, 7-digit = district. |
| `regionCode` / `districtCode` | Resolved `ref_region.code` / `ref_district.code`. `null` for `REPUBLIC` and `OTHER`. |
| `year` | Calendar year. |
| `population` | **Absolute head-count** (persons), e.g. `37543200`. The feed publishes "thousand people" — the backend already multiplied by 1000. Format with thousands separators; do **not** divide. |
| `source` | `SDMX` (from the feed / seed) or `MANUAL` (hand-entered). `/sync` never overwrites a `MANUAL` row. |
| `name` (responses only) | Territory name **in the caller's locale**, resolved server-side from `ref_region`/`ref_district`. Not stored, not editable here. Send `Accept-Language` (or whatever the app already uses) and it follows. |

Seed currently holds **2025 and 2026**. `/sync` grows it as stat.uz
publishes new years.

---

## 1. The table — year-scoped hierarchy

The table is a **republic → region → district** drill-down for one selected
year. It is not a flat paginated list.

### Year selector

```
GET /v1/references/populations/years
```
```json
{ "success": true, "data": [2026, 2025] }
```
Descending. Use it to populate the year dropdown. Default the dropdown to the
first entry (latest).

### Root rows — republic + regions

```
GET /v1/references/populations?year=2025
```
```json
{
  "success": true,
  "data": [
    { "id": 1,  "geoType": "REPUBLIC", "soatoId": 1700, "code": "UZ",    "name": "O‘zbekiston Respublikasi", "year": 2025, "population": 37543200, "source": "SDMX", "hasChildren": true },
    { "id": 15, "geoType": "REGION",   "soatoId": 1703, "code": "UZ-AN", "name": "Andijon viloyati",         "year": 2025, "population": 3268000,  "source": "SDMX", "hasChildren": true },
    { "id": 27, "geoType": "REGION",   "soatoId": 1706, "code": "UZ-BU", "name": "Buxoro viloyati",          "year": 2025, "population": 2011000,  "source": "SDMX", "hasChildren": true }
  ]
}
```
- First row is always the **republic total** (the feed's own `1700` figure —
  it is authoritative, not a sum of regions, so it may differ slightly from
  adding the regions up).
- Then every region, sorted by localized `name`.
- Then any `OTHER` rows (unmatched SOATO territories) for that year, after the
  regions — `hasChildren: false`, `name` falls back to the SOATO number.
  Normally none; fix one by `PUT`-ting a `regionCode`/`districtCode` +
  `geoType`.
- `year` omitted → backend uses the latest year.

### Child rows — a region's districts

When the user expands a region row (or navigates into it), pass its `code`:

```
GET /v1/references/populations?year=2025&regionCode=UZ-AN
```
Returns that region's `DISTRICT` rows for the **same year**, `hasChildren:
false`, sorted by localized `name`. Keep the year from the table's dropdown —
if the user changes the year, re-fetch both the open region and its expanded
children with the new `year`.

### `PopulationNodeResponse` fields

| Field | Use |
|---|---|
| `id` | Row id → open the detail view / edit. |
| `code` | `"UZ"` / region code / district code. Pass region `code` back as `regionCode` to load children. |
| `name` | Display, already localized. |
| `population` | The number to show for the selected year. |
| `hasChildren` | `true` for republic and regions, `false` for districts and `OTHER`. Controls the expand affordance. |
| `geoType`, `soatoId`, `source`, `year` | Metadata / badges. |

---

## 2. Detail view — `GET /{id}`

```
GET /v1/references/populations/15
```
```json
{
  "success": true,
  "data": {
    "id": 15,
    "geoType": "REGION",
    "soatoId": 1703,
    "code": "UZ-AN",
    "name": "Andijon viloyati",
    "regionCode": "UZ-AN",
    "regionName": "Andijon viloyati",
    "districtCode": null,
    "districtName": null,
    "year": 2025,
    "population": 3268000,
    "source": "SDMX",
    "deleted": false,
    "years": [
      { "id": 16, "year": 2026, "population": 3312000, "source": "SDMX" },
      { "id": 15, "year": 2025, "population": 3268000, "source": "SDMX" }
    ],
    "audit": {
      "createdAt": "2026-09-03T15:16:03+05:00",
      "createdBy": null,
      "updatedAt": "2026-09-03T15:16:03+05:00",
      "updatedBy": null
    }
  }
}
```

- **`year` / `population` / `source`** — the specific year-row you requested
  by `id`.
- **`years[]`** — the same territory's **whole time series** (all years,
  descending). Each entry has its own `id`, so "edit 2026" navigates to a
  different record than the one you opened. Use this for a mini
  year-vs-population table or sparkline.
- **`regionName` / `districtName`** — localized, for a breadcrumb
  ("Andijon viloyati › Andijon tumani").
- **`audit`** — who created / last changed **this year-row** and when.
  `createdBy` / `updatedBy` are `null` for seed and `/sync` rows; after a
  manual `PUT` they become `{ id, firstName, lastName, middleName }`. There
  is **no field-by-field change log** in this response.

---

## 3. Manual entry (correction)

Use when stat.uz is late with a year, or to fix an `OTHER` / wrong figure.
Manually entered rows get `source: "MANUAL"` and are **protected from
`/sync`** (it skips them).

### Create

```
POST /v1/references/populations
```
```json
{
  "geoType": "DISTRICT",
  "soatoId": 1703203,
  "regionCode": "UZ-AN",
  "districtCode": "AN-203",
  "year": 2027,
  "population": 245000
}
```
| Field | Rule |
|---|---|
| `geoType` | required — `REPUBLIC` / `REGION` / `DISTRICT` / `OTHER` |
| `soatoId` | required, positive |
| `year` | required, 1900–2100 |
| `population` | required, ≥ 0, **absolute persons** |
| `regionCode` / `districtCode` | optional, ≤ 50 chars — set them so the row joins geography (and reports pick it up) |

`(soatoId, year)` must be unique → `409 CONFLICT` otherwise. Response body is
the same `PopulationDetailResponse` as the GET.

### Update

```
PUT /v1/references/populations/{id}
```
```json
{
  "geoType": "DISTRICT",
  "regionCode": "UZ-AN",
  "districtCode": "AN-203",
  "population": 246500
}
```
`soatoId` and `year` are **identity — not editable** here. To move a figure
to a different year, delete and re-create. Returns the updated
`PopulationDetailResponse`; `audit.updatedBy` / `updatedAt` reflect the
change.

### Delete (soft)

```
DELETE /v1/references/populations/{id}
```
Soft delete — the row stops appearing in the table and detail. Note: a later
`/sync` **can bring an SDMX row back** (with `source: "SDMX"`). Deleting is
really only meaningful for `MANUAL` rows or genuinely stale `OTHER` rows.

---

## 4. "SDMX'dan yangilash" button

```
POST /v1/references/populations/sync
```
No body. Fetches the stat.uz feed and upserts by `(soatoId, year)` for years
≥ the configured `min-year` (2025). New feed years → new rows; revised
figures for an existing year → updated in place; `MANUAL` rows untouched.

```json
{
  "success": true,
  "message": "Aholi soni ma'lumotnomasi stat.uz SDMX'dan yangilandi.",
  "data": {
    "processed": 442,
    "inserted": 0,
    "updated": 442,
    "skippedManual": 0,
    "unmatched": 1,
    "minYear": 2025,
    "sdmxLastModified": "2026-04-22"
  }
}
```

Show a toast from the result: e.g. *"Yangilandi: +{inserted} qo'shildi,
{updated} yangilandi, {skippedManual} qo'lda kiritilgan o'tkazib yuborildi,
{unmatched} hudud mos kelmadi. Manba sanasi: {sdmxLastModified}."*

On `502 UPSTREAM_ERROR` — feed unreachable, nothing changed, offer retry.

The call can take a few seconds (network + upsert of ~440 rows). Disable the
button and show a spinner while it runs, then refresh the table.

---

## Suggested screen

```
┌─ Aholi soni ─────────────────────────────── [ Yil: 2026 ▾ ]  [ SDMX'dan yangilash ] ┐
│  Hudud                              Aholi soni      Manba                             │
│  ▸ O‘zbekiston Respublikasi         38 236 700      SDMX                              │
│  ▸ Andijon viloyati                  3 312 000      SDMX                              │
│    ▾ (expanded)                                                                      │
│       Andijon tumani                   246 000      SDMX      [ ✎ ]                   │
│       Oltinko‘l tumani                 190 500      MANUAL    [ ✎ ]  [ 🗑 ]           │
│  ▸ Buxoro viloyati                   2 040 000      SDMX                              │
│  …                                                                                   │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

- Year dropdown from `GET .../years`; changing it re-loads the tree at the
  same expansion.
- Row click / `✎` → detail drawer with `years[]` series + `audit`.
- `🗑` only for `source: "MANUAL"` rows (or stale `OTHER`).
- `+ Qo'shish` opens the create form (territory picker + year + number).
