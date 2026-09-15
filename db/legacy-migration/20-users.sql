-- =====================================================================
-- 20-users.sql  —  public.users -> public2.users
--                  public.users_organizations -> public2 (1:1)
--   user_roles: KO'CHIRILMAYDI (qaror — 02-mapping §5.6)
-- Mapping: docs/legacy-migration/02-mapping-5434.md §1
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

-- Rename: city_code->district_code, state_code->region_code
-- Drop:   password, position_code, ppn, district
-- Backfill: version=0; created_by_id/updated_by_id=NULL; username<-nnuzb;
--           created_at/updated_at NULL -> now()
INSERT INTO public2.users (
    id, version, created_at, created_by_id, updated_at, updated_by_id,
    active, birth_date, district_code, country_code, first_name, gender_code,
    last_name, line, middle_name, nnuzb, phone_number, region_code, username, uuid
)
SELECT
    u.id,
    0,
    COALESCE(u.created_at, now())::timestamp AT TIME ZONE 'Asia/Tashkent',
    NULL,
    COALESCE(u.updated_at, u.created_at, now())::timestamp AT TIME ZONE 'Asia/Tashkent',
    NULL,
    u.active,
    u.birth_date,
    left(u.city_code, 20),        -- district_code
    left(u.country_code, 20),
    left(u.first_name, 200),
    left(u.gender_code, 20),
    left(u.last_name, 200),
    left(u.line, 255),
    left(u.middle_name, 200),
    left(u.nnuzb, 32),
    left(u.phone_number, 20),
    left(u.state_code, 20),       -- region_code
    left(u.nnuzb, 200),           -- username <- nnuzb
    u.uuid
FROM public.users u;

-- ---- users_organizations (1:1) --------------------------------------
INSERT INTO public2.users_organizations (user_id, organization_id)
SELECT uo.user_id, uo.organization_id
FROM   public.users_organizations uo
JOIN   public.users u        ON u.id = uo.user_id
JOIN   public.organization o  ON o.id = uo.organization_id;

COMMIT;

\echo '20-users OK'
SELECT 'users' t, (SELECT count(*) FROM public.users) src,
       (SELECT count(*) FROM public2.users) dst
UNION ALL
SELECT 'users_organizations', (SELECT count(*) FROM public.users_organizations),
       (SELECT count(*) FROM public2.users_organizations);
