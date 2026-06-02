package uz.uzinfocom.app.platform.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.iam.domain.Permission;

import java.util.Collection;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    List<Permission> findAllByIdIn(Collection<Long> ids);

    boolean existsBySubjectIgnoreCase(String subject);

    boolean existsBySubjectIgnoreCaseAndIdNot(String subject, Long id);

}
