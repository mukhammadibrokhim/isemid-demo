# Aktlar moduli — backend javoblari

**Kimga:** ISEMID/YKEM frontend · **Sana:** 2026-09-02 · **Branch:** `isemid-v2`
**Nimaga javob:** `act-backend-questions.md` + `act-backend-reply.md`

Har bir band ostida: **holat** (bajarildi / kutiladi / faqat javob), **backendda nima o'zgardi**,
**frontendda nima qilish kerak**. Contract tafsilotlari `docs/act-lis-frontend-guide.md` da
yangilandi.

> **⚠️ Deploy:** hamma o'zgarishlar `isemid-v2` branchida, hali **commit/deploy qilinmagan**.
> `172.16.14.123:18090/api-docs` da paydo bo'lishi uchun branch merge + deploy kerak — bu
> backend/DevOps tomonда. Deploy'gача frontend `yarn api:types` yangi tiplarni ko'rmaydi.

## Round 2 da nima bajarildi (`act-backend-reply.md` javobiga)

| Band | Holat |
|---|---|
| `subject` — beshala Request/DetailResponse + `ActTableResponse` | ✅ bazaviy `act.subject` (VARCHAR 500) |
| `ATTACH_ACT` enforcement | ✅ `@PreAuthorize` barcha act kontrollerlarida |
| READ ajratish (`/mine` ↔ `/v1/acts`) | ✅ `READ` vs yangi `VIEW_ALL` amali |
| `ActTableResponse` ga `actNumber` | ✅ `act_number` bazaviy `act` ga ko'chirildi |
| ACT153 `sampleQtUnit` | ✅ 153 dan olib tashlandi (faqat 154 da) — LIS savoli ochiq |
| `force: true` avto | frontend tomonда (backend o'zgarmaydi) |
| `sortBy` + `actType`/`updatedAt` | allaqachon `ActSortFields.ALLOWED` da |

---

## Yangi (2026-09-30): LIS natijasidan keyin vrach aktni yopadi — `RESULT_RECEIVED`

LIS natija yuborganda akt endi darhol `COMPLETED` bo'lmaydi — `RESULT_RECEIVED`
ga o'tadi. Biriktirilgan xodim (vrach) natijani ko'radi va:

- **rozi bo'lsa** — `PATCH /v1/acts/{id}/close` (body yo'q) → `COMPLETED`.
  Kim/qachon yopgani `lisInfo.closedById` / `lisInfo.closedAt` da. `COMPLETED` — yakuniy,
  tahrirlash/qayta yuborish/o'chirish yo'q.
- **rozi bo'lmasa** — aktni yopmaydi: `PUT /v1/acts/{id}` → `IN_PROGRESS`,
  `PATCH .../ready` → `READY`, `POST .../send-to-lis` → `SENT` (yoki to'g'ridan-to'g'ri
  `send-to-lis`). LIS da **yangi zayavka** bo'lib tushadi — backend `force` ni o'zi qo'yadi
  (`lisInfo.actId` bor bo'lsa), frontend `force: true` yuborishi shart emas.
- `RESULT_RECEIVED` aktni **o'chirib bo'lmaydi**.
- `ACT_LIS_RESPONSE` bildirishnomasi endi `RESULT_RECEIVED` / `RETURNED_BY_LIS` da keladi;
  yopishda bildirishnoma yo'q.

```
SENT ──(LIS natija)──→ RESULT_RECEIVED ──(vrach: close)──→ COMPLETED
                             └──(vrach rozi emas: tahrirlash / qayta yuborish)──→ SENT
```

## Yangi: LIS aktni qayta ishlashga qaytarishi mumkin — `RETURNED_BY_LIS`

Bu savolda yo'q edi, lekin kiritildi.

**Status oqimi endi:**

```
NEW → IN_PROGRESS → READY → SENT → COMPLETED
                     ↑       │ │
     SEND_FAILED ────┘       │ └──→ RETURNED_BY_LIS ──┐
          ↑ └────────────────┘                        │
          └────────────────────────(qayta ishlash)────┘
```

| Status | Ma'no | Frontend affordances |
|---|---|---|
| `SEND_FAILED` | Yuborishning **o'zi** uzildi (tarmoq / LIS rad etdi) — LIS ga yetib bormadi | Tuzatib qayta yuborish; **o'chirsa bo'ladi** |
| `RETURNED_BY_LIS` | LIS aktni qabul qildi, keyin natija o'rniga **qayta ishlashga qaytardi** | Tahrirlash → `ready` → `send-to-lis` (qayta yuborishda **`force: true`**); **o'chirib bo'lmaydi** |

- `RETURNED_BY_LIS` dan chiqish: `PUT /v1/acts/{id}` → `IN_PROGRESS`,
  `PATCH .../ready` → `READY`, `POST .../send-to-lis` → `SENT`.
  Bir xil `senderActNumber` ni LIS dublikat deb hisoblaydi — `force` bilan uni yangi
  zayavka sifatida qabul qiladi. 2026-09-30 dan backend `force` ni o'zi qo'yadi
  (`lisInfo.actId` bor bo'lsa); `force: true` yuborish ham zarar qilmaydi.
- Sababi `lisInfo.lastError` da, LIS javobi to'liq `lisInfo.response` da.
- Bildirishnoma: `ACT_LIS_RESPONSE` endi `RESULT_RECEIVED` va `RETURNED_BY_LIS` ikkalasida ham
  keladi. Bildirishnomaning o'zi qaysi biri ekanini aytmaydi — aktni o'qib
  (`GET /v1/acts/{id}`) `status` / `lisInfo` ni tekshiring.

> ⚠️ **LIS contract noaniq.** LIS callback body'da "qaytarildi" ni qanday belgilashini
> bilmaymiz. Backend keng heuristika ishlatadi (`status`/`state`/`decision` kalitlarida
> `return`/`reject`/... yoki `rejected`/`returned` boolean) va **default `RESULT_RECEIVED`**.
> Ya'ni heuristikaga tushmagan haqiqiy qaytarish hozir natija sifatida ko'rinadi (vrach
> baribir qayta yuborishi mumkin). LIS'ning
> haqiqiy signalini `Act.xlsx` yoki LIS jamoasidan olish kerak — keyin aniqlashtiramiz.

---

## 1-blok

### 1.1. LIS qaysi maydonlarni majburiy talab qiladi?

**Holat:** ⏳ kutiladi — `Act.xlsx` yoki teng ro'yxat kerak.

Hozir ham `markReady` / `send-to-lis` faqat status o'tishini tekshiradi, to'liqlikni emas.
`@NotNull` qo'shilmadi, chunki qaysi maydon majburiy ekanini bilmaymiz.

**Frontendda:** hozircha tekshiruv qo'ymang; spec kelgach backend + frontend birga qo'shamiz.

---

### 1.2. `SEND_FAILED` sababini aytmaydi (`lisInfo`)

**Holat:** ✅ bajarildi.

**Backendda:** `lisInfo` obyekti beshala `Act…DetailResponse` da paydo bo'ldi, **doim mavjud**:

```jsonc
"lisInfo": {
  "attempt": 2,                 // necha marta yuborilgan (0 = hech qachon)
  "sentDate": "2026-09-02T10:15:00",
  "actId": 78412,               // LIS tomonidagi akt id
  "lastError": "namuna yetarli emas",  // oxirgi yuborish xatosi YOKI qaytarish sababi; keyingi urinishda tozalanadi
  "response": { ... }           // LIS javobi to'liq — COMPLETED yoki RETURNED_BY_LIS dan keyin
}
```

**Frontendda:** «Laboratoriya» panelini `lisInfo` dan to'ldiring. `SEND_FAILED` /
`RETURNED_BY_LIS` da `lastError` ni ko'rsating, `COMPLETED` da `response` ni.

---

### 1.3. `sampleQtUnit` LIS ga ketmaydi

**Holat:** ✅ ACT154 uchun bajarildi · ⏳ ACT153 — LIS savoli ochiq.

**Backendda:** `ActLisPayloadMapper.toSelectionItem(Act154Detail)` endi `sampleWeight` yonida
`sampleQtUnit` ni yuboradi. Enum qiymati (`GRAM`, `KILOGRAM`, …) o'z nomi bilan ketadi —
LIS shu nomlarni qabul qiladimi, tasdiqlang.

**ACT153 — ataylab yuborilmaydi.** Sizning kuzatuvingiz to'g'ri: bo'sh qiymatli birlik yo'q
birlikdan chalg'ituvchiroq. 153 ning miqdori `sampleVolume` + `sampleVolumeUnit` (bu
`sampleQtUnit` dan boshqa juftlik) va hozir na hajm qiymati, na birlik LIS ga ketadi.
**LIS savoli:** water (153) uchun LIS qaysi juftlikni kutadi — `sampleQt`/`sampleQtUnit` yoki
hajm juftligimi? Javob kelgach 153 uchun ham yopamiz.

**Frontendda:** ACT154 tanlagichini saqlang. ACT153 uchun — LIS javobigacha kutamiz.

---

### 1.4. `packageType` / `manufacturer` / `manufactureDate` nesting

**Holat:** ⏳ kutiladi — LIS contract + schema migratsiyasi.

Hozir uchalasi ham akt darajasida bir marta ketadi. Eski klient izohiga ko'ra LIS ularni
har `selectionActItems[]` ichida kutishi mumkin. Agar shunday bo'lsa —
`Act154Detail` ga namuna darajasidagi ustunlar + migratsiya kerak.

**Frontendda:** hozircha akt darajasidagi bitta maydon. Per-item kerak bo'lsa — LIS tasdiqidan
keyin qo'shamiz, o'shanda formaga namuna ustunlari qo'shasiz.

---

## 2-blok — Ruxsatlar

### 2.1. Aktlar uchun ruxsat subyekti

**Holat:** ✅ enforcement qo'shildi.

**Backendda:** barcha act kontrollerlari endi `@PreAuthorize` bilan `ATTACH_ACT` ni tekshiradi
(ilgari faqat `isAuthenticated()`):

| Amal | Endpoint(lar) | Authority |
|---|---|---|
| `READ` | `GET /v1/acts/mine`, `/v1/acts/{id}`, `/pdf`, `/v1/cards/{id}/acts` | `PERMISSION_ATTACH_ACT_READ` |
| `VIEW_ALL` | `GET /v1/acts` (butun tashkilot ro'yxati) | `PERMISSION_ATTACH_ACT_VIEW_ALL` |
| `ASSIGN` | `POST /v1/cards/{id}/acts` | `PERMISSION_ATTACH_ACT_ASSIGN` |
| `UPDATE` | `PUT /v1/acts/{id}`, `PATCH .../ready`, `POST .../send-to-lis` | `PERMISSION_ATTACH_ACT_UPDATE` |
| `DELETE` | `DELETE /v1/acts/{id}` | `PERMISSION_ATTACH_ACT_DELETE` |

`POST /v1/acts/{id}/lis/callback` — `isAuthenticated()` da qoldi (LIS chaqiradi, `ATTACH_ACT` yo'q).

**READ ajratildi** (sizning savolingiz bo'yicha): `/v1/acts/mine` = `READ` (o'z aktlarim);
`/v1/acts` (tashkilotning barchasi) = yangi **`VIEW_ALL`** amali. Bu kartalardagi
`USER_INCOMINGS` ↔ `ATTACHED_CARDS` ajratishiga o'xshaydi — endi «faqat o'zinikini ko'rsin»
degan rolni tuzsa bo'ladi (`READ` beriladi, `VIEW_ALL` berilmaydi).

**Seed:**
- `VIEW_ALL` amali + `isemid_epidim_head` ga `ATTACH_ACT`+`VIEW_ALL` —
  `iam/20260902-1300-seed-act-view-all-action.xml`.
- `super_admin` / `admin` — `zzz-rbac-grants/20260828-1200` runAlways CROSS JOIN avtomatik oladi.

Hozirgi grantlar (`20260825-1200` + yangi):

| Rol | ATTACH_ACT amallari |
|---|---|
| `isemid_epidim_head` | `READ`, `VIEW_ALL`, `ASSIGN`, `UPDATE` |
| `isemid_epidemiologist` | `READ`, `UPDATE` |
| `isemid_assistant_epidemiologist` | `READ`, `UPDATE` |
| `isemid_super_admin` / `isemid_admin` | to'liq (admin — `DELETE`/`MANAGE` dan tashqari) |

⚠️ **Diqqat:** `ATTACH_ACT` grant'i yo'q rollar (masalan lab rollari, agar bo'lsa) endi
`/v1/acts/**` ga `403` oladi. Bu grant ro'yxatiga yana rol qo'shish kerak bo'lsa — ayting.

**Katalog tarixi:** `ATTACH_ACT` subyekti `20260825-1200-seed-act-permission-subject.xml` da:

| Rol | Amallar |
|---|---|
| `isemid_epidim_head` (bo'lim mudiri) | `READ`, `ASSIGN`, `UPDATE` |
| `isemid_epidemiologist` | `READ`, `UPDATE` |
| `isemid_assistant_epidemiologist` | `READ`, `UPDATE` |
| `isemid_super_admin` / `isemid_admin` | to'liq (admin — `DELETE`/`MANAGE` dan tashqari) |

**Frontendda:** rol sozlashda `ATTACH_ACT` subyektidan foydalaning, kartalarnikidan
(`USER_INCOMINGS` / `ATTACHED_CARDS`) emas. Tugmalar: `UPDATE` → to'ldirish/Tayyor/LIS ga
yuborish; `DELETE` → o'chirish; `VIEW_ALL` → tashkilot bo'yicha ro'yxat; `ASSIGN` → aktlarni
biriktirish. `403` kelsa — foydalanuvchida tegishli amal yo'q.

---

## 3-blok — Ma'lumot modeli

### 3.1. `subject` (dalolatnoma mavzusi)

> ⚠️ **2026-09-29 dan eskirgan** — erkin matnli `subject` o'chirildi, endi `subject` = subyekt bloki (5-blok).

**Holat:** ✅ bajarildi.

**Backendda:** `subject` (matn, 500 belgi) — bazaviy `act` jadvalida yangi ustun. Beshala
`Act…Request` va `Act…DetailResponse` da, hamda `ActTableResponse` da. `Institution.subjectType`
(`PHYSICAL`/`LEGAL_ENTITY`) — bu boshqa maydon, tegilmadi. `Act156.title` ham alohida qoldi.

Migratsiya: `zzz-card-act/20260902-1200-add-act-subject-and-base-number.xml`.

**Frontendda:** hech narsa — kod 2026-08-25 spec bo'yicha `subject` ni allaqachon yuboradi va
o'qiydi. Deploy'dan keyin o'sha kuniyoq ishlaydi.

---

### 3.2. Reyestr qatorida kontekst

**Holat:** ✅ bajarildi (`actNumber` ham) · ⏳ xodim F.I.Sh. — kelgusiga.

**Backendda:** `ActTableResponse`:

```jsonc
{
  "id": 42,
  "actType": "ACT153", "actTypeName": "...",
  "status": "IN_PROGRESS",
  "subject": "5-sonli maktab oshxonasi",   // yangi
  "actNumber": 128,                        // server generatsiya qiladi (= act id), so'rovda yuborilmaydi
  "cardId": 1001,                          // yangi
  "cardType": "CARD_161",                  // yangi
  "assignedById": 55,                      // yangi — biriktirgan (supervayzer) id
  "createdAt": "..."
}
```

`actNumber` bazaviy `act` jadvaliga ko'chirildi (ilgari `act153/154/223` sub-jadvallarida
edi) — shu bir migratsiyada, backfill bilan. `Act…DetailResponse` shakli o'zgarmadi.

**Deferred:** biriktirilgan xodim **F.I.Sh.** (`iam` join) — siz keyingi roundga qoldirdingiz.

**Frontendda:** `subject` bilan «bu qaysi ish?», `actNumber` bilan qidiruv, `cardId` bilan
kartaga link, `cardType` ni lokalizatsiya.

---

### 3.3. Akt turi bo'yicha filtr + `sortBy`

**Holat:** ✅ bajarildi.

**Backendda:**
- `GET /v1/acts` va `/v1/acts/mine` va `/v1/cards/{id}/acts` — yangi `actType` so'rov
  parametri (`ACT153` / `ACT154` / `ACT156` / `ACT223` / `ACT224`).
- `sortBy` — **rostdan ishlaydiganlar:** `id`, `actType`, `status`, `createdAt`,
  `updatedAt`. Boshqa qiymat jimgina e'tiborsiz qoladi (default tartib). Swagger
  tavsifiga yozib qo'yildi.

**Frontendda:** `?actType=ACT153` — hujjat turi filtri. `sortBy` da yuqoridagi 5 tadan
foydalaning.

---

### 3.4. To'rtta LIS lug'ati proksilanmagan

**Holat:** ⏳ kutiladi — LIS'da bu turlar qanday nomlanishini bilish kerak.

Hozir `/v1/lis-reference/**` da 7 endpoint, `reference-dictionaries` faqat
`type=CONDITIONS`. Yetishmaydi: `purpose`, `packageTypeInfo`, `conservationTypeInfo`,
`sampleTypeInfo`.

**Savol LIS ga:** bu turlar `reference-dictionaries?type=...` da qanday ataladi
(`PURPOSE`? `PACKAGE_TYPE`?). Va — **`purposeId` bo'sh, faqat nomi bo'lsa, LIS aktni
qabul qiladimi?** Javob kelgach `LisUrlFactory` + controller'ga bir necha qator qo'shamiz.

**Frontendda:** hozircha bu 4 maydon qo'lda kiritiladi, `id` bo'sh.

---

### 3.5. Tekshirilayotgan obyekt 2–3 marta saqlanadi

> ✅ **2026-09-29 da hal qilindi** — 156/224 takroriy uchligi o'chirildi (5-blok). `Act224.nameOfInstitution`/`addressOfInstitution` ham o'chirildi (qiymatlari `subject.name`/`subject.legalAddress` ga ko'chirildi).

**Holat:** ⏳ kutiladi — product qarori + bir tomonlama migratsiya.

Tasdiqlandi:
- `Act` bazasida `@Embedded Institution institution` bor (`tin`, `institutionName`,
  `institutionAddress`, `institutionLegalAddress`, `subjectType`).
- `Act156` **ustiga yana** tekis `tin` / `institution_name` / `institution_address`.
- `Act224` **uch marta**: inherited `institution` + tekis uchlik + `name_of_institution` /
  `address_of_institution`.
- ACT153/154/223 da takror **yo'q** — faqat `institution` embeddable.

**Savollar:** qaysi to'plam kanonik (bosma blanka qaysidan chiqadi)? 224 dagi uchinchi
juftlik kerakmi? — javoblardan keyin 156/224 ni ham `institution` embeddable'ga
o'tkazamiz, ortiqcha ustunlarni tashlaymiz.

**Frontendda:** hozirgidek — bir marta so'rang, ikkala joyga yozing.

---

### 3.6. Mayda savollar

| Savol | Javob |
|---|---|
| **156 — guruh raqami** | ⏳ `Act156GroupDetailRequest` da yo'q. Kichik migratsiya + maydon — so'rasangiz qo'shamiz. |
| **153 — `objectTypeId` / `objectCode`** | ✅ ikkalasi ham DTO va bazadan o'chirildi (LIS ga yuborilmas edi). Frontend yubormasin. |
| **224 — `fullNameOfParticipant`** | ✅ tasdiq: ishtirok etganlar ro'yxati, bitta qatorda. To'g'ri. |

---

## 4-blok — Aniqlashtirishlar

**4.1.** `GET /v1/acts/{id}/pdf` — ✅ to'g'ri tushungansiz. `GET /v1/acts/{id}` bilan bir
xil (faqat `audit` yo'q). Blankani allaqachon yuklangan aktdan chizing, ortiqcha
so'rovsiz.

**4.2.** `labId` ro'yxati — proksilangan `GET /v1/lis-reference/organizations`
(+ `/organizations/{id}/departments`) dan oling. `lis.sanepid.uz` ga to'g'ridan-to'g'ri
so'rov — har qanday yo'lga `401`. Gayd tuzatildi.

**4.3.** Qayta yuborish — sizning tushuningiz to'g'ri: `409` da `force: true` bilan
takrorlang. `409` → `LisBadRequestException`. Domen kodi (`duplicate.selection.act`) LIS
javobiga bog'liq — aniq matnni LIS test muhitidan tasdiqlang.

---

## 5-blok — 2026-09-29: subyekt bloki («Tashkilot turi») va `actNumber`

**Nima o'zgardi (API buziladi, beshala akt turi):**

| Oldin | Endi |
|---|---|
| `institution: { subjectType, tin (number), institutionName, institutionAddress, institutionLegalAddress }` | `subject: { type, tin (string, 9 raqam), name, legalAddress, actualAddress }` |
| `subject: "erkin matn"` | **o'chirildi** — reyestr yorlig'i `subject.label` (javobda) |
| ACT156/224 tekis `tin` / `institutionName` / `institutionAddress` | **o'chirildi** — `subject` ichida |
| `actNumber` so'rovda | **o'chirildi** — server beradi (`= id`, LIS'ga ketadigan `senderActNumber` bilan bir xil) |
| ACT153: `goal`, `lisOrganizationId`, `laboratoryAddress`, namunada `sampleQtUnit` | so'rovdan **o'chirildi** (formada yo'q). LIS `goal` ← `purpose.samplingPurposeUz` |

**Forma → maydon:**

| «Tashkilot turi» | `subject.type` | Maydonlar |
|---|---|---|
| Tashkilot | `LEGAL_ENTITY` | STIR → `tin`, Muassasa nomi → `name`, Obyektning manzili → `legalAddress`, Muassasaning amaldagi manzili → `actualAddress` |
| Geografik joylashuv | `GEOGRAPHIC_POINT` | Amaldagi manzili → `actualAddress` |
| Jismoniy shaxs | `PHYSICAL_PERSON` | Amaldagi manzili → `actualAddress` |

`LEGAL_ENTITY` bo'lmasa `tin`/`name`/`legalAddress` server tomonidan tozalanadi.

**Maqsad («Tekshirish yoki namuna olish maqsadi», ACT153/154/223):** `purpose: {purposeId, samplingPurposeUz, ...}`
o'rniga so'rovda faqat **`purposeCode`** — `ref_catalog` dagi `type=PURPOSE` kodi (ro'yxat:
`GET` catalog lookup, `type=PURPOSE`). Noma'lum kod → `400 error.act.purpose-not-found`. Javobda
`purpose: { code, name }` (nom — joriy tilda; legacy actlarda `code=null`, `name` = saqlangan uz nomi).
`goal` so'rov va javobdan **o'chirildi** (154/223 ham); LIS `goal` ← maqsad nomi. Migratsiya:
`zzz-card-act/20260929-1400-act-purpose-code.xml`.

**Katalog seed'i:** `reference/20260929-1800-seed-act-purpose-sampling-basis-catalogs.xml`.
`PURPOSE` — LIS'ning `reference-dictionaries?type=PURPOSE` ro'yxati bilan bir xil, **kod = LIS purpose id**
(`1,2,3,4,5,6,7,8,9,44,45,46,47,115`), chunki LIS'ga maqsad id'si sifatida kod raqami ketadi. LIS'da yangi
maqsad paydo bo'lsa — shu id bilan yangi qator qo'shiladi. `SAMPLING_BASIS` — LIS'da bunday lug'at yo'q
(LIS faqat uz nomini `document` matni sifatida oladi), kodlar tizimning o'ziniki: `PLANNED`, `UNPLANNED`,
`EPID_INDICATION`, `APPLICATION`, `CONTRACT`, `AUTHORITY_REQUEST`, `NORMATIVE_DOC`, `MONITORING`, `OTHER`
(⚠️ ro'yxat taklif — product tasdiqlashi kerak).

**Namuna olish uchun asos (faqat ACT153):** erkin matnli `samplingDocuments` o'rniga so'rovda
**`samplingBasisCode`** — `ref_catalog` dagi `type=SAMPLING_BASIS` kodi (select ro'yxati: `GET` catalog
lookup, `type=SAMPLING_BASIS`). Noma'lum kod → `400 error.act.sampling-basis-not-found`. Javobda
`samplingDocuments` o'rniga `samplingBasis: { code, name }` (legacy actlarda `code=null`, `name` = saqlangan
matn). LIS `document` ← asos nomi (uz). Migratsiya: `zzz-card-act/20260929-1500-act153-sampling-basis-code.xml`.

**Namuna olish uchun asos (ACT154):** formada select + matn maydoni. So'rovda **`samplingBasisCode`** (xuddi shu
`type=SAMPLING_BASIS` katalogi, 153 bilan umumiy) + **`samplingBasisText`** (max 500, masalan GOST raqami).
Javobda `samplingBasis: { code, name }` + `samplingBasisText` (legacy actlarda `code=null`, `name` = eski `goal`
satri "GOST …"). LIS `document` ← "asos nomi (uz) + matn". Migratsiya: `zzz-card-act/20260929-1600-act154-sampling-basis.xml`.

**Namuna olish uchun asos (ACT223):** formada faqat select (ACT153 kabi). Erkin matnli
`supportingDocumentsForSampling` o'rniga so'rovda **`samplingBasisCode`** (`type=SAMPLING_BASIS`, 153/154 bilan umumiy).
Javobda `supportingDocumentsForSampling` o'rniga `samplingBasis: { code, name }` (legacy actlarda `code=null`,
`name` = saqlangan matn). LIS `document` ← asos nomi (uz). Migratsiya: `zzz-card-act/20260929-1700-act223-sampling-basis-code.xml`.

**ACT156 so'rovidan olib tashlandi:** `lisOrganizationId`, `laboratoryAddress` — 156 LIS'ga bormaydi. Javobda legacy
ma'lumot uchun qoldi (faqat o'qish).

**ACT224 dan olib tashlandi:** `nameOfInstitution`, `addressOfInstitution` — so'rovdan ham, javobdan ham. Ular
`subject` blokini takrorlardi; mavjud qiymatlar `subject.name` / `subject.legalAddress` ga ko'chirildi (bazada
qiymat bo'lsa, o'sha qoladi), ustunlar o'chirildi. Migratsiya: `zzz-card-act/20260929-1800-drop-act224-institution-duplicate.xml`.

**ACT223 so'rovidan olib tashlandi (formada yo'q):** `lisOrganizationId`, `laboratoryAddress`. Javobda legacy
ma'lumot uchun qoldi (faqat o'qish).

**ACT154 so'rovidan olib tashlandi (formada yo'q):** `title`, `documentConfirmSampling`, `lisOrganizationId`,
`laboratoryAddress`, namunadagi `sampleName`. Javoblarda legacy ma'lumot uchun qoldi (faqat o'qish).

**Validatsiya:** saqlashda faqat format (`tin` — 9 raqam). To'liqlik (`type` majburiy; `LEGAL_ENTITY` → `tin` + `name`;
qolganlari → `actualAddress`) — **READY ga o'tkazishda / LIS'ga yuborishda**, aks holda `400`
`error.act.subject-incomplete`. Qoralama istalgancha saqlanadi.

**Javoblar:** `ActDetailResponse.subject` va `ActTableResponse.subject` — `{ type, tin, name, legalAddress, actualAddress, label }`.

**Migratsiyalar:** `zzz-card-act/20260929-1200-backfill-act-number-from-id.xml`, `zzz-card-act/20260929-1300-act-subject.xml`
(`act.tin` → VARCHAR, 156/224 uchligi bazaga ko'chirilib o'chiriladi, `act.subject` ustuni o'chiriladi).

## Qisqacha — deploy'dan keyin frontendda (`yarn api:types`)

1. **`lisInfo`** paneli (1.2) — `SEND_FAILED`/`RETURNED_BY_LIS` da `lastError`,
   `COMPLETED` da `response`. Heuristika aniqlanmagunча `COMPLETED` da ham `response` ni ko'rsating.
2. **`RETURNED_BY_LIS`** — 7-status: palitra, tahrirlash mumkin / o'chirish yo'q,
   qayta yuborishда avto `force: true`.
3. **`actType`** filtri (3.3); `sortBy` ga `actType`/`updatedAt`.
4. Reyestr qatori: **`subject` / `actNumber` / `cardId` / `cardType`** (3.1, 3.2).
5. Rol sozlash: **`ATTACH_ACT`** (`READ`/`VIEW_ALL`/`ASSIGN`/`UPDATE`/`DELETE`) (2.1).
6. `labId` ni **`/v1/lis-reference/organizations`** dan (4.2).

## Bizdan kutiladigan javoblar

| # | Nima | Kimdan |
|---|---|---|
| 1 | **`isemid-v2` deploy** (yoki host manzili) | backend/DevOps |
| 2 | LIS callback'da «qaytarildi» signalining aniq shakli | LIS |
| 3 | `Act.xlsx` — majburiy maydonlar (1.1) | LIS |
| 4 | ACT153 `sampleQt` juftligi + `sampleQtUnit` enum nomlari (1.3) | LIS |
| 5 | `packageType` / `manufacturer` nesting (1.4) | LIS |
| 6 | 4 ta LIS lug'ati turlari nomi + bo'sh `id` bilan qabul (3.4) | LIS |
| 7 | Kanonik institution to'plami (3.5) | product / LIS |
