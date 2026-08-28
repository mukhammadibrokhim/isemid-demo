-- =====================================================================
-- 30-patient.sql  —  patient + pt_address + pt_affiliation + pt_identifier
-- Mapping: docs/legacy-migration/02-mapping-5434.md §1
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

-- ---- patient (deyarli 1:1, +version) --------------------------------
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
    p.category_code, p.first_name, p.gender_code, p.kinship_degree, p.kinship_full_name,
    p.last_name, p.marital_status_code, p.middle_name, p.phone_number, p.population_type_code,
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
-- type_code NULL -> 'UNKNOWN'; target varchar(30) — uzun qiymatlar skip
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
    left(COALESCE(NULLIF(TRIM(i.type_code), ''), 'UNKNOWN'), 30),
    left(i.value, 100), i.patient_id
FROM public.pt_identifier i
JOIN public.patient p ON p.id = i.patient_id;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'pt_identifier', i.id, 'type_code 30 belgigacha kesildi', i.type_code
FROM public.pt_identifier i
WHERE length(COALESCE(i.type_code,'UNKNOWN')) > 30;

COMMIT;

\echo '30-patient OK'
SELECT 'patient' t,        (SELECT count(*) FROM public.patient) src,        (SELECT count(*) FROM public2.patient) dst
UNION ALL SELECT 'pt_address',    (SELECT count(*) FROM public.pt_address),    (SELECT count(*) FROM public2.pt_address)
UNION ALL SELECT 'pt_affiliation',(SELECT count(*) FROM public.pt_affiliation),(SELECT count(*) FROM public2.pt_affiliation)
UNION ALL SELECT 'pt_identifier', (SELECT count(*) FROM public.pt_identifier), (SELECT count(*) FROM public2.pt_identifier);
