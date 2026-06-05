--liquibase formatted sql

--changeset uzinfocom:20260605-1100-create-ref-catalog
CREATE TABLE IF NOT EXISTS ref_catalog
(
    id            BIGSERIAL PRIMARY KEY,
    type          VARCHAR(100)                NOT NULL,
    code          VARCHAR(50)                 NOT NULL,
    parent_code   VARCHAR(50),
    name_uz       VARCHAR(255),
    name_uz_cyril VARCHAR(255),
    name_ru       VARCHAR(255),
    name_kaa      VARCHAR(255),
    deleted       BOOLEAN                     NOT NULL DEFAULT false,
    sort_order    INTEGER                     NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    version       BIGINT                      NOT NULL DEFAULT 0,
    CONSTRAINT uk_ref_catalog_type_code UNIQUE (type, code)
);

CREATE INDEX IF NOT EXISTS idx_ref_catalog_type_deleted
    ON ref_catalog (type, deleted);

CREATE INDEX IF NOT EXISTS idx_ref_catalog_type_parent_code
    ON ref_catalog (type, parent_code);

--changeset uzinfocom:20260605-1101-seed-reference-catalog-permission-subjects
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT CASE WHEN to_regclass('public.permission') IS NULL THEN 0 ELSE 1 END
INSERT INTO permission (created_at,
                        updated_at,
                        subject,
                        active,
                        description_uz,
                        description_ru,
                        description_uz_cyril,
                        description_kaa,
                        deleted)
VALUES (now(), now(), 'REFERENCE_CATALOG_READ', true, 'Kataloglarni ko''rish', 'Read catalogs', 'Kataloglarni ko''rish',
        'Read catalogs'),
       (now(), now(), 'REFERENCE_CATALOG_MANAGE', true, 'Kataloglarni boshqarish', 'Manage catalogs',
        'Kataloglarni boshqarish', 'Manage catalogs')
ON CONFLICT (subject) DO UPDATE SET active               = EXCLUDED.active,
                                    description_uz       = EXCLUDED.description_uz,
                                    description_ru       = EXCLUDED.description_ru,
                                    description_uz_cyril = EXCLUDED.description_uz_cyril,
                                    description_kaa      = EXCLUDED.description_kaa,
                                    updated_at           = now(),
                                    deleted              = FALSE;
