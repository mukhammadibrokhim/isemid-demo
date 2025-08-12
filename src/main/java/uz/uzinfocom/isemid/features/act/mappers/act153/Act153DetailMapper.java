package uz.uzinfocom.isemid.features.act.mappers.act153;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act153.Act153DetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.responses.act153.Act153DetailResponse;
import uz.uzinfocom.isemid.features.act.models.act153.Act153Detail;

@Mapper(componentModel = "spring")
public interface Act153DetailMapper {
    @Mapping(target = "id", source = "id")
    Act153Detail toEntity(Act153DetailRequest request);

    Act153DetailResponse toResponse(Act153Detail entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", source = "id")
    void update(@MappingTarget Act153Detail entity, Act153DetailRequest request);
}
