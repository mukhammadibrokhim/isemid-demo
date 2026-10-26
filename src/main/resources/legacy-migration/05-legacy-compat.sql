-- =====================================================================
-- 05-legacy-compat.sql  —  legacy `public` sxemasini skriptlar kutgan shaklga keltirish
-- Legacy baza nusxalari bir xil emas (eskirogida form058.source, cancel_reason,
-- pt_affiliation.organization_uuid, act'ning lis_*/subject ustunlari, form058_1* va
-- boshqa jadvallar yo'q). 40..61 skriptlari shu ustunlarni o'qiydi, shuning uchun
-- yetishmayotganlari BO'SH (NULL) ustun / bo'sh jadval sifatida qo'shiladi.
-- Faqat additiv va idempotent (IF NOT EXISTS): mavjud ma'lumotga tegmaydi, yangi
-- nusxada hech narsa qilmaydi. Dry run'da transaksiya bilan birga qaytariladi.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;

ALTER TABLE public.act ADD COLUMN IF NOT EXISTS institution_address varchar;
ALTER TABLE public.act ADD COLUMN IF NOT EXISTS institution_legal_address varchar;
ALTER TABLE public.act ADD COLUMN IF NOT EXISTS institution_name varchar;
ALTER TABLE public.act ADD COLUMN IF NOT EXISTS lis_act_id bigint;
ALTER TABLE public.act ADD COLUMN IF NOT EXISTS lis_attempt integer;
ALTER TABLE public.act ADD COLUMN IF NOT EXISTS lis_response text;
ALTER TABLE public.act ADD COLUMN IF NOT EXISTS lis_sent_date timestamp(6);
ALTER TABLE public.act ADD COLUMN IF NOT EXISTS subject_type varchar;
ALTER TABLE public.act ADD COLUMN IF NOT EXISTS tin integer;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS act_number bigint;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS activity_type_code varchar(255);
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS conservation_method_id integer;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS conservation_methods_ru text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS conservation_methods_uz text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS delivered_date_time timestamp;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS goal text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS laboratory_address text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS lis_organization_id bigint;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS package_type_id integer;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS package_type_ru varchar(255);
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS package_type_uz varchar(255);
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS participant_position_id integer;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS participant_position_ru text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS participant_position_uz varchar(255);
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS purpose_id integer;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS sample_taken_date_time timestamp;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS sampler_position_id integer;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS sampler_position_ru text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS sampler_position_uz varchar(255);
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS sampling_purpose_loinc text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS sampling_purpose_ru text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS sampling_purpose_uz text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS special_condition_id integer;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS special_sampling_conditions_ru text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS special_sampling_conditions_uz text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS storage_delivery_condition_id integer;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS storage_delivery_conditions_ru text;
ALTER TABLE public.act153 ADD COLUMN IF NOT EXISTS storage_delivery_conditions_uz text;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS category_id integer;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS category_name_ru text;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS category_name_uz text;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS depth_unit varchar(255);
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS distance_from_shore_unit varchar(255);
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS item_type_id integer;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS item_type_name_ru text;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS item_type_name_uz text;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS research_type_id integer;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS research_type_name_ru text;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS research_type_name_uz text;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS sample_qt_unit varchar(255);
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS sample_type_id integer;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS sample_type_ru text;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS sample_type_uz text;
ALTER TABLE public.act153_detail ADD COLUMN IF NOT EXISTS sample_volume_unit varchar;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS act_number bigint;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS activity_type_code varchar(255);
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS delivered_date_time timestamp;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS doc_number_of_taken_object varchar(255);
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS document_confirm_sampling text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS goal text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS laboratory_address text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS lis_organization_id bigint;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS manufacture_date date;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS manufacturing_company text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS package_type_id integer;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS package_type_ru varchar(255);
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS package_type_uz varchar(255);
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS participant_full_name varchar(255);
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS participant_position_id integer;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS participant_position_ru text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS participant_position_uz varchar(255);
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS purpose_id integer;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS sample_taken_date_time timestamp;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS sampler_position_id integer;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS sampler_position_ru text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS sampler_position_uz varchar(255);
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS sampling_purpose_loinc text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS sampling_purpose_ru text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS sampling_purpose_uz text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS special_condition_id integer;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS special_sampling_conditions_ru text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS special_sampling_conditions_uz text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS storage_delivery_condition_id integer;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS storage_delivery_conditions_ru text;
ALTER TABLE public.act154 ADD COLUMN IF NOT EXISTS storage_delivery_conditions_uz text;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS category_id integer;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS category_name_ru text;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS category_name_uz text;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS item_type_id integer;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS item_type_name_ru text;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS item_type_name_uz text;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS research_type_id integer;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS research_type_name_ru text;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS research_type_name_uz text;
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS sample_qt_unit varchar(255);
ALTER TABLE public.act154_detail ADD COLUMN IF NOT EXISTS sample_volume_unit varchar;
ALTER TABLE public.act155 ADD COLUMN IF NOT EXISTS activity_type_code varchar(255);
ALTER TABLE public.act155 ADD COLUMN IF NOT EXISTS institution_legal_address varchar;
ALTER TABLE public.act155 ADD COLUMN IF NOT EXISTS lis_act_id bigint;
ALTER TABLE public.act155 ADD COLUMN IF NOT EXISTS subject_type varchar;
ALTER TABLE public.act155_detail ADD COLUMN IF NOT EXISTS lis_organization_id bigint;
ALTER TABLE public.act156 ADD COLUMN IF NOT EXISTS activity_type_code varchar(255);
ALTER TABLE public.act156 ADD COLUMN IF NOT EXISTS lis_organization_id bigint;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS act_number bigint;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS activity_type_code varchar(255);
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS delivered_date_time timestamp;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS goal text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS laboratory_address text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS lis_organization_id bigint;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS package_type_id integer;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS package_type_ru varchar(255);
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS package_type_uz varchar(255);
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS participant_full_name varchar(255);
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS participant_position_id integer;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS participant_position_ru text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS participant_position_uz varchar(255);
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS purpose_id integer;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS sample_taken_date_time timestamp(6);
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS sampler_full_name varchar(255);
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS sampler_position_id integer;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS sampler_position_ru text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS sampler_position_uz varchar(255);
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS sampling_purpose_loinc text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS sampling_purpose_ru text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS sampling_purpose_uz text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS special_condition_id integer;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS special_sampling_conditions_ru text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS special_sampling_conditions_uz text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS storage_delivery_condition_id integer;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS storage_delivery_conditions_ru text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS storage_delivery_conditions_uz text;
ALTER TABLE public.act223 ADD COLUMN IF NOT EXISTS supporting_documents_for_sampling text;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS category_id integer;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS category_name_ru text;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS category_name_uz text;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS delivery_conditions_uz text;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS depth_unit varchar(255);
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS item_type_id integer;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS item_type_name_ru text;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS item_type_name_uz text;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS research_type_id integer;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS research_type_name_ru text;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS research_type_name_uz text;
ALTER TABLE public.act223_detail ADD COLUMN IF NOT EXISTS storage_conditions_uz text;
ALTER TABLE public.act224 ADD COLUMN IF NOT EXISTS activity_type_code varchar(255);
ALTER TABLE public.act224 ADD COLUMN IF NOT EXISTS additional_info text;
ALTER TABLE public.act224 ADD COLUMN IF NOT EXISTS institution_address varchar(255);
ALTER TABLE public.act224 ADD COLUMN IF NOT EXISTS institution_name varchar(255);
ALTER TABLE public.act224 ADD COLUMN IF NOT EXISTS tin integer;
CREATE TABLE IF NOT EXISTS public.act224_detail (
    id bigint,
    created_at timestamp(6),
    created_by_id bigint,
    updated_at timestamp(6),
    updated_by_id bigint,
    uuid uuid,
    execution_period timestamp(6),
    recommended_activities text,
    act224_id bigint
);
ALTER TABLE public.card ADD COLUMN IF NOT EXISTS form058_1_id bigint;
ALTER TABLE public.card161 ADD COLUMN IF NOT EXISTS activity_place_code varchar(255);
ALTER TABLE public.card161 ADD COLUMN IF NOT EXISTS infection_place_code varchar(255);
ALTER TABLE public.card161_infection_source ADD COLUMN IF NOT EXISTS contact_type_code varchar(255);
ALTER TABLE public.card161_infection_source ADD COLUMN IF NOT EXISTS duration varchar(255);
ALTER TABLE public.card161_infection_source ADD COLUMN IF NOT EXISTS relation_code varchar(255);
CREATE TABLE IF NOT EXISTS public.card161_main_probable_infection_factor (
    card161_id bigint,
    catalog_code varchar(255)
);
CREATE TABLE IF NOT EXISTS public.fm0581_animal_owner (
    id bigint,
    created_at timestamp,
    created_by_id bigint,
    updated_at timestamp,
    updated_by_id bigint,
    uuid uuid,
    last_name varchar(255),
    first_name varchar(255),
    middle_name varchar(255),
    state_code varchar(255),
    city_code varchar(255),
    neighborhood_code varchar(255),
    street_address varchar(255),
    house_number varchar(255),
    apartment_number varchar(255)
);
CREATE TABLE IF NOT EXISTS public.fm0581_bitten_person (
    id bigint,
    created_at timestamp,
    created_by_id bigint,
    updated_at timestamp,
    updated_by_id bigint,
    uuid uuid,
    form0581_id bigint,
    last_name varchar(255),
    first_name varchar(255),
    middle_name varchar(255),
    state_code varchar(255),
    city_code varchar(255),
    neighborhood_code varchar(255),
    street_address varchar(255),
    house_number varchar(255),
    apartment_number varchar(255)
);
ALTER TABLE public.form058 ADD COLUMN IF NOT EXISTS approved_full_name varchar(255);
ALTER TABLE public.form058 ADD COLUMN IF NOT EXISTS approved_org_uuid uuid;
ALTER TABLE public.form058 ADD COLUMN IF NOT EXISTS cancel_reason varchar(255);
ALTER TABLE public.form058 ADD COLUMN IF NOT EXISTS canceled_by bigint;
CREATE TABLE IF NOT EXISTS public.form058_1 (
    id bigint,
    created_at timestamp,
    created_by_id bigint,
    created_org_uuid uuid,
    updated_at timestamp,
    updated_by_id bigint,
    updated_org_uuid uuid,
    uuid uuid,
    mkb10code varchar(255),
    mkb10name varchar(255),
    mkb10usage_limit integer,
    final_mkb10code varchar(255),
    final_mkb10name varchar(255),
    injury_localization varchar(255),
    med_record_id varchar(255),
    patient_id bigint,
    sender_organization_id bigint,
    receiver_organization_id bigint,
    injury_date_time timestamp,
    visit_date_time timestamp,
    injury_state_code varchar(255),
    injury_city_code varchar(255),
    injury_address varchar(255),
    animal_owner_id bigint,
    animal_category_code varchar(255),
    animal_color varchar(255),
    animal_type varchar(255),
    animal_breed varchar(255),
    other_people_bitten boolean,
    hospitalized_at timestamp,
    hospital_place_id bigint,
    antirabic_aid_info varchar(1000),
    notifier_full_name varchar(255),
    receiver_full_name varchar(255),
    initial_report_date_time timestamp,
    status varchar(255),
    source varchar(20),
    cancel_reason varchar(255),
    canceled_by bigint,
    approved_full_name varchar(255),
    approved_org_uuid uuid,
    not_approve_comment varchar(1000)
);

-- act224: tavsiya etilgan choralar eski legacy'da bola jadvalda (act224_recommended_actions) va
-- act224'ning tekis ustunlarida; yangi nusxada bu jadval yo'q - 61-act-subtypes ikkalasini ham o'qiydi.
CREATE TABLE IF NOT EXISTS public.act224_recommended_actions (
    id bigint,
    created_at timestamp,
    created_by_id bigint,
    updated_at timestamp,
    updated_by_id bigint,
    uuid uuid,
    execution_period timestamp,
    recommended_activity varchar,
    act224_id bigint
);
ALTER TABLE public.act224 ADD COLUMN IF NOT EXISTS recommended_activities varchar;
ALTER TABLE public.act224 ADD COLUMN IF NOT EXISTS execution_period timestamp;

COMMIT;

\echo '05-legacy-compat OK'
