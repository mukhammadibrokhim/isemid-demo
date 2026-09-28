-- =====================================================================
-- 30-patient.sql  —  patient + pt_address + pt_affiliation + pt_identifier
-- Mapping: docs/legacy-migration/02-mapping-5434.md §1
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

-- ---- patient (deyarli 1:1, +version) --------------------------------
-- first_name / last_name / middle_name -> upper(trim(...)); NULL NULLligicha qoladi
INSERT INTO public2.patient (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    created_org_uuid, updated_org_uuid, age_months, age_years, birth_date,
    category_code, first_name, gender_code, kinship_degree, kinship_full_name,
    last_name, marital_status_code, middle_name, phone_number, population_type_code,
    profession_code, residential_status_code
)
SELECT
    p.id, 0,
    p.created_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    p.created_by_id,
    p.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent',
    p.updated_by_id, p.uuid,
    p.created_org_uuid, p.updated_org_uuid, p.age_months, p.age_years, p.birth_date,
    p.category_code, upper(TRIM(p.first_name)), p.gender_code, p.kinship_degree, p.kinship_full_name,
    upper(TRIM(p.last_name)), p.marital_status_code, upper(TRIM(p.middle_name)), p.phone_number, p.population_type_code,
    p.profession_code, p.residential_status_code
FROM public.patient p;

-- ---- pt_address ----------------------------------------------------
-- city_code->district_code, state_code->region_code, +version, org_uuid=NULL
INSERT INTO public2.pt_address (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    created_org_uuid, updated_org_uuid, apartment_number, district_code, house_number,
    neighborhood_code, region_code, street_address, type, patient_id
)
SELECT
    a.id, 0,
    a.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', a.created_by_id,
    a.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', a.updated_by_id, a.uuid,
    NULL, NULL,
    a.apartment_number, a.city_code, a.house_number, a.neighborhood_code, a.state_code,
    a.street_address, a.type, a.patient_id
FROM public.pt_address a
JOIN public.patient p ON p.id = a.patient_id;   -- yetim manzil skip (patient_id NN)

INSERT INTO public2._migration_notes (source_table, source_id, note)
SELECT 'pt_address', a.id, 'patient_id yetim (public.patient da yo''q)'
FROM public.pt_address a
LEFT JOIN public.patient p ON p.id = a.patient_id
WHERE p.id IS NULL;

-- ---- pt_affiliation ----------------------------------------------------
INSERT INTO public2.pt_affiliation (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    created_org_uuid, updated_org_uuid, address, district_code, last_visited_date,
    organization_id, organization_name, organization_uuid, region_code, type, patient_id
)
SELECT
    f.id, 0,
    f.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', f.created_by_id,
    f.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', f.updated_by_id, f.uuid,
    NULL, NULL,
    f.address, f.city_code, f.last_visited_date,
    -- organization_id: yetim bo'lsa NULL (nullable ustun)
    (SELECT o.id FROM public.organization o WHERE o.id = f.organization_id),
    f.organization_name, f.organization_uuid, f.state_code, f.type, f.patient_id
FROM public.pt_affiliation f
JOIN public.patient p ON p.id = f.patient_id;

INSERT INTO public2._migration_notes (source_table, source_id, note)
SELECT 'pt_affiliation', f.id, 'patient_id yetim'
FROM public.pt_affiliation f
LEFT JOIN public.patient p ON p.id = f.patient_id
WHERE p.id IS NULL;

-- ---- pt_identifier ----------------------------------------------------
-- Normallashtirish (bitta joyda, _pt_ident_norm temp jadvalida):
--   value     -> barcha bo'sh joylar (space/tab/NBSP) olib tashlanadi + UPPERCASE
--                ('aa 1234567' -> 'AA1234567'); bo'sh qolsa '—' + note
--   type_code -> upper(trim) va ref_catalog IDENTIFIER_TYPE kodlariga o'giriladi:
--                NNUZB / PINFL / JSHSHIR / NI     -> NNUZB
--                PASSPORT / PPN                   -> PPN
--                BCT                              -> BCT
--                NULL / ''                        -> UNKNOWN
--                qolganlari                       -> o'zgarmaydi (uppercase), note yoziladi
CREATE TEMP TABLE _pt_ident_norm ON COMMIT DROP AS
SELECT
    i.id,
    upper(TRIM(i.type_code))                                   AS src_type,
    i.value                                                    AS src_value,
    n.v                                                        AS norm_value,
    CASE
        WHEN n.t IS NULL OR n.t = ''                    THEN 'UNKNOWN'
        WHEN n.t IN ('NNUZB', 'PINFL', 'JSHSHIR', 'NI') THEN 'NNUZB'
        WHEN n.t IN ('PASSPORT', 'PPN')                 THEN 'PPN'
        ELSE n.t
    END                                                        AS norm_type
FROM public.pt_identifier i
CROSS JOIN LATERAL (
    SELECT upper(TRIM(i.type_code)) AS t,
           upper(regexp_replace(i.value, '[[:space:]' || chr(160) || ']+', '', 'g')) AS v
) n;

INSERT INTO public2.pt_identifier (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    created_org_uuid, updated_org_uuid, period_end, period_start, type_code, value, patient_id
)
SELECT
    i.id, 0,
    i.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', i.created_by_id,
    i.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', i.updated_by_id, i.uuid,
    NULL, NULL,
    i.period_end, i.period_start,
    left(n.norm_type, 30),
    left(COALESCE(NULLIF(n.norm_value, ''), '—'), 100),
    i.patient_id
FROM public.pt_identifier i
JOIN _pt_ident_norm n ON n.id = i.id
JOIN public.patient p ON p.id = i.patient_id;

-- type_code o'girish xulosasi — legacy kod -> yangi kod, qator soni
INSERT INTO public2._migration_notes (source_table, note, details)
SELECT 'pt_identifier', 'type_code mapping',
       COALESCE(src_type, 'NULL') || ' -> ' || norm_type || ' (' || count(*) || ' qator)'
FROM _pt_ident_norm
GROUP BY src_type, norm_type;

-- ref_catalog (IDENTIFIER_TYPE) da yo'q qolgan type_code'lar — turi bo'yicha bitta qayd
INSERT INTO public2._migration_notes (source_table, note, details)
SELECT 'pt_identifier', 'type_code ref_catalog IDENTIFIER_TYPE da yo''q', t.type_code || ' (' || t.cnt || ' qator)'
FROM (
    SELECT i.type_code, count(*) AS cnt
    FROM public2.pt_identifier i
    WHERE NOT EXISTS (
        SELECT 1 FROM public2.ref_catalog c
        WHERE c.type = 'IDENTIFIER_TYPE' AND c.code = i.type_code
    )
    GROUP BY i.type_code
) t;

-- NNUZB bo'lib, 14 raqam emas — shubhali qiymatlar (ko'chiriladi, faqat qayd)
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'pt_identifier', n.id, 'NNUZB qiymati 14 raqam emas', n.src_value
FROM _pt_ident_norm n
WHERE n.norm_type = 'NNUZB' AND n.norm_value !~ '^[0-9]{14}$';

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'pt_identifier', n.id, 'value bo''sh — ''—'' qo''yildi', n.src_value
FROM _pt_ident_norm n
WHERE COALESCE(n.norm_value, '') = '';

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'pt_identifier', n.id, 'value 100 belgigacha kesildi', n.src_value
FROM _pt_ident_norm n
WHERE length(n.norm_value) > 100;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'pt_identifier', n.id, 'type_code 30 belgigacha kesildi', n.norm_type
FROM _pt_ident_norm n
WHERE length(n.norm_type) > 30;

INSERT INTO public2._migration_notes (source_table, source_id, note)
SELECT 'pt_identifier', i.id, 'patient_id yetim (public.patient da yo''q)'
FROM public.pt_identifier i
LEFT JOIN public.patient p ON p.id = i.patient_id
WHERE p.id IS NULL;

COMMIT;

\echo '30-patient OK'
SELECT 'patient' t,        (SELECT count(*) FROM public.patient) src,        (SELECT count(*) FROM public2.patient) dst
UNION ALL SELECT 'pt_address',    (SELECT count(*) FROM public.pt_address),    (SELECT count(*) FROM public2.pt_address)
UNION ALL SELECT 'pt_affiliation',(SELECT count(*) FROM public.pt_affiliation),(SELECT count(*) FROM public2.pt_affiliation)
UNION ALL SELECT 'pt_identifier', (SELECT count(*) FROM public.pt_identifier), (SELECT count(*) FROM public2.pt_identifier);

-- identifier turlari taqsimoti (yangi sxemada)
SELECT type_code, count(*) cnt,
       count(*) FILTER (WHERE value ~ '^[0-9]{14}$') digits14,
       count(*) FILTER (WHERE value ~ '[[:space:]a-z]') not_normalized
FROM public2.pt_identifier
GROUP BY type_code
ORDER BY cnt DESC;
