package uz.uzinfocom.app.modules.airquality.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirBucketStat;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirTerritoryStat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Native-SQL aggregation over {@code air_measurement} + {@code air_station} for the reports
 * that set air quality against case counts (the "air and respiratory diseases" report and
 * the forecast's air factor). One bucketed series for a region/district, and one row per
 * region/district for a period.
 * <p>
 * Every value is converted to µg/m³ inside SQL ({@code mg/m³} x1000, {@code ng/m³} /1000,
 * a missing unit is taken as µg/m³) and rows in any other unit are left out, mirroring
 * {@code AirQualityNorms#toMicrograms}. {@code measured_at} is the station's local wall
 * clock with no zone, so bucketing truncates it as it is. The {@code date_trunc} field is
 * interpolated only from the callers' fixed literal set ({@code day}/{@code week}/{@code month}).
 */
@Repository
@RequiredArgsConstructor
public class AirAggregateRepository {

    /** Reading in µg/m³ or {@code null} for a unit that cannot be converted. */
    private static final String VALUE_UG = """
            case
                when m.unit is null or btrim(m.unit) = '' then m.value
                when lower(translate(m.unit, 'µμ³ ', 'uu3')) in ('ug/m3', 'mcg/m3') then m.value
                when lower(translate(m.unit, 'µμ³ ', 'uu3')) = 'mg/m3' then m.value * 1000
                when lower(translate(m.unit, 'µμ³ ', 'uu3')) = 'ng/m3' then m.value / 1000
                else null
            end""";

    private final EntityManager entityManager;

    /**
     * Average/maximum of one pollutant per time bucket over {@code [from, to)}; only non-empty
     * buckets. {@code regionCode}/{@code districtCode} narrow the stations, both {@code null}
     * means every station.
     *
     * @param truncField {@code day}, {@code week} or {@code month}
     */
    public List<AirBucketStat> bucketSeries(
            String regionCode, String districtCode, String parameter, String truncField,
            LocalDateTime fromInclusive, LocalDateTime toExclusive
    ) {
        requireTruncField(truncField);
        StringBuilder sql = new StringBuilder()
                .append("select date_trunc('").append(truncField).append("', r.measured_at) as bucket_start, ")
                .append("avg(r.v) as avg_v, max(r.v) as max_v, count(*) as readings from (")
                .append("select m.measured_at as measured_at, ").append(VALUE_UG).append(" as v ")
                .append("from air_measurement m join air_station st on st.id = m.station_id ")
                .append("where m.parameter = :parameter ")
                .append("and m.measured_at >= :fromInclusive and m.measured_at < :toExclusive");
        appendGeo(sql, regionCode, districtCode);
        sql.append(") r where r.v is not null group by 1 order by 1");

        Query query = entityManager.createNativeQuery(sql.toString())
                .setParameter("parameter", parameter)
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive);
        bindGeo(query, regionCode, districtCode);

        List<?> rows = query.getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new AirBucketStat(toLocalDate(r[0]), number(r[1]), number(r[2]), ((Number) r[3]).longValue());
                })
                .toList();
    }

    /**
     * One row per territory with readings of {@code parameter} over {@code [from, to)}.
     * {@code byDistrict} groups by the station's district (stations with no resolved district
     * are left out), otherwise by region.
     *
     * @param normMicrograms the pollutant's norm in µg/m³, used only to count exceedance days
     */
    public List<AirTerritoryStat> territoryStats(
            boolean byDistrict, String regionCode, String districtCode, String parameter,
            LocalDateTime fromInclusive, LocalDateTime toExclusive, BigDecimal normMicrograms
    ) {
        String codeColumn = byDistrict ? "st.district_code" : "st.region_code";
        StringBuilder sql = new StringBuilder()
                .append("with r as (select ").append(codeColumn).append(" as code, m.station_id as station_id, ")
                .append("date_trunc('day', m.measured_at) as day, ").append(VALUE_UG).append(" as v ")
                .append("from air_measurement m join air_station st on st.id = m.station_id ")
                .append("where m.parameter = :parameter ")
                .append("and m.measured_at >= :fromInclusive and m.measured_at < :toExclusive ")
                .append("and ").append(codeColumn).append(" is not null");
        appendGeo(sql, regionCode, districtCode);
        sql.append("), ok as (select * from r where v is not null), ")
                .append("d as (select code, day, avg(v) as d_avg from ok group by code, day), ")
                .append("s as (select code, count(distinct station_id) as stations, count(*) as readings, ")
                .append("avg(v) as avg_v, max(v) as max_v from ok group by code) ")
                .append("select s.code, s.stations, s.readings, s.avg_v, s.max_v, ")
                .append("(select count(*) from d where d.code = s.code and d.d_avg > :norm) as exceed_days, ")
                .append("(select count(*) from d where d.code = s.code) as days_with_data ")
                .append("from s order by s.code");

        Query query = entityManager.createNativeQuery(sql.toString())
                .setParameter("parameter", parameter)
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive)
                .setParameter("norm", normMicrograms);
        bindGeo(query, regionCode, districtCode);

        List<?> rows = query.getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new AirTerritoryStat(
                            (String) r[0], ((Number) r[1]).longValue(), ((Number) r[2]).longValue(),
                            number(r[3]), number(r[4]), ((Number) r[5]).longValue(), ((Number) r[6]).longValue());
                })
                .toList();
    }

    private static void appendGeo(StringBuilder sql, String regionCode, String districtCode) {
        if (StringUtils.hasText(regionCode)) {
            sql.append(" and st.region_code = :regionCode");
        }
        if (StringUtils.hasText(districtCode)) {
            sql.append(" and st.district_code = :districtCode");
        }
    }

    private static void bindGeo(Query query, String regionCode, String districtCode) {
        if (StringUtils.hasText(regionCode)) {
            query.setParameter("regionCode", regionCode);
        }
        if (StringUtils.hasText(districtCode)) {
            query.setParameter("districtCode", districtCode);
        }
    }

    private static void requireTruncField(String truncField) {
        if (!"day".equals(truncField) && !"week".equals(truncField) && !"month".equals(truncField)) {
            throw new IllegalArgumentException("Unsupported bucket: " + truncField);
        }
    }

    private static double number(Object value) {
        return value == null ? 0d : ((Number) value).doubleValue();
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        throw new IllegalStateException("Unexpected bucket_start type: " + value.getClass());
    }
}
