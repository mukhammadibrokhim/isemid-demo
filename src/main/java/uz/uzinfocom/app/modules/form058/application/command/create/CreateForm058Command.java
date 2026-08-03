package uz.uzinfocom.app.modules.form058.application.command.create;

import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateForm058Command(
        String icd10Code,
        String icd10Name,

        String finalIcd10Code,
        String finalIcd10Name,

        Integer icd10UsageLimit,

        CreatePatientCommand patient,

        String source,

        Boolean labConfirmation,

        LocalDate diseaseDate,
        LocalDate firstVisitDate,
        LocalDate visitDate,
        LocalDate admissionDate,
        LocalDate diagnosisDate,

        LocalDateTime initialReportDateTime,

        Long senderOrganizationId,
        Long receiverOrganizationId,
        Long hospitalPlaceId,

        String diseasePlaceCode,
        String diseaseCause,
        String epidemicMeasures,

        String notifierFullName,
        String journalFormCode,
        String comment,

        Double locationLatitude,
        Double locationLongitude,
        String location
) {

    public String resolvedFinalIcd10Code() {
        return StringUtils.hasText(finalIcd10Code)
                ? finalIcd10Code.trim()
                : icd10Code;
    }

    public String resolvedFinalIcd10Name() {
        return StringUtils.hasText(finalIcd10Name)
                ? finalIcd10Name.trim()
                : icd10Name;
    }
}