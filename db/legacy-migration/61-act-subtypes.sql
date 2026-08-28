-- =====================================================================
-- 61-act-subtypes.sql  —  act153/154/156/223/224 (JOINED, id=act.id) + *_detail
-- Mapping: docs/legacy-migration/02-mapping-5434.md §4
--   5434 legacy allaqachon refactor qilingan -> asosan 1:1.
--   Drop: eski nusxa ustunlar (position, participant_position, institution_*,
--         lis_act_id, lis_response, subject_type, tin(->identifier), delivered_date).
--   sampler_identifier_type/value <- legacy `tin` ('TIN' + tin::text).
--   participant_identifier_* -> NULL.  *_detail: +version=0.
-- Bog'liqlik: 60-act bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

-- ================= act153 =================
INSERT INTO public2.act153 (
    id, act_number, activity_type_code, sampling_documents, goal,
    sample_taken_date_time, delivered_date_time, purpose_id,
    sampling_purpose_uz, sampling_purpose_ru, sampling_purpose_loinc,
    sampler_full_name, sampler_position_id, sampler_position_uz, sampler_position_ru,
    participant_full_name, participant_position_id, participant_position_uz, participant_position_ru,
    special_condition_id, special_sampling_conditions_uz, special_sampling_conditions_ru,
    storage_delivery_condition_id, storage_delivery_conditions_uz, storage_delivery_conditions_ru,
    lis_organization_id, laboratory_address, package_type_id, package_type_uz, package_type_ru,
    conservation_method_id, conservation_methods_uz, conservation_methods_ru, additional_info,
    sampler_identifier_type, sampler_identifier_value, participant_identifier_type, participant_identifier_value
)
SELECT
    l.id, l.act_number, l.activity_type_code, l.sampling_documents, l.goal,
    l.sample_taken_date_time, l.delivered_date_time, l.purpose_id,
    l.sampling_purpose_uz, l.sampling_purpose_ru, l.sampling_purpose_loinc,
    l.sampler_full_name, l.sampler_position_id, l.sampler_position_uz, l.sampler_position_ru,
    l.participant_full_name, l.participant_position_id, l.participant_position_uz, l.participant_position_ru,
    l.special_condition_id, l.special_sampling_conditions_uz, l.special_sampling_conditions_ru,
    l.storage_delivery_condition_id, l.storage_delivery_conditions_uz, l.storage_delivery_conditions_ru,
    l.lis_organization_id, l.laboratory_address, l.package_type_id, l.package_type_uz, l.package_type_ru,
    l.conservation_method_id, l.conservation_methods_uz, l.conservation_methods_ru, l.additional_info,
    CASE WHEN l.tin IS NOT NULL THEN 'TIN' END, l.tin::text, NULL, NULL
FROM public.act153 l
JOIN public2.act a ON a.id = l.id AND a.act_type = 'ACT153';

INSERT INTO public2.act153_detail (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid, act153_id,
    research_type_id, research_type_name_uz, research_type_name_ru,
    category_id, category_name_uz, category_name_ru,
    item_type_id, item_type_name_uz, item_type_name_ru,
    object_type_id, object_code, address, sampling_depth, depth_unit,
    distance_from_shore, distance_from_shore_unit, sample_volume, sample_volume_unit,
    sample_qt_unit, sample_location, weather_at_sampling, water_temperature,
    sample_type_id, sample_type_uz, sample_type_ru
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid, d.act153_id,
    d.research_type_id, d.research_type_name_uz, d.research_type_name_ru,
    d.category_id, d.category_name_uz, d.category_name_ru,
    d.item_type_id, d.item_type_name_uz, d.item_type_name_ru,
    d.object_type_id, d.object_code, d.address, d.sampling_depth, d.depth_unit,
    d.distance_from_shore, d.distance_from_shore_unit, d.sample_volume, d.sample_volume_unit,
    d.sample_qt_unit, d.sample_location, d.weather_at_sampling, d.water_temperature,
    d.sample_type_id, d.sample_type_uz, d.sample_type_ru
FROM public.act153_detail d
JOIN public2.act153 p ON p.id = d.act153_id;

-- ================= act154 =================
INSERT INTO public2.act154 (
    id, title, act_number, activity_type_code, sample_taken_date_time, delivered_date_time,
    document_confirm_sampling, goal, purpose_id,
    sampling_purpose_uz, sampling_purpose_ru, sampling_purpose_loinc,
    sampler_full_name, sampler_position_id, sampler_position_uz, sampler_position_ru,
    participant_full_name, participant_position_id, participant_position_uz, participant_position_ru,
    manufacturing_company, manufacture_date, doc_number_of_taken_object,
    special_condition_id, special_sampling_conditions_uz, special_sampling_conditions_ru,
    storage_delivery_condition_id, storage_delivery_conditions_uz, storage_delivery_conditions_ru,
    lis_organization_id, laboratory_address, package_type_id, package_type_uz, package_type_ru,
    additional_info,
    sampler_identifier_type, sampler_identifier_value, participant_identifier_type, participant_identifier_value
)
SELECT
    l.id, l.title, l.act_number, l.activity_type_code, l.sample_taken_date_time, l.delivered_date_time,
    l.document_confirm_sampling, l.goal, l.purpose_id,
    l.sampling_purpose_uz, l.sampling_purpose_ru, l.sampling_purpose_loinc,
    l.sampler_full_name, l.sampler_position_id, l.sampler_position_uz, l.sampler_position_ru,
    l.participant_full_name, l.participant_position_id, l.participant_position_uz, l.participant_position_ru,
    l.manufacturing_company, l.manufacture_date, l.doc_number_of_taken_object,
    l.special_condition_id, l.special_sampling_conditions_uz, l.special_sampling_conditions_ru,
    l.storage_delivery_condition_id, l.storage_delivery_conditions_uz, l.storage_delivery_conditions_ru,
    l.lis_organization_id, l.laboratory_address, l.package_type_id, l.package_type_uz, l.package_type_ru,
    l.additional_info,
    CASE WHEN l.tin IS NOT NULL THEN 'TIN' END, l.tin::text, NULL, NULL
FROM public.act154 l
JOIN public2.act a ON a.id = l.id AND a.act_type = 'ACT154';

INSERT INTO public2.act154_detail (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid, act154_id,
    research_type_id, research_type_name_uz, research_type_name_ru,
    category_id, category_name_uz, category_name_ru,
    item_type_id, item_type_name_uz, item_type_name_ru,
    shift_code, sample_name, group_size, serial_number_of_group, sample_weight,
    sample_qt_unit, sample_volume, sample_volume_unit, note
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid, d.act154_id,
    d.research_type_id, d.research_type_name_uz, d.research_type_name_ru,
    d.category_id, d.category_name_uz, d.category_name_ru,
    d.item_type_id, d.item_type_name_uz, d.item_type_name_ru,
    d.shift_code, d.sample_name, d.group_size, d.serial_number_of_group, d.sample_weight,
    d.sample_qt_unit, d.sample_volume, d.sample_volume_unit, d.note
FROM public.act154_detail d
JOIN public2.act154 p ON p.id = d.act154_id;

-- ================= act156 =================
INSERT INTO public2.act156 (
    id, title, tin, institution_name, institution_address, activity_type_code,
    sample_taken_time, lis_organization_id, laboratory_address, sample_delivery_time,
    full_name_of_sampler, position_of_sampler,
    full_name_of_object_representative, position_of_object_representative
)
SELECT
    l.id, l.title, l.tin, l.institution_name, l.institution_address, l.activity_type_code,
    l.sample_taken_time, l.lis_organization_id, l.laboratory_address, l.sample_delivery_time,
    l.full_nameof_sampler, l.position_of_sampler,
    l.full_name_of_object_representative, l.position_of_object_representative
FROM public.act156 l
JOIN public2.act a ON a.id = l.id AND a.act_type = 'ACT156';

INSERT INTO public2.act156_group_detail (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid, act156_id,
    full_name_of_educator, hands_of_educator, first_food_bowl, second_food_bowl, tables, chairs,
    window_sill, door_handles, toys, toy_shelf, carpets, clothes_rack, full_name_of_place_owner,
    bed_clothes, bathroom_wall, towels, towel_rack, water_tap_faucet, wc_seats
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid, d.act156_id,
    d.full_name_of_educator, d.hands_of_educator, d.first_food_bowl, d.second_food_bowl, d.tables, d.chairs,
    d.window_sill, d.door_handles, d.toys, d.toy_shelf, d.carpets, d.clothes_rack, d.full_name_of_place_owner,
    d.bed_clothes, d.bathroom_wall, d.towels, d.towel_rack, d.water_tap_faucet, d.wcseats
FROM public.act156_group_detail d
JOIN public2.act156 p ON p.id = d.act156_id;

INSERT INTO public2.act156_kitchen_utensil (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid, act156_id,
    knife_for_bread, fruit_cutting_board, distribution_table, container_for_finished_products,
    full_name_of_chef, hands_of_chef, clothes_of_chef
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid, d.act156_id,
    d.knife_for_bread, d.fruit_cutting_board, d.distribution_table, d.container_for_finished_products,
    d.full_name_of_chef, d.hands_of_chef, d.clothes_of_chef
FROM public.act156_kitchen_utensil d
JOIN public2.act156 p ON p.id = d.act156_id;

-- ================= act223 =================
INSERT INTO public2.act223 (
    id, act_number, supporting_documents_for_sampling, goal, activity_type_code,
    sampler_full_name, sampler_position_id, sampler_position_uz, sampler_position_ru,
    participant_full_name, participant_position_id, participant_position_uz, participant_position_ru,
    purpose_id, sampling_purpose_uz, sampling_purpose_ru, sampling_purpose_loinc,
    sample_taken_date_time, delivered_date_time,
    special_condition_id, special_sampling_conditions_uz, special_sampling_conditions_ru,
    storage_delivery_condition_id, storage_delivery_conditions_uz, storage_delivery_conditions_ru,
    lis_organization_id, laboratory_address, package_type_id, package_type_uz, package_type_ru,
    additional_info,
    sampler_identifier_type, sampler_identifier_value, participant_identifier_type, participant_identifier_value
)
SELECT
    l.id, l.act_number, l.supporting_documents_for_sampling, l.goal, l.activity_type_code,
    l.sampler_full_name, l.sampler_position_id, l.sampler_position_uz, l.sampler_position_ru,
    l.participant_full_name, l.participant_position_id, l.participant_position_uz, l.participant_position_ru,
    l.purpose_id, l.sampling_purpose_uz, l.sampling_purpose_ru, l.sampling_purpose_loinc,
    l.sample_taken_date_time, l.delivered_date_time,
    l.special_condition_id, l.special_sampling_conditions_uz, l.special_sampling_conditions_ru,
    l.storage_delivery_condition_id, l.storage_delivery_conditions_uz, l.storage_delivery_conditions_ru,
    l.lis_organization_id, l.laboratory_address, l.package_type_id, l.package_type_uz, l.package_type_ru,
    l.additional_info,
    CASE WHEN l.tin IS NOT NULL THEN 'TIN' END, l.tin::text, NULL, NULL
FROM public.act223 l
JOIN public2.act a ON a.id = l.id AND a.act_type = 'ACT223';

INSERT INTO public2.act223_detail (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid, act223_id,
    research_type_id, research_type_name_uz, research_type_name_ru,
    category_id, category_name_uz, category_name_ru,
    item_type_id, item_type_name_uz, item_type_name_ru,
    exact_location_point_sampling, amount, depth_of_obtained_area, depth_unit
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid, d.act223_id,
    d.research_type_id, d.research_type_name_uz, d.research_type_name_ru,
    d.category_id, d.category_name_uz, d.category_name_ru,
    d.item_type_id, d.item_type_name_uz, d.item_type_name_ru,
    d.exact_location_point_sampling, d.amount, d.depth_of_obtained_area, d.depth_unit
FROM public.act223_detail d
JOIN public2.act223 p ON p.id = d.act223_id;

-- ================= act224 =================
INSERT INTO public2.act224 (
    id, tin, institution_name, institution_address, activity_type_code,
    full_name_of_epid_staff, position_of_epid_staff,
    full_name_of_participant_epid, position_of_participant_epid,
    name_of_institution, address_of_institution, name_of_regulatory_acts,
    checking_fulfillment_of_requirements, full_name_of_participant, additional_info
)
SELECT
    l.id, l.tin, l.institution_name, l.institution_address, l.activity_type_code,
    l.full_name_of_epid_staff, l.position_of_epid_staff,
    l.full_name_of_participant_epid, l.position_of_participant_epid,
    l.name_of_institution, l.address_of_institution, l.name_of_regulatory_acts,
    l.checking_fulfillment_of_requirements, l.full_name_of_participant, l.additional_info
FROM public.act224 l
JOIN public2.act a ON a.id = l.id AND a.act_type = 'ACT224';

INSERT INTO public2.act224_detail (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid, act224_id,
    recommended_activities, execution_period
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid, d.act224_id,
    d.recommended_activities, d.execution_period
FROM public.act224_detail d
JOIN public2.act224 p ON p.id = d.act224_id;

COMMIT;

\echo '61-act-subtypes OK'
SELECT 'act153' t,(SELECT count(*) FROM public2.act153) dst UNION ALL
SELECT 'act154',  (SELECT count(*) FROM public2.act154) UNION ALL
SELECT 'act156',  (SELECT count(*) FROM public2.act156) UNION ALL
SELECT 'act223',  (SELECT count(*) FROM public2.act223) UNION ALL
SELECT 'act224',  (SELECT count(*) FROM public2.act224);
