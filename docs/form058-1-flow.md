# Form058-1 — Uy/yovvoyi hayvon tishlashi (qutirishga shubha) tezkor xabarnomasi

Bu hujjat `form058-1` featurening ishlash oqimini (flow) tasvirlaydi. Modul `form058`
bilan bir xil arxitektura va holat mashinasi (state machine) asosida qurilgan.

- Asosiy paket: `src/main/java/uz/uzinfocom/isemid/features/form0581/`
- API bazaviy manzili: `/v1/form058-1`
- O'xshash modul (namuna sifatida ishlatilgan): `src/main/java/uz/uzinfocom/isemid/features/form058/`

---

## 1. Ma'lumotlar modeli

### 1.1. Asosiy entity — `Form0581` (jadval: `form058_1`)

Forma-specific maydonlar (jarohat, hayvon, kasalxonaga yotqizish ma'lumotlari):


| Guruh | Maydonlar |
|---|---|
| Tashxis | `mkb10Code`, `mkb10Name`, `mkb10UsageLimit`, `injuryLocalization`, `medRecordId` |
| Jarohat | `injuryDateTime`, `visitDateTime`, `injuryLocation` — `@Embeddable InjuryLocation` (quyida §1.5) |
| Hayvon | `animalInfo` — `@Embeddable AnimalInfo` (kategoriya kodi spravochnikdan, quyida §1.5) |
| Boshqa jabrlanganlar | `otherPeopleBitten` (boolean), `bittenPersons` (ro'yxat, quyida) |
| Kasalxona | `hospitalizedAt`, `hospitalPlaceId` |
| Xabar | `notifierFullName`, `receiverFullName`, `initialReportDateTime`, `antirabicAidInfo` |
| Marshrutlash | `senderOrganizationId` (yuboruvchi SES), `receiverOrganizationId` (qabul qiluvchi SES) |
| Holat | `status` (`FormStatus`, form058 bilan bir xil enum), `source` (DMED/LOCAL/QR) |
| Bekor qilish | `cancelReason`, `canceledBy` |
| Yakuniy diagnoz tasdiqlash | `diagnosisApproval` — `@Embeddable DiagnosisApproval` (quyida §1.5) |

### 1.2. Bemor ma'lumotlari — qayta ishlatiladi, dublikat qilinmaydi

Bemorga tegishli deyarli barcha maydonlar (F.I.Sh, jinsi, yoshi, telefon, PINFL/pasport,
doimiy/hozirgi yashash joyi, ish/o'qish joyi, bemor toifasi, oilaviy holati) **`form058` bilan
bir xil, mavjud `Patient` entitysi orqali** olinadi — `Form0581.patient` (`@ManyToOne`):

- `Patient.identifiers` — PINFL/pasport seriya-raqami
- `Patient.addresses` (`type=PERMANENT`/`TEMPORARY`) — doimiy/hozirgi yashash joyi
- `Patient.affiliations` — ish/o'qish joyi (bir nechta bo'lishi mumkin)
- `Patient.categoryCode`, `Patient.maritalStatusCode` — bemor toifasi, oilaviy holat

### 1.3. Yangi bola-entitylar (faqat form058-1'ga xos)

- **`Form0581AnimalOwner`** (jadval `fm0581_animal_owner`, bitta-birga, ixtiyoriy) —
  tishlagan hayvon egasining F.I.Sh va manzili. Faqat egasi ma'lum bo'lgandagina to'ldiriladi.
- **`Form0581BittenPerson`** (jadval `fm0581_bitten_person`, bitta-ko'p, `otherPeopleBitten=true`
  bo'lsa) — bemordan tashqari shu hayvon tomonidan jarohatlangan boshqa shaxslar ro'yxati
  (F.I.Sh + manzil).

### 1.4. Kod (spravochnik) maydonlar

`animalCategoryCode` va shunga o'xshash "select" maydonlar **Java enum sifatida emas, oddiy
kod (string) ustuni** sifatida saqlanadi — xuddi `Organization.stateCode`/`Patient.categoryCode`
kabi. Qiymatlar tashqi spravochnik orqali kiritiladi va kod bo'yicha aniqlanadi, backend FK
orqali cheklamaydi.

### 1.5. `@Embeddable` guruhlar

`Form0581` entitysida faqat **haqiqatan ham bitta izchil kontseptni ifodalaydigan**
maydon guruhlari `@Embeddable` qilib ajratilgan (qolgan barcha maydonlar — masalan
`mkb10Code`, `notifierFullName`, `cancelReason` — oddiy tekis (flat) ustunlar bo'lib
qoladi, ular guruhlash uchun asos bermaydi):

| Embeddable | Maydonlar | Qayerda ishlatiladi |
|---|---|---|
| `DiagnosisApproval` | `finalMkb10Code`, `finalMkb10Name`, `approvedFullName`, `approvedOrgUuid`, `notApproveComment` | `Form0581.diagnosisApproval` — `approve-diagnosis`/`not-approve` natijasi (§3.2 ga qarang) |
| `InjuryLocation` | `stateCode`, `cityCode`, `address` | `Form0581.injuryLocation` — jarohat olingan manzil (DE.58-61) |
| `AnimalInfo` | `categoryCode`, `color`, `type`, `breed` | `Form0581.animalInfo` — tishlagan hayvon tavsifi (DE.73, DE.78-80) |

Barcha holatlarda ustun nomlari o'zgarmagan (masalan `injury_state_code`,
`animal_category_code`) — embeddable'ning har bir maydoni `@Column(name=...)` orqali
aynan avvalgi ustun nomiga moslashtirilgan, shu sababli bu refaktoring uchun **yangi
migratsiya kerak bo'lmadi** (`DiagnosisApproval`ning yangi maydonlaridan tashqari,
§3.2 ga qarang). `Form0581Request`/`Form0581Response` DTOlari tekis (flat) holicha
qoladi — MapStruct nested `@Mapping(target = "injuryLocation.stateCode", source =
"injuryStateCode")` uslubidagi yo'llar orqali entitydagi embeddable bilan DTOdagi
tekis maydonlarni bog'laydi.

Bu yondashuv **faqat Form058-1'ga xos** — `Form058` entitysi o'zining tekis
maydonlarini saqlab qoladi, tegilmagan.

---

## 2. Karta (Card) biriktirish

`Card` entitysi (`features/card/models/Card.java`) ikkita forma turiga ham FK orqali bog'lana
oladi:

```
Card.form058   -> form058_id     (Form058 uchun)
Card.form0581  -> form058_1_id   (Form0581 uchun, YANGI ustun)
```

Bitta kartada bu ikkitadan faqat BITTASI to'ldirilgan bo'ladi.

**Form058-1'ga faqat quyidagi karta turlari biriktirilishi mumkin** (`Form0581Policy` orqali
cheklangan, `CardType` enumning o'zi o'zgartirilmagan):

- `CARD174`
- `CARD175`
- `CARD205`

(`CARD161` va `CARD_TUBE` — ruxsat etilmagan, `POST /assign-card` 400 xato qaytaradi.)

Karta yaratish/yangilash mexanizmi (`CardFactory`, `CardBuildService`, `UpdateCardStrategy`) —
form058 bilan **bir xil, umumiy** kod; faqat qaysi FK to'ldirilishi farq qiladi.

---

## 3. Holat (status) oqimi — `FormStatus`

`Form0581.status` maydoni `form058` bilan **bir xil** `FormStatus` enumidan foydalanadi:

```
NEW -> SENT -> CARD_LINKED -> APPROVED_PENDING -> APPROVED
                    \-> CARD_REJECTED
        \-> CANCELED (istalgan bosqichda, agar tahrirlash taqiqlanmagan bo'lsa)
```

### 3.1. To'liq oqim, bosqichma-bosqich

1. **Yaratish** — `POST /v1/form058-1`
   Forma yaratiladi, `status = SENT`, `source` avtomatik aniqlanadi (QR-rol → `QR`,
   `X-Source` header → `LOCAL`, aks holda → `DMED`).

2. **Karta biriktirish** — `POST /v1/form058-1/assign-card`
   `Form0581Facade.assignCard(...)` chaqiriladi:
   - `Form0581Policy.assertCardAssignable` — faqat CARD174/175/205 ga ruxsat.
   - Belgilangan xodimlarga (`assignUserIds`) karta(lar) yaratiladi/biriktiriladi
     (`CardBuildService.buildCards(...)`, `Card.status = NEW`).
   - **`Form0581.status = CARD_LINKED`** ga o'tadi.
   - DMED'ga xabar yuborish qismi tayyor, lekin hozircha **o'chirilgan** (§6 ga qarang).

3. **Xodim ishni boshlaydi** — `PATCH /v1/card/{cardId}/start`
   (`CardService.startWorkingOnCard`, faqat kartaga biriktirilgan xodim chaqira oladi):
   - `Card.status: NEW -> IN_PROGRESS`
   - **Bog'liq forma (`Form0581` yoki `Form058`) avtomatik `APPROVED_PENDING`ga o'tadi.**

4. **Xodim ishni tugatadi** — `PATCH /v1/card/{cardId}/complete`
   `Card.status -> COMPLETED` (forma statusiga bevosita ta'sir qilmaydi — u
   allaqachon `APPROVED_PENDING`).

5. **Bo'lim mudiri kartani ko'rib chiqadi**:
   - **Tasdiqlasa** — `PATCH /v1/card/{cardId}/supervisor/approve` → `Card.status = APPROVED`.
   - **Rad etsa** — `PUT /v1/card/{cardId}/supervisor/reject` → `Card.status = REJECTED`,
     va **`Form0581.status = CARD_REJECTED`** ga o'tadi.

6. **Forma tasdiqlanadi** — ikki xil yo'l bilan (ikkalasi ham `CARD_LINKED` yoki
   `APPROVED_PENDING` holatida bo'lishini talab qiladi):
   - **Oddiy tasdiqlash** — `PATCH /v1/form058-1/{id}/approve` → boshqa maydonlarsiz,
     to'g'ridan-to'g'ri **`Form0581.status = APPROVED`**.
   - **Diagnozni tasdiqlash** — `PUT /v1/form058-1/{id}/approve-diagnosis`
     (`Form0581ApproveDiagnosisRequest{finalMkb10Code, finalMkb10Name, approvedFullName,
     approvedOrgId}`) → `diagnosisApproval.finalMkb10Code/Name/approvedFullName` to'ldiriladi,
     `approvedOrgId` bo'yicha tashkilot topiladi (yo'q bo'lsa MIS'dan olib saqlanadi),
     **`Form0581.status = APPROVED`**.

### 3.2. Diagnozni rad etish

`PUT /v1/form058-1/{id}/not-approve` (`Form0581NotApproveRequest{comment}`) →
`diagnosisApproval.notApproveComment` to'ldiriladi, **`Form0581.status = NOT_APPROVED`**.

### 3.3. Bekor qilish

Istalgan bosqichda (agar forma `EDIT_FORBIDDEN` ro'yxatida bo'lmasa) —
`PATCH /v1/form058-1/{id}/cancel` (`Form0581CancelRequest{cancelReason}`) orqali
`Form0581Facade.cancel(...)` chaqiriladi → `status = CANCELED`, `cancelReason`/`canceledBy`
saqlanadi (DMED'ga xabar yuborish hozircha o'chirilgan, §6 ga qarang).

### 3.4. Tahrirlash taqiqlangan holatlar

`Form0581Policy.EDIT_FORBIDDEN` — quyidagi holatlarda forma (`PUT /{id}`) tahrirlanmaydi:
`CARD_LINKED`, `CARD_REJECTED`, `APPROVED_PENDING`, `APPROVED`, `CANCELED`.
(Faqat `NEW`/`SENT` bosqichida, karta biriktirilmagan paytda tahrirlash mumkin.)

---

## 4. API endpointlari (`Form0581Controller`, `/v1/form058-1`)

> **Eslatma**: `Form0581Response`/`Form0581DetailsResponse` endi `cards` maydonini
> **qaytarmaydi**. Formaga biriktirilgan kartalarni olish uchun alohida
> `GET /{id}/cards` endpointidan foydalaning.

| Method | Path | Tavsif |
|---|---|---|
| POST | `/` | Yangi forma yaratish |
| GET | `/all` | Yuboruvchi tashkilot nuqtai nazaridan ro'yxat |
| GET | `/received/all` | Qabul qiluvchi tashkilot nuqtai nazaridan ro'yxat |
| GET | `/by-nnuzb` | Bemor PINFL bo'yicha qidirish |
| GET | `/{id}` | Bitta formani olish |
| PUT | `/{id}` | Yangilash (faqat tahrirlash ruxsat etilgan holatda) |
| POST | `/assign-card` | Karta(lar) biriktirish (CARD174/175/205) |
| PATCH | `/{formId}/change-received-org/{orgId}` | Qabul qiluvchi tashkilotni almashtirish |
| PATCH | `/{formId}/cancel` | Bekor qilish |
| PATCH | `/{formId}/approve` | Oddiy tasdiqlash |
| PUT | `/{formId}/not-approve` | Diagnozni rad etish |
| PUT | `/{formId}/approve-diagnosis` | Yakuniy diagnozni tasdiqlash |
| DELETE | `/{formId}` | O'chirish (faqat `ROLE_isemid_super_admin`) |
| GET | `/confirmation-stats` | MKB-10 bo'yicha tasdiqlangan/tasdiqlanmagan statistika |
| GET | `/by-card/{cardId}` | Karta ID orqali bog'liq formani topish |
| GET | `/{id}/cards` | Forma ID orqali unga biriktirilgan barcha kartalarni olish |

Karta bilan bog'liq umumiy amallar (`/v1/card/...`): `accept`, **`start`** (yangi),
`complete`, `supervisor/approve`, `supervisor/reject` — `CardController` orqali, forma
turidan qat'i nazar bir xil.

---

## 5. Kirish huquqi (access scope)

`Form0581AccessSpecification` `form058` bilan bir xil umumiy
`OrganizationAccessScopeResolver`/`ResolvedAccessScope` mexanizmini ishlatadi:

- **REPUBLICAN** SES — cheklovsiz.
- **REGIONAL** (va Toshkent shahar boshqarmasi, URBAN sifatida saqlangan) — o'z
  viloyati/shahridagi barcha tashkilotlar, REPUBLICAN darajadagilar bundan mustasno.
- Qolgan barcha tashkilotlar — faqat o'ziga tegishli yozuvlar.

`Form0581` `Form058` bilan **to'liq bir xil**: ikkalasida ham `senderOrganizationId` va
`receiverOrganizationId` alohida ustunlar sifatida saqlanadi (JPA relation emas, oddiy
`Long` ID), va `Form0581Request`/`Form058Request` ikkalasida ham ikkalasi UUID sifatida
majburiy maydon (`uuidToLongId` orqali ichki ID'ga aylantiriladi).

---

## 6. DMED integratsiyasi — HOZIRCHA O'CHIRILGAN

**Mahsulot qarori**: form058-1 uchun DMED integratsiyasi hozircha ishlamaydi.
Barcha infratuzilma tayyor turibdi, faqat haqiqiy tarmoq chaqiruvi bloklangan:

- `Form0581Facade.DMED_INTEGRATION_ENABLED = false` — yagona joy, shu flag orqali
  chaqiruv o'chirilgan/yoqiladi.
- `DmedForm0581Client`, `DmedClientConfig.dmedForm0581Client(...)` bean'i, va
  `isemid.integration.paths.dmed-form0581-change-push-api-path=/api/integrations/v1/058-1/{formId}`
  properties — barchasi allaqachon yozilgan va ishga tayyor.

**Keyinchalik yoqish uchun**: `Form0581Facade`dagi `DMED_INTEGRATION_ENABLED`ni
`true`ga o'zgartirish kifoya — boshqa hech qanday kod o'zgarishi shart emas.
Yoqilgandan so'ng oqim `form058`nikiga o'xshab ishlaydi: `assignCard`/`cancel`
amallarida, agar forma `source == "DMED"` bo'lsa, `DmedForm0581Client` orqali DMED
tizimiga `Form058ChangeEvent` (uuid, status, tashkilot nomi, o'zgartirgan xodim)
yuboriladi.

> **Eslatma**: yo'l (`/058-1/{formId}`) hozircha taxminiy — DMED tomonida haqiqiy
> endpoint tasdiqlanguncha o'zgartirish talab qilinishi mumkin.

---

## 7. Form058 bilan tafovutlar

Endi funksionallik jihatidan **form0581 va form058 to'liq bir xil** — `not-approve`,
`approve-diagnosis`, `confirmation-stats` ham qo'shildi. Yagona farq — **modellashtirish
uslubi**:

- `Form058` — `finalMkb10Code`/`finalMkb10Name`/`approvedFullName`/`approvedOrgUuid`/
  `notApproveComment` alohida tekis (flat) maydonlar sifatida saqlanadi.
- `Form0581` — xuddi shu maydonlar guruhi `DiagnosisApproval` `@Embeddable` ob'ekti
  ichida (`Form0581.diagnosisApproval`) saqlanadi (§1.5 ga qarang).

Bu — faqat **Form0581'ga xos** ichki modellashtirish farqi; API request/response
darajasida (`Form0581Request`/`Form0581Response` maydonlari tekis holicha qoladi) hech
qanday farq sezilmaydi, `Form058`ning o'ziga esa **hech narsa tegilmagan**.
