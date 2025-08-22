package uz.uzinfocom.isemid.features.act.mappers.act154;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act154.Act154DetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.responses.act154.Act154DetailResponse;
import uz.uzinfocom.isemid.features.act.models.act154.Act154Detail;

@Mapper(componentModel = "spring")
public interface Act154DetailMapper {
    @Mapping(target = "id", source = "id")
    Act154Detail toEntity(Act154DetailRequest request);

    Act154DetailResponse toResponse(Act154Detail entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", source = "id")
    void update(@MappingTarget Act154Detail entity, Act154DetailRequest request);
}
