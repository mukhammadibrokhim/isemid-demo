package uz.uzinfocom.app.modules.act.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.ActTableResponse;
import uz.uzinfocom.app.modules.act.application.query.projection.ActTableProjection;
import uz.uzinfocom.app.modules.act.domain.model.Act;

@Mapper(componentModel = "spring")
public interface ActMapper {

    @Mapping(target = "status", source = "actStatus")
    @Mapping(target = "cardId", source = "card.id")
    ActDetailResponse toDetailResponse(Act act);

    @Mapping(target = "status", source = "actStatus")
    @Mapping(target = "cardId", source = "card.id")
    ActTableResponse toTableResponse(ActTableProjection projection);
}
