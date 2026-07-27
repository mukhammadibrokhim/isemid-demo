package uz.uzinfocom.isemid.features.form058.reposiotries;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.integrations.bloodbank.dtos.InfectedPatientResponse;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface Form058Repository extends JpaRepository<Form058, Long>, JpaSpecificationExecutor<Form058>, Form058RepositoryCustom {

    @Query(value = """
            with cte as (select f.created_org_uuid, p.category_code from form058 f left join patient p on f.patient_id = p.id
             where (f.status is null or f.status <> 'APPROVED') and (:mk10code is null or f.mkb10code = :mk10code) and (f.created_at between :start_date and :end_date)),
            base_name as (select code,case
                when :lan = 'uz' then name_uz
                when :lan = 'Cyrl' then name_uz_cyril
                when :lan = 'ru' then name_ru
                when :lan = 'kaa' then name_kaa
                else name_uz end as name
                from cls_catalog where type = 'city' or type = 'state'),
            state_catalog as (select code from cls_catalog
                where type = 'state'
                  and (:stateCode is null or code = :stateCode)
                  and (:accessStateCode is null or code = :accessStateCode)
                  and (:enumStateCode is null or code = :enumStateCode)),
            city_catalog as (select code, parent_code from cls_catalog
                where type = 'city'
                  and (:cityCode is null or code = :cityCode)
                  and (:accessCityCode is null or code = :accessCityCode)
                  and parent_code in (select code from state_catalog)),
            base as (select c.category_code, o.city_code from cte c left join organization o on c.created_org_uuid = o.uuid
                                                                     where (:accessLevelTypes is null or o.level_type = any(string_to_array(:accessLevelTypes, ',')))),
            city_base as (select cc.code as city_code, cc.parent_code as state_code, b.category_code, count(b.city_code) as count
                          from city_catalog cc left join base b on b.city_code = cc.code
                          group by cc.code, cc.parent_code, b.category_code)
            select sc.code as stateCode,(select bn.name from base_name bn where bn.code = sc.code) as stateName,
                   coalesce(
                           (select jsonb_agg(jsonb_build_object('city_code', cb.city_code,'count', cb.count,'city_name', (select bn.name from base_name bn where bn.code = cb.city_code),'category_code',cb.category_code)order by cb.count desc)
                            from city_base cb where cb.state_code = sc.code),'[]'::jsonb
                   )::text as cities
            from state_catalog sc
            """, nativeQuery = true)
    List<RegionCitySummaryProjection> findRegionCitySummary(
            @Param("mk10code") String mkb10Code,
            @Param("start_date") Timestamp startDate,
            @Param("end_date") Timestamp endDate,
            @Param("lan") String lang,
            @Param("stateCode") String stateCode,
            @Param("cityCode") String cityCode,
            @Param("accessStateCode") String accessStateCode,
            @Param("accessCityCode") String accessCityCode,
            @Param("accessLevelTypes") String accessLevelTypes,
            @Param("enumStateCode") String enumStateCode
    );

    interface RegionCitySummaryProjection {
        String getStateCode();

        String getStateName();

        String getCities();
    }

    @Query("SELECT f FROM Form058 f " +
            "JOIN f.patient p " +
            "JOIN p.identifiers i " +
            "WHERE i.typeCode = 'NNUZB' AND i.value = :nnuzb")
    List<Form058> findAllByNnuzbIdentifier(@Param("nnuzb") String nnuzb);

    @EntityGraph(attributePaths = {"patient.addresses"})
    List<Form058> findAll(Specification<Form058> spec);

    /**
     * method for integration with blood bank system (external).
     * The system checks for existence of donor candidate in emis system
     * @param nnuzb
     * @return
     */
    @Query(value = """
        select new uz.uzinfocom.isemid.features.integrations.bloodbank.dtos.InfectedPatientResponse(f.finalMkb10Code,
             f.finalMkb10Name, f.diagnosisDate) from Form058 f join f.patient p join p.identifiers i
                 WHERE i.typeCode = 'NNUZB' AND i.value = :nnuzb
    """)
    List<InfectedPatientResponse> getInfectedPatientResponses(@Param("nnuzb") String nnuzb);
}
