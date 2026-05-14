package uz.uzinfocom.isemid.features.act.mappers.act224;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act224.Act224Request;
import uz.uzinfocom.isemid.features.act.dtos.responses.act224.Act224Response;
import uz.uzinfocom.isemid.features.act.models.act224.Act224;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {OrganizationMapperHelper.class, Act224DetailMapper.class})
public interface Act224Mapper {

    @Mapping(target = "id", source = "act.id")
    @Mapping(target = "uuid", source = "act.uuid")
    @Mapping(target = "createdAt", source = "act.createdAt")
    @Mapping(target = "updatedAt", source = "act.updatedAt")
    @Mapping(target = "type", source = "act.type")
    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "details",source = "details")
    Act224Response toResponse(Act224 act);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act224 entity, Act224Request dto);
}
