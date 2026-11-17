# DMED integration — Overview & Authentication / Обзор и аутентификация / Umumiy ko'rinish va autentifikatsiya

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

Endpoints: [Form 058](../form058/dmed/README.md) · [Form 058-1](../form0581/dmed/README.md) · General auth reference: [auth](../auth/README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

DMED bizning API'ni chaqiradi: shaklni yuboradi (`form-058`, `form-058-1`), keyin yakuniy diagnoz bilan tasdiqlaydi (`confirm`). Autentifikatsiya — **API key**.

## 1. Kalit (credentials)

Kalit va qolgan ma'lumotlar **keyinroq alohida taqdim etiladi**. Shunda quyidagilar beriladi:

| Nima | Izoh |
|---|---|
| API key | `<clientId>.<token>` ko'rinishida (masalan `ic_xxx.yyyyyy`); **bir marta** ko'rsatiladi, xavfsiz saqlang |
| `sourceKey` | doim `dmed` — URL dagi `{source}` o'rniga **`DMED`** yoziladi |
| Scope'lar | `form058:submit`, `form058:confirm`, `form0581:submit`, `form0581:confirm` |
| Tashkilot UUID'lari | qaysi tashkilotlar nomidan yuborishi mumkin (`X-Organization-Id` uchun) |
| IP ro'yxati | agar belgilangan bo'lsa, faqat shu IP'lardan murojaat qilish mumkin |
| Host | test va production manzillari |

Kalit berilguncha quyidagi so'rov formatiga tayyorlanib turing.

## 2. Har bir so'rovda nima yuboriladi

| Header | Qiymat | Majburiy |
|---|---|---|
| `X-Api-Key` | `<clientId>.<token>` (butun satr, o'zgartirmasdan) | ✔ |
| `X-Organization-Id` | shaklni yuborayotgan tashkilot UUID'si | ✔ (yo'q bo'lsa `403`) |
| `Content-Type` | `application/json` | ✔ |

- `Authorization` header'ini **yubormang** (API key bilan birga kerak emas).
- Tashkilot faqat `X-Organization-Id` header'idan olinadi. Tanada `senderOrganizationUuid` yuborilmaydi.
- Formani yuborgan/tasdiqlagan xodim har doim tanada `submittedBy` bilan beriladi (`{ pinfl, fullName, position, phone }`), majburiy.
- Token olish bosqichi **yo'q**: API key to'g'ridan-to'g'ri har so'rovda yuboriladi.

## 3. Ish tartibi

1. **Yuborish:** `POST /integration/v1/DMED/form-058` yoki `.../form-058-1` → javob `{ "id", "uuid", "status": "SENT" }`.
2. Qabul qiluvchi tashkilot shaklni qabul qiladi (status `ACCEPTED` / `CARD_LINKED`).
3. **Tasdiqlash:** `POST /integration/v1/DMED/form-058/{idOrUuid}/confirm` (yoki `form-058-1`) → status `APPROVED`. Faqat yuborgan tashkilot tasdiqlay oladi.
4. Status o'zgarganda webhook ketadi (kerak bo'lsa — [Form 058 webhook](../form058/webhook/README.md), [Form 058-1 webhook](../form0581/webhook/README.md)).

## 4. Namuna so'rovlar

```bash
# Yuborish
curl -X POST https://HOST/integration/v1/DMED/form-058 \
  -H "X-Api-Key: ic_xxx.yyyyyy" \
  -H "X-Organization-Id: <TASHKILOT_UUID>" \
  -H "Content-Type: application/json" \
  -d @form-058.json

# Tasdiqlash
curl -X POST https://HOST/integration/v1/DMED/form-058/<ID_YOKI_UUID>/confirm \
  -H "X-Api-Key: ic_xxx.yyyyyy" \
  -H "X-Organization-Id: <TASHKILOT_UUID>" \
  -H "Content-Type: application/json" \
  -d '{"finalIcd10Code":"A01.0","finalIcd10Name":"Typhoid fever","submittedBy":{"pinfl":"31705901234560","fullName":"Dr. Karimov","position":"Epidemiolog"}}'
```

So'rov tanasi: [Form 058](../form058/dmed/README.md), [Form 058-1](../form0581/dmed/README.md).

## 5. Xatolar

| HTTP | Sabab |
|---|---|
| `400` | tana validatsiyadan o'tmadi (majburiy maydon yo'q, `submittedBy` yo'q, noto'g'ri sana tartibi, ma'lumotnomada yo'q kod va h.k.); `VALIDATION_FAILED`, sababi xabarda |
| `401` | API key yo'q, noto'g'ri yoki bekor qilingan |
| `403` | `X-Organization-Id` yo'q yoki ruxsat etilmagan; scope yo'q; `{source}` `DMED` emas; IP ruxsat etilmagan; klient o'chirilgan; boshqa tashkilot shaklini tasdiqlash |
| `409` | shakl hali qabul qilinmagan yoki allaqachon boshqa diagnoz bilan tasdiqlangan |
| `429` | juda ko'p xato urinish — `Retry-After` kuting |

## 6. Tayyorgarlik ro'yxati

- [ ] Kalit, host va tashkilot UUID'lari olindi
- [ ] Har so'rovda `X-Api-Key` va `X-Organization-Id` yuboriladi
- [ ] Tanada `submittedBy` va barcha majburiy maydonlar to'ldiriladi
- [ ] `receiverOrganizationId` — haqiqiy sanepid tashkilot UUID'si
- [ ] Tasdiqlash faqat shakl `ACCEPTED`/`CARD_LINKED` bo'lgandan keyin chaqiriladi

---

<a id="ru"></a>
# 🇷🇺 Русский

DMED вызывает наш API: отправляет форму (`form-058`, `form-058-1`), затем подтверждает её итоговым диагнозом (`confirm`). Аутентификация — **API key**.

## 1. Ключ (credentials)

Ключ и остальные данные **будут выданы позже отдельно**. Выдаются:

| Что | Описание |
|---|---|
| API key | вида `<clientId>.<token>` (например `ic_xxx.yyyyyy`); показывается **один раз**, храните надёжно |
| `sourceKey` | всегда `dmed` — в URL вместо `{source}` пишется **`DMED`** |
| Scope | `form058:submit`, `form058:confirm`, `form0581:submit`, `form0581:confirm` |
| UUID организаций | от имени каких организаций можно отправлять (для `X-Organization-Id`) |
| Список IP | если задан — обращаться можно только с этих IP |
| Хост | адреса test и production |

До получения ключа ориентируйтесь на формат запросов ниже.

## 2. Что отправляется в каждом запросе

| Заголовок | Значение | Обяз. |
|---|---|---|
| `X-Api-Key` | `<clientId>.<token>` (вся строка без изменений) | ✔ |
| `X-Organization-Id` | UUID организации, от имени которой отправляется форма | ✔ (без него `403`) |
| `Content-Type` | `application/json` | ✔ |

- Заголовок `Authorization` **не передавайте** (с API key он не нужен).
- Организация берётся только из заголовка `X-Organization-Id`. `senderOrganizationUuid` в теле не передаётся.
- Сотрудник, отправивший/подтвердивший форму, всегда указывается в теле как `submittedBy` (`{ pinfl, fullName, position, phone }`), обязателен.
- Этапа получения токена **нет**: API key передаётся напрямую в каждом запросе.

## 3. Порядок работы

1. **Отправка:** `POST /integration/v1/DMED/form-058` или `.../form-058-1` → ответ `{ "id", "uuid", "status": "SENT" }`.
2. Организация-получатель принимает форму (статус `ACCEPTED` / `CARD_LINKED`).
3. **Подтверждение:** `POST /integration/v1/DMED/form-058/{idOrUuid}/confirm` (или `form-058-1`) → статус `APPROVED`. Подтвердить может только отправившая организация.
4. При смене статуса уходит webhook (при необходимости — [webhook Form 058](../form058/webhook/README.md), [webhook Form 058-1](../form0581/webhook/README.md)).

## 4. Примеры запросов

```bash
# Отправка
curl -X POST https://HOST/integration/v1/DMED/form-058 \
  -H "X-Api-Key: ic_xxx.yyyyyy" \
  -H "X-Organization-Id: <UUID_ОРГАНИЗАЦИИ>" \
  -H "Content-Type: application/json" \
  -d @form-058.json

# Подтверждение
curl -X POST https://HOST/integration/v1/DMED/form-058/<ID_ИЛИ_UUID>/confirm \
  -H "X-Api-Key: ic_xxx.yyyyyy" \
  -H "X-Organization-Id: <UUID_ОРГАНИЗАЦИИ>" \
  -H "Content-Type: application/json" \
  -d '{"finalIcd10Code":"A01.0","finalIcd10Name":"Typhoid fever","submittedBy":{"pinfl":"31705901234560","fullName":"Dr. Karimov","position":"Epidemiolog"}}'
```

Тело запросов: [Form 058](../form058/dmed/README.md), [Form 058-1](../form0581/dmed/README.md).

## 5. Ошибки

| HTTP | Причина |
|---|---|
| `400` | тело не прошло валидацию (нет обязательного поля, нет `submittedBy`, неверный порядок дат, код отсутствует в справочнике и т. д.); `VALIDATION_FAILED`, причина в сообщении |
| `401` | API key отсутствует, неверен или отозван |
| `403` | нет `X-Organization-Id` или организация не разрешена; нет scope; `{source}` не `DMED`; IP не разрешён; клиент отключён; подтверждение формы другой организации |
| `409` | форма ещё не принята или уже подтверждена с другим диагнозом |
| `429` | слишком много неудачных попыток — подождите `Retry-After` |

## 6. Чек-лист готовности

- [ ] Получены ключ, хост и UUID организаций
- [ ] В каждом запросе передаются `X-Api-Key` и `X-Organization-Id`
- [ ] В теле заполнены `submittedBy` и все обязательные поля
- [ ] `receiverOrganizationId` — реальный UUID санэпид-организации
- [ ] Подтверждение вызывается только когда форма в `ACCEPTED`/`CARD_LINKED`

---

<a id="en"></a>
# 🇬🇧 English

DMED calls our API: it submits a form (`form-058`, `form-058-1`) and later confirms it with a final diagnosis (`confirm`). Authentication is by **API key**.

## 1. Credentials

The key and the remaining details **will be provided separately later**. You will receive:

| Item | Notes |
|---|---|
| API key | shaped `<clientId>.<token>` (e.g. `ic_xxx.yyyyyy`); shown **once**, store it safely |
| `sourceKey` | always `dmed` — write **`DMED`** in place of `{source}` in the URL |
| Scopes | `form058:submit`, `form058:confirm`, `form0581:submit`, `form0581:confirm` |
| Organization UUIDs | the organizations you may submit for (used in `X-Organization-Id`) |
| IP list | if configured, calls are accepted only from these IPs |
| Host | test and production addresses |

Until the key is issued, prepare against the request format below.

## 2. What to send on every request

| Header | Value | Req. |
|---|---|---|
| `X-Api-Key` | `<clientId>.<token>` (the whole string, unchanged) | ✔ |
| `X-Organization-Id` | UUID of the organization the form is submitted for | ✔ (`403` without it) |
| `Content-Type` | `application/json` | ✔ |

- Do **not** send an `Authorization` header (not needed with an API key).
- The organization comes only from the `X-Organization-Id` header. Do not send `senderOrganizationUuid` in the body.
- The staff member who submits/confirms is always given in the body as `submittedBy` (`{ pinfl, fullName, position, phone }`), mandatory.
- There is **no token step**: the API key is sent directly on each request.

## 3. Flow

1. **Submit:** `POST /integration/v1/DMED/form-058` or `.../form-058-1` → response `{ "id", "uuid", "status": "SENT" }`.
2. The receiving organization accepts the form (status `ACCEPTED` / `CARD_LINKED`).
3. **Confirm:** `POST /integration/v1/DMED/form-058/{idOrUuid}/confirm` (or `form-058-1`) → status `APPROVED`. Only the submitting organization can confirm.
4. A webhook is sent on status change (if configured — [Form 058 webhook](../form058/webhook/README.md), [Form 058-1 webhook](../form0581/webhook/README.md)).

## 4. Example requests

```bash
# Submit
curl -X POST https://HOST/integration/v1/DMED/form-058 \
  -H "X-Api-Key: ic_xxx.yyyyyy" \
  -H "X-Organization-Id: <ORGANIZATION_UUID>" \
  -H "Content-Type: application/json" \
  -d @form-058.json

# Confirm
curl -X POST https://HOST/integration/v1/DMED/form-058/<ID_OR_UUID>/confirm \
  -H "X-Api-Key: ic_xxx.yyyyyy" \
  -H "X-Organization-Id: <ORGANIZATION_UUID>" \
  -H "Content-Type: application/json" \
  -d '{"finalIcd10Code":"A01.0","finalIcd10Name":"Typhoid fever","submittedBy":{"pinfl":"31705901234560","fullName":"Dr. Karimov","position":"Epidemiolog"}}'
```

Request bodies: [Form 058](../form058/dmed/README.md), [Form 058-1](../form0581/dmed/README.md).

## 5. Errors

| HTTP | Cause |
|---|---|
| `400` | body failed validation (missing required field, missing `submittedBy`, wrong date order, code not in the catalog, etc.); `VALIDATION_FAILED`, reason in the message |
| `401` | API key missing, wrong or revoked |
| `403` | `X-Organization-Id` missing or not allowed; missing scope; `{source}` is not `DMED`; IP not allowed; client disabled; confirming another organization's form |
| `409` | form not accepted yet, or already confirmed with a different diagnosis |
| `429` | too many failed attempts — wait for `Retry-After` |

## 6. Readiness checklist

- [ ] Key, host and organization UUIDs received
- [ ] Every request carries `X-Api-Key` and `X-Organization-Id`
- [ ] Body includes `submittedBy` and all required fields
- [ ] `receiverOrganizationId` is a real sanitary-epidemiological organization UUID
- [ ] Confirm is called only once the form is `ACCEPTED`/`CARD_LINKED`
