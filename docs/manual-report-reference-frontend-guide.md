# Manual report reference — frontend integration guide

Covers the **«Qo'lda kiritiladigan hisobot ta'riflari» / «Ручные отчёты»**
reference (`ref_manual_report`) under **Ma'lumotnomalar**. One record = one
nosological form: a code, localized names, an "include in total" flag, a set
of **ICD-10 (MKB-10) diagnosis codes** it aggregates, and a set of
**report-type tags** («Hisobot turi») that decide which statistical reports
(Form 12, Form 13, Form 28.1, Form 28.2, …) the row shows up in.

This guide is written around the **«Hisobot turi» field** (screenshot: the
"Yangi hisobot ta'rifi" dialog) which the form is currently missing — plus
the rest of the screen for completeness. For exact request/response field
lists use the generated Swagger UI (**"Reference - Manual Reports"** and
**"Reference - Catalogs"** tags); every DTO field carries a `@Schema`
description. This guide covers the screen flow and the non-obvious parts.

## Access

| Action | Who |
|---|---|
| List / view / lookups | any authenticated user (`isAuthenticated()`) |
| Create / update / delete | `isemid_super_admin` **or** `isemid_admin` (`@adminAccessGuard.isAdmin()`) — otherwise `403 FORBIDDEN` |

Show the create/edit/delete buttons only to admins.

## Base path

```
/v1/references/manual-reports
```

## Response envelope

Standard wrapper, same as every module.

Single object:
```json
{ "success": true, "message": "Amal muvaffaqiyatli bajarildi.", "data": { } }
```

Paged (table):
```json
{ "success": true, "message": "...", "data": [ ], "meta": { }, "links": { } }
```

Error:
```json
{
  "success": false,
  "code": "VALIDATION_FAILED",
  "message": "...",
  "traceId": "9f1c2a3b4d",
  "path": "/v1/references/manual-reports",
  "timestamp": "2026-09-04T12:00:00Z",
  "violations": [ { "field": "icd10Codes", "message": "At least one ICD-10 ..." } ]
}
```

| `code` | HTTP | When |
|---|---|---|
| `VALIDATION_FAILED` | 400 | `code` blank / > 50 chars, any name > 255, `shortName` > 100, `icd10Codes` empty. `violations[]` names the fields. |
| `FORBIDDEN` | 403 | Caller is not an admin. |
| `NOT_FOUND` | 404 | No record with that `id` / `code` (or soft-deleted). |
| `CONFLICT` | 409 | `create` with a `code` that already exists; `update` on a soft-deleted record. |

---

## Data model — what a record means

| Field | Notes |
|---|---|
| `id` | Row id. |
| `code` | Unique, case-sensitive, ≤ 50 chars. **Immutable in practice** — the form says «keyin o'zgartirib bo'lmaydi»; the API allows changing it on `update` but don't expose that. Trimmed server-side, not upper-cased. |
| `shortName` | ≤ 100 chars. Shown in report headers (usually the ICD-10 range, e.g. `A15-A19`). Optional. |
| `nameUz` / `nameUzCyril` / `nameRu` / `nameKaa` | ≤ 255 chars each. All optional, but fill at least `nameUz` + `nameRu`. |
| `includeInTotal` | Bool, default **true**. Whether this row is summed into the report's «Jami» line. The toggle «Umumiy yakunga qo'shilsin». |
| `icd10Codes` | **Set of strings**, ≥ 1 required. The MKB-10 codes this form aggregates. Stored trimmed + UPPER-CASED. Values may carry `+` / `*` suffixes (`A39.0+`, `J17.0*`) — that matches the `ref_icd10` catalog, keep them. |
| `reportTypes` | **Set of strings**, optional (may be empty / omitted). The «Hisobot turi» tags. Each value is a `code` from the `REPORT_TYPE` catalog (`FORM_12`, `FORM_13`, `FORM_28_1`, `FORM_28_2`). Stored trimmed, case preserved; reports match case-insensitively. |
| `deleted` | Soft-delete flag. Deleted rows never come back from read endpoints. |

---

## 1. The «Hisobot turi» field — the missing piece

### 1a. Load the options from the catalog

The dropdown options are **not hard-coded** — fetch them from the shared
catalog by type `REPORT_TYPE`:

```
GET /v1/references/catalogs/types/REPORT_TYPE
```
```json
{
  "success": true,
  "data": [
    { "id": 900, "type": "REPORT_TYPE", "code": "FORM_12",   "parentCode": null, "name": "12-shakl statistik hisobot" },
    { "id": 903, "type": "REPORT_TYPE", "code": "FORM_13",   "parentCode": null, "name": "13-shakl hisobot" },
    { "id": 901, "type": "REPORT_TYPE", "code": "FORM_28_1", "parentCode": null, "name": "28.1-shakl statistik hisobot" },
    { "id": 902, "type": "REPORT_TYPE", "code": "FORM_28_2", "parentCode": null, "name": "28.2-shakl statistik hisobot" }
  ]
}
```

- `name` is already localized to the request locale (send the same
  `Accept-Language` the app uses elsewhere).
- Optional query params: `limit` (default 20, max 200), `name` (search across
  all locales). Four rows today, so just fetch without params.
- **Bind on `code`, display `name`.** The manual-report payload uses `code`.
- Cache it — it changes only when an admin edits the catalog.

### 1b. Render it

- **Multi-select** (chips / tags). A row can belong to several reports at
  once — e.g. code `105` → `["FORM_12","FORM_13","FORM_28_1"]`.
- **Not required.** No asterisk, empty is valid. An empty set just means the
  row appears in no report until someone tags it.
- Place it near «Umumiy yakunga qo'shilsin» in the dialog.
- Label: «Hisobot turi» / «Тип отчёта».
- Helper text: «Bu ta'rif qaysi statistik hisobotlarda chiqishini belgilaydi»
  / «Определяет, в каких статистических отчётах появляется эта запись».

### 1c. Send it

`POST` / `PUT` body — `reportTypes` is a plain array of catalog codes:

```json
{
  "code": "162",
  "shortName": "A15-A19",
  "nameUz": "Sil (birinchi marta aniqlangan, barcha shakli)",
  "nameUzCyril": "Сил ...",
  "nameRu": "Туберкулез ...",
  "nameKaa": "Sil ...",
  "includeInTotal": true,
  "reportTypes": ["FORM_12", "FORM_13", "FORM_28_1"],
  "icd10Codes": ["A15.0", "A15.1", "A16.0", "A19.9"]
}
```

- Omit the key or send `[]` when nothing is selected.
- Order does not matter (stored as a set).
- Duplicates are de-duplicated server-side.
- **Not validated against the catalog today** — the server accepts any
  string. Keep the UI a closed select so only real codes go out. (Server-side
  catalog validation can be added later if needed — ask backend.)

### 1d. Read it back (edit form prefill)

```
GET /v1/references/manual-reports/{id}
```
```json
{
  "success": true,
  "data": {
    "id": 105,
    "code": "162",
    "shortName": "A15-A19",
    "nameUz": "Sil ...", "nameUzCyril": "...", "nameRu": "...", "nameKaa": "...",
    "includeInTotal": true,
    "reportTypes": ["FORM_12", "FORM_13", "FORM_28_1"],
    "icd10Codes": ["A15.0", "A15.1", "A16.0", "A19.9"],
    "deleted": false
  }
}
```

`reportTypes` comes back as **codes**. Map each to its catalog `name` (from
1a) for the chip label; keep the code as the value. A code that is no longer
in the catalog (deleted type) still comes back — show the raw code as
fallback.

---

## 2. The table

```
GET /v1/references/manual-reports?page=1&size=20&sortBy=code&sortDir=asc
```

| Param | Notes |
|---|---|
| `page` | 1-based. |
| `size` | 1–200, default 20. |
| `sortBy` | `id` \| `code` \| `shortName` \| `nameUz` \| `nameUzCyril` \| `nameRu` \| `nameKaa` \| `createdAt` \| `updatedAt`. Unknown → default sort. |
| `sortDir` | `asc` \| `desc`. |
| `name` | Search across all four name locales. |
| `code` | Exact code filter. |

Row (`ManualReportTableResponse`): `id`, `code`, `shortName`, `nameUz`,
`nameUzCyril`, `nameRu`, `nameKaa`, `includeInTotal`, `deleted`.

> The table row does **not** include `reportTypes` or `icd10Codes` — those
> come only from `GET {id}`. If you want a «Hisobot turi» column in the
> table, tell backend to add it to `ManualReportTableResponse`; right now
> you would have to N+1 the detail endpoint, don't.

---

## 3. Create / update / delete

| | Method + path | Body | Notes |
|---|---|---|---|
| Create | `POST /v1/references/manual-reports` | `ManualReportCreateRequest` | 201. `code` must be unique → 409 `CONFLICT` on clash. |
| Update | `PUT /v1/references/manual-reports/{id}` | `ManualReportUpdateRequest` | Same body shape as create. Full replace — send the complete `icd10Codes` / `reportTypes` sets, not a delta. |
| Delete | `DELETE /v1/references/manual-reports/{id}` | — | Soft delete. |

Both request bodies: `code*`, `shortName`, `nameUz`, `nameUzCyril`,
`nameRu`, `nameKaa`, `includeInTotal`, `reportTypes`, `icd10Codes*`
(`*` = required; `icd10Codes` must be non-empty).

`update` is a **replace**: to remove one report-type tag, send the array
without it. To clear all tags, send `[]`.

---

## 4. Other read endpoints (context)

| Endpoint | Returns | Used by |
|---|---|---|
| `GET .../manual-reports/code/{code}` | one row (lookup shape: `id`, `code`, `shortName`, `name`) | code lookup |
| `GET .../manual-reports/icd10/{code}` | all rows whose `icd10Codes` contain that diagnosis | «which report(s) does this Form 058 diagnosis count toward» |

The statistical reports themselves (Form 12 = rows tagged `FORM_12`, Form 13
= columns tagged `FORM_13`, etc.) read this reference **server-side** — they
call `findByReportType("FORM_12")` internally. So tagging a row here is what
makes it appear in the corresponding report; there is no separate wiring.

---

## Checklist for the "Yangi hisobot ta'rifi" dialog

- [ ] On open: `GET /v1/references/catalogs/types/REPORT_TYPE` → build the
      «Hisobot turi» multi-select options (`code` value, `name` label).
- [ ] Add the «Hisobot turi» multi-select — **not required** — below
      «Umumiy yakunga qo'shilsin».
- [ ] On save: include `reportTypes: string[]` (catalog codes) in the
      `POST` / `PUT` body; omit or `[]` when empty.
- [ ] On edit open: `GET {id}` → prefill the multi-select from
      `data.reportTypes`, mapping codes → catalog names for labels.
- [ ] Keep `code` read-only in the edit form.
