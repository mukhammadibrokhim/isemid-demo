# Integration API — Patient / Пациент / Bemor

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

Two things live here: (1) the `GET .../patients/case` lookup, (2) the **shared `patient` block** that every form submission ([form058](../form058/README.md), [form0581](../form0581/README.md), [form129](../form129/README.md)) embeds. Authentication: [auth](../auth/README.md).

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

## 1. Bemor bo'yicha qidiruv

```http
GET /integration/v1/{source}/patients/case?identifierValue=<hujjat qiymati>
```

- Scope: `patient-case:read`. `{source}` klientning `sourceKey` iga teng bo'lishi shart.
- `identifierValue` — shaxsni tasdiqlovchi hujjat qiymati (JSHSHIR/PINFL/NNUZB, pasport va h.k.). Aynan qiymat bo'yicha qidiriladi.
- Tashkilot `X-Organization-Id` header orqali aniqlanadi (majburiy — [auth](../auth/README.md) 3.3).
- Har bir forma faqat so'rovchi tashkilot shu formaning **yuboruvchisi yoki qabul qiluvchisi** bo'lgandagina ko'rinadi.

Javob (`latestForm058` va `latestForm0581` bir-biridan mustaqil `null` bo'lishi mumkin):

```json
{
  "patient": {
    "uuid": "…", "firstName": "…", "lastName": "…", "middleName": "…",
    "birthDate": "1990-05-17", "genderCode": "MALE",
    "identifiers": [ { "typeCode": "PINFL", "value": "3xxxxxxxxxxxxx" } ]
  },
  "latestForm058": {
    "uuid": "…", "status": "SENT", "source": "LABX",
    "mkb10Code": "A00", "mkb10Name": "…", "finalMkb10Code": null, "finalMkb10Name": null,
    "diseaseDate": "2026-10-01T08:00:00", "firstVisitDate": "…", "visitDate": "…", "initialReportDateTime": "…"
  },
  "latestForm0581": {
    "uuid": "…", "status": "SENT", "source": "LABX",
    "mkb10Code": "A82", "mkb10Name": "…", "finalMkb10Code": null, "finalMkb10Name": null,
    "injuryDateTime": "…", "dpuVisitDateTime": "…"
  }
}
```

## 2. Umumiy `patient` bloki (formalarda)

`IntegrationPatientRequest`:

| Maydon | Majburiy | Izoh |
|---|---|---|
| `firstName`, `lastName`, `middleName` | yo'q | ≤ 255 |
| `birthDate` | yo'q | `yyyy-MM-dd`, kelajakda bo'lmasin |
| `genderCode` | yo'q | ma'lumotnoma kodi, ≤ 64 |
| `phoneNumber` | yo'q | ≤ 32 |
| `kinshipDegree`, `kinshipFullName` | yo'q | yaqin kishi orqali berilgan bo'lsa |
| `residentialStatusCode`, `maritalStatusCode`, `populationTypeCode`, `categoryCode`, `professionCode` | yo'q | ma'lumotnoma kodlari, ≤ 64 |
| `identifiers[]` | **ha**, bo'sh emas | `{ type (≤30, majburiy), value (≤100, majburiy), periodStart?, periodEnd? }` |
| `addresses[]` | **ha**, bo'sh emas | `{ type: PERMANENT\|TEMPORARY, regionCode, districtCode, neighborhoodCode, streetAddress, houseNumber, apartmentNumber }` (`stateCode`/`cityCode` — `regionCode`/`districtCode` ning sinonimi) |
| `affiliations[]` | yo'q | `{ type: WORKPLACE\|EDUCATIONAL, lastVisitedDate, organizationName, regionCode (yoki stateCode), cityCode, organizationId, organizationUuid, address }` — ro'yxatda bo'lmagan tashkilotni nom/manzil bilan berish mumkin |

**Identifikator qoidasi.** `type` `PINFL`, `NNUZB` yoki `JSHSHIR` bo'lsa (registrga e'tibor bermaydi), `value` aynan 14 raqam bo'lishi va tuzilma tekshiruvidan o'tishi shart: 1-raqam 1–6, kun 01–31, oy 01–12, oxirgi raqam — kontrol raqam (13 ta raqam `7,3,1,7,3,1,7,3,1,7,3,1,7` vaznlari bilan yig'indisi mod 10). Xato: `integration.patient.identifier.pinfl-format` / `pinfl-invalid`. Boshqa turlar (`PASSPORT`, `PPN`, …) faqat uzunlik bo'yicha tekshiriladi. Tavsiya etilgan turlar: `NNUZB` (= PINFL) va `PPN` (= pasport). DMED 058 uchun ikkalasi ham majburiy.

---

<a id="ru"></a>
# 🇷🇺 Русский

## 1. Поиск по пациенту

```http
GET /integration/v1/{source}/patients/case?identifierValue=<значение документа>
```

- Scope: `patient-case:read`. `{source}` должен совпадать с `sourceKey` клиента.
- `identifierValue` — значение документа, удостоверяющего личность (ПИНФЛ/NNUZB/JSHSHIR, паспорт и т. д.). Поиск по точному значению.
- Организация определяется заголовком `X-Organization-Id` (обязателен — [auth](../auth/README.md) п. 3.3).
- Форма видна, только если организация вызывающего была её **отправителем или получателем**.

Ответ (`latestForm058` и `latestForm0581` могут независимо быть `null`):

```json
{
  "patient": {
    "uuid": "…", "firstName": "…", "lastName": "…", "middleName": "…",
    "birthDate": "1990-05-17", "genderCode": "MALE",
    "identifiers": [ { "typeCode": "PINFL", "value": "3xxxxxxxxxxxxx" } ]
  },
  "latestForm058": {
    "uuid": "…", "status": "SENT", "source": "LABX",
    "mkb10Code": "A00", "mkb10Name": "…", "finalMkb10Code": null, "finalMkb10Name": null,
    "diseaseDate": "2026-10-01T08:00:00", "firstVisitDate": "…", "visitDate": "…", "initialReportDateTime": "…"
  },
  "latestForm0581": {
    "uuid": "…", "status": "SENT", "source": "LABX",
    "mkb10Code": "A82", "mkb10Name": "…", "finalMkb10Code": null, "finalMkb10Name": null,
    "injuryDateTime": "…", "dpuVisitDateTime": "…"
  }
}
```

## 2. Общий блок `patient` (в формах)

`IntegrationPatientRequest`:

| Поле | Обяз. | Описание |
|---|---|---|
| `firstName`, `lastName`, `middleName` | нет | ≤ 255 |
| `birthDate` | нет | `yyyy-MM-dd`, не в будущем |
| `genderCode` | нет | код справочника, ≤ 64 |
| `phoneNumber` | нет | ≤ 32 |
| `kinshipDegree`, `kinshipFullName` | нет | если сведения дал родственник |
| `residentialStatusCode`, `maritalStatusCode`, `populationTypeCode`, `categoryCode`, `professionCode` | нет | коды справочников, ≤ 64 |
| `identifiers[]` | **да**, не пусто | `{ type (≤30, обяз.), value (≤100, обяз.), periodStart?, periodEnd? }` |
| `addresses[]` | **да**, не пусто | `{ type: PERMANENT\|TEMPORARY, regionCode, districtCode, neighborhoodCode, streetAddress, houseNumber, apartmentNumber }` (`stateCode`/`cityCode` — синонимы `regionCode`/`districtCode`) |
| `affiliations[]` | нет | `{ type: WORKPLACE\|EDUCATIONAL, lastVisitedDate, organizationName, regionCode (или stateCode), cityCode, organizationId, organizationUuid, address }` — организацию, которой нет в системе, можно передать названием/адресом |

**Правило идентификатора.** Если `type` — `PINFL`, `NNUZB` или `JSHSHIR` (без учёта регистра), `value` должно состоять ровно из 14 цифр и пройти структурную проверку: 1-я цифра 1–6, день 01–31, месяц 01–12, последняя цифра — контрольная (сумма 13 цифр с весами `7,3,1,7,3,1,7,3,1,7,3,1,7` по модулю 10). Ошибки: `integration.patient.identifier.pinfl-format` / `pinfl-invalid`. Другие типы (`PASSPORT`, `PPN`, …) проверяются только по длине. Рекомендуемые типы: `NNUZB` (= PINFL) и `PPN` (= паспорт). Для DMED 058 обязательны оба.

---

<a id="en"></a>
# 🇬🇧 English

## 1. Patient lookup

```http
GET /integration/v1/{source}/patients/case?identifierValue=<document value>
```

- Scope: `patient-case:read`. `{source}` must equal the client's `sourceKey`.
- `identifierValue` — value of an identity document (JSHSHIR/PINFL/NNUZB, passport, …). Exact-value match.
- Organization comes from the `X-Organization-Id` header (mandatory — [auth](../auth/README.md) §3.3).
- A form is returned only if the caller's organization was its **sender or receiver**.

Response (`latestForm058` and `latestForm0581` can each independently be `null`):

```json
{
  "patient": {
    "uuid": "…", "firstName": "…", "lastName": "…", "middleName": "…",
    "birthDate": "1990-05-17", "genderCode": "MALE",
    "identifiers": [ { "typeCode": "PINFL", "value": "3xxxxxxxxxxxxx" } ]
  },
  "latestForm058": {
    "uuid": "…", "status": "SENT", "source": "LABX",
    "mkb10Code": "A00", "mkb10Name": "…", "finalMkb10Code": null, "finalMkb10Name": null,
    "diseaseDate": "2026-10-01T08:00:00", "firstVisitDate": "…", "visitDate": "…", "initialReportDateTime": "…"
  },
  "latestForm0581": {
    "uuid": "…", "status": "SENT", "source": "LABX",
    "mkb10Code": "A82", "mkb10Name": "…", "finalMkb10Code": null, "finalMkb10Name": null,
    "injuryDateTime": "…", "dpuVisitDateTime": "…"
  }
}
```

## 2. Shared `patient` block (inside forms)

`IntegrationPatientRequest`:

| Field | Req. | Notes |
|---|---|---|
| `firstName`, `lastName`, `middleName` | no | ≤ 255 |
| `birthDate` | no | `yyyy-MM-dd`, not in the future |
| `genderCode` | no | reference code, ≤ 64 |
| `phoneNumber` | no | ≤ 32 |
| `kinshipDegree`, `kinshipFullName` | no | when a relative provided the data |
| `residentialStatusCode`, `maritalStatusCode`, `populationTypeCode`, `categoryCode`, `professionCode` | no | reference codes, ≤ 64 |
| `identifiers[]` | **yes**, non-empty | `{ type (≤30, required), value (≤100, required), periodStart?, periodEnd? }` |
| `addresses[]` | **yes**, non-empty | `{ type: PERMANENT\|TEMPORARY, regionCode, districtCode, neighborhoodCode, streetAddress, houseNumber, apartmentNumber }` (`stateCode`/`cityCode` are aliases of `regionCode`/`districtCode`) |
| `affiliations[]` | no | `{ type: WORKPLACE\|EDUCATIONAL, lastVisitedDate, organizationName, regionCode (or stateCode), cityCode, organizationId, organizationUuid, address }` — an organization unknown to the system can be given by name/address |

**Identifier rule.** When `type` is `PINFL`, `NNUZB` or `JSHSHIR` (case-insensitive), `value` must be exactly 14 digits and structurally valid: first digit 1–6, day 01–31, month 01–12, last digit is a check digit (sum of the first 13 digits weighted `7,3,1,7,3,1,7,3,1,7,3,1,7`, mod 10). Errors: `integration.patient.identifier.pinfl-format` / `pinfl-invalid`. Other types (`PASSPORT`, `PPN`, …) are length-checked only. Recommended types: `NNUZB` (= PINFL) and `PPN` (= passport). Both are mandatory for DMED 058.
