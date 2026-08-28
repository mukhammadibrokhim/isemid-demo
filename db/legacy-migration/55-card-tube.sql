-- =====================================================================
-- 55-card-tube.sql  —  card_tube (JOINED subtype) + element + bola jadvallar
-- ⚠️ BAZA jadvali: ~45 ustun QAYTA NOMLANGAN. Juftliklar TAXMIN
--    (docs/legacy-migration/20-card.md §7a). `-- ??` = TEKSHIRILSIN.
-- Bog'liqlik: 50-card bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

INSERT INTO public2.card_tube (
    id, adult_count, children_under_14_count, roommate_children_count, roommates_count,
    room_count, household_contact, total_contact, teenager_count, pregnant_women_count,
    food_childcare_worker_count, family_room_count, floor_count, spittoon_count,
    room_area_sq_m, total_area_sq_m, isolated_room_area_sq_m,
    info_sent_to_clinic_date, info_sent_to_workplace_date,
    dg_icd10_code, dg_icd10_name, icd10_code, icd10_name,
    first_mb_date, mb_patient_reg_date, mb_detection_method,
    housing_improvement_date, primary_dispensary_date, dispensary_group, dispensary_id,
    received_by, family_budget_code, kinship_degree_code,
    heating_type_code, sewerage_type_code, sputum_disposal_method_code, needs_renovation_code,
    sanitary_hygienic_assessment_code, receives_disinfectant_code,
    disinfectant_amount_per_month, disinfectant_provider, follows_cough_precaution,
    uses_spittoon_at_home, uses_spittoon_at_work, uses_spittoon_in_pub_place,
    previous_housing_difference, visit_interval_unit, visit_interval_value,
    ftb_visit_interval_unit, ftb_visit_interval_val,
    vaccination_date, vaccination_name, serial_number, dose_volume,
    discharge_date, dismissal_date, start_date, end_date,
    retreatment_end_date, retreatment_start_date,
    habitability_code, harmful_habit_code, home_stay_reason_code, housing_condition_code,
    recovery_plan_code, work_condition_code, has_elevator, has_spittoon, has_ventilation,
    scheduled, full_name
)
SELECT
    l.id,
    l.adults, l.children_under14, l.child_roommates, l.roommates,
    l.rooms, l.household_contacts, l.total_contacts, l.teenagers, l.pregnant_women,
    l.food_or_child_workers, l.family_rooms, l.floors, l.spittoon_count,
    l.room_area, l.total_area, l.isolated_room_area,
    l.clinic_notification_date, l.workplace_notification_date,
    l.dg_mkb10code, l.dg_mkb10name, l.mkb10code, l.mkb10name,
    l.firstmbdate, l.mb_reg_date, l.mb_detection_method,
    l.improvement_date, l.dispensary_start_date, l.dispensary_group, l.dispensary_id::text,
    l.notified_to,           -- received_by            -- ??
    l.income_level_code,     -- family_budget_code     -- ??
    l.relation_code,         -- kinship_degree_code    -- ??
    l.heating_code, l.sewerage_code, l.sputum_disposal_code, l.renovation_needed_code,
    l.hygiene_assessment_code, l.gets_disinfectant_code,
    l.disinfectant_amount,   -- disinfectant_amount_per_month  -- ??
    l.disinfectant_provider, l.follows_cough_etiquette,
    l.spittoon_at_home, l.spittoon_at_work, l.spittoon_in_public,
    l.housing_diff, l.visit_interval_unit, l.visit_interval_value,
    NULL, NULL,              -- ftb_visit_interval_*   -- ?? yangi'da 2-juft, legacy'da 1 juft
    l.vaccine_date, l.vaccine_name,
    l.vaccine_series,        -- serial_number          -- ??
    l.dose_volume,
    l.discharge_date, l.dismissal_date, l.start_date, l.end_date,
    l.retreatment_end_date, l.retreatment_start_date,
    l.habitability_code, l.harmful_habit_code, l.home_stay_reason_code, l.housing_condition_code,
    l.recovery_plan_code, l.work_condition_code, l.has_elevator, l.has_spittoon, l.has_ventilation,
    l.scheduled, l.full_name
FROM public.card_tube l
JOIN public2.card c ON c.id = l.id;

-- legacy-only (drop): unit_in_dose
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card_tube', l.id, 'target''da ustun yo''q — tashlandi', 'unit_in_dose='||l.unit_in_dose
FROM public.card_tube l
JOIN public2.card_tube x ON x.id = l.id
WHERE l.unit_in_dose IS NOT NULL;

-- ---- element jadvallar (1:1) --------------------------------------
INSERT INTO public2.card_tube_checkup_dates (card_tube_id, checkup_date)
SELECT x.card_tube_id, x.checkup_date FROM public.card_tube_checkup_dates x JOIN public2.card_tube c ON c.id=x.card_tube_id;
INSERT INTO public2.card_tube_nutrition_type (card_tube_id, code)
SELECT x.card_tube_id, x.code FROM public.card_tube_nutrition_type x JOIN public2.card_tube c ON c.id=x.card_tube_id;

-- ---- card_tube_contact_monitoring (rename'lar) -------------------
INSERT INTO public2.card_tube_contact_monitoring (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    age, birth_date, contact_status_code, diagnosis_date, full_name,
    notification_receiver, relation_code, workplace_or_study_place, card_tube_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.age, d.birth_date, d.status_code, d.diag_date, d.full_name,
    d.notifier, d.relation_code, d.work_study_place, d.card_tube_id
FROM public.card_tube_contact_monitoring d
JOIN public2.card_tube c ON c.id = d.card_tube_id;

-- ---- card_tube_infection_source (rename'lar) --------------------
INSERT INTO public2.card_tube_infection_source (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    contact_duration, full_name, relation_degree_code, tb_contact_code, card_tube_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.duration, d.full_name, d.relation_code, d.contact_type_code, d.card_tube_id
FROM public.card_tube_infection_source d
JOIN public2.card_tube c ON c.id = d.card_tube_id;

-- ---- card_tube_tb_history (rename'lar) --------------------------
INSERT INTO public2.card_tube_tb_history (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    infection_date, infection_location, icd10_code, icd10_name, registration_group, card_tube_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.date, d.location, d.mkb_code, d.mkb_name, d.reg_group, d.card_tube_id
FROM public.card_tube_tb_history d
JOIN public2.card_tube c ON c.id = d.card_tube_id;

-- ---- card_tube_xray (1:1 + version) ---------------------------
INSERT INTO public2.card_tube_xray (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    result, xray_date, xray_place, card_tube_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.result, d.xray_date, d.xray_place, d.card_tube_id
FROM public.card_tube_xray d
JOIN public2.card_tube c ON c.id = d.card_tube_id;

COMMIT;
\echo '55-card-tube OK'
SELECT 'card_tube' t, (SELECT count(*) FROM public.card_tube) src, (SELECT count(*) FROM public2.card_tube) dst;
