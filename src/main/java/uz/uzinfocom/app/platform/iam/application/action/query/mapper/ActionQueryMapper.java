package uz.uzinfocom.app.platform.iam.application.action.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.iam.application.action.query.dto.ActionDetailResponse;
import uz.uzinfocom.app.platform.iam.application.action.query.dto.ActionTableResponse;
import uz.uzinfocom.app.platform.iam.application.action.query.projection.ActionTableProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.iam.domain.Action;

@Mapper(componentModel = "spring")
public interface ActionQueryMapper {

    ActionTableResponse toTableResponse(ActionTableProjection actionTableProjection);

    ActionTableResponse toTableResponse(Action action);

    @Mapping(target = "audit", source = "audit")
    ActionDetailResponse toDetailResponse(Action action, AuditResponse audit);
}
