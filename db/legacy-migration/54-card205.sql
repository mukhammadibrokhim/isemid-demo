-- =====================================================================
-- 54-card205.sql  —  card205 (JOINED subtype) + bola jadvallar
-- ⚠️ BAZA jadvali: nomlar DEYARLI DISJOINT. Juftliklar TAXMIN
--    (docs/legacy-migration/20-card.md §6a). `-- ??` = TEKSHIRILSIN.
-- Bola jadvallar nom o'zgargan (§6b) — foydalanuvchi tasdiqladi:
--    animal_bite_victim -> info_about_animal_bitten_people
--    other_bitten_animal -> info_bitten_animals
--    other_bitten_people -> info_bitten_people
-- Bog'liqlik: 50-card bajarilgan.
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;
SET TIME ZONE 'Asia/Tashkent';

INSERT INTO public2.card205 (
    id, age_of_animal, when_animal_appeared, breed_of_animal, colour_of_animal,
    condition_of_animal_code, animal_conservation_code, where_animal_comes_from,
    full_name_of_animal_owner, individual_signs_of_animal, address_of_bite_occurrence,
    date_of_bite_occurrence, epidemiological_observation_date, additional_information,
    date_of_treatment_preventive_institution, name_of_treatment_preventive_institution,
    icd10_code, icd10_name, dog_owner_compliance_code, date_time_of_feather_taken,
    veterinary_emergency_information_date, issue_date_of_first_certificate,
    issue_date_of_secondary_certificate, certificate_number_of_first_vet_results,
    certificate_number_of_second_vet_results, pet_registered_vet_department,
    position_of_bitten_victim_code, animal_type
)
SELECT
    l.id,
    l.animal_age,               -- age_of_animal
    l.animal_arrival_date,      -- when_animal_appeared
    l.animal_breed,             -- breed_of_animal
    l.animal_color,             -- colour_of_animal
    l.animal_condition_code,    -- condition_of_animal_code
    l.animal_hold_code,         -- animal_conservation_code   -- ??
    l.animal_origin,            -- where_animal_comes_from
    l.animal_owner_name,        -- full_name_of_animal_owner
    l.animal_signs,             -- individual_signs_of_animal
    l.bite_address,             -- address_of_bite_occurrence
    l.bite_date,                -- date_of_bite_occurrence
    l.epi_survey_date,          -- epidemiological_observation_date
    l.extra_info,               -- additional_information
    l.first_clinic_date,        -- date_of_treatment_preventive_institution  -- ??
    l.first_clinic_name,        -- name_of_treatment_preventive_institution
    l.mkb_code,                 -- icd10_code
    l.mkb_name,                 -- icd10_name
    l.owner_compliance_code,    -- dog_owner_compliance_code
    l.post_mortem_date_time::text,  -- date_time_of_feather_taken (varchar)  -- ??
    l.vet_alert_date,           -- veterinary_emergency_information_date
    l.vet_cert_date1,           -- issue_date_of_first_certificate
    l.vet_cert_date2,           -- issue_date_of_secondary_certificate
    l.vet_cert_num1,            -- certificate_number_of_first_vet_results
    l.vet_cert_num2,            -- certificate_number_of_second_vet_results
    l.vet_status_info,          -- pet_registered_vet_department   -- ??
    l.victim_position_code,     -- position_of_bitten_victim_code
    NULL                        -- animal_type   -- ?? legacy manba yo'q
FROM public.card205 l
JOIN public2.card c ON c.id = l.id;

INSERT INTO public2._migration_notes (source_table, source_id, note, details)
SELECT 'card205', l.id, 'target''da ustun yo''q — tashlandi', 'vet_certificate_info'
FROM public.card205 l
JOIN public2.card205 x ON x.id = l.id
WHERE l.vet_certificate_info IS NOT NULL;

-- ---- animal_bite_victim -> info_about_animal_bitten_people ---------
INSERT INTO public2.card205_info_about_animal_bitten_people (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    address_of_animal_bitten_owner, animal_category_code, animal_type, apartment_number,
    district, full_name_of_animal_bitten_owner, house_number, neighborhood, region, street, card205_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.owner_address, d.animal_category_code, d.animal_type, d.apartment_number,
    d.city_code, d.owner_full_name, d.house_number, d.neighborhood_code, d.state_code, d.street, d.card205_id
FROM public.card205_animal_bite_victim d
JOIN public2.card205 c ON c.id = d.card205_id;

-- ---- other_bitten_animal -> info_bitten_animals -------------------
INSERT INTO public2.card205_info_bitten_animals (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    bitten_animal_category_code, bitten_date_time, where_animal_bitten, card205_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.animal_category_code, d.bite_date_time, d.bite_location, d.card205_id
FROM public.card205_other_bitten_animal d
JOIN public2.card205 c ON c.id = d.card205_id;

-- ---- other_bitten_people -> info_bitten_people -------------------
INSERT INTO public2.card205_info_bitten_people (
    id, version, created_at, created_by_id, updated_at, updated_by_id, uuid,
    apartment_number, birth_date, bitten_date, district, first_name, gender, house_number,
    last_name, living_address, middle_name, neighborhood, region, street, card205_id
)
SELECT
    d.id, 0,
    d.created_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.created_by_id,
    d.updated_at::timestamp AT TIME ZONE 'Asia/Tashkent', d.updated_by_id, d.uuid,
    d.apartment_number, d.birth_date, d.bitten_date_time, d.city_code, d.first_name, d.gender, d.house_number,
    d.last_name, d.living_address, d.middle_name, d.neighborhood_code, d.state_code, d.street, d.card205_id
FROM public.card205_other_bitten_people d
JOIN public2.card205 c ON c.id = d.card205_id;

COMMIT;
\echo '54-card205 OK'
SELECT 'card205' t, (SELECT count(*) FROM public.card205) src, (SELECT count(*) FROM public2.card205) dst;
