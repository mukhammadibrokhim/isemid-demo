package uz.uzinfocom.app.modules.card.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.modules.card.application.query.projection.CardTableProjection;

@Mapper(componentModel = "spring")
public interface CardTableMapper {

    @Mapping(target = "formId", source = "form058.id")
    CardTableResponse toTableResponse(CardTableProjection projection);
}
