package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.uzinfocom.app.platform.reference.domain.Mahalla;

import java.util.List;
import java.util.Optional;

public interface MahallaRepository extends JpaRepository<Mahalla, Long> {

    Optional<Mahalla> findByIdAndDeletedFalse(Long id);

    Optional<Mahalla> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Mahalla> findAllByDeletedFalseOrderBySortOrderAscNameUzAsc();

    List<Mahalla> findAllByParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(String parentCode);
}
