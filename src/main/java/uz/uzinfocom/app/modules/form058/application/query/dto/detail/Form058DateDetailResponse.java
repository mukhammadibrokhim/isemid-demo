package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Form058DateDetailResponse(
        LocalDate admissionDate,
        LocalDate diseaseDate,
        LocalDate firstVisitDate,
        LocalDate diagnosisDate,
        LocalDate visitDate,
        LocalDateTime initialReportDateTime
) {
}