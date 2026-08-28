-- =====================================================================
-- 90-finalize.sql  —  orqaga havolalar, sequence'lar, validatsiya
-- Barcha 10..61 fayllar bajarilgandan keyin.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

-- ---- form058.assigned_card_id --------------------------------------
-- Legacy'da yo'q. Aynan bitta bog'langan card bo'lsa -> o'sha; ko'p bo'lsa NULL + note.
UPDATE public2.form058 f
SET    assigned_card_id = c.id
FROM   public2.card c
WHERE  c.form058_id = f.id AND f.id <> 0
  AND (SELECT count(*) FROM public2.card c2 WHERE c2.form058_id = f.id) = 1;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'form058.assigned_card_id', f.id, 'bir nechта card — assigned_card_id NULL',
       string_agg(c.id::text, ', ')
FROM public2.form058 f
JOIN public2.card c ON c.form058_id = f.id
WHERE f.id <> 0
GROUP BY f.id HAVING count(*) > 1;

-- ---- barcha public2 sequence'larni max(id) ga surish ---------------
DO $$
DECLARE r record;
BEGIN
  FOR r IN
    SELECT s.relname AS seq, t.relname AS tbl, a.attname AS col
    FROM pg_class s
    JOIN pg_depend  d ON d.objid = s.oid AND d.deptype = 'a'
    JOIN pg_class   t ON t.oid = d.refobjid
    JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = d.refobjsubid
    JOIN pg_namespace n ON n.oid = s.relnamespace
    WHERE s.relkind = 'S' AND n.nspname = 'public2'
  LOOP
    EXECUTE format(
      'SELECT setval(%L, GREATEST((SELECT COALESCE(max(%I),0) FROM public2.%I), 1), (SELECT count(*) > 0 FROM public2.%I))',
      'public2.'||r.seq, r.col, r.tbl, r.tbl);
  END LOOP;
END $$;

COMMIT;

-- =====================================================================
-- VALIDATSIYA (faqat hisobot)
-- =====================================================================
\echo '--- qator sonlari: src (public) vs dst (public2) ---'
\echo '    (dst >= src bo''lishi kerak — sentinel org/form058 tufayli organization/form058 da +1)'
SELECT 'organization' t, (SELECT count(*) FROM public.organization) src, (SELECT count(*) FROM public2.organization) dst
UNION ALL SELECT 'users',        (SELECT count(*) FROM public.users),        (SELECT count(*) FROM public2.users)
UNION ALL SELECT 'patient',      (SELECT count(*) FROM public.patient),      (SELECT count(*) FROM public2.patient)
UNION ALL SELECT 'pt_address',   (SELECT count(*) FROM public.pt_address),   (SELECT count(*) FROM public2.pt_address)
UNION ALL SELECT 'pt_affiliation',(SELECT count(*) FROM public.pt_affiliation),(SELECT count(*) FROM public2.pt_affiliation)
UNION ALL SELECT 'pt_identifier',(SELECT count(*) FROM public.pt_identifier),(SELECT count(*) FROM public2.pt_identifier)
UNION ALL SELECT 'fm058_location',(SELECT count(*) FROM public.fm058_location),(SELECT count(*) FROM public2.fm058_location)
UNION ALL SELECT 'form058',      (SELECT count(*) FROM public.form058),      (SELECT count(*) FROM public2.form058)
UNION ALL SELECT 'form058_1',    (SELECT count(*) FROM public.form058_1),    (SELECT count(*) FROM public2.form058_1)
UNION ALL SELECT 'form058_1_other_inj', (SELECT count(*) FROM public.fm0581_bitten_person), (SELECT count(*) FROM public2.form058_1_other_injured_person)
UNION ALL SELECT 'card',         (SELECT count(*) FROM public.card),         (SELECT count(*) FROM public2.card)
UNION ALL SELECT 'card161',      (SELECT count(*) FROM public.card161),      (SELECT count(*) FROM public2.card161)
UNION ALL SELECT 'card174',      (SELECT count(*) FROM public.card174),      (SELECT count(*) FROM public2.card174)
UNION ALL SELECT 'card175',      (SELECT count(*) FROM public.card175),      (SELECT count(*) FROM public2.card175)
UNION ALL SELECT 'card205',      (SELECT count(*) FROM public.card205),      (SELECT count(*) FROM public2.card205)
UNION ALL SELECT 'card_tube',    (SELECT count(*) FROM public.card_tube),    (SELECT count(*) FROM public2.card_tube)
UNION ALL SELECT 'act',          (SELECT count(*) FROM public.act),          (SELECT count(*) FROM public2.act)
UNION ALL SELECT 'act153',       (SELECT count(*) FROM public.act153),       (SELECT count(*) FROM public2.act153)
UNION ALL SELECT 'act154',       (SELECT count(*) FROM public.act154),       (SELECT count(*) FROM public2.act154)
UNION ALL SELECT 'act155(detal, ko''chmaydi)', (SELECT count(*) FROM public.act155), 0
UNION ALL SELECT 'act156',       (SELECT count(*) FROM public.act156),       (SELECT count(*) FROM public2.act156)
UNION ALL SELECT 'act223',       (SELECT count(*) FROM public.act223),       (SELECT count(*) FROM public2.act223)
UNION ALL SELECT 'act224',       (SELECT count(*) FROM public.act224),       (SELECT count(*) FROM public2.act224)
ORDER BY 1;

\echo '--- JOINED butunlik (0 kutiladi; act155 lar "act subtype yo''q" da chiqadi — bu normal) ---'
SELECT 'card subtype yo''q' issue, count(*) n
FROM public2.card c WHERE c.id <> 0
  AND NOT EXISTS (SELECT 1 FROM public2.card161 x WHERE x.id=c.id)
  AND NOT EXISTS (SELECT 1 FROM public2.card174 x WHERE x.id=c.id)
  AND NOT EXISTS (SELECT 1 FROM public2.card175 x WHERE x.id=c.id)
  AND NOT EXISTS (SELECT 1 FROM public2.card205 x WHERE x.id=c.id)
  AND NOT EXISTS (SELECT 1 FROM public2.card_tube x WHERE x.id=c.id)
UNION ALL
SELECT 'act subtype yo''q (act155dan tashqari xato)', count(*)
FROM public2.act a
WHERE a.act_type <> 'ACT155'
  AND NOT EXISTS (SELECT 1 FROM public2.act153 x WHERE x.id=a.id)
  AND NOT EXISTS (SELECT 1 FROM public2.act154 x WHERE x.id=a.id)
  AND NOT EXISTS (SELECT 1 FROM public2.act156 x WHERE x.id=a.id)
  AND NOT EXISTS (SELECT 1 FROM public2.act223 x WHERE x.id=a.id)
  AND NOT EXISTS (SELECT 1 FROM public2.act224 x WHERE x.id=a.id);

\echo '--- src qatorlar 100% ko''chganmi (dst da id bor-yo''qligi) ---'
SELECT 'form058 yo''qolган' t, count(*) n FROM public.form058 s WHERE NOT EXISTS (SELECT 1 FROM public2.form058 d WHERE d.id=s.id)
UNION ALL SELECT 'form058_1 yo''qolган', count(*) FROM public.form058_1 s WHERE NOT EXISTS (SELECT 1 FROM public2.form058_1 d WHERE d.id=s.id)
UNION ALL SELECT 'card yo''qolган',    count(*) FROM public.card s      WHERE NOT EXISTS (SELECT 1 FROM public2.card d WHERE d.id=s.id)
UNION ALL SELECT 'act yo''qolган',     count(*) FROM public.act s       WHERE NOT EXISTS (SELECT 1 FROM public2.act d WHERE d.id=s.id)
UNION ALL SELECT 'patient yo''qolган', count(*) FROM public.patient s   WHERE NOT EXISTS (SELECT 1 FROM public2.patient d WHERE d.id=s.id);

\echo '--- note-log xulosasi ---'
SELECT source_table, note, count(*) n
FROM public2._migration_notes
GROUP BY source_table, note
ORDER BY 1, 3 DESC;

\echo '90-finalize OK — public schema HALI SAQLANADI'
