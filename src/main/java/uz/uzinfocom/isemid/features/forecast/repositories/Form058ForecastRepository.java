package uz.uzinfocom.isemid.features.forecast.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.isemid.features.forecast.models.projection.DayCountProjection;
import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.time.LocalDateTime;
import java.util.List;

public interface Form058ForecastRepository extends Repository<Form058, Long> {

    @Query(value = """
        select
          (f.created_at::date) as day,
          count(*)::bigint as cnt
        from form058 f
        left join organization o on o.uuid = f.created_org_uuid
        where f.created_at >= :fromTs
          and f.created_at <  :toTs

          and (
               :levelType = 'REPUBLICAN'

               or (:levelType = 'REGIONAL'
                   and o.state_code = :stateCode)

               or (:levelType = 'URBAN'
                   and (
                        (:stateCode = 'UZ-TK' and o.state_code = :stateCode)
                        or (:stateCode <> 'UZ-TK' and o.city_code = :cityCode and o.level_type = 'URBAN')
                   )
               )

               or (:levelType = 'AREA'
                   and o.city_code = :cityCode
                   and o.level_type = 'AREA')

               or (:levelType in ('DISTRICT', 'INTERDISTRICT', 'NOT_DEFINED')
                   and o.city_code = :cityCode
                   and o.level_type in ('DISTRICT', 'INTERDISTRICT', 'NOT_DEFINED'))
          )

        group by (f.created_at::date)
        order by day
        """, nativeQuery = true)
    List<DayCountProjection> countCreatedByDayScoped(
            @Param("fromTs") LocalDateTime fromTs,
            @Param("toTs") LocalDateTime toTs,
            @Param("levelType") String levelType,
            @Param("stateCode") String stateCode,
            @Param("cityCode") String cityCode
    );
}
