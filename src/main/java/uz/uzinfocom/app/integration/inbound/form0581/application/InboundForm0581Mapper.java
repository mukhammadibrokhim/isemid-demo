package uz.uzinfocom.app.integration.inbound.form0581.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.integration.inbound.form0581.web.InboundCreateForm0581Request;
import uz.uzinfocom.app.modules.form0581.application.command.OtherInjuredPersonCommand;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Command;
import uz.uzinfocom.app.modules.form0581.web.dto.request.OtherInjuredPersonRequest;
import uz.uzinfocom.app.modules.patient.web.mapper.PatientRequestMapper;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = {OrganizationMappingHelper.class, PatientRequestMapper.class})
public interface InboundForm0581Mapper {

    @Mapping(target = "source", source = "source")
    @Mapping(target = "mkb10Code", source = "request.diagnosisInfo.mkb10Code")
    @Mapping(target = "mkb10Name", source = "request.diagnosisInfo.mkb10Name")
    @Mapping(target = "injuryLocalization", source = "request.diagnosisInfo.injuryLocalization")

    @Mapping(target = "injuryDateTime", source = "request.incidentInfo.injuryDateTime")
    @Mapping(target = "dpuVisitDateTime", source = "request.incidentInfo.dpuVisitDateTime")
    @Mapping(target = "injuryRegionCode", source = "request.incidentInfo.injuryRegionCode")
    @Mapping(target = "injuryDistrictCode", source = "request.incidentInfo.injuryDistrictCode")
    @Mapping(target = "injuryAddress", source = "request.incidentInfo.injuryAddress")

    @Mapping(target = "animalCategoryCode", source = "request.animalInfo.animalCategoryCode")
    @Mapping(target = "animalColor", source = "request.animalInfo.animalColor")
    @Mapping(target = "animalType", source = "request.animalInfo.animalType")
    @Mapping(target = "animalBreed", source = "request.animalInfo.animalBreed")

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
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "activeOrganizationId")

    @Mapping(target = "otherPeopleInjured", source = "request.otherPeopleInjured")
    @Mapping(target = "otherInjuredPeople", source = "request.otherInjuredPeople")

    @Mapping(target = "hospitalizedAt", source = "request.hospitalizationInfo.hospitalizedAt")
    @Mapping(target = "hospitalOrganizationId", source = "request.hospitalizationInfo.hospitalOrganizationId", qualifiedByName = "nullableActiveOrganizationId")

    @Mapping(target = "antirabicAssistanceInfo", source = "request.reportInfo.antirabicAssistanceInfo")
    @Mapping(target = "notifierFullName", source = "request.reportInfo.notifierFullName")
    @Mapping(target = "receiverFullName", source = "request.reportInfo.receiverFullName")
    @Mapping(target = "messageSentAt", source = "request.reportInfo.messageSentAt")
    CreateForm0581Command toCommand(InboundCreateForm0581Request request, String source, Long senderOrganizationId);

    OtherInjuredPersonCommand toCommand(OtherInjuredPersonRequest request);
}
