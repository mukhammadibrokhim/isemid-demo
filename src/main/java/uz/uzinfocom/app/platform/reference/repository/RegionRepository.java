package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.uzinfocom.app.platform.reference.domain.Region;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByIdAndDeletedFalse(Long id);

    Optional<Region> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Region> findAllByDeletedFalseOrderBySortOrderAscNameUzAsc();

    List<Region> findAllByParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(String parentCode);
}
