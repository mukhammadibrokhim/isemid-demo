package uz.uzinfocom.app.modules.form058.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form058.application.query.dto.detail.*;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.domain.model.Form058Location;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.*;
import uz.uzinfocom.app.modules.patient.application.query.mapper.PatientDetailResponseMapper;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(
        config = CentralMapperConfig.class,
        uses = {
                PatientDetailResponseMapper.class
        }
)
public interface Form058DetailResponseMapper {
    @Mapping(target = "id", source = "form058.id")
    @Mapping(target = "uuid", source = "form058.uuid")
    @Mapping(target = "status", source = "form058.status")
    @Mapping(target = "source", source = "form058.source")
    @Mapping(target = "senderOrganizationId", source = "form058.senderOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "form058.receiverOrganizationId")

    @Mapping(target = "diagnosisInfo", source = "form058.diagnosisInfo")
    @Mapping(target = "clinicalInfo", source = "form058.clinicalInfo")
    @Mapping(target = "dateInfo", source = "form058.dateInfo")
    @Mapping(target = "location", source = "form058.location")
    @Mapping(target = "epidemicInfo", source = "form058.epidemicInfo")
    @Mapping(target = "reportInfo", source = "form058.reportInfo")
    @Mapping(target = "cancellationInfo", source = "form058.cancellationInfo")
    @Mapping(target = "approvalInfo", source = "form058.approvalInfo")
    @Mapping(target = "deleteInfo", source = "form058.deleteInfo")

    @Mapping(target = "patient", source = "form058.patient")
    @Mapping(target = "audit", source = "audit")
    Form058DetailResponse toDetailedResponse(Form058 form058, AuditResponse audit);

    Form058DiagnosisDetailResponse toResponse(Form058DiagnosisInfo source);

    Form058ClinicalDetailResponse toResponse(Form058ClinicalInfo source);

    @Mapping(target = "admissionDate", expression = "java(toLocalDate(source.getAdmissionDate()))")
    @Mapping(target = "diseaseDate", expression = "java(toLocalDate(source.getDiseaseDate()))")
    @Mapping(target = "firstVisitDate", expression = "java(toLocalDate(source.getFirstVisitDate()))")
    @Mapping(target = "diagnosisDate", expression = "java(toLocalDate(source.getDiagnosisDate()))")
    @Mapping(target = "visitDate", expression = "java(toLocalDate(source.getVisitDate()))")
    Form058DateDetailResponse toResponse(Form058DateInfo source);

    @Mapping(target = "address", source = "location")
    Form058LocationDetailResponse toResponse(Form058Location source);

    Form058EpidemicDetailResponse toResponse(Form058EpidemicInfo source);

    Form058ReportDetailResponse toResponse(Form058ReportInfo source);

    Form058CancellationDetailResponse toResponse(Form058CancellationInfo source);

    Form058ApprovalDetailResponse toResponse(Form058ApprovalInfo source);

    @Mapping(target = "hasLinkedCards", source = "hasLinkedCards")
    @Mapping(target = "assignedCardId", source = "assignedCardId")
    Form058CardLinkDetailResponse toCardLinkResponse(Form058 source);

    @Mapping(target = "deleted", source = "source.deleteInfo.deleted")
    @Mapping(target = "deletedAt", source = "source.deleteInfo.deletedAt")
    @Mapping(target = "deletedBy", source = "source.deleteInfo.deletedBy")
    @Mapping(target = "deleteReason", source = "source.deleteInfo.deleteReason")
    Form058DeleteDetailResponse toDeleteResponse(Form058 source);


    default LocalDate toLocalDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }
}