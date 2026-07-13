package uz.uzinfocom.isemid.features.report.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.report.dtos.request.MonitoringFilter;
import uz.uzinfocom.isemid.features.report.projection.MonitoringDiseaseRow;
import uz.uzinfocom.isemid.features.report.projection.MonitoringLocationRow;
import uz.uzinfocom.isemid.features.report.repository.MonitoringQueryRepository;

import java.sql.Types;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class JdbcMonitoringQueryRepository
        implements MonitoringQueryRepository {

    private static final int MAX_MKB_HIERARCHY_DEPTH = 32;

    private static final String STATE_CATALOG_TYPE = "state";
    private static final String CITY_CATALOG_TYPE = "city";

    private static final String STATE_CODE_SQL_EXPRESSION =
            "organization.state_code";

    private static final String CITY_CODE_SQL_EXPRESSION =
            "organization.city_code";

    /**
     * When a region filter is set, cities and districts belonging to this region are selected.
     */
    private static final String BASE_CITY_FILTER_SQL = """
              AND catalog.parent_code = :regionCode
            """;

    /**
     * The organization's state_code will be checked when the region filter is set.
     */
    private static final String ORGANIZATION_REGION_FILTER_SQL = """
              AND organization.state_code = :regionCode
            """;

    /**
     * final_mkb10code value:
     * - trimming;
     * - outer brackets are removed;
     * - uppercase;
     * - compared with the normalized parameter.
     */


    private static final String MKB_FILTER_SQL = """
              AND UPPER(
                    TRIM(
                        BOTH '()'
                        FROM TRIM(form058.final_mkb10code)
                    )
                  ) = :mkb10Code
            """;

    /**
     * Age on the date Form058 was created via patient.birth_date
     * is calculated as the year of full completion.
     * <p>
     * Examples:
     * 2 years 11 months 29 days -> 2 years
     * 3 years 11 months 29 days -> 3 years
     * 4 years old -> 4 years old
     * <p>
     * Each Form058 falls into one of the following categories:
     * <p>
     * Gender:
     * - MALE
     * - FEMALE
     * - UNKNOWN
     * <p>
     * Age:
     * - 0-3
     * - 4-6.
     * - 7-10.
     * 11–17.
     * - 18-25
     * 26-45.
     * 46-65.
     * - 66+
     * - UNKNOWN
     */
    private static final String LOCATION_STATISTICS_SQL = """
            WITH base_regions AS (
                SELECT DISTINCT
                    catalog.code AS region_code

                FROM cls_catalog catalog

                WHERE catalog.type = :catalogType
                  AND catalog.code IS NOT NULL

                %s
            ),

            filtered_forms AS (
                SELECT
                    form058.id AS form_id,

                    %s AS region_code,

                    form058.status AS form_status,

                    CASE
                        WHEN UPPER(TRIM(patient.gender_code)) = 'MALE'
                            THEN 'MALE'

                        WHEN UPPER(TRIM(patient.gender_code)) = 'FEMALE'
                            THEN 'FEMALE'

                        ELSE 'UNKNOWN'
                    END AS normalized_gender,

                    CASE
                        WHEN patient.birth_date IS NULL
                            THEN NULL

                        WHEN form058.created_at IS NULL
                            THEN NULL

                        WHEN patient.birth_date >
                             form058.created_at::DATE
                            THEN NULL

                        ELSE EXTRACT(
                            YEAR FROM AGE(
                                form058.created_at::DATE,
                                patient.birth_date
                            )
                        )::INTEGER
                    END AS age_years

                FROM form058 form058

                INNER JOIN organization organization
                        ON organization.uuid =
                           form058.created_org_uuid

                LEFT JOIN patient patient
                       ON patient.id =
                          form058.patient_id

                WHERE form058.created_at >= :fromDateTime
                  AND form058.created_at < :toDateTimeExclusive

                %s
                %s
            )

            SELECT
                base_region.region_code,

                COUNT(form_data.form_id)
                    AS total_cases,

                /*
                 * APPROVED bo‘lgan formalar.
                 */
                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.form_status = :approvedStatus
                ) AS confirmed,

                /*
                 * APPROVED bo‘lmagan barcha statuslar.
                 *
                 * IS DISTINCT FROM ishlatilgani sababli
                 * status NULL bo‘lsa ham unconfirmed hisoblanadi.
                 */
                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.form_status
                          IS DISTINCT FROM :approvedStatus
                ) AS unconfirmed,

                /*
                 * Gender guruhlari.
                 */
                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.normalized_gender = 'MALE'
                ) AS male,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.normalized_gender = 'FEMALE'
                ) AS female,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.normalized_gender = 'UNKNOWN'
                ) AS unknown_gender,

                /*
                 * Yosh guruhlari.
                 *
                 * Yarim ochiq interval ishlatildi:
                 * >= boshlanish va < keyingi chegara.
                 *
                 * Bu guruhlar orasida overlap va bo‘shliq
                 * bo‘lmasligini aniq ta’minlaydi.
                 */
                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 0
                      AND form_data.age_years < 4
                ) AS age_0_3,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 4
                      AND form_data.age_years < 7
                ) AS age_4_6,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 7
                      AND form_data.age_years < 11
                ) AS age_7_10,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 11
                      AND form_data.age_years < 18
                ) AS age_11_17,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 18
                      AND form_data.age_years < 26
                ) AS age_18_25,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 26
                      AND form_data.age_years < 46
                ) AS age_26_45,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 46
                      AND form_data.age_years < 66
                ) AS age_46_65,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 66
                ) AS age_66_plus,

                /*
                 * Patient topilmagan, birth_date null yoki
                 * birth_date noto‘g‘ri bo‘lgan formalar.
                 *
                 * form_id IS NOT NULL sharti base_regions
                 * LEFT JOIN qilgan bo‘sh qatorni sanamaslik uchun kerak.
                 */
                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.form_id IS NOT NULL
                      AND form_data.age_years IS NULL
                ) AS unknown_age

            FROM base_regions base_region

            LEFT JOIN filtered_forms form_data
                   ON form_data.region_code =
                      base_region.region_code

            GROUP BY base_region.region_code

            ORDER BY base_region.region_code
            """;

    /**
     * Each Form058 disease is counted in the statistics exactly once.
     * <p>
     * final_mkb10code:
     * - null;
     * - empty;
     * - STRING;
     * - not found in cls_mkb10;
     * - no matching block found within the hierarchy
     * <p>
     * will fall to UNCLASSIFIED.
     */
    private static final String DISEASE_STATISTICS_SQL = """
            WITH RECURSIVE normalized_forms AS (
                SELECT
                    %s AS region_code,

                    CASE
                        WHEN form058.final_mkb10code IS NULL
                            THEN NULL

                        WHEN UPPER(
                                TRIM(
                                    BOTH '()'
                                    FROM TRIM(form058.final_mkb10code)
                                )
                             ) IN ('', 'STRING')
                            THEN NULL

                        ELSE UPPER(
                            TRIM(
                                BOTH '()'
                                FROM TRIM(form058.final_mkb10code)
                            )
                        )
                    END AS source_code

                FROM form058 form058

                INNER JOIN organization organization
                        ON organization.uuid =
                           form058.created_org_uuid

                WHERE form058.created_at >= :fromDateTime
                  AND form058.created_at < :toDateTimeExclusive

                %s
                %s
            ),

            filtered_codes AS (
                SELECT
                    normalized_form.region_code,
                    normalized_form.source_code,
                    COUNT(*) AS cases_count

                FROM normalized_forms normalized_form

                GROUP BY
                    normalized_form.region_code,
                    normalized_form.source_code
            ),

            mkb_hierarchy AS (
                SELECT
                    filtered_code.region_code,
                    filtered_code.source_code,
                    filtered_code.cases_count,

                    mkb.id,
                    mkb.parent_id,

                    UPPER(
                        TRIM(
                            BOTH '()'
                            FROM TRIM(mkb.code)
                        )
                    ) AS current_code,

                    0 AS hierarchy_depth,

                    ARRAY[mkb.id]::BIGINT[]
                        AS visited_ids

                FROM filtered_codes filtered_code

                INNER JOIN cls_mkb10 mkb
                        ON UPPER(
                               TRIM(
                                   BOTH '()'
                                   FROM TRIM(mkb.code)
                               )
                           ) = filtered_code.source_code

                WHERE filtered_code.source_code IS NOT NULL

                UNION ALL

                SELECT
                    hierarchy.region_code,
                    hierarchy.source_code,
                    hierarchy.cases_count,

                    parent.id,
                    parent.parent_id,

                    UPPER(
                        TRIM(
                            BOTH '()'
                            FROM TRIM(parent.code)
                        )
                    ) AS current_code,

                    hierarchy.hierarchy_depth + 1,

                    hierarchy.visited_ids || parent.id

                FROM mkb_hierarchy hierarchy

                INNER JOIN cls_mkb10 parent
                        ON parent.id =
                           hierarchy.parent_id

                WHERE hierarchy.hierarchy_depth <
                      :maxHierarchyDepth

                  AND NOT parent.id =
                          ANY(hierarchy.visited_ids)
            ),

            ranked_candidates AS (
                SELECT
                    hierarchy.region_code,
                    hierarchy.source_code,
                    hierarchy.current_code,
                    hierarchy.hierarchy_depth,

                    ROW_NUMBER() OVER (
                        PARTITION BY
                            hierarchy.region_code,
                            hierarchy.source_code

                        ORDER BY
                            /*
                             * Birinchi navbatda A00-A09 kabi
                             * block formatidagi code tanlanadi.
                             */
                            CASE
                                WHEN hierarchy.current_code ~
                                     '^[A-Z][0-9]{2}-[A-Z][0-9]{2}$'
                                    THEN 0
                                ELSE 1
                            END,

                            /*
                             * Eng yaqin block ancestor tanlanadi.
                             */
                            CASE
                                WHEN hierarchy.current_code ~
                                     '^[A-Z][0-9]{2}-[A-Z][0-9]{2}$'
                                    THEN hierarchy.hierarchy_depth
                                ELSE NULL
                            END ASC NULLS LAST,

                            /*
                             * Block topilmasa hierarchy ichidagi
                             * eng yuqori parent fallback sifatida olinadi.
                             */
                            hierarchy.hierarchy_depth DESC
                    ) AS candidate_rank

                FROM mkb_hierarchy hierarchy
            ),

            resolved_codes AS (
                SELECT
                    filtered_code.region_code,

                    COALESCE(
                        ranked_candidate.current_code,
                        'UNCLASSIFIED'
                    ) AS disease_code,

                    filtered_code.cases_count

                FROM filtered_codes filtered_code

                LEFT JOIN ranked_candidates ranked_candidate
                       ON ranked_candidate.region_code
                          IS NOT DISTINCT FROM
                          filtered_code.region_code

                      AND ranked_candidate.source_code
                          IS NOT DISTINCT FROM
                          filtered_code.source_code

                      AND ranked_candidate.candidate_rank = 1
            )

            SELECT
                resolved_code.region_code,
                resolved_code.disease_code,

                SUM(resolved_code.cases_count)
                    AS cases_count

            FROM resolved_codes resolved_code

            GROUP BY
                resolved_code.region_code,
                resolved_code.disease_code

            ORDER BY
                resolved_code.region_code,
                resolved_code.disease_code
            """;

    /**
     * MonitoringLocationRow record constructor order
     * this mapper order must be identical.
     */
    private static final RowMapper<MonitoringLocationRow>
            LOCATION_STATISTICS_ROW_MAPPER =
            (resultSet, rowNumber) ->
                    new MonitoringLocationRow(
                            resultSet.getString("region_code"),

                            resultSet.getLong("total_cases"),

                            resultSet.getLong("confirmed"),
                            resultSet.getLong("unconfirmed"),

                            resultSet.getLong("male"),
                            resultSet.getLong("female"),
                            resultSet.getLong("unknown_gender"),

                            resultSet.getLong("age_0_3"),
                            resultSet.getLong("age_4_6"),
                            resultSet.getLong("age_7_10"),
                            resultSet.getLong("age_11_17"),
                            resultSet.getLong("age_18_25"),
                            resultSet.getLong("age_26_45"),
                            resultSet.getLong("age_46_65"),
                            resultSet.getLong("age_66_plus"),
                            resultSet.getLong("unknown_age")
                    );

    private static final RowMapper<MonitoringDiseaseRow>
            DISEASE_STATISTICS_ROW_MAPPER =
            (resultSet, rowNumber) ->
                    new MonitoringDiseaseRow(
                            resultSet.getString("region_code"),
                            resultSet.getString("disease_code"),
                            resultSet.getLong("cases_count")
                    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<MonitoringLocationRow> findLocationStatistics(
            MonitoringFilter filter
    ) {
        validateFilter(filter);

        boolean filterByRegion =
                filter.hasRegionFilter();

        String sql =
                buildLocationStatisticsSql(
                        filter,
                        filterByRegion
                );

        MapSqlParameterSource parameters =
                createCommonParameters(filter)
                        .addValue(
                                "catalogType",
                                filterByRegion
                                        ? CITY_CATALOG_TYPE
                                        : STATE_CATALOG_TYPE,
                                Types.VARCHAR
                        )
                        .addValue(
                                "approvedStatus",
                                FormStatus.APPROVED.name(),
                                Types.VARCHAR
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                LOCATION_STATISTICS_ROW_MAPPER
        );
    }

    @Override
    public List<MonitoringDiseaseRow> findDiseaseStatistics(
            MonitoringFilter filter
    ) {
        validateFilter(filter);

        String regionColumn =
                resolveRegionColumn(filter);

        String organizationRegionFilterSql =
                resolveOrganizationRegionFilterSql(filter);

        String mkbFilterSql =
                resolveMkbFilterSql(filter);

        String sql =
                DISEASE_STATISTICS_SQL.formatted(
                        regionColumn,
                        organizationRegionFilterSql,
                        mkbFilterSql
                );

        MapSqlParameterSource parameters =
                createCommonParameters(filter)
                        .addValue(
                                "maxHierarchyDepth",
                                MAX_MKB_HIERARCHY_DEPTH,
                                Types.INTEGER
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                DISEASE_STATISTICS_ROW_MAPPER
        );
    }

    private String buildLocationStatisticsSql(
            MonitoringFilter filter,
            boolean filterByRegion
    ) {
        String regionColumn =
                resolveRegionColumn(filter);

        String baseRegionFilterSql =
                filterByRegion
                        ? BASE_CITY_FILTER_SQL
                        : "";

        String organizationRegionFilterSql =
                resolveOrganizationRegionFilterSql(filter);

        String mkbFilterSql =
                resolveMkbFilterSql(filter);

        return LOCATION_STATISTICS_SQL.formatted(
                baseRegionFilterSql,
                regionColumn,
                organizationRegionFilterSql,
                mkbFilterSql
        );
    }

    /**
     * No region filter:
     * grouped by organization.state_code.
     * <p>
     * Region filter available:
     * grouped by organization.city_code.
     */
    private String resolveRegionColumn(
            MonitoringFilter filter
    ) {
        return filter.hasRegionFilter()
                ? CITY_CODE_SQL_EXPRESSION
                : STATE_CODE_SQL_EXPRESSION;
    }

    private String resolveOrganizationRegionFilterSql(
            MonitoringFilter filter
    ) {
        return filter.hasRegionFilter()
                ? ORGANIZATION_REGION_FILTER_SQL
                : "";
    }

    private String resolveMkbFilterSql(
            MonitoringFilter filter
    ) {
        return filter.hasMkb10Filter()
                ? MKB_FILTER_SQL
                : "";
    }

    private MapSqlParameterSource createCommonParameters(
            MonitoringFilter filter
    ) {
        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "fromDateTime",
                                filter.fromDateTime(),
                                Types.TIMESTAMP
                        )
                        .addValue(
                                "toDateTimeExclusive",
                                filter.toDateTimeExclusive(),
                                Types.TIMESTAMP
                        );

        if (filter.hasRegionFilter()) {
            parameters.addValue(
                    "regionCode",
                    normalizeRegionCode(
                            filter.regionCode()
                    ),
                    Types.VARCHAR
            );
        }

        if (filter.hasMkb10Filter()) {
            parameters.addValue(
                    "mkb10Code",
                    normalizeMkb10Code(
                            filter.mkb10Code()
                    ),
                    Types.VARCHAR
            );
        }

        return parameters;
    }

    private String normalizeRegionCode(
            String regionCode
    ) {
        return Objects.requireNonNull(
                        regionCode,
                        "Monitoring regionCode must not be null"
                )
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    /**
     * MKB code with normalization within SQL
     * is reduced to the same form.
     * <p>
     * Examples:
     * " (A00-A09) " -> "A00-A09"
     * " a00-a09 " -> "A00-A09"
     * " ((A00)) " -> "A00"
     */
    private String normalizeMkb10Code(
            String mkb10Code
    ) {
        String normalized =
                Objects.requireNonNull(
                                mkb10Code,
                                "Monitoring mkb10Code must not be null"
                        )
                        .trim();

        while (normalized.startsWith("(")) {
            normalized =
                    normalized.substring(1).trim();
        }

        while (normalized.endsWith(")")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    ).trim();
        }

        return normalized.toUpperCase(Locale.ROOT);
    }

    private void validateFilter(
            MonitoringFilter filter
    ) {
        Objects.requireNonNull(
                filter,
                "Monitoring filter must not be null"
        );

        Objects.requireNonNull(
                filter.fromDate(),
                "Monitoring fromDate must not be null"
        );

        Objects.requireNonNull(
                filter.toDate(),
                "Monitoring toDate must not be null"
        );

        Objects.requireNonNull(
                filter.fromDateTime(),
                "Monitoring fromDateTime must not be null"
        );

        Objects.requireNonNull(
                filter.toDateTimeExclusive(),
                "Monitoring toDateTimeExclusive must not be null"
        );

        if (filter.fromDate().isAfter(filter.toDate())) {
            throw new IllegalArgumentException(
                    "Monitoring fromDate must not be after toDate"
            );
        }

        if (!filter.fromDateTime()
                .isBefore(filter.toDateTimeExclusive())) {
            throw new IllegalArgumentException(
                    "Monitoring fromDateTime must be before "
                            + "toDateTimeExclusive"
            );
        }

        if (filter.hasRegionFilter()
                && filter.regionCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Monitoring regionCode must not be blank"
            );
        }

        if (filter.hasMkb10Filter()
                && filter.mkb10Code().isBlank()) {
            throw new IllegalArgumentException(
                    "Monitoring mkb10Code must not be blank"
            );
        }
    }
}
