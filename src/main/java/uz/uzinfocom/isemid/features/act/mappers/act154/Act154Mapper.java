package uz.uzinfocom.isemid.features.act.mappers.act154;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act154.Act154Request;
import uz.uzinfocom.isemid.features.act.dtos.responses.act154.Act154Response;
import uz.uzinfocom.isemid.features.act.mappers.GenericActMapper;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;

@Mapper(
        componentModel = "spring",
        uses = {Act154DetailMapper.class, OrganizationMapperHelper.class}
)
public interface Act154Mapper extends GenericActMapper {

    Act154 toEntity(Act154Request dto);

    @Mapping(target = "id", source = "act.id")
    @Mapping(target = "uuid", source = "act.uuid")
    @Mapping(target = "createdAt", source = "act.createdAt")
    @Mapping(target = "updatedAt", source = "act.updatedAt")
    @Mapping(target = "type", source = "act.type")
    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    Act154Response toResponse(Act154 act);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act154 entity, Act154Request dto);
}
