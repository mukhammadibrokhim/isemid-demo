package uz.uzinfocom.app.platform.devpanel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;

import java.util.Optional;

public interface DevUserRepository extends JpaRepository<DevUser, Long>, JpaSpecificationExecutor<DevUser> {

    Optional<DevUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPositionId(Long positionId);
}
