-- =====================================================================
-- 52-card174.sql  —  card174 (JOINED subtype) + element + bola jadvallari
-- ⚠️ card174 BAZA jadvali: legacy va target ustun nomlari DEYARLI DISJOINT.
--    Quyidagi juftliklar TAXMIN. `-- ??` bilan belgilanganlar TEKSHIRILSIN.
--    Manba taxminlari: docs/legacy-migration/20-card.md §4a.
-- Orphan bola jadvallar (disinfection_info / external_sample_test /
--    preventive_measure) target'da yo'q -> ma'lumot card174 ota ustunlariga
--    (birinchi qator; ko'p bo'lsa log).  Qaror: 02-mapping §5.4.
-- Bog'liqlik: 50-card bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

INSERT INTO public2.card174 (
    id,
    affected_animal_count, affected_animal_type_code, affected_humans, affected_in_outbreak,
    animal_disposal_date, animal_disposal_method_code, animal_owner, animal_ownership_code,
    animal_primary_diagnosis, animal_type, blood_sucking_arthropods_increase,
    current_animal_infection_date, data_obtained_date, deratization_area, deratization_code,
    disinfected_factor_amount, disinfection_date, epizootology_existence, execution_control_results,
    human_primary_diagnosis, including_identified, including_industrial_conditions, including_who_applied,
    inspectors, investigation_date, is_area_exotic, isolation, last_disease_year, location_of_event,
    measure_taken, meat_submission, icd10_code, icd10_name, outbreak_localization, owner_address,
    pathogen_type, precautionary_measures, quarantine_end_date, quarantine_start_date, quarantine_type_code,
    report_to_veterinary_department_date, rodent_increase, serial_doc_number, stray_animal_capture,
    synanthropic_rodents_increase, test_date, test_result, test_sample_count, testing_method,
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
    NULL,                                   -- animal_type            -- ?? legacy manba yo'q
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
    l.last_case_year,                       -- last_disease_year
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
    NULL,                                   -- serial_doc_number      -- ?? legacy manba yo'q
    l.stray_capture,                        -- stray_animal_capture
    l.synanthropic_rodents,                 -- synanthropic_rodents_increase
    est.test_date,                          -- test_date              <- card174_external_sample_test
    est.test_result,                        -- test_result            <- card174_external_sample_test
    est.sample_count,                       -- test_sample_count      <- card174_external_sample_test
    est.test_method,                        -- testing_method         <- card174_external_sample_test
    l.treated_humans,
    l.treatment,
    l.vectors_increased,                    -- vector_increase
    l.wild_culling,                         -- wild_animal_culling
    l.wild_rodents,                         -- wild_rodents_increase
    l.additional_info                       -- additional_measures_info
FROM public.card174 l
JOIN public2.card c ON c.id = l.id
LEFT JOIN LATERAL (SELECT * FROM public.card174_disinfection_info z
                    WHERE z.card174_id = l.id ORDER BY z.id LIMIT 1) di ON true
LEFT JOIN LATERAL (SELECT * FROM public.card174_external_sample_test z
                    WHERE z.card174_id = l.id ORDER BY z.id LIMIT 1) est ON true
LEFT JOIN LATERAL (SELECT * FROM public.card174_preventive_measure z
                    WHERE z.card174_id = l.id ORDER BY z.id LIMIT 1) pm ON true;

-- ko'p qatorli orphan bola jadvallar -> log (faqat 1-qator ko'chirildi)
INSERT INTO public2._migration_notes (source_table, source_id, note)
SELECT t.src, t.card174_id, 'orphan bola jadval: faqat 1-qator card174 ga jamlandi'
FROM (
  SELECT 'card174_disinfection_info' src, card174_id FROM public.card174_disinfection_info GROUP BY card174_id HAVING count(*)>1
  UNION ALL
  SELECT 'card174_external_sample_test', card174_id FROM public.card174_external_sample_test GROUP BY card174_id HAVING count(*)>1
  UNION ALL
  SELECT 'card174_preventive_measure', card174_id FROM public.card174_preventive_measure GROUP BY card174_id HAVING count(*)>1
) t;

-- legacy-only ustunlar (target'da yo'q, drop) — ma'lumot bo'lsa log
INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card174', l.id, 'target''da ustun yo''q — tashlandi',
       concat_ws('; ',
         CASE WHEN l.reported_cases IS NOT NULL THEN 'reported_cases='||l.reported_cases END,
         CASE WHEN l.reported_human_vet_date IS NOT NULL THEN 'reported_human_vet_date' END,
         CASE WHEN l.reported_sanepid_date IS NOT NULL THEN 'reported_sanepid_date' END,
         CASE WHEN l.vaccination_by_epizootic IS NOT NULL THEN 'vaccination_by_epizootic' END)
FROM public.card174 l
JOIN public2.card174 x ON x.id = l.id
WHERE l.reported_cases IS NOT NULL OR l.reported_human_vet_date IS NOT NULL
   OR l.reported_sanepid_date IS NOT NULL OR l.vaccination_by_epizootic IS NOT NULL;

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
-- drop: contact_infection_date, vaccination_summary ; target-only sequential_number -> NULL
INSERT INTO public2.card174_infection_monitoring (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    address, application_date, birth_date, confirmation_date, first_name, gender_code,
    last_name, middle_name, possible_infection_date, possible_infection_factor,
    possible_infection_location, profession, sequential_number, card174_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.address, d.apply_date, d.birth_date, d.confirm_date, d.first_name, d.gender_code,
    d.last_name, d.middle_name, d.possible_date, d.possible_factor,
    d.possible_location, d.profession, NULL, d.card174_id
FROM public.card174_infection_monitoring d
JOIN public2.card174 c ON c.id = d.card174_id;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card174_infection_monitoring', d.id, 'target''da ustun yo''q — tashlandi',
       concat_ws('; ',
         CASE WHEN d.contact_infection_date IS NOT NULL THEN 'contact_infection_date' END,
         CASE WHEN d.vaccination_summary IS NOT NULL THEN 'vaccination_summary' END)
FROM public.card174_infection_monitoring d
JOIN public2.card174 c ON c.id = d.card174_id
WHERE d.contact_infection_date IS NOT NULL OR d.vaccination_summary IS NOT NULL;

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
