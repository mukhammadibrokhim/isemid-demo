package uz.uzinfocom.isemid.features.act.mappers.act153;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act153.Act153Request;
import uz.uzinfocom.isemid.features.act.dtos.responses.act153.Act153Response;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;

@Mapper(
        componentModel = "spring",
        uses = {Act153DetailMapper.class, OrganizationMapperHelper.class}
)
public interface Act153Mapper {

    @Mapping(target = "id", source = "act.id")
    @Mapping(target = "uuid", source = "act.uuid")
    @Mapping(target = "createdAt", source = "act.createdAt")
    @Mapping(target = "updatedAt", source = "act.updatedAt")
    @Mapping(target = "type", source = "act.type")
    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    Act153Response toResponse(Act153 act);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act153 entity, Act153Request dto);
}
