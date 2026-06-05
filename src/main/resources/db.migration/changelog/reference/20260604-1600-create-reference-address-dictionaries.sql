--liquibase formatted sql

--changeset uzinfocom:20260605-1700-create-reference-address-dictionaries-if-missing

CREATE TABLE IF NOT EXISTS ref_country
(
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(50)                 NOT NULL,
    name_uz       VARCHAR(255),
    name_uz_cyril VARCHAR(255),
    name_ru       VARCHAR(255),
    name_kaa      VARCHAR(255),
    deleted       BOOLEAN                     NOT NULL DEFAULT false,
    sort_order    INTEGER                     NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    version       BIGINT                      NOT NULL DEFAULT 0,

    CONSTRAINT uk_ref_country_code UNIQUE (code),
    CONSTRAINT chk_ref_country_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT chk_ref_country_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE IF NOT EXISTS ref_region
(
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(50)                 NOT NULL,
    parent_code      VARCHAR(50)                 NOT NULL,
    soato_id         INTEGER                     NOT NULL,
    legacy_soato_id  INTEGER                     NOT NULL,
    name_uz          VARCHAR(255),
    name_uz_cyril    VARCHAR(255),
    name_ru          VARCHAR(255),
    name_kaa         VARCHAR(255),
    deleted          BOOLEAN                     NOT NULL DEFAULT false,
    sort_order       INTEGER                     NOT NULL DEFAULT 0,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    version          BIGINT                      NOT NULL DEFAULT 0,

    CONSTRAINT uk_ref_region_code UNIQUE (code),
    CONSTRAINT uk_ref_region_soato_id UNIQUE (soato_id),
    CONSTRAINT uk_ref_region_legacy_soato_id UNIQUE (legacy_soato_id),
    CONSTRAINT chk_ref_region_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT chk_ref_region_parent_code_not_blank CHECK (btrim(parent_code) <> ''),
    CONSTRAINT chk_ref_region_soato_id_positive CHECK (soato_id > 0),
    CONSTRAINT chk_ref_region_legacy_soato_id_positive CHECK (legacy_soato_id > 0),
    CONSTRAINT chk_ref_region_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE IF NOT EXISTS ref_district
(
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(50)                 NOT NULL,
    parent_code   VARCHAR(50)                 NOT NULL,
    name_uz       VARCHAR(255),
    name_uz_cyril VARCHAR(255),
    name_ru       VARCHAR(255),
    name_kaa      VARCHAR(255),
    deleted       BOOLEAN                     NOT NULL DEFAULT false,
    sort_order    INTEGER                     NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    version       BIGINT                      NOT NULL DEFAULT 0,

    CONSTRAINT uk_ref_district_code UNIQUE (code),
    CONSTRAINT chk_ref_district_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT chk_ref_district_parent_code_not_blank CHECK (btrim(parent_code) <> ''),
    CONSTRAINT chk_ref_district_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE IF NOT EXISTS ref_neighborhood
(
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(50)                 NOT NULL,
    parent_code   VARCHAR(50)                 NOT NULL,
    name_uz       VARCHAR(255),
    name_uz_cyril VARCHAR(255),
    name_ru       VARCHAR(255),
    name_kaa      VARCHAR(255),
    deleted       BOOLEAN                     NOT NULL DEFAULT false,
    sort_order    INTEGER                     NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    version       BIGINT                      NOT NULL DEFAULT 0,

    CONSTRAINT uk_ref_neighborhood_code UNIQUE (code),
    CONSTRAINT chk_ref_neighborhood_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT chk_ref_neighborhood_parent_code_not_blank CHECK (btrim(parent_code) <> ''),
    CONSTRAINT chk_ref_neighborhood_sort_order CHECK (sort_order >= 0)
);

CREATE INDEX IF NOT EXISTS idx_ref_country_deleted_sort
    ON ref_country (deleted, sort_order, code);

CREATE INDEX IF NOT EXISTS idx_ref_region_parent_deleted_sort
    ON ref_region (parent_code, deleted, sort_order, code);

CREATE INDEX IF NOT EXISTS idx_ref_district_parent_deleted_sort
    ON ref_district (parent_code, deleted, sort_order, code);

CREATE INDEX IF NOT EXISTS idx_ref_neighborhood_parent_deleted_sort
    ON ref_neighborhood (parent_code, deleted, sort_order, code);


--changeset uzinfocom:20260605-1710-seed-uzbekistan-country-if-empty
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM ref_country

INSERT INTO ref_country (
    code,
    name_uz,
    name_uz_cyril,
    name_ru,
    name_kaa,
    deleted,
    sort_order
)
VALUES (
           'UZ',
           'O''zbekiston Respublikasi',
           'Ўзбекистон Республикаси',
           'Республика Узбекистан',
           'Ózbekstan Respublikası',
           false,
           10
       )
ON CONFLICT ON CONSTRAINT uk_ref_country_code
    DO NOTHING;


--changeset uzinfocom:20260605-1720-seed-regions-if-empty
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM ref_region

INSERT INTO ref_region (
    code,
    parent_code,
    soato_id,
    legacy_soato_id,
    name_uz,
    name_uz_cyril,
    name_ru,
    name_kaa,
    deleted,
    sort_order
)
VALUES
    ('UZ-AN', 'UZ', 1703, 17,
     'Andijon viloyati', 'Андижон вилояти', 'Андижанская область', 'Andijan wálayatı',
     false, 10),

    ('UZ-BU', 'UZ', 1706, 20,
     'Buxoro viloyati', 'Бухоро вилояти', 'Бухарская область', 'Buxara wálayatı',
     false, 20),

    ('UZ-FA', 'UZ', 1730, 15,
     'Farg''ona viloyati', 'Фарғона вилояти', 'Ферганская область', 'Ferǵana wálayatı',
     false, 30),

    ('UZ-JI', 'UZ', 1708, 13,
     'Jizzax viloyati', 'Жиззах вилояти', 'Джизакская область', 'Jizzax wálayatı',
     false, 40),

    ('UZ-NG', 'UZ', 1714, 16,
     'Namangan viloyati', 'Наманган вилояти', 'Наманганская область', 'Namangan wálayatı',
     false, 50),

    ('UZ-NW', 'UZ', 1712, 21,
     'Navoiy viloyati', 'Навоий вилояти', 'Навоийская область', 'Nawayı wálayatı',
     false, 60),

    ('UZ-QA', 'UZ', 1710, 18,
     'Qashqadaryo viloyati', 'Қашқадарё вилояти', 'Кашкадарьинская область', 'Qashqadárya wálayatı',
     false, 70),

    ('UZ-QR', 'UZ', 1735, 23,
     'Qoraqalpog''iston Respublikasi', 'Қорақалпоғистон Республикаси',
     'Республика Каракалпакстан', 'Qaraqalpaqstan Respublikası',
     false, 80),

    ('UZ-SA', 'UZ', 1718, 14,
     'Samarqand viloyati', 'Самарқанд вилояти', 'Самаркандская область', 'Samarqand wálayatı',
     false, 90),

    ('UZ-SI', 'UZ', 1724, 12,
     'Sirdaryo viloyati', 'Сирдарё вилояти', 'Сырдарьинская область', 'Sırdárya wálayatı',
     false, 100),

    ('UZ-SU', 'UZ', 1722, 19,
     'Surxondaryo viloyati', 'Сурхондарё вилояти', 'Сурхандарьинская область', 'Surxandárya wálayatı',
     false, 110),

    ('UZ-TK', 'UZ', 1726, 10,
     'Toshkent shahri', 'Тошкент шаҳри', 'Город Ташкент', 'Tashkent qalası',
     false, 120),

    ('UZ-TO', 'UZ', 1727, 11,
     'Toshkent viloyati', 'Тошкент вилояти', 'Ташкентская область', 'Tashkent wálayatı',
     false, 130),

    ('UZ-XO', 'UZ', 1733, 22,
     'Xorazm viloyati', 'Хоразм вилояти', 'Хорезмская область', 'Xorezm wálayatı',
     false, 140)
ON CONFLICT DO NOTHING;
