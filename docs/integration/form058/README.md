# Integration API — Form 058 / Форма №058 / 058-shakl

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

`POST /integration/v1/{source}/form-058` · scope `form058:submit` · auth: [auth](../auth/README.md) · patient block: [patient](../patient/README.md)

**Webhook (status-change notifications / уведомления о смене статуса / holat o'zgarishi xabarlari):** [webhook](webhook/README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

Yuqumli/parazitar kasallik haqida 058-shakl xabarnomasini yaratadi. Forma holati yaratilgandan so'ng javobdagi `status` da qaytadi.

## So'rov

Header: `Content-Type: application/json`, autentifikatsiya headerlari ([auth](../auth/README.md)), kerak bo'lsa `X-Organization-Id`.
`{source}` — klientning `sourceKey` i.

| Maydon | Majburiy | Izoh |
|---|---|---|
| `diagnosisInfo.mkb10Code` | ha | ≤ 20 |
| `diagnosisInfo.mkb10Name` | ha | ≤ 512 |
| `diagnosisInfo.mkb10UsageLimit` | yo'q | ≥ 0 |
| `patient` | ha | [patient bloki](../patient/README.md#uz) |
| `clinicalInfo.labConfirmation` | yo'q | boolean (blok o'zi majburiy) |
| `clinicalInfo.hospitalPlaceId` | yo'q | UUID |
| `dateInfo.diseaseDate` | ha | `yyyy-MM-dd'T'HH:mm:ss`, o'tmish/hozir |
| `dateInfo.firstVisitDate` | ha | xuddi shunday |
| `dateInfo.visitDate` | ha | xuddi shunday |
| `dateInfo.initialReportDateTime` | ha | xuddi shunday |
| `dateInfo.admissionDate`, `dateInfo.diagnosisDate` | yo'q | o'tmish/hozir |
| `receiverOrganizationId` | ha | qabul qiluvchi tashkilot UUID |
| `location` | yo'q | `{ latitude (−90..90), longitude (−180..180), location (≤1000) }` |
| `epidemicInfo.diseasePlaceCode` | ha | ma'lumotnoma kodi, ≤ 64 |
| `epidemicInfo.diseaseCause`, `epidemicInfo.epidemicMeasures` | yo'q | ≤ 2000 |
| `reportInfo.notifierFullName` | ha | ≤ 255 |
| `reportInfo.journalFormCode` | ha | ma'lumotnoma kodi, ≤ 64 |
| `reportInfo.comment` | yo'q | ≤ 2000 |
| `senderOrganizationUuid` | shartli | har doim yuboring; SSO/DHP token usulida majburiy |
| `submittedBy` | shartli | xuddi shu holatlarda majburiy: `{ pinfl, fullName, position, phone? }` |

**Sanalar tartibi** (aks holda `VALIDATION_FAILED`):
`diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.

Namuna:

```json
{
  "diagnosisInfo": { "mkb10Code": "A01.0", "mkb10Name": "Qorin tifi" },
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17", "genderCode": "MALE",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212", "streetAddress": "…" } ]
  },
  "clinicalInfo": { "labConfirmation": true },
  "dateInfo": {
    "diseaseDate": "2026-10-01T08:00:00", "firstVisitDate": "2026-10-02T09:30:00",
    "visitDate": "2026-10-02T09:30:00", "initialReportDateTime": "2026-10-02T10:00:00"
  },
  "receiverOrganizationId": "11111111-1111-1111-1111-111111111111",
  "epidemicInfo": { "diseasePlaceCode": "HOME" },
  "reportInfo": { "notifierFullName": "Dr. Karimov", "journalFormCode": "F058" },
  "senderOrganizationUuid": "22222222-2222-2222-2222-222222222222",
  "submittedBy": { "pinfl": "30101900123456", "fullName": "Karimov B.", "position": "Shifokor" }
}
```

(Kodlar — ma'lumotnoma qiymatlari; namunadagilar illyustrativ.)

## Javob

```json
{ "id": 1042, "uuid": "…", "status": "SENT" }
```

## DMED

`POST /integration/v1/DMED/form-058` — DMED uchun alohida, **tekis** (flat) shakldagi qat'iy kontrakt (`DmedCreateForm058Request`); umumiy endpointdagi ichma-ich tuzilmaga o'tkazilmaydi. Tashkilot tanadan olinmaydi (`X-Organization-Id` / klientga biriktirilgan tashkilot). Maydonlar ro'yxati — Swagger, guruh «Integration - DMED Form 058».

---

<a id="ru"></a>
# 🇷🇺 Русский

Создаёт извещение формы №058 об инфекционном/паразитарном заболевании. Статус формы после создания возвращается в поле `status` ответа.

## Запрос

Заголовки: `Content-Type: application/json`, заголовки аутентификации ([auth](../auth/README.md)), при необходимости `X-Organization-Id`.
`{source}` — `sourceKey` клиента.

| Поле | Обяз. | Описание |
|---|---|---|
| `diagnosisInfo.mkb10Code` | да | ≤ 20 |
| `diagnosisInfo.mkb10Name` | да | ≤ 512 |
| `diagnosisInfo.mkb10UsageLimit` | нет | ≥ 0 |
| `patient` | да | [блок patient](../patient/README.md#ru) |
| `clinicalInfo.labConfirmation` | нет | boolean (сам блок обязателен) |
| `clinicalInfo.hospitalPlaceId` | нет | UUID |
| `dateInfo.diseaseDate` | да | `yyyy-MM-dd'T'HH:mm:ss`, не в будущем |
| `dateInfo.firstVisitDate` | да | аналогично |
| `dateInfo.visitDate` | да | аналогично |
| `dateInfo.initialReportDateTime` | да | аналогично |
| `dateInfo.admissionDate`, `dateInfo.diagnosisDate` | нет | не в будущем |
| `receiverOrganizationId` | да | UUID организации-получателя |
| `location` | нет | `{ latitude (−90..90), longitude (−180..180), location (≤1000) }` |
| `epidemicInfo.diseasePlaceCode` | да | код справочника, ≤ 64 |
| `epidemicInfo.diseaseCause`, `epidemicInfo.epidemicMeasures` | нет | ≤ 2000 |
| `reportInfo.notifierFullName` | да | ≤ 255 |
| `reportInfo.journalFormCode` | да | код справочника, ≤ 64 |
| `reportInfo.comment` | нет | ≤ 2000 |
| `senderOrganizationUuid` | условно | передавайте всегда; обязательно при способе SSO/DHP-токен |
| `submittedBy` | условно | обязательно в тех же случаях: `{ pinfl, fullName, position, phone? }` |

**Порядок дат** (иначе `VALIDATION_FAILED`):
`diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.

Пример:

```json
{
  "diagnosisInfo": { "mkb10Code": "A01.0", "mkb10Name": "Брюшной тиф" },
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17", "genderCode": "MALE",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212", "streetAddress": "…" } ]
  },
  "clinicalInfo": { "labConfirmation": true },
  "dateInfo": {
    "diseaseDate": "2026-10-01T08:00:00", "firstVisitDate": "2026-10-02T09:30:00",
    "visitDate": "2026-10-02T09:30:00", "initialReportDateTime": "2026-10-02T10:00:00"
  },
  "receiverOrganizationId": "11111111-1111-1111-1111-111111111111",
  "epidemicInfo": { "diseasePlaceCode": "HOME" },
  "reportInfo": { "notifierFullName": "Dr. Karimov", "journalFormCode": "F058" },
  "senderOrganizationUuid": "22222222-2222-2222-2222-222222222222",
  "submittedBy": { "pinfl": "30101900123456", "fullName": "Karimov B.", "position": "Врач" }
}
```

(Коды — значения справочников; в примере они иллюстративны.)

## Ответ

```json
{ "id": 1042, "uuid": "…", "status": "SENT" }
```

## DMED

`POST /integration/v1/DMED/form-058` — отдельный фиксированный **плоский** контракт для DMED (`DmedCreateForm058Request`); не переводится на вложенную структуру общего эндпоинта. Организация из тела не берётся (`X-Organization-Id` / организация, закреплённая за клиентом). Список полей — Swagger, группа «Integration - DMED Form 058».

---

<a id="en"></a>
# 🇬🇧 English

Creates a Form 058 notification of an infectious/parasitic disease. The form's status after creation is returned in the response `status`.

## Request

Headers: `Content-Type: application/json`, auth headers ([auth](../auth/README.md)), `X-Organization-Id` when needed.
`{source}` is the client's `sourceKey`.

| Field | Req. | Notes |
|---|---|---|
| `diagnosisInfo.mkb10Code` | yes | ≤ 20 |
| `diagnosisInfo.mkb10Name` | yes | ≤ 512 |
| `diagnosisInfo.mkb10UsageLimit` | no | ≥ 0 |
| `patient` | yes | [patient block](../patient/README.md#en) |
| `clinicalInfo.labConfirmation` | no | boolean (the block itself is required) |
| `clinicalInfo.hospitalPlaceId` | no | UUID |
| `dateInfo.diseaseDate` | yes | `yyyy-MM-dd'T'HH:mm:ss`, not in the future |
| `dateInfo.firstVisitDate` | yes | same |
| `dateInfo.visitDate` | yes | same |
| `dateInfo.initialReportDateTime` | yes | same |
| `dateInfo.admissionDate`, `dateInfo.diagnosisDate` | no | not in the future |
| `receiverOrganizationId` | yes | receiving organization UUID |
| `location` | no | `{ latitude (−90..90), longitude (−180..180), location (≤1000) }` |
| `epidemicInfo.diseasePlaceCode` | yes | reference code, ≤ 64 |
| `epidemicInfo.diseaseCause`, `epidemicInfo.epidemicMeasures` | no | ≤ 2000 |
| `reportInfo.notifierFullName` | yes | ≤ 255 |
| `reportInfo.journalFormCode` | yes | reference code, ≤ 64 |
| `reportInfo.comment` | no | ≤ 2000 |
| `senderOrganizationUuid` | conditional | always send; mandatory with the SSO/DHP-token method |
| `submittedBy` | conditional | required in the same cases: `{ pinfl, fullName, position, phone? }` |

**Date ordering** (otherwise `VALIDATION_FAILED`):
`diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.

Example:

```json
{
  "diagnosisInfo": { "mkb10Code": "A01.0", "mkb10Name": "Typhoid fever" },
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17", "genderCode": "MALE",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212", "streetAddress": "…" } ]
  },
  "clinicalInfo": { "labConfirmation": true },
  "dateInfo": {
    "diseaseDate": "2026-10-01T08:00:00", "firstVisitDate": "2026-10-02T09:30:00",
    "visitDate": "2026-10-02T09:30:00", "initialReportDateTime": "2026-10-02T10:00:00"
  },
  "receiverOrganizationId": "11111111-1111-1111-1111-111111111111",
  "epidemicInfo": { "diseasePlaceCode": "HOME" },
  "reportInfo": { "notifierFullName": "Dr. Karimov", "journalFormCode": "F058" },
  "senderOrganizationUuid": "22222222-2222-2222-2222-222222222222",
  "submittedBy": { "pinfl": "30101900123456", "fullName": "Karimov B.", "position": "Physician" }
}
```

(Codes are reference-catalog values; the ones above are illustrative.)

## Response

```json
{ "id": 1042, "uuid": "…", "status": "SENT" }
```

## DMED

`POST /integration/v1/DMED/form-058` — a separate fixed **flat** contract for DMED (`DmedCreateForm058Request`); it is not migrated to the nested structure of the generic endpoint. The organization is never read from the body (`X-Organization-Id` / the client's bound organization). Field list: Swagger, group "Integration - DMED Form 058".
