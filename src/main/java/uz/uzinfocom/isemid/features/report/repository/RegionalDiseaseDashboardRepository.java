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
    var queryTemplate = """
        WITH periods AS (
                  SELECT TO_CHAR(DATE_TRUNC('{{granularity}}', gs), '{{format}}') AS period
                                      FROM generate_series(
                                          DATE_TRUNC('{{granularity}}', :dat1::TIMESTAMP),
                                          {{series_end}},
                                          INTERVAL '1 {{granularity}}'
                                      ) AS gs
            ),
            
            
        """;
    var seriesEnd = getSeriesEnd(granularity, filterRequest);

    var query =
        queryTemplate
            .replace("{{series_end}}", seriesEnd)
            .replace("{{granularity}}", granularity)
            .replace("{{format}}", format);
    try {
      return template.query(
          query,
          params,
          (rs, raw) ->
              new CaseCountDynamicsData(null,null,0,0,0,0));
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
      var startDate = LocalDateTime.of(from, LocalTime.of(0, 0, 0));
      var endDate = LocalDateTime.of(to.plusDays(1), LocalTime.of(0, 0, 0));
      return new Range(startDate, endDate);
    } else if (to.isEqual(from) && from.isBefore(LocalDate.now())) {
      var startDate = LocalDateTime.of(from, LocalTime.of(0, 0, 0));
      var endDate = startDate.plusDays(1);
      return new Range(startDate, endDate);
    } else if (to.isEqual(from) && from.isEqual(LocalDate.now())) {
      var startDate = LocalDateTime.of(from, LocalTime.of(0, 0, 0));
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
