# Mapping — `5434/isemid` `public` → `public2` (ASOSIY, tekshirilgan)

Bu fayl **`10/20/30/40-*.md` ni almashtiradi** (ular eskirган `5432` DDL'idan edi).
Manba: `legacy-5434-ddl.sql`, nishon: `public2-5434-ddl.sql`. Ustun-ustun diff bilan tekshirildi.

Qoidalar: `01-decisions-locked.md`. Kesib o'tuvchi: `id` saqlanadi · `version`→`0` ·
`ts AT TIME ZONE 'Asia/Tashkent'` · `deleted`→`false`, `deleted_at`/`delete_reason`→`NULL` ·
`created_org_uuid`/`updated_org_uuid` — legacy'da bor bo'lsa to'g'ridan, target-only bo'lsa `NULL`.

**Yaxshi xabar:** `5434` legacy — `act` oilasi allaqachon refactor qilinган (reference
triplet'lar bor). Faqat **`card174/175/205/card_tube` BAZA jadvallari** hali eski nomlarда.

---

## 1. tayanch (organization / users / patient / pt_*)

### organization
- **to'g'ridan (11):** `active, created_at, id, level_type, medical_type, name, parent_id, phone, tin, updated_at, uuid`
- **rename:** `city_code`→`district_code`, `state_code`→`region_code`, `line`→`address_line`
- **drop:** `country_code, district, email, service_area_code`
- **backfill:** `version`→0; `created_by_id`/`updated_by_id`→NULL; `name_uz`←`name`, `name_ru/kaa/uz_cyril`→NULL; `created_at`/`updated_at` NULL bo'lsa `now()`
- `organization_service_types` — 1:1

### users
- **to'g'ridan (14):** `active, birth_date, country_code, created_at, first_name, gender_code, id, last_name, line, middle_name, nnuzb, phone_number, updated_at, uuid`
- **rename:** `city_code`→`district_code`, `state_code`→`region_code`
- **drop:** `password, position_code, ppn, district`
- **backfill:** `version`→0; `created_by_id`/`updated_by_id`→NULL; `username`←`nnuzb`; `created_at`/`updated_at` NULL→`now()`
- `users_organizations` — 1:1 · `user_roles` — 1:1 (ixtiyoriy; role.id crosswalk kerak bo'lishi mumkin)

### patient — **deyarli 1:1** (23 ustun bir xil), faqat `+version`→0
### pt_address / pt_affiliation
- **rename:** `city_code`→`district_code`, `state_code`→`region_code`
- **backfill:** `version`→0; `created_org_uuid`/`updated_org_uuid`→NULL
- `pt_affiliation.organization_uuid` — legacy'da BOR (to'g'ridan)
### pt_identifier — 1:1 + `version`→0, `created_org_uuid`/`updated_org_uuid`→NULL

---

## 2. form058 + fm058_location

### fm058_location
- **to'g'ridan (8):** `created_at, created_by_id, id, latitude, location, longitude, updated_at, updated_by_id`
- **drop:** `uuid` (target'da yo'q) · **backfill:** `version`→0

### form058
- **to'g'ridan (32):** `admission_date, approved_full_name, approved_org_uuid, cancel_reason, card_by_full_name, created_at, created_by_id, created_org_uuid, diagnosis_date, disease_cause, disease_date, disease_place_code, doc_send_date, epidemic_measures, first_visit_date, hospital_place_id, id, initial_report_date_time, journal_form_code, lab_confirmation, location_id, notifier_full_name, patient_id, receiver_organization_id, sender_organization_id, source, status, updated_at, updated_by_id, updated_org_uuid, uuid, visit_date`
- **rename:** `mkb10code`→`icd10_code`, `mkb10name`→`icd10_name`, `mkb10usage_limit`→`icd10_usage_limit`, `final_mkb10code`→`final_icd10_code`, `final_mkb10name`→`final_icd10_name`, `comment`→`form_comment`, `not_approve_comment`→`cancel_reason` *(agar `cancel_reason` bo'sh bo'lsa; ikkisi ham bo'lsa `cancel_reason` ustun)*, `canceled_by`→`canceled_by_id`
- **`source`:** `CASE WHEN upper(trim(source))='DMED' THEN 'DMED' ELSE 'MANUAL' END`
- **`status`:** `NEW→SENT, SENT→SENT, RECEIVED→ACCEPTED, CARD_LINKED→CARD_LINKED, CARD_REJECTED→ACCEPTED, APPROVED_PENDING→ACCEPTED, APPROVED→APPROVED, CANCELED→CANCELED, NOT_APPROVED→CANCELED`
- **backfill (target NOT NULL):**
  - `version`→0
  - `deleted`→false
  - `has_linked_cards` ← `EXISTS(SELECT 1 FROM public.card c WHERE c.form058_id=f.id)`
  - `icd10_code`/`icd10_name` ← `mkb10code`/`mkb10name`; NULL bo'lsa **qator skip + log**
  - ~~`disease_place` (NOT NULL)~~ → ustun `public2` dan olib tashlandi (Liquibase `20260904-1400-drop-form058-disease-place.xml`) — yangi modelda ishlatilmaydi, backfill kerak emas
  - ~~`patient_full_name`, `patient_nnuzb`, `patient_pinfl`, `patient_birth_date`, `patient_gender`, `patient_phone`~~ → hammasi `public2` dan olib tashlandi (o'sha migratsiyaning 02-changeset'i) — bemor ma'lumoti faqat `patient`/`pt_identifier`da (`30-patient.sql`), `form058` faqat `patient_id` orqali bog'lanadi
- **location_* (NULLable):** legacy manba yo'q → NULL (yoki `pt_address` PERMANENT dan — QAROR)
- **approve audit (NULLable):** `approved_by_id, approved_organization_id, approved_at` → NULL; `approved_full_name`/`approved_org_uuid` legacy'da bor
- **`assigned_card_id`** → `90-finalize.sql` da UPDATE
- **`source_integration_client_id`** → NULL

---

## 3. card + sub-tiplar

### card — **16 ustun bir xil**, faqat `+version`→0, `+deleted`→false
- `status` — **1:1** (legacy = yangi enum)
- `card_type` — 1:1 · `form058_id` / `form058_1_id` — to'g'ridan (lekin `form058_1` ko'chirilmagani uchun `form058_1_id` — legacy'da bo'lsa ham → **NULL**, chunki target `form058_1` bo'sh; CHECK `chk_card_exactly_one_form` uchun `form058_id` bo'lishi shart)
- **⚠️ `form058_id IS NULL AND form058_1_id IS NOT NULL`** legacy card lar: `form058_1` ko'chirilmagani uchun bu card lar **skip + log**

### card_users — 1:1

### card161 — **49 ustun bir xil**, `+version`→0
- **drop:** `activity_place_code, comment, infection_place_code`
- **target-only (NULL):** `animal_lab_test_result_code, animal_observation_result_code, animal_ownership_code, case_status_code, clinical_form, disease_severity_code, disease_source_info, emergency_prophylaxis_given, is_occupational_disease, main_probable_infection_factor_code`
  *(⚠️ `main_probable_infection_factor_code` — legacy `card161_main_probable_infection_factor` element jadvali bor; u → shu skalyar ustunga? yoki alohida? — QAROR)*
- **bola jadvallari (hammasi `+version`→0):**
  - `card161_contact_person, card161_environmental_lab_test, card161_infection_source_detail, card161_outbreak_measure, card161_prevent_measure, card161_risk_factors, card161_screened_group` — 1:1
  - `card161_environmental_source` — `quality_feedback_from_patient_and_others`→`quality_feedback`
  - `card161_infection_source` — **drop** `contact_type_code, duration, relation_code` (target'da yo'q)
  - `card161_indirection_causing` — 1:1 (element)
  - `card161_vaccination` — **struktura o'zgargan:** legacy `(card161_id, vaccination_id)` → target to'liq entity. `JOIN public.vaccination v ON v.id = cv.vaccination_id`, yangi `id` IDENTITY, `serial_number`←`v.serial_number`, `vaccination_name`←`v.vaccination_name`, `vaccination_date`←`v.vaccination_date`, `vaccination_verified_code`←`v.vaccination_verified_code`, `dose_volume`←`v.dose_volume`, `scheduled`←`v.scheduled`
  - `card161_main_probable_infection_factor` (legacy element) — target'da element jadval yo'q → **QAROR** (skalyar `main_probable_infection_factor_code` ga birinchi qiymat? yoki drop?)

### card174 / card175 / card205 / card_tube — BAZA jadvallari: **KATTA REMAP**
Nomlar deyarli disjoint. Juftlik taxminlari `20-card.md` §4a/§5a/§6a/§7a da (ishonch darajasi bilan).
SQL yozishда har jadval alohida ko'rib chiqiladi. Element jadvallar (`*_affected_animals`,
`*_disease_transmission_condition` va h.k.) — hammasi 1:1.

- `card174_infection_monitoring` — rename: `apply_date`→`application_date`, `confirm_date`→`confirmation_date`, `possible_date`→`possible_infection_date`, `possible_factor`→`possible_infection_factor`, `possible_location`→`possible_infection_location`; **drop** `contact_infection_date, vaccination_summary`; target-only `sequential_number`→NULL
- `card174_outbreak_control_measure` — 1:1 + version
- `card174_disinfection_info` / `card174_external_sample_test` / `card174_preventive_measure` — **target'da jadval YO'Q** → §"7-band" (ma'lumot ota'ga jamlanadimi yoki drop — QAROR)
- `card_tube_contact_monitoring` — `diag_date`→`diagnosis_date`, `notifier`→`notification_receiver`, `status_code`→`contact_status_code`, `work_study_place`→`workplace_or_study_place`
- `card_tube_infection_source` — `contact_type_code`→`tb_contact_code`, `duration`→`contact_duration`, `relation_code`→`relation_degree_code`
- `card_tube_tb_history` — `date`→`infection_date`, `location`→`infection_location`, `mkb_code`→`icd10_code`, `mkb_name`→`icd10_name`, `reg_group`→`registration_group`
- `card_tube_checkup_dates` / `card_tube_nutrition_type` / `card_tube_xray` — 1:1
- `card205_animal_bite_victim` → `card205_info_about_animal_bitten_people` (tasdiqlanган), `card205_other_bitten_animal` → `card205_info_bitten_animals`, `card205_other_bitten_people` → `card205_info_bitten_people` — nom juftliklari `20-card.md` §6b

---

## 4. act + sub-tiplar

### act — **19 ustun bir xil** (jumladan `subject_type, tin, institution_name/address/legal_address, lis_act_id, lis_attempt, lis_response, lis_sent_date`)
- **drop:** `subject_actual_address, subject_legal_address, subject_legal_name, subject_tin` (target'da yo'q)
- **rename:** `status`→`act_status` (mapping pastda)
- **target-only (NULL/default):** `assigned_by_id`→NULL, `result_comment`→NULL, `lis_last_error`→NULL, `deleted`→false, `version`→0
- **`act_status` mapping:** `NEW→NEW, NOT_VIEWED→NEW, IN_PROGRESS→IN_PROGRESS, PENDING→READY, SENT→SENT, FAILED→SEND_FAILED, RECEIVED→COMPLETED, COMPLETED→COMPLETED, ACT_ATTACHED→COMPLETED`
- **`act_type`:** `ACT155` qatorlari **skip** (target'da jadval yo'q). Qolgani 1:1.
- `act_users` — 1:1 (`ACT155` act'ларини filtrlab)

### act153 / act154 / act223 (JOINED subtype)
- **~34 ustun bir xil** (triplet'lar, `act_number`, `goal`, `sampling_purpose_*`, `package_type_*`, `special_condition_id`, `storage_delivery_condition_id`, `lis_organization_id`, `laboratory_address`, ...)
- **drop:** `institution_address, institution_name, institution_legal_address, lis_act_id, lis_response, tin, subject_type, position, position_id, participant_position, delivered_date` (+ act223: `full_name_of_participant, full_nameof_sampler, position_of_participant, position_of_sampler, reason_inspectoring_loinc, reason_of_inspectoring, sample_taken_date` — bular **eski nusxa**, yangi ekvivalentlari `*_full_name`/`*_position_uz`/`sampling_purpose_loinc`/`sample_taken_date_time` da bor)
- **target-only (backfill):** `sampler_identifier_type/value` ← legacy `tin` (`type='TIN'`, `value=tin::text`); `participant_identifier_type/value` → NULL
- act154 qo'shimcha drop: `serial_number`

### act156
- **13 bir xil** · **rename:** `full_nameof_sampler`→`full_name_of_sampler` · **drop:** `institution_legal_address, lis_act_id, subject_type`

### act224
- **15 bir xil** · **drop:** `institution_legal_address, lis_act_id, subject_type`

### act*_detail (JOINED)
- `act153_detail` — **32 bir xil** · drop 17 ta eski (`sampling_date, sampling_purpose_uz/ru/loinc, storage_conditions, delivery_conditions, conservation_methods, special_sampling_conditions, laboratory_address, lis_organization_id, purpose_id, special_condition_id, storage_condition_id, conservation_method_id, delivery_condition_id, sample_type, additional_info` — bular header'da) · `+version`→0
- `act154_detail` — **25 bir xil** · drop 14 ta eski · `+version`→0
- `act223_detail` — **20 bir xil** · drop 5 ta (`delivery_conditions_uz/ru, storage_conditions_uz/ru, supporting_documents_for_sampling`) · `+version`→0
- `act224_detail` — **9 bir xil** · `+version`→0
- `act156_group_detail` — **25 bir xil** · rename `wcseats`→`wc_seats` · `+version`→0
- `act156_kitchen_utensil` — **14 bir xil** · `+version`→0

---

## 5. Qarorlar (round 5 — ✅ hal qilinди)

1. ~~**`form058.disease_place`** (NOT NULL)~~ → **BEKOR**: ustun yangi modelda ishlatilmaydi, `public2` dan olib tashlandi (Liquibase `20260904-1400-drop-form058-disease-place.xml`). Backfill/skip kerak emas; `disease_place_code` (kod ustuni) o'z holicha qoladi.
2. **`form058.location_*_code` / `location_address`** → **NULL**. Joylashuv `fm058_location` (lat/long, `location_id` orqali) bilan ishlaydi, kod ustunlari ishlatilmaydi.
3. **`card161_main_probable_infection_factor`** (legacy element) → target skalyar `card161.main_probable_infection_factor_code` ga **birinchi** `catalog_code`. Bittadan ko'p bo'lsa — log.
4. **card174 bola jadvallari** (`disinfection_info`, `external_sample_test`, `preventive_measure`) → target'da yangi qanday bo'lsa shунday; legacy'da ma'lumot bo'lsa `card174` ota ustunlariga ko'chiriladi (birinchi qator; ko'p bo'lsa log):
   - `disinfection_info` → `card174.{execution_control_results, disinfected_factor_amount, disinfection_date, location_of_event}`
   - `external_sample_test` → `card174.{test_sample_count, test_date, testing_method, test_result}` (`specimen` → target yo'q, drop)
   - `preventive_measure` → `card174.precautionary_measures` (matn birlashtirib)
5. **card174/175/205/tube baza** — SQL paytida jadval-jadval.
6. **`user_roles`** → **ko'chirilmaydi** (umuman).
