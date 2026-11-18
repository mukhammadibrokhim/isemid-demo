package uz.uzinfocom.app.modules.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import uz.uzinfocom.app.modules.iam.domain.Permission;

import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    Optional<Permission> findByIdAndDeletedFalse(Long id);

    boolean existsBySubjectIgnoreCase(String subject);

    boolean existsBySubjectIgnoreCaseAndIdNot(String subject, Long id);

    /**
     * Every {@code PERMISSION_<SUBJECT>_<ACTION>} authority that can currently be granted:
     * active, non-deleted subject x active, non-deleted action. Used to verify that the
     * authorities the code checks actually exist in the database.
     */
    @Query(value = """
        select concat('PERMISSION_', upper(p.subject), '_', upper(a.code))
        from permission p
        cross join action a
        where p.active = true
          and p.deleted_at is null
          and a.active = true
          and a.deleted_at is null
        """, nativeQuery = true)
    Set<String> findAllGrantableAuthorityNames();

}
