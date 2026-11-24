-- =====================================================================
-- 25-user-roles.sql  —  public.user_roles -> public2.user_roles
--                       (role.name crosswalk, id'lar bo'yicha EMAS)
--
-- Legacy role.id lar public2 dagi id lar bilan mos kelmaydi (public2.role
-- Liquibase seed'i), shuning uchun rol NOMI bo'yicha moslashtiriladi.
-- Faqat user -> rol bog'lanishi ko'chadi. Rolning permission'lari (subject/action)
-- KO'CHIRILMAYDI: ular public2 da Liquibase role-matrix seed'idan keladi
-- (iam/20261118-1100 + zzz-rbac-grants), eski subject/action'lar retire qilingan.
--
-- Mos kelmagan / public2 da yo'q (yoki deleted) rol -> _migration_notes ga yoziladi,
-- qator tashlanadi. user_local_roles ko'chirilmaydi (yangi ilova ishlatmaydi).
-- Mapping: docs/legacy-migration/02-mapping-5434.md §5.6
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;

-- Crosswalk: legacy nom (lower, 'role_' prefiksisiz) -> public2.role.name.
-- Legacy RoleServiceImpl 8 ta isemid_* rolni seed qilgan, public2 da ham shu nomlar.
-- Qo'shimcha nom kerak bo'lsa (masalan custom legacy rol), shu yerga qator qo'shing.
-- isemid_user / isemid_doctor public2 da retire qilingan: ataylab mapping YO'Q (notes'ga tushadi).
CREATE TEMP TABLE _role_xwalk (legacy_name text PRIMARY KEY, new_name text NOT NULL);
INSERT INTO _role_xwalk (legacy_name, new_name) VALUES
    ('isemid_super_admin',              'isemid_super_admin'),
    ('isemid_admin',                    'isemid_admin'),
    ('isemid_epidim_head',              'isemid_epidim_head'),
    ('isemid_head_doctor',              'isemid_head_doctor'),
    ('isemid_epidemiologist',           'isemid_epidemiologist'),
    ('isemid_assistant_epidemiologist', 'isemid_assistant_epidemiologist'),
    ('isemid_general_practitioner',     'isemid_general_practitioner'),
    ('isemid_qr_user',                  'isemid_qr_user');

CREATE TEMP TABLE _legacy_user_role AS
SELECT ur.user_id,
       ur.role_id                                              AS legacy_role_id,
       r.name                                                  AS legacy_name,
       regexp_replace(lower(btrim(r.name)), '^role_', '')      AS legacy_key
FROM   public.user_roles ur
JOIN   public.role r ON r.id = ur.role_id
JOIN   public2.users u ON u.id = ur.user_id;       -- faqat ko'chgan userlar

INSERT INTO public2.user_roles (user_id, role_id)
SELECT DISTINCT l.user_id, nr.id
FROM   _legacy_user_role l
JOIN   _role_xwalk x  ON x.legacy_name = l.legacy_key
JOIN   public2.role nr ON nr.name = x.new_name AND nr.deleted = false
ON CONFLICT DO NOTHING;

-- Ko'chmagan bog'lanishlar -> log (userlar rolsiz qolmasin, qo'lda ko'rib chiqiladi)
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'user_roles', l.user_id,
       'role not mapped - user_roles row dropped',
       'legacy_role=' || coalesce(l.legacy_name, '?') ||
       CASE WHEN x.legacy_name IS NULL THEN '; no crosswalk entry'
            ELSE '; target role ' || x.new_name || ' missing/deleted in public2' END
FROM   _legacy_user_role l
LEFT   JOIN _role_xwalk x ON x.legacy_name = l.legacy_key
WHERE  NOT EXISTS (
         SELECT 1
         FROM   public2.role nr
         WHERE  nr.name = x.new_name AND nr.deleted = false);

COMMIT;

\echo '25-user-roles OK'
SELECT 'user_roles (legacy, migrated users only)' t, (SELECT count(*) FROM _legacy_user_role) n
UNION ALL
SELECT 'user_roles (public2)', (SELECT count(*) FROM public2.user_roles)
UNION ALL
SELECT 'unmapped (see _migration_notes)',
       (SELECT count(*) FROM public2._migration_notes WHERE source_table = 'user_roles');
