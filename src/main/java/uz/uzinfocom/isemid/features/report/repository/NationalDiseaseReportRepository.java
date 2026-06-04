package uz.uzinfocom.isemid.features.report.repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.commons.exceptions.ForbiddenException;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.report.dtos.response.LocationReportRawData;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NationalDiseaseReportRepository {

  private final NamedParameterJdbcTemplate template;

  private static final String QUERY = """
            SELECT
                %s                                                             AS code,
                COUNT(*) FILTER (WHERE f.status = 'APPROVED')                  AS confirmed,
                COUNT(*) FILTER (WHERE f.status IS DISTINCT FROM 'APPROVED')   AS unconfirmed
            FROM form058 f
                LEFT JOIN patient p    ON f.patient_id = p.id
                LEFT JOIN pt_address a ON p.id = a.patient_id
            WHERE a.type = 'TEMPORARY'
              AND (:fromDate::date   IS NULL OR f.created_at >= :fromDate)
              AND (:toDate::date     IS NULL OR f.created_at <  :toDate)
              AND (:mkb10Code::varchar  IS NULL OR f.final_mkb10code = :mkb10Code)
              AND (:parentCode::varchar IS NULL OR %s = :parentCode)
              %s 
            GROUP BY %s
            ORDER BY %s
            """;

  public List<LocationReportRawData> getLocationReport(
      String mkb10Code,
      LocalDate fromDate,
      LocalDate toDate,
      String parentCode,
      LevelType level,
      String orgStateCode,
      String orgCityCode) {

    String col      = resolveColumn(level);
    String parentCol = resolveParentColumn(level,orgStateCode);
    String scopeWhere = resolveScopeWhere(level, orgStateCode);
    String sql      = QUERY.formatted(col, parentCol,scopeWhere, col, col).strip();

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("fromDate",  fromDate,  Types.TIMESTAMP);
    params.addValue("toDate",    toDate,    Types.TIMESTAMP);
    params.addValue("mkb10Code",  mkb10Code,  Types.VARCHAR);
    params.addValue("parentCode",  parentCode,  Types.VARCHAR);
    resolveScopeParams(params, level, orgStateCode, orgCityCode);


    log.info("SQL:\n{}", sql);
    log.info("Params: {}", params.getValues());
    return template.query(sql, params, (rs,row) ->
      new LocationReportRawData(rs.getString("code"),
          rs.getInt("confirmed"),
          rs.getInt("unconfirmed"))
    );
  }

  private String resolveScopeWhere(LevelType level, String orgStateCode) {
    return switch (level) {
      case REPUBLICAN                         -> "";
      case REGIONAL                           -> "AND a.state_code = :scopeStateCode";
      case URBAN                              -> "UZ-TK".equals(orgStateCode)
          ? "AND a.state_code = :scopeStateCode"
          : "AND a.city_code = :scopeCityCode";
      case AREA, DISTRICT,
           INTERDISTRICT, NOT_DEFINED         -> "AND a.state_code = :scopeStateCode AND a.city_code = :scopeCityCode";
    };
  }

  private String resolveColumn(LevelType level) {
    return switch (level) {
      case REPUBLICAN                         -> "a.state_code";
      case REGIONAL, URBAN                    -> "a.city_code";
      case AREA, DISTRICT,
           INTERDISTRICT, NOT_DEFINED         -> "a.neighborhood_code";
    };
  }

  private String resolveParentColumn(LevelType level, String orgStateCode) {
    return switch (level) {
      case REPUBLICAN, REGIONAL         -> "a.state_code";
      case URBAN                        -> "UZ-TK".equals(orgStateCode)
          ? "a.state_code"
          : "a.city_code";
      case AREA, DISTRICT,
           INTERDISTRICT, NOT_DEFINED   -> "a.city_code";
    };
  }

  private void resolveScopeParams(
      MapSqlParameterSource params,
      LevelType level,
      String orgStateCode,
      String orgCityCode) {
    switch (level) {
      case REPUBLICAN                                    -> { /* no scope */ }
      case REGIONAL                                      -> params.addValue("scopeStateCode", orgStateCode, Types.VARCHAR);
      case URBAN                                         -> {
        if ("UZ-TK".equals(orgStateCode)) {
          params.addValue("scopeStateCode", orgStateCode, Types.VARCHAR);
        } else {
          params.addValue("scopeCityCode", orgCityCode, Types.VARCHAR);
        }
      }
      case AREA, DISTRICT, INTERDISTRICT, NOT_DEFINED   -> {
        params.addValue("scopeStateCode", orgStateCode, Types.VARCHAR);
        params.addValue("scopeCityCode",  orgCityCode,  Types.VARCHAR);
      }
    }
  }
}
