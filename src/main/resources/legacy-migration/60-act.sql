-- =====================================================================
-- 60-act.sql  —  public.act -> public2.act  (+ act_users)
-- TAMOYIL: har bir qator ko'chiriladi (ACT155 BAZAVIY qatori ham).
--   Faqat act155/act155_detail DETALI ko'chmaydi (target'da bunday jadval yo'q)
--   -> 61-act-subtypes da act155 bo'limi yo'q; bu yerda note yoziladi.
--   created_org_uuid (NOT NULL + FK): legacy -> card -> sentinel org (butun-nol).
-- Mapping: docs/legacy-migration/02-mapping-5434.md §4
-- Bog'liqlik: 50-card, 20-users bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

INSERT INTO public2.act (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    created_org_uuid, updated_org_uuid, act_status, act_type, card_id,
    assigned_by_id, result_comment, subject_type, tin,
    institution_name, institution_address, institution_legal_address,
    lis_attempt, lis_sent_date, lis_act_id, lis_response, lis_last_error,
    deleted, deleted_at, deleted_by_id, delete_reason
)
SELECT
    l.id, 0,
    l.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', l.created_by_id,
    l.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', l.updated_by_id, l.uuid,
    COALESCE(
        l.created_org_uuid,
        (SELECT c.created_org_uuid FROM public2.card c WHERE c.id = l.card_id),
        '00000000-0000-0000-0000-000000000000'
    ),
    l.updated_org_uuid,
    CASE l.status
        WHEN 'NEW'          THEN 'NEW'
        WHEN 'NOT_VIEWED'   THEN 'NEW'
        WHEN 'IN_PROGRESS'  THEN 'IN_PROGRESS'
        WHEN 'PENDING'      THEN 'READY'
        WHEN 'SENT'         THEN 'SENT'
        WHEN 'FAILED'       THEN 'SEND_FAILED'
        WHEN 'RECEIVED'     THEN 'COMPLETED'
        WHEN 'COMPLETED'    THEN 'COMPLETED'
        WHEN 'ACT_ATTACHED' THEN 'COMPLETED'
        ELSE 'NEW'
    END,
    -- ACT155 target'da yo'q (2026-08-24): ACT154 ga aylantiriladi, detali 61-act-subtypes da act154 ga ko'chadi
    CASE WHEN l.act_type = 'ACT155' THEN 'ACT154' ELSE left(l.act_type, 50) END,
    (SELECT c.id FROM public2.card c WHERE c.id = l.card_id),   -- yetim -> NULL
    NULL, NULL,
    -- subject_type = SubjectType enum; noma'lum qiymat entity yuklanishini yiqitadi -> NULL (+ note, pastda)
    CASE WHEN l.subject_type IN ('LEGAL_ENTITY', 'GEOGRAPHIC_POINT', 'PHYSICAL_PERSON') THEN l.subject_type END,
    -- subject_* (legacy act'ning yangi variantida) -> tin / institution_* (asosiy qiymat ustun)
    COALESCE(l.tin::text, to_jsonb(l)->>'subject_tin'),
    COALESCE(l.institution_name, to_jsonb(l)->>'subject_legal_name'),
    COALESCE(l.institution_address, to_jsonb(l)->>'subject_actual_address'),
    COALESCE(l.institution_legal_address, to_jsonb(l)->>'subject_legal_address'),
    COALESCE(l.lis_attempt, 0),
    l.lis_sent_date,
    l.lis_act_id,
    CASE WHEN l.lis_response IS NULL OR btrim(l.lis_response) = '' THEN NULL
         WHEN l.lis_response ~ '^\s*[{\[]' THEN l.lis_response::jsonb
         ELSE jsonb_build_object('_raw', l.lis_response) END,
    NULL,
    false, NULL, NULL, NULL
FROM public.act l;

-- ACT155: ACT154 ga aylantirildi (detali 61-act-subtypes da act154/act154_detail ga)
INSERT INTO public2._migration_notes (source_table, source_id, note)
SELECT 'act', l.id, 'ACT155 converted to ACT154 (act155/act155_detail migrated in 61-act-subtypes)'
FROM public.act l WHERE l.act_type = 'ACT155';

-- subject_type enum'da yo'q -> NULL qilindi
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act', l.id, 'subject_type not in SubjectType enum -> NULL', l.subject_type
FROM public.act l
WHERE l.subject_type IS NOT NULL
  AND l.subject_type NOT IN ('LEGAL_ENTITY', 'GEOGRAPHIC_POINT', 'PHYSICAL_PERSON');

-- sentinel org ishlatilган
INSERT INTO public2._migration_notes (source_table, source_id, note)
SELECT 'act', l.id, 'created_org_uuid empty -> card/sentinel org'
FROM public.act l
WHERE l.created_org_uuid IS NULL;

-- ---- act_users (1:1) ---------------------------------------------
INSERT INTO public2.act_users (act_id, user_id)
SELECT au.act_id, au.user_id
FROM   public.act_users au
JOIN   public2.act a  ON a.id = au.act_id
JOIN   public2.users u ON u.id = au.user_id;

COMMIT;

\echo '60-act OK'
SELECT 'act' t, (SELECT count(*) FROM public.act) src, (SELECT count(*) FROM public2.act) dst
UNION ALL
SELECT 'act NOTE', (SELECT count(*) FROM public2._migration_notes WHERE source_table='act'), NULL
UNION ALL
SELECT 'act_users', (SELECT count(*) FROM public.act_users), (SELECT count(*) FROM public2.act_users);
