-- =====================================================================
-- 00-prep.sql  —  ko'chirishga tayyorgarlik
--   * note-log jadvali (hech narsa "skip" qilinmaydi — faqat qayd)
--   * timezone
--   * barcha target biznes jadvallarni tozalash (qayta ishga tushirish uchun)
-- Manba: public (5434/isemid) -> nishon: public2 (bir xil baza)
--
-- TAMOYIL: HECH BIR QATOR TASHLAB KETILMAYDI. Majburiy maydon bo'sh bo'lsa
-- sentinel qiymat qo'yiladi (0 / '—' / created_at) va note-log ga yoziladi.
-- Yagona istisno: act155/act155_detail DETALI (yangi sxemada bunday jadval yo'q;
-- act BAZAVIY qatori esa ko'chiriladi).
-- =====================================================================
\set ON_ERROR_STOP on
BEGIN;

SET TIME ZONE 'Asia/Tashkent';

-- ---------------------------------------------------------------------
-- Qayd jadvali: sentinel ishlatilgan / detal ko'chmagan qatorlar
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public2._migration_notes (
    logged_at    timestamptz NOT NULL DEFAULT now(),
    source_table text        NOT NULL,
    source_id    bigint,
    note         text        NOT NULL,
    details      text
);
TRUNCATE public2._migration_notes;

-- ---------------------------------------------------------------------
-- Target biznes jadvallarini tozalash (CASCADE). Reference/RBAC/dev/infra
-- jadvallariga TEGILMAYDI (Liquibase seed'i).
-- ---------------------------------------------------------------------
TRUNCATE
    public2.act_users,
    public2.act224_detail, public2.act223_detail, public2.act156_kitchen_utensil,
    public2.act156_group_detail, public2.act154_detail, public2.act153_detail,
    public2.act224, public2.act223, public2.act156, public2.act154, public2.act153,
    public2.act,
    public2.card_users,
    public2.card161_contact_person, public2.card161_emergency_prophylaxis,
    public2.card161_environmental_lab_test, public2.card161_environmental_source,
    public2.card161_indirection_causing, public2.card161_infection_source,
    public2.card161_infection_source_detail, public2.card161_injury_location,
    public2.card161_outbreak_measure, public2.card161_prevent_measure,
    public2.card161_risk_factors, public2.card161_screened_group,
    public2.card161_vaccination,
    public2.card174_affected_animals, public2.card174_disease_factors,
    public2.card174_disinfection_factors, public2.card174_elimination_method,
    public2.card174_infection_monitoring, public2.card174_outbreak_control_measure,
    public2.card175_disease_transmission_condition, public2.card175_part_of_injury,
    public2.card175_pathogen_main_factor, public2.card175_taken_measures_from_residence,
    public2.card205_info_about_animal_bitten_people, public2.card205_info_bitten_animals,
    public2.card205_info_bitten_people,
    public2.card_tube_checkup_dates, public2.card_tube_contact_monitoring,
    public2.card_tube_infection_source, public2.card_tube_nutrition_type,
    public2.card_tube_tb_history, public2.card_tube_xray,
    public2.card161, public2.card174, public2.card175, public2.card205, public2.card_tube,
    public2.card,
    public2.form058_1_other_injured_person, public2.form058_1,
    public2.form058,
    public2.fm058_location,
    public2.pt_identifier, public2.pt_affiliation, public2.pt_address,
    public2.patient,
    public2.users_organizations,
    public2.organization_service_types,
    public2.users,
    public2.organization
    RESTART IDENTITY CASCADE;

-- ---------------------------------------------------------------------
-- SENTINEL tashkilot — card/act.created_org_uuid (NOT NULL + FK -> organization.uuid)
-- bo'sh bo'lган qatorlar shунга ishora qiladi.
-- id = 0, uuid = butun-nol.  Legacy id lar 1 dan boshlanadi -> to'qnashuv yo'q.
-- ---------------------------------------------------------------------
INSERT INTO public2.organization (
    id, version, created_at, updated_at, active, level_type, medical_type,
    name, name_uz, uuid
) VALUES (
    0, 0, now(), now(), false, 'NOT_DEFINED', 'OTHER',
    'MIGRATSIYA: NOMA''LUM TASHKILOT', 'MIGRATSIYA: NOMA''LUM TASHKILOT',
    '00000000-0000-0000-0000-000000000000'
);

COMMIT;

\echo '00-prep OK — note-log tayyor, target tozalandi, sentinel organization(id=0) yaratildi'
