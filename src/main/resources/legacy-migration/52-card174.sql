-- =====================================================================
-- 52-card174.sql  —  card174 (JOINED subtype) + element + bola jadvallari
-- ⚠️ card174 BAZA jadvali: legacy va target ustun nomlari DEYARLI DISJOINT.
--    Quyidagi juftliklar TAXMIN. `-- ??` bilan belgilanganlar TEKSHIRILSIN.
--    Manba taxminlari: docs/legacy-migration/20-card.md §4a.
-- Orphan bola jadvallar (disinfection_info / preventive_measure) target'da
--    yo'q -> ma'lumot card174 ota ustunlariga (birinchi qator; ko'p bo'lsa
--    log).  Qaror: 02-mapping §5.4.
-- card174_external_sample_test -> card174_laboratory_test (HAMMA qatorlar).
-- card174_preventive_measure   -> card174_animal_measure  (HAMMA qatorlar;
--    1-qatorning measures matni precautionary_measures'ga ham ko'chadi).
-- Bog'liqlik: 50-card bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

INSERT INTO public2.card174 (
    id,
    affected_animal_count, affected_animal_type_code, affected_humans, affected_in_outbreak,
    animal_disposal_date, animal_disposal_method_code, animal_owner, animal_ownership_code,
    animal_primary_diagnosis, blood_sucking_arthropods_increase,
    current_animal_infection_date, data_obtained_date, deratization_area, deratization_code,
    disinfected_factor_amount, disinfection_date, epizootology_existence, execution_control_results,
    human_primary_diagnosis, including_identified, including_industrial_conditions, including_who_applied,
    inspectors, investigation_date, is_area_exotic, isolation, last_disease_year, location_of_event,
    measure_taken, meat_submission, icd10_code, icd10_name, outbreak_localization, owner_address,
    pathogen_type, precautionary_measures, quarantine_end_date, quarantine_start_date, quarantine_type_code,
    report_to_veterinary_department_date, rodent_increase, serial_doc_number, stray_animal_capture,
    synanthropic_rodents_increase,
    treated_humans, treatment, vector_increase, wild_animal_culling, wild_rodents_increase,
    additional_measures_info
)
SELECT
    l.id,
    l.affected_animal_count,
    l.affected_animal_code,                 -- affected_animal_type_code
    l.infected_humans,                      -- affected_humans        -- ?? infected_humans
    l.infected_at_outbreak,                 -- affected_in_outbreak
    l.disposal_date,                        -- animal_disposal_date
    l.disposal_method_code,                 -- animal_disposal_method_code
    l.owner_name,                           -- animal_owner
    l.ownership_code,                       -- animal_ownership_code
    l.animal_diag,                          -- animal_primary_diagnosis
    l.arthropods_increased,                 -- blood_sucking_arthropods_increase
    l.current_animal_infection_date,
    l.data_date,                            -- data_obtained_date
    l.derat_area,                           -- deratization_area
    l.derat_code,                           -- deratization_code
    di.disinfected_count,                   -- disinfected_factor_amount  <- card174_disinfection_info
    di.disinfection_date,                   -- disinfection_date          <- card174_disinfection_info
    l.epizootology_exists,                  -- epizootology_existence
    di.control_results,                     -- execution_control_results  <- card174_disinfection_info
    l.human_diag,                           -- human_primary_diagnosis
    l.confirmed_cases,                      -- including_identified   -- ?? confirmed_cases
    l.at_industry,                          -- including_industrial_conditions -- ??
    NULL,                                   -- including_who_applied  -- ?? legacy manba yo'q
    l.inspectors,
    l.investigation_date,
    l.is_exotic_area,                       -- is_area_exotic
    l.isolation,
    EXTRACT(YEAR FROM l.last_case_year)::int, -- last_disease_year (faqat yil)
    di.event_location,                      -- location_of_event      <- card174_disinfection_info
    l.action_taken,                         -- measure_taken
    l.meat_submission,
    l.mkb_code,                             -- icd10_code
    l.mkb_name,                             -- icd10_name
    l.outbreak_location,                    -- outbreak_localization  -- ?? (location_of_event bilan to'qnashuv)
    l.owner_addr,                           -- owner_address
    l.pathogen,                             -- pathogen_type
    pm.measures,                            -- precautionary_measures     <- card174_preventive_measure
    l.quarantine_end,                       -- quarantine_end_date
    l.quarantine_start,                     -- quarantine_start_date
    l.quarantine_code,                      -- quarantine_type_code
    l.reported_vet_date,                    -- report_to_veterinary_department_date -- ??
    l.rodents_increased,                    -- rodent_increase
    l.id,                                   -- serial_doc_number      -- legacy manba yo'q; yangi kartalardagi kabi id
    l.stray_capture,                        -- stray_animal_capture
    l.synanthropic_rodents,                 -- synanthropic_rodents_increase
    l.treated_humans,
    l.treatment,
    l.vectors_increased,                    -- vector_increase
    l.wild_culling,                         -- wild_animal_culling
    l.wild_rodents,                         -- wild_rodents_increase
    left(concat_ws(' | ', NULLIF(l.additional_info, ''),
         concat_ws(' | ',
         CASE WHEN l.reported_cases IS NOT NULL THEN 'Xabar berilgan holatlar: '||l.reported_cases END,
         CASE WHEN l.reported_human_vet_date IS NOT NULL THEN 'Inson/vet. xabar sanasi: '||l.reported_human_vet_date END,
         CASE WHEN l.reported_sanepid_date IS NOT NULL THEN 'SES xabar sanasi: '||l.reported_sanepid_date END,
         CASE WHEN l.vaccination_by_epizootic IS NOT NULL THEN 'Epizootiya bo''yicha emlash: '||l.vaccination_by_epizootic END,
         CASE WHEN (to_jsonb(l)->>'measures') IS NOT NULL THEN 'Chora-tadbirlar: '||(to_jsonb(l)->>'measures') END,
         CASE WHEN (to_jsonb(l)->>'measures_count') IS NOT NULL THEN 'Chora soni: '||(to_jsonb(l)->>'measures_count') END,
         CASE WHEN (to_jsonb(l)->>'measures_date') IS NOT NULL THEN 'Chora sanasi: '||(to_jsonb(l)->>'measures_date') END,
         CASE WHEN (to_jsonb(l)->>'measures_location') IS NOT NULL THEN 'Chora joyi: '||(to_jsonb(l)->>'measures_location') END)), 1000)  -- additional_measures_info + ustuni yo'q legacy maydonlar
FROM public.card174 l
JOIN public2.card c ON c.id = l.id
LEFT JOIN LATERAL (SELECT * FROM public.card174_disinfection_info z
                    WHERE z.card174_id = l.id ORDER BY z.id LIMIT 1) di ON true
LEFT JOIN LATERAL (SELECT * FROM public.card174_preventive_measure z
                    WHERE z.card174_id = l.id ORDER BY z.id LIMIT 1) pm ON true;

-- ko'p qatorli orphan bola jadvallar -> log (faqat 1-qator ko'chirildi)
INSERT INTO public2._migration_notes (source_table, source_id, note)
SELECT t.src, t.card174_id, 'multiple child rows: only the first row was merged into card174'
FROM (
  SELECT 'card174_disinfection_info' src, card174_id FROM public.card174_disinfection_info GROUP BY card174_id HAVING count(*)>1
) t;

-- disinfection_info: card174 ga faqat 1-qator jamlandi; qolgan qatorlarning
-- QIYMATLARI log'ga yoziladi (yo'qolmasin)
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card174_disinfection_info', z.card174_id, 'extra row (after the first) - values',
       concat_ws('; ', 'id='||z.id,
         CASE WHEN z.disinfection_date IS NOT NULL THEN 'disinfection_date='||z.disinfection_date END,
         CASE WHEN z.disinfected_count IS NOT NULL THEN 'disinfected_count='||z.disinfected_count END,
         CASE WHEN z.event_location IS NOT NULL THEN 'event_location='||z.event_location END,
         CASE WHEN z.control_results IS NOT NULL THEN 'control_results='||z.control_results END)
FROM public.card174_disinfection_info z
JOIN public2.card174 c ON c.id = z.card174_id
WHERE z.id <> (SELECT min(z2.id) FROM public.card174_disinfection_info z2 WHERE z2.card174_id = z.card174_id);

-- legacy-only ustunlar (target'da yo'q): additional_measures_info (1000) ga qo'shib
-- ko'chirildi; shu bilan birga to'liq QIYMATLAR log'ga ham yoziladi
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card174', l.id, 'no column in target - appended to additional_measures_info',
       concat_ws('; ',
         CASE WHEN l.reported_cases IS NOT NULL THEN 'reported_cases='||l.reported_cases END,
         CASE WHEN l.reported_human_vet_date IS NOT NULL THEN 'reported_human_vet_date='||l.reported_human_vet_date END,
         CASE WHEN l.reported_sanepid_date IS NOT NULL THEN 'reported_sanepid_date='||l.reported_sanepid_date END,
         CASE WHEN l.vaccination_by_epizootic IS NOT NULL THEN 'vaccination_by_epizootic='||l.vaccination_by_epizootic END,
         -- baza jadvalining o'z measures/measures_count/measures_date/measures_location
         -- ustunlari (card174_preventive_measure'dan olinadigan pm.measures'dan FARQLI)
         CASE WHEN (to_jsonb(l)->>'measures') IS NOT NULL THEN 'measures='||(to_jsonb(l)->>'measures') END,
         CASE WHEN (to_jsonb(l)->>'measures_count') IS NOT NULL THEN 'measures_count='||(to_jsonb(l)->>'measures_count') END,
         CASE WHEN (to_jsonb(l)->>'measures_date') IS NOT NULL THEN 'measures_date='||(to_jsonb(l)->>'measures_date') END,
         CASE WHEN (to_jsonb(l)->>'measures_location') IS NOT NULL THEN 'measures_location='||(to_jsonb(l)->>'measures_location') END)
FROM public.card174 l
JOIN public2.card174 x ON x.id = l.id
WHERE l.reported_cases IS NOT NULL OR l.reported_human_vet_date IS NOT NULL
   OR l.reported_sanepid_date IS NOT NULL OR l.vaccination_by_epizootic IS NOT NULL
   OR (to_jsonb(l)->>'measures') IS NOT NULL OR (to_jsonb(l)->>'measures_count') IS NOT NULL
   OR (to_jsonb(l)->>'measures_date') IS NOT NULL OR (to_jsonb(l)->>'measures_location') IS NOT NULL;

-- ---- element jadvallar (1:1) --------------------------------------
INSERT INTO public2.card174_affected_animals (card174_id, catalog_code)
SELECT x.card174_id, x.catalog_code FROM public.card174_affected_animals x JOIN public2.card174 c ON c.id=x.card174_id;
INSERT INTO public2.card174_disease_factors (card174_id, catalog_code)
SELECT x.card174_id, x.catalog_code FROM public.card174_disease_factors x JOIN public2.card174 c ON c.id=x.card174_id;
INSERT INTO public2.card174_disinfection_factors (card174_id, catalog_code)
SELECT x.card174_id, x.catalog_code FROM public.card174_disinfection_factors x JOIN public2.card174 c ON c.id=x.card174_id;
INSERT INTO public2.card174_elimination_method (card174_id, catalog_code)
SELECT x.card174_id, x.catalog_code FROM public.card174_elimination_method x JOIN public2.card174 c ON c.id=x.card174_id;

-- ---- card174_infection_monitoring (rename'lar) --------------------
-- drop: vaccination_summary ; target-only sequential_number, pinfl -> NULL
INSERT INTO public2.card174_infection_monitoring (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    address, application_date, birth_date, confirmation_date, contact_infection_date, first_name, gender_code,
    last_name, middle_name, possible_infection_date, possible_infection_factor,
    possible_infection_location, profession, sequential_number, card174_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.address, d.apply_date, d.birth_date, d.confirm_date, d.contact_infection_date, d.first_name, d.gender_code,
    d.last_name, d.middle_name, d.possible_date, d.possible_factor,
    d.possible_location, d.profession, NULL, d.card174_id
FROM public.card174_infection_monitoring d
JOIN public2.card174 c ON c.id = d.card174_id;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card174_infection_monitoring', d.id, 'no column in target - dropped',
       'vaccination_summary='||d.vaccination_summary
FROM public.card174_infection_monitoring d
JOIN public2.card174 c ON c.id = d.card174_id
WHERE d.vaccination_summary IS NOT NULL;

-- infection_monitoring_vaccination (join) + vaccination: target'da jadval yo'q ->
-- har monitoring qatori uchun emlash yozuvlari matn sifatida _migration_notes ga (yo'qolmasin)
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card174_infection_monitoring', imv.infection_monitoring_id,
       'vaccination records (infection_monitoring_vaccination) - no table in target',
       string_agg(concat_ws(', ',
           v.vaccination_name,
           CASE WHEN v.vaccination_date IS NOT NULL THEN 'sana='||v.vaccination_date END,
           CASE WHEN v.serial_number IS NOT NULL THEN 'seriya='||v.serial_number END,
           CASE WHEN v.dose_volume IS NOT NULL THEN 'doza='||v.dose_volume END,
           CASE WHEN v.vaccination_verified_code IS NOT NULL THEN 'tasdiq='||v.vaccination_verified_code END),
         ' | ' ORDER BY v.id)
FROM public.infection_monitoring_vaccination imv
JOIN public.vaccination v ON v.id = imv.vaccination_id
JOIN public2.card174_infection_monitoring m ON m.id = imv.infection_monitoring_id
GROUP BY imv.infection_monitoring_id;

-- ---- card174_external_sample_test -> card174_laboratory_test ------
-- hamma qatorlar ko'chadi; specimen -> sample_name. id target identity'dan,
-- audit ustunlari ota kartadan (legacy bola jadvalining audit ustunlariga
-- tayanilmaydi).
INSERT INTO public2.card174_laboratory_test (
    version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    sample_name, test_date, sample_count, testing_method, test_result, card174_id
)
SELECT
    0, p.created_at, p.created_by_id, p.updated_at, p.updated_by_id, gen_random_uuid(),
    z.specimen, z.test_date, z.sample_count, z.test_method, z.test_result, z.card174_id
FROM public.card174_external_sample_test z
JOIN public2.card174 c ON c.id = z.card174_id
JOIN public2.card p ON p.id = z.card174_id
ORDER BY z.id;

-- ---- card174_preventive_measure -> card174_animal_measure --------
-- hamma qatorlar ko'chadi; measures -> measure_name. id/audit — yuqoridagi
-- laboratory_test bilan bir xil qoida.
INSERT INTO public2.card174_animal_measure (
    version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    measure_name, measure_date, measure_count, measure_location, card174_id
)
SELECT
    0, p.created_at, p.created_by_id, p.updated_at, p.updated_by_id, gen_random_uuid(),
    left(z.measures, 500),                  -- ?? legacy uzunligi tekshirilsin
    z.measure_date,
    z.measure_count,                        -- ?? legacy turi integer deb taxmin
    left(z.measure_location, 500),
    z.card174_id
FROM public.card174_preventive_measure z
JOIN public2.card174 c ON c.id = z.card174_id
JOIN public2.card p ON p.id = z.card174_id
ORDER BY z.id;

-- ---- card174_outbreak_control_measure (1:1 + version) ------------
INSERT INTO public2.card174_outbreak_control_measure (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    event_conducted, lost_animals, meat_delivered, processed_area, processing_method_code,
    vaccinated_animals, card174_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.event_conducted, d.lost_animals, d.meat_delivered, d.processed_area, d.processing_method_code,
    d.vaccinated_animals, d.card174_id
FROM public.card174_outbreak_control_measure d
JOIN public2.card174 c ON c.id = d.card174_id;

COMMIT;
\echo '52-card174 OK'
SELECT 'card174' t, (SELECT count(*) FROM public.card174) src, (SELECT count(*) FROM public2.card174) dst;
