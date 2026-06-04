package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.uzinfocom.app.platform.reference.domain.Neighborhood;

import java.util.List;
import java.util.Optional;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {

    Optional<Neighborhood> findByIdAndDeletedFalse(Long id);

    Optional<Neighborhood> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Neighborhood> findAllByDeletedFalseOrderBySortOrderAscNameUzAsc();

    List<Neighborhood> findAllByParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(String parentCode);
}
