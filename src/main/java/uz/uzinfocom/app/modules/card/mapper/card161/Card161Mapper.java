package uz.uzinfocom.app.modules.card.mapper.card161;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card161DetailResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.Card161RiskFactorResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.ContactPersonResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.EnvironmentalLabTestResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.EnvironmentalSourceResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.HomePreventiveMeasureResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.InfectionSourceDetailResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.InfectionSourceResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.OutbreakDisinfectionMeasureResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.ScreenedGroupResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.VaccinationResponse;
import uz.uzinfocom.app.modules.card.domain.model.card161.Card161;
import uz.uzinfocom.app.modules.card.domain.model.card161.Card161RiskFactor;
import uz.uzinfocom.app.modules.card.domain.model.card161.ContactPerson;
import uz.uzinfocom.app.modules.card.domain.model.card161.EnvironmentalLabTest;
import uz.uzinfocom.app.modules.card.domain.model.card161.EnvironmentalSource;
import uz.uzinfocom.app.modules.card.domain.model.card161.HomePreventiveMeasure;
import uz.uzinfocom.app.modules.card.domain.model.card161.InfectionSource;
import uz.uzinfocom.app.modules.card.domain.model.card161.InfectionSourceDetail;
import uz.uzinfocom.app.modules.card.domain.model.card161.OutbreakDisinfectionMeasure;
import uz.uzinfocom.app.modules.card.domain.model.card161.ScreenedGroup;
import uz.uzinfocom.app.modules.card.domain.model.card161.Vaccination;
import uz.uzinfocom.app.modules.card.web.dto.request.Card161Request;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.Card161RiskFactorRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.ContactPersonRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EnvironmentalLabTestRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EnvironmentalSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.HomePreventiveMeasureRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.InfectionSourceDetailRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.InfectionSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.OutbreakDisinfectionMeasureRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.ScreenedGroupRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.VaccinationRequest;

/**
 * Field-level mapping only. Wiring a child's back-reference to its parent
 * (e.g. {@code Card161RiskFactor.card161}) is the handler's job — a mapper
 * invoked on a single child in isolation has no parent to wire it to.
 */
@Mapper(componentModel = "spring")
public interface Card161Mapper {

    @Mapping(target = "formId", source = "form058.id")
    @Mapping(target = "type", source = "cardType")
    Card161DetailResponse toResponse(Card161 card161);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    Card161RiskFactor toEntity(Card161RiskFactorRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    void update(@MappingTarget Card161RiskFactor entity, Card161RiskFactorRequest request);

    Card161RiskFactorResponse toResponse(Card161RiskFactor entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    ContactPerson toEntity(ContactPersonRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    void update(@MappingTarget ContactPerson entity, ContactPersonRequest request);

    ContactPersonResponse toResponse(ContactPerson entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    EnvironmentalLabTest toEntity(EnvironmentalLabTestRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    void update(@MappingTarget EnvironmentalLabTest entity, EnvironmentalLabTestRequest request);

    EnvironmentalLabTestResponse toResponse(EnvironmentalLabTest entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    EnvironmentalSource toEntity(EnvironmentalSourceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    void update(@MappingTarget EnvironmentalSource entity, EnvironmentalSourceRequest request);

    EnvironmentalSourceResponse toResponse(EnvironmentalSource entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    HomePreventiveMeasure toEntity(HomePreventiveMeasureRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    void update(@MappingTarget HomePreventiveMeasure entity, HomePreventiveMeasureRequest request);

    HomePreventiveMeasureResponse toResponse(HomePreventiveMeasure entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    InfectionSource toEntity(InfectionSourceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    void update(@MappingTarget InfectionSource entity, InfectionSourceRequest request);

    InfectionSourceResponse toResponse(InfectionSource entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    InfectionSourceDetail toEntity(InfectionSourceDetailRequest request);

    InfectionSourceDetailResponse toResponse(InfectionSourceDetail entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    OutbreakDisinfectionMeasure toEntity(OutbreakDisinfectionMeasureRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    void update(@MappingTarget OutbreakDisinfectionMeasure entity, OutbreakDisinfectionMeasureRequest request);

    OutbreakDisinfectionMeasureResponse toResponse(OutbreakDisinfectionMeasure entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    ScreenedGroup toEntity(ScreenedGroupRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    void update(@MappingTarget ScreenedGroup entity, ScreenedGroupRequest request);

    ScreenedGroupResponse toResponse(ScreenedGroup entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    Vaccination toEntity(VaccinationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card161", ignore = true)
    void update(@MappingTarget Vaccination entity, VaccinationRequest request);

    VaccinationResponse toResponse(Vaccination entity);

    /**
     * Copies only Card161's own scalar fields — child collections, the
     * polyclinic relation, and every Card-level field (status, form058,
     * audit, ...) are wired by {@code Card161Handler}, not here.
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
    @Mapping(target = "polyclinic", ignore = true)
    @Mapping(target = "polyclinicId", ignore = true)
    @Mapping(target = "vaccinations", ignore = true)
    @Mapping(target = "riskFactors", ignore = true)
    @Mapping(target = "possibleInfectionSources", ignore = true)
    @Mapping(target = "environmentalSources", ignore = true)
    @Mapping(target = "environmentalLabTests", ignore = true)
    @Mapping(target = "contactPersonDetails", ignore = true)
    @Mapping(target = "screenedGroups", ignore = true)
    @Mapping(target = "homePreventiveMeasures", ignore = true)
    @Mapping(target = "outbreakDisinfectionMeasures", ignore = true)
    @Mapping(target = "infectionSourceDetail", ignore = true)
    void copyOwnFields(@MappingTarget Card161 target, Card161Request request);
}
