# Report Excel export — frontend integration guide

Every report in the system (and the two notification journals, Form 058 /
058‑1) can be exported to `.xlsx`. The export **never** streams the file back
on the request that starts it — it is a **background job**: you `POST` a
submit endpoint, get a job id back, watch its progress, then download the
finished file from a shared endpoint.

The submit endpoint is **per report** (it takes that report's own filters);
everything after submit — progress, "my files" list, download — is the **same
three endpoints** (`/v1/exports/**`) regardless of which report produced the
job.

| | |
|---|---|
| Shared surface | `/v1/exports` |
| Swagger tag | `Exports` |
| Auth | `Authorization: Bearer <token>` on every call (same as the rest of the API) |
| Permission | submit needs `PERMISSION_REPORTS_READ` (report modules) or just `isAuthenticated()` (Form 058 / 058‑1); the `/v1/exports/**` endpoints need only `isAuthenticated()` and only ever see **your own** jobs |
| File format | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` (`.xlsx`) |
| Row cap | **200 000** matching rows — a bigger result is rejected at submit time (`app.export.max-rows`) |
| Retention | finished/failed files are deleted after **7 days** (`app.export.retention-days`); the job row goes with them |

---

## 1. The flow

```
                          POST .../export  (report-specific filters)
  ┌─────────┐   ───────────────────────────────────────────────►   ┌──────────┐
  │ browser │                                                       │ backend  │
  └─────────┘   ◄───────────────────────────────────────────────   └──────────┘
        │           { id, status: PENDING|PROCESSING, progressPercent, ... }
        │
        │   (a) SSE:   GET /v1/exports/{id}/progress      ← recommended
        │   (b) poll:  GET /v1/exports?exportType=FORM1&size=5
        │
        │   …until status === "COMPLETED"
        │
        ▼
     GET /v1/exports/{id}/download   →   .xlsx bytes (Content-Disposition: attachment)
```

`status` goes `PENDING → PROCESSING → COMPLETED`, or `→ FAILED`. Only
`COMPLETED` jobs can be downloaded (a download call on any other status is
`409 error.export.not-ready`).

---

## 2. Submit endpoints (one per report)

All are `POST`, all return `ApiResponse<ExportJobResponse>` (see §4). Unless
noted, parameters are **query-string** parameters (`?from=…&to=…`), the body
is empty.

### 2.1 Geography‑first case reports — `from` / `to` / `diagnosisCode`

| Report (UZ) | `exportType` | Endpoint |
|---|---|---|
| Shakl №1 | `FORM1` | `POST /v1/reports/form-1/export` |
| Shakl №4 | `FORM4` | `POST /v1/reports/form-4/export` |
| Shakl №6 | `FORM6` | `POST /v1/reports/form-6/export` |
| Shakl №8 | `FORM8` | `POST /v1/reports/form-8/export` |
| Shakl №9 | `FORM9` | `POST /v1/reports/form-9/export` |

Params (all optional):

| Param | Type | Default | Notes |
|---|---|---|---|
| `from` | `date` (`YYYY-MM-DD`) | today | period start, inclusive |
| `to` | `date` | today | period end, inclusive |
| `diagnosisCode` | string | — | ICD‑10 / XKT‑10 code filter |

### 2.2 Shakl №10 — `FORM10`

`POST /v1/reports/form-10/export`

| Param | Type | Default | Notes |
|---|---|---|---|
| `year` | int | current year | reporting year |
| `period` | enum | current month | `JANUARY`…`DECEMBER`, `Q1`…`Q4`, `HALF_YEAR`, `NINE_MONTHS`, `YEAR` |
| `diagnosisCode` | string | — | ICD‑10 filter |
| `koef` | long | `100000` | intensive‑rate coefficient (per N population) |

### 2.3 Shakl №11 — `FORM11`

`POST /v1/reports/form-11/export`

Restructured 2026‑09‑17 into a structural clone of Shakl №10 — same params,
**no more flat `population`/`from`/`to`**. Territory population is now
resolved automatically per row from the "Aholi soni" reference (`ref_population`);
see `form-10-form-11-report-frontend-guide.md` §3 for how.

| Param | Type | Default | Notes |
|---|---|---|---|
| `year` | int | current year | reporting year |
| `period` | enum | current month | `JANUARY`…`DECEMBER`, `Q1`…`Q4`, `HALF_YEAR`, `NINE_MONTHS`, `YEAR` |
| `diagnosisCode` | string | — | ICD‑10 filter |
| `koef` | long | `100000` | intensive‑rate coefficient (per N population) |

### 2.4 Disease‑first & by‑territory reports — `from` / `to` only

| Report (UZ) | `exportType` | Endpoint |
|---|---|---|
| Shakl №12 (nozologiya bo'yicha) | `FORM12` | `POST /v1/reports/form-12/export` |
| Shakl №12 (hududlar bo'yicha) | `FORM12_BY_TERRITORY` | `POST /v1/reports/form-12/by-territory/export` |
| Shakl №13 (hududlar bo'yicha) | `FORM13` | `POST /v1/reports/form-13/export` |
| Shakl №13 (nozologiya bo'yicha) | `FORM13_BY_DISEASE` | `POST /v1/reports/form-13/by-disease/export` |
| Shakl №28.1 (nozologiya bo'yicha) | `FORM281` | `POST /v1/reports/form-28-1/export` |
| Shakl №28.1 (hududlar bo'yicha) | `FORM281_BY_TERRITORY` | `POST /v1/reports/form-28-1/by-territory/export` |
| Shakl №28.2 (nozologiya bo'yicha) | `FORM282` | `POST /v1/reports/form-28-2/export` |
| Shakl №28.2 (hududlar bo'yicha) | `FORM282_BY_TERRITORY` | `POST /v1/reports/form-28-2/by-territory/export` |

Params: `from` (`date`, default = whole history), `to` (`date`, default =
today). Both optional.

> **By‑territory column set is dynamic.** For `FORM12_BY_TERRITORY`,
> `FORM13`, `FORM281_BY_TERRITORY`, `FORM282_BY_TERRITORY` the number of
> columns depends on how many `ref_manual_report` catalog entries are tagged
> for that form — one column group per disease, in the same code‑sorted order
> the on‑screen report uses. Don't hard‑code column positions.

### 2.5 Statistika — `STATISTICS`

`POST /v1/reports/statistics/export`

| Param | Type | Default | Notes |
|---|---|---|---|
| `fromA` / `toA` | `date` | history / today | **Davr A** — always present in the file |
| `fromB` / `toB` | `date` | — | **Davr B** — comparison period; if you omit these, the "Davr B …" columns are still in the file but blank |

### 2.6 Forecast — `FORECAST`

`POST /v1/reports/forecast/export`

| Param | Type | Default | Notes |
|---|---|---|---|
| `diagnosisCode` | string | — | ICD‑10 (initial or final code) |
| `bucket` | enum | `WEEK` | `DAY` / `WEEK` / `MONTH` |
| `horizon` | int (1‑120) | `8` | buckets ahead; clamped to 90/52/24 by bucket |
| `method` | enum | `AUTO` | `AUTO` / `NAIVE_MEAN` / `SES` / `HOLT` / `HOLT_WINTERS_ADDITIVE` |
| `from` / `to` | `date` | default look‑back / today | training window |

Exports the geography breakdown rows only (root + children), not the
`/series` chart or `/top-diseases`.

### 2.7 Manual‑entry reports — filter object (query params)

These take the **same filter object as their list endpoint** — pass its
fields as query params.

| Report (UZ) | `exportType` | Endpoint |
|---|---|---|
| Shakl №2 (qo'lda) | `FORM2_MANUAL` | `POST /v1/reports/form-2/manual-entries/export` |
| Shakl №3‑1 | `FORM31` | `POST /v1/reports/form-3-1/entries/export` |
| Shakl №3‑2 | `FORM32` | `POST /v1/reports/form-3-2/entries/export` |
| Shakl №7 | `FORM7` | `POST /v1/reports/form-7/entries/export` |
| Analitik hisobot | `ANALYTIC_REPORT` | `POST /v1/reports/analytic/export` |

Common filter params: `sortBy` (`id` / `fromDate` / `toDate` / `createdAt` /
`updatedAt`; analytic also `name` / `status`), `sortDir` (`asc` / `desc`),
`from`, `to`. Analytic also accepts `status` (`TEMPLATE` / `FINAL`).
`page` / `size` are accepted but ignored for export — the whole filtered set
is exported, not one page.

### 2.8 Notification journals

| Report | `exportType` | Endpoint |
|---|---|---|
| Form №058 | `FORM058` | `POST /v1/form-058/export` |
| Form №058‑1 | `FORM0581` | `POST /v1/form-058-1/export` |

Filter = the **same query params as the form list endpoint**
(`GET /v1/form-058`) — date range, status, region, diagnosis, etc. Auth:
`isAuthenticated()` (no `PERMISSION_REPORTS_*` needed).

---

## 3. Shared endpoints (`/v1/exports`)

### 3.1 `GET /v1/exports` — "Mening fayllarim" (my files)

Paged list of **your own** export jobs, newest first.

| Query param | Default | Notes |
|---|---|---|
| `page` | `1` | 1‑based |
| `size` | `20` | max `200` |
| `exportType` | — | filter to one type, e.g. `FORM1`; omit for all |

Returns `PagedResponse<ExportJobResponse>` (`data` = array, plus paging
metadata).

### 3.2 `GET /v1/exports/{id}/progress` — SSE progress

Server‑Sent Events, one `progress` event per second carrying the full
`ExportJobResponse` JSON; the stream **closes itself** once the job reaches
`COMPLETED` / `FAILED`.

```
event: progress
data: {"id":42,"exportType":"FORM1","status":"PROCESSING","progressPercent":37,"processedRows":1850,"totalRows":5000,...}

event: progress
data: {"id":42,"exportType":"FORM1","status":"COMPLETED","progressPercent":100,"fileName":"form1_export_42.xlsx",...}
```

> **The browser's native `EventSource` cannot send the `Authorization`
> header, so it will 401 here.** Use `fetch` + a streaming reader (e.g.
> [`@microsoft/fetch-event-source`](https://github.com/Azure/fetch-event-source)) —
> exactly the same constraint and workaround as the notification stream, see
> [notification-frontend-guide.md](notification-frontend-guide.md#the-sse-stream--read-this-before-using-eventsource).
> If you don't want SSE at all, poll `GET /v1/exports?exportType=…` instead
> (jobs are usually seconds‑to‑a‑minute).

### 3.3 `GET /v1/exports/{id}/download` — the file

Returns the `.xlsx` bytes with
`Content-Disposition: attachment; filename="form1_export_42.xlsx"`.

- `409 error.export.not-ready` if the job isn't `COMPLETED`.
- `404` if the job id isn't yours / doesn't exist.

Because it needs the bearer header, you can't just point `window.location`
at it — fetch as a blob and save:

```ts
const res = await fetch(`${API_BASE}/v1/exports/${id}/download`, {
  headers: { Authorization: `Bearer ${accessToken}` },
});
if (!res.ok) throw new Error(await res.text());
const blob = await res.blob();
const cd = res.headers.get("Content-Disposition") ?? "";
const fileName = /filename="?([^"]+)"?/.exec(cd)?.[1] ?? `export-${id}.xlsx`;
const url = URL.createObjectURL(blob);
const a = document.createElement("a");
a.href = url; a.download = fileName; a.click();
URL.revokeObjectURL(url);
```

---

## 4. Response shapes

### `ApiResponse<ExportJobResponse>` (submit)

```json
{
  "success": true,
  "message": "Export job has been queued.",
  "data": {
    "id": 42,
    "exportType": "FORM1",
    "status": "PENDING",
    "progressPercent": 0,
    "processedRows": 0,
    "totalRows": 5000,
    "fileName": null,
    "fileSizeBytes": null,
    "errorMessage": null,
    "createdAt": "2026-09-07T09:12:33Z",
    "completedAt": null
  }
}
```

| Field | When populated | Notes |
|---|---|---|
| `id` | always | use for `/progress` and `/download` |
| `status` | always | `PENDING` / `PROCESSING` / `COMPLETED` / `FAILED` |
| `progressPercent` | always | 0‑100 |
| `processedRows` / `totalRows` | `totalRows` known at submit; `processedRows` grows while `PROCESSING` | |
| `fileName` / `fileSizeBytes` | after `COMPLETED` | |
| `errorMessage` | after `FAILED` | |
| `completedAt` | after `COMPLETED` / `FAILED` | |

### `PagedResponse<ExportJobResponse>` (list)

Standard paged envelope — `data` is `ExportJobResponse[]`, plus the usual
`page` / `size` / `totalElements` / `totalPages` metadata.

---

## 5. Errors at submit

| HTTP | Code | Meaning |
|---|---|---|
| 400 | `VALIDATION_FAILED` + message key `error.export.too-large` | more than 200 000 rows match the filter — narrow the date range / add `diagnosisCode` and retry (message: `Filter matches {n} rows, which exceeds the export limit of {max}…`) |
| 400 | `VALIDATION_FAILED` | bad param (e.g. malformed date, `size > 200`) |
| 401 / 403 | — | missing token / missing `PERMISSION_REPORTS_READ` |
| 5xx | — | export worker queue saturated (rare) — the job is marked `FAILED`, retry shortly |

A job that fails **during** processing comes back as `status: "FAILED"` with
`errorMessage` set — surface that text and offer a retry.

---

## 6. Column headers are locale‑aware

Column headers (and the sheet title region) are rendered in the language of
the **`Accept-Language` header on the submit request** — `uz`, `uz-Cyrl`,
`kaa`, `ru`, `en` (default `uz`). Send the user's current UI language on the
`POST .../export` call and the downloaded file matches it. The header text is
purely cosmetic — data columns, their order, and their meaning do not change
with language.

> The **machine** column keys (used by the dev‑panel column‑ordering setting
> `export.excel.<type>.columns`) are language‑independent, so an admin's
> custom column selection keeps working across languages.

Form 058 / 058‑1 journals are the exception — their headers are still fixed
(Cyrillic, official‑blank layout) and ignore `Accept-Language` for now.

---

## 7. Minimal end‑to‑end example

```ts
import { fetchEventSource } from "@microsoft/fetch-event-source";

async function exportReport(
  submitUrl: string,          // e.g. `${API_BASE}/v1/reports/form-1/export?from=2026-01-01&to=2026-09-01`
  accessToken: string,
  lang: string,               // "uz" | "uz-Cyrl" | "kaa" | "ru" | "en"
  onProgress: (pct: number) => void,
): Promise<void> {
  const auth = { Authorization: `Bearer ${accessToken}` };

  // 1. submit
  const submit = await fetch(submitUrl, {
    method: "POST",
    headers: { ...auth, "Accept-Language": lang },
  });
  if (!submit.ok) throw new Error(await submit.text());   // handles error.export.too-large etc.
  const { data: job } = await submit.json();

  // 2. watch progress
  await new Promise<void>((resolve, reject) => {
    fetchEventSource(`${API_BASE}/v1/exports/${job.id}/progress`, {
      headers: auth,
      onmessage(ev) {
        if (ev.event !== "progress") return;
        const j = JSON.parse(ev.data);
        onProgress(j.progressPercent);
        if (j.status === "COMPLETED") resolve();
        if (j.status === "FAILED") reject(new Error(j.errorMessage ?? "export failed"));
      },
      onerror: reject,
    });
  });

  // 3. download
  const file = await fetch(`${API_BASE}/v1/exports/${job.id}/download`, { headers: auth });
  const blob = await file.blob();
  const name = /filename="?([^"]+)"?/.exec(file.headers.get("Content-Disposition") ?? "")?.[1]
             ?? `export-${job.id}.xlsx`;
  const url = URL.createObjectURL(blob);
  Object.assign(document.createElement("a"), { href: url, download: name }).click();
  URL.revokeObjectURL(url);
}
```

Polling variant of step 2, if you skip SSE:

```ts
async function waitForJob(id: number, auth: HeadersInit): Promise<ExportJobResponse> {
  for (;;) {
    const res = await fetch(`${API_BASE}/v1/exports?size=50`, { headers: auth });
    const { data } = await res.json();
    const job = data.find((j: ExportJobResponse) => j.id === id);
    if (job?.status === "COMPLETED") return job;
    if (job?.status === "FAILED") throw new Error(job.errorMessage ?? "export failed");
    await new Promise((r) => setTimeout(r, 2000));
  }
}
```
