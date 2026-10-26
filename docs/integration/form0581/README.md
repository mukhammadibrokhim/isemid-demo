# Integration API — Form 058-1 / Форма №058-1 / 058-1-shakl

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

`POST /integration/v1/{source}/form-058-1` · scope `form0581:submit` · auth: [auth](../auth/README.md) · patient block: [patient](../patient/README.md)

**Webhook (status-change notifications / уведомления о смене статуса / holat o'zgarishi xabarlari):** [webhook](webhook/README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

Hayvon chaqishi / zoonoz holatlar bo'yicha 058-1 xabarnomasini yaratadi.

## So'rov

| Maydon | Majburiy | Izoh |
|---|---|---|
| `diagnosisInfo.mkb10Code` | ha | ≤ 20 |
| `diagnosisInfo.mkb10Name` | ha | ≤ 512 |
| `diagnosisInfo.injuryLocalization` | yo'q | ≤ 500 |
| `incidentInfo.injuryDateTime` | ha | o'tmish/hozir |
| `incidentInfo.dpuVisitDateTime` | ha | o'tmish/hozir |
| `incidentInfo.injuryRegionCode` | ha | ma'lumotnoma kodi, ≤ 64 |
| `incidentInfo.injuryDistrictCode` | ha | ma'lumotnoma kodi, ≤ 64 |
| `incidentInfo.injuryAddress` | yo'q | ≤ 1000 |
| `animalInfo` | yo'q | `{ animalCategoryCode (≤64), animalColor, animalType, animalBreed (≤255) }` |
| `animalOwnerInfo` | yo'q | `{ ownerLastName, ownerFirstName, ownerMiddleName, regionCode, districtCode, neighborhoodCode, street, houseNumber, apartmentNumber }` |
| `patient` | ha | [patient bloki](../patient/README.md#uz) |
| `receiverOrganizationId` | ha | UUID; **sanitar-epidemiologik xizmat muassasasi** bo'lishi shart |
| `otherPeopleInjured` | yo'q | boolean |
| `otherInjuredPeople[]` | yo'q | `{ lastName, firstName, middleName, regionCode, districtCode, neighborhoodCode, street, houseNumber, apartmentNumber }` (yangi yozuvda `id` yuborilmaydi) |
| `hospitalizationInfo` | yo'q | `{ hospitalizedAt, hospitalOrganizationId (UUID) }` |
| `reportInfo.notifierFullName` | ha | ≤ 255 |
| `reportInfo.antirabicAssistanceInfo` | yo'q | ≤ 2000 |
| `reportInfo.messageSentAt` | yo'q | sana-vaqt |
| `senderOrganizationUuid`, `submittedBy` | tavsiya | har doim yuboring; SSO/DHP token usulida majburiy ([auth](../auth/README.md#uz) 3.3) |

**Sanalar tartibi:** `injuryDateTime ≤ dpuVisitDateTime`; `hospitalizedAt ≥ injuryDateTime`.

Namuna:

```json
{
  "diagnosisInfo": { "mkb10Code": "W54", "mkb10Name": "It chaqishi", "injuryLocalization": "O'ng bilak" },
  "incidentInfo": {
    "injuryDateTime": "2026-10-10T14:00:00", "dpuVisitDateTime": "2026-10-10T16:30:00",
    "injuryRegionCode": "UZ-QA", "injuryDistrictCode": "QA-212"
  },
  "animalInfo": { "animalType": "It", "animalCategoryCode": "STRAY" },
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "11111111-1111-1111-1111-111111111111",
  "reportInfo": { "notifierFullName": "Dr. Karimov" },
  "senderOrganizationUuid": "22222222-2222-2222-2222-222222222222",
  "submittedBy": { "pinfl": "30101900123456", "fullName": "Karimov B.", "position": "Shifokor" }
}
```

## Javob

```json
{ "id": 311, "uuid": "…", "status": "SENT" }
```

## DMED

`POST /integration/v1/DMED/form-058-1` — DMED uchun qat'iy **tekis** kontrakt (`DmedCreateForm0581Request`). Tashkilot tanadan olinmaydi. Maydonlar — Swagger, guruh «Integration - DMED Form 058-1».

---

<a id="ru"></a>
# 🇷🇺 Русский

Создаёт извещение формы №058-1 (укусы животных / зоонозы).

## Запрос

| Поле | Обяз. | Описание |
|---|---|---|
| `diagnosisInfo.mkb10Code` | да | ≤ 20 |
| `diagnosisInfo.mkb10Name` | да | ≤ 512 |
| `diagnosisInfo.injuryLocalization` | нет | ≤ 500 |
| `incidentInfo.injuryDateTime` | да | не в будущем |
| `incidentInfo.dpuVisitDateTime` | да | не в будущем |
| `incidentInfo.injuryRegionCode` | да | код справочника, ≤ 64 |
| `incidentInfo.injuryDistrictCode` | да | код справочника, ≤ 64 |
| `incidentInfo.injuryAddress` | нет | ≤ 1000 |
| `animalInfo` | нет | `{ animalCategoryCode (≤64), animalColor, animalType, animalBreed (≤255) }` |
| `animalOwnerInfo` | нет | `{ ownerLastName, ownerFirstName, ownerMiddleName, regionCode, districtCode, neighborhoodCode, street, houseNumber, apartmentNumber }` |
| `patient` | да | [блок patient](../patient/README.md#ru) |
| `receiverOrganizationId` | да | UUID; должна быть **учреждением санитарно-эпидемиологической службы** |
| `otherPeopleInjured` | нет | boolean |
| `otherInjuredPeople[]` | нет | `{ lastName, firstName, middleName, regionCode, districtCode, neighborhoodCode, street, houseNumber, apartmentNumber }` (для новой записи `id` не передаётся) |
| `hospitalizationInfo` | нет | `{ hospitalizedAt, hospitalOrganizationId (UUID) }` |
| `reportInfo.notifierFullName` | да | ≤ 255 |
| `reportInfo.antirabicAssistanceInfo` | нет | ≤ 2000 |
| `reportInfo.messageSentAt` | нет | дата-время |
| `senderOrganizationUuid`, `submittedBy` | рекомендуется | передавайте всегда; обязательны при способе SSO/DHP-токен ([auth](../auth/README.md#ru) п. 3.3) |

**Порядок дат:** `injuryDateTime ≤ dpuVisitDateTime`; `hospitalizedAt ≥ injuryDateTime`.

Пример:

```json
{
  "diagnosisInfo": { "mkb10Code": "W54", "mkb10Name": "Укус собаки", "injuryLocalization": "Правое предплечье" },
  "incidentInfo": {
    "injuryDateTime": "2026-10-10T14:00:00", "dpuVisitDateTime": "2026-10-10T16:30:00",
    "injuryRegionCode": "UZ-QA", "injuryDistrictCode": "QA-212"
  },
  "animalInfo": { "animalType": "Собака", "animalCategoryCode": "STRAY" },
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "11111111-1111-1111-1111-111111111111",
  "reportInfo": { "notifierFullName": "Dr. Karimov" },
  "senderOrganizationUuid": "22222222-2222-2222-2222-222222222222",
  "submittedBy": { "pinfl": "30101900123456", "fullName": "Karimov B.", "position": "Врач" }
}
```

## Ответ

```json
{ "id": 311, "uuid": "…", "status": "SENT" }
```

## DMED

`POST /integration/v1/DMED/form-058-1` — фиксированный **плоский** контракт DMED (`DmedCreateForm0581Request`). Организация из тела не берётся. Поля — Swagger, группа «Integration - DMED Form 058-1».

---

<a id="en"></a>
# 🇬🇧 English

Creates a Form 058-1 notification (animal bites / zoonoses).

## Request

| Field | Req. | Notes |
|---|---|---|
| `diagnosisInfo.mkb10Code` | yes | ≤ 20 |
| `diagnosisInfo.mkb10Name` | yes | ≤ 512 |
| `diagnosisInfo.injuryLocalization` | no | ≤ 500 |
| `incidentInfo.injuryDateTime` | yes | not in the future |
| `incidentInfo.dpuVisitDateTime` | yes | not in the future |
| `incidentInfo.injuryRegionCode` | yes | reference code, ≤ 64 |
| `incidentInfo.injuryDistrictCode` | yes | reference code, ≤ 64 |
| `incidentInfo.injuryAddress` | no | ≤ 1000 |
| `animalInfo` | no | `{ animalCategoryCode (≤64), animalColor, animalType, animalBreed (≤255) }` |
| `animalOwnerInfo` | no | `{ ownerLastName, ownerFirstName, ownerMiddleName, regionCode, districtCode, neighborhoodCode, street, houseNumber, apartmentNumber }` |
| `patient` | yes | [patient block](../patient/README.md#en) |
| `receiverOrganizationId` | yes | UUID; must be a **sanitary-epidemiological service institution** |
| `otherPeopleInjured` | no | boolean |
| `otherInjuredPeople[]` | no | `{ lastName, firstName, middleName, regionCode, districtCode, neighborhoodCode, street, houseNumber, apartmentNumber }` (omit `id` for a new record) |
| `hospitalizationInfo` | no | `{ hospitalizedAt, hospitalOrganizationId (UUID) }` |
| `reportInfo.notifierFullName` | yes | ≤ 255 |
| `reportInfo.antirabicAssistanceInfo` | no | ≤ 2000 |
| `reportInfo.messageSentAt` | no | date-time |
| `senderOrganizationUuid`, `submittedBy` | recommended | always send; mandatory with the SSO/DHP-token method ([auth](../auth/README.md#en) §3.3) |

**Date ordering:** `injuryDateTime ≤ dpuVisitDateTime`; `hospitalizedAt ≥ injuryDateTime`.

Example:

```json
{
  "diagnosisInfo": { "mkb10Code": "W54", "mkb10Name": "Dog bite", "injuryLocalization": "Right forearm" },
  "incidentInfo": {
    "injuryDateTime": "2026-10-10T14:00:00", "dpuVisitDateTime": "2026-10-10T16:30:00",
    "injuryRegionCode": "UZ-QA", "injuryDistrictCode": "QA-212"
  },
  "animalInfo": { "animalType": "Dog", "animalCategoryCode": "STRAY" },
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "11111111-1111-1111-1111-111111111111",
  "reportInfo": { "notifierFullName": "Dr. Karimov" },
  "senderOrganizationUuid": "22222222-2222-2222-2222-222222222222",
  "submittedBy": { "pinfl": "30101900123456", "fullName": "Karimov B.", "position": "Physician" }
}
```

## Response

```json
{ "id": 311, "uuid": "…", "status": "SENT" }
```

## DMED

`POST /integration/v1/DMED/form-058-1` — DMED's fixed **flat** contract (`DmedCreateForm0581Request`). The organization is never read from the body. Fields: Swagger, group "Integration - DMED Form 058-1".
