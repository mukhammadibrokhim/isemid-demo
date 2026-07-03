package uz.uzinfocom.isemid.features.forecast.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.isemid.features.forecast.models.projection.DayCountProjection;
import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface Form058ForecastRepository extends Repository<Form058, Long> {

    // createdOrg (Form058 -> AbsEntity.createdOrg, join column created_org_uuid)
    // is nullable both in the entity mapping and at the DB level (no NOT NULL
    // constraint in the changelog) — an inner join would silently drop rows
    // with no org from unfiltered (stateCode == null) counts. Left join keeps
    // them; the stateCode predicate still excludes them correctly when a
    // specific state is requested, since o.stateCode is null for those rows.
    @Query("""
            select
              cast(f.createdAt as date) as day,
              count(f) as cnt
            from Form058 f
            left join f.createdOrg o
            where f.createdAt >= :fromTs
              and f.createdAt <  :toTs
              and (cast(:stateCode as string) is null or o.stateCode = :stateCode)
              and (cast(:finalMkb10Code as string) is null or f.finalMkb10Code = :finalMkb10Code)
            group by cast(f.createdAt as date)
            order by cast(f.createdAt as date)
            """)
    List<DayCountProjection> countCreatedByDay(
            @Param("fromTs") LocalDateTime fromTs,
            @Param("toTs") LocalDateTime toTs,
            @Param("stateCode") String stateCode,
            @Param("finalMkb10Code") String finalMkb10Code
    );

    // Deliberately ignores the diagnosis filter — this answers "did this
    // org/state have any data at all" (system/reporting availability), not
    // "did this specific diagnosis ever occur". A period with zero matching
    // cases for a diagnosis is still a real observation; a period before
    // the org had any records at all is not.
    @Query("""
            select min(cast(f.createdAt as date))
            from Form058 f
            left join f.createdOrg o
            where (cast(:stateCode as string) is null or o.stateCode = :stateCode)
            """)
    LocalDate earliestRecordDate(@Param("stateCode") String stateCode);
}