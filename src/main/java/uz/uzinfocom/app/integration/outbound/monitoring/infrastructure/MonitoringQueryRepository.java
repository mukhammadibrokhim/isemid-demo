package uz.uzinfocom.app.integration.outbound.monitoring.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringDiseaseRow;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringDynamicsQuery;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringDynamicsRow;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringDynamicsWindow;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringFilter;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringGranularity;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringLocationRow;

import java.util.List;

/**
 * Native-SQL aggregation behind the integration monitoring endpoint, over
 * {@code form058} only (form058-1 is not part of this report).
 * <p>
 * Locations: with no region filter one row per {@code ref_region}, grouped by
 * the case creator's {@code organization.region_code}; with a region filter
 * one row per {@code ref_district} of that region, grouped by
 * {@code organization.district_code}. Locations with no cases are kept (all
 * zero) because the location list is the {@code left join} driver. The case's
 * location is that of the organization that <b>created</b> it
 * ({@code sender_organization_id}), like every other report.
 * <p>
 * A case counts when it is not soft-deleted and not {@code CANCELED}:
 * {@code confirmed} is {@code status = 'APPROVED'}, {@code unconfirmed} is
 * everything else that remains. Age is whole years on the case's own
 * {@code created_at}; a missing patient, a missing birth date or a birth date
 * after the case count as unknown, so the status, gender and age groupings
 * each add up to {@code totalCases}.
 * <p>
 * The diagnosis filter matches {@code final_icd10_code} only (no fallback to
 * the initial code) with brackets/case ignored, like every APPROVED-count
 * report.
 */
@Repository
@RequiredArgsConstructor
public class MonitoringQueryRepository {

    private static final int MAX_ICD10_HIERARCHY_DEPTH = 32;

    private static final String REGION_LOCATIONS_SQL = """
            select r.code as location_code from ref_region r where r.deleted = false
            """;

    private static final String DISTRICT_LOCATIONS_SQL = """
            select d.code as location_code from ref_district d
            where d.deleted = false and d.parent_code = (:regionCode)::text
            """;

    private static final String REGION_FILTER_SQL = """
              and o.region_code = (:regionCode)::text
            """;

    private static final String DIAGNOSIS_FILTER_SQL = """
              and upper(trim(both '()' from trim(f.final_icd10_code))) = (:diagnosisCode)::text
            """;

    private static final String LOCATION_STATISTICS_SQL = """
            with base_locations as (
                %1$s
            ),
            filtered_forms as (
                select f.id as form_id,
                       %2$s as location_code,
                       f.status as form_status,
                       case upper(trim(p.gender_code))
                           when 'MALE' then 'MALE'
                           when 'FEMALE' then 'FEMALE'
                           else 'UNKNOWN'
                       end as gender,
                       case
                           when p.birth_date is null then null
                           when p.birth_date > f.created_at::date then null
                           else extract(year from age(f.created_at::date, p.birth_date))::int
                       end as age_years
                from form058 f
                join organization o on o.id = f.sender_organization_id
                left join patient p on p.id = f.patient_id
                where f.deleted = false
                  and f.status <> 'CANCELED'
                  and f.created_at >= (:fromInclusive)::timestamptz
                  and f.created_at < (:toExclusive)::timestamptz
                %3$s
                %4$s
            )
            select b.location_code,
                   count(c.form_id) as total_cases,
                   count(c.form_id) filter (where c.form_status = 'APPROVED') as confirmed,
                   count(c.form_id) filter (where c.form_status <> 'APPROVED') as unconfirmed,
                   count(c.form_id) filter (where c.gender = 'MALE') as male,
                   count(c.form_id) filter (where c.gender = 'FEMALE') as female,
                   count(c.form_id) filter (where c.gender = 'UNKNOWN') as unknown_gender,
                   count(c.form_id) filter (where c.age_years >= 0 and c.age_years < 4) as age_0_3,
                   count(c.form_id) filter (where c.age_years >= 4 and c.age_years < 7) as age_4_6,
                   count(c.form_id) filter (where c.age_years >= 7 and c.age_years < 11) as age_7_10,
                   count(c.form_id) filter (where c.age_years >= 11 and c.age_years < 18) as age_11_17,
                   count(c.form_id) filter (where c.age_years >= 18 and c.age_years < 26) as age_18_25,
                   count(c.form_id) filter (where c.age_years >= 26 and c.age_years < 46) as age_26_45,
                   count(c.form_id) filter (where c.age_years >= 46 and c.age_years < 66) as age_46_65,
                   count(c.form_id) filter (where c.age_years >= 66) as age_66_plus,
                   count(c.form_id) filter (where c.form_id is not null and c.age_years is null) as unknown_age
            from base_locations b
            left join filtered_forms c on c.location_code = b.location_code
            group by b.location_code
            order by b.location_code
            """;

    /**
     * Every case is counted under exactly one disease: its {@code
     * final_icd10_code} is resolved to the nearest ancestor ICD-10 block
     * ({@code (A00-A09)} shape; class-level {@code (A00-B99)} included), else
     * to the topmost ancestor, else {@code UNCLASSIFIED} (null / empty /
     * {@code STRING} / unknown code).
     */
    private static final String DISEASE_STATISTICS_SQL = """
            with recursive normalized_forms as (
                select %1$s as location_code,
                       case
                           when f.final_icd10_code is null then null
                           when upper(trim(both '()' from trim(f.final_icd10_code))) in ('', 'STRING') then null
                           else upper(trim(both '()' from trim(f.final_icd10_code)))
                       end as source_code
                from form058 f
                join organization o on o.id = f.sender_organization_id
                where f.deleted = false
                  and f.status <> 'CANCELED'
                  and f.created_at >= (:fromInclusive)::timestamptz
                  and f.created_at < (:toExclusive)::timestamptz
                %2$s
                %3$s
            ),
            filtered_codes as (
                select n.location_code, n.source_code, count(*) as cases_count
                from normalized_forms n
                group by n.location_code, n.source_code
            ),
            icd10_hierarchy as (
                select fc.location_code, fc.source_code, fc.cases_count,
                       i.id, i.parent_id,
                       upper(trim(both '()' from trim(i.code))) as current_code,
                       0 as hierarchy_depth,
                       array[i.id]::bigint[] as visited_ids
                from filtered_codes fc
                join ref_icd10 i on upper(trim(both '()' from trim(i.code))) = fc.source_code
                where fc.source_code is not null

                union all

                select h.location_code, h.source_code, h.cases_count,
                       parent.id, parent.parent_id,
                       upper(trim(both '()' from trim(parent.code))),
                       h.hierarchy_depth + 1,
                       h.visited_ids || parent.id
                from icd10_hierarchy h
                join ref_icd10 parent on parent.id = h.parent_id
                where h.hierarchy_depth < (:maxHierarchyDepth)::int
                  and not parent.id = any(h.visited_ids)
            ),
            ranked_candidates as (
                select h.location_code, h.source_code, h.current_code,
                       row_number() over (
                           partition by h.location_code, h.source_code
                           order by
                               case when h.current_code ~ '^[A-Z][0-9]{2}-[A-Z][0-9]{2}$' then 0 else 1 end,
                               case when h.current_code ~ '^[A-Z][0-9]{2}-[A-Z][0-9]{2}$'
                                    then h.hierarchy_depth else null end asc nulls last,
                               h.hierarchy_depth desc
                       ) as candidate_rank
                from icd10_hierarchy h
            ),
            resolved_codes as (
                select fc.location_code,
                       coalesce(rc.current_code, 'UNCLASSIFIED') as disease_code,
                       fc.cases_count
                from filtered_codes fc
                left join ranked_candidates rc
                       on rc.location_code is not distinct from fc.location_code
                      and rc.source_code is not distinct from fc.source_code
                      and rc.candidate_rank = 1
            )
            select r.location_code, r.disease_code, sum(r.cases_count) as cases_count
            from resolved_codes r
            group by r.location_code, r.disease_code
            order by r.location_code, r.disease_code
            """;

    private static final String DISTRICT_FILTER_SQL = """
              and o.district_code = (:districtCode)::text
            """;

    /**
     * Dynamics: one row per (period, location, disease block) over a full
     * generated series, so periods without cases are present too (all zero).
     * Cases are bucketed on their local {@code Asia/Tashkent} time. The
     * disease is the case's final ICD-10 code resolved - once per distinct
     * code, not per case - to its nearest ancestor-or-self block (a code
     * containing {@code -}), else to the code itself; no usable final code
     * leaves it {@code null} (the case still counts in the totals).
     */
    private static final String DYNAMICS_SQL = """
            with recursive periods as (
                select to_char(gs, '%1$s') as period
                from generate_series((:seriesStart)::timestamp, (:seriesEnd)::timestamp, interval '%2$s') gs
            ),
            raw_data as (
                select %3$s as period,
                       %4$s as location_code,
                       f.status as form_status,
                       upper(trim(p.gender_code)) as gender,
                       case
                           when p.birth_date is null then null
                           when p.birth_date > f.created_at::date then null
                           else extract(year from age(f.created_at::date, p.birth_date))::int
                       end as age_years,
                       case
                           when f.final_icd10_code is null then null
                           when upper(trim(both '()' from trim(f.final_icd10_code))) in ('', 'STRING') then null
                           else upper(trim(both '()' from trim(f.final_icd10_code)))
                       end as source_code
                from form058 f
                join organization o on o.id = f.sender_organization_id
                left join patient p on p.id = f.patient_id
                where f.deleted = false
                  and f.status <> 'CANCELED'
                  and f.created_at >= (:fromInclusive)::timestamptz
                  and f.created_at < (:toExclusive)::timestamptz
                %5$s
            ),
            aggregated as (
                select r.period, r.location_code, r.source_code,
                       count(*) filter (where r.form_status = 'APPROVED') as confirmed,
                       count(*) filter (where r.form_status <> 'APPROVED') as unconfirmed,
                       count(*) filter (where r.gender = 'MALE') as male,
                       count(*) filter (where r.gender = 'FEMALE') as female,
                       count(*) filter (where r.age_years >= 0 and r.age_years < 4) as age_0_3,
                       count(*) filter (where r.age_years >= 4 and r.age_years < 7) as age_3_6,
                       count(*) filter (where r.age_years >= 7 and r.age_years < 11) as age_6_10,
                       count(*) filter (where r.age_years >= 11 and r.age_years < 18) as age_10_17,
                       count(*) filter (where r.age_years >= 18 and r.age_years < 26) as age_18_25,
                       count(*) filter (where r.age_years >= 26 and r.age_years < 46) as age_25_45,
                       count(*) filter (where r.age_years >= 46 and r.age_years < 66) as age_45_65,
                       count(*) filter (where r.age_years >= 66) as age_65_plus
                from raw_data r
                group by r.period, r.location_code, r.source_code
            ),
            distinct_codes as (
                select distinct a.source_code from aggregated a where a.source_code is not null
            ),
            icd10_hierarchy as (
                select dc.source_code, i.parent_id,
                       upper(trim(both '()' from trim(i.code))) as current_code,
                       0 as hierarchy_depth,
                       array[i.id]::bigint[] as visited_ids
                from distinct_codes dc
                join ref_icd10 i on upper(trim(both '()' from trim(i.code))) = dc.source_code

                union all

                select h.source_code, parent.parent_id,
                       upper(trim(both '()' from trim(parent.code))),
                       h.hierarchy_depth + 1,
                       h.visited_ids || parent.id
                from icd10_hierarchy h
                join ref_icd10 parent on parent.id = h.parent_id
                where h.hierarchy_depth < (:maxHierarchyDepth)::int
                  and not parent.id = any(h.visited_ids)
            ),
            resolved_blocks as (
                select distinct on (h.source_code) h.source_code, h.current_code as block_code
                from icd10_hierarchy h
                where position('-' in h.current_code) > 0
                order by h.source_code, h.hierarchy_depth
            )
            select p.period,
                   %6$s as location_code,
                   case when a.source_code is null then null
                        else coalesce(rb.block_code, a.source_code) end as disease_code,
                   coalesce(a.confirmed, 0), coalesce(a.unconfirmed, 0),
                   coalesce(a.male, 0), coalesce(a.female, 0),
                   coalesce(a.age_0_3, 0), coalesce(a.age_3_6, 0), coalesce(a.age_6_10, 0),
                   coalesce(a.age_10_17, 0), coalesce(a.age_18_25, 0), coalesce(a.age_25_45, 0),
                   coalesce(a.age_45_65, 0), coalesce(a.age_65_plus, 0)
            from periods p
            left join aggregated a on a.period = p.period
            left join resolved_blocks rb on rb.source_code = a.source_code
            order by p.period, 2, 3
            """;

    private static final String LOCAL_CREATED_AT = "(f.created_at at time zone 'Asia/Tashkent')";

    private final EntityManager entityManager;

    public List<MonitoringDynamicsRow> findDynamics(MonitoringDynamicsQuery query) {
        MonitoringDynamicsWindow window = query.window();
        MonitoringGranularity granularity = window.granularity();

        String filters = (query.hasRegion() ? REGION_FILTER_SQL : "")
                + (query.hasDistrict() ? DISTRICT_FILTER_SQL : "")
                + (query.hasDiagnosis() ? DIAGNOSIS_FILTER_SQL : "");
        String fallback = query.fallbackLocation();

        String sql = DYNAMICS_SQL.formatted(
                granularity.labelFormat(),
                granularity.step(),
                granularity.bucketSql(LOCAL_CREATED_AT, "(:weekAnchor)::date"),
                query.hasDistrict() ? "o.district_code" : "o.region_code",
                filters,
                fallback != null
                        ? "coalesce(a.location_code, (:fallbackLocation)::text)"
                        : "a.location_code"
        );

        Query nativeQuery = entityManager.createNativeQuery(sql)
                .setParameter("seriesStart", window.seriesStart())
                .setParameter("seriesEnd", window.seriesEnd())
                .setParameter("fromInclusive", window.fromInclusive())
                .setParameter("toExclusive", window.toExclusive())
                .setParameter("maxHierarchyDepth", MAX_ICD10_HIERARCHY_DEPTH);
        if (granularity.usesWeekAnchor()) {
            nativeQuery.setParameter("weekAnchor", window.weekAnchor());
        }
        if (query.hasRegion()) {
            nativeQuery.setParameter("regionCode", query.regionCode());
        }
        if (query.hasDistrict()) {
            nativeQuery.setParameter("districtCode", query.districtCode());
        }
        if (query.hasDiagnosis()) {
            nativeQuery.setParameter("diagnosisCode", query.diagnosisCode());
        }
        if (fallback != null) {
            nativeQuery.setParameter("fallbackLocation", fallback);
        }

        List<?> rows = nativeQuery.getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new MonitoringDynamicsRow(
                            (String) r[0], (String) r[1], (String) r[2],
                            count(r[3]), count(r[4]), count(r[5]), count(r[6]),
                            count(r[7]), count(r[8]), count(r[9]), count(r[10]),
                            count(r[11]), count(r[12]), count(r[13]), count(r[14])
                    );
                })
                .toList();
    }

    public List<MonitoringLocationRow> findLocationStatistics(MonitoringFilter filter) {
        String sql = LOCATION_STATISTICS_SQL.formatted(
                filter.hasRegion() ? DISTRICT_LOCATIONS_SQL : REGION_LOCATIONS_SQL,
                locationColumn(filter),
                filter.hasRegion() ? REGION_FILTER_SQL : "",
                filter.hasDiagnosis() ? DIAGNOSIS_FILTER_SQL : ""
        );

        List<?> rows = bind(entityManager.createNativeQuery(sql), filter).getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new MonitoringLocationRow(
                            (String) r[0],
                            count(r[1]), count(r[2]), count(r[3]),
                            count(r[4]), count(r[5]), count(r[6]),
                            count(r[7]), count(r[8]), count(r[9]), count(r[10]),
                            count(r[11]), count(r[12]), count(r[13]), count(r[14]),
                            count(r[15])
                    );
                })
                .toList();
    }

    public List<MonitoringDiseaseRow> findDiseaseStatistics(MonitoringFilter filter) {
        String sql = DISEASE_STATISTICS_SQL.formatted(
                locationColumn(filter),
                filter.hasRegion() ? REGION_FILTER_SQL : "",
                filter.hasDiagnosis() ? DIAGNOSIS_FILTER_SQL : ""
        );

        Query query = bind(entityManager.createNativeQuery(sql), filter)
                .setParameter("maxHierarchyDepth", MAX_ICD10_HIERARCHY_DEPTH);
        List<?> rows = query.getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new MonitoringDiseaseRow((String) r[0], (String) r[1], count(r[2]));
                })
                .toList();
    }

    private String locationColumn(MonitoringFilter filter) {
        return filter.hasRegion() ? "o.district_code" : "o.region_code";
    }

    private Query bind(Query query, MonitoringFilter filter) {
        query.setParameter("fromInclusive", filter.fromInclusive())
                .setParameter("toExclusive", filter.toExclusive());
        if (filter.hasRegion()) {
            query.setParameter("regionCode", filter.regionCode());
        }
        if (filter.hasDiagnosis()) {
            query.setParameter("diagnosisCode", filter.diagnosisCode());
        }
        return query;
    }

    private long count(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }
}
