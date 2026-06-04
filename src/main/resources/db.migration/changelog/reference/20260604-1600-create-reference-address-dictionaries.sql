--liquibase formatted sql

--changeset uzinfocom:20260604-1600-create-reference-address-dictionaries
CREATE TABLE IF NOT EXISTS ref_country (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name_uz VARCHAR(255),
    name_uz_cyril VARCHAR(255),
    name_ru VARCHAR(255),
    name_kaa VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ref_country_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS ref_region (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    parent_code VARCHAR(50) NOT NULL,
    name_uz VARCHAR(255),
    name_uz_cyril VARCHAR(255),
    name_ru VARCHAR(255),
    name_kaa VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ref_region_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS ref_district (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    parent_code VARCHAR(50) NOT NULL,
    name_uz VARCHAR(255),
    name_uz_cyril VARCHAR(255),
    name_ru VARCHAR(255),
    name_kaa VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ref_district_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS ref_mahalla (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    parent_code VARCHAR(50) NOT NULL,
    name_uz VARCHAR(255),
    name_uz_cyril VARCHAR(255),
    name_ru VARCHAR(255),
    name_kaa VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ref_mahalla_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_ref_country_code ON ref_country (code);
CREATE INDEX IF NOT EXISTS idx_ref_country_deleted ON ref_country (deleted);

CREATE INDEX IF NOT EXISTS idx_ref_region_code ON ref_region (code);
CREATE INDEX IF NOT EXISTS idx_ref_region_parent_code ON ref_region (parent_code);
CREATE INDEX IF NOT EXISTS idx_ref_region_deleted ON ref_region (deleted);

CREATE INDEX IF NOT EXISTS idx_ref_district_code ON ref_district (code);
CREATE INDEX IF NOT EXISTS idx_ref_district_parent_code ON ref_district (parent_code);
CREATE INDEX IF NOT EXISTS idx_ref_district_deleted ON ref_district (deleted);

CREATE INDEX IF NOT EXISTS idx_ref_mahalla_code ON ref_mahalla (code);
CREATE INDEX IF NOT EXISTS idx_ref_mahalla_parent_code ON ref_mahalla (parent_code);
CREATE INDEX IF NOT EXISTS idx_ref_mahalla_deleted ON ref_mahalla (deleted);

--changeset uzinfocom:20260604-1601-seed-reference-permission-subjects
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT CASE WHEN to_regclass('public.permission') IS NULL THEN 0 ELSE 1 END
INSERT INTO permission (
    uuid,
    created_at,
    updated_at,
    subject,
    active,
    description_uz,
    description_ru,
    description_uz_cyril,
    description_kaa
) VALUES
    ('d5afd2d2-89f8-44b0-a48d-b1a4a6718ce0'::uuid, now(), now(), 'REFERENCE_COUNTRY_READ', true, 'Davlatlarni ko''rish', 'Read countries', 'Davlatlarni ko''rish', 'Read countries'),
    ('6d3f1e0f-9b36-4c8f-bd6f-d88fffe4649d'::uuid, now(), now(), 'REFERENCE_COUNTRY_MANAGE', true, 'Davlatlarni boshqarish', 'Manage countries', 'Davlatlarni boshqarish', 'Manage countries'),
    ('73cd37a1-dd35-4c95-a1f1-f6a0332c2d55'::uuid, now(), now(), 'REFERENCE_REGION_READ', true, 'Viloyatlarni ko''rish', 'Read regions', 'Viloyatlarni ko''rish', 'Read regions'),
    ('27f3cc06-130d-46c1-b54f-e68573c87d3c'::uuid, now(), now(), 'REFERENCE_REGION_MANAGE', true, 'Viloyatlarni boshqarish', 'Manage regions', 'Viloyatlarni boshqarish', 'Manage regions'),
    ('9007d604-414f-4e80-9d5d-63efb5a31ff8'::uuid, now(), now(), 'REFERENCE_DISTRICT_READ', true, 'Tumanlarni ko''rish', 'Read districts', 'Tumanlarni ko''rish', 'Read districts'),
    ('c15e7607-ac98-44a1-b3ce-952cb4db872d'::uuid, now(), now(), 'REFERENCE_DISTRICT_MANAGE', true, 'Tumanlarni boshqarish', 'Manage districts', 'Tumanlarni boshqarish', 'Manage districts'),
    ('727dbff3-1d89-4ba6-b996-2fcfc97fb655'::uuid, now(), now(), 'REFERENCE_MAHALLA_READ', true, 'Mahallalarni ko''rish', 'Read mahallas', 'Mahallalarni ko''rish', 'Read mahallas'),
    ('bfcf2cff-5d65-4d13-9fcd-ea3a75a32d41'::uuid, now(), now(), 'REFERENCE_MAHALLA_MANAGE', true, 'Mahallalarni boshqarish', 'Manage mahallas', 'Mahallalarni boshqarish', 'Manage mahallas')
ON CONFLICT (subject) DO UPDATE SET
    active = EXCLUDED.active,
    description_uz = EXCLUDED.description_uz,
    description_ru = EXCLUDED.description_ru,
    description_uz_cyril = EXCLUDED.description_uz_cyril,
    description_kaa = EXCLUDED.description_kaa,
    updated_at = now();
