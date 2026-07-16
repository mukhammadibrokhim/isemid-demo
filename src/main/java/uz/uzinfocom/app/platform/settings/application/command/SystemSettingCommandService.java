package uz.uzinfocom.app.platform.settings.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.settings.application.dto.SystemSettingCreateRequest;
import uz.uzinfocom.app.platform.settings.application.dto.SystemSettingUpdateRequest;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingResponse;
import uz.uzinfocom.app.platform.settings.application.query.mapper.SystemSettingMapper;
import uz.uzinfocom.app.platform.settings.domain.SystemSetting;
import uz.uzinfocom.app.platform.settings.repository.SystemSettingRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingCommandService {

    private final SystemSettingRepository systemSettingRepository;
    private final SystemSettingMapper systemSettingMapper;
    private final AuditResolver auditResolver;

    @Transactional
    public SystemSettingResponse create(SystemSettingCreateRequest request) {
        String settingKey = request.settingKey().trim();

        if (systemSettingRepository.existsBySettingKeyIgnoreCase(settingKey)) {
            throw new ConflictException("settings.setting-key.already-exists", settingKey);
        }

        SystemSetting systemSetting = SystemSetting.builder()
                .settingKey(settingKey)
                .settingValue(request.settingValue())
                .valueType(request.valueType())
                .description(request.description())
                .active(request.active() == null || request.active())
                .deleted(false)
                .build();

        SystemSetting saved = systemSettingRepository.save(systemSetting);
        log.debug("System setting created. id={}, key={}", saved.getId(), saved.getSettingKey());

        return systemSettingMapper.toResponse(saved, auditResolver.resolve(saved));
    }

    @Transactional
    public SystemSettingResponse update(Long id, SystemSettingUpdateRequest request) {
        SystemSetting systemSetting = findEditable(id);

        systemSetting.setSettingValue(request.settingValue());
        systemSetting.setValueType(request.valueType());
        systemSetting.setDescription(request.description());
        if (request.active() != null) {
            systemSetting.setActive(request.active());
        }

        SystemSetting saved = systemSettingRepository.save(systemSetting);
        log.debug("System setting updated. id={}, key={}", saved.getId(), saved.getSettingKey());

        return systemSettingMapper.toResponse(saved, auditResolver.resolve(saved));
    }

    @Transactional
    public void delete(Long id) {
        SystemSetting systemSetting = findEditable(id);

        systemSetting.setDeleted(true);
        systemSettingRepository.save(systemSetting);

        log.debug("System setting soft-deleted. id={}, key={}", systemSetting.getId(), systemSetting.getSettingKey());
    }

    @Transactional
    public void restore(Long id) {
        SystemSetting systemSetting = systemSettingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("settings.not-found", id));

        systemSetting.setDeleted(false);
        systemSettingRepository.save(systemSetting);

        log.debug("System setting restored. id={}, key={}", systemSetting.getId(), systemSetting.getSettingKey());
    }

    private SystemSetting findEditable(Long id) {
        SystemSetting systemSetting = systemSettingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("settings.not-found", id));

        if (systemSetting.isDeleted()) {
            throw new ConflictException("settings.update.deleted-conflict", id);
        }

        return systemSetting;
    }
}
