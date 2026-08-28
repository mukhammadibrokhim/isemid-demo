-- =====================================================================
-- 53-card175.sql  —  card175 (JOINED subtype) + element jadvallar
-- ⚠️ BAZA jadvali: legacy va target nomlari DEYARLI DISJOINT. Juftliklar TAXMIN
--    (docs/legacy-migration/20-card.md §5a). `-- ??` = TEKSHIRILSIN.
--    diagnoz-kod ustunlari BOG'LANGAN form058/form0581 dan olinadi (qaror).
-- Bog'liqlik: 50-card, 40-form058 bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

INSERT INTO public2.card175 (
    id, clinical_form, pathogen_type,
    initial_diagnosis_code, checking_diagnosis_code, diagnosis_confirmed_code,
    date_of_discharge_from_hospital, hospital_discharge_status_code,
    date_of_diagnosis_of_emergency_day, time_of_epidemiological_investigation,
    date_of_final_diagnosis, date_of_illness, severity_of_illness_code,
    relevance_of_disease_to_profession_code, reason_of_late_hospitalization_code,
    name_of_medicine, quantity_of_medicine, serial_number, reason_of_leaving_home_code,
    where_patient_come_code, patient_identified_code, patient_come_code,
    brief_epidemiological_comment, owner_of_disease_spreader_code, disease_spreader_type_code,
    transport_type_code, date_of_vaccination, information_about_last_vaccination, vaccination_count,
    observation_result_of_animals_code, place_of_application, prevention_and_aid_code,
    information_about_vaccination_code
)
SELECT
    l.id, l.clinical_form, l.pathogen_type,
    f.icd10_code,            -- initial_diagnosis_code   <- bog'langan form
    f.final_icd10_code,      -- checking_diagnosis_code  <- bog'langan form
    l.diagnosis_lab_confirmed,   -- diagnosis_confirmed_code  -- ??
    l.discharge_date,        -- date_of_discharge_from_hospital
    l.discharge_status_code, -- hospital_discharge_status_code
    l.urgent_diagnosis_date, -- date_of_diagnosis_of_emergency_day  -- ??
    l.epi_check_time,        -- time_of_epidemiological_investigation
    l.final_diagnosis_date,  -- date_of_final_diagnosis
    l.illness_date,          -- date_of_illness
    l.illness_severity_code, -- severity_of_illness_code
    l.job_relation_code,     -- relevance_of_disease_to_profession_code  -- ??
    l.late_hospitalization_reason,  -- reason_of_late_hospitalization_code
    l.medicine_name,         -- name_of_medicine
    l.medicine_qty,          -- quantity_of_medicine
    l.medicine_series,       -- serial_number   -- ??
    l.no_home_stay_reason,   -- reason_of_leaving_home_code  -- ??
    l.origin_place_code,     -- where_patient_come_code   -- ??
    l.patient_detected_code, -- patient_identified_code
    l.patient_source_code,   -- patient_come_code  -- ??
    l.possible_infection_place,  -- brief_epidemiological_comment  -- ??
    l.spreader_owner_code,   -- owner_of_disease_spreader_code
    l.spreader_type_code,    -- disease_spreader_type_code
    l.transport_type,        -- transport_type_code
    l.vaccination_date,      -- date_of_vaccination
    l.vaccination_info,      -- information_about_last_vaccination  -- ??
    l.vaccination_plan,      -- vaccination_count  -- ??
    l.animal_obs_result_code,  -- observation_result_of_animals_code
    l.application_place,     -- place_of_application
    l.emergency_prophylaxis_info,  -- prevention_and_aid_code  -- ??
    NULL                     -- information_about_vaccination_code  -- ?? legacy manba yo'q
FROM public.card175 l
JOIN public2.card c ON c.id = l.id
LEFT JOIN public2.form058 f ON f.id = c.form058_id;

-- legacy-only (drop): animal_diagnosis_code, primary_diagnosis (erkin matn)
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card175', l.id, 'target''da ustun yo''q — tashlandi',
       concat_ws('; ',
         CASE WHEN l.animal_diagnosis_code IS NOT NULL THEN 'animal_diagnosis_code' END,
         CASE WHEN l.primary_diagnosis IS NOT NULL THEN 'primary_diagnosis (erkin matn)' END)
FROM public.card175 l
JOIN public2.card175 x ON x.id = l.id
WHERE l.animal_diagnosis_code IS NOT NULL OR l.primary_diagnosis IS NOT NULL;

-- ---- element jadvallar (1:1) --------------------------------------
INSERT INTO public2.card175_disease_transmission_condition (card175_id, catalog_code)
SELECT x.card175_id, x.catalog_code FROM public.card175_disease_transmission_condition x JOIN public2.card175 c ON c.id=x.card175_id;
INSERT INTO public2.card175_part_of_injury (card175_id, catalog_code)
SELECT x.card175_id, x.catalog_code FROM public.card175_part_of_injury x JOIN public2.card175 c ON c.id=x.card175_id;
INSERT INTO public2.card175_pathogen_main_factor (card175_id, catalog_code)
SELECT x.card175_id, x.catalog_code FROM public.card175_pathogen_main_factor x JOIN public2.card175 c ON c.id=x.card175_id;
INSERT INTO public2.card175_taken_measures_from_residence (card175_id, catalog_code)
SELECT x.card175_id, x.catalog_code FROM public.card175_taken_measures_from_residence x JOIN public2.card175 c ON c.id=x.card175_id;

COMMIT;
\echo '53-card175 OK'
SELECT 'card175' t, (SELECT count(*) FROM public.card175) src, (SELECT count(*) FROM public2.card175) dst;
