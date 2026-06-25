package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import java.time.Instant;
import java.time.LocalDate;

public record Form058DateDetailResponse(
        LocalDate admissionDate,
        LocalDate diseaseDate,
        LocalDate firstVisitDate,
        LocalDate diagnosisDate,
        LocalDate visitDate,
        Instant docSendDate,
        Instant initialReportDateTime
) {
}