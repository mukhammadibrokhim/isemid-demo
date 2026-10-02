package uz.uzinfocom.app.modules.report.analytic.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * One "Analitik hisobot" — an ad-hoc analytical report the caller builds by
 * picking a date range plus multi-select regions/ICD-10 diagnoses, previewing
 * the computed per-region population and confirmed-case rate ({@code
 * AnalyticReportComputeService}), and free-editing the result into {@link
 * #content} before saving. {@link #status} distinguishes a reusable "Shablon
 * sifatida saqlash" template from a finished "Saqlash" report — both are the
 * same shape, just reopened/edited differently by the client.
 */
@Getter
@Setter
@Entity
@Table(
        name = "rp_analytic_report",
        indexes = {
                @Index(name = "idx_analytic_report_organization_id", columnList = "organization_id"),
                @Index(name = "idx_analytic_report_from_to", columnList = "from_date,to_date")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticReport extends AuditableEntity {

    @Column(name = "organization_id", nullable = false, updatable = false)
    private Long organizationId;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnalyticReportStatus status = AnalyticReportStatus.FINAL;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Builder.Default
    @Column(name = "koef", nullable = false)
    private Long koef = 100_000L;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "rp_analytic_report_region_codes",
            joinColumns = @JoinColumn(name = "analytic_report_id"),
            indexes = @Index(name = "idx_analytic_report_region_codes_report_id", columnList = "analytic_report_id")
    )
    @Column(name = "region_code", length = 20)
    private Set<String> regionCodes = new HashSet<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "rp_analytic_report_icd10_codes",
            joinColumns = @JoinColumn(name = "analytic_report_id"),
            indexes = @Index(name = "idx_analytic_report_icd10_codes_report_id", columnList = "analytic_report_id")
    )
    @Column(name = "icd10_code", length = 20)
    private Set<String> icd10Codes = new HashSet<>();
}
