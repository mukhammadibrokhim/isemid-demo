package uz.uzinfocom.app.modules.card.mapper.card174;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card174DetailResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card174.InfectionMonitoringResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card174.OutbreakControlMeasureResponse;
import uz.uzinfocom.app.modules.card.domain.model.card174.Card174;
import uz.uzinfocom.app.modules.card.domain.model.card174.InfectionMonitoring;
import uz.uzinfocom.app.modules.card.domain.model.card174.OutbreakControlMeasure;
import uz.uzinfocom.app.modules.card.web.dto.request.Card174Request;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.InfectionMonitoringRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.OutbreakControlMeasureRequest;

/**
 * Field-level mapping only. Wiring a child's back-reference to its parent
 * (e.g. {@code InfectionMonitoring.card174}) is the handler's job.
 */
@Mapper(componentModel = "spring")
public interface Card174Mapper {

    @Mapping(target = "formId", source = "form058.id")
    @Mapping(target = "type", source = "cardType")
    Card174DetailResponse toResponse(Card174 card174);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card174", ignore = true)
    InfectionMonitoring toEntity(InfectionMonitoringRequest request);

    InfectionMonitoringResponse toResponse(InfectionMonitoring entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card174", ignore = true)
    OutbreakControlMeasure toEntity(OutbreakControlMeasureRequest request);

    OutbreakControlMeasureResponse toResponse(OutbreakControlMeasure entity);

    /**
     * Copies only Card174's own scalar fields — child collections and every
     * Card-level field (status, form058, audit, ...) are wired by
     * {@code Card174Handler}, not here.
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
    @Mapping(target = "infectionMonitoring", ignore = true)
    @Mapping(target = "outbreakControlMeasures", ignore = true)
    void copyOwnFields(@MappingTarget Card174 target, Card174Request request);
}
