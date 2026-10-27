# Integration API — Form 058 / Форма №058 / 058-shakl

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

`POST /integration/v1/{source}/form-058` · scope `form058:submit` / `form058:confirm` · auth: [auth](../auth/README.md) · patient block: [patient](../patient/README.md)

**Webhook (status-change notifications / уведомления о смене статуса / holat o'zgarishi xabarlari):** [webhook](webhook/README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

Yuqumli/parazitar kasallik haqida 058-shakl xabarnomasini yaratadi. Forma holati yaratilgandan so'ng javobdagi `status` da qaytadi.

## So'rov

Header: `Content-Type: application/json`, autentifikatsiya headerlari ([auth](../auth/README.md)).
`{source}` — klientning `sourceKey` i.
Tashkilot tanadagi `senderOrganizationUuid` dan olinadi. `X-Organization-Id` yuborsangiz, xuddi shu UUID bo'lsin (ishonchli yo'l — ikkalasini yuborish, [auth](../auth/README.md)).
Xato xabarlari tili: `Accept-Language` (`uz`, `uz-Cyrl`, `kaa`, `ru`, `en`; standart — `uz`).

| Maydon | Majburiy | Izoh |
|---|---|---|
| `diagnosisInfo.mkb10Code` | ha | ≤ 20; MKB-10 ma'lumotnomasida bo'lishi shart, aks holda xato qaytariladi |
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
| `location` | yo'q | `{ latitude (−90..90), longitude (−180..180), location (≤1000) }`; ichidagi barcha maydonlar ixtiyoriy |
| `epidemicInfo.diseasePlaceCode` | ha | ma'lumotnoma kodi, ≤ 64 |
| `epidemicInfo.diseaseCause`, `epidemicInfo.epidemicMeasures` | yo'q | ≤ 2000 |
| `reportInfo.notifierFullName` | ha | ≤ 255 |
| `reportInfo.journalFormCode` | ha | ma'lumotnoma kodi, ≤ 64 |
| `reportInfo.comment` | yo'q | ≤ 2000 |
| `senderOrganizationUuid` | shartli | **integratsiya klientlari uchun majburiy** (barcha autentifikatsiya usullarida): formani yuborayotgan faol tashkilot UUID si; topilmasa yoki faol bo'lmasa `400`. Provayderga bog'langan klient (`EXTERNAL_TOKEN`) faqat o'z provayderining tashkilotini ko'rsata oladi, aks holda `403`. Klient bo'lmagan SSO/DHP foydalanuvchisi uchun ixtiyoriy; berilsa, tanlangan tashkilotga teng bo'lishi shart |
| `submittedBy` | shartli | integratsiya klientlari uchun majburiy (audit): `{ pinfl, fullName, position, phone? }` |

**Sanalar tartibi** (aks holda `VALIDATION_FAILED`):
`diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.

Barcha sanalarni bir xil formatda (`yyyy-MM-dd'T'HH:mm:ss`) yuboring. Zona ko'rsatilmaydi, "kelajak emas" sharti server vaqtiga nisbatan tekshiriladi — UTC emas, mahalliy vaqtni yuboring, aks holda yaqin o'tmishdagi sana rad etilishi mumkin.

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

(Kodlar — ma'lumotnoma qiymatlari; namunadagilar illyustrativ. `diseasePlaceCode`, `journalFormCode`, `hospitalPlaceId` va `receiverOrganizationId` qiymatlari ro'yxati bu hujjatda keltirilmagan — ularni integratsiya jamoasi bilan kelishing.)

## Javob

```json
{ "id": 1042, "uuid": "…", "status": "SENT" }
```

### Statuslar

| Status | Ma'nosi |
|---|---|
| `SENT` | yuborildi, qabul qiluvchi hali qaror qilmagan |
| `ACCEPTED` | qabul qiluvchi qabul qildi — tasdiqlash (confirm) mumkin |
| `CARD_LINKED` | formaga karta bog'landi — tasdiqlash mumkin |
| `APPROVED` | yakuniy diagnoz bilan tasdiqlandi |
| `CANCELED` | yuboruvchi qaytarib oldi yoki qabul qiluvchi rad etdi (faqat `SENT` holatidan); yopiq |

Bu API'da faqat yuborish va tasdiqlash bor — formani tahrirlash yoki bekor qilish yo'q. Holat o'zgarishini [webhook](webhook/README.md) orqali oling.

### Qayta yuborish

Yuborish (submit) idempotent emas: takroriy `POST` yangi forma yaratishi mumkin. Javob kelmasa, qayta yuborishdan oldin natijani qabul qiluvchi tomon bilan tekshiring.

### Xatolar

```json
{ "success": false, "code": "VALIDATION_FAILED", "message": "…", "traceId": "9f1c2a3b4d",
  "path": "/integration/v1/labx/form-058", "timestamp": "…", "violations": [ … ] }
```

| HTTP | `code` | Sabab |
|---|---|---|
| `400` | `VALIDATION_FAILED`, `MALFORMED_JSON`, `REQUEST_BODY_MISSING` | maydon validatsiyasi, sanalar tartibi, tashkilot topilmadi/faol emas, `submittedBy` yo'q, MKB-10 ma'lumotnomada yo'q |
| `401` | — | autentifikatsiya xato/yo'q ([auth](../auth/README.md)) |
| `403` | `FORBIDDEN`, `SCOPE_VIOLATION` | scope yo'q, `{source}` mos emas, tashkilot ruxsat etilmagan, forma boshqa tashkilotniki |
| `404` | `NOT_FOUND` | confirm: forma topilmadi |
| `409` | `CONFLICT` | confirm: status tasdiqlanadigan emas |

Murojaat qilganda `traceId` ni keltiring.

## Tasdiqlash (confirm)

Yuborilgan shaklni yakuniy diagnoz (MKB-10) bilan tasdiqlaydi — status `APPROVED` bo'ladi.

`POST /integration/v1/{source}/form-058/{idOrUuid}/confirm` · scope `form058:confirm`

- Shakl yo'lda `{idOrUuid}` bilan ko'rsatiladi: raqamli `id` **yoki** `uuid` (yuborishda javobda qaytgan qiymat).
- `{source}` — sizning `sourceKey` bilan bir xil bo'lishi kerak, aks holda `403`.
- Tasdiqlashni **faqat shaklni yuborgan tashkilot** qila oladi va faqat qabul qiluvchi shaklni qabul qilgandan keyin (status `ACCEPTED` / `CARD_LINKED`); boshqa statusda `409`, boshqa tashkilot bo'lsa `403`, forma topilmasa `404`.
- Scope `form058:confirm` — `form058:submit` dan **alohida**; mijozga alohida berilishi kerak, aks holda `403`.

| Maydon | Maj. | Izoh |
|---|---|---|
| `finalIcd10Code` | ✔ | MKB-10 kodi, ≤ 32 (yuborishdagi `mkb10Code` ≤ 20); **ma'lumotnomada bo'lishi shart**, aks holda `400` |
| `finalIcd10Name` |  | ≤ 1024; yuborilmasa ma'lumotnomadagi nom olinadi |
| `senderOrganizationUuid` |  | shaklni yuborgan tashkilot UUID; **integratsiya klientlari uchun majburiy** (yuborishdagi qoida bilan bir xil), formaning yuboruvchi tashkilotiga teng bo'lishi shart, aks holda `403` |
| `submittedBy` |  | integratsiya klientlari uchun majburiy (audit) |

```json
{
  "finalIcd10Code": "A01.0",
  "finalIcd10Name": "Typhoid fever",
  "senderOrganizationUuid": "e9140422-0562-4733-b709-150832dd5d77"
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

`POST /integration/v1/DMED/form-058` — [dmed/README.md](dmed/README.md)

---

<a id="ru"></a>
# 🇷🇺 Русский

Создаёт извещение формы №058 об инфекционном/паразитарном заболевании. Статус формы после создания возвращается в поле `status` ответа.

## Запрос

Заголовки: `Content-Type: application/json`, заголовки аутентификации ([auth](../auth/README.md)).
`{source}` — `sourceKey` клиента.
Организация берётся из `senderOrganizationUuid` в теле. Если передаёте `X-Organization-Id`, UUID должен совпадать (надёжный способ — передавать оба, см. [auth](../auth/README.md)).
Язык сообщений об ошибках: `Accept-Language` (`uz`, `uz-Cyrl`, `kaa`, `ru`, `en`; по умолчанию `uz`).

| Поле | Обяз. | Описание |
|---|---|---|
| `diagnosisInfo.mkb10Code` | да | ≤ 20; должен существовать в справочнике МКБ-10, иначе возвращается ошибка |
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
| `location` | нет | `{ latitude (−90..90), longitude (−180..180), location (≤1000) }`; все поля внутри необязательны |
| `epidemicInfo.diseasePlaceCode` | да | код справочника, ≤ 64 |
| `epidemicInfo.diseaseCause`, `epidemicInfo.epidemicMeasures` | нет | ≤ 2000 |
| `reportInfo.notifierFullName` | да | ≤ 255 |
| `reportInfo.journalFormCode` | да | код справочника, ≤ 64 |
| `reportInfo.comment` | нет | ≤ 2000 |
| `senderOrganizationUuid` | условно | **обязательно для интеграционных клиентов** (при любом способе аутентификации): UUID активной организации-отправителя; не найдена или неактивна — `400`. Клиент, привязанный к провайдеру (`EXTERNAL_TOKEN`), может указать только организацию своего провайдера, иначе `403`. Для SSO/DHP-пользователя (не клиента) необязательно; если указано, должно совпадать с выбранной организацией |
| `submittedBy` | условно | обязательно для интеграционных клиентов (аудит): `{ pinfl, fullName, position, phone? }` |

**Порядок дат** (иначе `VALIDATION_FAILED`):
`diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.

Передавайте все даты в одном формате (`yyyy-MM-dd'T'HH:mm:ss`). Часовой пояс не указывается, проверка «не в будущем» идёт по времени сервера — отправляйте местное время, а не UTC, иначе недавняя дата может быть отклонена.

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

(Коды — значения справочников; в примере они иллюстративны. Перечни значений `diseasePlaceCode`, `journalFormCode`, `hospitalPlaceId` и `receiverOrganizationId` в этом документе не приводятся — согласуйте их с командой интеграции.)

## Ответ

```json
{ "id": 1042, "uuid": "…", "status": "SENT" }
```

### Статусы

| Статус | Значение |
|---|---|
| `SENT` | отправлена, получатель ещё не принял решение |
| `ACCEPTED` | получатель принял — можно подтверждать (confirm) |
| `CARD_LINKED` | к форме привязана карта — можно подтверждать |
| `APPROVED` | подтверждена итоговым диагнозом |
| `CANCELED` | отправитель отозвал или получатель отклонил (только из `SENT`); закрыта |

В этом API есть только отправка и подтверждение — редактирования и отмены формы нет. О смене статуса узнавайте через [webhook](webhook/README.md).

### Повторная отправка

Отправка (submit) не идемпотентна: повторный `POST` может создать новую форму. Если ответ не получен, перед повторной отправкой уточните результат у принимающей стороны.

### Ошибки

```json
{ "success": false, "code": "VALIDATION_FAILED", "message": "…", "traceId": "9f1c2a3b4d",
  "path": "/integration/v1/labx/form-058", "timestamp": "…", "violations": [ … ] }
```

| HTTP | `code` | Причина |
|---|---|---|
| `400` | `VALIDATION_FAILED`, `MALFORMED_JSON`, `REQUEST_BODY_MISSING` | валидация полей, порядок дат, организация не найдена/неактивна, нет `submittedBy`, МКБ-10 нет в справочнике |
| `401` | — | аутентификация неверна/отсутствует ([auth](../auth/README.md)) |
| `403` | `FORBIDDEN`, `SCOPE_VIOLATION` | нет scope, `{source}` не совпал, организация не разрешена, форма чужой организации |
| `404` | `NOT_FOUND` | confirm: форма не найдена |
| `409` | `CONFLICT` | confirm: статус не подтверждаемый |

При обращении в поддержку указывайте `traceId`.

## Подтверждение (confirm)

Подтверждает ранее отправленную форму итоговым диагнозом (МКБ-10) — статус становится `APPROVED`.

`POST /integration/v1/{source}/form-058/{idOrUuid}/confirm` · scope `form058:confirm`

- Форма указывается в пути как `{idOrUuid}`: числовой `id` **или** `uuid` (значение из ответа при отправке).
- `{source}` — должен совпадать с вашим `sourceKey`, иначе `403`.
- Подтвердить может **только организация-отправитель формы**, и только после принятия формы получателем (статус `ACCEPTED` / `CARD_LINKED`); в другом статусе — `409`, для чужой организации — `403`, если форма не найдена — `404`.
- Scope `form058:confirm` — **отдельный** от `form058:submit`; его нужно выдать клиенту отдельно, иначе `403`.

| Поле | Обяз. | Описание |
|---|---|---|
| `finalIcd10Code` | ✔ | код МКБ-10, ≤ 32 (при отправке `mkb10Code` ≤ 20); **должен существовать в справочнике**, иначе `400` |
| `finalIcd10Name` |  | ≤ 1024; если не передано, берётся название из справочника |
| `senderOrganizationUuid` |  | UUID организации-отправителя формы; **обязателен для интеграционных клиентов** (то же правило, что при отправке), должен совпадать с организацией-отправителем формы, иначе `403` |
| `submittedBy` |  | обязателен для интеграционных клиентов (аудит) |

```json
{
  "finalIcd10Code": "A01.0",
  "finalIcd10Name": "Typhoid fever",
  "senderOrganizationUuid": "e9140422-0562-4733-b709-150832dd5d77"
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

`POST /integration/v1/DMED/form-058` — [dmed/README.md](dmed/README.md)

---

<a id="en"></a>
# 🇬🇧 English

Creates a Form 058 notification of an infectious/parasitic disease. The form's status after creation is returned in the response `status`.

## Request

Headers: `Content-Type: application/json`, auth headers ([auth](../auth/README.md)).
`{source}` is the client's `sourceKey`.
The organization comes from `senderOrganizationUuid` in the body. If you also send `X-Organization-Id`, it must carry the same UUID (the safest approach is to send both, see [auth](../auth/README.md)).
Error message language: `Accept-Language` (`uz`, `uz-Cyrl`, `kaa`, `ru`, `en`; default `uz`).

| Field | Req. | Notes |
|---|---|---|
| `diagnosisInfo.mkb10Code` | yes | ≤ 20; must exist in the ICD-10 catalog, otherwise an error is returned |
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
| `location` | no | `{ latitude (−90..90), longitude (−180..180), location (≤1000) }`; every field inside is optional |
| `epidemicInfo.diseasePlaceCode` | yes | reference code, ≤ 64 |
| `epidemicInfo.diseaseCause`, `epidemicInfo.epidemicMeasures` | no | ≤ 2000 |
| `reportInfo.notifierFullName` | yes | ≤ 255 |
| `reportInfo.journalFormCode` | yes | reference code, ≤ 64 |
| `reportInfo.comment` | no | ≤ 2000 |
| `senderOrganizationUuid` | conditional | **required for integration clients** (every authentication method): UUID of the active sending organization; unknown or inactive gives `400`. A provider-bound client (`EXTERNAL_TOKEN`) may name only an organization of its own provider, otherwise `403`. Optional for an SSO/DHP user who is not an integration client; if given, it must equal the selected organization |
| `submittedBy` | conditional | required for integration clients (audit): `{ pinfl, fullName, position, phone? }` |

**Date ordering** (otherwise `VALIDATION_FAILED`):
`diseaseDate ≤ firstVisitDate ≤ visitDate`; `admissionDate`, `diagnosisDate`, `initialReportDateTime` ≥ `diseaseDate`.

Send every date in the same format (`yyyy-MM-dd'T'HH:mm:ss`). No time zone is carried and the "not in the future" check uses the server's time, so send local time rather than UTC, otherwise a recent date may be rejected.

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

(Codes are reference-catalog values; the ones above are illustrative. The lists of valid `diseasePlaceCode`, `journalFormCode`, `hospitalPlaceId` and `receiverOrganizationId` values are not given here — agree them with the integration team.)

## Response

```json
{ "id": 1042, "uuid": "…", "status": "SENT" }
```

### Statuses

| Status | Meaning |
|---|---|
| `SENT` | submitted, the receiver has not decided yet |
| `ACCEPTED` | the receiver accepted it — can be confirmed |
| `CARD_LINKED` | a card was linked to the form — can be confirmed |
| `APPROVED` | confirmed with the final diagnosis |
| `CANCELED` | withdrawn by the sender or rejected by the receiver (from `SENT` only); closed |

This API only offers submit and confirm — there is no way to edit or cancel a form. Learn about status changes through the [webhook](webhook/README.md).

### Retrying

Submit is not idempotent: repeating the `POST` may create another form. If you get no response, check the outcome with the receiving side before resending.

### Errors

```json
{ "success": false, "code": "VALIDATION_FAILED", "message": "…", "traceId": "9f1c2a3b4d",
  "path": "/integration/v1/labx/form-058", "timestamp": "…", "violations": [ … ] }
```

| HTTP | `code` | Cause |
|---|---|---|
| `400` | `VALIDATION_FAILED`, `MALFORMED_JSON`, `REQUEST_BODY_MISSING` | field validation, date ordering, organization unknown/inactive, missing `submittedBy`, ICD-10 code not in the catalog |
| `401` | — | authentication wrong/missing ([auth](../auth/README.md)) |
| `403` | `FORBIDDEN`, `SCOPE_VIOLATION` | missing scope, `{source}` mismatch, organization not allowed, form belongs to another organization |
| `404` | `NOT_FOUND` | confirm: form not found |
| `409` | `CONFLICT` | confirm: the status is not confirmable |

Quote the `traceId` when contacting support.

## Confirm

Confirms a previously submitted form with a final diagnosis (ICD-10) — the status becomes `APPROVED`.

`POST /integration/v1/{source}/form-058/{idOrUuid}/confirm` · scope `form058:confirm`

- The form is identified in the path as `{idOrUuid}`: the numeric `id` **or** the `uuid` (as returned on submit).
- `{source}` — must match your `sourceKey`, otherwise `403`.
- **Only the organization that sent the form** can confirm it, and only after the receiver has accepted it (status `ACCEPTED` / `CARD_LINKED`); any other status gives `409`, a different organization gives `403`, an unknown form gives `404`.
- Scope `form058:confirm` is **separate** from `form058:submit`; it must be granted to the client explicitly, otherwise `403`.

| Field | Req. | Notes |
|---|---|---|
| `finalIcd10Code` | ✔ | ICD-10 code, ≤ 32 (`mkb10Code` on submit is ≤ 20); **must exist in the catalog**, otherwise `400` |
| `finalIcd10Name` |  | ≤ 1024; defaults to the catalog name |
| `senderOrganizationUuid` |  | UUID of the organization that sent the form; **required for integration clients** (same rule as on submit), must equal the form's sending organization, otherwise `403` |
| `submittedBy` |  | required for integration clients (audit) |

```json
{
  "finalIcd10Code": "A01.0",
  "finalIcd10Name": "Typhoid fever",
  "senderOrganizationUuid": "e9140422-0562-4733-b709-150832dd5d77"
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

`POST /integration/v1/DMED/form-058` — [dmed/README.md](dmed/README.md)
