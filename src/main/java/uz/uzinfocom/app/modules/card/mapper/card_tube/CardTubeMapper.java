package uz.uzinfocom.app.modules.card.mapper.card_tube;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardTubeDetailResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube.ContactMonitoringResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube.InfectionSourceResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube.TBHistoryResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube.XRayResponse;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.CardTube;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.ContactMonitoring;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.InfectionSource;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.TBHistory;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.XRay;
import uz.uzinfocom.app.modules.card.web.dto.request.CardTubeRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.ContactMonitoringRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.InfectionSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.TBHistoryRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.XRayRequest;

/**
 * Field-level mapping only. Wiring a child's back-reference to its parent
 * is the handler's job.
 */
@Mapper(componentModel = "spring")
public interface CardTubeMapper {

    @Mapping(target = "formId", source = "form058.id")
    @Mapping(target = "type", source = "cardType")
    CardTubeDetailResponse toResponse(CardTube cardTube);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardTube", ignore = true)
    XRay toEntity(XRayRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardTube", ignore = true)
    void update(@MappingTarget XRay entity, XRayRequest request);

    XRayResponse toResponse(XRay entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardTube", ignore = true)
    TBHistory toEntity(TBHistoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardTube", ignore = true)
    void update(@MappingTarget TBHistory entity, TBHistoryRequest request);

    TBHistoryResponse toResponse(TBHistory entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardTube", ignore = true)
    InfectionSource toEntity(InfectionSourceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardTube", ignore = true)
    void update(@MappingTarget InfectionSource entity, InfectionSourceRequest request);

    InfectionSourceResponse toResponse(InfectionSource entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardTube", ignore = true)
    ContactMonitoring toEntity(ContactMonitoringRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardTube", ignore = true)
    void update(@MappingTarget ContactMonitoring entity, ContactMonitoringRequest request);

    ContactMonitoringResponse toResponse(ContactMonitoring entity);

    /**
     * Copies only CardTube's own scalar fields — child collections and
     * every Card-level field (status, form058, audit, ...) are wired by
     * {@code CardTubeHandler}, not here.
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
    @Mapping(target = "cardType", ignore = true)
    @Mapping(target = "assignedById", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "form058", ignore = true)
    @Mapping(target = "supervisorComment", ignore = true)
    @Mapping(target = "attachedUserComment", ignore = true)
    @Mapping(target = "completedDate", ignore = true)
    @Mapping(target = "acts", ignore = true)
    @Mapping(target = "preMBTChestXRay", ignore = true)
    @Mapping(target = "previousTBHistory", ignore = true)
    @Mapping(target = "possibleInfectionSources", ignore = true)
    @Mapping(target = "contactMonitoringList", ignore = true)
    void copyOwnFields(@MappingTarget CardTube target, CardTubeRequest request);
}
