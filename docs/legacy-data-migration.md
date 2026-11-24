# Legacy → yangi sxema: ma'lumot ko'chirish bo'yicha baholash

**Holat:** Phase 0 — baholash va mapping skeleti. Kod yozilmagan.
**Manba:** `localhost:5432/isemid` (foydalanuvchi tasdig'iga ko'ra prod sxemasi bilan bir xil) — 78 jadval, Hibernate `ddl-auto=update` bilan qurilgan, `databasechangelog` bo'sh.
**Nishon:** yangi loyihaning Liquibase sxemasi (namuna: `localhost:5434/isemid2`, 100 jadval).
**Hajm:** prod'da ~300K+ qator.

---

## 1. Asosiy xulosa — bu yengil migratsiya EMAS

Yangi sxema — legacy'ning to'liq qayta ishlab chiqilgan versiyasi. Deyarli har bir jadvalда:

- **Ustun nomlari ingliz tiliga o'tkazilgan va qayta nomlangan**
  (`apply_date`→`application_date`, `mkb_code`→`icd10_code`, `wcseats`→`wc_seats`, ...).
- **Act modulida** reference qiymatlar denormalizatsiya qilingan — bitta `*_code` ustuni o'rniga
  `*_id` + `*_uz` + `*_ru` uchligi (masalan `act153` da ~30 ta yangi shунday ustun).
- **Sana tiplari** `timestamp without time zone` → `timestamp WITH time zone` (butun bazada).
- **Optimistik-lock `version` ustuni** deyarli barcha entity jadvallariga qo'shilgan (NOT NULL).
- **Soft-delete** (`deleted`, `deleted_at`, `deleted_by_id`, `delete_reason`) ko'p aggregate'larга qo'shilgan.
- **Manzil ustunlari qayta nomlangan**: `city_code`/`state_code` → `district_code`/`region_code`.
  Bu **oddiy string** (yangi sxemada `ref_*` ga FK YO'Q) — jadval join emas, faqat ustun nomi
  o'zgaradi + qaysi legacy ustun qaysi yangisiga tushishi aniqlanadi.

**Muhim:** yangi sxemada birorта biznes jadval `ref_*` (ICD-10, catalog, region/district)
jadvallariga **FK bilan bog'lanmaydi**. `icd10_code`, `icd10_name`, `catalog_code`,
`region_code`, `district_code` — hammasi denormalizatsiya qilingan string.
Shuning uchun **ID crosswalk kerak emas** — legacy string qiymati to'g'ridan-to'g'ri ko'chiriladi.
(Ixtiyoriy: ko'chirishdан keyin seed qilinган `ref_*` da yo'q kodlar bo'yicha validatsiya hisoboti.)
- `card174`, `card175`, `card205`, `card_tube` — ustun to'plamlari **deyarli butunlay bir-biridan farq qiladi**;
  har bir maydon domen mutaxassisi bilan qo'lда moslashtirilishi kerak.

**Natija:** bu `INSERT ... SELECT` bir-ikki `CASE` bilan hал bo'ladigan ish emas.
~50 ta jadval uchun maydon-darajасидa mapping + reference crosswalk'lar kerak.
Realistik baho: **bir necha hafta**lik ish, domen mutaxassisi ishtiroki bilan.

---

## 2. Ko'chirish arxitekturasi (tasdiqlangan yondashuv)

Bitta bazada ikki schema — `postgres_fdw` shart emas:

```sql
-- 0. TO'LIQ backup (pg_dump -Fc) — majburiy, har muhitda

-- 1. eskini chetga surish
ALTER SCHEMA public RENAME TO legacy;
CREATE SCHEMA public;
-- extension'lar (pgcrypto), GRANT'lar qayta tiklanadi
-- DIQQAT: legacy `uuid-ossp` ishlatgan, yangi `pgcrypto`

-- 2. yangi ilova / Liquibase ishga tushadi → public da toza yangi sxema
--    (databasechangelog ham toza public da)

-- 3. tartiblangan migration skriptlari (db/legacy-migration/*.sql):
--    INSERT INTO public.<yangi> (...) SELECT <transform> FROM legacy.<eski> ...
--    FK bog'liqligi tartibida

-- 4. barcha sequence'larni to'g'rilash (setval)

-- 5. tekshirish: qator sonlari, FK butunligi, tanlab solishtirish

-- 6. keyin: DROP SCHEMA legacy CASCADE;
```

- **Dev ko'chirilmaydi** — faqat prod. Dev uchun toza sxema + Liquibase seed yetadi.
- Skriptlar **Liquibase changelog'ga qo'shilmaydi** (bu sxema emas, bir martalik ma'lumot).
- `db/legacy-migration/` — tartiblangan `.sql` + `run.sh`, cutover'da qo'lda ishga tushiriladi.

---

## 3. Jadvallarни tasniflash

### 3a. Umuman KO'CHIRILMAYDI (yangi Liquibase seed'idan keladi)

- Barcha `cls_*` (14490 qatorли `cls_mkb10` ham) → yangi `ref_icd10`, `ref_catalog`, `ref_manual_report*`.
  (Biznes jadvallar bularга FK bilan bog'lanmagani uchun crosswalk ham shart emas —
  faqat `cls_manual_report`/`cls_manual_report_mkb10_codes` da foydalanuvchi kiritган
  ma'lumot bo'lsa, u alohida ko'rib chiqiladi; prod'da 0 qator.)
- RBAC seed: `permission`, `role`, `role_permissions`, `permission_actions`
  (yangi `action` jadvali + RBAC yangi struktura Liquibase'da seed qilingan —
  [[project_rbac_architecture]])
- Yangi-only infra jadvallari: `action`, `audit_event`, `dev_*`, `event_publication`,
  `export_job`, `integration_client`, `notification`, `outbound_webhook_dispatch`,
  `security_route_policy`, `system_settings`, `risk_rule`, `user_local_roles`,
  `ref_country/region/district/neighborhood`, `ref_manual_report_types`

**Faqat biznes ma'lumot ko'chiriladi:** `users`, `user_roles`, `users_organizations`,
`organization`, `organization_service_types`, `patient` + `pt_*`, `form058` + `fm058_location`,
`card` + barcha `card161/174/175/205/tube*`, `act` + barcha `act15x/22x*`.

### 3b. 1:1 KO'CHIRILADI (ustunlar bir xil, faqat `version=0` va `timestamptz` qo'shiladi)

`act_users`, `card_users`, `users_organizations`, `user_roles`,
`organization_service_types`, `card161_indirection_causing`,
`card174_affected_animals`, `card174_disease_factors`, `card174_disinfection_factors`,
`card174_elimination_method`, `card175_disease_transmission_condition`,
`card175_part_of_injury`, `card175_pathogen_main_factor`,
`card175_taken_measures_from_residence`, `card_tube_checkup_dates`,
`card_tube_nutrition_type`, `patient` (faqat `version` qo'shiladi)

### 3c. MAYDON-DARAJАСИДА REMAP kerak (domen tekshiruvi bilan)

Har biri uchun to'liq ustun-ustun jadval yoziladi (Phase 1):

| jadval | og'irlik | asosiy o'zgarish |
|---|---|---|
| `form058` | O'RTA | `mkb10code/name`→`icd10_code/name`, `final_*` xuddi shunday, `comment`→`form_comment`; yangi NOT NULL: `source`, `has_linked_cards`, `deleted`, `version` |
| `organization` | O'RTA | `city_code/state_code`→`district_code/region_code` (SOATO crosswalk), `line`→`address_line`, `email` DROP?, `name`→`name_uz/ru/kaa/uz_cyril` |
| `users` | O'RTA | `city_code/state_code/district`→`region_code/district_code`, `password`/`ppn`/`position_code` DROP?, `username` yangi NOT NULL |
| `pt_address`, `pt_affiliation` | O'RTA | manzil kodlari, `pt_affiliation` ga `organization_uuid` |
| `act` | KATTA | `status`→`act_status` (enum qiymat mapping), yangi: `subject`, `subject_type`, `institution_*`, `lis_*`, soft-delete; ACT155 → ACT154 |
| `act153/154/223` (+ `_detail`) | KATTA | ~30 ta yangi `*_id/*_uz/*_ru` reference uchligi; eski `position`/`tin`/`loinc` → yangi identifier ustunlari |
| `act156`, `act224` (+ bola) | O'RTA | nomlar (`full_nameof_sampler`→`full_name_of_sampler`, `wcseats`→`wc_seats`), `organization_id`→`lis_organization_id` |
| `card` | KICHIK | faqat yangi ustunlar: soft-delete, `version`, `form058_1_id` (legacy'da manba yo'q) |
| `card161` | O'RTA | `comment` DROP/map, yangi klinik ustunlar (manba yo'q) |
| `card174` | KATTA | ustunlar deyarli butunlay boshqacha — to'liq qo'lда mapping |
| `card175` | KATTA | xuddi shunday — deyarli disjoint ustun to'plami |
| `card205` | KATTA | `mkb_code`→`icd10_code`, `vet_cert_*`→`certificate_*`, deyarli barcha nomlar o'zgargan |
| `card_tube` | KATTA | `mkb10code`→`icd10_code`, `rooms`→`room_count`, `adults`→`adult_count` ... ~45 ustun qayta nomlangan; `dispensary_id` bigint→varchar |
| `card174_infection_monitoring`, `card_tube_*` bola jadvallari | O'RTA | nom o'zgarishlari (`apply_date`→`application_date`, `diag_date`→`diagnosis_date`, ...) |
| `card205_*` bola jadvallari | KATTA | jadval nomlari VA semantikasi almashgan — 3d ga qarang |

### 3d. ❓ QAROR KERAK — nishon jadval yo'q (ma'lumot yo'qolishi mumkin)

| legacy jadval | holat | savol |
|---|---|---|
| `act155`, `act155_detail` | yangi'da yo'q (ACT154 ga birlashtirilgan) | Pestitsid namuna maydonlari (`applied_pesticides`, `manufacturer`, `product_batch_quantity`...) `act154`/`act154_detail` ning qaysi ustunlarига tushadi? Yoki tashlab yuboriladimi? |
| `card174_disinfection_info` | yangi'da yo'q | `control_results`, `disinfected_count`, `disinfection_date`, `event_location` — yangi `card174` ning qaysi ustunlariga? (yangi'da `disinfection_date`, `disinfected_factor_amount` bor — mos keladimi?) |
| `card174_external_sample_test` | yangi'da yo'q | `sample_count`, `specimen`, `test_date`, `test_method`, `test_result` — yangi `card174.test_date/test_result/test_sample_count/testing_method` ga? (1:1 bola → 1:1 ota?) |
| `card174_preventive_measure` | yangi'da yo'q | `measure_count/date/location/measures` — qayerga? |
| `vaccination` | yangi'da alohida jadval yo'q | yangi `card161_vaccination` endi to'liq entity (join emas) — `vaccination` qatorlari `card161_vaccination` ga `card161_id` bilan ko'chiriladimi? `card174` va `infection_monitoring` uchun vaksinatsiyalar nima bo'ladi? |
| `infection_monitoring_vaccination` | yangi'da yo'q | `card174_infection_monitoring` ↔ `vaccination` bog'lanishi — yangi'da `card174_infection_monitoring.vaccination_summary`→`?` (varchar summary bor). Batafsil vaksinatsiya yozuvlari tashlanadimi? |
| `card205_animal_bite_victim` → `card205_info_bitten_people` | nom+semantika shubhali | Legacy `animal_bite_victim` = hayvon tishlagan **jabrlanuvchi**. Yangi `info_bitten_people` ustunlari (`first_name`, `birth_date`, `region/district`) mos. Lekin yangi `info_about_animal_bitten_people` da `animal_category_code`, `animal_type`, `full_name_of_animal_bitten_owner` bor — bu legacy `animal_bite_victim` ning `animal_*` ustunlariga o'xshaydi. **Uch jadval orasida ustunlar aralashgan — domen tasdig'i shart.** |

### 3e. Boshqa ochiq savollar

1. **`form058_1`** — legacy'da umuman yo'q. Yangi `card.form058_1_id` va `form058_1` jadvali bor.
   Legacy'da zoonoz/hayvon-tishlash bildirishnomalari (058-1) qayerda saqlangan?
   `card205` bor, lekin uning ustidagi 058-1 forma yo'q. → prod'ni tekshirish kerak.
2. **Manzil ustunlari** (string, join yo'q) — legacy `state_code`→`region_code`,
   `city_code`→`district_code` deb qabul qilinsinmi? `organization.district` / `users.district`
   (erkin matn) yangi `district_code` ga tushadimi yoki tashlanadimi?
   Legacy kod formati yangi SOATO bilan bir xilmi (agar boshqacha bo'lsa — value transform)?
3. **`users`** — `password` ustuni ko'chiriladimi (yangi tizim SSO/DHP JWT ishlatadi —
   parol kerak emasga o'xshaydi)? `username` NOT NULL — qanday to'ldiriladi (`ppn`? email?)?
4. **`act.status` → `act.act_status`** — enum qiymatlari to'liq mos keladimi?
   Legacy: `NEW, IN_PROGRESS, COMPLETED, NOT_VIEWED, ACT_ATTACHED`. Yangi qiymatlar tekshirilsin.
5. **`card.status`** — legacy `REJECTED_BY_USER`/`ACCEPTED_BY_USER` va
   `form058` reject-status rename (changelog `20260806-1200`) — qiymat mapping kerak.

---

## 4. Kesib o'tuvchi (cross-cutting) transformlar

Har bir INSERT'da qo'llanadi:

| transform | qoida |
|---|---|
| `version` | mavjud bo'lmasa `0` |
| `created_at`/`updated_at` | `timestamp` → `timestamptz`: `AT TIME ZONE 'Asia/Tashkent'` bilan (yoki UTC deb qabul qilish — QAROR KERAK) |
| `deleted` | `false` (legacy'da soft-delete yo'q) |
| `uuid` | legacy'da bo'lsa saqlanadi; yo'q joyda `gen_random_uuid()` |
| `*_org_uuid` (`created_org_uuid` ...) | legacy'da yo'q joyda `NULL` |
| ICD-10 | legacy `mkbXXcode`/`mkbXXname` (matn) → yangi `icd10_code`/`icd10_name` (matn) — to'g'ridan-to'g'ri nusxa, FK yo'q. Ixtiyoriy: `ref_icd10` da yo'q kodlar hisoboti |
| `catalog_code` (element to'plamlari) | to'g'ridan-to'g'ri nusxa (string, FK yo'q). Ixtiyoriy: `ref_catalog` qamrovi hisoboti |
| manzil kodlari | legacy `state_code`→yangi `region_code`, legacy `city_code`→yangi `district_code` (tasdiqlash kerak); `neighborhood_code` bir xil. String, FK yo'q. `users.region_code` varchar(20) — uzunlik tekshiruvi |
| audit FK (`created_by_id`, `updated_by_id`, `assigned_by_id`) | `users` birinchi ko'chiriladi; mos foydalanuvchi yo'q bo'lsa `NULL` |
| PK / sequence | legacy `id` saqlanadi (FK'lar mos kelishi uchun), oxirida barcha jadval uchun `setval(pg_get_serial_sequence(...), max(id))` |

---

## 5. Bosqichli reja

| Bosqich | Ish | Natija |
|---|---|---|
| **0 (hozir)** | Sxema diff, tasnif, qaror registri | *bu hujjat* |
| **1** | 3d/3e savollariga domen javoblari; har remap jadval uchun to'liq ustun-ustun mapping jadvali | `docs/legacy-migration-mapping-<module>.md` × N |
| **2** | `db/legacy-migration/*.sql` yozish (FK tartibida), `5432/isemid` (kichik) ustida iteratsiya | ishlaydigan skript to'plami |
| **3** | Prod restore (to'liq ~300K) ustida quruq yugurtirish — vaqt, qator sonlari, FK butunlik, tanlab solishtirish | validatsiya hisoboti |
| **4** | Cutover: eski tizim to'xtatiladi → oxirgi backup → schema rename → Liquibase → skriptlar → tekshirish → yangi tizim yoqiladi | migratsiya tugadi |

---

## 6. Ochiq qarorlar — kim javob beradi

3d va 3e bo'limlaridagi har bir ❓ uchun domen mas'uli javobi kerak.
Javob berilгунча o'sha ustunlар `NULL` bilan ko'chiriladi va hisobotда belgilanadi
(kelishilган default — [[feedback_no_answer_default]]).
