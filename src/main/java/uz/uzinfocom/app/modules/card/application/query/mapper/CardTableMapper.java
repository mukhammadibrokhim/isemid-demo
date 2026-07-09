package uz.uzinfocom.app.modules.card.application.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.modules.card.application.query.projection.CardTableProjection;

@Mapper(componentModel = "spring")
public interface CardTableMapper {

    CardTableResponse toTableResponse(CardTableProjection projection);
}
