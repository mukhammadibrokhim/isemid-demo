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
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class JdbcMonitoringQueryRepository
        implements MonitoringQueryRepository {

    private static final int MAX_MKB_HIERARCHY_DEPTH = 32;

    private static final String STATE_REGION_COLUMN =
            "organization.state_code";

    private static final String CITY_REGION_COLUMN =
            "organization.city_code";

    private static final String MKB_FILTER_SQL = """
              AND UPPER(
                    TRIM(
                        BOTH '()'
                        FROM TRIM(form058.final_mkb10code)
                    )
                  ) = :mkb10Code
            """;

    private static final String REGION_FILTER_SQL = """
              AND organization.state_code = :regionCode
            """;

    private static final String REGION_BY_STATE_SQL = """
            WITH base_regions AS (
                SELECT
                    catalog.code AS region_code
                FROM cls_catalog catalog
                WHERE catalog.type = 'state'
            ),
            filtered_forms AS (
                SELECT
                    form058.id AS form_id,
                    organization.state_code AS region_code,
                    form058.status AS form_status,

                    LOWER(TRIM(patient.gender_code))
                        AS gender_code,

                    CASE
                        WHEN patient.birth_date IS NULL
                            THEN NULL

                        WHEN patient.birth_date >
                             CAST(form058.created_at AS DATE)
                            THEN NULL

                        ELSE EXTRACT(
                            YEAR FROM AGE(
                                CAST(form058.created_at AS DATE),
                                patient.birth_date
                            )
                        )::INTEGER
                    END AS age_years

                FROM form058 form058

                INNER JOIN organization organization
                        ON organization.uuid =
                           form058.created_org_uuid

                LEFT JOIN patient patient
                       ON patient.id = form058.patient_id

                WHERE form058.created_at >= :fromDateTime
                  AND form058.created_at < :toDateTimeExclusive

                %s
            )
            SELECT
                base_region.region_code,

                COUNT(form_data.form_id)
                    AS total_cases,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.form_status = :approvedStatus
                ) AS confirmed,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.form_status
                          IS DISTINCT FROM :approvedStatus
                ) AS unconfirmed,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.gender_code = 'male'
                ) AS male,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.gender_code = 'female'
                ) AS female,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 0 AND 3
                ) AS age_0_3,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 4 AND 6
                ) AS age_4_6,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 7 AND 10
                ) AS age_7_10,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 11 AND 17
                ) AS age_11_17,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 18 AND 25
                ) AS age_18_25,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 26 AND 45
                ) AS age_26_45,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 46 AND 65
                ) AS age_46_65,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 66
                ) AS age_66_plus

            FROM base_regions base_region

            LEFT JOIN filtered_forms form_data
                   ON form_data.region_code =
                      base_region.region_code

            GROUP BY base_region.region_code
            ORDER BY base_region.region_code
            """;

    private static final String REGION_BY_CITY_SQL = """
            WITH base_regions AS (
                SELECT
                    catalog.code AS region_code
                FROM cls_catalog catalog
                WHERE catalog.type = 'city'
                  AND catalog.parent_code = :regionCode
            ),
            filtered_forms AS (
                SELECT
                    form058.id AS form_id,
                    organization.city_code AS region_code,
                    form058.status AS form_status,

                    LOWER(TRIM(patient.gender_code))
                        AS gender_code,

                    CASE
                        WHEN patient.birth_date IS NULL
                            THEN NULL

                        WHEN patient.birth_date >
                             CAST(form058.created_at AS DATE)
                            THEN NULL

                        ELSE EXTRACT(
                            YEAR FROM AGE(
                                CAST(form058.created_at AS DATE),
                                patient.birth_date
                            )
                        )::INTEGER
                    END AS age_years

                FROM form058 form058

                INNER JOIN organization organization
                        ON organization.uuid =
                           form058.created_org_uuid

                LEFT JOIN patient patient
                       ON patient.id = form058.patient_id

                WHERE form058.created_at >= :fromDateTime
                  AND form058.created_at < :toDateTimeExclusive

                  AND organization.state_code = :regionCode

                %s
            )
            SELECT
                base_region.region_code,

                COUNT(form_data.form_id)
                    AS total_cases,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.form_status = :approvedStatus
                ) AS confirmed,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.form_status
                          IS DISTINCT FROM :approvedStatus
                ) AS unconfirmed,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.gender_code = 'male'
                ) AS male,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.gender_code = 'female'
                ) AS female,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 0 AND 3
                ) AS age_0_3,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 4 AND 6
                ) AS age_4_6,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 7 AND 10
                ) AS age_7_10,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 11 AND 17
                ) AS age_11_17,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 18 AND 25
                ) AS age_18_25,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 26 AND 45
                ) AS age_26_45,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years BETWEEN 46 AND 65
                ) AS age_46_65,

                COUNT(form_data.form_id) FILTER (
                    WHERE form_data.age_years >= 66
                ) AS age_66_plus

            FROM base_regions base_region

            LEFT JOIN filtered_forms form_data
                   ON form_data.region_code =
                      base_region.region_code

            GROUP BY base_region.region_code
            ORDER BY base_region.region_code
            """;

    private static final String DISEASE_STATISTICS_SQL = """
            WITH RECURSIVE filtered_codes AS (
                SELECT
                    %s AS region_code,

                    UPPER(
                        TRIM(
                            BOTH '()'
                            FROM TRIM(form058.final_mkb10code)
                        )
                    ) AS source_code,

                    COUNT(form058.id)
                        AS cases_count

                FROM form058 form058

                INNER JOIN organization organization
                        ON organization.uuid =
                           form058.created_org_uuid

                WHERE form058.created_at >= :fromDateTime
                  AND form058.created_at < :toDateTimeExclusive

                  AND form058.final_mkb10code IS NOT NULL
                  AND TRIM(form058.final_mkb10code) <> ''

                  AND LOWER(
                        TRIM(form058.final_mkb10code)
                      ) <> 'string'

                %s
                %s

                GROUP BY
                    %s,
                    UPPER(
                        TRIM(
                            BOTH '()'
                            FROM TRIM(form058.final_mkb10code)
                        )
                    )
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
                        ON parent.id = hierarchy.parent_id

                WHERE hierarchy.hierarchy_depth
                      < :maxHierarchyDepth

                  AND NOT parent.id =
                          ANY(hierarchy.visited_ids)
            ),
            ranked_blocks AS (
                SELECT
                    hierarchy.region_code,
                    hierarchy.source_code,
                    hierarchy.cases_count,
                    hierarchy.current_code,

                    ROW_NUMBER() OVER (
                        PARTITION BY
                            hierarchy.region_code,
                            hierarchy.source_code
                        ORDER BY hierarchy.hierarchy_depth ASC
                    ) AS block_rank

                FROM mkb_hierarchy hierarchy

                WHERE hierarchy.current_code ~
                      '^[A-Z][0-9]{2}-[A-Z][0-9]{2}$'
            )
            SELECT
                ranked_block.region_code,
                ranked_block.current_code AS disease_code,

                SUM(ranked_block.cases_count)
                    AS cases_count

            FROM ranked_blocks ranked_block

            WHERE ranked_block.block_rank = 1

            GROUP BY
                ranked_block.region_code,
                ranked_block.current_code

            ORDER BY
                ranked_block.region_code,
                ranked_block.current_code
            """;

    private static final RowMapper<MonitoringLocationRow>
            REGION_ROW_MAPPER =
            (resultSet, rowNumber) ->
                    new MonitoringLocationRow(
                            resultSet.getString("region_code"),
                            resultSet.getLong("total_cases"),
                            resultSet.getLong("confirmed"),
                            resultSet.getLong("unconfirmed"),
                            resultSet.getLong("male"),
                            resultSet.getLong("female"),
                            resultSet.getLong("age_0_3"),
                            resultSet.getLong("age_4_6"),
                            resultSet.getLong("age_7_10"),
                            resultSet.getLong("age_11_17"),
                            resultSet.getLong("age_18_25"),
                            resultSet.getLong("age_26_45"),
                            resultSet.getLong("age_46_65"),
                            resultSet.getLong("age_66_plus")
                    );

    private static final RowMapper<MonitoringDiseaseRow>
            DISEASE_ROW_MAPPER =
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

        String mkbFilterSql = filter.hasMkb10Filter()
                ? MKB_FILTER_SQL
                : "";

        String sql = filter.hasRegionFilter()
                ? REGION_BY_CITY_SQL.formatted(mkbFilterSql)
                : REGION_BY_STATE_SQL.formatted(mkbFilterSql);

        MapSqlParameterSource parameters =
                createCommonParameters(filter)
                        .addValue(
                                "approvedStatus",
                                FormStatus.APPROVED.name(),
                                Types.VARCHAR
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                REGION_ROW_MAPPER
        );
    }

    @Override
    public List<MonitoringDiseaseRow> findDiseaseStatistics(
            MonitoringFilter filter
    ) {
        validateFilter(filter);

        String regionColumn = filter.hasRegionFilter()
                ? CITY_REGION_COLUMN
                : STATE_REGION_COLUMN;

        String regionFilterSql = filter.hasRegionFilter()
                ? REGION_FILTER_SQL
                : "";

        String mkbFilterSql = filter.hasMkb10Filter()
                ? MKB_FILTER_SQL
                : "";

        String sql = DISEASE_STATISTICS_SQL.formatted(
                regionColumn,
                regionFilterSql,
                mkbFilterSql,
                regionColumn
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
                DISEASE_ROW_MAPPER
        );
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
                    filter.regionCode(),
                    Types.VARCHAR
            );
        }

        if (filter.hasMkb10Filter()) {
            parameters.addValue(
                    "mkb10Code",
                    filter.mkb10Code(),
                    Types.VARCHAR
            );
        }

        return parameters;
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

        if (filter.fromDate().isAfter(filter.toDate())) {
            throw new IllegalArgumentException(
                    "Monitoring fromDate must not be after toDate"
            );
        }
    }

}
