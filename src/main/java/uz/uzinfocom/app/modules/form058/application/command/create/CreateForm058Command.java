package uz.uzinfocom.app.modules.form058.application.command.create;

import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateForm058Command(
        String mkb10Code,
        String mkb10Name,

        String finalMkb10Code,
        String finalMkb10Name,

        Integer mkb10UsageLimit,

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

    public String resolvedFinalMkb10Code() {
        return StringUtils.hasText(finalMkb10Code)
                ? finalMkb10Code.trim()
                : mkb10Code;
    }

    public String resolvedFinalMkb10Name() {
        return StringUtils.hasText(finalMkb10Name)
                ? finalMkb10Name.trim()
                : mkb10Name;
    }
}