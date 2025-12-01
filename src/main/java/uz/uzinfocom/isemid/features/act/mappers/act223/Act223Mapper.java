package uz.uzinfocom.isemid.features.act.mappers.act223;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act223.Act223Request;
import uz.uzinfocom.isemid.features.act.dtos.responses.act223.Act223Response;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,uses = OrganizationMapperHelper.class)
public interface Act223Mapper {

    @Mapping(target = "id", source = "act.id")
    @Mapping(target = "uuid", source = "act.uuid")
    @Mapping(target = "createdAt", source = "act.createdAt")
    @Mapping(target = "updatedAt", source = "act.updatedAt")
    @Mapping(target = "type", source = "act.type")
    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    Act223Response toResponse(Act223 act);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act223 entity, Act223Request dto);
}
