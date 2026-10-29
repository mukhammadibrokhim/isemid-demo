package uz.uzinfocom.app.platform.devpanel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.devpanel.domain.DevRequestLog;

public interface DevRequestLogRepository
        extends JpaRepository<DevRequestLog, Long>, JpaSpecificationExecutor<DevRequestLog> {
}
