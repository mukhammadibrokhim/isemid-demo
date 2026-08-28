-- =====================================================================
-- 40-form058.sql  —  fm058_location + form058
-- TAMOYIL: HAR BIR QATOR KO'CHIRILADI. Majburiy maydon bo'sh -> sentinel
--          ('—' / 0 / created_at) + note-log.  Hech narsa skip qilinmaydi.
-- form058_1 / fm0581_* : 45-form058-1.sql da (alohida).
-- Mapping: docs/legacy-migration/02-mapping-5434.md §2
-- Bog'liqlik: 10/20/30 bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

-- ---- fm058_location (form058 dan oldin) ------------------------------
INSERT INTO public2.fm058_location (
    id, version, created_at, updated_at, created_by_id, updated_by_id,
    latitude, longitude, location
)
SELECT
    l.id, 0,
    l.created_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    l.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    l.created_by_id, l.updated_by_id,
    l.latitude, l.longitude, left(l.location, 1000)
FROM public.fm058_location l;

-- ---- form058 (barcha qatorlar) --------------------------------------
-- Target'da FK faqat source_integration_client_id da (u -> NULL).
-- sender/receiver_organization_id: NOT NULL, FK YO'Q -> NULL bo'lsa 0.
-- patient_id: nullable, FK yo'q -> to'g'ridan.
WITH src AS (
    SELECT
        f.*,
        left(NULLIF(TRIM(BOTH ' ' FROM
            concat_ws(' ', p.last_name, p.first_name, p.middle_name)), ''), 255) AS p_full_name,
        p.birth_date  AS p_birth_date,
        left(p.gender_code, 32) AS p_gender,
        left(p.phone_number, 64) AS p_phone,
        left(COALESCE(
          (SELECT i.value FROM public.pt_identifier i
            WHERE i.patient_id = f.patient_id AND upper(i.type_code) = 'NNUZB' ORDER BY i.id LIMIT 1),
          (SELECT i.value FROM public.pt_identifier i
            WHERE i.patient_id = f.patient_id ORDER BY i.id LIMIT 1),
          lpad(COALESCE(f.patient_id, f.id)::text, 14, '0')
        ), 14) AS p_nnuzb,
        left((SELECT i.value FROM public.pt_identifier i
          WHERE i.patient_id = f.patient_id AND upper(i.type_code) = 'PINFL' ORDER BY i.id LIMIT 1), 14) AS p_pinfl,
        EXISTS (SELECT 1 FROM public.card c WHERE c.form058_id = f.id) AS has_cards
    FROM public.form058 f
    LEFT JOIN public.patient p ON p.id = f.patient_id
)
INSERT INTO public2.form058 (
    id, version, uuid, created_at, updated_at, created_by_id, updated_by_id,
    status, source, sender_organization_id, receiver_organization_id, hospital_place_id,
    icd10_code, icd10_name, final_icd10_code, final_icd10_name,
    disease_date, first_visit_date, visit_date, initial_report_date_time,
    disease_place, notifier_full_name, journal_form_code, form_comment,
    patient_nnuzb, patient_pinfl, patient_full_name, patient_birth_date, patient_gender, patient_phone,
    location_region_code, location_district_code, location_neighborhood_code, location_address,
    has_linked_cards, assigned_card_id, cancel_reason, canceled_by_id, canceled_at,
    approved_by_id, approved_organization_id, approved_at,
    patient_id, location_id, created_org_uuid, updated_org_uuid,
    icd10_usage_limit, lab_confirmation, admission_date, diagnosis_date, doc_send_date,
    disease_place_code, disease_cause, epidemic_measures, card_by_full_name,
    approved_full_name, approved_org_uuid,
    deleted, deleted_at, deleted_by_id, delete_reason, source_integration_client_id
)
SELECT
    e.id, 0, e.uuid,
    e.created_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    e.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    e.created_by_id, e.updated_by_id,
    CASE e.status
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
    CASE WHEN upper(trim(e.source)) = 'DMED' THEN 'DMED' ELSE 'MANUAL' END,
    COALESCE(e.sender_organization_id, 0),
    COALESCE(e.receiver_organization_id, 0),
    e.hospital_place_id,
    left(COALESCE(NULLIF(TRIM(e.mkb10code), ''), '—'), 20),
    left(COALESCE(NULLIF(TRIM(e.mkb10name), ''), '—'), 512),
    left(e.final_mkb10code, 20), left(e.final_mkb10name, 512),
    COALESCE(e.disease_date, e.first_visit_date, e.visit_date, e.created_at::timestamp),
    COALESCE(e.first_visit_date, e.visit_date, e.disease_date, e.created_at::timestamp),
    COALESCE(e.visit_date, e.first_visit_date, e.disease_date, e.created_at::timestamp),
    COALESCE(e.initial_report_date_time, e.created_at)::timestamp AT TIME ZONE 'Asia/Tashkent',
    left(COALESCE(NULLIF(TRIM(e.disease_place_code), ''), '—'), 512),
    left(COALESCE(NULLIF(TRIM(e.notifier_full_name), ''), '—'), 255),
    left(COALESCE(NULLIF(TRIM(e.journal_form_code), ''), 'JOURNAL_060'), 64),
    left(e.comment, 2000),
    e.p_nnuzb, e.p_pinfl,
    COALESCE(e.p_full_name, '—'),
    e.p_birth_date, e.p_gender, e.p_phone,
    NULL, NULL, NULL, NULL,                                 -- location_* : geo (fm058_location)
    e.has_cards, NULL,                                      -- assigned_card_id -> 90
    left(COALESCE(e.cancel_reason, e.not_approve_comment), 1000),
    e.canceled_by, NULL,
    NULL, NULL, NULL,
    e.patient_id, e.location_id, e.created_org_uuid, e.updated_org_uuid,
    e.mkb10usage_limit, e.lab_confirmation, e.admission_date, e.diagnosis_date,
    e.doc_send_date::timestamp AT TIME ZONE 'Asia/Tashkent',
    left(e.disease_place_code, 64), left(e.disease_cause, 2000), left(e.epidemic_measures, 2000),
    left(e.card_by_full_name, 255),
    left(e.approved_full_name, 255), e.approved_org_uuid,
    false, NULL, NULL, NULL, NULL
FROM src e;

-- note-log: sentinel ishlatilган qatorlar (ko'chirildi, faqat qayd)
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'form058', f.id, 'sentinel/fallback ishlatildi',
       concat_ws('; ',
         CASE WHEN NOT EXISTS (SELECT 1 FROM public.pt_identifier i WHERE i.patient_id=f.patient_id AND upper(i.type_code)='NNUZB')
              THEN 'NNUZB yo''q -> fallback' END,
         CASE WHEN NULLIF(TRIM(concat_ws(' ',p.last_name,p.first_name,p.middle_name)),'') IS NULL THEN 'patient FIO bo''sh -> ''—''' END,
         CASE WHEN f.mkb10code IS NULL OR f.mkb10name IS NULL THEN 'icd10 bo''sh -> ''—''' END,
         CASE WHEN f.disease_place_code IS NULL THEN 'disease_place bo''sh -> ''—''' END,
         CASE WHEN f.sender_organization_id IS NULL THEN 'sender_org bo''sh -> 0' END,
         CASE WHEN f.receiver_organization_id IS NULL THEN 'receiver_org bo''sh -> 0' END,
         CASE WHEN COALESCE(f.disease_date,f.first_visit_date,f.visit_date) IS NULL THEN 'sanalar bo''sh -> created_at' END
       )
FROM public.form058 f
LEFT JOIN public.patient p ON p.id = f.patient_id
WHERE NOT EXISTS (SELECT 1 FROM public.pt_identifier i WHERE i.patient_id=f.patient_id AND upper(i.type_code)='NNUZB')
   OR NULLIF(TRIM(concat_ws(' ',p.last_name,p.first_name,p.middle_name)),'') IS NULL
   OR f.mkb10code IS NULL OR f.mkb10name IS NULL
   OR f.disease_place_code IS NULL
   OR f.sender_organization_id IS NULL OR f.receiver_organization_id IS NULL
   OR COALESCE(f.disease_date,f.first_visit_date,f.visit_date) IS NULL;

-- SENTINEL form058 (id=0): hech qanday formaga bog'lanmagan card lar shунга
-- ishora qiladi (card CHECK: aynan bitta form kerak). Legacy id lar 1 dan.
INSERT INTO public2.form058 (
    id, version, uuid, created_at, updated_at, status, source,
    sender_organization_id, receiver_organization_id, icd10_code, icd10_name,
    disease_date, first_visit_date, visit_date, initial_report_date_time,
    disease_place, notifier_full_name, journal_form_code, patient_nnuzb, patient_full_name,
    has_linked_cards, deleted
) VALUES (
    0, 0, '00000000-0000-0000-0000-000000000001', now(), now(), 'CANCELED', 'MANUAL',
    0, 0, '—', 'MIGRATSIYA: NOMA''LUM',
    now(), now(), now(), now(),
    '—', 'MIGRATSIYA: NOMA''LUM', 'JOURNAL_060', '00000000000000', 'MIGRATSIYA: NOMA''LUM',
    false, false
);

COMMIT;

\echo '40-form058 OK'
SELECT 'fm058_location' t, (SELECT count(*) FROM public.fm058_location) src, (SELECT count(*) FROM public2.fm058_location) dst
UNION ALL
SELECT 'form058', (SELECT count(*) FROM public.form058), (SELECT count(*) FROM public2.form058)
UNION ALL
SELECT 'form058 NOTE', (SELECT count(*) FROM public2._migration_notes WHERE source_table='form058'), NULL;
