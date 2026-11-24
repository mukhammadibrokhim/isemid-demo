# Mapping: `card` va sub-tiplari

Manba qoidalar: `00-overview.md`. **NN** = yangi `NOT NULL`, **⚠️** = domen tasdig'i.

`card` — JOINED inheritance: avval `public2.card` qatori, keyin `card.card_type` ga
mos subtype qatori (**bir xil `id`**). Subtype bola jadvallari eng oxirida.

---

## 1. `public.card` → `public2.card`

| legacy | yangi | transform |
|---|---|---|
| `id` | `id` | saqlanadi |
| — | `version` NN | `0` |
| `created_at`/`updated_at` | bir xil NN | `timestamptz` |
| `created_by_id`/`updated_by_id` | bir xil | to'g'ridan |
| `created_org_uuid` | `created_org_uuid` **NN** | to'g'ridan; NULL → ⚠️ (`assigned_by` user org?) |
| `updated_org_uuid` | `updated_org_uuid` | to'g'ridan |
| `uuid` | `uuid` NN | to'g'ridan |
| `assigned_by_id` | `assigned_by_id` | to'g'ridan |
| `attached_user_comment` | `attached_user_comment` | to'g'ridan |
| `card_type` | `card_type` NN | 1:1 (`CARD161/CARD174/CARD175/CARD205/CARD_TUBE`) |
| `completed_date` | `completed_date` | to'g'ridan |
| `status` | `status` NN | **1:1** — legacy va yangi `CardStatus` bir xil (`NEW, IN_PROGRESS, COMPLETED, ACCEPTED_BY_USER, REJECTED_BY_USER, APPROVED, REJECTED`) |
| `supervisor_comment` | `supervisor_comment` | to'g'ridan |
| `form058_id` | `form058_id` | to'g'ridan |
| — | `form058_1_id` | `NULL` (qaror: 058-1 ko'chirilmaydi) |
| — | `deleted` NN | `false` |
| — | `deleted_at`/`deleted_by_id`/`delete_reason` | `NULL` |

**CHECK `chk_card_exactly_one_form`:** `(form058_id NOT NULL) XOR (form058_1_id NOT NULL)`.
Legacy `card.form058_id` NULL bo'lган qatorlar bo'lsa — CHECK buziladi. ⚠️ Legacy'da
`form058_id IS NULL` card lar bormi? Bo'lsa: tashlanadimi yoki ⚠️.

---

## 2. `public.card_users` → `public2.card_users`

1:1: `(card_id, user_id)` — o'zgarишsiz.

---

## 3. `card161` (JOINED subtype)

### 3a. `public.card161` → `public2.card161` — asosan bir xil nomlar

Quyidagilar **to'g'ridan** (nom bir xil): `id`, `area`, `area_condition_code`,
`caller_type`, `case_status_code`, `delivery_method_code`, `densely_populated`,
`detected_code`, `diagnosis_verified_code`, `disease_causing_factors`,
`disease_detected_date`, `district_code`, `epidemiological_exam_date`,
`epidemiologist`, `epidemiologist_assistant`, `estimated_infection_date_from`,
`estimated_infection_date_to`, `final_diagnosis_date`, `food_preparation`,
`food_storage`, `has_lice`, `has_other_insects`, `has_rodents`,
`home_stay_exclusion_reason_code`, `hospital_name`,
`important_causes_of_disease_code`, `infection_location_code`, `initial_symptoms`,
`is_infection_source_missing`, `is_resident`, `isolation_status`,
`late_admission_reason_code`, `liquid_waste_disposal_type_code`,
`living_condition_code`, `main_probable_infection_factor_code`, `number_of_people`,
`number_of_rooms`, `observation_end_date`, `outbreak_infection_code`,
`polyclinic_id`, `probable_infection_location_code`, `region_code`,
`residential_treatment_facility`, `room_condition_code`, `sanitary_maintenance`,
`sewerage_status`, `solid_waste_disposal_type_code`, `visited_objects_code`,
`water_supply_code`, `water_supply_status`, `yard_condition_code`.
(Type/uzunlik farqlari bor — `varchar(255)` → `varchar(64)` — qiymatlar sig'adi.)

| legacy | yangi | transform |
|---|---|---|
| `comment` (text) | — | **yangi'da YO'Q.** ⚠️ Tashlanadimi yoki `disease_source_info` ga? |
| `has_other_insects` NN | `has_other_insects` (nullable) | to'g'ridan |
| `has_rodents` NN | `has_rodents` (nullable) | to'g'ridan |
| `number_of_people` NN | `number_of_people` (nullable) | to'g'ridan |
| `number_of_rooms` NN | `number_of_rooms` (nullable) | to'g'ridan |
| — | `emergency_prophylaxis_given` | `NULL` (legacy manba yo'q) |
| — | `clinical_form` | `NULL` |
| — | `disease_severity_code` | `NULL` |
| — | `is_occupational_disease` | `NULL` |
| — | `disease_source_info` | `NULL` (yoki eski `comment`? ⚠️) |
| — | `animal_ownership_code` | `NULL` |
| — | `animal_observation_result_code` | `NULL` |
| — | `animal_lab_test_result_code` | `NULL` |

### 3b. `card161` bola jadvallari

`version`+`timestamptz` qo'shiladi, `card161_id` FK saqlanadi. Nomlar:

| legacy jadval | yangi jadval | holat |
|---|---|---|
| `card161_contact_person` | `card161_contact_person` | 1:1 (`address, age, full_name, immunization_status, job_type_and_location, restriction_measures`) |
| `card161_environmental_lab_test` | `card161_environmental_lab_test` | 1:1 (`examination_date, material, object_arthropods_animals, sample_quantity, test_type_and_result`) |
| `card161_environmental_source` | `card161_environmental_source` | `quality_feedback_from_patient_and_others` → **`quality_feedback`** (nom qisqardi); qolgani 1:1 |
| `card161_infection_source` | `card161_infection_source` | 1:1 (`contact_info_and_donor_residence, diagnosis_clinical_form_or_donor_status, full_name, test_result`) |
| `card161_infection_source_detail` | `card161_infection_source_detail` | 1:1 (`animal_type_code, infection_source_disease_period_code, infection_source_not_found_code, person_full_name`) |
| `card161_outbreak_measure` | `card161_outbreak_measure` | 1:1 (`conducted_at, conducted_location_code, drug_type, execution_monitoring_result, executors, preventive_measures_code`) |
| `card161_prevent_measure` | `card161_prevent_measure` | 1:1 (`dose, drug_name, lis_result, next_disease_time, notified_person, observation_result, received_date, result_time, series`) |
| `card161_risk_factors` | `card161_risk_factors` | 1:1 (`address_location, risk_factor_code, season_time`) |
| `card161_screened_group` | `card161_screened_group` | 1:1 (`contact_count, laboratory_test_conducted, prophylactic_address, required_prophylactic_substance, team_name, treated_with_prophylactic_substance`) |
| `card161_indirection_causing` (element) | `card161_indirection_causing` | 1:1 (`card161_id, catalog_code`) |
| — | `card161_emergency_prophylaxis` | yangi jadval, legacy manba **yo'q** → ko'chirilmaydi |
| — | `card161_injury_location` (element) | yangi, legacy manba yo'q → ko'chirilmaydi |

### 3c. `card161_vaccination` — struktura o'zgardi  ⚠️

- **Legacy:** `card161_vaccination(card161_id, vaccination_id)` — join jadval → `public.vaccination`.
- **Yangi:** `card161_vaccination` — to'liq entity (`dose_volume, scheduled, serial_number,
  vaccination_date, vaccination_name, vaccination_verified_code, card161_id`).

Ko'chirish: `public.card161_vaccination cv JOIN public.vaccination v ON v.id = cv.vaccination_id`
→ yangi `card161_vaccination` (yangi `id` — IDENTITY, legacy `vaccination.id` saqlanmaydi;
`serial_number` ← `v.serial_number`, `vaccination_name` ← `v.vaccination_name`,
`vaccination_verified_code` ← `v.vaccination_verified_code`, va h.k.).
⚠️ Bir `vaccination` bir necha `card161` ga bog'langan bo'lsa — dublikat qator yaratiladi (to'g'ri).
⚠️ `public.infection_monitoring_vaccination` (card174 tomon) — `20` §5c ga qarang.

---

## 4. `card174` (JOINED subtype) — KATTA REMAP  ⚠️

Legacy va yangi ustun nomlari deyarli butunlay boshqacha. To'liq yonma-yon jadval,
har biriga domen tasdig'i kerak.

### 4a. `public.card174` → `public2.card174` — taxminiy juftlik

| legacy | yangi (taxmin) | ishonch |
|---|---|---|
| `affected_animal_code` | `affected_animal_type_code` | yuqori |
| `affected_animal_count` | `affected_animal_count` | 1:1 |
| `animal_diag` | `animal_primary_diagnosis` | o'rta |
| `human_diag` | `human_primary_diagnosis` | o'rta |
| `arthropods_increased` | `blood_sucking_arthropods_increase` | o'rta |
| `rodents_increased` | `rodent_increase` | o'rta |
| `synanthropic_rodents` | `synanthropic_rodents_increase` | o'rta |
| `vectors_increased` | `vector_increase` | o'rta |
| `wild_rodents` | `wild_rodents_increase` | o'rta |
| `at_industry` | `including_industrial_conditions` | past ⚠️ |
| `confirmed_cases` / `reported_cases` / `reported_human_vet_date` | `including_identified` / `including_who_applied` / ? | past ⚠️ |
| `current_animal_infection_date` | `current_animal_infection_date` | 1:1 |
| `data_date` | `data_obtained_date` | yuqori |
| `derat_area` | `deratization_area` | yuqori |
| `derat_code` | `deratization_code` | yuqori |
| `disposal_date` | `animal_disposal_date` | yuqori |
| `disposal_method_code` | `animal_disposal_method_code` | yuqori |
| `epizootology_exists` | `epizootology_existence` | yuqori |
| `infected_humans` | `affected_humans` | o'rta |
| `infected_at_outbreak` | `affected_in_outbreak` | o'rta |
| `inspectors` | `inspectors` | 1:1 |
| `investigation_date` | `investigation_date` | 1:1 |
| `is_exotic_area` | `is_area_exotic` | yuqori |
| `isolation` | `isolation` | 1:1 |
| `last_case_year` | `last_disease_year` | yuqori |
| `meat_submission` | `meat_submission` | 1:1 |
| `mkb_code` / `mkb_name` | `icd10_code` / `icd10_name` | yuqori |
| `outbreak_location` | `outbreak_localization` yoki `location_of_event` | ⚠️ |
| `owner_addr` | `owner_address` | yuqori |
| `owner_name` | `animal_owner` | o'rta |
| `ownership_code` | `animal_ownership_code` | yuqori |
| `pathogen` | `pathogen_type` | yuqori |
| `quarantine_code` | `quarantine_type_code` | yuqori |
| `quarantine_start` / `quarantine_end` | `quarantine_start_date` / `quarantine_end_date` | yuqori |
| `reported_vet_date` | `report_to_veterinary_department_date` | o'rta |
| `stray_capture` | `stray_animal_capture` | yuqori |
| `treated_humans` | `treated_humans` | 1:1 |
| `treatment` | `treatment` | 1:1 |
| `wild_culling` | `wild_animal_culling` | yuqori |
| `action_taken` | `measure_taken` | o'rta |
| `additional_info` | `additional_measures_info` | o'rta ⚠️ |
| `derat_area`/`disposal_date`... | ... | |
| — | `animal_type`, `animal_owner`, `disinfected_factor_amount`, `disinfection_date`, `execution_control_results`, `precautionary_measures`, `serial_doc_number`, `test_date`, `test_result`, `test_sample_count`, `testing_method` | ⚠️ ba'zilari legacy `card174_external_sample_test` / `card174_disinfection_info` bola jadvallaridan kelishi mumkin (4c) |
| `reported_sanepid_date` | ? | ⚠️ yangi'da mos yo'q |

### 4b. `card174` element jadvallari — 1:1

`card174_affected_animals`, `card174_disease_factors`, `card174_disinfection_factors`,
`card174_elimination_method` — hammasi `(card174_id, catalog_code)` — **1:1**.

### 4c. `card174` bola jadvallari  ⚠️

| legacy | yangi | holat |
|---|---|---|
| `card174_infection_monitoring` | `card174_infection_monitoring` | nom o'zgarishlari: `apply_date`→`application_date`, `confirm_date`→`confirmation_date`, `contact_infection_date`→? , `possible_date`→`possible_infection_date`, `possible_factor`→`possible_infection_factor`, `possible_location`→`possible_infection_location`; `vaccination_summary` → **yangi'da YO'Q** ⚠️; yangi `sequential_number` ← ? |
| `card174_outbreak_control_measure` | `card174_outbreak_control_measure` | 1:1 (`event_conducted, lost_animals, meat_delivered, processed_area, processing_method_code, vaccinated_animals`) |
| `card174_disinfection_info` | **yangi'da jadval YO'Q** | ⚠️ `control_results`→`card174.execution_control_results`? `disinfected_count`→`card174.disinfected_factor_amount`? `disinfection_date`→`card174.disinfection_date`? `event_location`→`card174.location_of_event`? (1:1 bola → ota ustunlariga?) |
| `card174_external_sample_test` | **yangi'da jadval YO'Q** | ⚠️ `sample_count`→`card174.test_sample_count`, `specimen`→?, `test_date`→`card174.test_date`, `test_method`→`card174.testing_method`, `test_result`→`card174.test_result` (1:1 bola → ota?) |
| `card174_preventive_measure` | **yangi'da jadval YO'Q** | ⚠️ `measure_count/date/location/measures` → `card174.precautionary_measures` (matn)? yoki tashlanadi? |
| `infection_monitoring_vaccination` (join) | **yangi'da YO'Q** | ⚠️ `card174_infection_monitoring` ↔ `vaccination`. Yangi `infection_monitoring` da faqat matnli maydon yo'q. Batafsil vaksinatsiya yozuvlari **yo'qoladi** yoki matn sifatida jamlanadi? |

---

## 5. `card175` (JOINED subtype) — KATTA REMAP  ⚠️

### 5a. `public.card175` → `public2.card175` — taxminiy juftlik

| legacy | yangi (taxmin) | ishonch |
|---|---|---|
| `animal_diagnosis_code` | `checking_diagnosis_code` | past ⚠️ |
| `animal_obs_result_code` | `observation_result_of_animals_code` | yuqori |
| `application_place` | `place_of_application` | yuqori |
| `clinical_form` | `clinical_form` | 1:1 |
| `diagnosis_lab_confirmed` | `diagnosis_confirmed_code` | o'rta |
| `discharge_date` | `date_of_discharge_from_hospital` | yuqori |
| `discharge_status_code` | `hospital_discharge_status_code` | yuqori |
| `emergency_prophylaxis_info` | `prevention_and_aid_code` | past ⚠️ |
| `epi_check_time` | `time_of_epidemiological_investigation` | yuqori |
| `final_diagnosis_date` | `date_of_final_diagnosis` | yuqori |
| `illness_date` | `date_of_illness` | yuqori |
| `illness_severity_code` | `severity_of_illness_code` | yuqori |
| `job_relation_code` | `relevance_of_disease_to_profession_code` | o'rta |
| `late_hospitalization_reason` | `reason_of_late_hospitalization_code` | yuqori |
| `medicine_name` | `name_of_medicine` | yuqori |
| `medicine_qty` | `quantity_of_medicine` | yuqori |
| `medicine_series` | `serial_number` | o'rta ⚠️ |
| `no_home_stay_reason` | `reason_of_leaving_home_code` | o'rta |
| `origin_place_code` | `where_patient_come_code` | past ⚠️ |
| `pathogen_type` | `pathogen_type` | 1:1 |
| `patient_detected_code` | `patient_identified_code` | yuqori |
| `patient_source_code` | `patient_come_code` | o'rta ⚠️ |
| `possible_infection_place` | `brief_epidemiological_comment` | past ⚠️ |
| `primary_diagnosis` | `initial_diagnosis_code` | o'rta ⚠️ |
| `spreader_owner_code` | `owner_of_disease_spreader_code` | yuqori |
| `spreader_type_code` | `disease_spreader_type_code` | yuqori |
| `transport_type` | `transport_type_code` | yuqori |
| `urgent_diagnosis_date` | `date_of_diagnosis_of_emergency_day` | o'rta |
| `vaccination_date` | `date_of_vaccination` | yuqori |
| `vaccination_info` | `information_about_last_vaccination` | o'rta |
| `vaccination_plan` | `vaccination_count` | past ⚠️ |
| — | `checking_diagnosis_code`, `information_about_vaccination_code`, `patient_identified_code` va h.k. | ⚠️ |

### 5b. `card175` element jadvallari — 1:1

`card175_disease_transmission_condition`, `card175_part_of_injury`,
`card175_pathogen_main_factor`, `card175_taken_measures_from_residence` —
hammasi `(card175_id, catalog_code)` — **1:1**.

---

## 6. `card205` (JOINED subtype) — KATTA REMAP  ⚠️

### 6a. `public.card205` → `public2.card205` — taxminiy juftlik

| legacy | yangi (taxmin) | ishonch |
|---|---|---|
| `animal_age` | `age_of_animal` | yuqori |
| `animal_arrival_date` | `when_animal_appeared` | o'rta |
| `animal_breed` | `breed_of_animal` | yuqori |
| `animal_color` | `colour_of_animal` | yuqori |
| `animal_condition_code` | `condition_of_animal_code` | yuqori |
| `animal_hold_code` | `animal_conservation_code` | o'rta |
| `animal_origin` | `where_animal_comes_from` | o'rta |
| `animal_owner_name` | `full_name_of_animal_owner` | yuqori |
| `animal_signs` | `individual_signs_of_animal` | yuqori |
| `bite_address` | `address_of_bite_occurrence` | yuqori |
| `bite_date` | `date_of_bite_occurrence` | yuqori |
| `epi_survey_date` | `epidemiological_observation_date` | yuqori |
| `extra_info` | `additional_information` | yuqori |
| `first_clinic_date` | `date_of_treatment_preventive_institution` | o'rta |
| `first_clinic_name` | `name_of_treatment_preventive_institution` | yuqori |
| `mkb_code` / `mkb_name` | `icd10_code` / `icd10_name` | yuqori |
| `owner_compliance_code` | `dog_owner_compliance_code` | yuqori |
| `post_mortem_date_time` | `date_time_of_feather_taken` | past ⚠️ |
| `vet_alert_date` | `veterinary_emergency_information_date` | o'rta |
| `vet_cert_date1` / `vet_cert_date2` | `issue_date_of_first_certificate` / `issue_date_of_secondary_certificate` | yuqori |
| `vet_cert_num1` / `vet_cert_num2` | `certificate_number_of_first_vet_results` / `certificate_number_of_second_vet_results` | yuqori |
| `vet_certificate_info` | ? | ⚠️ |
| `vet_status_info` | `pet_registered_vet_department` | past ⚠️ |
| `victim_position_code` | `position_of_bitten_victim_code` | yuqori |

### 6b. `card205` bola jadvallari — nom VA semantika almashgan  ⚠️ (yuqori xavf)

| legacy | yangi | izoh |
|---|---|---|
| `card205_animal_bite_victim` | `card205_info_about_animal_bitten_people` (?) | legacy: `animal_category_code, animal_type, apartment_number, city_code→region?, house_number, neighborhood_code, owner_address, owner_full_name, state_code, street`. Yangi: `address_of_animal_bitten_owner, animal_category_code, animal_type, apartment_number, district, full_name_of_animal_bitten_owner, house_number, neighborhood, region, street`. ⚠️ `owner_*` ↔ `*_animal_bitten_owner`; `city_code`→`district`, `state_code`→`region` |
| `card205_other_bitten_animal` | `card205_info_bitten_animals` | legacy: `animal_category_code, bite_date_time, bite_location`. Yangi: `bitten_animal_category_code, bitten_date_time, where_animal_bitten`. ⚠️ nom o'zgarishi 1:1 taxmin |
| `card205_other_bitten_people` | `card205_info_bitten_people` | legacy: `apartment_number, birth_date, bitten_date_time, city_code, first_name, gender, house_number, last_name, living_address, middle_name, neighborhood_code, state_code, street`. Yangi: `apartment_number, birth_date, bitten_date, district, first_name, gender, house_number, last_name, living_address, middle_name, neighborhood, region, street`. ⚠️ `bitten_date_time`→`bitten_date`; `city_code`→`district`, `state_code`→`region` |

**⚠️ Domen tasdig'i:** legacy 3 jadval ↔ yangi 3 jadval juftligi to'g'rimi? Ayniqsa
`animal_bite_victim` (asosiy jabrlanuvchi) → `info_about_animal_bitten_people`
(hayvon egasi?) — semantik farq bo'lishi mumkin. `docs/legacy-data-migration.md` §3d ga qarang.

---

## 7. `card_tube` (JOINED subtype) — KATTA REMAP (~50 ustun qayta nomlangan)  ⚠️

### 7a. `public.card_tube` → `public2.card_tube` — taxminiy juftlik

| legacy | yangi | ishonch |
|---|---|---|
| `adults` | `adult_count` | yuqori |
| `children_under14` | `children_under_14_count` | yuqori |
| `child_roommates` | `roommate_children_count` | o'rta |
| `roommates` | `roommates_count` | yuqori |
| `household_contacts` | `household_contact` | yuqori |
| `total_contacts` | `total_contact` | yuqori |
| `teenagers` | `teenager_count` | yuqori |
| `pregnant_women` | `pregnant_women_count` | yuqori |
| `food_or_child_workers` | `food_childcare_worker_count` | yuqori |
| `family_rooms` | `family_room_count` | yuqori |
| `rooms` | `room_count` | yuqori |
| `floors` | `floor_count` | yuqori |
| `spittoon_count` | `spittoon_count` | 1:1 |
| `room_area` | `room_area_sq_m` | yuqori |
| `total_area` | `total_area_sq_m` | yuqori |
| `isolated_room_area` | `isolated_room_area_sq_m` | yuqori |
| `clinic_notification_date` | `info_sent_to_clinic_date` | yuqori |
| `workplace_notification_date` | `info_sent_to_workplace_date` | yuqori |
| `dg_mkb10code` / `dg_mkb10name` | `dg_icd10_code` / `dg_icd10_name` | yuqori |
| `mkb10code` / `mkb10name` | `icd10_code` / `icd10_name` | yuqori |
| `mkb10name` | `icd10_name` | yuqori |
| `firstmbdate` | `first_mb_date` | yuqori |
| `mb_reg_date` | `mb_patient_reg_date` | yuqori |
| `mb_detection_method` | `mb_detection_method` | 1:1 |
| `improvement_date` | `housing_improvement_date` | yuqori |
| `dispensary_start_date` | `primary_dispensary_date` | o'rta |
| `dispensary_group` | `dispensary_group` | 1:1 |
| `dispensary_id` (bigint) | `dispensary_id` (varchar64) | **tip o'zgardi** → `::text` |
| `dispensary_id` | ... | ⚠️ |
| `notified_to` | `received_by` | past ⚠️ |
| `income_level_code` | `family_budget_code` | o'rta ⚠️ |
| `relation_code` | `kinship_degree_code` | o'rta |
| `heating_code` | `heating_type_code` | yuqori |
| `sewerage_code` | `sewerage_type_code` | yuqori |
| `sputum_disposal_code` | `sputum_disposal_method_code` | yuqori |
| `renovation_needed_code` | `needs_renovation_code` | yuqori |
| `hygiene_assessment_code` | `sanitary_hygienic_assessment_code` | yuqori |
| `gets_disinfectant_code` | `receives_disinfectant_code` | yuqori |
| `disinfectant_amount` | `disinfectant_amount_per_month` | o'rta |
| `follows_cough_etiquette` | `follows_cough_precaution` | yuqori |
| `spittoon_at_home` / `spittoon_at_work` / `spittoon_in_public` | `uses_spittoon_at_home` / `_at_work` / `_in_pub_place` | yuqori |
| `housing_diff` | `previous_housing_difference` | yuqori |
| `visit_interval_unit` / `visit_interval_value` | `visit_interval_unit` / `visit_interval_value` | 1:1 |
| `visit_interval_unit` (2-marta?) | `ftb_visit_interval_unit` / `ftb_visit_interval_val` | ⚠️ yangi'da 2 juft bor |
| `vaccine_date` / `vaccine_name` / `vaccine_series` | `vaccination_date` / `vaccination_name` / `serial_number` | o'rta |
| `dose_volume` NN / `unit_in_dose` NN / `scheduled` NN | `dose_volume` / ? / `scheduled` | ⚠️ `unit_in_dose` yangi'da mos yo'q |
| `dismissal_date` / `discharge_date` / `start_date` / `end_date` / `retreatment_*` | bir xil nomlar | to'g'ridan |
| `habitability_code`, `harmful_habit_code`, `home_stay_reason_code`, `housing_condition_code`, `recovery_plan_code`, `work_condition_code`, `has_elevator`, `has_spittoon`, `has_ventilation` | bir xil | to'g'ridan |

### 7b. `card_tube` bola jadvallari  ⚠️

| legacy | yangi | izoh |
|---|---|---|
| `card_tube_checkup_dates` (element) | `card_tube_checkup_dates` | 1:1 (`card_tube_id, checkup_date`) |
| `card_tube_nutrition_type` (element) | `card_tube_nutrition_type` | 1:1 (`card_tube_id, code`) |
| `card_tube_contact_monitoring` | `card_tube_contact_monitoring` | `diag_date`→`diagnosis_date`, `notifier`→`notification_receiver`, `status_code`→`contact_status_code`, `work_study_place`→`workplace_or_study_place`; qolgani ~1:1 |
| `card_tube_infection_source` | `card_tube_infection_source` | `contact_type_code`→`tb_contact_code`, `duration`→`contact_duration`, `relation_code`→`relation_degree_code` |
| `card_tube_tb_history` | `card_tube_tb_history` | `date`→`infection_date`, `location`→`infection_location`, `mkb_code`/`mkb_name`→`icd10_code`/`icd10_name`, `reg_group`→`registration_group` |
| `card_tube_xray` | `card_tube_xray` | 1:1 (`result, xray_date, xray_place`) |

---

## 8. ⚠️ Ushbu modul uchun ochiq qarorlar

1. **card161.comment** (text) — tashlanadimi yoki `disease_source_info` ga?
2. **card174** — 4a jadvalidagi "past/o'rta ishonch" juftliklar; `reported_sanepid_date` qayerga?
3. **card174 bola jadvallari** (`disinfection_info`, `external_sample_test`, `preventive_measure`, `infection_monitoring_vaccination`) — yangi'da jadval yo'q. Ma'lumot ota ustunlariga jamlanadimi yoki yo'qoladi? (4c)
4. **card174_infection_monitoring.vaccination_summary** — yangi'da yo'q; `infection_monitoring_vaccination` batafsil yozuvlari nima bo'ladi?
5. **card175** — 5a "past ishonch" juftliklar (`primary_diagnosis`→`initial_diagnosis_code` — kod jadvalimi yoki matn?).
6. **card205 bola jadvallari** — legacy 3 ↔ yangi 3 juftligi va `animal_bite_victim` semantikasi (6b). ⚠️ eng xavfli.
7. **card205.vet_certificate_info** — yangi'da mos ustun yo'q.
8. **card_tube** — `unit_in_dose`, ikkinchi `visit_interval` juftligi, `dispensary_id` bigint→varchar.
9. **card.form058_id IS NULL** legacy qatorlar (CHECK buziladi).
10. **`*_org_uuid NOT NULL`** — legacy NULL qatorlar uchun manba.
