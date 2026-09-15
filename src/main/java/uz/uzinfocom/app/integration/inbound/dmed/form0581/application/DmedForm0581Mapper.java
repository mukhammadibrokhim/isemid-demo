package uz.uzinfocom.app.integration.inbound.dmed.form0581.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.integration.inbound.common.web.IntegrationPatientRequestMapper;
import uz.uzinfocom.app.integration.inbound.dmed.form0581.web.DmedCreateForm0581Request;
import uz.uzinfocom.app.modules.form0581.application.command.OtherInjuredPersonCommand;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Command;
import uz.uzinfocom.app.modules.form0581.web.dto.request.OtherInjuredPersonRequest;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = {OrganizationMappingHelper.class, IntegrationPatientRequestMapper.class})
public interface DmedForm0581Mapper {

    @Mapping(target = "source", source = "source")
    @Mapping(target = "icd10Code", source = "request.mkb10Code")
    @Mapping(target = "icd10Name", source = "request.mkb10Name")
    @Mapping(target = "injuryLocalization", source = "request.injuryLocalization")

    @Mapping(target = "injuryDateTime", source = "request.injuryDateTime")
    @Mapping(target = "dpuVisitDateTime", source = "request.dpuVisitDateTime")
    @Mapping(target = "injuryRegionCode", source = "request.injuryRegionCode")
    @Mapping(target = "injuryDistrictCode", source = "request.injuryDistrictCode")
    @Mapping(target = "injuryAddress", source = "request.injuryAddress")

    @Mapping(target = "animalCategoryCode", source = "request.animalCategoryCode")
    @Mapping(target = "animalColor", source = "request.animalColor")
    @Mapping(target = "animalType", source = "request.animalType")
    @Mapping(target = "animalBreed", source = "request.animalBreed")

    @Mapping(target = "ownerLastName", source = "request.animalOwnerInfo.ownerLastName")
    @Mapping(target = "ownerFirstName", source = "request.animalOwnerInfo.ownerFirstName")
    @Mapping(target = "ownerMiddleName", source = "request.animalOwnerInfo.ownerMiddleName")
    @Mapping(target = "ownerRegionCode", source = "request.animalOwnerInfo.regionCode")
    @Mapping(target = "ownerDistrictCode", source = "request.animalOwnerInfo.districtCode")
    @Mapping(target = "ownerNeighborhoodCode", source = "request.animalOwnerInfo.neighborhoodCode")
    @Mapping(target = "ownerStreet", source = "request.animalOwnerInfo.street")
    @Mapping(target = "ownerHouseNumber", source = "request.animalOwnerInfo.houseNumber")
    @Mapping(target = "ownerApartmentNumber", source = "request.animalOwnerInfo.apartmentNumber")

    @Mapping(target = "patient", source = "request.patient")

    @Mapping(target = "senderOrganizationId", source = "senderOrganizationId")
    @Mapping(target = "sourceIntegrationClientId", source = "sourceIntegrationClientId")
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "activeOrganizationId")

    @Mapping(target = "otherPeopleInjured", source = "request.otherPeopleInjured")
    @Mapping(target = "otherInjuredPeople", source = "request.otherInjuredPeople")

    @Mapping(target = "hospitalizedAt", source = "request.hospitalizedAt")
    @Mapping(target = "hospitalOrganizationId", source = "request.hospitalOrganizationId", qualifiedByName = "nullableActiveOrganizationId")

    @Mapping(target = "antirabicAssistanceInfo", source = "request.antirabicAssistanceInfo")
    @Mapping(target = "notifierFullName", source = "request.notifierFullName")
    @Mapping(target = "receiverFullName", source = "request.receiverFullName")
    @Mapping(target = "messageSentAt", source = "request.messageSentAt")
    CreateForm0581Command toCommand(
            DmedCreateForm0581Request request, String source, Long senderOrganizationId, Long sourceIntegrationClientId);

    OtherInjuredPersonCommand toCommand(OtherInjuredPersonRequest request);
}
