package uz.uzinfocom.app.modules.act.mapper.act153;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153Detail;
import uz.uzinfocom.app.modules.act.mapper.ActEmbeddedMapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act153Request;
import uz.uzinfocom.app.modules.act.web.dto.request.act153.Act153SampleRequest;

/**
 * Field-level mapping only. Wiring a sample's back-reference to its parent
 * ({@code Act153Detail.act153}) is the handler's job.
 */
@Mapper(componentModel = "spring", uses = ActEmbeddedMapper.class)
public interface Act153Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act153", ignore = true)
    Act153Detail toEntity(Act153SampleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act153", ignore = true)
    void update(@MappingTarget Act153Detail entity, Act153SampleRequest request);

    /**
     * Copies only Act153's own fields â€” {@code samples} and every
     * Act-level field (status, card, users, ...) are wired by
     * {@code Act153Handler}, not here.
     */
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
    @Mapping(target = "act153Details", ignore = true)
    void copyOwnFields(@MappingTarget Act153 target, Act153Request request);
}
