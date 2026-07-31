package uz.uzinfocom.app.platform.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.iam.domain.Action;

import java.util.Optional;

public interface ActionRepository extends JpaRepository<Action, Long>, JpaSpecificationExecutor<Action> {

    Optional<Action> findByIdAndDeletedFalse(Long id);

    Optional<Action> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

}
