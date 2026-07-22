package uz.uzinfocom.app.integration.outbound.patientcase.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.integration.outbound.patientcase.web.dto.PatientCaseResponse;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public interface PatientCaseMapper {

    PatientCaseResponse.IdentifierSummary toIdentifierSummary(PatientIdentifier identifier);

    List<PatientCaseResponse.IdentifierSummary> toIdentifierSummaries(List<PatientIdentifier> identifiers);

    PatientCaseResponse.PatientSummary toPatientSummary(Patient patient);

    @Mapping(target = "mkb10Code", source = "diagnosisInfo.mkb10Code")
    @Mapping(target = "mkb10Name", source = "diagnosisInfo.mkb10Name")
    @Mapping(target = "finalMkb10Code", source = "diagnosisInfo.finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "diagnosisInfo.finalMkb10Name")
    @Mapping(target = "diseaseDate", source = "dateInfo.diseaseDate")
    @Mapping(target = "firstVisitDate", source = "dateInfo.firstVisitDate")
    @Mapping(target = "visitDate", source = "dateInfo.visitDate")
    @Mapping(target = "initialReportDateTime", source = "dateInfo.initialReportDateTime")
    PatientCaseResponse.Form058Summary toForm058Summary(Form058 form058);

    @Mapping(target = "mkb10Code", source = "diagnosisInfo.mkb10Code")
    @Mapping(target = "mkb10Name", source = "diagnosisInfo.mkb10Name")
    @Mapping(target = "finalMkb10Code", source = "diagnosisInfo.finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "diagnosisInfo.finalMkb10Name")
    @Mapping(target = "injuryDateTime", source = "incidentInfo.injuryDateTime")
    @Mapping(target = "dpuVisitDateTime", source = "incidentInfo.dpuVisitDateTime")
    PatientCaseResponse.Form0581Summary toForm0581Summary(Form0581 form0581);

    default PatientCaseResponse toResponse(Patient patient, Form058 latestForm058, Form0581 latestForm0581) {
        return new PatientCaseResponse(
                toPatientSummary(patient),
                latestForm058 == null ? null : toForm058Summary(latestForm058),
                latestForm0581 == null ? null : toForm0581Summary(latestForm0581)
        );
    }
}
