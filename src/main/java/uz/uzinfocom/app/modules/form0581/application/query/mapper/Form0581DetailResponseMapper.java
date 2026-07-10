package uz.uzinfocom.app.modules.form0581.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form0581.application.query.dto.detail.*;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581OtherInjuredPerson;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.*;
import uz.uzinfocom.app.modules.patient.application.query.mapper.PatientDetailResponseMapper;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(
        config = CentralMapperConfig.class,
        uses = {
                PatientDetailResponseMapper.class
        }
)
public interface Form0581DetailResponseMapper {

    @Mapping(target = "id", source = "form0581.id")
    @Mapping(target = "uuid", source = "form0581.uuid")
    @Mapping(target = "status", source = "form0581.status")
    @Mapping(target = "source", source = "form0581.source")
    @Mapping(target = "senderOrganizationId", source = "form0581.senderOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "form0581.receiverOrganizationId")

    @Mapping(target = "diagnosisInfo", source = "form0581.diagnosisInfo")
    @Mapping(target = "incidentInfo", source = "form0581.incidentInfo")
    @Mapping(target = "animalInfo", source = "form0581.animalInfo")
    @Mapping(target = "animalOwnerInfo", source = "form0581.animalOwnerInfo")
    @Mapping(target = "otherPeopleInjured", source = "form0581.otherPeopleInjured")
    @Mapping(target = "otherInjuredPeople", source = "form0581.otherInjuredPeople")
    @Mapping(target = "hospitalizationInfo", source = "form0581.hospitalizationInfo")
    @Mapping(target = "reportInfo", source = "form0581.reportInfo")
    @Mapping(target = "cancellationInfo", source = "form0581.cancellationInfo")
    @Mapping(target = "approvalInfo", source = "form0581.approvalInfo")
    @Mapping(target = "deleteInfo", source = "form0581.deleteInfo")

    @Mapping(target = "patient", source = "form0581.patient")
    @Mapping(target = "audit", source = "audit")
    Form0581DetailResponse toDetailedResponse(Form0581 form0581, AuditResponse audit);

    Form0581DiagnosisDetailResponse toResponse(Form0581DiagnosisInfo source);

    Form0581IncidentDetailResponse toResponse(Form0581IncidentInfo source);

    Form0581AnimalDetailResponse toResponse(Form0581AnimalInfo source);

    @Mapping(target = "ownerRegionCode", source = "ownerAddress.regionCode")
    @Mapping(target = "ownerDistrictCode", source = "ownerAddress.districtCode")
    @Mapping(target = "ownerNeighborhoodCode", source = "ownerAddress.neighborhoodCode")
    @Mapping(target = "ownerStreet", source = "ownerAddress.street")
    @Mapping(target = "ownerHouseNumber", source = "ownerAddress.houseNumber")
    @Mapping(target = "ownerApartmentNumber", source = "ownerAddress.apartmentNumber")
    Form0581AnimalOwnerDetailResponse toResponse(Form0581AnimalOwnerInfo source);

    Form0581HospitalizationDetailResponse toResponse(Form0581HospitalizationInfo source);

    Form0581ReportDetailResponse toResponse(Form0581ReportInfo source);

    Form0581CancellationDetailResponse toResponse(Form0581CancellationInfo source);

    Form0581ApprovalDetailResponse toResponse(Form0581ApprovalInfo source);

    Form0581DeleteDetailResponse toResponse(Form0581DeleteInfo source);

    @Mapping(target = "regionCode", source = "address.regionCode")
    @Mapping(target = "districtCode", source = "address.districtCode")
    @Mapping(target = "neighborhoodCode", source = "address.neighborhoodCode")
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "houseNumber", source = "address.houseNumber")
    @Mapping(target = "apartmentNumber", source = "address.apartmentNumber")
    Form0581OtherInjuredPersonDetailResponse toResponse(Form0581OtherInjuredPerson source);
}
