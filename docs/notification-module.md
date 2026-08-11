# Notification module

Package: `uz.uzinfocom.app.platform.notification`. In-app notifications for
end users — no message broker exists in this deployment (confirmed absent
from `pom.xml`/`application*.properties`), so events are generated and
delivered entirely in-process. Deliberately modeled on two existing
platform pieces rather than introducing new infrastructure:

- `platform.audit` (`AuditEventListener`) — the async, `AFTER_COMMIT`
  event-consumption pattern.
- `platform.export` (`ExportJobController`/`ExportJobService`) — the
  per-user SSE-streaming pattern.

## Layout

```
notification/
├── domain/
│   ├── Notification.java        one row per (event, recipient) — see "Fan-out model" below
│   └── NotificationType.java    FORM058_RECEIVED, FORM058_ACKNOWLEDGED, FORM058_CANCELED,
│                                  FORM0581_RECEIVED, FORM0581_ACKNOWLEDGED, FORM0581_CANCELED,
│                                  CARD_ASSIGNED, ACT_ASSIGNED, ACT_LIS_RESPONSE, EXPORT_READY
├── repository/
│   └── NotificationRepository.java
├── application/
│   ├── NotificationEventListener.java   the trigger — see below
│   ├── NotificationQueryService.java    list / unread-count / message resolution
│   ├── NotificationCommandService.java  mark-read / mark-all-read
│   ├── NotificationStreamService.java   SSE unread-count push
│   ├── dto/                             NotificationResponse, NotificationFilterRequest
│   └── exception/
│       └── NotificationNotFoundException.java
└── web/
    └── NotificationController.java      /v1/notifications/**
```

## How a notification gets created: reusing the existing event bus

No business command service was changed to "know about" notifications
directly (with one exception below). `AuditEventListener` already consumes
`EntityCreatedEvent`/`StatusChangedEvent`, published `AFTER_COMMIT` by the
Form058/Form0581/Card/Act command services. Spring dispatches an event to
**every** matching `@TransactionalEventListener` bean, so
`NotificationEventListener` simply listens to the same events and runs
alongside the audit trail:

| Trigger | Event (publisher) | Recipients |
|---|---|---|
| Form058 received | `EntityCreatedEvent(FORM058, id, actorUserId)` — `CreateForm058Service` | active users of `Form058.receiverOrganizationId` |
| Form058 acknowledged | `StatusChangedEvent(FORM058, id, "SENT", "ACCEPTED", ...)` — `AcceptForm058Service.accept` | active users of `Form058.senderOrganizationId` |
| Form058 canceled | `StatusChangedEvent(FORM058, id, oldStatus, "CANCELED", ...)` — `CancelForm058Service.cancel` | active users of **both** `Form058.senderOrganizationId` and `Form058.receiverOrganizationId` |
| Form0581 received | `EntityCreatedEvent(FORM0581, id, actorUserId)` — `CreateForm0581Service` | active users of `Form0581.receiverOrganizationId` |
| Form0581 acknowledged | `StatusChangedEvent(FORM0581, id, "SENT", "ACCEPTED", ...)` — `AcceptForm0581Service.accept` | active users of `Form0581.senderOrganizationId` |
| Form0581 canceled | `StatusChangedEvent(FORM0581, id, oldStatus, "CANCELED", ...)` — `CancelForm0581Service.cancel` | active users of **both** `Form0581.senderOrganizationId` and `Form0581.receiverOrganizationId` |
| Card assigned | `EntityCreatedEvent(CARD, id, assignedById)` — `CardCommandService.createBlankCards` (**new** publish call — cards previously fired no per-card event) | `card.getUsers()` |
| Act assigned | `EntityCreatedEvent(ACT, id, assignedById)` — `ActCommandService.assignActs` | `act.getUsers()` |
| LIS response received | `StatusChangedEvent(ACT, id, "SENT", "COMPLETED", ...)` — `ActCommandService.receiveLisResponse` | `act.getUsers()` |
| Export ready to download | `ExportJobCompletedEvent(jobId, createdBy, exportType, fileName)` — `ExportJobService.completeJob` | `event.recipientUserId()` only (whoever submitted the export) |

**Fixed bug (this change)**: the acknowledged handlers previously matched
`oldStatus == "SENT" && newStatus == "RECEIVED"` — but neither
`FormStatus` nor `Form0581Status` has ever had a `RECEIVED` value (the
receiver's acceptance moves the form to `ACCEPTED`, see
`AcceptForm058Service`/`AcceptForm0581Service`). `FORM0581_ACKNOWLEDGED`
therefore never fired in practice; `FORM058_ACKNOWLEDGED`/`FORM058_CANCELED`
didn't exist at all (`FORM058` wasn't even routed in
`on(StatusChangedEvent)`, only `ACT` and `FORM0581` were). Both are now
wired the same way Form0581 already was, matching `Form058CancelValidator`'s
identical "either side may cancel while `SENT`" rule.

The export-ready trigger is the one exception to "reuse `EntityCreatedEvent`/
`StatusChangedEvent` as-is": `ExportJobCompletedEvent`
(`platform.export.domain.event`) is its own small record, not routed through
`AuditEntityType` — an export job isn't a Form058/Form0581/Card/Act business
record on the audit trail, and it only ever has one recipient (the submitter)
rather than a resolved set, so there's nothing to reuse from the audit event
shapes. It's still consumed the same way, via
`on(ExportJobCompletedEvent event)` on the same listener, and unlike every
other trigger it does **not** exclude the actor — the recipient here *is* the
person who should be told, since they're the one waiting on the download.

`NotificationEventListener` never trusts data carried on the event itself
beyond the entity id — it reloads the Form058/Form0581/Card/Act from its
own repository inside its own `REQUIRES_NEW` transaction (same pattern
`AuditEventListener` uses), then resolves recipients from that fresh load:

```java
@Async("applicationTaskExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void on(EntityCreatedEvent event) { ... }
```

`AFTER_COMMIT` means a notification is only ever created for a change that
actually committed; `@Async` on the shared `applicationTaskExecutor` means a
slow or failing fan-out can never delay the original request or roll back
the Form058/Card/Act change that triggered it — mirrors the doc comment on
`AuditEventListener`.

**LIS response filtering**: `StatusChangedEvent(ACT, ...)` fires for every
act status transition (NEW→IN_PROGRESS, READY→SENT, SEND_FAILED, ...), not
just the LIS callback. The listener only reacts when
`oldStatus == "SENT" && newStatus == "COMPLETED"` — the one transition
`ActCommandService.receiveLisResponse` produces.

**Form058/Form0581 status filtering**: same idea — `StatusChangedEvent(FORM058, ...)`
/`StatusChangedEvent(FORM0581, ...)` fires for every transition
(`SENT→ACCEPTED`, `ACCEPTED→CARD_LINKED`, `CARD_LINKED→APPROVED`, ...), but
the listener only reacts to `SENT→ACCEPTED` (the receiver's acknowledgement,
see `Form058AcceptValidator`/`Form0581AcceptValidator`) and any `→CANCELED`
transition. `Form058CancelValidator`/`Form0581CancelValidator` allow
*either* the sender or the receiver to cancel while still `SENT`, so
`handleForm058Canceled`/`handleForm0581Canceled` notify both organizations
rather than trying to resolve which side didn't act — `excludingActor`
already drops the acting user from whichever side's recipient list they
belong to.

**Ordering pitfall already hit once**: the settings-enabled check must run
*before* any DB lookup of the source entity, not just before the fan-out —
loading the entity as a method argument (Java evaluates arguments eagerly)
silently ran an extra query even when the notification type was disabled.
Each `handleXxxReceived`/`handleXxxAssigned` method now checks
`SystemSettingResolver` first and returns early before touching a
repository — regression-guarded by
`NotificationEventListenerTest.formReceivedNotificationIsSkippedWhenDisabledBySetting`.

**Self-invocation pitfall on the publishing side (`ExportJobService`)**:
`completeJob` is called from `runExport` on the *same instance*
(`this.completeJob(...)`), which bypasses Spring's proxy-based AOP entirely
— a plain `@Transactional` on `completeJob` would be silently ignored, and
without an active transaction `@TransactionalEventListener(AFTER_COMMIT)`
never fires at all. `completeJob` therefore runs its save *and* the
`eventPublisher.publishEvent(...)` call inside the same
`requiresNewTransactionTemplate.executeWithoutResult(...)` block
`updateProgress` already used for the identical reason (see that method's
comment) — a real, programmatically-started transaction rather than a
proxy-intercepted one.

## Fan-out model: one row per (event, recipient)

`Notification` is written once per recipient at creation time, not as one
event row plus a separate per-user read-tracking join — this mirrors how
`Card.users`/`Act.users` already model direct assignment in this codebase,
and keeps "my unread notifications" a single indexed query
(`(recipient_user_id, read, occurred_at)`) instead of a join-time ACL
check. The actor who triggered the event is always excluded from its own
recipient list (`excludingActor`) — nobody needs to be told they just did
the thing they did.

Each row also carries a best-effort `organization_id` (the form's
`receiverOrganizationId`, walked up through `Card`/`Act` → `Form058`/
`Form0581` where applicable) even though rows are already recipient-scoped
— kept for future filtering/reporting, not required by the current read
path.

## Per-type on/off switch: reused `system_settings`, no new admin surface

Each notification kind is gated by a boolean key, checked via the existing
`SystemSettingResolver.resolveBoolean(key, true)` (short-TTL cached
read-through over the `system_settings` table):

- `notification.form058-received.enabled`
- `notification.form058-acknowledged.enabled`
- `notification.form058-canceled.enabled`
- `notification.form0581-received.enabled`
- `notification.form0581-acknowledged.enabled`
- `notification.form0581-canceled.enabled`
- `notification.card-assigned.enabled`
- `notification.act-assigned.enabled`
- `notification.act-lis-response.enabled`
- `notification.export-ready.enabled`

No row for a key means enabled (`resolveBoolean`'s documented "empty table
= no behavior change" contract). These are the same rows managed by
`SystemSettingController`/`SystemSettingCommandService` — writable from
**either** `/v1/admin/settings` or the dev-monitoring panel's
`/v1/dev/settings` (`DevSettingController`, HTTP-Basic-authenticated,
separate from normal SSO users) — same table, so a value written from one
surface is visible on the other immediately. No new controller or table
was added just for toggles.

## Message text: key + params, resolved at read time

`Notification.messageKey` (e.g. `notification.card-assigned`) and
`messageParams` (a JSON array, e.g. `[50]` for the card id) are stored
instead of a pre-rendered string — `NotificationQueryService` resolves the
localized text on every read via the existing `MessageResolver.resolve(key,
args...)` (falls back to the key itself if no translation exists yet, same
as every other message-code lookup in this app). This keeps notifications
correctly localized per viewer/locale rather than frozen in whatever
language was active when the event fired.

## Delivery API (`/v1/notifications`)

All endpoints `@PreAuthorize("isAuthenticated()")`, scoped by
`@CurrentUser PrincipalUser principal` — never a client-supplied user id,
same rule as `Card.MINE`/`Act.MINE`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/v1/notifications` | Paged list of the caller's own notifications, newest first; `unreadOnly` filter |
| GET | `/v1/notifications/unread-count` | Badge count |
| GET | `/v1/notifications/unread-count/by-type` | Unread count per `NotificationType`, every type present (0 if none) |
| POST | `/v1/notifications/{id}/read` | Mark one as read |
| POST | `/v1/notifications/read-all` | Mark all as read |
| GET | `/v1/notifications/stream` | SSE stream, unread count pushed every second |

`NotificationStreamService.stream(...)` mirrors
`ExportJobService.streamProgress`/`pushProgress` exactly: a dedicated
single-thread `ScheduledExecutorService` polls and pushes an SSE event once
a second; on client disconnect (`IOException`/`IllegalStateException` from
`emitter.send`) it calls `emitter.complete()` quietly instead of letting
the exception reach MVC's normal error-response machinery — which would
otherwise fail trying to write a JSON error body onto an already-committed
`text/event-stream` response.

## Testing notes

- `NotificationEventListenerTest` mocks every repository/collaborator and
  asserts fan-out membership, exclusion of the actor, and the
  settings-disabled short-circuit — no Spring context needed, same style as
  `LoginHistoryRecorderTest`.
- `CardCommandServiceAssignCardsTest.publishesAnEntityCreatedEventPerCreatedCard`
  covers the one new call site in existing business code
  (`CardCommandService`), rather than adding a separate test file — the
  assign-cards flow already had thorough coverage there.
