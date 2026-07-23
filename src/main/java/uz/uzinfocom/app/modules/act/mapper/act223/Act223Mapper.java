package uz.uzinfocom.app.modules.act.mapper.act223;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.act.domain.model.act223.Act223;
import uz.uzinfocom.app.modules.act.domain.model.act223.Act223Detail;
import uz.uzinfocom.app.modules.act.mapper.ActEmbeddedMapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act223Request;
import uz.uzinfocom.app.modules.act.web.dto.request.act223.Act223SampleRequest;

@Mapper(componentModel = "spring", uses = ActEmbeddedMapper.class)
public interface Act223Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act223", ignore = true)
    Act223Detail toEntity(Act223SampleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act223", ignore = true)
    void update(@MappingTarget Act223Detail entity, Act223SampleRequest request);

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
    @Mapping(target = "act223Details", ignore = true)
    void copyOwnFields(@MappingTarget Act223 target, Act223Request request);
}
