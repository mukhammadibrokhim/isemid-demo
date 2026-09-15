# Internal notifications — frontend guide

`orchestration.notification` covers **internal, in-app notifications only** —
alerts shown inside this app's own SPA to its own signed-in users (a form
was assigned to you, a card needs attention, your export is ready, ...).
This is not a generic "notify anyone about anything" system, and it is not
how external parties are informed of anything — see the Scope note below
for the one case that trips people up (integration sources).

How to list, read, and live-update the current user's internal
notifications. Full backend architecture is in
[`notification-module.md`](./notification-module.md); this document is only
about what a client-side integration needs to know.

Available in Swagger UI as its own group, **"Notifications"**
(`/swagger-ui.html`, group dropdown → "Notifications").

All six endpoints require `Authorization: Bearer <token>` and are always
scoped to the caller — there is no way to pass another user's id.

> **Scope: internal only**. This module is only for human users signed into
> this app's own SPA — it's an *internal-system* notification mechanism, not
> a general delivery channel. It is **not** how a Form058/Form0581's *source*
> finds out about status changes — i.e. forms submitted through the inbound-integration API,
> where `Form058.sourceIntegrationClientId` / `Form0581.sourceIntegrationClientId`
> is set to the submitting `IntegrationClient`'s id (`null` for forms entered
> through SSO/DHP in the normal UI). That integration client never gets an
> in-app `Notification` row or an SSE event here — it's notified separately,
> via outbound **webhooks** (`OutboundWebhookEventListener` →
> `OutboundWebhookDispatchService`, see `orchestration.webhook`): an HTTP callback
> POSTed to the client's own registered URL, with its own retry/backoff and
> payload shape, unrelated to anything on this page. The *receiving*
> organization's human users (`receiverOrganizationId`) still get a normal
> in-app `FORM058_RECEIVED`/`FORM0581_RECEIVED` notification regardless of
> whether the form came from an integration source or the UI — only the
> submitting side's notification path differs. If you're building the
> integration-client side (not the SPA), this guide doesn't apply — ask
> about the webhook contract instead.

| Method | Path | Purpose |
|---|---|---|
| GET | `/v1/notifications` | Paged list, newest first. Query params: `page` (1-based), `size` (max 200), `unreadOnly` |
| GET | `/v1/notifications/unread-count` | Badge count (single number) |
| GET | `/v1/notifications/unread-count/by-type` | Unread count per type, every `NotificationType` present (0 if none) |
| POST | `/v1/notifications/{id}/read` | Mark one as read |
| POST | `/v1/notifications/read-all` | Mark all as read |
| GET | `/v1/notifications/stream` | **SSE** — unread count pushed once a second (see below, read this before wiring it up) |

## CORS

Same rule as every other endpoint in this API: the backend's allowed
origins are configured server-side (`app.cors.allowed-origins` — in dev
that's `http://localhost:3000` by default). If your frontend runs on a
different origin/port, ask the backend team to add it, otherwise the
browser blocks the request before it reaches the server. This API
authenticates via `Authorization: Bearer`, never cookies — do **not** set
`credentials: "include"` / `withCredentials: true` anywhere in this module;
the backend does not allow credentialed CORS (`allowCredentials=false`), and
turning it on client-side without a matching server change will make the
browser reject the response outright.

## The five plain REST endpoints

Nothing special — normal `fetch`/`axios` calls with the bearer header, same
as the rest of the app.

```ts
const res = await fetch(`${API_BASE}/v1/notifications?page=1&size=20&unreadOnly=true`, {
  headers: { Authorization: `Bearer ${accessToken}` },
});
const { data, meta } = (await res.json()).data
  ? await res.json()
  : null; // see PagedResponse shape below
```

Response shapes (same envelope as every other endpoint in this API):

```jsonc
// GET /v1/notifications  ->  PagedResponse<NotificationResponse>
{
  "success": true,
  "message": "Успешный запрос.",
  "data": [
    {
      "id": 501,
      "type": "CARD_ASSIGNED",
      "entityType": "CARD",
      "entityId": 50,
      "organizationId": 12,
      "message": "Вам назначена карта №50",   // already localized server-side
      "read": false,
      "readAt": null,
      "occurredAt": "2026-08-11T09:12:03Z"
    }
  ],
  "meta": { "page": 1, "size": 20, "totalElements": 37, "totalPages": 2 },
  "links": { "self": "...", "next": "...", "prev": null }
}

// GET /v1/notifications/unread-count  ->  ApiResponse<Long>
{ "success": true, "message": "Успешный запрос.", "data": 5 }

// GET /v1/notifications/unread-count/by-type  ->  ApiResponse<Map<NotificationType, Long>>
{
  "success": true,
  "message": "Успешный запрос.",
  "data": {
    "FORM058_RECEIVED": 0, "FORM058_ACKNOWLEDGED": 0, "FORM058_CANCELED": 0,
    "FORM0581_RECEIVED": 2, "FORM0581_ACKNOWLEDGED": 0, "FORM0581_CANCELED": 0,
    "CARD_ASSIGNED": 3, "ACT_ASSIGNED": 0, "ACT_LIS_RESPONSE": 0, "EXPORT_READY": 0
  }
}
```

## The SSE stream — read this before using `EventSource`

`GET /v1/notifications/stream` is a Server-Sent Events endpoint that pushes
the caller's unread count once a second:

```
event: unread-count
data: 5

event: unread-count
data: 5

event: unread-count
data: 6
```

It requires the same `Authorization: Bearer <token>` header as every other
endpoint on this API — **and the browser's native `EventSource` cannot send
custom headers.** If you do:

```ts
// BROKEN — do not do this
const es = new EventSource(`${API_BASE}/v1/notifications/stream`);
```

...the request goes out with no bearer token, the backend responds
`401 Bearer token is missing`, and depending on the browser this often shows
up in devtools as a failed/blocked cross-origin request rather than a clean
401 — easy to misdiagnose as a CORS bug when the real cause is the missing
header. There is currently no query-string token fallback on this endpoint
(unlike some SSE APIs), so `EventSource` as-is is **not usable here**.

Use `fetch` + a streaming reader instead, which supports headers normally.
Two options:

### Option A — a small library (recommended)

[`@microsoft/fetch-event-source`](https://github.com/Azure/fetch-event-source)
gives you `EventSource`-like ergonomics with full header support and handles
reconnection for you:

```ts
import { fetchEventSource } from "@microsoft/fetch-event-source";

const controller = new AbortController();

fetchEventSource(`${API_BASE}/v1/notifications/stream`, {
  headers: { Authorization: `Bearer ${accessToken}` },
  signal: controller.signal,
  onmessage(ev) {
    if (ev.event === "unread-count") {
      setUnreadCount(Number(ev.data));
    }
  },
  onerror(err) {
    console.error("notification stream error", err);
    // returning nothing here lets the library keep retrying with backoff;
    // throw to stop retrying (e.g. on a 401 after token refresh already failed)
  },
});

// on logout / unmount:
controller.abort();
```

### Option B — no dependency, hand-rolled

```ts
async function streamUnreadCount(accessToken: string, onCount: (n: number) => void, signal: AbortSignal) {
  const res = await fetch(`${API_BASE}/v1/notifications/stream`, {
    headers: { Authorization: `Bearer ${accessToken}`, Accept: "text/event-stream" },
    signal,
  });
  if (!res.ok || !res.body) throw new Error(`stream failed: ${res.status}`);

  const reader = res.body.pipeThrough(new TextDecoderStream()).getReader();
  let buffer = "";

  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += value;

    let boundary;
    while ((boundary = buffer.indexOf("\n\n")) !== -1) {
      const rawEvent = buffer.slice(0, boundary);
      buffer = buffer.slice(boundary + 2);

      const dataLine = rawEvent.split("\n").find((l) => l.startsWith("data:"));
      if (dataLine) onCount(Number(dataLine.slice(5).trim()));
    }
  }
}
```

Either way, wrap the call in a reconnect loop — the server closes the
connection after **15 minutes** (`SseEmitter` timeout) even if the client is
still open, so a long-lived tab needs to reconnect after that point. Option
A does this automatically; with option B, catch the loop exiting and call it
again (with a short delay, and stop entirely on `AbortError`/logout).

### What the stream is (and isn't) for

The stream only ever carries the **unread count**, not the notification list
itself or per-item detail. Typical usage: keep a badge count live via the
stream, and re-fetch `GET /v1/notifications` (or
`/unread-count/by-type` for a per-tab badge) only when the user opens the
notification panel or the count changes.

## Marking as read

```ts
await fetch(`${API_BASE}/v1/notifications/${id}/read`, {
  method: "POST",
  headers: { Authorization: `Bearer ${accessToken}` },
});
// or, for "mark all":
await fetch(`${API_BASE}/v1/notifications/read-all`, {
  method: "POST",
  headers: { Authorization: `Bearer ${accessToken}` },
});
```

Neither call updates the live SSE count immediately by itself — the stream
polls the DB once a second, so the badge catches up within ~1s. If you want
it to feel instant, decrement the local badge state optimistically at the
same time you fire the read request.

## Which notification kinds exist

`NotificationType`: `FORM058_RECEIVED`, `FORM058_ACKNOWLEDGED`,
`FORM058_CARD_LINKED`, `FORM058_APPROVED`, `FORM058_CANCELED`,
`FORM058_REOPENED`, `FORM0581_RECEIVED`, `FORM0581_ACKNOWLEDGED`,
`FORM0581_CARD_LINKED`, `FORM0581_APPROVED`, `FORM0581_CANCELED`,
`FORM0581_REOPENED`, `CARD_ASSIGNED`, `CARD_ACCEPTED_BY_USER`,
`CARD_REJECTED_BY_USER`, `CARD_COMPLETED`, `CARD_APPROVED`, `CARD_REJECTED`,
`ACT_ASSIGNED`, `ACT_LIS_RESPONSE`, `EXPORT_READY` — plus the four
affiliation-specific types below. `*_ACKNOWLEDGED` fires when the receiving
organization accepts the form (`SENT` → `ACCEPTED`) — sent to the *sender's*
organization; `*_RECEIVED` is the opposite direction, sent to the
*receiver's* organization when the form first arrives. Each type is
independently toggleable server-side via `/v1/dev/settings` — a type being disabled there
just means it stops being created, existing rows and the API shape are
unaffected either way, so the frontend doesn't need to handle a "type is
disabled" case specially.

### Affiliation types — a third kind of recipient

`FORM058_AFFILIATED_RECEIVED`/`FORM058_AFFILIATED_CARD_LINKED` and their
Form0581 counterparts `FORM0581_AFFILIATED_RECEIVED`/
`FORM0581_AFFILIATED_CARD_LINKED` are different from every other type above:
they're sent to an organization that is **neither the sender nor the
receiver** of the form, but is the patient's workplace or place of study
(see
[form058-form0581-frontend-guide.md § Affiliated organizations](./form058-form0581-frontend-guide.md#affiliated-organizations)
for the full picture — the dedicated `GET /v1/form-058/affiliated` /
`GET /v1/form-058-1/affiliated` listings and the card/act attachment rule
these four notifications are cueing you into).

| `type` | `entityType`/`entityId` | Recipient | When |
|---|---|---|---|
| `FORM058_AFFILIATED_RECEIVED` | `FORM058` / form id | active users of the affiliated org | a new Form058 is created whose patient is affiliated with your org |
| `FORM058_AFFILIATED_CARD_LINKED` | `FORM058` / form id | active users of the affiliated org | that form reaches `CARD_LINKED` — the cue that `Card`s now exist and you may attach an `Act` (`POST /v1/cards/{id}/acts`) |
| `FORM0581_AFFILIATED_RECEIVED` | `FORM0581` / form id | active users of the affiliated org | a new Form0581 is created whose patient is affiliated with your org |
| `FORM0581_AFFILIATED_CARD_LINKED` | `FORM0581` / form id | active users of the affiliated org | that form reaches `CARD_LINKED` — the cue that `Card`s now exist and you may attach an `Act` (`POST /v1/cards/{id}/acts`) |

Handle these exactly like every other notification (same `NotificationResponse`
shape, same read/unread flow) — the only difference is *why* you received
it, not the delivery mechanism. A sensible click-through: open the form via
`GET /v1/form-058/{entityId}` / `GET /v1/form-058-1/{entityId}` (or route
into the `/v1/form-058/affiliated` / `/v1/form-058-1/affiliated` list view
filtered to that id) rather than assuming you're its sender or receiver.
