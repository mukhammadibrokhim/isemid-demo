# Form 28.1 / Form 28.2 report — frontend integration guide

Two statistical reports built as **clones of Form 12** (disease‑first rows, a
geography drill‑down per row):

| | Form 28.1 | Form 28.2 |
|---|---|---|
| Title | «Ayrim yuqumli va parazitar kasalliklar haqida ma'lumotlar» | «Kasalxona ichki infeksiyalari haqida ma'lumotlar» |
| Base path | `/v1/reports/form-28-1` | `/v1/reports/form-28-2` |
| Rows | `ref_manual_report` entries tagged **`FORM_28_1`** | entries tagged **`FORM_28_2`** |
| Swagger tag | `Report — Form 28.1` | `Report — Form 28.2` |

Everything below applies to **both** unless a column table says otherwise.

---

## 1. What the numbers mean

Each data cell counts **notifications** from `form058` + `form058_1` (unioned)
where **all** of:

- `deleted = false`
- `status = 'APPROVED'` (confirmed cases only — no primary / not‑yet‑decided)
- `final_icd10_code is not null` **and** `final_icd10_code` ∈ the row's manual‑report
  ICD‑10 set. **Final code only** — there is no fallback to the initial
  `icd10_code`; a case that never got a final diagnosis is not counted anywhere
  in these reports.
- `created_at` ∈ the selected `[from, to]` period
- the case's `sender_organization_id` is inside the caller's access scope (and,
  when drilling down, inside the requested region/district)

Age is **complete units at `created_at`** (not "as of today").

There is **no year‑over‑year** column — one period only. (This is the main
difference from Form 12/13.)

---

## 2. Access

```
isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')
```

Same as every other report. A caller without the permission gets `403`.

The rows a caller sees, and how deep the drill‑down goes, follow their
**organization scope**:

| Caller scope | `root` returns | drill‑down levels |
|---|---|---|
| Republican / ALL | regions + `Jami` | region → district → organization |
| Region | districts + `Jami` | district → organization |
| District | organizations + `Jami` | (none below organization) |
| Single organization | that org's own totals only, no `Jami` | none (`hasChildren = false`) |

---

## 3. Response envelope

Standard wrapper:

```json
{ "success": true, "message": "...", "data": [ /* rows */ ] }
```

`data` is a flat array of row objects. The frontend renders the tree itself
(see §6) and builds the Excel export from this JSON.

---

## 4. `GET {base}/root` — the report table (row 1 = first disease)

| Query param | Type | Notes |
|---|---|---|
| `from` | `YYYY-MM-DD` | Period start, inclusive. Optional. |
| `to` | `YYYY-MM-DD` | Period end, inclusive. Optional. |

**Both omitted → whole history** (not "today"). One side omitted → the other
side defaults to today.

Returns one row per `FORM_28_1` / `FORM_28_2` manual‑report entry, **sorted by
`code`** (case‑insensitive), then a trailing **`Jami`** row.

- `Jami` = the sum of only the rows whose catalog entry has
  `includeInTotal = true`. Its `code` is `"TOTAL"`, `rowCode`/`icd10Display` are
  `null`, `hasChildren = false`.
- The disease list is admin‑managed: `Ma'lumotnomalar → Qo'lda kiritiladigan
  hisobotlar`, tag field «Hisobot turi». Tagging a row `FORM_28_1` /
  `FORM_28_2` there is what makes it appear here — no other wiring. See
  `manual-report-reference-frontend-guide.md`.

### Row shape — common columns

| Field | Meaning |
|---|---|
| `code` | Row id. For a disease row it is the manual‑report **id** (pass it back as `manualReportId` to drill down). For geography rows it is the region/district code or organization id. `"TOTAL"` for `Jami`. |
| `name` | Localized display name (nosological form name, or geography node name). |
| `rowCode` | «Qator kodi» — the manual‑report `code` (e.g. `101`, `201`). `null` for geography + `Jami`. |
| `icd10Display` | «Kasalliklarning XKT bo'yicha shifri» — the manual‑report `shortName` (usually an ICD‑10 range like `A01.1-A01.4`). `null` for geography + `Jami`. |
| `hasChildren` | Show the expand arrow when `true`. |

### Form 28.1 — data columns

| JSON field | Varaqa column |
|---|---|
| `total` | Qayd qilingan kasalliklar, jami |
| `female` | Ayollarda |
| `under18` | 17 yoshgacha bo'lgan bolalarda (17 yoshni qo'shgan holda) — age < 18 |
| `under15` | 14 yoshgacha bo'lgan bolalarda (14 yoshni qo'shgan holda) — age < 15 |
| `under1` | Ulardan 1 yoshgacha bo'lgan bolalarda — age < 1 |
| `age1to2` | 1-2 yoshgacha (2 yoshni qo'shgan holda) — age 1–2 |
| `age3to5` | 3-5 yoshdagilarda — age 3–5 |
| `ruralTotal` | Qishloq aholisida — Jami 1‑ustundan |
| `ruralUnder18` | Qishloq aholisida — 17 yoshgacha |
| `ruralUnder15` | Qishloq aholisida — 14 yoshgacha |
| `ruralUnder1` | Qishloq aholisida — 1 yoshgacha |
| `ruralAge1to2` | Qishloq aholisida — 1-2 yoshgacha |
| `ruralAge3to5` | Qishloq aholisida — 3-5 yoshdagilarda |

Rural = `patient.population_type_code = 'VILLAGE_RESIDENT'`. «Ayollarda» is **not**
repeated in the rural block. The age columns are **nested/overlapping**
(`under18 ⊇ under15 ⊇ under1`); `age1to2` and `age3to5` are disjoint bands.

### Form 28.2 — data columns

| JSON field | Varaqa column |
|---|---|
| `total` | Qayd qilingan kasalliklar, jami |
| `under18` | 17 yoshgacha bo'lgan bolalarda (17 yoshni qo'shgan holda) — age < 18 years |
| `underOneMonth` | 1 oygacha — age < 1 month |
| `oneMonthToUnderOneYear` | 1 oy 1 yoshgacha — 1 month ≤ age < 1 year |

`underOneMonth` + `oneMonthToUnderOneYear` together = all children under 1 year.

### Example

```
GET /v1/reports/form-28-1/root?from=2026-08-28&to=2026-09-04
```
```json
{
  "success": true,
  "message": "Amal muvaffaqiyatli bajarildi.",
  "data": [
    {
      "code": "39", "name": "Ichterlama", "rowCode": "101", "icd10Display": "A01.0",
      "hasChildren": true,
      "total": 12, "female": 5, "under18": 3, "under15": 2, "under1": 0,
      "age1to2": 1, "age3to5": 1,
      "ruralTotal": 7, "ruralUnder18": 2, "ruralUnder15": 1, "ruralUnder1": 0,
      "ruralAge1to2": 1, "ruralAge3to5": 0
    },
    { "code": "TOTAL", "name": "Jami", "rowCode": null, "icd10Display": null,
      "hasChildren": false, "total": 40, "female": 18, "...": "..." }
  ]
}
```

---

## 5. `GET {base}/children` — drill one disease down the geography

| Query param | Type | Notes |
|---|---|---|
| `manualReportId` | `long` | **Required.** The `code` of the disease row being expanded (it holds the manual‑report id). |
| `regionCode` | string | Optional. Omit for the first level. |
| `districtCode` | string | Optional. |
| `from` / `to` | `YYYY-MM-DD` | Same period you passed to `root`. Optional, same defaults. |

Levels, given the caller has republican scope:

| Call | Returns |
|---|---|
| `manualReportId=39` | one row per **region** (`hasChildren = true`) |
| `manualReportId=39&regionCode=1703` | one row per **district** of that region (`hasChildren = true`) |
| `manualReportId=39&districtCode=1703215` | one row per **organization** of that district (`hasChildren = false`) |

- A region/district scoped caller skips the levels above them — just call
  `children?manualReportId=…` and you get your own first level.
- `regionCode` / `districtCode` outside the caller's scope → `403`
  (`organization.scope_violation`).
- Geography rows carry the **same data columns** as the disease row (28.1: 13
  fields; 28.2: 4 fields), plus `code` = geo code / org id, `name` = localized
  geo name, `rowCode` = `null`, `icd10Display` = `null`.
- **No `Jami` row** on `children` responses — only `root` has it.

```
GET /v1/reports/form-28-2/children?manualReportId=201&regionCode=1703&from=2026-08-28&to=2026-09-04
```
```json
{
  "success": true, "message": "...",
  "data": [
    { "code": "1703215", "name": "Andijon shahri", "rowCode": null,
      "icd10Display": null, "hasChildren": true,
      "total": 4, "under18": 1, "underOneMonth": 0, "oneMonthToUnderOneYear": 1 }
  ]
}
```

---

## 6. Suggested UI flow

1. Filter bar: **Sanadan / Sanagacha** date pickers, and (optional, cosmetic
   for the operator) **Viloyat / Shahar-Tuman** selectors. The region/district
   selectors just feed `regionCode` / `districtCode` into a `children` call —
   a republican user picking a region is equivalent to expanding that region's
   row.
2. On load / filter change → `GET {base}/root?from&to`. Render every non‑`Jami`
   row with an expand chevron where `hasChildren`. Pin the `Jami` row last.
3. On expand of a disease row → `GET {base}/children?manualReportId={row.code}&from&to`,
   insert the returned region rows as children.
4. On expand of a region row → same endpoint `+ &regionCode={row.code}`; on a
   district row `+ &districtCode={row.code}`.
5. Keep `manualReportId` fixed to the disease you're drilling — it does not
   change as you go deeper.
6. **Excel export** is built client‑side from the row array (header layout =
   the varaqa in §4). There is no server export endpoint for these two.

---

## 7. Edge cases

- Empty period / no matching cases → rows still returned, all counts `0`
  (so the varaqa always renders in full). `Jami` = all `0`.
- A disease whose catalog entry has an **empty ICD‑10 set** → always `0`, and
  `children` returns rows of `0` (nothing to match).
- `manualReportId` of a soft‑deleted / non‑existent entry → `404`
  (`reference.manual_report.not_found_by_id`).
- Single‑organization caller: `root` returns just one data row (their own
  totals) with `hasChildren = false`, no `Jami`, and `children` returns `[]`.
- A case whose final code sits in **two** tagged entries' sets is counted in
  **both** rows (independent per‑row roll‑up) — the `Jami` row can therefore be
  less than the plain sum of the visible rows if you also filter
  `includeInTotal`, and more than a naïve "distinct cases" count. This matches
  Form 12.
