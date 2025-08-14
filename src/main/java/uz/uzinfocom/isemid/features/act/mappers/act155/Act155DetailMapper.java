package uz.uzinfocom.isemid.features.act.mappers.act155;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act154.Act154DetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act155.Act155DetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.responses.act155.Act155DetailResponse;
import uz.uzinfocom.isemid.features.act.models.act155.Act155Detail;

@Mapper(componentModel = "spring")
public interface Act155DetailMapper {
    @Mapping(target = "id", source = "id")
    Act155Detail toEntity(Act155DetailRequest request);

    Act155DetailResponse toResponse(Act155Detail entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", source = "id")
    void update(@MappingTarget Act155Detail entity, Act154DetailRequest request);
}
