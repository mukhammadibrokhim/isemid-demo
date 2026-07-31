package uz.uzinfocom.app.modules.report.form2.manual.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.time.LocalDate;

/**
 * One "Shakl №2 yaratish" submission for a single reporting organization and
 * an arbitrary {@code [fromDate, toDate]} period. {@code
 * registeredCasesLastYear}/{@code registeredCasesCurrentYear} are computed
 * server-side at creation time from the existing Form 2 case data (see
 * {@code Form2ManualEntryQueryService#prefill} /
 * {@code Form2ManualEntryCommandService#create}) and stored as a snapshot,
 * never trusted from the client. Every other count below has no source of
 * truth anywhere else in the system (disinfection, inspections, fines,
 * etc.) and is always operator-entered.
 */
@Getter
@Setter
@Entity
@Table(
        name = "form2_manual_entry",
        indexes = {
                @Index(name = "idx_form2_manual_entry_organization_id", columnList = "organization_id"),
                @Index(name = "idx_form2_manual_entry_from_to", columnList = "from_date,to_date")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Form2ManualEntry extends AuditableEntity {

    @Column(name = "organization_id", nullable = false, updatable = false)
    private Long organizationId;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "registered_cases_last_year", nullable = false)
    private Long registeredCasesLastYear;

    @Column(name = "registered_cases_current_year", nullable = false)
    private Long registeredCasesCurrentYear;

    @Builder.Default
    @Column(name = "mtm_school_count", nullable = false)
    private Integer mtmSchoolCount = 0;

    @Builder.Default
    @Column(name = "disinfection_foci_count", nullable = false)
    private Integer disinfectionFociCount = 0;

    @Builder.Default
    @Column(name = "inspected_objects_count", nullable = false)
    private Integer inspectedObjectsCount = 0;

    @Builder.Default
    @Column(name = "closed_objects_count", nullable = false)
    private Integer closedObjectsCount = 0;

    @Builder.Default
    @Column(name = "ddu_count", nullable = false)
    private Integer dduCount = 0;

    @Builder.Default
    @Column(name = "school_count", nullable = false)
    private Integer schoolCount = 0;

    @Builder.Default
    @Column(name = "fines_count", nullable = false)
    private Integer finesCount = 0;

    @Builder.Default
    @Column(name = "prosecutor_referral_count", nullable = false)
    private Integer prosecutorReferralCount = 0;
}
