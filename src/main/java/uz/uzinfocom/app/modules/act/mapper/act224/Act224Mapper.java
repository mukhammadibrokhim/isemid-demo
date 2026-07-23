package uz.uzinfocom.app.modules.act.mapper.act224;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.act.domain.model.act224.Act224;
import uz.uzinfocom.app.modules.act.domain.model.act224.Act224Detail;
import uz.uzinfocom.app.modules.act.mapper.ActEmbeddedMapper;
import uz.uzinfocom.app.modules.act.web.dto.request.act224.Act224RecommendationRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.Act224Request;

@Mapper(componentModel = "spring", uses = ActEmbeddedMapper.class)
public interface Act224Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act224", ignore = true)
    Act224Detail toEntity(Act224RecommendationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act224", ignore = true)
    void update(@MappingTarget Act224Detail entity, Act224RecommendationRequest request);

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
    @Mapping(target = "act224Details", ignore = true)
    void copyOwnFields(@MappingTarget Act224 target, Act224Request request);
}
