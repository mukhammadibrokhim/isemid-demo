# «Forecast» report — how the prognosis is actually computed

Companion to [forecast-report-frontend-guide.md](forecast-report-frontend-guide.md),
which documents the HTTP contract. This doc documents the **math** behind
`ForecastReportQueryService` — where the numbers in `predicted` /
`lowerBound` / `upperBound` / `endemicThreshold` / `alert` / `riskLevel`
actually come from, step by step, with a worked numeric example.

Everything here is self-contained arithmetic in
[`TimeSeriesForecaster`](../src/main/java/uz/uzinfocom/app/modules/report/forecast/application/query/forecasting/TimeSeriesForecaster.java)
and
[`EndemicChannel`](../src/main/java/uz/uzinfocom/app/modules/report/forecast/application/query/forecasting/EndemicChannel.java)
— no external forecasting library, no ML model, no calls out. Both classes
are pure functions over a `double[]` and are unit-tested directly in
[`TimeSeriesForecasterTest`](../src/test/java/uz/uzinfocom/app/modules/report/forecast/TimeSeriesForecasterTest.java).

---

## 1. Pipeline overview

For any one node (a region, a district, an organization, or the whole
republic) and any one disease filter, `ForecastReportQueryService.compute()`
does exactly this:

```
1. Query      -> raw notification counts per bucket (form058 + form058_1)
2. Gap-fill   -> every bucket in the training window gets a count, 0 if none
3. Forecast   -> TimeSeriesForecaster.forecast(history, horizon, seasonLength, method)
                 -> a point estimate + a ±band for each of the next `horizon` buckets
4. Threshold  -> EndemicChannel.from(history grouped by "time of year")
                 -> a per-season alert line
5. Assemble   -> predicted vs threshold -> alert; point deltas -> trend;
                 max(predicted) -> peak
```

Steps 1-2 happen in `ForecastReportQueryService.seriesFor` /
`countByBucketGroupedByDiagnosis` (SQL `date_trunc` + gap-fill by index
lookup). Steps 3-5 happen once per node/disease in `compute()`. Everything
below is about steps 3 and 4.

---

## 2. The four models

`TimeSeriesForecaster` implements four classical **exponential-smoothing**
style extrapolation methods, in increasing order of what they can capture:

| Model | Captures | Needs |
|---|---|---|
| `NAIVE_MEAN` | Nothing but the average level | any history, even length 1 |
| `SES` (simple exponential smoothing) | A level that drifts slowly | a few points |
| `HOLT` | Level **+ linear trend** (rising/falling) | ≥ 4 points, ideally |
| `HOLT_WINTERS_ADDITIVE` | Level + trend **+ repeating seasonal shape** | ≥ 2 full seasonal cycles |

All four are **deterministic** — same input always gives the same output —
and all four run in O(n) with no iterative optimisation (no
maximum-likelihood fitting of the smoothing constants; see §2.5).

### 2.1 `NAIVE_MEAN` — the floor

```
level = average of all observed counts
forecast for every future bucket = level   (flat line)
```

This is what a series with fewer than 3 history buckets gets, and also the
name for the plain historical average used as an anchor by the others.
There's no way to say anything about a trend from 1-2 points, so the model
doesn't try.

### 2.2 `SES` — level only

Simple exponential smoothing keeps a running **level** that is nudged toward
each new observation by a fixed fraction `α = 0.3`:

```
level₀ = y₀                              (first observed count)
for each new observation yᵢ (i ≥ 1):
    errorᵢ = yᵢ - levelᵢ₋₁               (how wrong the last level was)
    levelᵢ = levelᵢ₋₁ + α · errorᵢ        (nudge the level toward reality)

forecast for every future bucket = levelₙ₋₁   (flat line at the final level)
```

`α = 0.3` means the level moves 30% of the way toward each new data point
and keeps 70% of its previous belief — smooths out single-bucket noise
without overreacting, but (deliberately) has **no concept of an upward or
downward trend**: the forecast is flat.

### 2.3 `HOLT` — level + trend

Holt's linear method adds a second smoothed quantity, the **trend**
(`β = 0.1`), so the forecast can keep rising or falling instead of going
flat:

```
level₀ = y₀
trend₀ = y₁ - y₀                                   (first observed slope)

for each new observation yᵢ (i ≥ 1):
    predᵢ    = levelᵢ₋₁ + trendᵢ₋₁                  (one-step-ahead guess)
    errorᵢ   = yᵢ - predᵢ
    levelᵢ   = predᵢ + α · errorᵢ
    trendᵢ   = trendᵢ₋₁ + β · (α · errorᵢ)

forecast h buckets ahead = levelₙ₋₁ + h · trendₙ₋₁   (a straight line)
```

This is the workhorse for most district/organization-level series: enough
data to detect a rising or falling trend, not enough for a reliable
seasonal cycle.

### 2.4 `HOLT_WINTERS_ADDITIVE` — level + trend + season

Holt-Winters adds a third smoothed component, one value per **seasonal
phase** (`γ = 0.3`) — e.g. for `bucket=WEEK` this is 52 numbers, one per
ISO week-of-year, that say "this time of year tends to run N cases above/
below the trend line."

```
m = seasonLength   (7 for DAY, 52 for WEEK, 12 for MONTH)

initial level  = mean of the first full cycle (bucket 0..m-1)
initial trend  = (mean of 2nd cycle - mean of 1st cycle) / m
initial season[s] = y[s] - initial level     for s = 0..m-1

for each t from m to n-1:
    s = t mod m                              (which phase this bucket is)
    predₜ  = levelₜ₋₁ + trendₜ₋₁ + season[s]
    errorₜ = yₜ - predₜ

    newLevel  = α · (yₜ - season[s]) + (1-α) · (levelₜ₋₁ + trendₜ₋₁)
    newTrend  = β · (newLevel - levelₜ₋₁) + (1-β) · trendₜ₋₁
    season[s] = γ · (yₜ - newLevel) + (1-γ) · season[s]
    levelₜ = newLevel; trendₜ = newTrend

forecast h buckets ahead = levelₙ₋₁ + h·trendₙ₋₁ + season[(n-1+h) mod m]
```

Only runs when there's at least **2 full cycles** of history
(`n ≥ 2m` — e.g. 104 weeks for `WEEK`); otherwise `TimeSeriesForecaster`
silently falls back to `HOLT` (see `holtWinters()`, which calls `holt()`
directly when the seasonal precondition isn't met).

### 2.5 Fixed smoothing constants — why no auto-tuning

`α = 0.3`, `β = 0.1`, `γ = 0.3` are **not fitted per series** (no grid
search / maximum-likelihood optimisation over historical error). They're
fixed textbook "reasonable without tuning" defaults, chosen so:

- every call stays O(n) and instant, even for a republic-wide `DAY`-bucket
  query;
- the result is exactly reproducible — same inputs, same output, every
  time, which matters for an "explain this alert" conversation with an
  epidemiologist;
- there's no risk of an over-fit model swinging wildly for a
  disease/territory with a short or noisy series.

The trade-off: the model won't adapt its smoothness to how noisy a
*specific* series is — a genuinely more volatile disease gets the same
`α`/`β`/`γ` as a stable one. This is a decision-support baseline, not a
tuned production forecasting service (see the frontend guide's Notes for
the same caveat from the API-consumer side).

---

## 3. Choosing the model — `AUTO`

`resolveAuto()` (`TimeSeriesForecaster.java:71`) is the default (`method`
param omitted or explicitly `AUTO`) and runs this exact ladder:

```
n = number of history buckets, m = seasonLength (7/52/12)

n < 3                    -> NAIVE_MEAN
seasonLength ≥ 2 AND n ≥ 2m  -> HOLT_WINTERS_ADDITIVE
n ≥ 4                    -> HOLT
otherwise                -> SES
```

The model is picked **independently per node and per disease** — a
republic (long, dense history) can get `HOLT_WINTERS_ADDITIVE` while a
small district or a rare disease code (short/sparse history) gets `SES` or
even `NAIVE_MEAN` for the *same* API call. The response always echoes back
which model actually ran (`method` field) — there is no silent guessing on
the client side.

---

## 4. The prediction band (`lowerBound` / `upperBound`)

Every model above produces only a **point** forecast. The ± band comes from
a separate, model-agnostic step in `forecast()`:

```
σ = residualStdDev(oneStepErrors, history)

for h = 1..horizon:
    point[h] = max(0, model's forecast for step h)
    band     = 1.96 · σ · √h
    lower[h] = max(0, point[h] - band)
    upper[h] =        point[h] + band
```

- `σ` is the **sample standard deviation of the model's own in-sample
  one-step-ahead errors** — literally the `errorᵢ` values accumulated while
  fitting the model above (residualStdDev needs ≥ 2 errors to compute a
  real spread). This is "how wrong was this exact model, one step at a
  time, on data it just saw" — not a generic assumption.
- **Fallback**: if there are fewer than 2 errors (a length-1 series, or the
  degenerate case where the computed SD is 0/NaN), it falls back to a
  Poisson-style `√(mean count)`, floored at `1.0` so a dead-flat history
  still produces a *visible* (non-zero-width) band instead of a fake
  "perfectly certain" point line.
- **`1.96`** is the standard z-score for a ~95% interval under a normal
  approximation.
- **`√h`** — the band widens with the square root of how many steps ahead
  you're forecasting (uncertainty compounds over a random-walk-style
  horizon), so bucket 8 has a visibly wider band than bucket 1.
- Everything is clamped at 0 — a disease count can't be negative.

---

## 5. The endemic (epidemic-threshold) channel

`predicted > threshold` is what actually sets `alert = true`. The threshold
is **not** a fixed number — it's computed **per seasonal phase**, in
`EndemicChannel.from()`, using the same classical WHO/CDC "endemic channel"
idea used in real epidemiological surveillance:

```
for each seasonal phase s (month 1-12 / ISO week 1-53 / weekday 1-7):
    values = every historical bucket that fell in phase s across all years
    if values.size ≥ 2:
        threshold[s] = mean(values) + 1.96 · stddev(values)
    else:
        threshold[s] = global fallback (see below)

global fallback = mean(all history) + 1.96 · sqrt(mean(all history))
                  (only used when there's no per-phase estimate at all)
```

In plain words: *"for this exact time of year (say, ISO week 34, or the
month of August, or Mondays), what's the historically normal ceiling? Mean
plus roughly two standard deviations."* A forecast bucket landing above
**its own season's** ceiling is what triggers the red alert dot on the
chart — this correctly treats "expected winter respiratory-disease bump" as
normal and only flags a bucket that's unusual **for that time of year**,
not unusual in absolute terms.

`seasonIndex()` (`ForecastBucketUnit.java:97`) is what maps a bucket's
`LocalDate` to its phase: `DAY` → day-of-week, `WEEK` → ISO week-of-year,
`MONTH` → month-of-year. This is the same key both Holt-Winters' seasonal
component and the endemic channel use, so "the same time of year" lines up
identically across both.

---

## 6. What each derived field actually is

Computed once per node/disease in `ForecastReportQueryService.compute()`,
from the raw model output + the endemic channel:

| Field | Formula |
|---|---|
| `predicted` | `round(point[h])`, clamped ≥ 0 |
| `lowerBound` / `upperBound` | see §4, rounded |
| `endemicThreshold` | `ceil(channel.thresholdFor(seasonIndex(bucket)))` |
| `alert` | `predicted > endemicThreshold` |
| `forecastTotal` | `Σ predicted` over the whole horizon |
| `alertBuckets` | count of horizon buckets where `alert = true` |
| `peakPeriodStart` | the bucket with the single highest `predicted` |
| `trendPerBucket` | `(point[last] - point[first]) / (horizon - 1)` — the average slope **of the forecast line itself**, not a re-estimate of the historical trend |

`trendPerBucket` for a 1-bucket horizon is just `point[0] - lastActual`
(`ForecastReportQueryService.trendPerBucket`) since there's no line to take
a slope of.

---

## 7. "Top diseases" risk ranking (`/top-diseases`)

Same forecasting machinery, run **once per ICD-10 code** seen in the node's
training window (each code's own independent history → its own model
selection → its own endemic channel), then labelled and sorted:

```
riskLevel:
    alertBuckets > 0          -> HIGH    (forecast breaches its own endemic
                                           threshold at least once)
    else trendPerBucket > 0   -> MEDIUM  (no breach, but still rising)
    else                      -> LOW     (flat or falling, no breach)

sort: HIGH > MEDIUM > LOW,
      then by alertBuckets desc,
      then by trendPerBucket desc,
      then by forecastTotal desc
```

Codes with fewer than `minCases` (default 3) total notifications in the
*whole* training window are dropped **before** forecasting — too sparse a
series (often 1-2 points) to say anything meaningful with any of the four
models, and it would otherwise be `NAIVE_MEAN` for almost every rare code.

**Important nuance repeated from the frontend guide**: `HIGH` for a small
district and `HIGH` for the whole republic are not the same absolute case
count — each is "above *that series'* own seasonal ceiling," so risk labels
aren't comparable across geography without also looking at the raw numbers.

---

## 8. Worked example (by hand)

Take a tiny weekly series, `horizon = 3`, `bucket = WEEK` (`seasonLength =
52`, so no seasonal model qualifies — `n = 6 < 2×52`):

```
history (weekly counts): [10, 12, 11, 15, 14, 18]
```

`resolveAuto`: `n = 6 ≥ 4` → **`HOLT`**.

**Fitting Holt** (`α = 0.3, β = 0.1`):

```
level₀ = 10, trend₀ = 12 - 10 = 2

i=1 (y=12): pred=12,   err=0,    level=12+0.3·0=12.0,  trend=2+0.1·0=2.0
i=2 (y=11): pred=14.0, err=-3.0, level=14.0-0.9=13.1,  trend=2.0-0.09=1.91
i=3 (y=15): pred=15.01,err=-0.01,level≈15.0,           trend≈1.909
i=4 (y=14): pred≈16.91,err≈-2.91,level≈16.04,          trend≈1.822
i=5 (y=18): pred≈17.86,err≈0.14, level≈17.90,          trend≈1.836
```

One-step errors: `[0, -3.0, -0.01, -2.91, 0.14]` (5 values).

**Forecast** (`level ≈ 17.90`, `trend ≈ 1.836`):

```
h=1: 17.90 + 1×1.836 ≈ 19.74 → predicted = 20
h=2: 17.90 + 2×1.836 ≈ 21.57 → predicted = 22
h=3: 17.90 + 3×1.836 ≈ 23.41 → predicted = 23
```

**σ** = sample stddev of the 5 errors above ≈ `1.66`.

**Band for h=1**: `1.96 × 1.66 × √1 ≈ 3.25` → `lower ≈ 16, upper ≈ 23`.
**Band for h=3**: `1.96 × 1.66 × √3 ≈ 5.63` → `lower ≈ 18, upper ≈ 29`.
(Notice the band visibly widens from h=1 to h=3 — the `√h` term.)

**Endemic threshold**: with only one observation per ISO week across a
single year of data, every phase has `< 2` values, so every threshold falls
back to the **global** one: `mean(10,12,11,15,14,18) + 1.96·sqrt(mean)`
= `13.33 + 1.96×3.65 ≈ 20.5` → `ceil → 21`.

**Alerts**: `predicted=20 ≤ 21` (no), `22 > 21` (yes), `23 > 21` (yes) →
`alertBuckets = 2`, `riskLevel` for this code would be `HIGH`.

**`trendPerBucket`** = `(23 - 20) / (3 - 1) = 1.5` (rising).

This is exactly what `TimeSeriesForecasterTest` exercises in code form —
see that file for the automated version of this same walk-through.

---

## 9. Explicit non-goals (read before trusting a number too far)

- **Not an epidemic transmission model.** No `R₀`/`Rt`, no SEIR
  compartments, no contact-network assumptions — this only extrapolates the
  *reporting* time series, which conflates true incidence with reporting
  behaviour (delays, backlogs, onboarding of new organizations).
- **No per-series tuning.** Same `α/β/γ` for every disease and every
  territory (§2.5) — a deliberate simplicity/reproducibility trade-off, not
  an oversight.
- **`created_at`-based, not onset-based.** The last 1-2 buckets are
  routinely undercounted purely from reporting lag, which can look like a
  false "declining trend" right at the series' tail.
- **The 95% band is a normal approximation** around the model's own
  in-sample error, not a proper distributional (e.g. Poisson/negative-
  binomial count) prediction interval — reasonable for the case counts
  this system deals with, but don't read `lowerBound`/`upperBound` as a
  rigorously calibrated confidence interval.
