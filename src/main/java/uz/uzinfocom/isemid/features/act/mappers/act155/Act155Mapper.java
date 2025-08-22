package uz.uzinfocom.isemid.features.act.mappers.act155;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act155.Act155Request;
import uz.uzinfocom.isemid.features.act.dtos.responses.act155.Act155Response;
import uz.uzinfocom.isemid.features.act.models.act155.Act155;


@Mapper(
        componentModel = "spring",
        uses = {Act155DetailMapper.class}
)
public interface Act155Mapper {

    Act155 toEntity(Act155Request request);

    @Mapping(target = "id", source = "act.id")
    @Mapping(target = "uuid", source = "act.uuid")
    @Mapping(target = "createdAt", source = "act.createdAt")
    @Mapping(target = "updatedAt", source = "act.updatedAt")
    @Mapping(target = "type", source = "act.type")
    Act155Response toResponse(Act155 act);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act155 entity, Act155Request dto);
}
