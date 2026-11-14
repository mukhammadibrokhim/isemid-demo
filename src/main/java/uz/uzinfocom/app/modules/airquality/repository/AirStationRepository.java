package uz.uzinfocom.app.modules.airquality.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.uzinfocom.app.modules.airquality.domain.AirStation;

import java.util.Collection;
import java.util.List;

public interface AirStationRepository extends JpaRepository<AirStation, Long> {

    List<AirStation> findAllByExternalIdIn(Collection<String> externalIds);

    List<AirStation> findAllByActiveTrueOrderByIdAsc();

    /** The station set is small (dozens), so the region/district/active filters are applied in memory. */
    List<AirStation> findAllByOrderByNameAscIdAsc();
}
