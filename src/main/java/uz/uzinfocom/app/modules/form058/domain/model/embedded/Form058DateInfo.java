package uz.uzinfocom.app.modules.form058.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form058DateInfo {

    @Column(name = "admission_date")
    private LocalDateTime admissionDate;

    @Column(name = "disease_date", nullable = false)
    private LocalDateTime diseaseDate;

    @Column(name = "first_visit_date", nullable = false)
    private LocalDateTime firstVisitDate;

    @Column(name = "diagnosis_date")
    private LocalDateTime diagnosisDate;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Column(name = "initial_report_date_time", nullable = false)
    private LocalDateTime initialReportDateTime;
}
