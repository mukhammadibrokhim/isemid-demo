-- =====================================================================
-- 96-fix-organization-provider-key.sql  —  public2.organization.provider_key
--                                           backfill (legacy -> SSO)
--
-- QACHON: `10-organization.sql` `provider_key`ni backfill qilishdan OLDIN
-- ishga tushirilgan bo'lsa (ya'ni migratsiya avval bir marta bajarilgan va
-- qayta 00-prep bilan boshdan ishga tushirilmoqchi emassiz), migratsiya
-- qilingan tashkilotlarda `provider_key` NULL bo'lib qoladi. Buning oqibati:
-- `OrganizationSyncService#syncByUuid` (admin-triggered qayta sinxronlash)
-- caller providerKeyOverride yubormasa, saqlangan (NULL) qiymatga tayanadi
-- va "providerKey must be specified..." xatosini beradi. Kundalik login
-- (`resolve()` orqali uuid bo'yicha topish) buzilmaydi — bu faqat admin
-- qo'lda qayta sinxronlash qilganda ko'rinadi.
--
-- SABAB: legacy `isemid` tizimi DHP provayderi paydo bo'lishidan oldingi
-- davrga tegishli — barcha tashkilotlar faqat SSV SSO orqali sinxronlangan.
--
-- BU FAYL XAVFSIZ: faqat `provider_key IS NULL` bo'lgan qatorlarni yangilaydi,
-- sentinel tashkilotga (id=0, uuid=all-zero — haqiqiy IAM sync orqali hech
-- qachon topilmaydi) tegmaydi. Istalgan marta qayta ishga tushirsa bo'ladi
-- (ikkinchi marta ishga tushirilganda 0 qator yangilanadi). `00-prep` KERAK
-- EMAS.
--
-- Windows:
--   $env:PGPASSWORD='parol'
--   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5434 -U postgres -d isemid -v ON_ERROR_STOP=1 -X -f 96-fix-organization-provider-key.sql
-- Linux:
--   PGPASSWORD=parol psql -h localhost -p 5434 -U postgres -d isemid -v ON_ERROR_STOP=1 -X -f 96-fix-organization-provider-key.sql
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;

UPDATE public2.organization
SET    provider_key = 'sso'
WHERE  provider_key IS NULL
  AND  id <> 0;

COMMIT;

\echo '96-fix-organization-provider-key OK'
SELECT count(*) AS still_null
FROM   public2.organization
WHERE  provider_key IS NULL
  AND  id <> 0;
