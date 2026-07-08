package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.reference.domain.Mkb10;

import java.util.List;
import java.util.Optional;

public interface Mkb10Repository extends JpaRepository<Mkb10, Long>, JpaSpecificationExecutor<Mkb10> {

    Optional<Mkb10> findByIdAndDeletedFalse(Long id);

    Optional<Mkb10> findByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Mkb10> findAllByParentIsNullAndDeletedFalseOrderByCodeAsc();

    List<Mkb10> findAllByParent_IdAndDeletedFalseOrderByCodeAsc(Long parentId);

    long countByParent_IdAndDeletedFalse(Long parentId);
}
