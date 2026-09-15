-- =====================================================================
-- 10-organization.sql  —  public.organization -> public2.organization
--                         public.organization_service_types -> public2 (1:1)
-- Mapping: docs/legacy-migration/02-mapping-5434.md §1
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

-- ---- organization -----------------------------------------------------
-- Rename: city_code->district_code, state_code->region_code, line->address_line
-- Drop:   country_code, district, email, service_area_code
-- Backfill: version=0; created_by_id/updated_by_id=NULL; name_uz<-name;
--           level_type NULL -> 'NOT_DEFINED'; medical_type NULL -> 'OTHER'
--           (MedicalType has no NOT_DEFINED constant — its sentinel is OTHER);
--           created_at/updated_at NULL -> now()
-- parent_id: 2-bosqich (self-FK) — avval NULL, keyin UPDATE.
INSERT INTO public2.organization (
    id, version, created_at, created_by_id, updated_at, updated_by_id,
    active, district_code, level_type, medical_type, name, phone, region_code,
    tin, uuid, address_line, name_kaa, name_ru, name_uz, name_uz_cyril, parent_id
)
SELECT
    o.id,
    0,
    COALESCE(o.created_at, now())::timestamp AT TIME ZONE 'Asia/Tashkent',
    NULL,
    COALESCE(o.updated_at, o.created_at, now())::timestamp AT TIME ZONE 'Asia/Tashkent',
    NULL,
    o.active,
    left(o.city_code, 64),               -- district_code
    left(COALESCE(o.level_type, 'NOT_DEFINED'), 50),
    left(COALESCE(o.medical_type, 'OTHER'), 50),
    left(o.name, 500),
    left(o.phone, 50),
    left(o.state_code, 64),               -- region_code
    left(o.tin, 50),
    o.uuid,
    left(o.line, 255),                    -- address_line
    NULL, NULL, NULL,
    left(o.name, 500),                    -- name_uz <- name
    NULL
FROM public.organization o;

-- parent_id (self-FK) — endi barcha qatorlar bor
UPDATE public2.organization t
SET    parent_id = o.parent_id
FROM   public.organization o
WHERE  o.id = t.id
  AND  o.parent_id IS NOT NULL;

-- ---- organization_service_types (1:1) --------------------------------
INSERT INTO public2.organization_service_types (organization_id, service_type)
SELECT ost.organization_id, ost.service_type
FROM   public.organization_service_types ost
JOIN   public.organization o ON o.id = ost.organization_id;   -- orphan himoyasi

COMMIT;

\echo '10-organization OK'
SELECT 'organization' t, (SELECT count(*) FROM public.organization) src,
       (SELECT count(*) FROM public2.organization) dst
UNION ALL
SELECT 'organization_service_types', (SELECT count(*) FROM public.organization_service_types),
       (SELECT count(*) FROM public2.organization_service_types);
