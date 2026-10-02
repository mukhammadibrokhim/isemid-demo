package uz.uzinfocom.app.modules.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.modules.iam.domain.Permission;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    Optional<Permission> findByIdAndDeletedFalse(Long id);

    boolean existsBySubjectIgnoreCase(String subject);

    boolean existsBySubjectIgnoreCaseAndIdNot(String subject, Long id);

}
