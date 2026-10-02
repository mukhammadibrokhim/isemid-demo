# Form 10 / Form 11 report — frontend integration guide

Two **structurally identical** morbidity reports — geography‑first
(republic→region→district→organization), confirmed notifications only, with a
**"Joriy davr" / "Yig'ma"** two‑block period model. Form 11 additionally cuts
by urban/rural.

| | Form 10 | Form 11 |
|---|---|---|
| Title | «Respublika bo'yicha ma'muriy hududlar kesimida yuqumli kasalliklar bilan kasallanish to'g'risidagi ma'lumotlar» | «Yuqumli va parazitar kasalliklar bilan kasallanish ko'rsatkichlari» |
| Base path | `/v1/reports/form-10` | `/v1/reports/form-11` |
| Swagger tag | `Report — Form 10` | `Report — Form 11` |
| Age cut | under **14** | under **18** |
| Extra slices | — | **city** / **rural** |

Everything below applies to **both** unless a table says otherwise — they
share one query engine and one response shape (`Form11*` mirrors `Form10*`
field‑for‑field, plus `city`/`rural`).

> **If your screen still has a manual "Aholi soni" (population) input** —
> that was the *old* system's design and is now obsolete for both reports.
> See §3 for why, and delete that field. Only "Koeffitsiyent" survives, as
> `koef`.

---

## 1. What the numbers mean

Each data cell counts **notifications** from `form058` + `form058_1` (unioned)
where **all** of:

- `deleted = false`
- `status = 'APPROVED'` (confirmed cases only — no primary / not‑yet‑decided)
- optionally, `final_icd10_code = diagnosisCode` when `diagnosisCode` is
  passed. **Final code only** — no fallback to the initial `icd10_code`.
- `created_at` inside the resolved month span (see §2)
- the case's `sender_organization_id` is inside the caller's access scope
  (and, when drilling down, inside the requested region/district)

Age is **complete calendar years at `created_at`** (`age(created_at, birth_date)`,
not "as of today").

- Form 10's `child` = age < 14.
- Form 11's `child` = age < 18; `city` = `patient.population_type_code = 'CITY_RESIDENT'`;
  `rural` = `'VILLAGE_RESIDENT'`.

---

## 2. Period model — `year` + `period`, not `from`/`to`

Both reports take **`year`** (int) + **`period`** (`ReportPeriod` enum), not a
free date range:

```
JANUARY … DECEMBER   (single month)
Q1, Q2, Q3, Q4        (quarter)
HALF_YEAR             (Jan–Jun)
NINE_MONTHS           (Jan–Sep)
YEAR                  (Jan–Dec)
```

Each value expands into **two blocks**, both always shown side by side:

| Block | Span |
|---|---|
| **"Joriy davr"** | the period's own month span (e.g. `Q2` → Apr–Jun) |
| **"Yig'ma"** | January through the period's end month (e.g. `Q2` → Jan–Jun) |

So `Q2` ("Joriy" = Apr–Jun) is a *different* row shape from `HALF_YEAR`
("Joriy" = Jan–Jun) even though both have "Yig'ma" = Jan–Jun. Likewise `Q3` vs
`NINE_MONTHS`, `Q4` vs `YEAR`.

Each block is further split into **previous year / current year / growth**,
so one API call effectively renders 4 hierarchy walks worth of data
(current‑year Joriy, previous‑year Joriy, current‑year Yig'ma, previous‑year
Yig'ma), zipped into one row per territory.

Defaults: `year` → current year, `period` → current month, if omitted.

---

## 3. Population — resolved automatically per territory, not entered by hand

This is the part that differs from the legacy system and from `report-excel-export-frontend-guide.md`'s
now‑outdated §2.3 for Form 11 (that section still shows a `population`
query param — ignore it, it predates this design; see the fix landing
alongside this guide).

**There is no `population` request parameter on either report.** The backend
resolves each row's own population figure server‑side from the **`ref_population`**
reference table (the "Aholi soni" screen — see
`population-reference-frontend-guide.md`), keyed by **that row's own territory
code + the report year**:

| Row is… | Population source |
|---|---|
| Republic root / "Jami" | `ref_population` REPUBLIC row (`soatoId 1700`, code `"UZ"`) for the year |
| A region row | `ref_population` REGION row for that region's code + the year |
| A district row | `ref_population` DISTRICT row for that district's code + the year |
| An organization row | **No population of its own** — reuses its parent district's (or the root's, for a district‑scope caller) figure |

Two independent lookups happen per row — one for the current year, one for
`year - 1` — because "Joriy davr" and "Yig'ma" both compare against the same
year‑ago period, and a territory's population can differ year to year.

A territory with no `ref_population` row for that year resolves to population
`0`, and the intensive rate for that row is then `0` (never a divide‑by‑zero
error) — a data‑seed gap, not a bug to route around client‑side.

**Why this replaces the manual field:** the old "Aholi soni" input asked the
operator to type in a population figure by hand for the whole report (one
number for everything, like the `10000` in the screenshot). Now every row —
region, district, organization — gets its **own correct, year‑specific**
population automatically, which a single manual number could never represent
across a whole hierarchy. If a population figure looks wrong, it's fixed once
in the "Aholi soni" reference screen (super‑admin only), not per report call.

**"Koeffitsiyent" stays** — it is not a population figure, it's the intensive
rate's denominator base (per how many people the rate is expressed), e.g.
`100000` = "per 100,000 population". Keep that one field, wired to `koef`.

### The actual formula

```
intensiveRate = caseCount * koef / territoryPopulation
```

computed separately for previous year and current year, for every metric
slice (`total`, and Form10's `child` / Form11's `city`+`rural`+`child`), in
both blocks (Joriy davr, Yig'ma). Rounded to 2 decimals server‑side — render
as received, don't re‑round.

---

## 4. Access

```
isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')
```

| Caller scope | `root` returns | drill‑down levels |
|---|---|---|
| Republican / ALL | regions + `Jami` | region → district → organization |
| Region | districts + `Jami` | district → organization |
| District | organizations + `Jami` | (none below organization) |
| Single organization | that org's own totals only, no `Jami` | none (`hasChildren = false`) |

---

## 5. `GET {base}/root`

| Query param | Type | Default | Notes |
|---|---|---|---|
| `year` | int | current year | |
| `period` | `ReportPeriod` | current month | see §2 |
| `diagnosisCode` | string | — | ICD‑10 filter, final code only |
| `koef` | long | `100000` | intensive‑rate base |

Response — one row per region/district/organization plus a trailing `Jami`:

```json
{
  "success": true,
  "message": "...",
  "data": [
    {
      "code": "1703", "name": "Andijon viloyati", "hasChildren": true,
      "current": {
        "total":  { "absPreviousYear": 12, "absCurrentYear": 9, "absGrowthPercent": 3,
                     "intensivePreviousYear": 3.68, "intensiveCurrentYear": 2.72, "intensiveGrowthPercent": "-26.09" },
        "child":  { "...": "same shape" }
      },
      "cumulative": {
        "total": { "...": "same shape, Jan→period-end span" },
        "child": { "...": "same shape" }
      }
    },
    { "code": "TOTAL", "name": "Jami", "hasChildren": false, "current": { "...": "..." }, "cumulative": { "...": "..." } }
  ]
}
```

Form 11's `current`/`cumulative` blocks additionally carry `city` and `rural`
next to `total`/`child` — same 6‑field metric shape.

### Metric field reference (`Form10Metric` / `Form11Metric`, identical shape)

| Field | Meaning |
|---|---|
| `absPreviousYear` | Case count, same span, `year - 1`. |
| `absCurrentYear` | Case count, `year`. |
| `absGrowthPercent` | **Not a percentage** despite the name — it's `\|absCurrentYear - absPreviousYear\|`, a plain case‑count difference. Render it as a count, not with a `%` sign. |
| `intensivePreviousYear` | `absPreviousYear * koef / <territory population for year - 1>`. |
| `intensiveCurrentYear` | `absCurrentYear * koef / <territory population for year>`. |
| `intensiveGrowthPercent` | A **string**. Usually a signed percentage with 2 decimals (e.g. `"-12.22"`); once either direction's ratio reaches 2×, it switches to `"X marta"` / `"-X marta"` ("X‑fold", 1 decimal) instead of an unbounded percentage like `"900.00"`. Render verbatim — don't parse it as a number, and don't append `%` when it already says `marta`. |

---

## 6. `GET {base}/children`

| Query param | Type | Notes |
|---|---|---|
| `regionCode` | string | Optional. Omit for the caller's own first level. |
| `districtCode` | string | Optional. |
| `year`, `period`, `diagnosisCode`, `koef` | — | Same as `root`, pass through what the user has selected. |

Same drill pattern as every other geography report: no `regionCode`/`districtCode`
→ caller's own first level; `regionCode` alone → that region's districts;
`+districtCode` → that district's organizations. Out‑of‑scope codes → `403`
(`organization.scope_violation`). No `Jami` row on `children`.

---

## 7. Excel export

Full mechanics (submit → progress → download) are in
`report-excel-export-frontend-guide.md` §1/§3 — this is just the submit call:

```
POST /v1/reports/form-10/export     exportType: FORM10
POST /v1/reports/form-11/export     exportType: FORM11
```

Same query params as `root` (`year`, `period`, `diagnosisCode`, `koef`) — no
`population`, no `from`/`to`. The exported sheet flattens the whole
hierarchy (region→district→organization) into rows, with the "Joriy
davr"/"Yig'ma" × total/child(+city/rural) × (prev|curr|growth) ×
(absolute|intensive) columns nested exactly like the on‑screen table.

---

## 8. Suggested UI flow

1. Filter bar: **Yil** (year) + **Davr** (period — month/quarter/half‑year/9
   months/year dropdown) + optional **Tashxis kodi** (ICD‑10) + **Koeffitsiyent**
   (numeric, default `100000`). **No population field.**
2. On load / filter change → `GET {base}/root?year&period&diagnosisCode&koef`.
   Render every non‑`Jami` row with an expand chevron where `hasChildren`;
   pin `Jami` last.
3. On expand → `GET {base}/children?regionCode|districtCode&year&period&diagnosisCode&koef`.
4. Table columns: two top‑level groups ("Joriy davr", "Yig'ma"), each split
   into total (+ Form 11's city/rural) / child, each split into
   prev‑year / curr‑year / growth, each split into absolute / intensive —
   matching the Excel export's nested headers (§7) so the on‑screen table and
   the download look the same.
5. Changing **Koeffitsiyent** only rescales the intensive columns — re‑issue
   the same `root`/`children` calls with the new `koef`; the backend
   recomputes server‑side (it is not something to compute client‑side from a
   cached absolute count, since population also varies previous‑year vs
   current‑year).

---

## 9. Edge cases

- No `ref_population` row for a territory/year → population `0` → intensive
  rate `0` for every metric on that row (never an error). Worth a visual
  affordance (e.g. dash instead of `0.00`) if the analyst needs to tell
  "genuinely zero incidence" from "no population data yet" apart — the API
  does not distinguish them.
- Organization rows never have their own population — they always show their
  parent district's (or root's) rate. Don't expect an organization‑level
  intensive rate to differ from siblings under the same district beyond what
  their case counts alone explain.
- `intensiveGrowthPercent` is a string for a reason (the `"X marta"` case) —
  don't `parseFloat` it for sorting/coloring; use `intensiveCurrentYear` /
  `intensivePreviousYear` directly if you need a numeric comparison.
- Single‑organization caller: `root` returns just one data row (their own
  totals, reusing the parent district's population) with `hasChildren =
  false`, no `Jami`.
