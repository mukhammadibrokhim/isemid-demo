package uz.uzinfocom.isemid.features.report.repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.commons.exceptions.ForbiddenException;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.report.dtos.response.LocationReportRawData;

@Repository
@RequiredArgsConstructor
public class NationalDiseaseReportRepository {

  private final NamedParameterJdbcTemplate template;

  private static final String QUERY = """
            SELECT
                %s                                                              AS code,
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
      Organization organization) {


    if (organization == null) {
      throw new ForbiddenException("User has no active organization");
    }

    LevelType level = organization.getLevelType();

    String col      = resolveColumn(level);
    String parentCol = resolveParentColumn(level);
    String where    = resolveAccessWhere(level);
    String sql      = QUERY.formatted(col, parentCol, where, col, col).strip();

    MapSqlParameterSource params = resolveAccessParams(organization);
    params.addValue("fromDate",  fromDate,  Types.TIMESTAMP);
    params.addValue("toDate",    toDate,    Types.TIMESTAMP);
    params.addValue("mkb10Code",  mkb10Code,  Types.VARCHAR);
    params.addValue("parentCode",  parentCode,  Types.VARCHAR);

    return template.query(sql, params, (rs,row) ->
      new LocationReportRawData(rs.getString("code"),
          rs.getInt("confirmed"),
          rs.getInt("unconfirmed"))
    );
  }

  private String resolveColumn(LevelType level) {
    return switch (level) {
      case REPUBLICAN                         -> "a.state_code";
      case REGIONAL, URBAN                    -> "a.city_code";
      case AREA, DISTRICT,
           INTERDISTRICT, NOT_DEFINED         -> "a.neighborhood_code";
    };
  }

  private String resolveParentColumn(LevelType level) {
    return switch (level) {
      case REPUBLICAN,
           REGIONAL, URBAN                    -> "a.state_code";
      case AREA, DISTRICT,
           INTERDISTRICT, NOT_DEFINED         -> "a.city_code";
    };
  }

  private String resolveAccessWhere(LevelType level) {
    return switch (level) {
      case REPUBLICAN                         -> "";
      case REGIONAL, URBAN                    -> "AND a.state_code = :stateCode::varchar";
      case AREA, DISTRICT,
           INTERDISTRICT, NOT_DEFINED         -> "AND a.state_code = :stateCode::varchar AND a.city_code = :cityCode::varchar";
    };
  }

  private MapSqlParameterSource resolveAccessParams(Organization organization) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    LevelType level = organization.getLevelType();

    if (level != LevelType.REPUBLICAN) {
      params.addValue("stateCode", organization.getStateCode());
    }
    if (level != LevelType.REPUBLICAN && level != LevelType.REGIONAL
        && level != LevelType.URBAN) {
      params.addValue("cityCode", organization.getCityCode());
    }

    return params;
  }


}
