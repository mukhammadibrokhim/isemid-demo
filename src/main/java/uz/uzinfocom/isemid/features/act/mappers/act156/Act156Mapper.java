package uz.uzinfocom.isemid.features.act.mappers.act156;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act156.Act156Request;
import uz.uzinfocom.isemid.features.act.dtos.responses.act156.Act156Response;
import uz.uzinfocom.isemid.features.act.models.act156.Act156;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface Act156Mapper {

    @Mapping(target = "id", source = "act.id")
    @Mapping(target = "uuid", source = "act.uuid")
    @Mapping(target = "createdAt", source = "act.createdAt")
    @Mapping(target = "updatedAt", source = "act.updatedAt")
    @Mapping(target = "type", source = "act.type")
    @Mapping(target = "status", source = "act.status")
    Act156Response toResponse(Act156 act);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act156 entity, Act156Request dto);
}
