package uz.uzinfocom.isemid.features.act.mappers.act224;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act224.Act224DetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.responses.act224.Act224DetailResponse;
import uz.uzinfocom.isemid.features.act.models.act224.Act224Detail;

@Mapper(componentModel = "spring")
public interface Act224DetailMapper {

    @Mapping(target = "id", source = "id")
    Act224Detail toEntity(Act224DetailRequest request);

    Act224DetailResponse toResponse(Act224Detail entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", source = "id")
    void update(@MappingTarget Act224Detail entity, Act224DetailRequest request);
}
