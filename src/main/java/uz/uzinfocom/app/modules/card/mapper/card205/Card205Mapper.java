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
    @Mapping(target = "location.region", source = "region")
    @Mapping(target = "location.district", source = "district")
    @Mapping(target = "location.neighborhood", source = "neighborhood")
    @Mapping(target = "location.street", source = "street")
    @Mapping(target = "location.houseNumber", source = "houseNumber")
    @Mapping(target = "location.apartmentNumber", source = "apartmentNumber")
    InformationOtherBittenPeople toEntity(InformationOtherBittenPeopleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card205", ignore = true)
    @Mapping(target = "location.region", source = "region")
    @Mapping(target = "location.district", source = "district")
    @Mapping(target = "location.neighborhood", source = "neighborhood")
    @Mapping(target = "location.street", source = "street")
    @Mapping(target = "location.houseNumber", source = "houseNumber")
    @Mapping(target = "location.apartmentNumber", source = "apartmentNumber")
    void update(@MappingTarget InformationOtherBittenPeople entity, InformationOtherBittenPeopleRequest request);

    @Mapping(target = "region", source = "location.region")
    @Mapping(target = "district", source = "location.district")
    @Mapping(target = "neighborhood", source = "location.neighborhood")
    @Mapping(target = "street", source = "location.street")
    @Mapping(target = "houseNumber", source = "location.houseNumber")
    @Mapping(target = "apartmentNumber", source = "location.apartmentNumber")
    InformationOtherBittenPeopleResponse toResponse(InformationOtherBittenPeople entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card205", ignore = true)
    InformationOtherBittenAnimals toEntity(InformationOtherBittenAnimalsRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card205", ignore = true)
    void update(@MappingTarget InformationOtherBittenAnimals entity, InformationOtherBittenAnimalsRequest request);

    InformationOtherBittenAnimalsResponse toResponse(InformationOtherBittenAnimals entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card205", ignore = true)
    @Mapping(target = "location.region", source = "region")
    @Mapping(target = "location.district", source = "district")
    @Mapping(target = "location.neighborhood", source = "neighborhood")
    @Mapping(target = "location.street", source = "street")
    @Mapping(target = "location.houseNumber", source = "houseNumber")
    @Mapping(target = "location.apartmentNumber", source = "apartmentNumber")
    InformationAboutAnimaBittenPeople toEntity(InformationAboutAnimaBittenPeopleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card205", ignore = true)
    @Mapping(target = "location.region", source = "region")
    @Mapping(target = "location.district", source = "district")
    @Mapping(target = "location.neighborhood", source = "neighborhood")
    @Mapping(target = "location.street", source = "street")
    @Mapping(target = "location.houseNumber", source = "houseNumber")
    @Mapping(target = "location.apartmentNumber", source = "apartmentNumber")
    void update(@MappingTarget InformationAboutAnimaBittenPeople entity, InformationAboutAnimaBittenPeopleRequest request);

    @Mapping(target = "region", source = "location.region")
    @Mapping(target = "district", source = "location.district")
    @Mapping(target = "neighborhood", source = "location.neighborhood")
    @Mapping(target = "street", source = "location.street")
    @Mapping(target = "houseNumber", source = "location.houseNumber")
    @Mapping(target = "apartmentNumber", source = "location.apartmentNumber")
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
