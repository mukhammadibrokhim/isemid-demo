-- =====================================================================
-- 45-form058-1.sql  —  form058_1 + fm0581_animal_owner (owner_* ga) +
--                      fm0581_bitten_person -> form058_1_other_injured_person
-- TAMOYIL: har bir qator ko'chiriladi, majburiy maydon bo'sh -> sentinel + note.
-- (Avval "form058_1 skip" qarori bekor — foydalanuvchi: hech narsa yo'qolmasin.)
-- Bog'liqlik: 10/20/30 bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

-- ---- form058_1 ----------------------------------------------------
-- owner_* maydonlari legacy fm0581_animal_owner (animal_owner_id) dan.
INSERT INTO public2.form058_1 (
    id, version, uuid, created_at, updated_at, created_by_id, updated_by_id,
    created_org_uuid, updated_org_uuid, icd10_code, icd10_name, injury_localization,
    final_icd10_code, final_icd10_name, status, source, patient_id,
    sender_organization_id, receiver_organization_id, injury_date_time, dpu_visit_date_time,
    injury_region_code, injury_district_code, injury_address,
    animal_category_code, animal_color, animal_type, animal_breed,
    owner_last_name, owner_first_name, owner_middle_name,
    owner_region_code, owner_district_code, owner_neighborhood_code,
    owner_street, owner_house_number, owner_apartment_number,
    other_people_injured, hospitalized_at, hospital_organization_id, antirabic_assistance_info,
    notifier_full_name, receiver_full_name, message_sent_at,
    cancel_reason, canceled_by_id, canceled_at, approved_by_id, approved_organization_id, approved_at,
    approved_full_name, approved_org_uuid,
    deleted, deleted_at, deleted_by_id, delete_reason, source_integration_client_id, has_linked_cards
)
SELECT
    l.id, 0, l.uuid,
    l.created_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    l.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    l.created_by_id, l.updated_by_id,
    COALESCE(l.created_org_uuid, '00000000-0000-0000-0000-000000000000'),
    l.updated_org_uuid,
    left(COALESCE(NULLIF(TRIM(l.mkb10code), ''), '—'), 20),
    left(COALESCE(NULLIF(TRIM(l.mkb10name), ''), '—'), 512),
    left(l.injury_localization, 500),
    left(l.final_mkb10code, 20), left(l.final_mkb10name, 512),
    CASE l.status
        WHEN 'NEW'              THEN 'SENT'
        WHEN 'SENT'             THEN 'SENT'
        WHEN 'RECEIVED'         THEN 'ACCEPTED'
        WHEN 'CARD_LINKED'      THEN 'CARD_LINKED'
        WHEN 'CARD_REJECTED'    THEN 'ACCEPTED'
        WHEN 'APPROVED_PENDING' THEN 'ACCEPTED'
        WHEN 'APPROVED'         THEN 'APPROVED'
        WHEN 'CANCELED'         THEN 'CANCELED'
        WHEN 'NOT_APPROVED'     THEN 'CANCELED'
        ELSE 'SENT'
    END,
    left(CASE WHEN upper(trim(l.source)) = 'DMED' THEN 'DMED' ELSE 'MANUAL' END, 20),
    l.patient_id,
    COALESCE(l.sender_organization_id, 0),
    COALESCE(l.receiver_organization_id, 0),
    COALESCE(l.injury_date_time, l.created_at::timestamp),
    COALESCE(l.visit_date_time, l.injury_date_time, l.created_at::timestamp),
    left(COALESCE(NULLIF(TRIM(l.injury_state_code), ''), '—'), 64),
    left(COALESCE(NULLIF(TRIM(l.injury_city_code), ''), '—'), 64),
    left(l.injury_address, 1000),
    left(l.animal_category_code, 64), left(l.animal_color, 255),
    left(l.animal_type, 255), left(l.animal_breed, 255),
    left(ao.last_name, 255), left(ao.first_name, 255), left(ao.middle_name, 255),
    left(ao.state_code, 64), left(ao.city_code, 64), left(ao.neighborhood_code, 64),
    left(ao.street_address, 255), left(ao.house_number, 32), left(ao.apartment_number, 32),
    l.other_people_bitten, l.hospitalized_at, l.hospital_place_id,
    left(l.antirabic_aid_info, 2000),
    left(COALESCE(NULLIF(TRIM(l.notifier_full_name), ''), '—'), 255),
    left(l.receiver_full_name, 255),
    NULL,
    left(COALESCE(l.cancel_reason, l.not_approve_comment), 1000),
    l.canceled_by, NULL, NULL, NULL, NULL,
    left(l.approved_full_name, 255), l.approved_org_uuid,
    false, NULL, NULL, NULL, NULL,
    EXISTS (SELECT 1 FROM public.card c WHERE c.form058_1_id = l.id)
FROM public.form058_1 l
LEFT JOIN public.fm0581_animal_owner ao ON ao.id = l.animal_owner_id;

-- fm0581_animal_owner.med_record_id / form058_1.med_record_id — target'da yo'q -> qayd
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'form058_1', l.id, 'target''da ustun yo''q — tashlandi', 'med_record_id='||l.med_record_id
FROM public.form058_1 l WHERE l.med_record_id IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'form058_1', l.id, 'sentinel/fallback ishlatildi',
       concat_ws('; ',
         CASE WHEN l.created_org_uuid IS NULL THEN 'created_org_uuid -> sentinel' END,
         CASE WHEN l.mkb10code IS NULL OR l.mkb10name IS NULL THEN 'icd10 -> ''—''' END,
         CASE WHEN l.injury_state_code IS NULL THEN 'injury_region -> ''—''' END,
         CASE WHEN l.injury_city_code IS NULL THEN 'injury_district -> ''—''' END,
         CASE WHEN l.sender_organization_id IS NULL THEN 'sender_org -> 0' END,
         CASE WHEN l.receiver_organization_id IS NULL THEN 'receiver_org -> 0' END,
         CASE WHEN l.injury_date_time IS NULL THEN 'injury_date_time -> created_at' END)
FROM public.form058_1 l
WHERE l.created_org_uuid IS NULL OR l.mkb10code IS NULL OR l.mkb10name IS NULL
   OR l.injury_state_code IS NULL OR l.injury_city_code IS NULL
   OR l.sender_organization_id IS NULL OR l.receiver_organization_id IS NULL
   OR l.injury_date_time IS NULL;

-- ---- fm0581_bitten_person -> form058_1_other_injured_person -------
INSERT INTO public2.form058_1_other_injured_person (
    id, version, uuid, created_at, updated_at, created_by_id, updated_by_id, form0581_id,
    last_name, first_name, middle_name, region_code, district_code, neighborhood_code,
    street, house_number, apartment_number
)
SELECT
    b.id, 0, b.uuid,
    b.created_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    b.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    b.created_by_id, b.updated_by_id, b.form0581_id,
    left(b.last_name, 255), left(b.first_name, 255), left(b.middle_name, 255),
    left(b.state_code, 64), left(b.city_code, 64), left(b.neighborhood_code, 64),
    left(b.street_address, 255), left(b.house_number, 32), left(b.apartment_number, 32)
FROM public.fm0581_bitten_person b
JOIN public2.form058_1 f ON f.id = b.form0581_id;

COMMIT;

\echo '45-form058-1 OK'
SELECT 'form058_1' t, (SELECT count(*) FROM public.form058_1) src, (SELECT count(*) FROM public2.form058_1) dst
UNION ALL
SELECT 'form058_1_other_injured_person', (SELECT count(*) FROM public.fm0581_bitten_person),
       (SELECT count(*) FROM public2.form058_1_other_injured_person);
