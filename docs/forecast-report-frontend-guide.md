# «Forecast» report — frontend integration guide

Surveillance **forecasting** over `form058` + `form058_1`. Same drill‑down
convention as every other report — geography first
(republic→region→district→organization, one level per call via
`report.shared.ReportHierarchyService`) — plus a per‑node **chart** endpoint.

The first screen, with no params, is the whole republic broken down by
region (each row a compact forecast summary) plus a **`Jami`** row. From
there you drill by territory within your access scope, and open any node's
full history + forecast chart.

| | |
|---|---|
| Title | «Kasallanish prognozi» |
| Base path | `/v1/reports/forecast` |
| Swagger tag | `Report — Forecast` |
| Access | `isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')` |

| Endpoint | Returns |
|---|---|
| `GET /v1/reports/forecast/root` | Geography breakdown — first hierarchy level of the caller's scope + `Jami`, one compact forecast row each |
| `GET /v1/reports/forecast/children` | Geography breakdown — next level down (region → districts, district → organizations) |
| `GET /v1/reports/forecast/series` | Full `history[]` + `forecast[]` + endemic channel for **one** node (the chart) |
| `GET /v1/reports/forecast/top-diseases` | Risk‑ranked **"which diseases might rise"** list for **one** node — every ICD‑10 code seen in the training window forecast independently |

---

## 1. What the numbers mean

Every count is over `form058` + `form058_1` (unioned), `deleted = false`,
`sender_organization_id` inside the resolved geography node, bucketed by
**`created_at`** in `Asia/Tashkent`.

**Case set is wider than the confirmed‑count reports (Form 8/10/11/12/13):**
every *live* notification is counted — `status <> 'CANCELED'`, i.e. primary
**and** confirmed alike. A forecast of disease burden must not wait for
final approval. Consequently the ICD‑10 filter matches the **initial
`icd10_code` OR the final `final_icd10_code`**, not the final code alone.

- **`actual`** — observed notifications in a past bucket (`series` →
  `history[]`).
- **`predicted`** — point forecast for a future bucket, rounded to a
  non‑negative integer.
- **`lowerBound` / `upperBound`** — ~95% prediction interval
  (`point ± 1.96·σ·√step`, σ = the model's in‑sample one‑step error;
  `lowerBound` clamped at 0).
- **`endemicThreshold`** — classical epidemic threshold for that **seasonal
  moment**: `mean + 1.96·SD` over the history of the *same* month‑of‑year
  (MONTH), ISO‑week‑of‑year (WEEK) or day‑of‑week (DAY). Phases with < 2
  historical observations fall back to a single global threshold.
- **`alert`** — `predicted > endemicThreshold` for that bucket.

### Bucketing

| `bucket` | `periodStart` is | Seasonality | Default look‑back (`from` omitted) | Max `horizon` |
|---|---|---|---|---|
| `DAY` | that day | day‑of‑week (7) | 180 days | 90 |
| `WEEK` *(default)* | Monday of the ISO week | week‑of‑year (52) | 104 weeks | 52 |
| `MONTH` | 1st of the month | month‑of‑year (12) | 36 months | 24 |

The `series` history is **gap‑filled**: buckets with no cases are present
with `actual = 0`.

### Model selection (`method = AUTO`, the default)

| Condition | Model |
|---|---|
| < 3 history buckets | `NAIVE_MEAN` (flat line at the running mean) |
| ≥ 2 full seasonal cycles (`n ≥ 2 × seasonLength`) | `HOLT_WINTERS_ADDITIVE` |
| ≥ 4 buckets | `HOLT` (level + linear trend) |
| otherwise | `SES` (level only) |

The model is picked **per node** — one region may get Holt‑Winters, a
smaller district Holt. Every response reports the model actually used.
Smoothing constants are fixed (α=0.3, β=0.1, γ=0.3); deterministic.

---

## 2. Access & scope

```
isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')
```

| Caller scope | `root` returns | drill‑down |
|---|---|---|
| Republican / ALL | regions + `Jami` | region → district → organization |
| Region | districts + `Jami` | district → organization |
| District | organizations + `Jami` | (none below organization) |
| Single organization | that org's own row only, no `Jami` | none (`hasChildren = false`) |

`regionCode` / `districtCode` outside the caller's scope → `403`
(`organization.scope_violation`), same rule as every other report.

---

## 3. Common query params

`root`, `children`, `series` and `top-diseases` all share `bucket` /
`horizon` / `method` / `from` / `to`. `root` has no geography params;
`children`, `series` and `top-diseases` add `regionCode` / `districtCode`.
`diagnosisCode` is specific to `root`/`children`/`series` — `top-diseases`
has no such filter (it forecasts *every* code, that's the point) and instead
adds its own `limit` / `minCases` (see §6).

| Param | Type | Default | Notes |
|---|---|---|---|
| `regionCode` | string | — | `children` / `series` only. Omit for the caller's own level / whole scope. |
| `districtCode` | string | — | `children` / `series` only. |
| `diagnosisCode` | string | — | ICD‑10, matched against initial **or** final code. Case‑insensitive, trimmed. Omit = all diseases. |
| `bucket` | `DAY`\|`WEEK`\|`MONTH` | `WEEK` | Aggregation interval. |
| `horizon` | int (1–120) | `8` | Future buckets. Silently clamped to the unit max (90 / 52 / 24). |
| `method` | enum | `AUTO` | `AUTO`, `NAIVE_MEAN`, `SES`, `HOLT`, `HOLT_WINTERS_ADDITIVE`. |
| `from` | `YYYY-MM-DD` | *unit look‑back before `to`* | Training window start, inclusive. Snapped down to bucket start. |
| `to` | `YYYY-MM-DD` | today | Training window end, inclusive. The bucket containing `to` is the last history bucket. |

A training window longer than 520 buckets is trimmed to the **most recent** 520.

---

## 4. `GET /v1/reports/forecast/root` and `/children`

Response envelope: `{ "success": true, "message": "...", "data": [ /* rows */ ] }`.
`data` is a flat array — `root` ends with the `Jami` row, `children` never
has it.

### Row shape — `ForecastNodeResponse`

| Field | Meaning |
|---|---|
| `code` | Region/district code, organization id, `"UZ"` (republic root) or `"TOTAL"` (`Jami`). |
| `name` | Localized node name (`"Jami"` for the total row). |
| `hasChildren` | Show the expand arrow when `true`. `Jami` is always `false`. |
| `method` | Model actually used for **this node**. |
| `trainingTotal` | Σ observed notifications in this node's training window. |
| `lastActual` | Observed count of the last history bucket. |
| `nextPredicted` | Point forecast for the next (nearest) bucket. |
| `forecastTotal` | Σ point forecasts over the whole horizon. |
| `trendPerBucket` | Estimated level change per future bucket (slope). `> 0` rising. 2 decimals. |
| `alertBuckets` | Count of horizon buckets predicted above their endemic threshold. |
| `peakPeriodStart` | Start date of the highest‑`predicted` bucket, or `null`. |

The `Jami` row is the whole scope's own aggregated series, forecast as one —
**not** the sum of the region rows' `forecastTotal` (forecasting is not
linear), so treat it as its own figure.

### Example — republic caller, first screen

```
GET /v1/reports/forecast/root?bucket=WEEK&horizon=8
```
```json
{
  "success": true,
  "message": "Amal muvaffaqiyatli bajarildi.",
  "data": [
    { "code": "1703", "name": "Andijon viloyati", "hasChildren": true,
      "method": "HOLT_WINTERS_ADDITIVE", "trainingTotal": 3820, "lastActual": 41,
      "nextPredicted": 44, "forecastTotal": 372, "trendPerBucket": 0.6,
      "alertBuckets": 2, "peakPeriodStart": "2026-10-27" },
    { "code": "1706", "name": "Buxoro viloyati", "hasChildren": true,
      "method": "HOLT", "trainingTotal": 2110, "lastActual": 19,
      "nextPredicted": 20, "forecastTotal": 168, "trendPerBucket": 0.1,
      "alertBuckets": 0, "peakPeriodStart": "2026-09-15" },
    "… one row per region …",
    { "code": "TOTAL", "name": "Jami", "hasChildren": false,
      "method": "HOLT_WINTERS_ADDITIVE", "trainingTotal": 41200, "lastActual": 512,
      "nextPredicted": 530, "forecastTotal": 4360, "trendPerBucket": 3.1,
      "alertBuckets": 3, "peakPeriodStart": "2026-10-27" }
  ]
}
```

### Example — drill into a region

```
GET /v1/reports/forecast/children?regionCode=1703&bucket=WEEK&horizon=8
```
Returns one row per district of Andijon (same shape, no `Jami`).
`districtCode=1703215` returns that district's organizations.

---

## 5. `GET /v1/reports/forecast/series`

The chart data for **one** node. Same params as `children` (`regionCode` /
`districtCode` pick the node; omit both for the caller's whole scope — for a
republic caller, the whole republic).

Response `data` = `ForecastResponse`:

```json
{
  "summary": {
    "territoryCode": "1703", "territoryName": "Andijon viloyati",
    "diagnosisCode": "A09", "bucket": "WEEK",
    "trainingStart": "2024-09-02", "trainingEnd": "2026-09-07",
    "trainingBuckets": 105, "trainingTotal": 2840, "trainingMeanPerBucket": 27.05,
    "method": "HOLT_WINTERS_ADDITIVE", "trendPerBucket": 0.42,
    "forecastTotal": 174, "alertBuckets": 2, "peakPeriodStart": "2026-09-28"
  },
  "history": [
    { "periodStart": "2024-09-02", "periodEnd": "2024-09-08", "actual": 19 },
    "… gap-filled, one per ISO week …"
  ],
  "forecast": [
    { "periodStart": "2026-09-08", "periodEnd": "2026-09-14", "predicted": 26,
      "lowerBound": 15, "upperBound": 37, "endemicThreshold": 31, "alert": false },
    { "periodStart": "2026-09-15", "periodEnd": "2026-09-21", "predicted": 29,
      "lowerBound": 17, "upperBound": 41, "endemicThreshold": 28, "alert": true },
    "… 6 more …"
  ]
}
```

`summary` fields beyond the row shape: `trainingStart` / `trainingEnd` /
`trainingBuckets` / `trainingMeanPerBucket` describe the observed series;
`territoryCode` / `territoryName` echo the resolved node.

---

## 6. `GET /v1/reports/forecast/top-diseases`

Answers **"in this territory, which diseases are likely to increase, and how
risky is each one"** — the piece `root`/`children`/`series` deliberately
don't cover, since those are geography‑first and forecast at most one disease
at a time (`diagnosisCode`). This endpoint is disease‑first for a **single**
resolved node: every ICD‑10 code that appears in the node's training window
gets its **own independent forecast** (same models, same endemic channel as
`/series`), and the response is a risk‑ranked top list.

Same node‑selection params as `/series` (`regionCode` / `districtCode`, omit
both for the caller's whole scope) plus `bucket` / `horizon` / `method` /
`from` / `to`, plus two of its own:

| Param | Type | Default | Notes |
|---|---|---|---|
| `limit` | int (1–50) | `10` | How many diseases to return, highest risk first. |
| `minCases` | int ≥ 0 | `3` | A code with fewer than this many notifications in the *whole* training window is dropped before forecasting — too sparse a series to say anything useful about. |

### Row shape — `ForecastDiseaseRiskResponse`

| Field | Meaning |
|---|---|
| `diagnosisCode` | ICD‑10 code (current best known — final code if set, else initial). |
| `diagnosisName` | Localized disease name from the MKB‑10 catalog. Falls back to the raw code if the code isn't in the catalog. |
| `riskLevel` | `HIGH` \| `MEDIUM` \| `LOW` — see below. |
| `method`, `trainingTotal`, `lastActual`, `nextPredicted`, `forecastTotal`, `trendPerBucket`, `alertBuckets`, `peakPeriodStart` | Same meaning as the `ForecastNodeResponse` fields (§4) — but computed for **this one disease's own series**, not the whole node. |

**`riskLevel`** (derived per disease, not returned by any other endpoint):
- **`HIGH`** — at least one future bucket (`alertBuckets > 0`) is predicted
  above its endemic threshold.
- **`MEDIUM`** — no threshold breach, but the trend is still rising
  (`trendPerBucket > 0`).
- **`LOW`** — neither: flat or falling, no alert.

Sort order (already applied server‑side, no client re‑sort needed): `HIGH`
before `MEDIUM` before `LOW`; within a tier, higher `alertBuckets`, then
higher `trendPerBucket`, then higher `forecastTotal` first.

### Example

```
GET /v1/reports/forecast/top-diseases?regionCode=1703&bucket=WEEK&horizon=8&limit=5
```
```json
{
  "success": true,
  "message": "Amal muvaffaqiyatli bajarildi.",
  "data": [
    { "diagnosisCode": "A09", "diagnosisName": "Boshqa va noaniq kelib chiqishi bo'lgan gastroenterit va kolit",
      "riskLevel": "HIGH", "method": "HOLT", "trainingTotal": 612, "lastActual": 14,
      "nextPredicted": 19, "forecastTotal": 168, "trendPerBucket": 0.8,
      "alertBuckets": 3, "peakPeriodStart": "2026-10-06" },
    { "diagnosisCode": "J06", "diagnosisName": "Yuqori nafas yo'llarining o'tkir ko'p sohali va aniqlanmagan infeksiyalari",
      "riskLevel": "MEDIUM", "method": "HOLT_WINTERS_ADDITIVE", "trainingTotal": 4310, "lastActual": 51,
      "nextPredicted": 55, "forecastTotal": 460, "trendPerBucket": 0.3,
      "alertBuckets": 0, "peakPeriodStart": "2026-11-24" },
    "… up to `limit` rows, LOW‑risk ones last …"
  ]
}
```

### Notes

- One node at a time, by design — running this republic‑wide across every
  region in one call would mean forecasting every disease × every region,
  which is expensive for no real benefit (a national "top diseases" view is
  better read off the `Jami` node). Call it once for whichever node the user
  has drilled into (or the whole scope, if nothing is selected yet).
- Costs one grouped SQL query total (not one per disease) plus one forecast
  computation per code that survives the `minCases` filter — set `minCases`
  higher for a `DAY` bucket / long window if the call feels slow on a
  disease‑diverse node (a district, say, tends to have far fewer distinct
  codes than the whole republic).
- A code with **no** matching MKB‑10 catalog entry still appears —
  `diagnosisName` falls back to the raw `diagnosisCode`.

---

## 7. The screen

```
┌──────────────────────────────────────────────────────────────┐
│  FILTER BAR                                                   │
│  [Hudud ▾] [MKB-10 ▾] [Kun|Hafta|Oy] [Gorizont ▯] [from–to]   │
├──────────────────────────────────────────────────────────────┤
│  SUMMARY CARDS   (from the selected node's row, or /series summary) │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐                  │
│  │ Model  │ │ Trend  │ │ Jami   │ │ Alert  │                  │
│  └────────┘ └────────┘ └────────┘ └────────┘                  │
├──────────────────────────────────────────────────────────────┤
│  CHART   (/series → history[] + forecast[])                   │
│    ── observed   ‑‑ forecast   ▒ band   ┈ threshold   • alert │
├──────────────────────────────────────────────────────────────┤
│  TOP DISEASES   (/top-diseases for the selected node)         │
│    Kasallik | Xavf darajasi | Joriy | Prognoz | Trend         │
├──────────────────────────────────────────────────────────────┤
│  TERRITORY TABLE   (/root, then /children on expand)          │
│    Hudud | Model | Joriy | Prognoz(8) | Trend | Alert  [▸]    │
└──────────────────────────────────────────────────────────────┘
```

The **TOP DISEASES** panel is what answers "which diseases might increase
here, and how dangerous" for the currently selected node — it's the one
thing the chart and the territory table, on their own, don't show (the chart
is one series for the whole node; the table's `alertBuckets` is also for the
whole node, not broken down by disease).

### 7.1 Load sequence

1. On open, no territory selected → fire **all three**:
   - `GET /root` → the territory table (regions + `Jami`).
   - `GET /series` (no `regionCode`/`districtCode`) → the chart + summary
     cards for the whole scope (the republic, for a republic caller).
   - `GET /top-diseases` (same, no geography params) → the top‑diseases panel
     for the whole scope.
2. Expand a region row → `GET /children?regionCode={row.code}`; a district
   row → `+ &districtCode={row.code}`. Insert the returned rows under it.
3. Click a row (or a "ko'rish" action) → `GET /series?regionCode=…[&districtCode=…]`
   **and** `GET /top-diseases?regionCode=…[&districtCode=…]` in parallel →
   repaint the chart, summary cards and top‑diseases panel for that node.
   Keep the table as is.
4. Any filter‑bar change → refetch everything currently shown (`/root` +
   the open `/children` + the selected `/series` + the selected
   `/top-diseases`) with the new params. `diagnosisCode` doesn't apply to
   `/top-diseases` — leave it out of that one call.

### 7.2 Filter bar → params

| Control | Param | Notes |
|---|---|---|
| Hudud (cascading select) | `regionCode` / `districtCode` on `/series` (+ `/children`) | Also acts as the "selected node" for the chart. Empty = whole scope. |
| MKB‑10 (searchable, over `/v1/references/icd10`) | `diagnosisCode` | "Barcha kasalliklar" = send nothing. |
| Interval (segmented `Kun/Hafta/Oy`) | `bucket` | Default `WEEK`. Clamp the `horizon` input's `max` to 90/52/24 by unit. |
| Gorizont (number) | `horizon` | Default 8. |
| Davr (two date pickers) | `from` / `to` | `from` empty = default look‑back; `to` empty = today. |
| (advanced) Model | `method` | Leave on `AUTO`. |

### 7.3 Summary cards

From the selected node — the `/series` `summary`, or equivalently that node's
`/root`/`/children` row:

| Card | Value | Sub‑line |
|---|---|---|
| Model | `method` → label (`HOLT_WINTERS_ADDITIVE` → "Holt‑Winters (mavsumiy)", `HOLT` → "Chiziqli trend", `SES` → "Silliqlash", `NAIVE_MEAN` → "O'rtacha") | "avtomatik tanlandi" when the request sent `AUTO` |
| Trend | arrow from `sign(trendPerBucket)` + `Math.abs(trendPerBucket).toFixed(1)` | `bucket` unit; colour warning when `> 0` |
| Kutilayotgan jami | `forecastTotal` | "keyingi N " + unit |
| Chegaradan oshish | `alertBuckets` | `peakPeriodStart` formatted; colour danger when `> 0` |

### 7.4 Chart → `/series` `history[]` + `forecast[]`

One shared time axis; six Chart.js line datasets in this z‑order (recipe in §9):

| # | Dataset | Data (length = history + forecast) | Style |
|---|---|---|---|
| 1 | band upper | `nulls(H-1)`, `history[H-1].actual`, `forecast[].upperBound` | thin dashed, `fill: '+1'`, ~12% orange fill, `pointRadius: 0` |
| 2 | band lower | `nulls(H-1)`, `history[H-1].actual`, `forecast[].lowerBound` | thin dashed, no fill, `pointRadius: 0` |
| 3 | observed | `history[].actual`, then `nulls(F)` | 2px solid blue `#2a78d6`, `tension: 0.3` |
| 4 | forecast | `nulls(H-1)`, `history[H-1].actual`, `forecast[].predicted` | 2px dashed orange `#eb6834`, `spanGaps: true` |
| 5 | endemic threshold | `nulls(H)`, `forecast[].endemicThreshold` | 1.5px dashed grey `#898781`, `pointRadius: 0` |
| 6 | alert dots | per forecast bucket: `alert ? predicted : null` | `showLine: false`, `pointRadius: 5`, red `#e34948` |

The repeated `history[H-1].actual` seam point joins the blue line, the orange
line and the band — don't skip it. `y.beginAtZero = true`; hide vertical
gridlines; round every tooltip value. Colours are hard‑coded hex (canvas
can't read CSS vars) and work in both themes; never rely on colour alone —
the forecast line is also dashed, alerts are also a distinct shape.

### 7.5 Territory table → `/root` + `/children`

Columns: `name` · `method` (label) · `lastActual` ("Joriy") · `forecastTotal`
("Prognoz, N") · `trendPerBucket` (arrow) · `alertBuckets` (badge, danger
when `> 0`) · expand chevron where `hasChildren`. Pin `Jami` last, bold, no
chevron. A row's click opens its chart and top‑diseases panel (§7.1 step 3).

### 7.6 Top diseases panel → `/top-diseases`

A short ranked list (5–10 rows, `limit`) for the currently selected node —
the answer to "which diseases might increase here, how badly":

- Columns: `diagnosisName` (with `diagnosisCode` as a subtitle/tooltip) ·
  `riskLevel` as a coloured badge (`HIGH` = danger/red, `MEDIUM` =
  warning/yellow, `LOW` = neutral/grey — reuse the same colours as the
  `alertBuckets` badge in the territory table) · `lastActual` ("Joriy") ·
  `forecastTotal` ("Kutilayotgan jami") · `trendPerBucket` (arrow, same
  convention as the summary card in §7.3).
- Rows already arrive sorted by risk (§6) — render in response order, don't
  re‑sort client‑side.
- Row click → set `diagnosisCode` in the filter bar to that row's code and
  refetch `/series` (**not** `/top-diseases`) — this switches the chart to
  that one disease's own history/forecast, using the existing single‑disease
  flow (§1, §5). The top‑diseases panel itself keeps showing all diseases for
  the node until the node changes.
- Empty state (no code clears `minCases`, or the node has no cases at all):
  show "Ushbu davrda xavf ostida kasallik aniqlanmadi" rather than an empty
  table.

### 7.7 Export

Excel/PDF is built **client‑side** from the table rows + the selected
`/series` (+ `/top-diseases`, if included). There is no server export
endpoint for this report.

---

## 8. Edge cases & caveats

- **Empty node / no cases** → `series.history[]` is still fully gap‑filled
  with `actual = 0`; forecast is a flat 0 line, `method = NAIVE_MEAN`, no
  alerts. `root`/`children` still return the row, all figures 0.
- **`Jami` ≠ Σ rows.** Forecasting is non‑linear; the `Jami` row forecasts
  the whole‑scope series directly. Region `forecastTotal`s won't add up to
  it — don't reconcile them.
- **Model varies by row.** A small district with a short/sparse series gets
  `HOLT`/`SES`/`NAIVE_MEAN` while the republic gets `HOLT_WINTERS_ADDITIVE`.
  Show the per‑row `method` so users understand why bands differ in width.
- **`created_at`, not onset.** By registration date, matching every other
  report — reporting lag means the **last one or two buckets can be
  undercounted**. Set `to` a bucket or two before today for a clean tail.
- **`final_icd10_code` drifts.** The ICD‑10 filter re‑evaluates against the
  *current* code every call; a historical bucket's `actual` for a given
  `diagnosisCode` is not frozen.
- **Reporting coverage changes.** Organizations onboarding mid‑history
  depress the early series — consider `from` after coverage stabilised.
- **Fixed smoothing constants**, no MLE tuning: a decision‑support baseline
  (endemic channel + smoothing), not a transmission model (no Rt, no SEIR).
- Horizon beyond the unit max is **clamped, not rejected**.
- A republic‑wide `/root` with `bucket=DAY` and a long window is the
  heaviest call in this report (per‑org × per‑day rows folded up the
  hierarchy) — prefer `WEEK`/`MONTH` for the national view.
- **`/top-diseases` risk labels are per‑disease, per‑node — not comparable
  across nodes at a glance.** A district's `HIGH` and a region's `HIGH` are
  both "above that series' own endemic threshold," not the same absolute
  case count — don't build a cross‑territory ranking by counting `HIGH`
  labels without also showing the underlying numbers.
- **`minCases` can hide a real but rare/emerging disease.** The default (3)
  is a noise filter, not a safety threshold — for a small organization‑level
  node, consider surfacing a "0" `minCases` toggle so a first‑ever case of
  something new isn't silently dropped from the list.

---

## 9. Chart.js dataset recipe (`/series`)

```js
function buildForecastChart(canvas, data) {
  const { history, forecast } = data;
  const H = history.length, F = forecast.length;
  const nulls = n => Array(n).fill(null);
  const seam = H > 0 ? history[H - 1].actual : 0;

  const labels = [...history, ...forecast].map(p => p.periodStart);

  const upper     = [...nulls(H - 1), seam, ...forecast.map(p => p.upperBound)];
  const lower     = [...nulls(H - 1), seam, ...forecast.map(p => p.lowerBound)];
  const observed  = [...history.map(p => p.actual), ...nulls(F)];
  const predicted = [...nulls(H - 1), seam, ...forecast.map(p => p.predicted)];
  const threshold = [...nulls(H), ...forecast.map(p => p.endemicThreshold)];
  const alerts    = [...nulls(H), ...forecast.map(p => (p.alert ? p.predicted : null))];

  return new Chart(canvas, {
    type: 'line',
    data: { labels, datasets: [
      { data: upper, borderColor: 'rgba(235,104,52,.35)', borderWidth: 1, borderDash: [3,3],
        pointRadius: 0, fill: '+1', backgroundColor: 'rgba(235,104,52,.12)', spanGaps: true },
      { data: lower, borderColor: 'rgba(235,104,52,.35)', borderWidth: 1, borderDash: [3,3],
        pointRadius: 0, fill: false, spanGaps: true },
      { label: 'Kuzatilgan', data: observed, borderColor: '#2a78d6', borderWidth: 2,
        pointRadius: 0, tension: .3 },
      { label: 'Prognoz', data: predicted, borderColor: '#eb6834', borderWidth: 2,
        borderDash: [6,4], pointRadius: 0, tension: .3, spanGaps: true },
      { label: 'Epidemik chegara', data: threshold, borderColor: '#898781', borderWidth: 1.5,
        borderDash: [2,3], pointRadius: 0, spanGaps: true },
      { label: 'alert', data: alerts, showLine: false, pointRadius: 5, backgroundColor: '#e34948' },
    ]},
    options: {
      responsive: true, maintainAspectRatio: false,
      plugins: { legend: { display: false },
                 tooltip: { mode: 'index', intersect: false,
                            callbacks: { label: c => `${c.dataset.label}: ${Math.round(c.parsed.y)}` } } },
      interaction: { mode: 'index', intersect: false },
      scales: { x: { grid: { display: false } }, y: { beginAtZero: true } },
    },
  });
}
```

Build a custom HTML legend (small squares + labels) above the canvas.
Alert count and peak come from `summary`, not from recomputing over
`forecast[]`.
