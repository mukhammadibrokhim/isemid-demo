# Integration API — Form 058-1 / Форма №058-1 / 058-1-shakl

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

`POST /integration/v1/{source}/form-058-1` · scope `form0581:submit` / `form0581:confirm` · auth: [auth](../auth/README.md) · patient block: [patient](../patient/README.md)

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

## Tasdiqlash (confirm)

Yuborilgan shaklni yakuniy diagnoz (MKB-10) bilan tasdiqlaydi — status `APPROVED` bo'ladi.

`POST /integration/v1/{source}/form-058-1/{idOrUuid}/confirm` · scope `form0581:confirm`

- Shakl yo'lda `{idOrUuid}` bilan ko'rsatiladi: raqamli `id` **yoki** `uuid` (yuborishda javobda qaytgan qiymat).
- `{source}` — sizning `sourceKey` bilan bir xil bo'lishi kerak, aks holda `403`.
- Tasdiqlashni **faqat shaklni yuborgan tashkilot** qila oladi va faqat qabul qiluvchi shaklni qabul qilgandan keyin (status `CARD_LINKED`); boshqa statusda `409`, boshqa tashkilot bo'lsa `403`.
- Scope `form0581:confirm` — `form0581:submit` dan **alohida**; mijozga alohida berilishi kerak, aks holda `403`.

| Maydon | Maj. | Izoh |
|---|---|---|
| `finalIcd10Code` | ✔ | MKB-10 kodi, ≤ 32; **ma'lumotnomada bo'lishi shart**, aks holda `400` |
| `finalIcd10Name` | ✔ | ≤ 1024; diagnoz nomi |
| `organizationUuid` |  | shaklni yuborgan tashkilot UUID; `EXTERNAL_TOKEN` mijozlari va standart tashkilotsiz mijozlar uchun **majburiy**, ruxsat etilgan tashkilotlar ro'yxatida bo'lishi kerak |
| `submittedBy` |  | yuboruvchi (audit uchun) |

```json
{
  "finalIcd10Code": "A01.0",
  "finalIcd10Name": "Typhoid fever",
  "organizationUuid": "e9140422-0562-4733-b709-150832dd5d77"
}
```

Javob (yuborishdagi bilan bir xil shakl):

```json
{ "id": 1042, "uuid": "…", "status": "APPROVED" }
```

- **Takroriy chaqiruv xavfsiz:** shakl allaqachon shu diagnoz bilan `APPROVED` bo'lsa, xatosiz joriy holat qaytariladi (qayta tasdiqlanmaydi, webhook ham qayta ketmaydi).
- Boshqa diagnoz bilan `APPROVED` shaklni qayta tasdiqlash — `409` (status endi tasdiqlanadigan emas).
- Status o'zgarganda webhook: [webhook](webhook/README.md).

## DMED

`POST /integration/v1/DMED/form-058-1` — [dmed/README.md](dmed/README.md)

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

## Подтверждение (confirm)

Подтверждает ранее отправленную форму итоговым диагнозом (МКБ-10) — статус становится `APPROVED`.

`POST /integration/v1/{source}/form-058-1/{idOrUuid}/confirm` · scope `form0581:confirm`

- Форма указывается в пути как `{idOrUuid}`: числовой `id` **или** `uuid` (значение из ответа при отправке).
- `{source}` — должен совпадать с вашим `sourceKey`, иначе `403`.
- Подтвердить может **только организация-отправитель формы**, и только после принятия формы получателем (статус `CARD_LINKED`); в другом статусе — `409`, для чужой организации — `403`.
- Scope `form0581:confirm` — **отдельный** от `form0581:submit`; его нужно выдать клиенту отдельно, иначе `403`.

| Поле | Обяз. | Описание |
|---|---|---|
| `finalIcd10Code` | ✔ | код МКБ-10, ≤ 32; **должен существовать в справочнике**, иначе `400` |
| `finalIcd10Name` | ✔ | ≤ 1024; название диагноза |
| `organizationUuid` |  | UUID организации-отправителя формы; **обязателен** для клиентов `EXTERNAL_TOKEN` и клиентов без организации по умолчанию, должен входить в список разрешённых организаций |
| `submittedBy` |  | отправитель (для аудита) |

```json
{
  "finalIcd10Code": "A01.0",
  "finalIcd10Name": "Typhoid fever",
  "organizationUuid": "e9140422-0562-4733-b709-150832dd5d77"
}
```

Ответ (тот же формат, что при отправке):

```json
{ "id": 1042, "uuid": "…", "status": "APPROVED" }
```

- **Повторный вызов безопасен:** если форма уже `APPROVED` с этим же диагнозом, возвращается текущее состояние без ошибки (повторного подтверждения и повторного webhook нет).
- Подтверждение уже `APPROVED` формы с другим диагнозом — `409` (статус больше не подтверждаемый).
- Webhook при смене статуса: [webhook](webhook/README.md).

## DMED

`POST /integration/v1/DMED/form-058-1` — [dmed/README.md](dmed/README.md)

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

## Confirm

Confirms a previously submitted form with a final diagnosis (ICD-10) — the status becomes `APPROVED`.

`POST /integration/v1/{source}/form-058-1/{idOrUuid}/confirm` · scope `form0581:confirm`

- The form is identified in the path as `{idOrUuid}`: the numeric `id` **or** the `uuid` (as returned on submit).
- `{source}` — must match your `sourceKey`, otherwise `403`.
- **Only the organization that sent the form** can confirm it, and only after the receiver has accepted it (status `CARD_LINKED`); any other status gives `409`, a different organization gives `403`.
- Scope `form0581:confirm` is **separate** from `form0581:submit`; it must be granted to the client explicitly, otherwise `403`.

| Field | Req. | Notes |
|---|---|---|
| `finalIcd10Code` | ✔ | ICD-10 code, ≤ 32; **must exist in the catalog**, otherwise `400` |
| `finalIcd10Name` | ✔ | ≤ 1024; diagnosis name |
| `organizationUuid` |  | UUID of the organization that sent the form; **required** for `EXTERNAL_TOKEN` clients and clients without a default organization, must be in the allowed organizations list |
| `submittedBy` |  | submitter (for audit) |

```json
{
  "finalIcd10Code": "A01.0",
  "finalIcd10Name": "Typhoid fever",
  "organizationUuid": "e9140422-0562-4733-b709-150832dd5d77"
}
```

Response (same shape as on submit):

```json
{ "id": 1042, "uuid": "…", "status": "APPROVED" }
```

- **Repeating the call is safe:** if the form is already `APPROVED` with the same diagnosis, the current state is returned without error (no re-approval, no second webhook).
- Confirming an already `APPROVED` form with a different diagnosis gives `409` (the status is no longer confirmable).
- Status-change webhook: [webhook](webhook/README.md).

## DMED

`POST /integration/v1/DMED/form-058-1` — [dmed/README.md](dmed/README.md)
