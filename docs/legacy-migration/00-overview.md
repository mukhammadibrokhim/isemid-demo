# Legacy `public` → yangi `public2`: ko'chirish mapping'i — umumiy qism

**Holat:** Phase 1 — mapping loyihasi. Kod YOZILMAGAN. Bu hujjatlar tasdiqlanгач
`db/legacy-migration/*.sql` yoziladi.

**Manba:** `localhost:5432/isemid`, `public` schema (eski struktura, `ddl-auto=update`,
`databasechangelog` — eski loyihaники, yangisiga aloqasiz).
**Nishon:** shu bazaning `public2` schema'si (yangi loyiha, Liquibase egaligida).
**Afzallik:** bitta baza, ikki schema → `INSERT INTO public2.x SELECT ... FROM public.y`
to'g'ridan-to'g'ri ishlaydi, FDW/dump kerak emas.

Ushbu hujjat to'plami faqat **form058 + card + act** va ular bog'liq bo'lgan
tayanch jadvallar (organization, users, patient, pt_*) ni qamrаб oladi.

| Fayl | Mavzu |
|---|---|
| `00-overview.md` | *(bu fayl)* tartib, kesib o'tuvchi qoidalar, qarorlar, validatsiya |
| `10-form058.md` | `form058`, `fm058_location` |
| `20-card.md` | `card`, `card_users`, `card161*`, `card174*`, `card175*`, `card205*`, `card_tube*` |
| `30-act.md` | `act`, `act_users`, `act153/154/156/223/224` (+ detail/child) |
| `40-support.md` | `organization`, `users`, `patient`, `pt_address`, `pt_affiliation`, `pt_identifier` |

---

## 1. Tasdiqlangan domen qarorlari (2026-08-27)

| Modul | Qaror |
|---|---|
| **form058.source** | Yangi NOT NULL. **Legacy `public.form058` da `source` (yoki YKEM/DMED) ustuni UMUMAN YO'Q** (to'liq ustun ro'yxati tekshirildi). Shu sabab barcha ko'chirilган qatorlar → `'MANUAL'`. (Yangi `Form058SourceResolver` hozircha faqat `MANUAL` ni ruxsat etadi; `DMED` keyin qo'shiladi.) ⚠️ agar YKEM/DMED farqi boshqa yo'l bilan (masalan `created_by` user tashkiloti) aniqlanadigan bo'lsa — ayting. |
| **form058.has_linked_cards** | Hisoblanadi: `EXISTS (SELECT 1 FROM public.card c WHERE c.form058_id = f.id)` → `true`, aks holda `false`. |
| **form058.status** | ✅ `NEW→SENT`, `SENT→SENT`, `RECEIVED→ACCEPTED`, `APPROVED_PENDING→ACCEPTED`, `CARD_LINKED→CARD_LINKED`, `APPROVED→APPROVED`, `CANCELED→CANCELED`, `NOT_APPROVED→CANCELED`. |
| **form058_1** | Legacy'dan **ko'chirilmaydi**. Barcha migratsiya qilingan `card` lar `form058_id` bilan bog'lanadi, `form058_1_id = NULL`. |
| **act ↔ card** | Legacy `act.card_id` to'g'ri — yangi sxemada ham `act.card_id`. To'g'ridan-to'g'ri ko'chiriladi. |
| **act.status** | ✅ `NEW→NEW`, `IN_PROGRESS→IN_PROGRESS`, `COMPLETED→COMPLETED`, `NOT_VIEWED→NEW`, `ACT_ATTACHED→COMPLETED`. |
| **user_roles** | ✅ Ixtiyoriy — SSO token ma'lumotiga qarab rol avtomatik biriktiriladi. Ko'chirilsa: legacy↔yangi `role.name` bo'yicha crosswalk, mos kelmasa qator tashlanadi. Ko'chirmasa ham bo'ladi. |
| **act155 / act155_detail** | Legacy qatorlar **ko'chirilmaydi** (yangi sxemada jadval yo'q). `act` dan ham `act_type='ACT155'` qatorlari tashlanadi. |
| **act.subject_type / institution_* / lis_*** | Legacy'da manba yo'q → `NULL` qoldiriladi (yoki DDL default). |
| **users.password** | **Ko'chirilmaydi** (yangi tizim SSO/JWT). |

Hal qilinishi kerak bo'lganlar `⚠️ QAROR` deb belgilanган — har hujjat oxirида ro'yxat bor.

---

## 2. Ko'chirish tartibi (FK bog'liqligi — yangi sxema `public2` FK'lari bo'yicha)

```
1.  organization                         (40-support.md)
2.  users  + users_organizations + user_roles + user_local_roles   (40)
3.  patient + pt_address + pt_affiliation + pt_identifier           (40)
4.  fm058_location                        (10)   — form058 dan oldin (form058.location_id FK)
5.  form058                               (10)
    [form058_1 — TASHLAB O'TILADI]
6.  card                                  (20)   — form058 kerak (chk_card_exactly_one_form)
7.  card161 / card174 / card175 / card205 / card_tube   (20)  — JOINED: PK = card.id ga FK
8.  card* bola jadvallari (contact_person, vaccination, ... _detail)  (20)
9.  card_users                            (20)
10. act                                   (30)   — card kerak (act.card_id FK, nullable)
11. act153 / act154 / act156 / act223 / act224   (30)  — JOINED: PK = act.id ga FK
12. act* detail / child jadvallar         (30)
13. act_users                             (30)
14. OXIRIDA:
    - UPDATE public2.form058.assigned_card_id  (card ko'chgach — 10 §4)
    - barcha jadval uchun setval(pg_get_serial_sequence('public2.<t>','id'), (SELECT max(id) ...))
```

**Muhim:** `card` va `act` — JOINED inheritance. `card161` va h.k. ning `id` ustuni
`card.id` ga FK (`fk_card161_...` / legacy `fk4he10m...`). Ya'ni avval `card` bazasi,
keyin subtype qatori — **ikkalasi bir xil `id` bilan**. `act` ham xuddi shunday.

---

## 3. Kesib o'tuvchi transformlar (har `INSERT ... SELECT` da)

| Ustun | Qoida |
|---|---|
| `id` | Legacy `id` **saqlanadi** (barcha FK'lar mos kelishi uchun). |
| `version` | Yangi ustun (deyarli barcha entity jadvallarда). Legacy'da yo'q → `0`. |
| `created_at`, `updated_at` | Legacy `timestamp(6) without tz` → yangi `timestamp with time zone`. **✅ QAROR:** `X AT TIME ZONE 'Asia/Tashkent'` (legacy qiymatlar Toshkent mahalliy vaqti deb qabul qilinadi). Butun migratsiyada shu qoida. |
| `created_by_id`, `updated_by_id`, `assigned_by_id`, `deleted_by_id`, `canceled_by_id`, `approved_by_id` | `users` birinchi ko'chgani + `id` saqlangani uchun to'g'ridan-to'g'ri. Legacy'da NULL → NULL. |
| `created_org_uuid`, `updated_org_uuid` | Legacy'da bor jadvallarda saqlanadi. Yangi'da qo'shilgan, legacy'da yo'q joyда → `NULL` (agar ustun `NOT NULL` bo'lsa — pastga qarang). |
| `deleted` | Yangi `boolean NOT NULL DEFAULT false`. Legacy'da soft-delete yo'q → `false`. |
| `deleted_at`, `delete_reason` | → `NULL`. |
| `uuid` | Legacy'da bor (barcha asosiy jadvallarда `uuid NOT NULL`) → saqlanadi. |
| ICD-10 (`mkb10code`/`mkb10name`, `mkb_code`/`mkb_name`, `dg_mkb10code`...) | → yangi `icd10_code` / `icd10_name` (matn, FK yo'q — to'g'ridan nusxa). `usage_limit` bo'lsa → `icd10_usage_limit`. |
| `catalog_code` element-jadvallari (`card161_indirection_causing` va h.k.) | → to'g'ridan nusxa (`catalog_code` VARCHAR, FK yo'q). |
| Manzil kodlari | Legacy `state_code` → yangi `region_code`; legacy `city_code` → yangi `district_code`; `neighborhood_code` bir xil. **✅ QAROR:** hozircha format bir xil (`UZ-AN`, `AN-206`) → **to'g'ridan nusxa**, transform yo'q. Crosswalk keyinroq (kerak bo'lsa) qo'shiladi. |
| Reference "triplet" (`*_id` + `*_uz` + `*_ru`, faqat `act` da) | **✅ QAROR:** bu maydonlar LIS'dan kelган — **`*_id = NULL`, `*_uz = <legacy matn>`, `*_ru = NULL`**. `ref_catalog` ga bog'lash **shart emas**. |

### NOT NULL ustunlar — legacy'da manba yo'q

**✅ QAROR (umumiy):** har `NOT NULL` bo'shliq **SQL yozish paytida** ustun-ustun hal
qilinadi. Legacy qatorдa zaruriy maydon `NULL` bo'lsa va uni tiklab bo'lmasa —
o'sha qator **ko'chirilmaydi** va `db/legacy-migration/_skipped.log` ga yoziladi
(migratsiya toxtamaydi). Quyidagi jadval — dastlabki taklif; har `⚠️` uchun
tasdiq yoki boshqa qiymat kerak:

| Jadval.ustun | Taklif |
|---|---|
| `act.created_org_uuid` (NOT NULL) | ⚠️ legacy `act` da `created_org_uuid` bor — to'g'ridan. NULL bo'lган qatorlar uchun QAROR (masalan `assigned_by` userning tashkiloti). |
| `card.created_org_uuid` (NOT NULL) | legacy'da bor — to'g'ridan. NULL → QAROR. |
| `form058.sender_organization_id` / `receiver_organization_id` (NOT NULL) | legacy'da bor (nullable) — NULL qatorlar bo'lsa QAROR. |
| `form058.icd10_code` / `icd10_name` (NOT NULL) | legacy `mkb10code`/`mkb10name` (nullable). NULL bo'lsa → QAROR (masalan `'0'` / `'—'`?). |
| `form058.disease_date`, `first_visit_date`, `visit_date`, `initial_report_date_time`, `disease_place`, `notifier_full_name`, `patient_nnuzb`, `patient_full_name` (barchasi NOT NULL) | legacy'da nullable. NULL bo'lgan qatorlar soni tekshiriladi → default yoki QAROR (`40` §validatsiya). |

---

## 4. Xavfsizlik / ma'lumot yo'qolmaslik qoidalari

1. **Backup birinchi:** `pg_dump -Fc isemid` — har ishga tushirishdan oldin.
2. `public` — faqat `SELECT`. Hech qachon yozilmaydi.
3. `public2.databasechangelog*` ga tegilmaydi.
4. Migratsiya = `db/legacy-migration/NN-*.sql`, Liquibase changelog EMAS.
5. Har fayl **bitta tranzaksiya**; xato → to'liq `ROLLBACK`.
6. **Qayta ishga tushiriladigan:** har fayl boshida `TRUNCATE public2.<shu_fayldagi_jadvallar> CASCADE`
   (FK tartibida) yoki `INSERT ... ON CONFLICT (id) DO NOTHING`.
7. `public` **DROP qilinmaydi** — 5-bo'lim validatsiyasi to'liq o'tмаguncha.

---

## 5. Validatsiya (har fayldan keyin + oxirида)

- **Qator soni:** har `public.X` (soft-delete filtrisiz) vs `public2.Y`.
  `act` uchun: `public.act` − (`act_type='ACT155'` soni) = `public2.act`.
- **FK butunligi:** `public2` da orphan yo'qligi:
  `card` → `form058`; `act` → `card`; `card161..` → `card`; `*_detail` → parent; `*_users` → `act`/`card` + `users`.
- **JOINED butunligi:** har `public2.card` qatoriга aynan bitta subtype qatori
  (`card161`/`card174`/`card175`/`card205`/`card_tube`) mos kelishi;
  `card.card_type` subtype jadvaliга mos kelishi. `act` ham shunday.
- **Tanlab solishtirish:** har moduldan ~20 qator qo'lда (id bo'yicha).
- **Reference qamrovi (ixtiyoriy):** `icd10_code` / `catalog_code` qiymatlari
  `public2.ref_icd10` / `ref_catalog` da bor-yo'qligi hisoboti (FK yo'q — faqat sifat nazorati).
- **Sequence:** har jadval uchun `currval` ≥ `max(id)`.

---

## 6. Qarorlar holati (umumiy)

✅ **Hal qilinди:** timestamp zonasi (`Asia/Tashkent`), manzil kodlari (to'g'ridan nusxa),
reference triplet (`*_uz` ga matn, `*_id`=NULL), NOT NULL bo'shliq strategiyasi
(ustun-ustun + skip+log), `form058.source` (`'MANUAL'`), form058 & act status mapping'lari.

⚠️ **Hali kerak:**
1. **`*_org_uuid NOT NULL`** (`act.created_org_uuid`, `card.created_org_uuid`): legacy'да
   bu ustunlar bor (nullable). `NULL` qatorlar uchun — skip? yoki `assigned_by`/`created_by`
   user tashkiloti uuid'si?
2. Modul-specifik `⚠️` lar — `10/20/30/40` hujjatlarning oxiri.
