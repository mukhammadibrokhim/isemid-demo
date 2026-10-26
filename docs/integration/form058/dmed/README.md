# DMED — Form 058 / Форма №058

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

`POST /integration/v1/DMED/form-058` · scope `form058:submit` · [form058](../README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

DMED uchun **tekis** (flat) formatdagi qat'iy kontrakt. Umumiy `{source}` endpointidan farqi: maydonlar ichma-ich bloklarsiz, to'g'ridan-to'g'ri yuqori darajada.

## Qanday chaqiriladi

- `{source}` — **doim `DMED`** (sizning `sourceKey` ham `dmed` bo'lishi kerak, aks holda `403`).
- Autentifikatsiya: [auth](../../auth/README.md). Scope: `form058:submit`.
- Tashkilot **tanadan olinmaydi**: `senderOrganizationUuid` va `submittedBy` yuborilmaydi (yuborsangiz ham e'tiborga olinmaydi). Tashkilotni `X-Organization-Id` header bilan bering — **majburiy**, yo'q bo'lsa `403`.

| Maydon | Maj. | Izoh |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `mkb10UsageLimit` |  | ≥ 0 |
| `finalMkb10Code` |  | yakuniy diagnoz kodi (≤ 20); bo'lmasa `mkb10Code` ga teng |
| `finalMkb10Name` |  | yakuniy diagnoz nomi (≤ 512) |
| `patient` | ✔ | [patient bloki](../../patient/README.md#uz); **`identifiers` ichida ikkalasi ham majburiy: `NNUZB` (= PINFL/JSHSHIR) va `PPN` (= pasport); `addresses` ichida kamida bitta `PERMANENT` manzil majburiy** |
| `labConfirmation` |  | boolean |
| `hospitalPlaceId` |  | UUID |
| `admissionDate` |  | `yyyy-MM-dd'T'HH:mm:ss`, o'tmish/hozir |
| `diseaseDate` | ✔ | xuddi shunday |
| `firstVisitDate` | ✔ | xuddi shunday |
| `diagnosisDate` |  | xuddi shunday |
| `visitDate` | ✔ | xuddi shunday |
| `initialReportDateTime` | ✔ | xuddi shunday |
| `receiverOrganizationId` | ✔ | UUID |
| `location` |  | `{ latitude, longitude, location }` |
| `diseasePlaceCode` | ✔ | ma'lumotnoma kodi (≤ 64) |
| `diseaseCause` |  | ≤ 2000 |
| `epidemicMeasures` |  | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `journalFormCode` | ✔ | ma'lumotnoma kodi (≤ 64) |
| `comment` |  | ≤ 2000 |

## Qo'shimcha qoidalar

- Sanalar tartibi: `diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.
- `NNUZB` (PINFL/JSHSHIR) identifikatori — 14 raqam va kontrol raqamdan o'tishi shart.
- Manzillarda `stateCode`/`cityCode` — `regionCode`/`districtCode` ning sinonimi.

## Namuna

```json
{
  "mkb10Code": "A01.0", "mkb10Name": "Typhoid fever",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "NNUZB", "value": "31705901234560" }, { "type": "PPN", "value": "AA1234567" } ],
    "addresses": [ { "type": "PERMANENT", "stateCode": "UZ-QA", "cityCode": "QA-212", "streetAddress": "…" } ]
  },
  "labConfirmation": true, "diseasePlaceCode": "HOME",
  "diseaseDate": "2026-10-01T08:00:00", "firstVisitDate": "2026-10-02T09:30:00",
  "visitDate": "2026-10-02T09:30:00", "initialReportDateTime": "2026-10-02T10:00:00",
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "notifierFullName": "Dr. Karimov", "journalFormCode": "F058"
}
```

## Javob

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

Statuslar o'zgarganda webhook: [webhook](../webhook/README.md).

---

<a id="ru"></a>
# 🇷🇺 Русский

Фиксированный **плоский** контракт для DMED. Отличие от общего `{source}`-эндпоинта: поля идут на верхнем уровне, без вложенных блоков.

## Как вызывать

- `{source}` — **всегда `DMED`** (ваш `sourceKey` тоже должен быть `dmed`, иначе `403`).
- Аутентификация: [auth](../../auth/README.md). Scope: `form058:submit`.
- Организация **не берётся из тела**: `senderOrganizationUuid` и `submittedBy` не передаются (если передать — игнорируются). Организацию указывайте заголовком `X-Organization-Id` — он **обязателен**, без него `403`.

| Поле | Обяз. | Описание |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `mkb10UsageLimit` |  | ≥ 0 |
| `finalMkb10Code` |  | итоговый код диагноза (≤ 20); если нет — равен `mkb10Code` |
| `finalMkb10Name` |  | итоговое название диагноза (≤ 512) |
| `patient` | ✔ | [блок patient](../../patient/README.md#ru); **в `identifiers` обязательны оба: `NNUZB` (= PINFL/JSHSHIR) и `PPN` (= паспорт); в `addresses` обязателен хотя бы один адрес `PERMANENT`** |
| `labConfirmation` |  | boolean |
| `hospitalPlaceId` |  | UUID |
| `admissionDate` |  | `yyyy-MM-dd'T'HH:mm:ss`, не в будущем |
| `diseaseDate` | ✔ | аналогично |
| `firstVisitDate` | ✔ | аналогично |
| `diagnosisDate` |  | аналогично |
| `visitDate` | ✔ | аналогично |
| `initialReportDateTime` | ✔ | аналогично |
| `receiverOrganizationId` | ✔ | UUID |
| `location` |  | `{ latitude, longitude, location }` |
| `diseasePlaceCode` | ✔ | код справочника (≤ 64) |
| `diseaseCause` |  | ≤ 2000 |
| `epidemicMeasures` |  | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `journalFormCode` | ✔ | код справочника (≤ 64) |
| `comment` |  | ≤ 2000 |

## Дополнительные правила

- Порядок дат: `diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.
- Идентификатор `NNUZB` (PINFL/JSHSHIR) — 14 цифр с проверкой контрольной цифры.
- В адресах `stateCode`/`cityCode` — синонимы `regionCode`/`districtCode`.

## Пример

```json
{
  "mkb10Code": "A01.0", "mkb10Name": "Typhoid fever",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "NNUZB", "value": "31705901234560" }, { "type": "PPN", "value": "AA1234567" } ],
    "addresses": [ { "type": "PERMANENT", "stateCode": "UZ-QA", "cityCode": "QA-212", "streetAddress": "…" } ]
  },
  "labConfirmation": true, "diseasePlaceCode": "HOME",
  "diseaseDate": "2026-10-01T08:00:00", "firstVisitDate": "2026-10-02T09:30:00",
  "visitDate": "2026-10-02T09:30:00", "initialReportDateTime": "2026-10-02T10:00:00",
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "notifierFullName": "Dr. Karimov", "journalFormCode": "F058"
}
```

## Ответ

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

Webhook при смене статуса: [webhook](../webhook/README.md).

---

<a id="en"></a>
# 🇬🇧 English

A fixed **flat** contract for DMED. Unlike the generic `{source}` endpoint, fields sit at the top level with no nested blocks.

## How to call

- `{source}` — **always `DMED`** (your `sourceKey` must also be `dmed`, otherwise `403`).
- Authentication: [auth](../../auth/README.md). Scope: `form058:submit`.
- The organization is **not read from the body**: do not send `senderOrganizationUuid` / `submittedBy` (they are ignored if sent). Provide the organization with the `X-Organization-Id` header — it is **mandatory**, `403` without it.

| Field | Req. | Notes |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `mkb10UsageLimit` |  | ≥ 0 |
| `finalMkb10Code` |  | final diagnosis code (≤ 20); defaults to `mkb10Code` |
| `finalMkb10Name` |  | final diagnosis name (≤ 512) |
| `patient` | ✔ | [patient block](../../patient/README.md#en); **`identifiers` must contain both `NNUZB` (= PINFL/JSHSHIR) and `PPN` (= passport); `addresses` must contain at least one `PERMANENT` address** |
| `labConfirmation` |  | boolean |
| `hospitalPlaceId` |  | UUID |
| `admissionDate` |  | `yyyy-MM-dd'T'HH:mm:ss`, not in the future |
| `diseaseDate` | ✔ | same |
| `firstVisitDate` | ✔ | same |
| `diagnosisDate` |  | same |
| `visitDate` | ✔ | same |
| `initialReportDateTime` | ✔ | same |
| `receiverOrganizationId` | ✔ | UUID |
| `location` |  | `{ latitude, longitude, location }` |
| `diseasePlaceCode` | ✔ | reference code (≤ 64) |
| `diseaseCause` |  | ≤ 2000 |
| `epidemicMeasures` |  | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `journalFormCode` | ✔ | reference code (≤ 64) |
| `comment` |  | ≤ 2000 |

## Extra rules

- Date order: `diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.
- An `NNUZB` (PINFL/JSHSHIR) identifier must be 14 digits and pass the check-digit rule.
- In addresses `stateCode`/`cityCode` are aliases of `regionCode`/`districtCode`.

## Example

```json
{
  "mkb10Code": "A01.0", "mkb10Name": "Typhoid fever",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "NNUZB", "value": "31705901234560" }, { "type": "PPN", "value": "AA1234567" } ],
    "addresses": [ { "type": "PERMANENT", "stateCode": "UZ-QA", "cityCode": "QA-212", "streetAddress": "…" } ]
  },
  "labConfirmation": true, "diseasePlaceCode": "HOME",
  "diseaseDate": "2026-10-01T08:00:00", "firstVisitDate": "2026-10-02T09:30:00",
  "visitDate": "2026-10-02T09:30:00", "initialReportDateTime": "2026-10-02T10:00:00",
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "notifierFullName": "Dr. Karimov", "journalFormCode": "F058"
}
```

## Response

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

Status-change webhook: [webhook](../webhook/README.md).

---
