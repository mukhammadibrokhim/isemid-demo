package uz.uzinfocom.app.platform.settings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.settings.domain.SystemSetting;

import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long>, JpaSpecificationExecutor<SystemSetting> {

    Optional<SystemSetting> findByIdAndDeletedFalse(Long id);

    Optional<SystemSetting> findBySettingKeyAndDeletedFalse(String settingKey);

    boolean existsBySettingKeyIgnoreCase(String settingKey);
}
