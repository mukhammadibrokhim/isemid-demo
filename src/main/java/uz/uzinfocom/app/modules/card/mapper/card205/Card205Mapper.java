package uz.uzinfocom.app.modules.card.mapper.card205;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card205DetailResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card205.InformationAboutAnimaBittenPeopleResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card205.InformationOtherBittenAnimalsResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card205.InformationOtherBittenPeopleResponse;
import uz.uzinfocom.app.modules.card.domain.model.card205.Card205;
import uz.uzinfocom.app.modules.card.domain.model.card205.InformationAboutAnimaBittenPeople;
import uz.uzinfocom.app.modules.card.domain.model.card205.InformationOtherBittenAnimals;
import uz.uzinfocom.app.modules.card.domain.model.card205.InformationOtherBittenPeople;
import uz.uzinfocom.app.modules.card.web.dto.request.Card205Request;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationAboutAnimaBittenPeopleRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationOtherBittenAnimalsRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationOtherBittenPeopleRequest;

/**
 * Field-level mapping only. Wiring a child's back-reference to its parent
 * is the handler's job.
 */
@Mapper(componentModel = "spring")
public interface Card205Mapper {

    @Mapping(target = "formId", source = "form058.id")
    @Mapping(target = "type", source = "cardType")
    Card205DetailResponse toResponse(Card205 card205);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card205", ignore = true)
    InformationOtherBittenPeople toEntity(InformationOtherBittenPeopleRequest request);

    InformationOtherBittenPeopleResponse toResponse(InformationOtherBittenPeople entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card205", ignore = true)
    InformationOtherBittenAnimals toEntity(InformationOtherBittenAnimalsRequest request);

    InformationOtherBittenAnimalsResponse toResponse(InformationOtherBittenAnimals entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card205", ignore = true)
    InformationAboutAnimaBittenPeople toEntity(InformationAboutAnimaBittenPeopleRequest request);

    InformationAboutAnimaBittenPeopleResponse toResponse(InformationAboutAnimaBittenPeople entity);

    /**
     * Copies only Card205's own scalar fields — child collections and every
     * Card-level field (status, form058, audit, ...) are wired by
     * {@code Card205Handler}, not here.
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
    @Mapping(target = "infoBittenPeople", ignore = true)
    @Mapping(target = "infoOtherBittenAnimal", ignore = true)
    @Mapping(target = "infoAbtAnimalBittenPeople", ignore = true)
    void copyOwnFields(@MappingTarget Card205 target, Card205Request request);
}
