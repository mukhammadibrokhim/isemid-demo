package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.uzinfocom.app.platform.reference.domain.District;

import java.util.List;
import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District, Long> {

    Optional<District> findByIdAndDeletedFalse(Long id);

    Optional<District> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<District> findAllByDeletedFalseOrderBySortOrderAscNameUzAsc();

    List<District> findAllByParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(String parentCode);
}
