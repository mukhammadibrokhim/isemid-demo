package uz.uzinfocom.app.platform.settings.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingFilterRequest;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingResponse;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingTableResponse;
import uz.uzinfocom.app.platform.settings.application.query.mapper.SystemSettingMapper;
import uz.uzinfocom.app.platform.settings.application.query.projection.SystemSettingTableProjection;
import uz.uzinfocom.app.platform.settings.application.query.specification.SystemSettingSpecification;
import uz.uzinfocom.app.platform.settings.domain.SystemSetting;
import uz.uzinfocom.app.platform.settings.repository.SystemSettingRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SystemSettingQueryService {

    private final SystemSettingRepository systemSettingRepository;
    private final SystemSettingMapper systemSettingMapper;
    private final AuditResolver auditResolver;

    @Transactional(readOnly = true)
    public Page<SystemSettingTableResponse> findTable(SystemSettingFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, SystemSettingSortFields.ALLOWED);

        Page<SystemSettingTableProjection> page = Objects.requireNonNull(
                systemSettingRepository.findBy(
                        SystemSettingSpecification.byFilter(request),
                        query -> query
                                .as(SystemSettingTableProjection.class)
                                .page(pageable)
                ),
                "System setting page returned null"
        );

        return page.map(systemSettingMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public SystemSettingResponse getById(Long id) {
        SystemSetting systemSetting = systemSettingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("settings.not-found", id));

        return systemSettingMapper.toResponse(systemSetting, auditResolver.resolve(systemSetting));
    }

    @Transactional(readOnly = true)
    public SystemSettingResponse getByKey(String settingKey) {
        SystemSetting systemSetting = systemSettingRepository.findBySettingKeyAndDeletedFalse(settingKey)
                .orElseThrow(() -> new NotFoundException("settings.not-found-by-key", settingKey));

        return systemSettingMapper.toResponse(systemSetting, auditResolver.resolve(systemSetting));
    }
}
