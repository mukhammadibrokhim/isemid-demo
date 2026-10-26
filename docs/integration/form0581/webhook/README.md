# Webhook — Form 058-1 / Форма №058-1 / 058-1-shakl

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

Submission: [form0581](../README.md) · Auth: [auth](../../auth/README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

Ro'yxatdan o'tgan klient o'z tizimiga formalar holati o'zgarganda avtomatik xabar (webhook) olishi mumkin. Shunda holatni `GET` bilan so'rab turish shart emas.

## Qachon yuboriladi

Faqat shu integratsiya klienti orqali yuborilgan **FORM0581** formalari bo'yicha, va faqat **holat o'zgarganda** (forma yaratilgandagi dastlabki `SENT` holati uchun webhook yuborilmaydi — u yaratish javobida bor). Webhook yoqilgan bo'lishi kerak.

## Holat o'zgarishlari

| Eski holat | Yangi holat | Sabab |
|---|---|---|
| `SENT` | `ACCEPTED` | qabul qiluvchi formani qabul qildi |
| `ACCEPTED` | `CARD_LINKED` | formaga karta biriktirildi |
| `CARD_LINKED` | `CARD_LINKED` | yana bir karta biriktirildi (status o'zgarmaydi, lekin bildirishnoma yuboriladi) |
| `CARD_LINKED` | `APPROVED` | yuboruvchi yakuniy tasdiqladi (karta biriktirilgandan keyingina) |
| `SENT` | `CANCELED` | yuboruvchi qaytarib oldi yoki qabul qiluvchi rad etdi |
| `CANCELED` | `SENT` | super-admin formani qayta ochdi |

**Hayot sikli:**

```
SENT ─► ACCEPTED ─► CARD_LINKED ─► APPROVED
  └─► CANCELED ─(reopen)─► SENT
```

## Sozlash

Webhook sizning tomoningizdan qabul qilinadigan manzil bo'lishi kerak. Ulanish uchun bizga quyidagilarni bering, biz yoqamiz:

- `callbackUrl` — **HTTPS** manzil, internetdan ochiq (localhost, ichki/xususiy tarmoq manzillari rad etiladi);
- `httpMethod` — `POST` yoki `PATCH`;
- autentifikatsiya: `NONE`, `BASIC_AUTH` (login + parol), `BEARER_TOKEN` (token) yoki `API_KEY_HEADER` (header nomi + qiymat) — biz so'rovga shuni qo'shamiz;
- agar kerak bo'lsa, tegishli maxfiy qiymat (u bizda shifrlangan holda saqlanadi).

## So'rov

Biz sizning manzilingizga JSON yuboramiz:

```http
POST <callbackUrl>   (or PATCH)
Content-Type: application/json
<auth header if configured>

```

```json
{ "entityType": "FORM0581", "entityId": 311, "oldStatus": "SENT", "newStatus": "ACCEPTED", "occurredAt": "2026-10-26T14:15:30.123+05:00" }
```

**Maydonlar:**

- `entityType` — forma turi: `FORM0581`
- `entityId` — formaning raqamli ID si — yaratish javobidagi `id` bilan bir xil (`uuid` emas)
- `oldStatus` — oldingi holat
- `newStatus` — yangi holat
- `occurredAt` — o'zgarish vaqti, ISO-8601, vaqt zonasi offseti bilan (masalan `+05:00`)

## Javobingiz

Muvaffaqiyat deb faqat **`2xx`** javob hisoblanadi. Boshqa kod yoki ulanish xatosi/timeout bo'lsa, yuborish takrorlanadi. Javob tanasi o'qilmaydi — tez `200`/`204` qaytaring va ishlovni keyinga qoldiring.

## Qayta urinishlar

Muvaffaqiyatsiz yuborish quyidagi oraliqlar bilan takrorlanadi (sukut bo'yicha): **1 daqiqa, 5 daqiqa, 15 daqiqa, 1 soat, 6 soat**. Jami 1 ta asosiy + 5 ta qayta urinish; undan keyin yuborish to'xtatiladi va avtomatik takrorlanmaydi (kerak bo'lsa bizdan qayta yuborishni so'rang). Qayta urinishlar webhooklar tartibini o'zgartirishi mumkin. Manzilingiz ketma-ket xato bersa, tizim bu klient uchun yuborishni qisqa muddatga to'xtatadi (circuit breaker) — bunday urinishlar ham hisobga olinadi.

## Tavsiyalar

- **Idempotent** bo'ling: bir xil xabar ikki marta kelishi mumkin.
- Tartibga ishonmang: `occurredAt` va `newStatus` ga qarang, eng yangisini oling.
- To'liq ma'lumot kerak bo'lsa, xabardan keyin o'zingiz so'rang (masalan `GET /integration/v1/{source}/patients/case`).
- Webhook tanasida imzo yo'q — himoya uchun autentifikatsiya headeridan foydalaning va (imkon bo'lsa) bizning IP manzillarimizni cheklang.
- Webhook yaratish/rad etish sababini (`reason`) o'z ichiga olmaydi.

---

<a id="ru"></a>
# 🇷🇺 Русский

Зарегистрированный клиент может автоматически получать уведомление (webhook) в свою систему при смене статуса форм. Тогда не нужно опрашивать статус через `GET`.

## Когда отправляется

Только по формам **FORM0581**, поданным через этого интеграционного клиента, и только **при смене статуса** (для начального `SENT` при создании webhook не отправляется — он уже в ответе на создание). Webhook должен быть включён.

## Смена статусов

| Старый статус | Новый статус | Причина |
|---|---|---|
| `SENT` | `ACCEPTED` | получатель принял форму |
| `ACCEPTED` | `CARD_LINKED` | к форме привязана карточка |
| `CARD_LINKED` | `CARD_LINKED` | привязана ещё одна карточка (статус не меняется, но уведомление отправляется) |
| `CARD_LINKED` | `APPROVED` | отправитель дал итоговое подтверждение (только после привязки карточки) |
| `SENT` | `CANCELED` | отправитель отозвал форму или получатель отклонил её |
| `CANCELED` | `SENT` | супер-администратор вновь открыл форму |

**Жизненный цикл:**

```
SENT ─► ACCEPTED ─► CARD_LINKED ─► APPROVED
  └─► CANCELED ─(reopen)─► SENT
```

## Настройка

Webhook отправляется на ваш адрес. Чтобы подключить, передайте нам, и мы включим:

- `callbackUrl` — **HTTPS**-адрес, доступный из интернета (localhost и адреса внутренних/частных сетей отклоняются);
- `httpMethod` — `POST` или `PATCH`;
- аутентификация: `NONE`, `BASIC_AUTH` (логин + пароль), `BEARER_TOKEN` (токен) или `API_KEY_HEADER` (имя заголовка + значение) — мы добавим это в запрос;
- при необходимости — соответствующий секрет (хранится у нас в зашифрованном виде).

## Запрос

Мы отправляем на ваш адрес JSON:

```http
POST <callbackUrl>   (or PATCH)
Content-Type: application/json
<auth header if configured>

```

```json
{ "entityType": "FORM0581", "entityId": 311, "oldStatus": "SENT", "newStatus": "ACCEPTED", "occurredAt": "2026-10-26T14:15:30.123+05:00" }
```

**Поля:**

- `entityType` — тип формы: `FORM0581`
- `entityId` — числовой ID формы — тот же `id`, что в ответе на создание (не `uuid`)
- `oldStatus` — прежний статус
- `newStatus` — новый статус
- `occurredAt` — время изменения, ISO-8601 со смещением часового пояса (например `+05:00`)

## Ваш ответ

Успехом считается только ответ **`2xx`**. Любой другой код, ошибка соединения или таймаут — отправка будет повторена. Тело ответа не читается — быстро возвращайте `200`/`204`, а обработку выполняйте отдельно.

## Повторы

Неудачная отправка повторяется с интервалами (по умолчанию): **1 мин, 5 мин, 15 мин, 1 час, 6 часов**. Итого 1 основная + 5 повторных попыток; после этого отправка прекращается и автоматически не возобновляется (при необходимости попросите нас повторить). Повторы могут менять порядок уведомлений. Если ваш адрес подряд отвечает ошибками, отправка для вашего клиента ненадолго приостанавливается (circuit breaker) — такие попытки тоже засчитываются.

## Рекомендации

- Делайте обработку **идемпотентной**: одно и то же уведомление может прийти дважды.
- Не полагайтесь на порядок: ориентируйтесь на `occurredAt` и `newStatus`, берите самое свежее.
- Если нужны подробности, запросите их сами после уведомления (например, `GET /integration/v1/{source}/patients/case`).
- В теле webhook нет подписи — для защиты используйте заголовок аутентификации и (по возможности) ограничьте наши IP-адреса.
- Webhook не содержит причину (`reason`) отмены/отказа.

---

<a id="en"></a>
# 🇬🇧 English

A registered client can automatically receive a notification (webhook) in its own system whenever the status of its forms changes, so there is no need to poll the status with `GET`.

## When it is sent

Only for **FORM0581** forms submitted through this integration client, and only **when the status changes** (no webhook is sent for the initial `SENT` at creation — it is already in the creation response). The webhook must be enabled.

## Status changes

| Old status | New status | Reason |
|---|---|---|
| `SENT` | `ACCEPTED` | the receiver accepted the form |
| `ACCEPTED` | `CARD_LINKED` | a card was linked to the form |
| `CARD_LINKED` | `CARD_LINKED` | another card was linked (the status does not change, but a notification is still sent) |
| `CARD_LINKED` | `APPROVED` | the sender gave the final approval (only after a card is linked) |
| `SENT` | `CANCELED` | the sender withdrew the form or the receiver rejected it |
| `CANCELED` | `SENT` | a super admin reopened the form |

**Lifecycle:**

```
SENT ─► ACCEPTED ─► CARD_LINKED ─► APPROVED
  └─► CANCELED ─(reopen)─► SENT
```

## Setup

The webhook is delivered to an address you operate. To connect, give us the following and we will enable it:

- `callbackUrl` — an **HTTPS** address reachable from the internet (localhost and internal/private-network addresses are rejected);
- `httpMethod` — `POST` or `PATCH`;
- authentication: `NONE`, `BASIC_AUTH` (username + password), `BEARER_TOKEN` (token) or `API_KEY_HEADER` (header name + value) — we add it to the request;
- the matching secret, if any (stored encrypted on our side).

## Request

We send JSON to your address:

```http
POST <callbackUrl>   (or PATCH)
Content-Type: application/json
<auth header if configured>

```

```json
{ "entityType": "FORM0581", "entityId": 311, "oldStatus": "SENT", "newStatus": "ACCEPTED", "occurredAt": "2026-10-26T14:15:30.123+05:00" }
```

**Fields:**

- `entityType` — form type: `FORM0581`
- `entityId` — numeric form ID — the same `id` as in the creation response (not the `uuid`)
- `oldStatus` — previous status
- `newStatus` — new status
- `occurredAt` — time of change, ISO-8601 with a time-zone offset (e.g. `+05:00`)

## Your response

Only a **`2xx`** response counts as success. Any other code, a connection error or a timeout causes the delivery to be retried. The response body is not read — return `200`/`204` quickly and process asynchronously.

## Retries

A failed delivery is retried at these intervals (default): **1 min, 5 min, 15 min, 1 h, 6 h**. That is 1 initial + 5 retry attempts; after that delivery stops and is not resumed automatically (ask us to resend if needed). Retries can reorder notifications. If your endpoint keeps failing, delivery for your client is briefly paused (circuit breaker) — those attempts still count.

## Recommendations

- Make your handler **idempotent**: the same notification may arrive twice.
- Do not rely on ordering: use `occurredAt` and `newStatus` and keep the latest.
- If you need details, fetch them yourself after the notification (e.g. `GET /integration/v1/{source}/patients/case`).
- The webhook body is not signed — protect the endpoint with the authentication header and, where possible, restrict our IP addresses.
- The webhook does not include the cancel/reject `reason`.

---
