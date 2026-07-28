package uz.uzinfocom.app.platform.devmonitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevUser;

import java.util.Optional;

public interface DevUserRepository extends JpaRepository<DevUser, Long> {

    Optional<DevUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
