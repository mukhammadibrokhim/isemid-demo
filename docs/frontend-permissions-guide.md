# Frontend uchun ruxsatlar (permission) qo'llanmasi — eski va yangi model

Maqsad: frontend menyu, route guard va tugmalarni **yangi** nomlarga o'tkazishi uchun hammasi bir joyda:
nima bo'lgan, nima bo'ldi, qaysi rol nimani oladi. Batafsil asoslash:
[permission-naming-mapping.md](permission-naming-mapping.md). Bu hujjatdagi rol jadvali **lokal bazadan
(2026-11-20)** olingan; jonli bazada qo'lda o'zgartirilgan bo'lishi mumkin — haqiqiy manba `GET /v1/user/me`.

---

## 1. Authority qanday hosil bo'ladi

```
PERMISSION_<SUBJECT>_<ACTION>        masalan  PERMISSION_FORM058_CREATE
```

- **Subject** = resurs (FORM058, CARD, REPORT…), **Action** = harakat (CREATE, READ_SENT…).
- Rolda `MANAGE` bo'lsa, backend uni shu subject'ning **barcha** action'lariga kengaytiradi.
- Faol bo'lmagan (`active=false` yoki o'chirilgan) subject/action authority **bermaydi**.
- Foydalanuvchining ruxsatlari `GET /v1/user/me` javobida keladi (tashkilot header'i berilsa, shu
  tashkilotdagi rollar bo'yicha).
- Ruxsat — "bu **turdagi** amalni qila oladimi". "Aynan **shu yozuv** uniki-mi" (yuboruvchi/qabul qiluvchi
  tashkilot, biriktirilgan xodim) backend ichida alohida tekshiriladi, frontend buni hal qilmaydi.

---

## 2. Eski → yangi (subject'lar)

Eski subject'lar **o'chirilgan** (soft-delete): ular endi authority **bermaydi**. Menyu/guard'da ularni
ishlatmang.

| Eski subject | Yangi | Izoh |
|---|---|---|
| `FORM058_SENDER` | `FORM058` | `CREATE`, `UPDATE`, `READ_SENT`… (yuboruvchi tomon) |
| `FORM058_RECEIVER` | `FORM058` | `ACCEPT`, `REJECT`, `READ_RECEIVED`… (qabul qiluvchi tomon) |
| `FORM058_COMPLETE_BY_SUPERVISOR` | `FORM058` + `APPROVE` | |
| `RECEIVED` | `FORM058` + `READ_RECEIVED` | |
| `FORM058_CHANGE_ORGANIZATION` | `FORM058` + `CHANGE_ORG` | |
| `FORM058_ATTACHED_CARDS` | `CARD` + `ASSIGN` | |
| `FORM0581`, `FORM129` | o'zgarmadi | 058 bilan bir xil model (yuboruvchi/qabul qiluvchi action'lar) |
| `ATTACHED_CARDS`, `USER_INCOMINGS`, `CARD_FULL_INFO` | `CARD` | `READ`, `VIEW_ALL` … |
| `ATTACH_ACT`, `ACTS` | `ACT` | |
| `REPORTS`, `FORM1`, `FORM12`, `GRAPH1`, `MAP_GRAPHIC` | `REPORT` | ko'rish + `EXPORT` |
| `REPORTS` (CREATE/UPDATE/DELETE) | `REPORT_ENTRY` | qo'lda kiritiladigan hisobotlar |
| `ANALYTICAL_REPORT` | `ANALYTIC_REPORT` | |
| `STAFF`, `USER` | `EMPLOYEES` | xodimlar |
| `ORGANIZATIONS` | `ORGANIZATION` | |
| `ROLES`, `PERMISSIONS` | `ROLE`, `PERMISSION` | |
| `MKB_LIST` | `ICD10` | |
| `COUNTRIES`, `REFERENCE_COUNTRY_*` | `COUNTRY` | |
| `STATES`, `REFERENCE_REGION_*` | `REGION` | |
| `CITIES`, `STREETS`, `RESIDENCES`, `REFERENCE_DISTRICT_*` | `DISTRICT` | |
| `NEIGHBORHOODS`, `REFERENCE_NEIGHBORHOOD_*` | `NEIGHBORHOOD` | |
| `CATALOGS`, `FORM_CATEGORIES`, `SICK_PATIENT_CATEGORIES`, `DISEASE_PLACES`, `POPULATION_TYPES` | `CATALOG` | |
| *(yo'q edi)* | `POPULATION` | yangi: aholi soni ma'lumotnomasi |
| `ALL`, `MANUAL_REPORT`, `SPEECH_TO_TEXT` | o'zgarmadi | |

## 3. Eski → yangi (action'lar)

| Eski | Yangi |
|---|---|
| `CARD_ATTACH` | `ASSIGN` (subject `CARD`) |
| `ORG_CHANGE` | `CHANGE_ORG` |
| `FORM_CANCEL` | `CANCEL` (yuboruvchi) / `REJECT` (qabul qiluvchi) |
| `IMPORT` | olib tashlandi |
| `READ`, `CREATE`, `UPDATE`, `DELETE`, `MANAGE`, `EXPORT`, `APPROVE`, `ASSIGN`, `VIEW_ALL` | o'zgarmadi |
| — | yangi: `ACCEPT`, `REJECT`, `COMPLETE`, `RETURN`, `SYNC`, `READ_SENT`, `READ_RECEIVED`, `SUBMIT`, `CONFIRM` |

Hozir faol action'lar (20 ta): `ACCEPT APPROVE ASSIGN CANCEL CHANGE_ORG COMPLETE CONFIRM CREATE DELETE
EXPORT MANAGE READ READ_RECEIVED READ_SENT REJECT RETURN SUBMIT SYNC UPDATE VIEW_ALL`.

Hozir faol subject'lar (22 ta): `ACT ALL ANALYTIC_REPORT CARD CATALOG COUNTRY DISTRICT EMPLOYEES FORM058
FORM0581 FORM129 ICD10 MANUAL_REPORT NEIGHBORHOOD ORGANIZATION PERMISSION POPULATION REGION REPORT
REPORT_ENTRY ROLE SPEECH_TO_TEXT`.

---

## 4. Qaysi subject'da qaysi action nima qiladi

### 4.1 FORM058, FORM0581, FORM129 — yuboruvchi va qabul qiluvchi alohida

| Tomon | Action | Nima qiladi |
|---|---|---|
| Yuboruvchi | `CREATE` | yangi forma yaratish/yuborish (`POST`) |
| | `UPDATE` | tahrirlash (129 da yo'q) |
| | `DELETE` | o'chirish (129 da yo'q — u faqat admin) |
| | `CANCEL` | yuborilgan (`SENT`) formani qaytarib olish (129 da yo'q) |
| | `APPROVE` | yuboruvchining yakuniy tasdig'i (qabuldan / karta biriktirilgandan keyin; 129 da yo'q) |
| | `READ_SENT` | **chiquvchi** ro'yxat (`direction=OUTGOING`), yuborgan formasini ochish/PDF |
| Qabul qiluvchi | `ACCEPT` | qabul qilish |
| | `REJECT` | rad etish (058/058-1 da `cancel` endpoint'i, 129 da `reject`) |
| | `READ_RECEIVED` | **kiruvchi** ro'yxat (`direction=INCOMING`), qabul qilganini ochish/PDF; `/affiliated` ham shu |
| | `CHANGE_ORG` | qabul qiluvchi tashkilotni almashtirish (faqat 058) |
| ikkalasi | `READ_SENT` + `READ_RECEIVED` | `direction=ALL` ro'yxati uchun ikkalasi ham kerak |
| — | `EXPORT` | Excel eksport (+ tegishli `READ_*`) |

Statusga bog'liq qoida (backendda, frontend ruxsatni bunga qarab emas, **forma statusiga** qarab
tugmani ko'rsatsin): `CANCEL` faqat `SENT` da; `UPDATE` `APPROVED`/`CANCELED` gacha; `DELETE` `SENT`,
`ACCEPTED` da; `APPROVE` `ACCEPTED`/`CARD_LINKED` da.

### 4.2 CARD (epidemiologik tekshiruv kartalari)

| Action | Endpoint / ma'no | Kim |
|---|---|---|
| `READ` | "mening kartalarim" (`/cards/mine`), karta ochish, PDF | biriktirilgan xodim |
| `VIEW_ALL` | barcha kartalar (`GET /cards`) | mudir, admin |
| `ASSIGN` | formaga karta yaratish/xodim biriktirish, qayta biriktirish | mudir |
| `UPDATE` | kartani tahrirlash | biriktirilgan xodim |
| `ACCEPT`, `REJECT` | kartani qabul qilish / rad etish | biriktirilgan xodim |
| `COMPLETE` | kartani yakunlash | biriktirilgan xodim |
| `APPROVE` | nazoratchi tasdig'i | mudir |
| `RETURN` | nazoratchi qayta ishlashga qaytarishi | mudir |
| `DELETE` | kartani o'chirish | mudir |

Karta "to'liq ma'lumot" bloki alohida ruxsat emas — `CARD` `READ` bilan ko'rinadi.

### 4.3 ACT (laboratoriya/dalolatnomalar)

`READ` (mening aktlarim, ochish, PDF) · `VIEW_ALL` (hamma aktlar) · `ASSIGN` (kartadan akt yaratish) ·
`UPDATE` (tahrir, tayyor, LIS'ga yuborish, yopish) · `DELETE`.

### 4.4 Hisobotlar — 3 guruh

| Subject | Ma'no | Action'lar |
|---|---|---|
| `REPORT` | statistik hisobotlarni **ko'rish** (1, 4, 6, 8–13, 28.1, 28.2, statistika, prognoz, xarita, havo) | `READ`, `EXPORT` |
| `REPORT_ENTRY` | qo'lda kiritiladigan hisobotlar (2, 31, 32, 7) | `READ`, `CREATE`, `UPDATE`, `DELETE` |
| `ANALYTIC_REPORT` | o'z tahliliy hisobotini tuzish | `READ`, `CREATE`, `UPDATE`, `DELETE`, `EXPORT` |

`EXPORT` `READ` dan alohida: "ko'ra oladi, yuklay olmaydi" mumkin.

### 4.5 Ma'lumotnomalar va boshqalar

- `COUNTRY`, `REGION`, `DISTRICT`, `NEIGHBORHOOD`, `CATALOG`, `ICD10`, `MANUAL_REPORT`: `READ`, `CREATE`,
  `UPDATE`, `DELETE`; `DISTRICT`, `NEIGHBORHOOD`, `POPULATION`, `ORGANIZATION` da qo'shimcha `SYNC`.
  **Ma'lumotnoma/tashkilot/xodim o'qish hamma autentifikatsiyadan o'tgan foydalanuvchiga ochiq** (lookup
  uchun) — menyu uchun kerak bo'lsa ham backend 403 bermaydi. Yozish: admin yoki tegishli ruxsat.
- `POPULATION`: faqat super-admin yoki tegishli ruxsat.
- `EMPLOYEES`: `READ`, `CREATE`, `UPDATE`, `SYNC` (xodimlar ro'yxati/tahrir).
- `ROLE`, `PERMISSION`: rol va ruxsatlarni boshqarish paneli.
- `SPEECH_TO_TEXT` `CREATE`: ovozdan matnga (mikrofon tugmasi).
- `ALL`: "hammasini ko'rsat" belgisi (menyu uchun).

---

## 5. Rollar bo'yicha to'liq jadval (hozirgi holat)

`—` = bu subject'da ruxsat yo'q.

### isemid_general_practitioner — Umumiy amaliyot shifokori (UASH, yuboruvchi)

| Subject | Action'lar |
|---|---|
| FORM058 | `CREATE`, `READ_SENT`, `UPDATE`, `DELETE`, `CANCEL`, `APPROVE` |
| FORM0581 | `CREATE`, `READ_SENT`, `UPDATE`, `DELETE`, `CANCEL`, `APPROVE` |
| FORM129 | `CREATE`, `READ_SENT` |
| SPEECH_TO_TEXT | `CREATE` |
| CARD, ACT, REPORT, … | — |

→ Menyuda: **Form 058, Form 058-1, Form 129** (yuboruvchi/chiquvchi ro'yxat + "Yaratish"). Hisobot, karta, akt yo'q.

### isemid_epidim_head — Epidemiologiya bo'limi mudiri (qabul qiladi, biriktiradi, nazorat qiladi)

| Subject | Action'lar |
|---|---|
| FORM058 | `READ_RECEIVED`, `ACCEPT`, `REJECT`, `CHANGE_ORG` |
| FORM0581 | `READ_RECEIVED`, `ACCEPT`, `REJECT` |
| FORM129 | `READ_RECEIVED`, `ACCEPT`, `REJECT` |
| CARD | `READ`, `UPDATE`, `DELETE`, `APPROVE`, `RETURN`, `ASSIGN`, `VIEW_ALL` |
| ACT | `READ`, `UPDATE`, `ASSIGN`, `VIEW_ALL` |
| REPORT | `READ`, `EXPORT` |
| REPORT_ENTRY | `READ`, `UPDATE` |
| ANALYTIC_REPORT | `READ`, `UPDATE`, `EXPORT` |
| SPEECH_TO_TEXT | `CREATE` |

### isemid_epidemiologist — Epidemiolog

| Subject | Action'lar |
|---|---|
| FORM058 / FORM0581 / FORM129 | `READ_RECEIVED` |
| CARD | `READ`, `UPDATE`, `ACCEPT`, `REJECT`, `COMPLETE` |
| ACT | `READ`, `UPDATE`, `ASSIGN` |
| REPORT | `READ`, `EXPORT` |
| REPORT_ENTRY | `READ`, `CREATE`, `UPDATE` |
| ANALYTIC_REPORT | `READ`, `CREATE`, `UPDATE`, `EXPORT` |
| EMPLOYEES | `READ`, `CREATE`, `UPDATE` |

### isemid_assistant_epidemiologist — Yordamchi epidemiolog (hisobot yo'q, forma qabul qilmaydi)

| Subject | Action'lar |
|---|---|
| FORM058 / FORM0581 / FORM129 | `READ_RECEIVED` |
| CARD | `READ`, `UPDATE`, `ACCEPT`, `REJECT`, `COMPLETE` |
| ACT | `READ`, `UPDATE`, `ASSIGN` |
| SPEECH_TO_TEXT | `CREATE` |

### isemid_head_doctor — Bosh shifokor (faqat ko'rish)

| Subject | Action'lar |
|---|---|
| FORM058 / FORM0581 / FORM129 | `READ_SENT`, `READ_RECEIVED` |
| REPORT | `READ`, `EXPORT` |
| ANALYTIC_REPORT | `READ`, `EXPORT` |

### isemid_admin / isemid_super_admin

- `isemid_super_admin`: **22 subject × barcha action** (`MANAGE` bilan), har ishga tushishda avtomatik
  sinxronlanadi. Instance darajasida ham to'siqsiz (har qanday tomon nomidan harakat qila oladi).
- `isemid_admin`: super-adminniki **minus `DELETE` va `MANAGE`**. Instance bypass yo'q (to'g'ri
  tashkilot/xodim bo'lishi kerak).
- `isemid_qr_user`: hozircha ruxsat yo'q. `isemid_user`, `isemid_doctor`: ishlatilmaydi, o'chirilgan.

---

## 6. Ish jarayoni (qanday oqadi)

1. **UASH** forma 058 / 058-1 / 129 yaratadi va yuboradi (`CREATE`) → "Chiquvchilar" (`READ_SENT`).
2. **Mudir** kiruvchilarni ko'radi (`READ_RECEIVED`), qabul qiladi yoki rad etadi (`ACCEPT`/`REJECT`),
   kartalarni yaratib epidemiolog/yordamchiga biriktiradi (`CARD` `ASSIGN`).
3. **Epidemiolog / yordamchi** o'z kartalarini ko'radi (`CARD` `READ`, "mening"), qabul qiladi, to'ldiradi,
   yakunlaydi, kerak bo'lsa akt yaratadi (`ACT` `ASSIGN`).
4. **Mudir** nazorat qiladi: kartani tasdiqlaydi (`APPROVE`) yoki qaytaradi (`RETURN`), hammasini ko'radi (`VIEW_ALL`).
5. **UASH** qabul qilingach yakuniy `APPROVE` ni beradi (yuboruvchi huquqi).

---

## 7. Frontend uchun ishchi jadval: ekran → shart

| Ekran / tugma | Ko'rsatish sharti (authority) |
|---|---|
| Menyu: Form 058 | `PERMISSION_FORM058_CREATE` **yoki** `_READ_SENT` **yoki** `_READ_RECEIVED` |
| Menyu: Form 058-1 | `PERMISSION_FORM0581_*` xuddi shunday |
| Menyu: Form 129 | `PERMISSION_FORM129_*` xuddi shunday |
| "Yaratish" tugmasi | `PERMISSION_FORM<058\|0581\|129>_CREATE` |
| Chiquvchilar tabi | `…_READ_SENT` |
| Kiruvchilar tabi | `…_READ_RECEIVED` |
| "Qabul qilish" / "Rad etish" | `…_ACCEPT` / `…_REJECT` (+ forma statusi) |
| "Qaytarib olish" (yuboruvchi) | `…_CANCEL` (+ status `SENT`) |
| "Tasdiqlash" (yuboruvchi) | `…_APPROVE` (+ status) |
| "Tashkilotni almashtirish" | `PERMISSION_FORM058_CHANGE_ORG` |
| Menyu: Kartalarim | `PERMISSION_CARD_READ` |
| Menyu: Barcha kartalar | `PERMISSION_CARD_VIEW_ALL` |
| Menyu: Aktlarim / Barcha aktlar | `PERMISSION_ACT_READ` / `PERMISSION_ACT_VIEW_ALL` |
| Menyu: Hisobotlar | `PERMISSION_REPORT_READ`; eksport tugmasi `PERMISSION_REPORT_EXPORT` |
| Qo'lda hisobotlar (2/31/32/7) | `PERMISSION_REPORT_ENTRY_READ` / `_CREATE` / `_UPDATE` |
| Tahliliy hisobot | `PERMISSION_ANALYTIC_REPORT_READ` … |
| Mikrofon (ovozdan matn) | `PERMISSION_SPEECH_TO_TEXT_CREATE` |
| Rollar/ruxsatlar paneli | `PERMISSION_ROLE_*`, `PERMISSION_PERMISSION_*` |

Qoidalar:
- Authority'larni bitta konstanta faylida saqlang.
- `…_MANAGE` bor bo'lsa, backend allaqachon barcha action'larni alohida qaytaradi — qo'shimcha mantiq kerak emas.
- Bitta foydalanuvchi ham yuboruvchi, ham qabul qiluvchi tomonga ega bo'lishi mumkin.
- Faqat §4 va §7 da ko'rsatilgan ruxsatlar backendda **majburiy**; menyu-only subject'larni (`ALL` kabi)
  backend tekshirmaydi.
