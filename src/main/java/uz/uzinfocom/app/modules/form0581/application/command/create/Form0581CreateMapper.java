package uz.uzinfocom.app.modules.form0581.application.command.create;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public abstract class Form0581CreateMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "status", constant = "SENT")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "patient", ignore = true)

    @Mapping(target = "senderOrganizationId", source = "senderOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "receiverOrganizationId")

    @Mapping(target = "diagnosisInfo.mkb10Code", source = "mkb10Code")
    @Mapping(target = "diagnosisInfo.mkb10Name", source = "mkb10Name")
    @Mapping(target = "diagnosisInfo.injuryLocalization", source = "injuryLocalization")
    @Mapping(target = "diagnosisInfo.finalMkb10Code", source = "mkb10Code")
    @Mapping(target = "diagnosisInfo.finalMkb10Name", source = "mkb10Name")

    @Mapping(target = "incidentInfo.injuryDateTime", source = "injuryDateTime")
    @Mapping(target = "incidentInfo.dpuVisitDateTime", source = "dpuVisitDateTime")
    @Mapping(target = "incidentInfo.injuryRegionCode", source = "injuryRegionCode")
    @Mapping(target = "incidentInfo.injuryDistrictCode", source = "injuryDistrictCode")
    @Mapping(target = "incidentInfo.injuryAddress", source = "injuryAddress")

    @Mapping(target = "animalInfo.animalCategoryCode", source = "animalCategoryCode")
    @Mapping(target = "animalInfo.animalColor", source = "animalColor")
    @Mapping(target = "animalInfo.animalType", source = "animalType")
    @Mapping(target = "animalInfo.animalBreed", source = "animalBreed")

    @Mapping(target = "animalOwnerInfo.ownerLastName", source = "ownerLastName")
    @Mapping(target = "animalOwnerInfo.ownerFirstName", source = "ownerFirstName")
    @Mapping(target = "animalOwnerInfo.ownerMiddleName", source = "ownerMiddleName")
    @Mapping(target = "animalOwnerInfo.ownerAddress.regionCode", source = "ownerRegionCode")
    @Mapping(target = "animalOwnerInfo.ownerAddress.districtCode", source = "ownerDistrictCode")
    @Mapping(target = "animalOwnerInfo.ownerAddress.neighborhoodCode", source = "ownerNeighborhoodCode")
    @Mapping(target = "animalOwnerInfo.ownerAddress.street", source = "ownerStreet")
    @Mapping(target = "animalOwnerInfo.ownerAddress.houseNumber", source = "ownerHouseNumber")
    @Mapping(target = "animalOwnerInfo.ownerAddress.apartmentNumber", source = "ownerApartmentNumber")

    @Mapping(target = "otherPeopleInjured", source = "otherPeopleInjured")
    @Mapping(target = "otherInjuredPeople", ignore = true)

    @Mapping(target = "hospitalizationInfo.hospitalizedAt", source = "hospitalizedAt")
    @Mapping(target = "hospitalizationInfo.hospitalOrganizationId", source = "hospitalOrganizationId")

    @Mapping(target = "reportInfo.antirabicAssistanceInfo", source = "antirabicAssistanceInfo")
    @Mapping(target = "reportInfo.notifierFullName", source = "notifierFullName")
    @Mapping(target = "reportInfo.receiverFullName", source = "receiverFullName")
    @Mapping(target = "reportInfo.messageSentAt", source = "messageSentAt")

    @Mapping(target = "cancellationInfo", ignore = true)
    @Mapping(target = "approvalInfo", ignore = true)
    @Mapping(target = "deleteInfo", ignore = true)
    public abstract Form0581 toEntity(CreateForm0581Command command);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "status", source = "status")
    public abstract CreateForm0581Result toResult(Form0581 form0581);
}
