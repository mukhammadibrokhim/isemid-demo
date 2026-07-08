package uz.uzinfocom.isemid.features.forecast.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.isemid.features.forecast.models.projection.DayCountProjection;
import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.time.LocalDateTime;
import java.util.List;

public interface Form058ForecastRepository extends Repository<Form058, Long> {

    @Query("""
            select
              cast(f.createdAt as date) as day,
              count(f) as cnt
            from Form058 f
            join f.createdOrg o
            where f.createdAt >= :fromTs
              and f.createdAt <  :toTs
              and (:stateCode is null or o.stateCode = :stateCode)
              and (:finalMkb10Code is null or f.finalMkb10Code = :finalMkb10Code)
            group by cast(f.createdAt as date)
            order by cast(f.createdAt as date)
            """)
    List<DayCountProjection> countCreatedByDay(
            @Param("fromTs") LocalDateTime fromTs,
            @Param("toTs") LocalDateTime toTs,
            @Param("stateCode") String stateCode,
            @Param("finalMkb10Code") String finalMkb10Code
    );
}