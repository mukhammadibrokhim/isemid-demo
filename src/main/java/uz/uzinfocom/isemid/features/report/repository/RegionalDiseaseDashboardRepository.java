package uz.uzinfocom.isemid.features.report.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.features.report.dtos.CaseCountDynamicsData;
import uz.uzinfocom.isemid.features.report.dtos.request.DiseaseFilterRequest;

@Repository
@RequiredArgsConstructor
public class RegionalDiseaseDashboardRepository {
  private final NamedParameterJdbcTemplate template;

  public List<CaseCountDynamicsData> filterDiseaseAggregatingData(DiseaseFilterRequest filterRequest){
    MapSqlParameterSource params = new MapSqlParameterSource();
    final var dateTimeRange = getDateTimeRange(filterRequest.fromDate(), filterRequest.toDate());
    params.addValue("dat1", dateTimeRange.from());
    params.addValue("dat2", dateTimeRange.to());
    params.addValue("regionCode", filterRequest.regionCode());
    params.addValue("mkb10Code", filterRequest.mkb10Code());
    var granularity = getGranularity(filterRequest.fromDate(), filterRequest.toDate());
    var format = getFormat(granularity);

    boolean isWeek = granularity.equals("week");
    var seriesStart = isWeek
        ? ":dat1::TIMESTAMP"
        : "DATE_TRUNC('{{granularity}}', :dat1::TIMESTAMP)";
    var periodLabelInSeries = isWeek
        ? "TO_CHAR(gs, '{{format}}')"
        : "TO_CHAR(DATE_TRUNC('{{granularity}}', gs), '{{format}}')";
    var rawPeriodLabel = isWeek
        ? "TO_CHAR(:dat1::DATE + (((f.created_at::DATE - :dat1::DATE) / 7) * 7), '{{format}}')"
        : "TO_CHAR(DATE_TRUNC('{{granularity}}', f.created_at), '{{format}}')";

    var queryTemplate = """
        WITH periods AS (
            SELECT {{period_label_in_series}} AS period
            FROM generate_series(
                {{series_start}},
                {{series_end}},
                INTERVAL '1 {{granularity}}'
            ) AS gs
        ),
        raw_data AS (
            SELECT
                {{raw_period_label}} AS period,
                f.status,
                p.gender_code,
                o.state_code,
                CASE
                    WHEN EXTRACT(YEAR FROM AGE(p.birth_date)) < 3  THEN 'age_0_3'
                    WHEN EXTRACT(YEAR FROM AGE(p.birth_date)) < 6  THEN 'age_3_6'
                    WHEN EXTRACT(YEAR FROM AGE(p.birth_date)) < 10 THEN 'age_6_10'
                    WHEN EXTRACT(YEAR FROM AGE(p.birth_date)) < 17 THEN 'age_10_17'
                    WHEN EXTRACT(YEAR FROM AGE(p.birth_date)) < 25 THEN 'age_18_25'
                    WHEN EXTRACT(YEAR FROM AGE(p.birth_date)) < 45 THEN 'age_25_45'
                    WHEN EXTRACT(YEAR FROM AGE(p.birth_date)) < 65 THEN 'age_45_65'
                    ELSE 'age_65_plus'
                END AS age_group
            FROM form058 f
            LEFT JOIN organization o ON f.created_org_uuid = o.uuid
            LEFT JOIN patient p ON f.patient_id = p.id
            WHERE f.created_at >= :dat1 AND f.created_at < :dat2
            AND (:regionCode::VARCHAR IS NULL OR o.state_code = :regionCode::VARCHAR)
            AND (:mkb10Code::VARCHAR  IS NULL OR f.final_mkb10code = :mkb10Code::VARCHAR)
        ),
        aggregated AS (
            SELECT
                period,
                state_code,
                COUNT(*) FILTER (WHERE status = 'CONFIRMED')    AS confirmed,
                COUNT(*) FILTER (WHERE status != 'CONFIRMED')   AS unconfirmed,
                COUNT(*) FILTER (WHERE UPPER(gender_code) = 'MALE')    AS male,
                COUNT(*) FILTER (WHERE UPPER(gender_code) = 'FEMALE')  AS female,
                COUNT(*) FILTER (WHERE age_group = 'age_0_3')   AS age_0_3,
                COUNT(*) FILTER (WHERE age_group = 'age_3_6')   AS age_3_6,
                COUNT(*) FILTER (WHERE age_group = 'age_6_10')  AS age_6_10,
                COUNT(*) FILTER (WHERE age_group = 'age_10_17') AS age_10_17,
                COUNT(*) FILTER (WHERE age_group = 'age_18_25') AS age_18_25,
                COUNT(*) FILTER (WHERE age_group = 'age_25_45') AS age_25_45,
                COUNT(*) FILTER (WHERE age_group = 'age_45_65') AS age_45_65,
                COUNT(*) FILTER (WHERE age_group = 'age_65_plus') AS age_65_plus
            FROM raw_data
            GROUP BY period, state_code
        )
        SELECT
            p.period,
            COALESCE(a.state_code, :regionCode::VARCHAR) AS location_code,
            COALESCE(a.confirmed, 0)              AS confirmed,
            COALESCE(a.unconfirmed, 0)            AS unconfirmed,
            COALESCE(a.male, 0)                   AS male,
            COALESCE(a.female, 0)                 AS female,
            COALESCE(a.age_0_3, 0)                AS age_0_3,
            COALESCE(a.age_3_6, 0)                AS age_3_6,
            COALESCE(a.age_6_10, 0)               AS age_6_10,
            COALESCE(a.age_10_17, 0)              AS age_10_17,
            COALESCE(a.age_18_25, 0)              AS age_18_25,
            COALESCE(a.age_25_45, 0)              AS age_25_45,
            COALESCE(a.age_45_65, 0)              AS age_45_65,
            COALESCE(a.age_65_plus, 0)            AS age_65_plus
        FROM periods p
        LEFT JOIN aggregated a ON p.period = a.period
        ORDER BY p.period
        """;
    var seriesEnd = getSeriesEnd(granularity, filterRequest);

    var query =
        queryTemplate
            .replace("{{series_start}}", seriesStart)
            .replace("{{series_end}}", seriesEnd)
            .replace("{{period_label_in_series}}", periodLabelInSeries)
            .replace("{{raw_period_label}}", rawPeriodLabel)
            .replace("{{granularity}}", granularity)
            .replace("{{format}}", format);

    try {
      return template.query(
          query,
          params,
          (rs, raw) -> new CaseCountDynamicsData(
              rs.getString("period"),
              rs.getString("location_code"),
              rs.getInt("confirmed"),
              rs.getInt("unconfirmed"),
              rs.getInt("male"),
              rs.getInt("female"),
              rs.getInt("age_0_3"),
              rs.getInt("age_3_6"),
              rs.getInt("age_6_10"),
              rs.getInt("age_10_17"),
              rs.getInt("age_18_25"),
              rs.getInt("age_25_45"),
              rs.getInt("age_45_65"),
              rs.getInt("age_65_plus")));
    } catch (DataAccessException e) {
      throw new RuntimeException("invalid db command", e);
    }
  }

  private static String getSeriesEnd(String granularity, DiseaseFilterRequest requestData) {
    final String seriesEnd;

    if (granularity.equals("hour")
        && requestData.fromDate().isEqual(requestData.toDate())
        && !requestData.fromDate().isEqual(LocalDate.now())) {

      seriesEnd = "DATE_TRUNC('{{granularity}}', :dat2::TIMESTAMP) - INTERVAL '1 {{granularity}}'";

    } else if (granularity.equals("day") && !requestData.fromDate().isEqual(LocalDate.now())) {

      seriesEnd = "DATE_TRUNC('{{granularity}}', :dat2::TIMESTAMP) - INTERVAL '1 {{granularity}}'";

    } else {

      seriesEnd = "DATE_TRUNC('{{granularity}}', :dat2::TIMESTAMP)";
    }
    return seriesEnd;
  }

  private static Range getDateTimeRange(LocalDate from, LocalDate to) {
    if (to.isAfter(from)) {
      var startDate = from.atStartOfDay();
      var endDate = to.plusDays(1).atTime(LocalTime.MIN);
      return new Range(startDate, endDate);
    } else if (to.isEqual(from) && from.isBefore(LocalDate.now())) {
      var startDate = from.atStartOfDay();
      var endDate = to.plusDays(1).atTime(LocalTime.MIN);
      return new Range(startDate, endDate);
    } else if (to.isEqual(from) && from.isEqual(LocalDate.now())) {
      var startDate = from.atStartOfDay();
      var endDate = LocalDateTime.of(to, LocalTime.now());
      return new Range(startDate, endDate);
    } else {
      throw new ApiException(HttpStatus.BAD_REQUEST,"invalid date range");
    }
  }

  private static String getGranularity(LocalDate from, LocalDate to) {
    long daysBetween = ChronoUnit.DAYS.between(from, to);

    String granularity;
    if (daysBetween <= 1) {
      granularity = "hour";
    } else if (daysBetween <= 30) {
      granularity = "day";
    } else if (daysBetween <= 180) {
      granularity = "week";
    } else {
      granularity = "month";
    }
    return granularity;
  }

  private static String getFormat(String granularity) {
    return switch (granularity) {
      case "hour" -> "YYYY-MM-DD HH24";
      case "day" -> "YYYY-MM-DD";
      case "week" -> "YYYY-MM-DD"; // ISO year + week
      case "month" -> "YYYY-MM";
      default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
    };
  }

  record Range(LocalDateTime from, LocalDateTime to) {}

}
