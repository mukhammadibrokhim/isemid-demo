-- =====================================================================
-- 50-card.sql  —  public.card -> public2.card  (+ card_users 1:1)
-- TAMOYIL: har bir qator ko'chiriladi.
--   created_org_uuid (NOT NULL + FK): legacy -> yoki bog'langan form -> yoki
--     sentinel organization (uuid = butun-nol).
--   card_type (NOT NULL): legacy -> yoki subtype jadvalidan aniqlanadi -> 'CARD161'.
--   CHECK chk_card_exactly_one_form: form058_id bo'lsa u; bo'lmasa form058_1_id;
--     ikkalasi ham yo'q -> sentinel form058(id=0).
-- Bog'liqlik: 40-form058, 45-form058-1 bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

INSERT INTO public2.card (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    created_org_uuid, updated_org_uuid, assigned_by_id, attached_user_comment,
    card_type, completed_date, status, supervisor_comment,
    form058_id, form058_1_id, deleted, deleted_at, deleted_by_id, delete_reason
)
SELECT
    c.id, 0,
    c.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', c.created_by_id,
    c.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', c.updated_by_id, c.uuid,
    COALESCE(
        c.created_org_uuid,
        (SELECT f.created_org_uuid FROM public2.form058 f WHERE f.id = c.form058_id),
        '00000000-0000-0000-0000-000000000000'
    ),
    c.updated_org_uuid, c.assigned_by_id, left(c.attached_user_comment, 1000),
    left(COALESCE(
        c.card_type,
        CASE
          WHEN EXISTS (SELECT 1 FROM public.card161   x WHERE x.id = c.id) THEN 'CARD161'
          WHEN EXISTS (SELECT 1 FROM public.card174   x WHERE x.id = c.id) THEN 'CARD174'
          WHEN EXISTS (SELECT 1 FROM public.card175   x WHERE x.id = c.id) THEN 'CARD175'
          WHEN EXISTS (SELECT 1 FROM public.card205   x WHERE x.id = c.id) THEN 'CARD205'
          WHEN EXISTS (SELECT 1 FROM public.card_tube x WHERE x.id = c.id) THEN 'CARD_TUBE'
          ELSE 'CARD161'
        END
    ), 20),
    c.completed_date,
    COALESCE(c.status, 'NEW'),
    left(c.supervisor_comment, 1000),
    -- CHECK: aynan bitta
    CASE WHEN c.form058_id IS NOT NULL THEN c.form058_id
         WHEN c.form058_1_id IS NOT NULL THEN NULL
         ELSE 0 END,                                  -- sentinel form058
    CASE WHEN c.form058_id IS NULL AND c.form058_1_id IS NOT NULL THEN c.form058_1_id
         ELSE NULL END,
    false, NULL, NULL, NULL
FROM public.card c;

-- note-log: sentinel/derive ishlatilган qatorlar
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card', c.id, 'sentinel/derive ishlatildi',
       concat_ws('; ',
         CASE WHEN c.created_org_uuid IS NULL THEN 'created_org_uuid -> form/sentinel' END,
         CASE WHEN c.card_type IS NULL THEN 'card_type subtype''dan aniqlandi' END,
         CASE WHEN c.status IS NULL THEN 'status -> NEW' END,
         CASE WHEN c.form058_id IS NULL AND c.form058_1_id IS NOT NULL THEN 'form058_1 ga bog''landi' END,
         CASE WHEN c.form058_id IS NULL AND c.form058_1_id IS NULL THEN 'formaga bog''lanmagan -> sentinel form058(0)' END,
         CASE WHEN c.form058_id IS NOT NULL AND c.form058_1_id IS NOT NULL THEN 'ikkala form ham bor edi -> form058 saqlandi' END
       )
FROM public.card c
WHERE c.created_org_uuid IS NULL OR c.card_type IS NULL OR c.status IS NULL
   OR c.form058_id IS NULL OR c.form058_1_id IS NOT NULL;

-- ---- card_users (1:1) ----------------------------------------------
INSERT INTO public2.card_users (card_id, user_id)
SELECT cu.card_id, cu.user_id
FROM   public.card_users cu
JOIN   public2.card c ON c.id = cu.card_id
JOIN   public2.users u ON u.id = cu.user_id;

COMMIT;

\echo '50-card OK'
SELECT 'card' t, (SELECT count(*) FROM public.card) src, (SELECT count(*) FROM public2.card) dst
UNION ALL
SELECT 'card NOTE', (SELECT count(*) FROM public2._migration_notes WHERE source_table='card'), NULL
UNION ALL
SELECT 'card_users', (SELECT count(*) FROM public.card_users), (SELECT count(*) FROM public2.card_users);
