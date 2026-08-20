package uz.uzinfocom.app.platform.devpanel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.devpanel.domain.DevPosition;

public interface DevPositionRepository extends JpaRepository<DevPosition, Long>, JpaSpecificationExecutor<DevPosition> {

    boolean existsByName(String name);
}
