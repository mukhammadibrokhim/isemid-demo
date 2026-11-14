package uz.uzinfocom.app.modules.airquality.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.airquality.domain.AirMeasurement;

import java.time.LocalDateTime;
import java.util.List;

public interface AirMeasurementRepository extends JpaRepository<AirMeasurement, Long> {

    boolean existsByStationIdAndMeasuredAt(Long stationId, LocalDateTime measuredAt);

    List<AirMeasurement> findAllByStationIdAndMeasuredAtOrderByParameterAsc(Long stationId, LocalDateTime measuredAt);

    @Query("""
            select m from AirMeasurement m
            where m.stationId = :stationId
              and m.measuredAt >= :from
              and m.measuredAt <= :to
            order by m.measuredAt desc, m.parameter asc
            """)
    List<AirMeasurement> findSeries(
            @Param("stationId") Long stationId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
            select m from AirMeasurement m
            where m.stationId = :stationId
              and m.parameter = :parameter
              and m.measuredAt >= :from
              and m.measuredAt <= :to
            order by m.measuredAt desc
            """)
    List<AirMeasurement> findSeriesByParameter(
            @Param("stationId") Long stationId,
            @Param("parameter") String parameter,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
