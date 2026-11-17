package uz.uzinfocom.app.modules.act.mapper.act154;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154Detail;
import uz.uzinfocom.app.modules.act.mapper.ActEmbeddedMapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act154Request;
import uz.uzinfocom.app.modules.act.web.dto.request.act154.Act154SampleRequest;

@Mapper(componentModel = "spring", uses = ActEmbeddedMapper.class)
public interface Act154Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act154", ignore = true)
    @Mapping(target = "sampleName", ignore = true) // not on the form; legacy-migrated data only
    Act154Detail toEntity(Act154SampleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act154", ignore = true)
    @Mapping(target = "sampleName", ignore = true)
    void update(@MappingTarget Act154Detail entity, Act154SampleRequest request);

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
    @Mapping(target = "actNumber", ignore = true) // generated from id on assign
    @Mapping(target = "activityTypeCode", ignore = true) // validated against ref_catalog by the handler
    // Not on the act154 form; kept on the entity only for legacy-migrated data.
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "documentConfirmSampling", ignore = true)
    @Mapping(target = "lisOrganizationId", ignore = true)
    @Mapping(target = "laboratoryAddress", ignore = true)
    @Mapping(target = "purpose", ignore = true) // resolved from purposeCode by the handler
    @Mapping(target = "samplingBasisCode", ignore = true) // validated against ref_catalog by the handler
    @Mapping(target = "act154Details", ignore = true)
    void copyOwnFields(@MappingTarget Act154 target, Act154Request request);
}
