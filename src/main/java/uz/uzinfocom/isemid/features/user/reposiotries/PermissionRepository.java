package uz.uzinfocom.isemid.features.user.reposiotries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.user.models.Permission;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findBySubject(String subject);
}
