# DMED — Form 058 / Форма №058

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

`POST /integration/v1/DMED/form-058` · scope `form058:submit` / `form058:confirm` · [form058](../README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

DMED uchun **tekis** (flat) formatdagi qat'iy kontrakt. Umumiy `{source}` endpointidan farqi: maydonlar ichma-ich bloklarsiz, to'g'ridan-to'g'ri yuqori darajada.

## Qanday chaqiriladi

- `{source}` — **doim `DMED`** (sizning `sourceKey` ham `dmed` bo'lishi kerak, aks holda `403`).
- Autentifikatsiya (API key) va umumiy tartib: [DMED umumiy ko'rinish](../../dmed/README.md#uz), [auth](../../auth/README.md). Scope: `form058:submit`.
- Tashkilot **tanadan olinmaydi**: `senderOrganizationUuid` yuborilmaydi (yuborsangiz ham e'tiborga olinmaydi). Tashkilotni `X-Organization-Id` header bilan bering — **majburiy**, yo'q bo'lsa `403`. Formani yuborgan xodim tanada `submittedBy` bilan beriladi — **majburiy**, yo'q bo'lsa `400`.

| Maydon | Maj. | Izoh |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `mkb10UsageLimit` | ✔ | ≥ 0 |
| `finalMkb10Code` | ✔ | yakuniy diagnoz kodi (≤ 20) |
| `finalMkb10Name` | ✔ | yakuniy diagnoz nomi (≤ 512) |
| `patient` | ✔ | [patient bloki](../../patient/README.md#uz); **`identifiers` ichida ikkalasi ham majburiy: `NNUZB` (= PINFL/JSHSHIR) va `PPN` (= pasport); `addresses` ichida kamida bitta `PERMANENT` manzil majburiy** |
| `labConfirmation` | ✔ | boolean |
| `hospitalPlaceId` | ✔ | UUID |
| `admissionDate` | ✔ | `yyyy-MM-dd'T'HH:mm:ss`, o'tmish/hozir |
| `diseaseDate` | ✔ | xuddi shunday |
| `firstVisitDate` | ✔ | xuddi shunday |
| `diagnosisDate` | ✔ | xuddi shunday |
| `visitDate` | ✔ | xuddi shunday |
| `initialReportDateTime` | ✔ | xuddi shunday |
| `receiverOrganizationId` | ✔ | UUID |
| `location` | ✔ | `{ latitude, longitude, location }` |
| `diseasePlaceCode` | ✔ | ma'lumotnoma kodi (≤ 64) |
| `diseaseCause` | ✔ | ≤ 2000 |
| `epidemicMeasures` | ✔ | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `journalFormCode` | ✔ | ma'lumotnoma kodi (≤ 64) |
| `comment` | ✔ | ≤ 2000 |
| `submittedBy` | ✔ | Formani yuborgan xodim: `{ pinfl, fullName, position, phone }`. `pinfl`, `fullName`, `position` majburiy, `phone` ixtiyoriy. Audit uchun saqlanadi (kim yuborgani) |

## Qo'shimcha qoidalar

- Sanalar tartibi: `diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.
- `NNUZB` (PINFL/JSHSHIR) identifikatori — 14 raqam va kontrol raqamdan o'tishi shart.
- Manzillarda `stateCode`/`cityCode` — `regionCode`/`districtCode` ning sinonimi.

## Namuna

```json
{
  "mkb10Code": "A01.0", "mkb10Name": "Typhoid fever", "mkb10UsageLimit": 0,
  "finalMkb10Code": "A01.0", "finalMkb10Name": "Typhoid fever",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "NNUZB", "value": "31705901234560" }, { "type": "PPN", "value": "AA1234567" } ],
    "addresses": [ { "type": "PERMANENT", "stateCode": "UZ-QA", "cityCode": "QA-212", "streetAddress": "…" } ]
  },
  "labConfirmation": true, "hospitalPlaceId": "0b2f6b1e-5c1a-4f0e-9d53-6a8e5c0c1a11",
  "admissionDate": "2026-10-02T11:00:00", "diseaseDate": "2026-10-01T08:00:00",
  "firstVisitDate": "2026-10-02T09:30:00", "diagnosisDate": "2026-10-02T12:00:00",
  "visitDate": "2026-10-02T09:30:00", "initialReportDateTime": "2026-10-02T10:00:00",
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "location": { "latitude": 41.31, "longitude": 69.24, "location": "Tashkent" },
  "diseasePlaceCode": "HOME", "diseaseCause": "…", "epidemicMeasures": "…",
  "notifierFullName": "Dr. Karimov", "journalFormCode": "F058", "comment": "…",
  "submittedBy": { "pinfl": "31705901234560", "fullName": "Dr. Karimov", "position": "Epidemiolog", "phone": "+998901234567" }
}
```

## Javob

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

## Tasdiqlash (confirm)

Yuborilgan shaklni yakuniy diagnoz (MKB-10) bilan tasdiqlaydi — status `APPROVED` bo'ladi.

`POST /integration/v1/DMED/form-058/{idOrUuid}/confirm` · scope `form058:confirm`

- Shakl yo'lda `{idOrUuid}` bilan ko'rsatiladi: raqamli `id` **yoki** `uuid` (yuborishda javobda qaytgan qiymat).
- `{source}` — **doim `DMED`**; `sourceKey` ham `dmed` bo'lishi kerak, aks holda `403`. Tashkilot `X-Organization-Id` header bilan beriladi (**majburiy**); tanada `senderOrganizationUuid` yo'q, tasdiqlagan xodim `submittedBy` bilan beriladi (**majburiy**).
- Tasdiqlashni **faqat shaklni yuborgan tashkilot** qila oladi va faqat qabul qiluvchi shaklni qabul qilgandan keyin (status `ACCEPTED` / `CARD_LINKED`); boshqa statusda `409`, boshqa tashkilot bo'lsa `403`.
- Scope `form058:confirm` — `form058:submit` dan **alohida**; mijozga alohida berilishi kerak, aks holda `403`.

| Maydon | Maj. | Izoh |
|---|---|---|
| `finalIcd10Code` | ✔ | MKB-10 kodi, ≤ 32; **ma'lumotnomada bo'lishi shart**, aks holda `400` |
| `finalIcd10Name` | ✔ | ≤ 1024; diagnoz nomi |
| `submittedBy` | ✔ | Tasdiqlagan xodim: `{ pinfl, fullName, position, phone }` (yuborishdagi kabi). Audit uchun saqlanadi |

```json
{ "finalIcd10Code": "A01.0", "finalIcd10Name": "Typhoid fever", "submittedBy": { "pinfl": "31705901234560", "fullName": "Dr. Karimov", "position": "Epidemiolog", "phone": "+998901234567" } }
```

Javob (yuborishdagi bilan bir xil shakl):

```json
{ "id": 1042, "uuid": "…", "status": "APPROVED" }
```

- **Takroriy chaqiruv xavfsiz:** shakl allaqachon shu diagnoz bilan `APPROVED` bo'lsa, xatosiz joriy holat qaytariladi (qayta tasdiqlanmaydi, webhook ham qayta ketmaydi).
- Boshqa diagnoz bilan `APPROVED` shaklni qayta tasdiqlash — `409` (status endi tasdiqlanadigan emas).
- Status o'zgarganda webhook: [webhook](../webhook/README.md).

Statuslar o'zgarganda webhook: [webhook](../webhook/README.md).

---

<a id="ru"></a>
# 🇷🇺 Русский

Фиксированный **плоский** контракт для DMED. Отличие от общего `{source}`-эндпоинта: поля идут на верхнем уровне, без вложенных блоков.

## Как вызывать

- `{source}` — **всегда `DMED`** (ваш `sourceKey` тоже должен быть `dmed`, иначе `403`).
- Аутентификация (API key) и общий порядок: [обзор DMED](../../dmed/README.md#ru), [auth](../../auth/README.md). Scope: `form058:submit`.
- Организация **не берётся из тела**: `senderOrganizationUuid` не передаётся (если передать — игнорируется). Организацию указывайте заголовком `X-Organization-Id` — он **обязателен**, без него `403`. Сотрудник, отправивший форму, передаётся в теле как `submittedBy` — **обязателен**, без него `400`.

| Поле | Обяз. | Описание |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `mkb10UsageLimit` | ✔ | ≥ 0 |
| `finalMkb10Code` | ✔ | итоговый код диагноза (≤ 20) |
| `finalMkb10Name` | ✔ | итоговое название диагноза (≤ 512) |
| `patient` | ✔ | [блок patient](../../patient/README.md#ru); **в `identifiers` обязательны оба: `NNUZB` (= PINFL/JSHSHIR) и `PPN` (= паспорт); в `addresses` обязателен хотя бы один адрес `PERMANENT`** |
| `labConfirmation` | ✔ | boolean |
| `hospitalPlaceId` | ✔ | UUID |
| `admissionDate` | ✔ | `yyyy-MM-dd'T'HH:mm:ss`, не в будущем |
| `diseaseDate` | ✔ | аналогично |
| `firstVisitDate` | ✔ | аналогично |
| `diagnosisDate` | ✔ | аналогично |
| `visitDate` | ✔ | аналогично |
| `initialReportDateTime` | ✔ | аналогично |
| `receiverOrganizationId` | ✔ | UUID |
| `location` | ✔ | `{ latitude, longitude, location }` |
| `diseasePlaceCode` | ✔ | код справочника (≤ 64) |
| `diseaseCause` | ✔ | ≤ 2000 |
| `epidemicMeasures` | ✔ | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `journalFormCode` | ✔ | код справочника (≤ 64) |
| `comment` | ✔ | ≤ 2000 |
| `submittedBy` | ✔ | Сотрудник, отправивший форму: `{ pinfl, fullName, position, phone }`. `pinfl`, `fullName`, `position` обязательны, `phone` необязателен. Сохраняется в аудите (кто отправил) |

## Дополнительные правила

- Порядок дат: `diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.
- Идентификатор `NNUZB` (PINFL/JSHSHIR) — 14 цифр с проверкой контрольной цифры.
- В адресах `stateCode`/`cityCode` — синонимы `regionCode`/`districtCode`.

## Пример

```json
{
  "mkb10Code": "A01.0", "mkb10Name": "Typhoid fever", "mkb10UsageLimit": 0,
  "finalMkb10Code": "A01.0", "finalMkb10Name": "Typhoid fever",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "NNUZB", "value": "31705901234560" }, { "type": "PPN", "value": "AA1234567" } ],
    "addresses": [ { "type": "PERMANENT", "stateCode": "UZ-QA", "cityCode": "QA-212", "streetAddress": "…" } ]
  },
  "labConfirmation": true, "hospitalPlaceId": "0b2f6b1e-5c1a-4f0e-9d53-6a8e5c0c1a11",
  "admissionDate": "2026-10-02T11:00:00", "diseaseDate": "2026-10-01T08:00:00",
  "firstVisitDate": "2026-10-02T09:30:00", "diagnosisDate": "2026-10-02T12:00:00",
  "visitDate": "2026-10-02T09:30:00", "initialReportDateTime": "2026-10-02T10:00:00",
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "location": { "latitude": 41.31, "longitude": 69.24, "location": "Tashkent" },
  "diseasePlaceCode": "HOME", "diseaseCause": "…", "epidemicMeasures": "…",
  "notifierFullName": "Dr. Karimov", "journalFormCode": "F058", "comment": "…",
  "submittedBy": { "pinfl": "31705901234560", "fullName": "Dr. Karimov", "position": "Epidemiolog", "phone": "+998901234567" }
}
```

## Ответ

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

## Подтверждение (confirm)

Подтверждает ранее отправленную форму итоговым диагнозом (МКБ-10) — статус становится `APPROVED`.

`POST /integration/v1/DMED/form-058/{idOrUuid}/confirm` · scope `form058:confirm`

- Форма указывается в пути как `{idOrUuid}`: числовой `id` **или** `uuid` (значение из ответа при отправке).
- `{source}` — **всегда `DMED`**; ваш `sourceKey` тоже должен быть `dmed`, иначе `403`. Организация передаётся заголовком `X-Organization-Id` (**обязателен**); `senderOrganizationUuid` в теле нет, подтвердивший сотрудник передаётся как `submittedBy` (**обязателен**).
- Подтвердить может **только организация-отправитель формы**, и только после принятия формы получателем (статус `ACCEPTED` / `CARD_LINKED`); в другом статусе — `409`, для чужой организации — `403`.
- Scope `form058:confirm` — **отдельный** от `form058:submit`; его нужно выдать клиенту отдельно, иначе `403`.

| Поле | Обяз. | Описание |
|---|---|---|
| `finalIcd10Code` | ✔ | код МКБ-10, ≤ 32; **должен существовать в справочнике**, иначе `400` |
| `finalIcd10Name` | ✔ | ≤ 1024; название диагноза |
| `submittedBy` | ✔ | Сотрудник, подтвердивший форму: `{ pinfl, fullName, position, phone }` (как при отправке). Сохраняется в аудите |

```json
{ "finalIcd10Code": "A01.0", "finalIcd10Name": "Typhoid fever", "submittedBy": { "pinfl": "31705901234560", "fullName": "Dr. Karimov", "position": "Epidemiolog", "phone": "+998901234567" } }
```

Ответ (тот же формат, что при отправке):

```json
{ "id": 1042, "uuid": "…", "status": "APPROVED" }
```

- **Повторный вызов безопасен:** если форма уже `APPROVED` с этим же диагнозом, возвращается текущее состояние без ошибки (повторного подтверждения и повторного webhook нет).
- Подтверждение уже `APPROVED` формы с другим диагнозом — `409` (статус больше не подтверждаемый).
- Webhook при смене статуса: [webhook](../webhook/README.md).

Webhook при смене статуса: [webhook](../webhook/README.md).

---

<a id="en"></a>
# 🇬🇧 English

A fixed **flat** contract for DMED. Unlike the generic `{source}` endpoint, fields sit at the top level with no nested blocks.

## How to call

- `{source}` — **always `DMED`** (your `sourceKey` must also be `dmed`, otherwise `403`).
- Authentication (API key) and overall flow: [DMED overview](../../dmed/README.md#en), [auth](../../auth/README.md). Scope: `form058:submit`.
- The organization is **not read from the body**: do not send `senderOrganizationUuid` (ignored if sent). Provide the organization with the `X-Organization-Id` header — it is **mandatory**, `403` without it. The staff member submitting is sent in the body as `submittedBy` — **mandatory**, `400` without it.

| Field | Req. | Notes |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `mkb10UsageLimit` | ✔ | ≥ 0 |
| `finalMkb10Code` | ✔ | final diagnosis code (≤ 20) |
| `finalMkb10Name` | ✔ | final diagnosis name (≤ 512) |
| `patient` | ✔ | [patient block](../../patient/README.md#en); **`identifiers` must contain both `NNUZB` (= PINFL/JSHSHIR) and `PPN` (= passport); `addresses` must contain at least one `PERMANENT` address** |
| `labConfirmation` | ✔ | boolean |
| `hospitalPlaceId` | ✔ | UUID |
| `admissionDate` | ✔ | `yyyy-MM-dd'T'HH:mm:ss`, not in the future |
| `diseaseDate` | ✔ | same |
| `firstVisitDate` | ✔ | same |
| `diagnosisDate` | ✔ | same |
| `visitDate` | ✔ | same |
| `initialReportDateTime` | ✔ | same |
| `receiverOrganizationId` | ✔ | UUID |
| `location` | ✔ | `{ latitude, longitude, location }` |
| `diseasePlaceCode` | ✔ | reference code (≤ 64) |
| `diseaseCause` | ✔ | ≤ 2000 |
| `epidemicMeasures` | ✔ | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `journalFormCode` | ✔ | reference code (≤ 64) |
| `comment` | ✔ | ≤ 2000 |
| `submittedBy` | ✔ | The staff member who submitted the form: `{ pinfl, fullName, position, phone }`. `pinfl`, `fullName`, `position` are required, `phone` is optional. Stored in the audit (who sent it) |

## Extra rules

- Date order: `diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.
- An `NNUZB` (PINFL/JSHSHIR) identifier must be 14 digits and pass the check-digit rule.
- In addresses `stateCode`/`cityCode` are aliases of `regionCode`/`districtCode`.

## Example

```json
{
  "mkb10Code": "A01.0", "mkb10Name": "Typhoid fever", "mkb10UsageLimit": 0,
  "finalMkb10Code": "A01.0", "finalMkb10Name": "Typhoid fever",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "NNUZB", "value": "31705901234560" }, { "type": "PPN", "value": "AA1234567" } ],
    "addresses": [ { "type": "PERMANENT", "stateCode": "UZ-QA", "cityCode": "QA-212", "streetAddress": "…" } ]
  },
  "labConfirmation": true, "hospitalPlaceId": "0b2f6b1e-5c1a-4f0e-9d53-6a8e5c0c1a11",
  "admissionDate": "2026-10-02T11:00:00", "diseaseDate": "2026-10-01T08:00:00",
  "firstVisitDate": "2026-10-02T09:30:00", "diagnosisDate": "2026-10-02T12:00:00",
  "visitDate": "2026-10-02T09:30:00", "initialReportDateTime": "2026-10-02T10:00:00",
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "location": { "latitude": 41.31, "longitude": 69.24, "location": "Tashkent" },
  "diseasePlaceCode": "HOME", "diseaseCause": "…", "epidemicMeasures": "…",
  "notifierFullName": "Dr. Karimov", "journalFormCode": "F058", "comment": "…",
  "submittedBy": { "pinfl": "31705901234560", "fullName": "Dr. Karimov", "position": "Epidemiolog", "phone": "+998901234567" }
}
```

## Response

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

## Confirm

Confirms a previously submitted form with a final diagnosis (ICD-10) — the status becomes `APPROVED`.

`POST /integration/v1/DMED/form-058/{idOrUuid}/confirm` · scope `form058:confirm`

- The form is identified in the path as `{idOrUuid}`: the numeric `id` **or** the `uuid` (as returned on submit).
- `{source}` — **always `DMED`**; your `sourceKey` must also be `dmed`, otherwise `403`. The organization comes from the `X-Organization-Id` header (**mandatory**); there is no `senderOrganizationUuid` in the body, the confirming staff member is sent as `submittedBy` (**mandatory**).
- **Only the organization that sent the form** can confirm it, and only after the receiver has accepted it (status `ACCEPTED` / `CARD_LINKED`); any other status gives `409`, a different organization gives `403`.
- Scope `form058:confirm` is **separate** from `form058:submit`; it must be granted to the client explicitly, otherwise `403`.

| Field | Req. | Notes |
|---|---|---|
| `finalIcd10Code` | ✔ | ICD-10 code, ≤ 32; **must exist in the catalog**, otherwise `400` |
| `finalIcd10Name` | ✔ | ≤ 1024; diagnosis name |
| `submittedBy` | ✔ | The staff member confirming: `{ pinfl, fullName, position, phone }` (as on submit). Stored in the audit |

```json
{ "finalIcd10Code": "A01.0", "finalIcd10Name": "Typhoid fever", "submittedBy": { "pinfl": "31705901234560", "fullName": "Dr. Karimov", "position": "Epidemiolog", "phone": "+998901234567" } }
```

Response (same shape as on submit):

```json
{ "id": 1042, "uuid": "…", "status": "APPROVED" }
```

- **Repeating the call is safe:** if the form is already `APPROVED` with the same diagnosis, the current state is returned without error (no re-approval, no second webhook).
- Confirming an already `APPROVED` form with a different diagnosis gives `409` (the status is no longer confirmable).
- Status-change webhook: [webhook](../webhook/README.md).

Status-change webhook: [webhook](../webhook/README.md).

---
