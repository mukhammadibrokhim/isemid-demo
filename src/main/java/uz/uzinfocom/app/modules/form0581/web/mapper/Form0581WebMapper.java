package uz.uzinfocom.app.modules.form0581.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form0581.application.command.OtherInjuredPersonCommand;
import uz.uzinfocom.app.modules.form0581.application.command.approve.ApproveForm0581Command;
import uz.uzinfocom.app.modules.form0581.application.command.approve.NotApproveForm0581Command;
import uz.uzinfocom.app.modules.form0581.application.command.cancel.CancelForm0581Command;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Command;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Result;
import uz.uzinfocom.app.modules.form0581.application.command.update.UpdateForm0581Command;
import uz.uzinfocom.app.modules.form0581.application.command.update.UpdateForm0581Result;
import uz.uzinfocom.app.modules.form0581.application.shared.Form0581OrganizationMappingHelper;
import uz.uzinfocom.app.modules.form0581.web.dto.request.ApproveForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.CancelForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.CreateForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.NotApproveForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.OtherInjuredPersonRequest;
import uz.uzinfocom.app.modules.form0581.web.dto.request.UpdateForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.response.CreateForm0581Response;
import uz.uzinfocom.app.modules.form0581.web.dto.response.UpdateForm0581Response;
import uz.uzinfocom.app.modules.patient.web.mapper.PatientRequestMapper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = {Form0581OrganizationMappingHelper.class, PatientRequestMapper.class})
public interface Form0581WebMapper {

    @Mapping(target = "source", source = "source")
    @Mapping(target = "mkb10Code", source = "request.mkb10Code")
    @Mapping(target = "mkb10Name", source = "request.mkb10Name")
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

    @Mapping(target = "ownerLastName", source = "request.animalOwner.ownerLastName")
    @Mapping(target = "ownerFirstName", source = "request.animalOwner.ownerFirstName")
    @Mapping(target = "ownerMiddleName", source = "request.animalOwner.ownerMiddleName")
    @Mapping(target = "ownerRegionCode", source = "request.animalOwner.regionCode")
    @Mapping(target = "ownerDistrictCode", source = "request.animalOwner.districtCode")
    @Mapping(target = "ownerNeighborhoodCode", source = "request.animalOwner.neighborhoodCode")
    @Mapping(target = "ownerStreet", source = "request.animalOwner.street")
    @Mapping(target = "ownerHouseNumber", source = "request.animalOwner.houseNumber")
    @Mapping(target = "ownerApartmentNumber", source = "request.animalOwner.apartmentNumber")

    @Mapping(target = "patient", source = "request.patient")

    @Mapping(target = "senderOrganizationId", source = "request.senderOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "activeOrganizationId")

    @Mapping(target = "otherPeopleInjured", source = "request.otherPeopleInjured")
    @Mapping(target = "otherInjuredPeople", source = "request.otherInjuredPeople")

    @Mapping(target = "hospitalizedAt", source = "request.hospitalizedAt")
    @Mapping(target = "hospitalOrganizationId", source = "request.hospitalOrganizationId", qualifiedByName = "nullableActiveOrganizationId")

    @Mapping(target = "antirabicAssistanceInfo", source = "request.antirabicAssistanceInfo")
    @Mapping(target = "notifierFullName", source = "request.notifierFullName")
    @Mapping(target = "receiverFullName", source = "request.receiverFullName")
    @Mapping(target = "messageSentAt", source = "request.messageSentAt")
    CreateForm0581Command toCommand(CreateForm0581Request request, String source);

    OtherInjuredPersonCommand toCommand(OtherInjuredPersonRequest request);

    CreateForm0581Response toResponse(CreateForm0581Result result);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "mkb10Code", source = "request.mkb10Code")
    @Mapping(target = "mkb10Name", source = "request.mkb10Name")
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

    @Mapping(target = "ownerLastName", source = "request.animalOwner.ownerLastName")
    @Mapping(target = "ownerFirstName", source = "request.animalOwner.ownerFirstName")
    @Mapping(target = "ownerMiddleName", source = "request.animalOwner.ownerMiddleName")
    @Mapping(target = "ownerRegionCode", source = "request.animalOwner.regionCode")
    @Mapping(target = "ownerDistrictCode", source = "request.animalOwner.districtCode")
    @Mapping(target = "ownerNeighborhoodCode", source = "request.animalOwner.neighborhoodCode")
    @Mapping(target = "ownerStreet", source = "request.animalOwner.street")
    @Mapping(target = "ownerHouseNumber", source = "request.animalOwner.houseNumber")
    @Mapping(target = "ownerApartmentNumber", source = "request.animalOwner.apartmentNumber")

    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "nullableActiveOrganizationId")

    @Mapping(target = "otherPeopleInjured", source = "request.otherPeopleInjured")
    @Mapping(target = "otherInjuredPeople", source = "request.otherInjuredPeople")

    @Mapping(target = "hospitalizedAt", source = "request.hospitalizedAt")
    @Mapping(target = "hospitalOrganizationId", source = "request.hospitalOrganizationId", qualifiedByName = "nullableActiveOrganizationId")

    @Mapping(target = "antirabicAssistanceInfo", source = "request.antirabicAssistanceInfo")
    @Mapping(target = "notifierFullName", source = "request.notifierFullName")
    @Mapping(target = "receiverFullName", source = "request.receiverFullName")
    @Mapping(target = "messageSentAt", source = "request.messageSentAt")

    @Mapping(target = "patient", source = "request.patient")
    UpdateForm0581Command toCommand(Long id, UpdateForm0581Request request);

    UpdateForm0581Response toResponse(UpdateForm0581Result result);

    @Mapping(target = "formId", source = "id")
    @Mapping(target = "finalMkb10Code", source = "request.finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "request.finalMkb10Name")
    ApproveForm0581Command toCommand(Long id, ApproveForm0581Request request);

    @Mapping(target = "formId", source = "id")
    @Mapping(target = "reason", source = "request.reason")
    NotApproveForm0581Command toCommand(Long id, NotApproveForm0581Request request);

    @Mapping(target = "formId", source = "id")
    @Mapping(target = "reason", source = "request.reason")
    CancelForm0581Command toCommand(Long id, CancelForm0581Request request);
}
