# DMED — Form 058-1 / Форма №058-1

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

`POST /integration/v1/DMED/form-058-1` · scope `form0581:submit` · [form0581](../README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

DMED uchun **tekis** (flat) formatdagi qat'iy kontrakt. Umumiy `{source}` endpointidan farqi: maydonlar ichma-ich bloklarsiz, to'g'ridan-to'g'ri yuqori darajada.

## Qanday chaqiriladi

- `{source}` — **doim `DMED`** (sizning `sourceKey` ham `dmed` bo'lishi kerak, aks holda `403`).
- Autentifikatsiya: [auth](../../auth/README.md). Scope: `form0581:submit`.
- Tashkilot **tanadan olinmaydi**: `senderOrganizationUuid` va `submittedBy` yuborilmaydi (yuborsangiz ham e'tiborga olinmaydi). Tashkilotni `X-Organization-Id` header bilan bering — **majburiy**, yo'q bo'lsa `403`.

| Maydon | Maj. | Izoh |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `injuryLocalization` |  | ≤ 500 |
| `injuryDateTime` | ✔ | o'tmish/hozir |
| `dpuVisitDateTime` | ✔ | o'tmish/hozir; `injuryDateTime` dan keyin yoki teng |
| `injuryRegionCode` | ✔ | ma'lumotnoma kodi (≤ 64) |
| `injuryDistrictCode` | ✔ | ma'lumotnoma kodi (≤ 64) |
| `injuryAddress` |  | ≤ 1000 |
| `animalCategoryCode` |  | ≤ 64 |
| `animalColor, animalType, animalBreed` |  | ≤ 255 |
| `animalOwnerInfo` |  | [form0581](../../form0581/README.md#uz) dagi kabi |
| `patient` | ✔ | [patient bloki](../../patient/README.md#uz) |
| `receiverOrganizationId` | ✔ | UUID; sanitar-epidemiologik xizmat muassasasi |
| `otherPeopleInjured` |  | boolean |
| `otherInjuredPeople[]` |  | [form0581](../../form0581/README.md#uz) dagi kabi |
| `hospitalizedAt` |  | sana-vaqt; `injuryDateTime` dan oldin bo'lmasin |
| `hospitalOrganizationId` |  | UUID |
| `antirabicAssistanceInfo` |  | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `messageSentAt` |  | sana-vaqt |

## Qo'shimcha qoidalar

- Sanalar: `injuryDateTime ≤ dpuVisitDateTime`; `hospitalizedAt ≥ injuryDateTime`.
- PINFL/NNUZB/JSHSHIR identifikatori 14 raqam va kontrol raqamdan o'tishi shart.

## Namuna

```json
{
  "mkb10Code": "W54", "mkb10Name": "Dog bite", "injuryLocalization": "Right forearm",
  "injuryDateTime": "2026-10-10T14:00:00", "dpuVisitDateTime": "2026-10-10T16:30:00",
  "injuryRegionCode": "UZ-QA", "injuryDistrictCode": "QA-212", "animalType": "Dog",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "notifierFullName": "Dr. Karimov"
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
- Аутентификация: [auth](../../auth/README.md). Scope: `form0581:submit`.
- Организация **не берётся из тела**: `senderOrganizationUuid` и `submittedBy` не передаются (если передать — игнорируются). Организацию указывайте заголовком `X-Organization-Id` — он **обязателен**, без него `403`.

| Поле | Обяз. | Описание |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `injuryLocalization` |  | ≤ 500 |
| `injuryDateTime` | ✔ | не в будущем |
| `dpuVisitDateTime` | ✔ | не в будущем; не раньше `injuryDateTime` |
| `injuryRegionCode` | ✔ | код справочника (≤ 64) |
| `injuryDistrictCode` | ✔ | код справочника (≤ 64) |
| `injuryAddress` |  | ≤ 1000 |
| `animalCategoryCode` |  | ≤ 64 |
| `animalColor, animalType, animalBreed` |  | ≤ 255 |
| `animalOwnerInfo` |  | как в [form0581](../../form0581/README.md#ru) |
| `patient` | ✔ | [блок patient](../../patient/README.md#ru) |
| `receiverOrganizationId` | ✔ | UUID; учреждение санэпидслужбы |
| `otherPeopleInjured` |  | boolean |
| `otherInjuredPeople[]` |  | как в [form0581](../../form0581/README.md#ru) |
| `hospitalizedAt` |  | дата-время; не раньше `injuryDateTime` |
| `hospitalOrganizationId` |  | UUID |
| `antirabicAssistanceInfo` |  | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `messageSentAt` |  | дата-время |

## Дополнительные правила

- Даты: `injuryDateTime ≤ dpuVisitDateTime`; `hospitalizedAt ≥ injuryDateTime`.
- Идентификатор PINFL/NNUZB/JSHSHIR — 14 цифр с проверкой контрольной цифры.

## Пример

```json
{
  "mkb10Code": "W54", "mkb10Name": "Dog bite", "injuryLocalization": "Right forearm",
  "injuryDateTime": "2026-10-10T14:00:00", "dpuVisitDateTime": "2026-10-10T16:30:00",
  "injuryRegionCode": "UZ-QA", "injuryDistrictCode": "QA-212", "animalType": "Dog",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "notifierFullName": "Dr. Karimov"
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
- Authentication: [auth](../../auth/README.md). Scope: `form0581:submit`.
- The organization is **not read from the body**: do not send `senderOrganizationUuid` / `submittedBy` (they are ignored if sent). Provide the organization with the `X-Organization-Id` header — it is **mandatory**, `403` without it.

| Field | Req. | Notes |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `injuryLocalization` |  | ≤ 500 |
| `injuryDateTime` | ✔ | not in the future |
| `dpuVisitDateTime` | ✔ | not in the future; not before `injuryDateTime` |
| `injuryRegionCode` | ✔ | reference code (≤ 64) |
| `injuryDistrictCode` | ✔ | reference code (≤ 64) |
| `injuryAddress` |  | ≤ 1000 |
| `animalCategoryCode` |  | ≤ 64 |
| `animalColor, animalType, animalBreed` |  | ≤ 255 |
| `animalOwnerInfo` |  | as in [form0581](../../form0581/README.md#en) |
| `patient` | ✔ | [patient block](../../patient/README.md#en) |
| `receiverOrganizationId` | ✔ | UUID; a sanitary-epidemiological service institution |
| `otherPeopleInjured` |  | boolean |
| `otherInjuredPeople[]` |  | as in [form0581](../../form0581/README.md#en) |
| `hospitalizedAt` |  | date-time; not before `injuryDateTime` |
| `hospitalOrganizationId` |  | UUID |
| `antirabicAssistanceInfo` |  | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `messageSentAt` |  | date-time |

## Extra rules

- Dates: `injuryDateTime ≤ dpuVisitDateTime`; `hospitalizedAt ≥ injuryDateTime`.
- A PINFL/NNUZB/JSHSHIR identifier must be 14 digits and pass the check-digit rule.

## Example

```json
{
  "mkb10Code": "W54", "mkb10Name": "Dog bite", "injuryLocalization": "Right forearm",
  "injuryDateTime": "2026-10-10T14:00:00", "dpuVisitDateTime": "2026-10-10T16:30:00",
  "injuryRegionCode": "UZ-QA", "injuryDistrictCode": "QA-212", "animalType": "Dog",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "notifierFullName": "Dr. Karimov"
}
```

## Response

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

Status-change webhook: [webhook](../webhook/README.md).

---
