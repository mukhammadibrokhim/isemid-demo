-- =====================================================================
-- 97-backfill-form058-accept-approve-info.sql
--   public2.form058 / public2.form058_1:
--   accepted_by_id, accepted_at, approved_by_id, approved_at,
--   approved_organization_id backfill
--
-- SABAB: `40-form058.sql`/`45-form058-1.sql` legacy migratsiyasi
-- approve audit uchligini (`approved_by_id`, `approved_organization_id`,
-- `approved_at`) ataylab NULL qoldirgan - legacy tizimda faqat
-- `approved_full_name`/`approved_org_uuid` (erkin matn/eski uuid) bor edi,
-- yangi ichki id'larga mos kelmaydi (`docs/legacy-migration/10-form058.md`).
-- Xuddi shunday, `accepted_by_id`/`accepted_at` ustunlari (2026-09-18
-- migratsiyasida qo'shilgan) legacy'dan ko'chirilgan qatorlarda umuman
-- to'ldirilmagan.
--
-- STRATEGIYA (ikki bosqichli, faqat NULL bo'lgan qatorlarga tegadi):
--   1-BOSQICH (aniq manba): `audit_event` - har bir ACCEPTED/APPROVED
--      status o'zgarishini alohida actor_user_id + occurred_at bilan
--      yozib boradi (ilova orqali sodir bo'lgan har bir accept/approve
--      uchun). Bir nechta qayta ochish/qabul holatida ENG SO'NGGI voqea
--      olinadi.
--   2-BOSQICH (taxminiy zaxira): audit_event yozuvi yo'q qatorlar uchun
--      (legacy'dan ko'chirilgan, hech qachon ilova orqali accept/approve
--      bo'lmagan) - `updated_by_id`/`updated_at` dan olinadi. Bu aniq
--      "kim accept/approve qildi" emas, balki yozuvga oxirgi tegilgan
--      foydalanuvchi/vaqt - eng yaqin mavjud taxmin sifatida ishlatiladi.
--   3-BOSQICH: `approved_organization_id` - legacy'dan saqlanib qolgan
--      `approved_org_uuid` orqali `organization.uuid`'ga moslab topiladi
--      (agar shu tashkilot hozirgi bazada mavjud bo'lsa).
--
-- BU FAYL XAVFSIZ: har bir UPDATE faqat tegishli ustun hali NULL bo'lgan
-- qatorlarni yangilaydi - istalgan marta qayta ishga tushirsa bo'ladi
-- (ikkinchi marta ishga tushirilganda 0 qator yangilanadi). `00-prep`
-- KERAK EMAS.
--
-- Windows:
--   $env:PGPASSWORD='parol'
--   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5434 -U postgres -d isemid -v ON_ERROR_STOP=1 -X -f 97-backfill-form058-accept-approve-info.sql
-- Linux:
--   PGPASSWORD=parol psql -h localhost -p 5434 -U postgres -d isemid -v ON_ERROR_STOP=1 -X -f 97-backfill-form058-accept-approve-info.sql
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;

-- ---------------------------------------------------------------------
-- form058: acceptedBy/acceptedAt
-- ---------------------------------------------------------------------
UPDATE public2.form058 f
SET    accepted_by_id = ae.actor_user_id,
       accepted_at    = ae.occurred_at
FROM (
    SELECT DISTINCT ON (entity_id) entity_id, actor_user_id, occurred_at
    FROM   public2.audit_event
    WHERE  entity_type = 'FORM058'
      AND  event_type  = 'STATUS_CHANGED'
      AND  new_status  = 'ACCEPTED'
    ORDER BY entity_id, occurred_at DESC
) ae
WHERE f.id = ae.entity_id
  AND f.accepted_by_id IS NULL;

UPDATE public2.form058
SET    accepted_by_id = updated_by_id,
       accepted_at    = updated_at
WHERE  accepted_by_id IS NULL
  AND  status IN ('ACCEPTED', 'CARD_LINKED', 'APPROVED');

-- ---------------------------------------------------------------------
-- form058: approvedBy/approvedAt
-- ---------------------------------------------------------------------
UPDATE public2.form058 f
SET    approved_by_id = ae.actor_user_id,
       approved_at    = ae.occurred_at
FROM (
    SELECT DISTINCT ON (entity_id) entity_id, actor_user_id, occurred_at
    FROM   public2.audit_event
    WHERE  entity_type = 'FORM058'
      AND  event_type  = 'STATUS_CHANGED'
      AND  new_status  = 'APPROVED'
    ORDER BY entity_id, occurred_at DESC
) ae
WHERE f.id = ae.entity_id
  AND f.approved_by_id IS NULL;

UPDATE public2.form058
SET    approved_by_id = updated_by_id,
       approved_at    = updated_at
WHERE  approved_by_id IS NULL
  AND  status = 'APPROVED';

-- ---------------------------------------------------------------------
-- form058: approvedOrganizationId (from legacy approved_org_uuid)
-- ---------------------------------------------------------------------
UPDATE public2.form058 f
SET    approved_organization_id = o.id
FROM   public2.organization o
WHERE  f.approved_organization_id IS NULL
  AND  f.approved_org_uuid IS NOT NULL
  AND  o.uuid = f.approved_org_uuid;

-- ---------------------------------------------------------------------
-- form058_1: acceptedBy/acceptedAt
-- ---------------------------------------------------------------------
UPDATE public2.form058_1 f
SET    accepted_by_id = ae.actor_user_id,
       accepted_at    = ae.occurred_at
FROM (
    SELECT DISTINCT ON (entity_id) entity_id, actor_user_id, occurred_at
    FROM   public2.audit_event
    WHERE  entity_type = 'FORM0581'
      AND  event_type  = 'STATUS_CHANGED'
      AND  new_status  = 'ACCEPTED'
    ORDER BY entity_id, occurred_at DESC
) ae
WHERE f.id = ae.entity_id
  AND f.accepted_by_id IS NULL;

UPDATE public2.form058_1
SET    accepted_by_id = updated_by_id,
       accepted_at    = updated_at
WHERE  accepted_by_id IS NULL
  AND  status IN ('ACCEPTED', 'CARD_LINKED', 'APPROVED');

-- ---------------------------------------------------------------------
-- form058_1: approvedBy/approvedAt
-- ---------------------------------------------------------------------
UPDATE public2.form058_1 f
SET    approved_by_id = ae.actor_user_id,
       approved_at    = ae.occurred_at
FROM (
    SELECT DISTINCT ON (entity_id) entity_id, actor_user_id, occurred_at
    FROM   public2.audit_event
    WHERE  entity_type = 'FORM0581'
      AND  event_type  = 'STATUS_CHANGED'
      AND  new_status  = 'APPROVED'
    ORDER BY entity_id, occurred_at DESC
) ae
WHERE f.id = ae.entity_id
  AND f.approved_by_id IS NULL;

UPDATE public2.form058_1
SET    approved_by_id = updated_by_id,
       approved_at    = updated_at
WHERE  approved_by_id IS NULL
  AND  status = 'APPROVED';

-- ---------------------------------------------------------------------
-- form058_1: approvedOrganizationId (from legacy approved_org_uuid)
-- ---------------------------------------------------------------------
UPDATE public2.form058_1 f
SET    approved_organization_id = o.id
FROM   public2.organization o
WHERE  f.approved_organization_id IS NULL
  AND  f.approved_org_uuid IS NOT NULL
  AND  o.uuid = f.approved_org_uuid;

COMMIT;

\echo '97-backfill-form058-accept-approve-info OK'
SELECT 'form058 still-null accepted_by_id'         t, count(*) FROM public2.form058   WHERE accepted_by_id IS NULL AND status IN ('ACCEPTED','CARD_LINKED','APPROVED')
UNION ALL
SELECT 'form058 still-null approved_by_id'         t, count(*) FROM public2.form058   WHERE approved_by_id IS NULL AND status = 'APPROVED'
UNION ALL
SELECT 'form058 still-null approved_organization_id (uuid present)' t, count(*) FROM public2.form058 WHERE approved_organization_id IS NULL AND approved_org_uuid IS NOT NULL
UNION ALL
SELECT 'form058_1 still-null accepted_by_id'       t, count(*) FROM public2.form058_1 WHERE accepted_by_id IS NULL AND status IN ('ACCEPTED','CARD_LINKED','APPROVED')
UNION ALL
SELECT 'form058_1 still-null approved_by_id'       t, count(*) FROM public2.form058_1 WHERE approved_by_id IS NULL AND status = 'APPROVED'
UNION ALL
SELECT 'form058_1 still-null approved_organization_id (uuid present)' t, count(*) FROM public2.form058_1 WHERE approved_organization_id IS NULL AND approved_org_uuid IS NOT NULL;
