package uz.uzinfocom.isemid.features.report.repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.report.dtos.response.LocationReportRow;
import uz.uzinfocom.isemid.features.report.service.scope.ReportScope;

/**
 * Native, set-based location report.
 *
 * <p>The {@code cls_catalog} dimension drives the result, so every location of the target
 * granularity yields a row even with zero cases (no Java-side merge). Localized name,
 * confirmed/unconfirmed split, the sanepid visibility matrix, and the grand total are all
 * computed inside PostgreSQL in a single round-trip. No entities are materialized.</p>
 *
 * <p>The two {@code %s} tokens are filled from server-side enums/switches
 * ({@link ReportScope#dimension()} address column and
 * {@link ReportScope#visibilityLevelSql()}) and never contain request strings, so there is
 * no injection surface. All value inputs are named bind parameters.</p>
 */
@Repository
@Slf4j
public class LocationReportRepository {

    // language=sql
    private static final String SQL = """
        WITH base AS (
                                     SELECT
                                         c.code,
                                         CASE :locale
                                             WHEN 'uz-Cyrl' THEN c.name_uz_cyril
                                             WHEN 'ru'      THEN c.name_ru
                                             WHEN 'kaa'     THEN c.name_kaa
                                             ELSE                c.name_uz
                                         END AS name,
                                         f.id,
                                         f.status
                                     FROM cls_catalog c
                                     LEFT JOIN pt_address a
                                            ON a.%s = c.code
                                           AND a.type = 'TEMPORARY'
                                     LEFT JOIN form058 f
                                            ON f.patient_id = a.patient_id
                                           AND (CAST(:fromDate AS date)     IS NULL OR f.created_at >= :fromDate)
                                           AND (CAST(:toDate AS date)       IS NULL OR f.created_at <  :toDate)
                                           AND (CAST(:mkb10Code AS varchar) IS NULL OR f.final_mkb10code = :mkb10Code)
                                     LEFT JOIN organization co
                                            ON co.uuid = f.created_org_uuid
                                     WHERE c.type = :dimType
                                       AND (CAST(:dimParent AS varchar) IS NULL OR c.parent_code = :dimParent)
                                       AND ( f.id IS NULL
                                             OR :isSanepid = FALSE
                                             OR f.sender_organization_id = :currentOrgId
                                             OR (%s) )
                                 )
                                 SELECT
                                     code,
                                     name,
                                     COUNT(*) FILTER (WHERE id IS NOT NULL AND status = 'APPROVED')                AS confirmed,
                                     COUNT(*) FILTER (WHERE id IS NOT NULL AND status IS DISTINCT FROM 'APPROVED') AS unconfirmed,
                                     GROUPING(code) AS is_total
                                 FROM base
                                 GROUP BY GROUPING SETS (
                                     (code, name),
                                     ()
                                 )
                                 ORDER BY is_total, code;
            """;

    private final NamedParameterJdbcTemplate template;

    public LocationReportRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * @param toDate exclusive upper bound (half-open interval {@code [fromDate, toDate)})
     */
    public List<LocationReportRow> fetch(
            String mkb10Code,
            LocalDate fromDate,
            LocalDate toDate,
            ReportScope scope,
            String locale) {

        String sql = SQL.formatted(
                scope.dimension().addressColumn(),
                scope.visibilityLevelSql());

        log.info("Fetching location reports for {}", scope);
        log.info("dimension (catalogType.name): {}", scope.dimension().catalogType().name());
        log.info("dimension (catalogType): {}", scope.dimension().catalogType());

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("locale", locale, Types.VARCHAR)
                .addValue("dimType", scope.dimension().catalogType().name(), Types.VARCHAR)
                .addValue("dimParent", scope.dimensionParentCode(), Types.VARCHAR)
                .addValue("fromDate", fromDate, Types.DATE)
                .addValue("toDate", toDate, Types.DATE)
                .addValue("mkb10Code", mkb10Code, Types.VARCHAR)
                .addValue("isSanepid", scope.isSanepid(), Types.BOOLEAN)
                .addValue("currentOrgId", scope.currentOrgId())
                .addValue("orgStateCode", scope.orgStateCode(), Types.VARCHAR)
                .addValue("orgCityCode", scope.orgCityCode(), Types.VARCHAR);

        return template.query(sql, params, (rs, row) -> new LocationReportRow(
                rs.getString("code"),
                rs.getString("name"),
                rs.getInt("confirmed"),
                rs.getInt("unconfirmed"),
                rs.getInt("is_total") == 1));
    }
}
