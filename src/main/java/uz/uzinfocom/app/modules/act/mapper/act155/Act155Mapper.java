package uz.uzinfocom.app.modules.act.mapper.act155;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.act.domain.model.act155.Act155;
import uz.uzinfocom.app.modules.act.domain.model.act155.Act155Detail;
import uz.uzinfocom.app.modules.act.mapper.ActEmbeddedMapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act155Request;
import uz.uzinfocom.app.modules.act.web.dto.request.act155.Act155SampleRequest;

@Mapper(componentModel = "spring", uses = ActEmbeddedMapper.class)
public interface Act155Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act155", ignore = true)
    Act155Detail toEntity(Act155SampleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act155", ignore = true)
    void update(@MappingTarget Act155Detail entity, Act155SampleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdOrgUuid", ignore = true)
    @Mapping(target = "createdOrg", ignore = true)
    @Mapping(target = "updatedOrgUuid", ignore = true)
    @Mapping(target = "updatedOrg", ignore = true)
    @Mapping(target = "actType", ignore = true)
    @Mapping(target = "actStatus", ignore = true)
    @Mapping(target = "lisInfo", ignore = true)
    @Mapping(target = "card", ignore = true)
    @Mapping(target = "assignedById", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "resultComment", ignore = true)
    @Mapping(target = "act155Details", ignore = true)
    void copyOwnFields(@MappingTarget Act155 target, Act155Request request);
}
