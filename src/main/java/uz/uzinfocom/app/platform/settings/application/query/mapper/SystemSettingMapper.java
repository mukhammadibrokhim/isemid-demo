package uz.uzinfocom.app.platform.settings.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingResponse;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingTableResponse;
import uz.uzinfocom.app.platform.settings.application.query.projection.SystemSettingTableProjection;
import uz.uzinfocom.app.platform.settings.domain.SystemSetting;

@Mapper(componentModel = "spring")
public interface SystemSettingMapper {

    @Mapping(target = "audit", source = "audit")
    SystemSettingResponse toResponse(SystemSetting systemSetting, AuditResponse audit);

    SystemSettingTableResponse toTableResponse(SystemSettingTableProjection projection);
}
