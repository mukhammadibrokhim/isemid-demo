-- =====================================================================
-- 95-fix-sequences.sql  —  public2 identity sequence'larini max(id) ga surish
--
-- QACHON: legacy ko'chirishдан keyin ilova birinchi INSERT'da
--   duplicate key value violates unique constraint "<jadval>_pkey"
--   Key (id)=(N) already exists
-- xatosini bersa (masalan `pt_address_pkey`).
--
-- SABAB: `00-prep` `RESTART IDENTITY` sequence'larni 1 ga qaytaradi;
-- keyin qatorlar EXPLICIT id bilan kiritiladi (identity sequence surilmaydi);
-- eski `90-finalize` `deptype='a'` filtri identity sequence'larni topa olmagan.
--
-- BU FAYL XAVFSIZ: hech qanday qator o'zgartirmaydi/o'chirmaydi, faqat
-- setval. Istalgan marta qayta ishga tushirса bo'ladi. `00-prep` KERAK EMAS.
--
-- Windows:
--   $env:PGPASSWORD='parol'
--   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5434 -U postgres -d isemid -v ON_ERROR_STOP=1 -X -f 95-fix-sequences.sql
-- Linux:
--   PGPASSWORD=parol psql -h localhost -p 5434 -U postgres -d isemid -v ON_ERROR_STOP=1 -X -f 95-fix-sequences.sql
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;

DO $$
DECLARE
  r         record;
  v_seq     text;
  v_max     bigint;
  v_newval  bigint;
BEGIN
  FOR r IN
    SELECT c.relname AS tbl, a.attname AS col
    FROM pg_class     c
    JOIN pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_attribute a ON a.attrelid = c.oid AND a.attnum > 0 AND NOT a.attisdropped
    WHERE c.relkind = 'r'
      AND n.nspname = 'public2'
      AND pg_get_serial_sequence(format('%I.%I', n.nspname, c.relname), a.attname) IS NOT NULL
    ORDER BY c.relname, a.attname
  LOOP
    v_seq := pg_get_serial_sequence(format('public2.%I', r.tbl), r.col);
    EXECUTE format('SELECT COALESCE(max(%I), 0) FROM public2.%I', r.col, r.tbl) INTO v_max;
    -- rows bor bo'lsa: is_called=true, keyingi nextval = v_max+1
    -- rows yo'q bo'lsa: is_called=false, keyingi nextval = 1
    PERFORM setval(v_seq, GREATEST(v_max, 1), v_max > 0);
    v_newval := GREATEST(v_max, 1);
    RAISE NOTICE '% . % -> seq %  (max_id=%, keyingi nextval=%)',
      r.tbl, r.col, v_seq, v_max, CASE WHEN v_max > 0 THEN v_max + 1 ELSE 1 END;
  END LOOP;
END $$;

COMMIT;

\echo '95-fix-sequences OK — endi ilova INSERT lari to''qnashmasligi kerak'
