# DMED — Form 058-1 / Форма №058-1

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

`POST /integration/v1/DMED/form-058-1` · scope `form0581:submit` / `form0581:confirm` · [form0581](../README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

DMED uchun **tekis** (flat) formatdagi qat'iy kontrakt. Umumiy `{source}` endpointidan farqi: maydonlar ichma-ich bloklarsiz, to'g'ridan-to'g'ri yuqori darajada.

## Qanday chaqiriladi

- `{source}` — **doim `DMED`** (sizning `sourceKey` ham `dmed` bo'lishi kerak, aks holda `403`).
- Autentifikatsiya (API key) va umumiy tartib: [DMED umumiy ko'rinish](../../dmed/README.md#uz), [auth](../../auth/README.md). Scope: `form0581:submit`.
- Tashkilot **tanadan olinmaydi**: `senderOrganizationUuid` yuborilmaydi (yuborsangiz ham e'tiborga olinmaydi). Tashkilotni `X-Organization-Id` header bilan bering — **majburiy**, yo'q bo'lsa `403`. Formani yuborgan xodim tanada `submittedBy` bilan beriladi — **majburiy**, yo'q bo'lsa `400`.

| Maydon | Maj. | Izoh |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `injuryLocalization` | ✔ | ≤ 500 |
| `injuryDateTime` | ✔ | o'tmish/hozir |
| `dpuVisitDateTime` | ✔ | o'tmish/hozir; `injuryDateTime` dan keyin yoki teng |
| `injuryRegionCode` | ✔ | ma'lumotnoma kodi (≤ 64) |
| `injuryDistrictCode` | ✔ | ma'lumotnoma kodi (≤ 64) |
| `injuryAddress` | ✔ | ≤ 1000 |
| `animalCategoryCode` | ✔ | ≤ 64 |
| `animalColor, animalType, animalBreed` | ✔ | ≤ 255 |
| `animalOwnerInfo` | ✔ | Hayvon egasi obyekti, maydonlari [Ichki bloklar](#uz-nested) bo'limida |
| `patient` | ✔ | [patient bloki](../../patient/README.md#uz) |
| `receiverOrganizationId` | ✔ | UUID; sanitar-epidemiologik xizmat muassasasi |
| `otherPeopleInjured` | ✔ | boolean |
| `otherInjuredPeople[]` | ✔ | Boshqa jabrlanganlar ro'yxati (bo'sh `[]` mumkin), elementlari [Ichki bloklar](#uz-nested) bo'limida |
| `hospitalizedAt` | ✔ | sana-vaqt; `injuryDateTime` dan oldin bo'lmasin |
| `hospitalOrganizationId` | ✔ | UUID |
| `antirabicAssistanceInfo` | ✔ | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `messageSentAt` | ✔ | sana-vaqt |
| `submittedBy` | ✔ | Formani yuborgan xodim: `{ pinfl, fullName, position, phone }`. `pinfl`, `fullName`, `position` majburiy, `phone` ixtiyoriy. Audit uchun saqlanadi (kim yuborgani) |

<a id="uz-nested"></a>
## Ichki bloklar

`animalOwnerInfo` va `otherInjuredPeople[]` elementlari. Bloklarning o'zi majburiy (`otherInjuredPeople` bo'sh `[]` bo'lishi mumkin); ichidagi maydonlar ixtiyoriy.

**`animalOwnerInfo`** — hayvon egasi

| Maydon | Izoh |
|---|---|
| `ownerLastName` | familiya, ≤ 255 |
| `ownerFirstName` | ism, ≤ 255 |
| `ownerMiddleName` | otasining ismi, ≤ 255 |
| `regionCode` | viloyat kodi (ma'lumotnoma), ≤ 64 |
| `districtCode` | tuman kodi (ma'lumotnoma), ≤ 64 |
| `neighborhoodCode` | mahalla kodi (ma'lumotnoma), ≤ 64 |
| `street` | ko'cha, ≤ 255 |
| `houseNumber` | uy raqami, ≤ 32 |
| `apartmentNumber` | xonadon raqami, ≤ 32 |

**`otherInjuredPeople[]`** — voqeada jabrlangan boshqa shaxslar (`otherPeopleInjured = true` bo'lsa to'ldiriladi)

| Maydon | Izoh |
|---|---|
| `lastName` | familiya, ≤ 255 |
| `firstName` | ism, ≤ 255 |
| `middleName` | otasining ismi, ≤ 255 |
| `regionCode` | viloyat kodi (ma'lumotnoma), ≤ 64 |
| `districtCode` | tuman kodi (ma'lumotnoma), ≤ 64 |
| `neighborhoodCode` | mahalla kodi (ma'lumotnoma), ≤ 64 |
| `street` | ko'cha, ≤ 255 |
| `houseNumber` | uy raqami, ≤ 32 |
| `apartmentNumber` | xonadon raqami, ≤ 32 |

`id` — yangi yozuvda yuborilmaydi.

## Qo'shimcha qoidalar

- Sanalar: `injuryDateTime ≤ dpuVisitDateTime`; `hospitalizedAt ≥ injuryDateTime`.
- PINFL/NNUZB/JSHSHIR identifikatori 14 raqam va kontrol raqamdan o'tishi shart.

## Namuna

```json
{
  "mkb10Code": "W54", "mkb10Name": "Dog bite", "injuryLocalization": "Right forearm",
  "injuryDateTime": "2026-10-10T14:00:00", "dpuVisitDateTime": "2026-10-10T16:30:00",
  "injuryRegionCode": "UZ-QA", "injuryDistrictCode": "QA-212", "injuryAddress": "…",
  "animalCategoryCode": "STRAY", "animalColor": "Brown", "animalType": "Dog", "animalBreed": "Mixed",
  "animalOwnerInfo": { "ownerLastName": "Qodirov", "ownerFirstName": "Bahodir", "ownerMiddleName": "Aliyevich",
    "regionCode": "UZ-QA", "districtCode": "QA-212", "neighborhoodCode": "QA-212-01",
    "street": "Navoiy", "houseNumber": "12", "apartmentNumber": "5" },
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "otherPeopleInjured": true,
  "otherInjuredPeople": [ { "lastName": "Karimov", "firstName": "Aziz", "middleName": "Olimovich",
    "regionCode": "UZ-QA", "districtCode": "QA-212", "neighborhoodCode": "QA-212-01",
    "street": "Navoiy", "houseNumber": "14", "apartmentNumber": "2" } ],
  "hospitalizedAt": "2026-10-10T18:00:00", "hospitalOrganizationId": "0b2f6b1e-5c1a-4f0e-9d53-6a8e5c0c1a11",
  "antirabicAssistanceInfo": "…", "notifierFullName": "Dr. Karimov", "messageSentAt": "2026-10-10T17:00:00",
  "submittedBy": { "pinfl": "31705901234560", "fullName": "Dr. Karimov", "position": "Epidemiolog", "phone": "+998901234567" }
}
```

## Javob

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

## Tasdiqlash (confirm)

Yuborilgan shaklni yakuniy diagnoz (MKB-10) bilan tasdiqlaydi — status `APPROVED` bo'ladi.

`POST /integration/v1/DMED/form-058-1/{idOrUuid}/confirm` · scope `form0581:confirm`

- Shakl yo'lda `{idOrUuid}` bilan ko'rsatiladi: raqamli `id` **yoki** `uuid` (yuborishda javobda qaytgan qiymat).
- `{source}` — **doim `DMED`**; `sourceKey` ham `dmed` bo'lishi kerak, aks holda `403`. Tashkilot `X-Organization-Id` header bilan beriladi (**majburiy**); tanada `senderOrganizationUuid` yo'q, tasdiqlagan xodim `submittedBy` bilan beriladi (**majburiy**).
- Tasdiqlashni **faqat shaklni yuborgan tashkilot** qila oladi va faqat qabul qiluvchi shaklni qabul qilgandan keyin (status `CARD_LINKED`); boshqa statusda `409`, boshqa tashkilot bo'lsa `403`.
- Scope `form0581:confirm` — `form0581:submit` dan **alohida**; mijozga alohida berilishi kerak, aks holda `403`.

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
- Аутентификация (API key) и общий порядок: [обзор DMED](../../dmed/README.md#ru), [auth](../../auth/README.md). Scope: `form0581:submit`.
- Организация **не берётся из тела**: `senderOrganizationUuid` не передаётся (если передать — игнорируется). Организацию указывайте заголовком `X-Organization-Id` — он **обязателен**, без него `403`. Сотрудник, отправивший форму, передаётся в теле как `submittedBy` — **обязателен**, без него `400`.

| Поле | Обяз. | Описание |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `injuryLocalization` | ✔ | ≤ 500 |
| `injuryDateTime` | ✔ | не в будущем |
| `dpuVisitDateTime` | ✔ | не в будущем; не раньше `injuryDateTime` |
| `injuryRegionCode` | ✔ | код справочника (≤ 64) |
| `injuryDistrictCode` | ✔ | код справочника (≤ 64) |
| `injuryAddress` | ✔ | ≤ 1000 |
| `animalCategoryCode` | ✔ | ≤ 64 |
| `animalColor, animalType, animalBreed` | ✔ | ≤ 255 |
| `animalOwnerInfo` | ✔ | Объект владельца животного, поля — в разделе [Вложенные блоки](#ru-nested) |
| `patient` | ✔ | [блок patient](../../patient/README.md#ru) |
| `receiverOrganizationId` | ✔ | UUID; учреждение санэпидслужбы |
| `otherPeopleInjured` | ✔ | boolean |
| `otherInjuredPeople[]` | ✔ | Список иных пострадавших (может быть пустым `[]`), элементы — в разделе [Вложенные блоки](#ru-nested) |
| `hospitalizedAt` | ✔ | дата-время; не раньше `injuryDateTime` |
| `hospitalOrganizationId` | ✔ | UUID |
| `antirabicAssistanceInfo` | ✔ | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `messageSentAt` | ✔ | дата-время |
| `submittedBy` | ✔ | Сотрудник, отправивший форму: `{ pinfl, fullName, position, phone }`. `pinfl`, `fullName`, `position` обязательны, `phone` необязателен. Сохраняется в аудите (кто отправил) |

<a id="ru-nested"></a>
## Вложенные блоки

Элементы `animalOwnerInfo` и `otherInjuredPeople[]`. Сами блоки обязательны (`otherInjuredPeople` может быть пустым `[]`); поля внутри необязательны.

**`animalOwnerInfo`** — владелец животного

| Поле | Описание |
|---|---|
| `ownerLastName` | фамилия, ≤ 255 |
| `ownerFirstName` | имя, ≤ 255 |
| `ownerMiddleName` | отчество, ≤ 255 |
| `regionCode` | код региона (справочник), ≤ 64 |
| `districtCode` | код района (справочник), ≤ 64 |
| `neighborhoodCode` | код махалли (справочник), ≤ 64 |
| `street` | улица, ≤ 255 |
| `houseNumber` | номер дома, ≤ 32 |
| `apartmentNumber` | номер квартиры, ≤ 32 |

**`otherInjuredPeople[]`** — иные пострадавшие в происшествии (заполняется, если `otherPeopleInjured = true`)

| Поле | Описание |
|---|---|
| `lastName` | фамилия, ≤ 255 |
| `firstName` | имя, ≤ 255 |
| `middleName` | отчество, ≤ 255 |
| `regionCode` | код региона (справочник), ≤ 64 |
| `districtCode` | код района (справочник), ≤ 64 |
| `neighborhoodCode` | код махалли (справочник), ≤ 64 |
| `street` | улица, ≤ 255 |
| `houseNumber` | номер дома, ≤ 32 |
| `apartmentNumber` | номер квартиры, ≤ 32 |

`id` — для новой записи не передаётся.

## Дополнительные правила

- Даты: `injuryDateTime ≤ dpuVisitDateTime`; `hospitalizedAt ≥ injuryDateTime`.
- Идентификатор PINFL/NNUZB/JSHSHIR — 14 цифр с проверкой контрольной цифры.

## Пример

```json
{
  "mkb10Code": "W54", "mkb10Name": "Dog bite", "injuryLocalization": "Right forearm",
  "injuryDateTime": "2026-10-10T14:00:00", "dpuVisitDateTime": "2026-10-10T16:30:00",
  "injuryRegionCode": "UZ-QA", "injuryDistrictCode": "QA-212", "injuryAddress": "…",
  "animalCategoryCode": "STRAY", "animalColor": "Brown", "animalType": "Dog", "animalBreed": "Mixed",
  "animalOwnerInfo": { "ownerLastName": "Qodirov", "ownerFirstName": "Bahodir", "ownerMiddleName": "Aliyevich",
    "regionCode": "UZ-QA", "districtCode": "QA-212", "neighborhoodCode": "QA-212-01",
    "street": "Navoiy", "houseNumber": "12", "apartmentNumber": "5" },
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "otherPeopleInjured": true,
  "otherInjuredPeople": [ { "lastName": "Karimov", "firstName": "Aziz", "middleName": "Olimovich",
    "regionCode": "UZ-QA", "districtCode": "QA-212", "neighborhoodCode": "QA-212-01",
    "street": "Navoiy", "houseNumber": "14", "apartmentNumber": "2" } ],
  "hospitalizedAt": "2026-10-10T18:00:00", "hospitalOrganizationId": "0b2f6b1e-5c1a-4f0e-9d53-6a8e5c0c1a11",
  "antirabicAssistanceInfo": "…", "notifierFullName": "Dr. Karimov", "messageSentAt": "2026-10-10T17:00:00",
  "submittedBy": { "pinfl": "31705901234560", "fullName": "Dr. Karimov", "position": "Epidemiolog", "phone": "+998901234567" }
}
```

## Ответ

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

## Подтверждение (confirm)

Подтверждает ранее отправленную форму итоговым диагнозом (МКБ-10) — статус становится `APPROVED`.

`POST /integration/v1/DMED/form-058-1/{idOrUuid}/confirm` · scope `form0581:confirm`

- Форма указывается в пути как `{idOrUuid}`: числовой `id` **или** `uuid` (значение из ответа при отправке).
- `{source}` — **всегда `DMED`**; ваш `sourceKey` тоже должен быть `dmed`, иначе `403`. Организация передаётся заголовком `X-Organization-Id` (**обязателен**); `senderOrganizationUuid` в теле нет, подтвердивший сотрудник передаётся как `submittedBy` (**обязателен**).
- Подтвердить может **только организация-отправитель формы**, и только после принятия формы получателем (статус `CARD_LINKED`); в другом статусе — `409`, для чужой организации — `403`.
- Scope `form0581:confirm` — **отдельный** от `form0581:submit`; его нужно выдать клиенту отдельно, иначе `403`.

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
- Authentication (API key) and overall flow: [DMED overview](../../dmed/README.md#en), [auth](../../auth/README.md). Scope: `form0581:submit`.
- The organization is **not read from the body**: do not send `senderOrganizationUuid` (ignored if sent). Provide the organization with the `X-Organization-Id` header — it is **mandatory**, `403` without it. The staff member submitting is sent in the body as `submittedBy` — **mandatory**, `400` without it.

| Field | Req. | Notes |
|---|---|---|
| `mkb10Code` | ✔ | ≤ 20 |
| `mkb10Name` | ✔ | ≤ 512 |
| `injuryLocalization` | ✔ | ≤ 500 |
| `injuryDateTime` | ✔ | not in the future |
| `dpuVisitDateTime` | ✔ | not in the future; not before `injuryDateTime` |
| `injuryRegionCode` | ✔ | reference code (≤ 64) |
| `injuryDistrictCode` | ✔ | reference code (≤ 64) |
| `injuryAddress` | ✔ | ≤ 1000 |
| `animalCategoryCode` | ✔ | ≤ 64 |
| `animalColor, animalType, animalBreed` | ✔ | ≤ 255 |
| `animalOwnerInfo` | ✔ | Animal owner object, fields in [Nested blocks](#en-nested) |
| `patient` | ✔ | [patient block](../../patient/README.md#en) |
| `receiverOrganizationId` | ✔ | UUID; a sanitary-epidemiological service institution |
| `otherPeopleInjured` | ✔ | boolean |
| `otherInjuredPeople[]` | ✔ | List of other injured people (may be an empty `[]`), elements in [Nested blocks](#en-nested) |
| `hospitalizedAt` | ✔ | date-time; not before `injuryDateTime` |
| `hospitalOrganizationId` | ✔ | UUID |
| `antirabicAssistanceInfo` | ✔ | ≤ 2000 |
| `notifierFullName` | ✔ | ≤ 255 |
| `messageSentAt` | ✔ | date-time |
| `submittedBy` | ✔ | The staff member who submitted the form: `{ pinfl, fullName, position, phone }`. `pinfl`, `fullName`, `position` are required, `phone` is optional. Stored in the audit (who sent it) |

<a id="en-nested"></a>
## Nested blocks

Elements of `animalOwnerInfo` and `otherInjuredPeople[]`. The blocks themselves are required (`otherInjuredPeople` may be an empty `[]`); the fields inside are optional.

**`animalOwnerInfo`** — the animal owner

| Field | Notes |
|---|---|
| `ownerLastName` | last name, ≤ 255 |
| `ownerFirstName` | first name, ≤ 255 |
| `ownerMiddleName` | middle name, ≤ 255 |
| `regionCode` | region code (catalog), ≤ 64 |
| `districtCode` | district code (catalog), ≤ 64 |
| `neighborhoodCode` | neighborhood code (catalog), ≤ 64 |
| `street` | street, ≤ 255 |
| `houseNumber` | house number, ≤ 32 |
| `apartmentNumber` | apartment number, ≤ 32 |

**`otherInjuredPeople[]`** — other people injured in the incident (fill in when `otherPeopleInjured = true`)

| Field | Notes |
|---|---|
| `lastName` | last name, ≤ 255 |
| `firstName` | first name, ≤ 255 |
| `middleName` | middle name, ≤ 255 |
| `regionCode` | region code (catalog), ≤ 64 |
| `districtCode` | district code (catalog), ≤ 64 |
| `neighborhoodCode` | neighborhood code (catalog), ≤ 64 |
| `street` | street, ≤ 255 |
| `houseNumber` | house number, ≤ 32 |
| `apartmentNumber` | apartment number, ≤ 32 |

`id` — omit for a new record.

## Extra rules

- Dates: `injuryDateTime ≤ dpuVisitDateTime`; `hospitalizedAt ≥ injuryDateTime`.
- A PINFL/NNUZB/JSHSHIR identifier must be 14 digits and pass the check-digit rule.

## Example

```json
{
  "mkb10Code": "W54", "mkb10Name": "Dog bite", "injuryLocalization": "Right forearm",
  "injuryDateTime": "2026-10-10T14:00:00", "dpuVisitDateTime": "2026-10-10T16:30:00",
  "injuryRegionCode": "UZ-QA", "injuryDistrictCode": "QA-212", "injuryAddress": "…",
  "animalCategoryCode": "STRAY", "animalColor": "Brown", "animalType": "Dog", "animalBreed": "Mixed",
  "animalOwnerInfo": { "ownerLastName": "Qodirov", "ownerFirstName": "Bahodir", "ownerMiddleName": "Aliyevich",
    "regionCode": "UZ-QA", "districtCode": "QA-212", "neighborhoodCode": "QA-212-01",
    "street": "Navoiy", "houseNumber": "12", "apartmentNumber": "5" },
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "e9140422-0562-4733-b709-150832dd5d77",
  "otherPeopleInjured": true,
  "otherInjuredPeople": [ { "lastName": "Karimov", "firstName": "Aziz", "middleName": "Olimovich",
    "regionCode": "UZ-QA", "districtCode": "QA-212", "neighborhoodCode": "QA-212-01",
    "street": "Navoiy", "houseNumber": "14", "apartmentNumber": "2" } ],
  "hospitalizedAt": "2026-10-10T18:00:00", "hospitalOrganizationId": "0b2f6b1e-5c1a-4f0e-9d53-6a8e5c0c1a11",
  "antirabicAssistanceInfo": "…", "notifierFullName": "Dr. Karimov", "messageSentAt": "2026-10-10T17:00:00",
  "submittedBy": { "pinfl": "31705901234560", "fullName": "Dr. Karimov", "position": "Epidemiolog", "phone": "+998901234567" }
}
```

## Response

```json
{ "id": 1, "uuid": "…", "status": "SENT" }
```

## Confirm

Confirms a previously submitted form with a final diagnosis (ICD-10) — the status becomes `APPROVED`.

`POST /integration/v1/DMED/form-058-1/{idOrUuid}/confirm` · scope `form0581:confirm`

- The form is identified in the path as `{idOrUuid}`: the numeric `id` **or** the `uuid` (as returned on submit).
- `{source}` — **always `DMED`**; your `sourceKey` must also be `dmed`, otherwise `403`. The organization comes from the `X-Organization-Id` header (**mandatory**); there is no `senderOrganizationUuid` in the body, the confirming staff member is sent as `submittedBy` (**mandatory**).
- **Only the organization that sent the form** can confirm it, and only after the receiver has accepted it (status `CARD_LINKED`); any other status gives `409`, a different organization gives `403`.
- Scope `form0581:confirm` is **separate** from `form0581:submit`; it must be granted to the client explicitly, otherwise `403`.

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
