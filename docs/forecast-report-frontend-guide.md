# «Forecast» report — frontend integration guide

Surveillance **forecasting** over `form058` + `form058_1`. Unlike every
other report this is **not** a paged geography drill-down — it is a single
call that:

1. resolves **one** geography node (the caller's whole access scope, or an
   explicit `regionCode`/`districtCode` inside it — validated exactly like
   the other reports via `report.shared.ReportHierarchyService`),
2. pulls that node's whole sub-tree as one **time series** bucketed by
   `DAY` / `WEEK` / `MONTH` over the training window,
3. extrapolates it `horizon` buckets ahead (exponential smoothing / Holt /
   Holt-Winters, auto-selected), with a ~95% prediction band and the
   classical **endemic-channel** epidemic threshold.

| | |
|---|---|
| Title | «Kasallanish prognozi» |
| Base path | `GET /v1/reports/forecast` |
| Swagger tag | `Report — Forecast` |
| Access | `isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')` |

---

## 1. What the numbers mean

Every count is over `form058` + `form058_1` (unioned), `deleted = false`,
`sender_organization_id` inside the resolved geography node, bucketed by
**`created_at`** in `Asia/Tashkent`.

**Case set is wider than the confirmed-count reports (Form 8/10/11/12/13):**
every *live* notification is counted — `status <> 'CANCELED'`, i.e. primary
**and** confirmed alike. A forecast of disease burden must not wait for
final approval. Consequently the ICD-10 filter matches the **initial
`icd10_code` OR the final `final_icd10_code`**, not the final code alone.

- **`actual`** — observed notifications in a past bucket (`history[]`).
- **`predicted`** — point forecast for a future bucket (`forecast[]`),
  rounded to a non-negative integer.
- **`lowerBound` / `upperBound`** — ~95% prediction interval
  (`point ± 1.96·σ·√step`, where σ is the model's in-sample one-step error;
  `lowerBound` clamped at 0).
- **`endemicThreshold`** — classical epidemic threshold for that **seasonal
  moment**: `mean + 1.96·SD` over the history of the *same* month-of-year
  (MONTH), ISO-week-of-year (WEEK) or day-of-week (DAY). Phases with < 2
  historical observations fall back to a single global threshold.
- **`alert`** — `predicted > endemicThreshold` for that bucket: a signal
  that the forecast rises above the usual seasonal ceiling.

### Bucketing

| `bucket` | `periodStart` is | Seasonality | Default look-back (`from` omitted) | Max `horizon` |
|---|---|---|---|---|
| `DAY` | that day | day-of-week (7) | 180 days | 90 |
| `WEEK` *(default)* | Monday of the ISO week | week-of-year (52) | 104 weeks | 52 |
| `MONTH` | 1st of the month | month-of-year (12) | 36 months | 24 |

The series is **gap-filled**: buckets with no cases are present with
`actual = 0`, so `history[]` is a contiguous, evenly-spaced array.

### Model selection (`method = AUTO`, the default)

| Condition | Model used |
|---|---|
| < 3 history buckets | `NAIVE_MEAN` (flat line at the running mean) |
| ≥ 2 full seasonal cycles (`n ≥ 2 × seasonLength`) | `HOLT_WINTERS_ADDITIVE` (level + trend + season) |
| ≥ 4 buckets | `HOLT` (level + linear trend) |
| otherwise | `SES` (level only) |

Pass `method` explicitly (`NAIVE_MEAN` / `SES` / `HOLT` /
`HOLT_WINTERS_ADDITIVE`) to force one. `summary.method` always reports the
model **actually** used (never `AUTO`).

Smoothing constants are fixed (α=0.3, β=0.1, γ=0.3) — no per-series tuning;
the forecast is deterministic for a given series.

---

## 2. Request

`GET /v1/reports/forecast`

| Query param | Type | Default | Notes |
|---|---|---|---|
| `regionCode` | string | — | Omit for the caller's whole access scope. |
| `districtCode` | string | — | With `districtCode` the node is that district's whole sub-tree. |
| `diagnosisCode` | string | — | ICD-10 (КХК-10) code; matched against initial **or** final code. Case-insensitive, trimmed. Omit for all diseases. |
| `bucket` | `DAY` \| `WEEK` \| `MONTH` | `WEEK` | Aggregation interval. |
| `horizon` | int (1–120) | `8` | Future buckets to predict. Silently clamped to the unit max (90 / 52 / 24). |
| `method` | enum | `AUTO` | `AUTO`, `NAIVE_MEAN`, `SES`, `HOLT`, `HOLT_WINTERS_ADDITIVE`. |
| `from` | `YYYY-MM-DD` | *unit look-back before `to`* | Training window start, inclusive. Snapped down to the bucket start. |
| `to` | `YYYY-MM-DD` | today | Training window end, inclusive. The bucket containing `to` is the last history bucket. |

- `regionCode` / `districtCode` outside the caller's scope → `403`
  (`organization.scope_violation`), same rule as every other report.
- A training window longer than 520 buckets is trimmed to the **most
  recent** 520.

---

## 3. Response

```json
{ "success": true, "message": "...", "data": { "summary": {…}, "history": [ … ], "forecast": [ … ] } }
```

### `summary`

| Field | Meaning |
|---|---|
| `territoryCode` / `territoryName` | Resolved geography node (`"UZ"` for the republic root). |
| `diagnosisCode` | Echo of the normalized filter, or `null`. |
| `bucket` | `DAY` / `WEEK` / `MONTH`. |
| `trainingStart` / `trainingEnd` | Span of `history[]` (dates, inclusive). |
| `trainingBuckets` | `history.length`. |
| `trainingTotal` | Σ `actual` over the history. |
| `trainingMeanPerBucket` | `trainingTotal / trainingBuckets`, 2 decimals. |
| `method` | Model actually used. |
| `trendPerBucket` | Estimated level change per future bucket (slope). `> 0` rising, `< 0` falling, `≈ 0` flat. 2 decimals. |
| `forecastTotal` | Σ `predicted` over the horizon. |
| `alertBuckets` | Count of forecast buckets with `alert = true`. |
| `peakPeriodStart` | Start date of the highest-`predicted` bucket, or `null` if horizon empty. |

### `history[]` — oldest first

```json
{ "periodStart": "2026-06-01", "periodEnd": "2026-06-07", "actual": 14 }
```

### `forecast[]` — nearest first

```json
{
  "periodStart": "2026-09-07", "periodEnd": "2026-09-13",
  "predicted": 21, "lowerBound": 12, "upperBound": 30,
  "endemicThreshold": 25, "alert": false
}
```

### Example

```
GET /v1/reports/forecast?regionCode=1703&diagnosisCode=A09&bucket=WEEK&horizon=6&from=2024-01-01&to=2026-09-05
```
```json
{
  "success": true,
  "message": "Amal muvaffaqiyatli bajarildi.",
  "data": {
    "summary": {
      "territoryCode": "1703", "territoryName": "Andijon viloyati",
      "diagnosisCode": "A09", "bucket": "WEEK",
      "trainingStart": "2024-01-01", "trainingEnd": "2026-09-07",
      "trainingBuckets": 141, "trainingTotal": 3820, "trainingMeanPerBucket": 27.09,
      "method": "HOLT_WINTERS_ADDITIVE", "trendPerBucket": 0.42,
      "forecastTotal": 174, "alertBuckets": 2, "peakPeriodStart": "2026-09-28"
    },
    "history": [
      { "periodStart": "2024-01-01", "periodEnd": "2024-01-07", "actual": 19 },
      "… 140 more, gap-filled, one per ISO week …"
    ],
    "forecast": [
      { "periodStart": "2026-09-08", "periodEnd": "2026-09-14", "predicted": 26, "lowerBound": 15, "upperBound": 37, "endemicThreshold": 31, "alert": false },
      { "periodStart": "2026-09-15", "periodEnd": "2026-09-21", "predicted": 29, "lowerBound": 17, "upperBound": 41, "endemicThreshold": 28, "alert": true },
      "… 4 more …"
    ]
  }
}
```

---

## 4. The screen

One screen, one call. Three stacked regions, top to bottom:

```
┌──────────────────────────────────────────────────────────────┐
│  FILTER BAR                                                   │
│  [Hudud ▾] [MKB-10 ▾] [Kun|Hafta|Oy] [Gorizont ▯] [from–to]   │
├──────────────────────────────────────────────────────────────┤
│  SUMMARY CARDS  (from `summary`)                              │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐                  │
│  │ Model  │ │ Trend  │ │ Jami   │ │ Alert  │                  │
│  └────────┘ └────────┘ └────────┘ └────────┘                  │
├──────────────────────────────────────────────────────────────┤
│  CHART  (history[] + forecast[])                              │
│    ── observed    ‑‑ forecast    ▒ band    ┈ threshold   • alert │
├──────────────────────────────────────────────────────────────┤
│  TABLE  (forecast[], optional)                                │
└──────────────────────────────────────────────────────────────┘
```

### 4.1 Filter bar → request params

| Control | Type | Param | Notes |
|---|---|---|---|
| Hudud | cascading select (viloyat → tuman) | `regionCode` / `districtCode` | Empty = caller's whole scope. A region/district-scoped caller can hide the levels above them. |
| MKB-10 | searchable select over `/v1/references/icd10` | `diagnosisCode` | "Barcha kasalliklar" option = send nothing. |
| Interval | 3-way segmented `Kun / Hafta / Oy` | `bucket` = `DAY` / `WEEK` / `MONTH` | Default `WEEK`. |
| Gorizont | number input | `horizon` | Default 8. Clamp the input's `max` to 90 / 52 / 24 by the current `bucket` so the user can't ask for more than the server returns. |
| Davr | two date pickers | `from` / `to` | Leave `from` empty for the default look-back. `to` empty = today. |
| (advanced) Model | select, collapsed by default | `method` | Leave on `AUTO`. Only surface `SES` / `HOLT` / `HOLT_WINTERS_ADDITIVE` / `NAIVE_MEAN` for power users. |

Any change → debounce ~300 ms → refetch → re-render the whole screen. No
per-widget calls; there is only one endpoint.

### 4.2 Summary cards → `summary`

| Card | Value | Sub-line |
|---|---|---|
| Model | `method` mapped to a label (`HOLT_WINTERS_ADDITIVE` → "Holt-Winters (mavsumiy)", `HOLT` → "Chiziqli trend", `SES` → "Silliqlash", `NAIVE_MEAN` → "O'rtacha") | "avtomatik tanlandi" when the request sent `AUTO` |
| Trend | arrow from `sign(trendPerBucket)` (`↑` / `↓` / `→`) + `Math.abs(trendPerBucket).toFixed(1)` | `bucket` unit ("hafta / interval"); colour warning when `> 0` |
| Kutilayotgan jami | `forecastTotal` | "keyingi N " + unit, N = `forecast.length` |
| Chegaradan oshish | `alertBuckets` | `peakPeriodStart` formatted ("cho'qqi 24-noy"); colour danger when `alertBuckets > 0` |

Optionally a 5th card "O'rganilgan davr" — `trainingBuckets` buckets,
`trainingStart … trainingEnd`, `trainingMeanPerBucket` o'rtacha.

### 4.3 Chart → `history[]` + `forecast[]`

One shared time axis: `x = [...history.map(p => p.periodStart), ...forecast.map(p => p.periodStart)]`.

Six datasets (Chart.js line chart), in this z-order:

| # | Dataset | Data (length = history + forecast) | Style |
|---|---|---|---|
| 1 | band upper | `nulls(H-1)`, then `history[H-1].actual`, then `forecast[].upperBound` | thin dashed, `fill: '+1'`, ~12% orange fill, `pointRadius: 0` |
| 2 | band lower | `nulls(H-1)`, then `history[H-1].actual`, then `forecast[].lowerBound` | thin dashed, no fill, `pointRadius: 0` |
| 3 | observed | `history[].actual`, then `nulls(F)` | 2px solid blue `#2a78d6`, `tension: 0.3` |
| 4 | forecast | `nulls(H-1)`, then `history[H-1].actual`, then `forecast[].predicted` | 2px dashed orange `#eb6834`, `tension: 0.3`, `spanGaps: true` |
| 5 | endemic threshold | `nulls(H)`, then `forecast[].endemicThreshold` | 1.5px dashed grey `#898781`, `pointRadius: 0` |
| 6 | alert dots | for each forecast bucket: `alert ? predicted : null` (nulls over history) | `showLine: false`, `pointRadius: 5`, red `#e34948` |

The single repeated `history[H-1].actual` point at the seam is what visually
joins the blue line, the orange line and the band — don't skip it.

`y.beginAtZero = true`; hide vertical gridlines; `tooltip.mode = 'index'`.
Round every tooltip value (`Math.round`).

Colours are hard-coded hex (canvas can't read CSS vars); the set above works
in light and dark. Never rely on colour alone — the forecast line is also
dashed, the alert points are also a distinct shape.

### 4.4 Table → `forecast[]` (optional)

One row per `forecast[]` entry: `periodStart`–`periodEnd`, `predicted`,
`lowerBound`–`upperBound`, `endemicThreshold`, and a status chip
(`alert` → "chegaradan oshgan", danger; else "me'yorda", muted).

### 4.5 Export

Excel/PDF is built **client-side** from `history` + `forecast` + `summary`.
There is no server export endpoint for this report (unlike Form 058/058-1).

---

## 5. Edge cases & caveats

- **Empty node / no cases** → `history[]` is still fully gap-filled with
  `actual = 0`; the forecast is a flat 0 line with `method = NAIVE_MEAN`
  and no alerts.
- **Short history** (a young organization, a narrow `from`/`to`) → `AUTO`
  degrades to `HOLT` / `SES` / `NAIVE_MEAN`; the prediction band widens
  (fewer residuals to estimate σ from), and `HOLT_WINTERS_ADDITIVE`
  requested explicitly on too-short data silently falls back to `HOLT`.
- **`created_at`, not onset.** The series is by registration date, matching
  every other report — reporting lag means the **last one or two buckets
  can be undercounted**. For a clean forecast set `to` a bucket or two
  before today, or read `predicted` for the near term with that in mind.
- **`final_icd10_code` drifts.** A case's confirmed code can change after
  the fact; the ICD-10 filter re-evaluates against the *current* code every
  call, so a historical bucket's `actual` for a given `diagnosisCode` is
  not frozen.
- **Reporting coverage changes.** If organizations came online mid-history,
  the earlier part of the series is structurally lower — consider setting
  `from` after the coverage stabilised.
- **Fixed smoothing constants.** No MLE tuning; treat this as a decision-support
  baseline (endemic channel + smoothing), not an epidemiological model of
  transmission (no Rt, no SEIR).
- Horizon beyond the unit max is **clamped, not rejected** — request 200
  weeks, get 52.

---

## 6. Chart.js dataset recipe

```js
function buildForecastChart(canvas, data) {
  const { history, forecast } = data;
  const H = history.length, F = forecast.length;
  const nulls = n => Array(n).fill(null);
  const seam = H > 0 ? history[H - 1].actual : 0;

  const labels = [...history, ...forecast].map(p => p.periodStart);

  const upper   = [...nulls(H - 1), seam, ...forecast.map(p => p.upperBound)];
  const lower   = [...nulls(H - 1), seam, ...forecast.map(p => p.lowerBound)];
  const observed = [...history.map(p => p.actual), ...nulls(F)];
  const predicted = [...nulls(H - 1), seam, ...forecast.map(p => p.predicted)];
  const threshold = [...nulls(H), ...forecast.map(p => p.endemicThreshold)];
  const alerts = [...nulls(H), ...forecast.map(p => (p.alert ? p.predicted : null))];

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
      scales: { x: { grid: { display: false } },
                y: { beginAtZero: true } },
    },
  });
}
```

Build a custom HTML legend (small squares + labels) above the canvas —
`Chart.js` default legend markers don't match the "band / dashed / dotted"
visual language. Alert count and peak come from `summary`, not from
recomputing over `forecast[]`.
