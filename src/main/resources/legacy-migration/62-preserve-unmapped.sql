-- =====================================================================
-- 62-preserve-unmapped.sql  —  yangi sxemada o'rni yo'q legacy ustunlar qiymati
-- Hech narsa yo'qolmasin: bu ustunlarning bo'sh bo'lmagan qiymatlari har bir qator
-- uchun public2._migration_notes ga JSON ko'rinishida yoziladi (source_table,
-- source_id = legacy id, details = {"ustun": qiymat}). Ustun legacy nusxada bo'lmasa
-- (to_jsonb orqali o'qiladi) xato bermaydi, shunchaki o'tkazib yuboriladi.
-- users.password ataylab olinmaydi (SSO; parol hash'i hech qayerga ko'chirilmaydi).
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act153', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act153 l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('document_send_date', 'hospitalization_date', 'participant_position', 'position', 'sample_object_name', 'loinc') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act153_detail', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act153_detail l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('additional_info', 'laboratory_address', 'organization_id', 'sampling_purpose', 'sampling_purpose_loinc', 'storage_conditions', 'object_name', 'sample_name') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act154', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act154 l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('document_send_date', 'hospitalization_date', 'position', 'serial_number') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act154_detail', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act154_detail l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('doc_number_of_taken_object', 'document_confirm_sampling', 'laboratory_address', 'manufacture_date', 'manufacturing_company', 'organization_id', 'sampling_purpose_loinc') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act155', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act155 l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('document_send_date') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act155_detail', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act155_detail l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('created_at', 'created_by_id', 'updated_at', 'updated_by_id', 'uuid', 'organization_id', 'product_name', 'loinc') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act156', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act156 l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('document_send_date', 'organization_id') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act223', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act223 l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('document_send_date', 'full_name_of_participant', 'full_nameof_sampler', 'institution_address', 'institution_name', 'position_of_sampler', 'loinc') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act223_detail', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act223_detail l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('supporting_documents_for_sampling') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'act224', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.act224 l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('document_send_date') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card174_disinfection_info', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.card174_disinfection_info l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('created_at', 'created_by_id', 'updated_at', 'updated_by_id', 'uuid') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'fm058_location', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.fm058_location l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('uuid') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'organization', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.organization l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('country_code', 'district') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'users', l.id, 'legacy column(s) with no target - values preserved', q.v::text
FROM public.users l
CROSS JOIN LATERAL (
    SELECT jsonb_object_agg(e.k, e.v) AS v
    FROM jsonb_each(to_jsonb(l)) e(k, v)
    WHERE e.k IN ('district') AND e.v <> 'null'::jsonb
) q
WHERE q.v IS NOT NULL;

COMMIT;

\echo '62-preserve-unmapped OK'
