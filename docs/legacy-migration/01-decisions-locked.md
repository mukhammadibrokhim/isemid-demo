# Tasdiqlangan qarorlar (2026-08-27)

Bu fayl `10/20/30/40-*.md` dagi `⚠️` bandlarni **bekor qiladi** (override).
Ziddiyat bo'lsa — shu fayl ustun.

---

## ⚠️ MUHIM: manba/nishon aniqlandi (round 4)

- **Manba:** `localhost:5434/isemid` → schema `public` (PG 17.2). ← `5432/isemid/public` ESKIRGAN,
  ishlatilmaydi. `10/20/30/40-*.md` `5432` DDL'idan yozilган — **`5434` bo'yicha qayta yoziladi**.
- **Nishon:** `localhost:5434/isemid` → schema `public2` (**bir xil baza** → `postgres_fdw` shart emas,
  `INSERT INTO public2.x SELECT FROM public.y` to'g'ridan-to'g'ri).
- `5434/public` da qo'shimcha jadvallar: `form058_1` (+`fm0581_animal_owner`, `fm0581_bitten_person`),
  `act224_detail`, `card161_main_probable_infection_factor`, `form129`, `analytic_*`, `form_report_*`,
  `token_revocations`, `cls_manual_report_types`.

### Round 4 qarorlari

| # | Qaror |
|---|---|
| **form058_1** | Legacy'da ma'lumot bo'lsa ham **SKIP** (ko'chirilmaydi). `card.form058_1_id → NULL`. |
| **act.subject_* / institution_* / lis_* / tin** | Legacy `5434.act` da ustunlar BOR → **to'g'ridan ko'chiriladi** (avvalgi "NULL" qarori bekor). |
| **form058** yangi mos ustunlar | `source` (§form058), `approved_full_name`→`approved_full_name`, `approved_org_uuid`→`approved_org_uuid`, `cancel_reason`→`cancel_reason`, `canceled_by`→`canceled_by_id` — to'g'ridan. |
| **act.status** (`5434`: NEW/IN_PROGRESS/SENT/PENDING/RECEIVED/COMPLETED/NOT_VIEWED/ACT_ATTACHED/FAILED) → yangi `ActStatus` | `NEW→NEW`, `NOT_VIEWED→NEW`, `IN_PROGRESS→IN_PROGRESS`, `PENDING→READY`, `SENT→SENT`, `FAILED→SEND_FAILED`, `RECEIVED→COMPLETED`, `COMPLETED→COMPLETED`, `ACT_ATTACHED→COMPLETED`. |
| **form058.status** `CARD_REJECTED` | Yangi `FormStatus` da yo'q → `ACCEPTED` (forma qabul qilinган, karta rad etilган — forma ortga qaytadi). Qolgan mapping o'zgarmaydi. |

⏳ **Kerak:** `pg_dump --schema-only -n public2` **`5434` dan** (mening `new-public2-ddl.sql` `5432` dan — ishonchsiz).

---

## Kesib o'tuvchi

| # | Qaror |
|---|---|
| **Timestamp** | Legacy `timestamp` → `timestamptz`: **`ts AT TIME ZONE 'Asia/Tashkent'`** (legacy qiymat Toshkent mahalliy vaqti). Butun migratsiyada. |
| **Manzil kodlari** | `state_code`→`region_code`, `city_code`→`district_code`, `neighborhood_code`→bir xil. Format hozircha bir xil (`UZ-AN`, `AN-206`) → **to'g'ridan nusxa**, transform yo'q. Crosswalk keyin (kerak bo'lsa). |
| **`id` / `version` / `deleted`** | `id` saqlanadi; `version` → `0`; `deleted` → `false`, `deleted_at`/`delete_reason` → `NULL`. |
| **NOT NULL bo'shliq** | Ustun-ustun, SQL yozish paytida hal qilinadi. Zaruriy maydon `NULL` va tiklab bo'lmasa → qator **skip** + `db/legacy-migration/_skipped.log`. Migratsiya to'xtamaydi. |

---

## form058

| Maydon | Qaror |
|---|---|
| **`source`** (yangi NN) | Legacy `public.form058` da **`source varchar(20)`** ustuni bor (foydalanuvchi IDE'да ko'rsatdi — mening 2026-08-27 17:18 dump'imда yo'q edi, ya'ni dump eskirган / ustun keyin qo'shilган). Qoida: `NULL`/bo'sh → **`'MANUAL'`**; `'DMED'` → `'DMED'`; boshqa qiymat (jumладан `'YKEM'`) → `'MANUAL'`. SQL: `CASE WHEN upper(trim(f.source)) = 'DMED' THEN 'DMED' ELSE 'MANUAL' END`. **⚠️ SQL yozishдан oldin yangi `\d public.form058` kerak** (ustun aniq nomi/qiymatlari uchun). |
| **`status`** | `NEW→SENT`, `SENT→SENT`, `RECEIVED→ACCEPTED`, `APPROVED_PENDING→ACCEPTED`, `CARD_LINKED→CARD_LINKED`, `APPROVED→APPROVED`, `CANCELED→CANCELED`, `NOT_APPROVED→CANCELED`. |
| **`not_approve_comment` → `cancel_reason`** | Ha (rad/bekor sababi bir xil maydon). |
| **`has_linked_cards`** | `EXISTS (SELECT 1 FROM public.card c WHERE c.form058_id = f.id)`. |
| **`form_comment`** ← `comment` | to'g'ridan (nom o'zgardi). |
| **patient denormal maydonlar** (`patient_full_name` va h.k.) | `public.patient` (+ `pt_identifier`) dan JOIN. `patient_nnuzb` uchun `pt_identifier.type_code` qaysi qiymat = NNUZB — **⚠️ hali kerak** (yagona ochiq band). |
| **`form058_1`** | Legacy'dan ko'chirilmaydi. |

## card

| Maydon | Qaror |
|---|---|
| **`status`** | Legacy = yangi, **1:1** (`NEW, IN_PROGRESS, COMPLETED, ACCEPTED_BY_USER, REJECTED_BY_USER, APPROVED, REJECTED`). |
| **`form058_1_id`** | `NULL`. |
| **card174/175/205/tube nom juftliklari** | Hujjatdagi "yuqori ishonch" juftliklar qabul qilinadi. "past/o'rta ishonch" va `card205` bola jadvallari — **§ pastda "7-band" tushuntirishi**. |

## act

| Maydon | Qaror |
|---|---|
| **ACT155** | `act` / `act155` / `act155_detail` / tegishli `act_users` — **ko'chirilmaydi**. |
| **`status`** | `NEW→NEW`, `IN_PROGRESS→IN_PROGRESS`, `COMPLETED→COMPLETED`, `NOT_VIEWED→NEW`, `ACT_ATTACHED→COMPLETED`. |
| **`card_id`** | to'g'ridan (legacy ham `card_id`). |
| **Reference triplet** (`*_id` + `*_uz` + `*_ru`) | LIS'dan kelgan maydonlar → **`*_id = NULL`, `*_uz = <legacy matn>`, `*_ru = NULL`**. `ref_catalog` ga bog'lash **yo'q**. |
| **`subject_type`, `institution_*`, `lis_*`** | Legacy'da manba yo'q → `NULL` / DDL default. |
| **`*_identifier_type/value`** ← legacy `tin` | `type='TIN'`, `value = tin::text`. |
| **detail → header ko'chган maydonlar** | Header qiymati = o'sha `act`ning **birinchi (eng kichik `id`) detail qatoridan**. |

## Tayanch (organization / users / patient / pt_*)

| Maydon | Qaror |
|---|---|
| **`users.password`, `ppn`, `position_code`** | Ko'chirilmaydi (DROP). |
| **`organization.country_code`, `email`, `service_area_code`** | DROP. |
| **`organization`/`users` `.district`** (erkin matn) | DROP. |
| **`organization.name` → `name_uz`** | Ha, `name` ni `name_uz` ga ham nusxa (qolgan `name_ru/kaa/uz_cyril` = `NULL`). |
| **`users.username`** | `nnuzb` (NULL bo'lsa `NULL` — yangi'da nullable). |
| **`organization.level_type` / `medical_type` NULL** | `'NOT_DEFINED'`. |
| **`patient`** | ~1:1 + `version`, `timestamptz`. |
| **`pt_address` / `pt_affiliation` `patient_id IS NULL`** | Qator **skip** + log (yetim manzil). |
| **`pt_identifier.type_code IS NULL`** | `'UNKNOWN'`. |
| **`pt_affiliation.organization_uuid`** | `organization_id` bo'yicha `public.organization.uuid` dan lookup. |
| **`user_roles`** | **Ixtiyoriy.** SSO token ma'lumotidan rol avtomatik biriktiriladi. Ko'chirilsa — legacy↔yangi `role.name` crosswalk; mos kelmasa qator tashlanadi + log. |

---

## "7-band" — card174/175/205/tube past-ishonch juftliklari haqida

**Muammo:** bu 4 ta jadvalда legacy va yangi ustun **nomlari** deyarli butunlay
boshqacha. Men faqat nomlarga qarab taxmin qila olaman (`20-card.md` §4–7 jadvallari).
"Yuqori ishonch" — nom aniq mos (`mkb_code`→`icd10_code`). "Past ishonch" — men
faqat taxmin qilyapman, xato bo'lishi mumkin (masalan `card175.primary_diagnosis`
→ `initial_diagnosis_code`: bu kod maydonimi yoki erkin matnmi? `card205_animal_bite_victim`
→ `card205_info_about_animal_bitten_people`: bu ikkalasi bir xil narsami — jabrlanuvchi
odam yoki hayvon egasi?).

**Mendan kerak bo'lgan narsa:** `20-card.md` §4a, §5a, §6a, §6b, §7a jadvallaridagi
**"o'rta"/"past" ishonch qatorlarni** ko'rib chiqib, har biriga:
- ✅ to'g'ri, yoki
- ❌ noto'g'ri → to'g'ri yangi ustun nomi, yoki
- 🗑 legacy ustun tashlanadi (yangi'da mos yo'q).

Buni jadval-jadval qilsak qulay. Aytсангiz, `card174` dan boshlab har birини
navbat bilan ko'rib chiqamiz. Yoki siz `20-card.md` ni to'g'ridan tahrirlab,
qatorlarga belgi qo'yib bering — men shунга qarab SQL yozaman.

---

## Qolgan ochiq bandlar

✅ **`form058.patient_nnuzb`** ← `pt_identifier.value` WHERE `type_code = 'NNUZB'`. Topilmasa → qator **skip** + log.
✅ **`act`/`card` `.created_org_uuid` NULL** → qator **skip** + log.
✅ **`card175`** — `initial_diagnosis_code` / `checking_diagnosis_code` va shunga o'xshash
diagnoz-kod ustunlari legacy `card175` dan EMAS, **bog'langan `form058` / `form058_1` dan**
tortiladi (`f.icd10_code`). Legacy `card175.primary_diagnosis` (erkin matn) → agar yangi'да
erkin-matn diagnoz maydoni bo'lmasa — tashlanadi.
✅ **`card205_animal_bite_victim` → `card205_info_about_animal_bitten_people`** — to'g'ri (tasdiqlandi).

⏳ **card174 / card_tube** (va card175/205 ning qolган "o'rta/past ishonch" qatorlari) —
SQL yozish paytida jadval-jadval ko'rib chiqiladi (`20-card.md` §4a, §6a, §7a).
Bu SQL yozishни **to'sib turmaydi** — o'sha jadvalga kelganда aniqlanadi.

⚠️ **Kerak:** yangi `pg_dump --schema-only -n public -t form058` (yoki `\d public.form058`) —
`source` ustuni dump'да yo'q edi.

---

## Round 6 — sinov natijasidan keyingi yumshatishlar (2026-08-28)

Sinov (`isemid_test`, 6 form058) da barcha qatorlar `NNUZB yo'q` bilan skip
bo'ldi (test bemorlarda identifikator turi `REO`, `NNUZB` emas). Tuzatildi:

- **`form058.patient_nnuzb`** (NOT NULL) endi **skip qilmaydi**: `NNUZB` bo'lsa
  NNUZB; bo'lmasa har qanday identifikator; u ham bo'lmasa
  `lpad(patient_id, 14, '0')`. Fallback ishlatilgan qatorlar `_migration_skipped`
  ga `INFO:` bilan yoziladi (lekin **ko'chiriladi**).
- **`card.created_org_uuid`** (NOT NULL): `NULL` bo'lsa bog'langan
  `form058.created_org_uuid` dan olinadi. Ikkalasi ham NULL bo'lsagina skip.
- **`act.created_org_uuid`** (NOT NULL): `NULL` bo'lsa bog'langan
  `card.created_org_uuid` dan.
- Barcha toraytiruvchi `varchar` ustunlar `left(x, N)` bilan himoyalandi
  (`icd10_code`(20), `patient_nnuzb`(14), `journal_form_code`(64), ...).

O'zgargan fayllar: `40-form058.sql`, `50-card.sql`, `60-act.sql`.
