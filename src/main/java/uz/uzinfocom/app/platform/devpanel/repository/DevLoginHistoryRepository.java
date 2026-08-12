package uz.uzinfocom.app.platform.devpanel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.devpanel.domain.DevLoginHistory;

public interface DevLoginHistoryRepository
        extends JpaRepository<DevLoginHistory, Long>, JpaSpecificationExecutor<DevLoginHistory> {
}
