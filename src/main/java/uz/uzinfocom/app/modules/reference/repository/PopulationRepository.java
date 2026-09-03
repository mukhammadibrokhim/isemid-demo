package uz.uzinfocom.app.modules.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import uz.uzinfocom.app.modules.reference.domain.Population;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationGeoType;

import java.util.List;
import java.util.Optional;

public interface PopulationRepository
        extends JpaRepository<Population, Long>, JpaSpecificationExecutor<Population> {

    Optional<Population> findByIdAndDeletedFalse(Long id);

    Optional<Population> findBySoatoIdAndYearAndDeletedFalse(Integer soatoId, Integer year);

    boolean existsBySoatoIdAndYearAndDeletedFalse(Integer soatoId, Integer year);

    List<Population> findAllByDeletedFalse();

    /** Every active year for one territory — the detail view's population time series. */
    List<Population> findAllBySoatoIdAndDeletedFalseOrderByYearDesc(Integer soatoId);

    /** Republic + region rows for a year — the hierarchy root. */
    List<Population> findAllByYearAndGeoTypeInAndDeletedFalse(Integer year, List<PopulationGeoType> geoTypes);

    /** District rows under one region for a year — a hierarchy node's children. */
    List<Population> findAllByRegionCodeAndYearAndGeoTypeAndDeletedFalse(
            String regionCode, Integer year, PopulationGeoType geoType);

    @Query("select max(p.year) from Population p where p.deleted = false")
    Optional<Integer> findMaxYear();

    @Query("select distinct p.year from Population p where p.deleted = false order by p.year desc")
    List<Integer> findDistinctYears();
}
