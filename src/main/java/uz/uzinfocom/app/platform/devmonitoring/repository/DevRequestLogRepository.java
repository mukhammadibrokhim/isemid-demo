package uz.uzinfocom.app.platform.devmonitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevRequestLog;

public interface DevRequestLogRepository
        extends JpaRepository<DevRequestLog, Long>, JpaSpecificationExecutor<DevRequestLog> {
}
