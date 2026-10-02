-- =====================================================================
-- 51-card161.sql  —  card161 (JOINED subtype, id = card.id) + bola jadvallari
-- Mapping: docs/legacy-migration/02-mapping-5434.md §3
-- Bog'liqlik: 50-card bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

-- ---- card161 (subtype) ---------------------------------------------
-- 49 ustun bir xil. Drop: activity_place_code, comment, infection_place_code.
-- Target-only -> NULL (9 ta), + main_probable_infection_factor_code <- element jadval (birinchi qiymat).
INSERT INTO public2.card161 (
    id, area, area_condition_code, caller_type, delivery_method_code, densely_populated,
    detected_code, diagnosis_verified_code, disease_causing_factors, disease_detected_date,
    district_code, epidemiological_exam_date, epidemiologist, epidemiologist_assistant,
    estimated_infection_date_from, estimated_infection_date_to, final_diagnosis_date,
    food_preparation, food_storage, has_lice, has_other_insects, has_rodents,
    home_stay_exclusion_reason_code, hospital_name, important_causes_of_disease_code,
    infection_location_code, initial_symptoms, is_infection_source_missing, is_resident,
    isolation_status, late_admission_reason_code, liquid_waste_disposal_type_code,
    living_condition_code, number_of_people, number_of_rooms, observation_end_date,
    outbreak_infection_code, polyclinic_id, probable_infection_location_code, region_code,
    residential_treatment_facility, room_condition_code, sanitary_maintenance, sewerage_status,
    solid_waste_disposal_type_code, visited_objects_code, water_supply_code, water_supply_status,
    yard_condition_code,
    main_probable_infection_factor_code,
    case_status_code, clinical_form, disease_severity_code, disease_source_info,
    emergency_prophylaxis_given, is_occupational_disease, animal_ownership_code,
    animal_observation_result_code, animal_lab_test_result_code
)
SELECT
    l.id, l.area, l.area_condition_code, l.caller_type, l.delivery_method_code, l.densely_populated,
    l.detected_code, l.diagnosis_verified_code, l.disease_causing_factors, l.disease_detected_date,
    l.district_code, l.epidemiological_exam_date, l.epidemiologist, l.epidemiologist_assistant,
    l.estimated_infection_date_from, l.estimated_infection_date_to, l.final_diagnosis_date,
    l.food_preparation, l.food_storage, l.has_lice, l.has_other_insects, l.has_rodents,
    l.home_stay_exclusion_reason_code, l.hospital_name, l.important_causes_of_disease_code,
    l.infection_location_code, l.initial_symptoms, l.is_infection_source_missing, l.is_resident,
    l.isolation_status, l.late_admission_reason_code, l.liquid_waste_disposal_type_code,
    l.living_condition_code, l.number_of_people, l.number_of_rooms, l.observation_end_date,
    l.outbreak_infection_code, l.polyclinic_id, l.probable_infection_location_code, l.region_code,
    l.residential_treatment_facility, l.room_condition_code, l.sanitary_maintenance, l.sewerage_status,
    l.solid_waste_disposal_type_code, l.visited_objects_code, l.water_supply_code, l.water_supply_status,
    l.yard_condition_code,
    (SELECT m.catalog_code FROM public.card161_main_probable_infection_factor m
      WHERE m.card161_id = l.id ORDER BY m.catalog_code LIMIT 1),
    NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
FROM public.card161 l
JOIN public2.card c ON c.id = l.id;      -- faqat ko'chirilган card lar

-- ko'p qiymatli main_probable_infection_factor — ma'lumot yo'qolgani haqida log
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card161_main_probable_infection_factor', l.id,
       'element jadval -> skalyar: faqat 1-qiymat ko''chirildi',
       string_agg(m.catalog_code, ', ')
FROM public.card161 l
JOIN public.card161_main_probable_infection_factor m ON m.card161_id = l.id
JOIN public2.card161 c161 ON c161.id = l.id
GROUP BY l.id
HAVING count(*) > 1;

-- ---- bola jadvallari (hammasi +version=0, +tz) ---------------------

INSERT INTO public2.card161_contact_person
    (id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     address, age, full_name, immunization_status, job_type_and_location, restriction_measures, card161_id)
SELECT x.id, 0,
    x.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.created_by_id,
    x.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.updated_by_id, x.uuid,
    x.address, x.age, x.full_name, x.immunization_status, x.job_type_and_location, x.restriction_measures, x.card161_id
FROM public.card161_contact_person x
JOIN public2.card161 c ON c.id = x.card161_id;

INSERT INTO public2.card161_environmental_lab_test
    (id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     examination_date, material, object_arthropods_animals, sample_quantity, test_type_and_result, card161_id)
SELECT x.id, 0,
    x.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.created_by_id,
    x.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.updated_by_id, x.uuid,
    x.examination_date, x.material, x.object_arthropods_animals, x.sample_quantity, x.test_type_and_result, x.card161_id
FROM public.card161_environmental_lab_test x
JOIN public2.card161 c ON c.id = x.card161_id;

-- quality_feedback_from_patient_and_others -> quality_feedback
INSERT INTO public2.card161_environmental_source
    (id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     collection_location, collection_time, food_and_water_source_types, quality_feedback,
     storage_conditions, usage_location, usage_time, card161_id)
SELECT x.id, 0,
    x.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.created_by_id,
    x.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.updated_by_id, x.uuid,
    x.collection_location, x.collection_time, x.food_and_water_source_types,
    x.quality_feedback_from_patient_and_others,
    x.storage_conditions, x.usage_location, x.usage_time, x.card161_id
FROM public.card161_environmental_source x
JOIN public2.card161 c ON c.id = x.card161_id;

-- drop: contact_type_code, duration, relation_code
INSERT INTO public2.card161_infection_source
    (id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     contact_info_and_donor_residence, diagnosis_clinical_form_or_donor_status, full_name, test_result, card161_id)
SELECT x.id, 0,
    x.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.created_by_id,
    x.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.updated_by_id, x.uuid,
    x.contact_info_and_donor_residence, x.diagnosis_clinical_form_or_donor_status, x.full_name, x.test_result, x.card161_id
FROM public.card161_infection_source x
JOIN public2.card161 c ON c.id = x.card161_id;

INSERT INTO public2.card161_infection_source_detail
    (id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     animal_type_code, infection_source_disease_period_code, infection_source_not_found_code, person_full_name, card161_id)
SELECT x.id, 0,
    x.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.created_by_id,
    x.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.updated_by_id, x.uuid,
    x.animal_type_code, x.infection_source_disease_period_code, x.infection_source_not_found_code, x.person_full_name, x.card161_id
FROM public.card161_infection_source_detail x
JOIN public2.card161 c ON c.id = x.card161_id;

INSERT INTO public2.card161_outbreak_measure
    (id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     conducted_at, conducted_location_code, drug_type, execution_monitoring_result, executors, preventive_measures_code, card161_id)
SELECT x.id, 0,
    x.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.created_by_id,
    x.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.updated_by_id, x.uuid,
    x.conducted_at, x.conducted_location_code, x.drug_type, x.execution_monitoring_result, x.executors, x.preventive_measures_code, x.card161_id
FROM public.card161_outbreak_measure x
JOIN public2.card161 c ON c.id = x.card161_id;

INSERT INTO public2.card161_prevent_measure
    (id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     dose, drug_name, lis_result, next_disease_time, notified_person, observation_result, received_date, result_time, series, card161_id)
SELECT x.id, 0,
    x.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.created_by_id,
    x.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.updated_by_id, x.uuid,
    x.dose, x.drug_name, x.lis_result, x.next_disease_time, x.notified_person, x.observation_result, x.received_date, x.result_time, x.series, x.card161_id
FROM public.card161_prevent_measure x
JOIN public2.card161 c ON c.id = x.card161_id;

INSERT INTO public2.card161_risk_factors
    (id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     address_location, risk_factor_code, season_time, card161_id)
SELECT x.id, 0,
    x.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.created_by_id,
    x.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.updated_by_id, x.uuid,
    x.address_location, x.risk_factor_code, x.season_time, x.card161_id
FROM public.card161_risk_factors x
JOIN public2.card161 c ON c.id = x.card161_id;

INSERT INTO public2.card161_screened_group
    (id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     contact_count, laboratory_test_conducted, prophylactic_address, required_prophylactic_substance, team_name, treated_with_prophylactic_substance, card161_id)
SELECT x.id, 0,
    x.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.created_by_id,
    x.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', x.updated_by_id, x.uuid,
    x.contact_count, x.laboratory_test_conducted, x.prophylactic_address, x.required_prophylactic_substance, x.team_name, x.treated_with_prophylactic_substance, x.card161_id
FROM public.card161_screened_group x
JOIN public2.card161 c ON c.id = x.card161_id;

-- element: 1:1
INSERT INTO public2.card161_indirection_causing (card161_id, catalog_code)
SELECT x.card161_id, x.catalog_code
FROM public.card161_indirection_causing x
JOIN public2.card161 c ON c.id = x.card161_id;

-- card161_vaccination: legacy join-jadval -> yangi to'liq entity (JOIN vaccination)
INSERT INTO public2.card161_vaccination
    (version, created_at, created_by_id, updated_at, updated_by_id, uuid,
     dose_volume, scheduled, serial_number, vaccination_date, vaccination_name, vaccination_verified_code, card161_id)
SELECT
    0,
    v.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', v.created_by_id,
    v.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', v.updated_by_id, v.uuid,
    v.dose_volume, v.scheduled, v.serial_number, v.vaccination_date, v.vaccination_name, v.vaccination_verified_code,
    cv.card161_id
FROM public.card161_vaccination cv
JOIN public.vaccination v ON v.id = cv.vaccination_id
JOIN public2.card161 c ON c.id = cv.card161_id;

COMMIT;

\echo '51-card161 OK'
SELECT 'card161' t, (SELECT count(*) FROM public.card161) src, (SELECT count(*) FROM public2.card161) dst
UNION ALL SELECT 'card161_vaccination', (SELECT count(*) FROM public.card161_vaccination), (SELECT count(*) FROM public2.card161_vaccination);
