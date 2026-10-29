# Integration API — Form 129 / Форма №129 / 129-shakl

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

`POST /integration/v1/{source}/form-129` · scope `form129:submit` · auth: [auth](../auth/README.md) · patient block: [patient](../patient/README.md)

One generic endpoint for every source (DMED included — there is no separate DMED contract for Form 129).

**Webhook (status-change notifications / уведомления о смене статуса / holat o'zgarishi xabarlari):** [webhook](webhook/README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

Laboratoriya serologiya xabarnomasi (zaxm / gepatit B / brutsellyoz). Hayot sikli oddiy: yaratiladi → qabul qiluvchi qabul qiladi yoki rad etadi. Integratsiya orqali faqat yaratish mumkin.

## So'rov

| Maydon | Majburiy | Izoh |
|---|---|---|
| `reportingInstitutionName` | ha | ≤ 500 |
| `medicalId` | ha | ≤ 128 |
| `patient` | ha | [patient bloki](../patient/README.md#uz) |
| `receiverOrganizationId` | ha | UUID; sanitar-epidemiologik xizmat muassasasi |
| `notifierFullName` | ha | ≤ 255 |
| `senderOrganizationUuid` | ha | formani yuborayotgan tashkilot UUID si. `EXTERNAL_TOKEN` klienti uchun ruxsat etilgan tashkilotlar ro'yxatida bo'lishi, boshqalar uchun o'z tashkilotiga teng bo'lishi shart ([auth](../auth/README.md#uz) 3.3) |
| `submittedBy` | ha | audit uchun: `{ pinfl, fullName, position, phone? }` |

Autentifikatsiya: scope `form129:submit`; usullar — client credentials, API key, Basic, IP allow-list, SSO/DHP token ([auth](../auth/README.md#uz)).

Laboratoriya natijalari (hammasi ixtiyoriy; har biri `…Outcome` + `…ResultText` (≤ 500) jufti):

| Test | Maydonlar | `Outcome` qiymatlari |
|---|---|---|
| RW | `rwOutcome`, `rwResultText` | `NEGATIVE`, `POSITIVE` |
| RPR/VDRL | `rprVdrlOutcome`, `rprVdrlResultText` | xuddi shunday |
| RPGA | `rpgaOutcome`, `rpgaResultText` | xuddi shunday |
| ELISA | `elisaOutcome`, `elisaResultText` | xuddi shunday |
| TPHA | `tphaOutcome`, `tphaResultText` | xuddi shunday |
| Western blot | `westernBlotOutcome`, `westernBlotResultText` | xuddi shunday |
| HBsAg | `hbsAgOutcome`, `hbsAgResultText` | xuddi shunday |
| HBeAg | `hbeAgOutcome`, `hbeAgResultText` | xuddi shunday |
| Anti-HBc IgG | `antiHbcIgGOutcome`, `antiHbcIgGResultText` | xuddi shunday |
| Anti-HBc IgM | `antiHbcIgMOutcome`, `antiHbcIgMResultText` | xuddi shunday |
| Anti-HBe | `antiHbeOutcome`, `antiHbeResultText` | xuddi shunday |
| Anti-HBs | `antiHbsOutcome`, `antiHbsResultText` | xuddi shunday |
| PCR (sifat) | `pcrQualitativeOutcome`, `pcrQualitativeResultText` | xuddi shunday |
| Wright–Heddelson | `wrightHeddelsonOutcome`, `wrightHeddelsonResultText` | `NEGATIVE`, `DOUBTFUL`, `POSITIVE` |

Namuna:

```json
{
  "reportingInstitutionName": "Markaziy laboratoriya",
  "medicalId": "LAB-001",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "11111111-1111-1111-1111-111111111111",
  "hbsAgOutcome": "POSITIVE", "hbsAgResultText": "1:64",
  "wrightHeddelsonOutcome": "DOUBTFUL",
  "notifierFullName": "Dr. Karimov",
  "senderOrganizationUuid": "22222222-2222-2222-2222-222222222222",
  "submittedBy": { "pinfl": "30101900123456", "fullName": "Karimov B.", "position": "Laborant" }
}
```

## Javob

```json
{ "id": 58, "uuid": "…", "status": "SENT" }
```

---

<a id="ru"></a>
# 🇷🇺 Русский

Лабораторное серологическое извещение (сифилис / гепатит B / бруцеллёз). Простой жизненный цикл: создание → получатель принимает или отклоняет. Через интеграцию можно только создавать.

## Запрос

| Поле | Обяз. | Описание |
|---|---|---|
| `reportingInstitutionName` | да | ≤ 500 |
| `medicalId` | да | ≤ 128 |
| `patient` | да | [блок patient](../patient/README.md#ru) |
| `receiverOrganizationId` | да | UUID; учреждение санитарно-эпидемиологической службы |
| `notifierFullName` | да | ≤ 255 |
| `senderOrganizationUuid` | да | UUID организации-отправителя. Для клиента `EXTERNAL_TOKEN` должна входить в список разрешённых организаций, для остальных — совпадать с их организацией ([auth](../auth/README.md#ru) п. 3.3) |
| `submittedBy` | да | для аудита: `{ pinfl, fullName, position, phone? }` |

Аутентификация: scope `form129:submit`; способы — client credentials, API key, Basic, IP allow-list, токен SSO/DHP ([auth](../auth/README.md#ru)).

Результаты анализов (все необязательны; каждый — пара `…Outcome` + `…ResultText` (≤ 500)):

| Тест | Поля | Значения `Outcome` |
|---|---|---|
| RW | `rwOutcome`, `rwResultText` | `NEGATIVE`, `POSITIVE` |
| RPR/VDRL | `rprVdrlOutcome`, `rprVdrlResultText` | то же |
| RPGA | `rpgaOutcome`, `rpgaResultText` | то же |
| ELISA | `elisaOutcome`, `elisaResultText` | то же |
| TPHA | `tphaOutcome`, `tphaResultText` | то же |
| Western blot | `westernBlotOutcome`, `westernBlotResultText` | то же |
| HBsAg | `hbsAgOutcome`, `hbsAgResultText` | то же |
| HBeAg | `hbeAgOutcome`, `hbeAgResultText` | то же |
| Anti-HBc IgG | `antiHbcIgGOutcome`, `antiHbcIgGResultText` | то же |
| Anti-HBc IgM | `antiHbcIgMOutcome`, `antiHbcIgMResultText` | то же |
| Anti-HBe | `antiHbeOutcome`, `antiHbeResultText` | то же |
| Anti-HBs | `antiHbsOutcome`, `antiHbsResultText` | то же |
| PCR (качеств.) | `pcrQualitativeOutcome`, `pcrQualitativeResultText` | то же |
| Райт–Хеддельсон | `wrightHeddelsonOutcome`, `wrightHeddelsonResultText` | `NEGATIVE`, `DOUBTFUL`, `POSITIVE` |

Пример:

```json
{
  "reportingInstitutionName": "Центральная лаборатория",
  "medicalId": "LAB-001",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "11111111-1111-1111-1111-111111111111",
  "hbsAgOutcome": "POSITIVE", "hbsAgResultText": "1:64",
  "wrightHeddelsonOutcome": "DOUBTFUL",
  "notifierFullName": "Dr. Karimov",
  "senderOrganizationUuid": "22222222-2222-2222-2222-222222222222",
  "submittedBy": { "pinfl": "30101900123456", "fullName": "Karimov B.", "position": "Лаборант" }
}
```

## Ответ

```json
{ "id": 58, "uuid": "…", "status": "SENT" }
```

---

<a id="en"></a>
# 🇬🇧 English

Lab serology notification (syphilis / hepatitis B / brucellosis). Simple lifecycle: create → the receiver accepts or rejects. Through the integration API you can only create.

## Request

| Field | Req. | Notes |
|---|---|---|
| `reportingInstitutionName` | yes | ≤ 500 |
| `medicalId` | yes | ≤ 128 |
| `patient` | yes | [patient block](../patient/README.md#en) |
| `receiverOrganizationId` | yes | UUID; a sanitary-epidemiological service institution |
| `notifierFullName` | yes | ≤ 255 |
| `senderOrganizationUuid` | yes | UUID of the sending organization. For an `EXTERNAL_TOKEN` client it must be in its allowed-organizations list; for other callers it must equal their own organization ([auth](../auth/README.md#en) §3.3) |
| `submittedBy` | yes | for audit: `{ pinfl, fullName, position, phone? }` |

Authentication: scope `form129:submit`; methods — client credentials, API key, Basic, IP allow-list, SSO/DHP token ([auth](../auth/README.md#en)).

Lab results (all optional; each is an `…Outcome` + `…ResultText` (≤ 500) pair):

| Test | Fields | `Outcome` values |
|---|---|---|
| RW | `rwOutcome`, `rwResultText` | `NEGATIVE`, `POSITIVE` |
| RPR/VDRL | `rprVdrlOutcome`, `rprVdrlResultText` | same |
| RPGA | `rpgaOutcome`, `rpgaResultText` | same |
| ELISA | `elisaOutcome`, `elisaResultText` | same |
| TPHA | `tphaOutcome`, `tphaResultText` | same |
| Western blot | `westernBlotOutcome`, `westernBlotResultText` | same |
| HBsAg | `hbsAgOutcome`, `hbsAgResultText` | same |
| HBeAg | `hbeAgOutcome`, `hbeAgResultText` | same |
| Anti-HBc IgG | `antiHbcIgGOutcome`, `antiHbcIgGResultText` | same |
| Anti-HBc IgM | `antiHbcIgMOutcome`, `antiHbcIgMResultText` | same |
| Anti-HBe | `antiHbeOutcome`, `antiHbeResultText` | same |
| Anti-HBs | `antiHbsOutcome`, `antiHbsResultText` | same |
| PCR (qualitative) | `pcrQualitativeOutcome`, `pcrQualitativeResultText` | same |
| Wright–Heddelson | `wrightHeddelsonOutcome`, `wrightHeddelsonResultText` | `NEGATIVE`, `DOUBTFUL`, `POSITIVE` |

Example:

```json
{
  "reportingInstitutionName": "Central laboratory",
  "medicalId": "LAB-001",
  "patient": {
    "firstName": "Ali", "lastName": "Valiyev", "birthDate": "1990-05-17",
    "identifiers": [ { "type": "PINFL", "value": "31705901234560" } ],
    "addresses": [ { "type": "PERMANENT", "regionCode": "UZ-QA", "districtCode": "QA-212" } ]
  },
  "receiverOrganizationId": "11111111-1111-1111-1111-111111111111",
  "hbsAgOutcome": "POSITIVE", "hbsAgResultText": "1:64",
  "wrightHeddelsonOutcome": "DOUBTFUL",
  "notifierFullName": "Dr. Karimov",
  "senderOrganizationUuid": "22222222-2222-2222-2222-222222222222",
  "submittedBy": { "pinfl": "30101900123456", "fullName": "Karimov B.", "position": "Lab technician" }
}
```

## Response

```json
{ "id": 58, "uuid": "…", "status": "SENT" }
```
