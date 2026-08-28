# Mapping: `act` va sub-tiplari

Manba qoidalar: `00-overview.md`. **NN** = yangi `NOT NULL`, **⚠️** = domen tasdig'i.

`act` — JOINED inheritance (`act153.id → act.id` va h.k.). `card` **dan keyin** ko'chiriladi
(`act.card_id → card.id`).

**Qaror:** `act_type = 'ACT155'` qatorlari (`act`, `act155`, `act155_detail`, va shu
act'larga ishora qiluvchi `act_users`) **umuman ko'chirilmaydi**.

---

## 1. `public.act` → `public2.act`

| legacy | yangi | transform |
|---|---|---|
| `id` | `id` | saqlanadi |
| — | `version` NN | `0` |
| `created_at`/`updated_at` | bir xil NN | `timestamptz` (00 §3) |
| `created_by_id`/`updated_by_id` | bir xil | to'g'ridan |
| `created_org_uuid` | `created_org_uuid` **NN** | to'g'ridan; NULL → ⚠️ |
| `updated_org_uuid` | `updated_org_uuid` | to'g'ridan |
| `uuid` | `uuid` NN | to'g'ridan |
| `card_id` | `card_id` | to'g'ridan (nullable) |
| `status` | `act_status` NN | mapping — §3 |
| `act_type` | `act_type` NN | 1:1 (`ACT153/154/156/223/224`); `ACT155` — filtrlanadi |
| — | `assigned_by_id` | `NULL` (legacy `act` da yo'q) |
| — | `result_comment` | `NULL` |
| — | `subject_type` | `NULL` (qaror: ko'chirilmaydi) |
| — | `tin` | `NULL` (legacy `act` base'da yo'q; subtype'larда bor — §4) |
| — | `institution_name` / `institution_address` / `institution_legal_address` | `NULL` (qaror) |
| — | `lis_attempt` | `0` (DDL default) |
| — | `lis_sent_date` / `lis_act_id` / `lis_response` / `lis_last_error` | `NULL` |
| — | `deleted` NN | `false` |
| — | `deleted_at` / `deleted_by_id` / `delete_reason` | `NULL` |

---

## 2. `public.act_users` → `public2.act_users`

1:1: `(act_id, user_id)`. **Filtr:** `act_id` `ACT155` act'ga tegishli bo'lsa — tashlanadi:
```
INSERT INTO public2.act_users
SELECT au.* FROM public.act_users au
JOIN public.act a ON a.id = au.act_id
WHERE a.act_type <> 'ACT155';
```

---

## 3. `status` → yangi `ActStatus`  ⚠️ QAROR

Legacy: `NEW, IN_PROGRESS, COMPLETED, NOT_VIEWED, ACT_ATTACHED`
Yangi: `NEW, IN_PROGRESS, READY, SENT, SEND_FAILED, COMPLETED`

| legacy | → taklif | izoh |
|---|---|---|
| `NEW` | `NEW` | 1:1 |
| `IN_PROGRESS` | `IN_PROGRESS` | 1:1 |
| `COMPLETED` | `COMPLETED` | 1:1 |
| `NOT_VIEWED` | `NEW` | ⚠️ "ko'rilmagan" ≈ yangi tayinlangan |
| `ACT_ATTACHED` | `COMPLETED` | ⚠️ akt biriktirilган = yakunlangan? Yoki `READY`? |

Yangi `READY` / `SENT` / `SEND_FAILED` — LIS integratsiyasiga oid, legacy'да yo'q.

---

## 4. Subtiplar

Barcha `*_detail` / bola jadvallariga `version` + `timestamptz` qo'shiladi,
parent FK (`act15x_id`) saqlanadi.

**Muhim umumiy naqsh:** yangi sxemada legacy `act15x_detail` dagi ba'zi
maydonlar **header (`act15x`) ga ko'tarilган** (masalan `laboratory_address`,
`sampling_purpose*`, `storage/delivery conditions`, `organization_id →
lis_organization_id`). Ya'ni bitta `act153` uchun bu qiymatlar legacy'да
har `detail` qatorида takrorlanган — yangi'да bir marta header'да.
→ Migratsiyaда: header ustunlarini legacy `detail` dan (birinchi qator yoki
eng ko'p uchraydigan qiymat) olib to'ldirish kerak. **⚠️ QAROR:** qaysi
detail qatoridan?

**Reference triplet (`*_id` + `*_uz` + `*_ru`):** legacy'да faqat matn yoki
`*_loinc` bor. **⚠️ QAROR (har triplet uchun bir xil):**
(a) `*_id = NULL`, `*_uz = <legacy matn>`, `*_ru = NULL` — matnни saqlab qolish; yoki
(b) `LEFT JOIN public2.ref_catalog` bilan `*_id` + nomlarni to'ldirishga urinish.

### 4a. `act153` + `act153_detail`

**`public.act153` → `public2.act153`:**

| legacy | yangi | izoh |
|---|---|---|
| `id` | `id` | saqlanadi |
| `sampling_documents` | `sampling_documents` | 1:1 |
| `sampler_full_name` | `sampler_full_name` | 1:1 |
| `participant_full_name` | `participant_full_name` | 1:1 |
| `additional_info` | `additional_info` | 1:1 |
| `"position"` | `sampler_position_uz` (+ `sampler_position_id`=NULL) | ⚠️ triplet |
| `participant_position` | `participant_position_uz` (+ `_id`=NULL) | ⚠️ triplet |
| `tin` (integer) | `sampler_identifier_type='TIN'`, `sampler_identifier_value = tin::text` | ⚠️ tasdiqlash |
| `loinc` (text) | `sampling_purpose_loinc` (varchar100) | ⚠️ |
| `document_send_date` | — | ⚠️ yangi'да yo'q — tashlanadimi? |
| `hospitalization_date` | — | ⚠️ yangi'да yo'q |
| `sample_object_name` | — | ⚠️ yangi'да yo'q (detail'даги `object_name`?) |
| — | `act_number` | ⚠️ manba? (akt raqami) |
| — | `activity_type_code`, `goal`, `sample_taken_date_time`, `delivered_date_time` | ⚠️ legacy `act153_detail.sampling_date` / `sampling_purpose` dan header'ga? |
| — | `purpose_id`+`sampling_purpose_uz/ru`, `special_condition_id`+triplet, `storage_delivery_condition_id`+triplet, `package_type_id`+triplet, `conservation_method_id`+triplet | legacy `act153_detail` (`sampling_purpose`, `special_sampling_conditions`, `storage_conditions`, `delivery_conditions`, `conservation_methods`) dan — ⚠️ detail→header |
| — | `lis_organization_id` | legacy `act153_detail.organization_id` dan — ⚠️ detail→header |
| — | `laboratory_address` | legacy `act153_detail.laboratory_address` dan — ⚠️ detail→header |
| — | `sampler_identifier_type/value`, `participant_identifier_type/value` | `tin` dan (yuqorida) |

**`public.act153_detail` → `public2.act153_detail`:**

| legacy | yangi | izoh |
|---|---|---|
| `id`, `created_at`, `created_by_id`, `updated_at`, `updated_by_id`, `uuid` | bir xil | `+version`, `timestamptz` |
| `act153_id` | `act153_id` NN | to'g'ridan |
| `object_code` | `object_code` | 1:1 |
| `object_name` | ⚠️ | yangi'да `object_type_id`+? — ⚠️ triplet yoki `object_code` |
| `address` | `address` | 1:1 |
| `sample_location` | `sample_location` | 1:1 |
| `sample_type` | `sample_type_uz` (+`sample_type_id`=NULL) | ⚠️ triplet |
| `sample_volume` | `sample_volume` (+`sample_volume_unit`) | ⚠️ birlik qayerdан? |
| `sampling_depth` | `sampling_depth` (+`depth_unit`) | ⚠️ birlik |
| `distance_from_shore` | `distance_from_shore` (+`_unit`) | ⚠️ birlik |
| `water_temperature` | `water_temperature` | 1:1 |
| `weather_at_sampling` | `weather_at_sampling` | 1:1 |
| `additional_info` | ⚠️ | yangi detail'да yo'q — header'га? |
| `sampling_date` | ⚠️ | header `sample_taken_date_time` ga? |
| `sampling_purpose` / `sampling_purpose_loinc` | ⚠️ | header'га |
| `delivery_conditions` / `storage_conditions` / `special_sampling_conditions` / `conservation_methods` | ⚠️ | header'га |
| `laboratory_address` / `organization_id` | ⚠️ | header'га |
| `sample_name` | ⚠️ | yangi detail'да yo'q |
| `sample_type` | `sample_type_uz` | ⚠️ |
| — | `research_type_id/uz/ru`, `category_id/uz/ru`, `item_type_id/uz/ru`, `object_type_id` | ⚠️ manba? yangi klassifikatsiya — legacy'да yo'q bo'lsa NULL |
| — | `sample_qt_unit` | ⚠️ |

### 4b. `act154` + `act154_detail`

**`public.act154` → `public2.act154`:**

| legacy | yangi | izoh |
|---|---|---|
| `id` | `id` | saqlanadi |
| `title` | `title` | 1:1 |
| `additional_info` | `additional_info` | 1:1 |
| `name_of_object` | ⚠️ | yangi'да yo'q — `act154` da `title`? yoki detail'ga? |
| `object_address` | ⚠️ | yangi'да yo'q |
| `sampler_full_name` | `sampler_full_name` | 1:1 |
| `"position"` | `sampler_position_uz` (+`_id`=NULL) | ⚠️ triplet |
| `serial_number` (bigint) | ⚠️ | yangi `act154` da yo'q — `act_number`? |
| `tin` (integer) | `sampler_identifier_type='TIN'` + `..._value` | ⚠️ |
| `document_send_date` | — | ⚠️ yangi'да yo'q |
| `hospitalization_date` | — | ⚠️ yangi'да yo'q |
| — | `act_number`, `activity_type_code`, `sample_taken_date_time`, `delivered_date_time`, `goal`, `document_confirm_sampling`, `manufacturing_company`, `manufacture_date`, `doc_number_of_taken_object`, `purpose_id`+triplet, `special_condition_id`+triplet, `storage_delivery_condition_id`+triplet, `package_type_id`+triplet, `lis_organization_id`, `laboratory_address` | ⚠️ ko'pi legacy `act154_detail` dan header'ga (`manufacturing_company`, `manufacture_date`, `doc_number_of_taken_object`, `document_confirm_sampling`, `purpose_of_sampling`, `laboratory_address`, `organization_id`, `package_type`, `storage_condition`, `delivery_term`) |

**`public.act154_detail` → `public2.act154_detail`:**

| legacy | yangi | izoh |
|---|---|---|
| `id`, audit, `uuid` | bir xil | `+version`, `tz` |
| `act154_id` | `act154_id` NN | to'g'ridan |
| `sample_name` | `sample_name` | 1:1 |
| `group_size` | `group_size` | 1:1 |
| `serial_number_of_group` | `serial_number_of_group` | 1:1 |
| `sample_weight` | `sample_weight` | 1:1 |
| `sample_volume` | `sample_volume` (+`sample_volume_unit`) | ⚠️ birlik |
| `shift_code` | `shift_code` | 1:1 |
| `note` | `note` | 1:1 |
| `delivery_term` / `laboratory_address` / `organization_id` / `manufacture_date` / `manufacturing_company` / `document_confirm_sampling` / `doc_number_of_taken_object` / `group_size` / `package_type` / `purpose_of_checking` / `purpose_of_sampling` / `sample_taken_date` / `sample_type` / `sampling_purpose_loinc` / `storage_condition` | ⚠️ | ko'pi header'ga ko'tarilган — §4 umumiy naqsh |
| — | `research_type_id/uz/ru`, `category_id/uz/ru`, `item_type_id/uz/ru` | ⚠️ yangi klassifikatsiya, legacy'да yo'q → NULL |
| — | `sample_qt_unit` | ⚠️ |

### 4c. `act156` + bola jadvallari — asosan tractable

**`public.act156` → `public2.act156`:**

| legacy | yangi | izoh |
|---|---|---|
| `id`, `title`, `tin`, `institution_name`, `institution_address`, `laboratory_address`, `sample_delivery_time`, `sample_taken_time`, `position_of_sampler`, `position_of_object_representative` | bir xil | 1:1 |
| `full_nameof_sampler` | `full_name_of_sampler` | typo tuzatildi |
| `full_name_of_object_representative` | `full_name_of_object_representative` | 1:1 |
| `organization_id` | `lis_organization_id` | nom o'zgardi |
| `document_send_date` | — | ⚠️ yangi'да yo'q |
| — | `activity_type_code` | ⚠️ manba yo'q → NULL |

**`public.act156_group_detail` → `public2.act156_group_detail`:** `wcseats` → **`wc_seats`**;
qolgan barcha `boolean` maydonlar 1:1. `+version`, `tz`.

**`public.act156_kitchen_utensil` → `public2.act156_kitchen_utensil`:** 1:1. `+version`, `tz`.

### 4d. `act223` + `act223_detail`

**`public.act223` → `public2.act223`:**

| legacy | yangi | izoh |
|---|---|---|
| `id` | `id` | saqlanadi |
| `additional_info` | `additional_info` | 1:1 |
| `full_nameof_sampler` | `sampler_full_name` | nom |
| `full_name_of_participant` | `participant_full_name` | nom |
| `position_of_sampler` | `sampler_position_uz` (+`_id`=NULL) | ⚠️ triplet |
| `position_of_participant` | `participant_position_uz` (+`_id`=NULL) | ⚠️ triplet |
| `reason_of_inspectoring` | `goal` yoki `activity_type_code` | ⚠️ |
| `reason_inspectoring_loinc` | `sampling_purpose_loinc` | ⚠️ |
| `sample_taken_date` | `sample_taken_date_time` | tip kengaydi |
| `tin` | `sampler_identifier_type='TIN'` + value | ⚠️ |
| `loinc` | `sampling_purpose_loinc` | ⚠️ (`reason_inspectoring_loinc` bilan to'qnashuv — ⚠️) |
| `institution_name` / `institution_address` | — | ⚠️ yangi `act223` da yo'q (act base'da ham `institution_*` bor lekin qaror bo'yicha ko'chirilmaydi) |
| `document_send_date` | — | ⚠️ yo'q |
| — | `act_number`, `supporting_documents_for_sampling`, `delivered_date_time`, `purpose_id`+triplet, `special_condition_id`+triplet, `storage_delivery_condition_id`+triplet, `package_type_id`+triplet, `lis_organization_id`, `laboratory_address` | ⚠️ legacy `act223_detail` dan header'ga (`supporting_documents_for_sampling`) va boshqa manba yo'q → NULL |

**`public.act223_detail` → `public2.act223_detail`:**

| legacy | yangi | izoh |
|---|---|---|
| `id`, audit, `uuid` | bir xil | `+version`, `tz` |
| `act223_id` | `act223_id` NN | to'g'ridan |
| `amount` | `amount` | 1:1 |
| `depth_of_obtained_area` | `depth_of_obtained_area` (+`depth_unit`) | ⚠️ birlik |
| `exact_location_point_sampling` | `exact_location_point_sampling` | 1:1 |
| `supporting_documents_for_sampling` | ⚠️ header `act223.supporting_documents_for_sampling` ga | detail→header |
| — | `research_type_id/uz/ru`, `category_id/uz/ru`, `item_type_id/uz/ru` | ⚠️ NULL |

### 4e. `act224` + child

**`public.act224` → `public2.act224`:**

| legacy | yangi | izoh |
|---|---|---|
| `id` | `id` | saqlanadi |
| `name_of_institution` | `name_of_institution` | 1:1 |
| `address_of_institution` | `address_of_institution` | 1:1 |
| `name_of_regulatory_acts` | `name_of_regulatory_acts` | 1:1 |
| `checking_fulfillment_of_requirements` | `checking_fulfillment_of_requirements` | 1:1 (tip kengaydi) |
| `full_name_of_epid_staff` / `position_of_epid_staff` | bir xil | 1:1 |
| `full_name_of_participant_epid` / `position_of_participant_epid` | bir xil | 1:1 |
| `full_name_of_participant` | `full_name_of_participant` | 1:1 |
| `recommended_activities` (scalar) | → `act224_detail` qatori sifatida | ⚠️ (§ pastda) |
| `execution_period` (scalar) | → `act224_detail.execution_period` | ⚠️ |
| `region_code` | — | ⚠️ yangi'да yo'q |
| `document_send_date` | — | ⚠️ yangi'да yo'q |
| — | `tin`, `institution_name`, `institution_address`, `activity_type_code` | ⚠️ manba yo'q → NULL (`name_of_institution` bor, `institution_name` alohida) |

**`public.act224_recommended_actions` → `public2.act224_detail`** (jadval nomi o'zgardi):

| legacy | yangi | izoh |
|---|---|---|
| `id`, audit, `uuid` | bir xil | `+version`, `tz` |
| `act224_id` | `act224_id` NN | to'g'ridan |
| `recommended_activity` | `recommended_activities` | nom (birlik→ko'plik) |
| `execution_period` | `execution_period` | 1:1 |

⚠️ **Qo'shimcha:** legacy `act224` da `recommended_activities` + `execution_period`
scalar maydonlar ham bor (child jadvaldan tashqari). Bularni ham `act224_detail` ga
qo'shimcha bir qator qilib kiritish kerakmi, yoki child jadval yetarlimi?

---

## 5. ⚠️ Ushbu modul uchun ochiq qarorlar

1. **`status` mapping:** `NOT_VIEWED → NEW`, `ACT_ATTACHED → COMPLETED` to'g'rimi? (§3)
2. **`act.created_org_uuid NOT NULL`** — legacy NULL qatorlar uchun manba.
3. **Reference triplet strategiyasi** (§4): matn saqlash (a) yoki `ref_catalog` join (b)?
4. **`tin` (subtype'lardagi)** → `*_identifier_type='TIN'` + `*_identifier_value` to'g'rimi? Kimning TIN'i — sampler yoki participant?
5. **Detail → header ko'tarilган maydonlar** (§4): qaysi `detail` qatoridan header qiymati olinadi (birinchi? MODE?)?
6. **`document_send_date`, `hospitalization_date`, `sample_object_name`, `name_of_object`, `object_address`, `region_code` (act224), act153/223 `institution_*`** — yangi sxemada mos ustun yo'q. Har biri: tashlanadimi yoki qayerga?
7. **`act153.loinc` va `act223.loinc` + `reason_inspectoring_loinc`** — bittasi `sampling_purpose_loinc` ga; ikkinchisi?
8. **yangi klassifikatsiya ustunlari** (`research_type_id/uz/ru`, `category_id/uz/ru`, `item_type_id/uz/ru`, `*_unit`) — legacy'да manba yo'q → hammasi NULL (tasdiqlash).
9. **`act224` scalar `recommended_activities`/`execution_period`** — child jadval bilan dublikatmi? (§4e)
