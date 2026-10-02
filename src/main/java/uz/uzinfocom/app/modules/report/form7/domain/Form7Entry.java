package uz.uzinfocom.app.modules.report.form7.domain;

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
 * One "Shakl №7 yaratish" submission for a single reporting organization and
 * an arbitrary {@code [fromDate, toDate]} period — infectious-disease
 * registry movement over the reporting period.
 * <p>
 * The {@code registered*} block plus {@code primaryDiagnosisConfirmed} are
 * computed server-side at creation/update time from the existing form058 +
 * form058_1 case data (see {@code Form7EntryQueryService#prefill} /
 * {@code Form7EntryCommandService}) and stored as a snapshot, never trusted
 * from the client — exactly like {@code Form2ManualEntry}'s
 * {@code registeredCases*}. Every other count (cases at period start/end,
 * urban/rural split, examined, to be examined, hospitalized) has no other
 * source of truth in the system and is always operator-entered.
 */
@Getter
@Setter
@Entity
@Table(
        name = "rp_form7_entry",
        indexes = {
                @Index(name = "idx_form7_entry_organization_id", columnList = "organization_id"),
                @Index(name = "idx_form7_entry_from_to", columnList = "from_date,to_date")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Form7Entry extends AuditableEntity {

    @Column(name = "organization_id", nullable = false, updatable = false)
    private Long organizationId;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    /** Davr boshida kasallanishlar soni — operator-entered. */
    @Builder.Default
    @Column(name = "cases_at_period_start", nullable = false)
    private Integer casesAtPeriodStart = 0;

    /** Hisobot davrida ro'yxatga olingan bemorlar — Jami. Snapshot (form058 + form058_1). */
    @Column(name = "registered_total", nullable = false)
    private Long registeredTotal;

    /** Hisobot davrida ro'yxatga olingan — 14 yoshgacha bolalar. Snapshot. */
    @Column(name = "registered_under_14", nullable = false)
    private Long registeredUnder14;

    /** Hisobot davrida ro'yxatga olingan — 18 yoshgacha bolalar. Snapshot. */
    @Column(name = "registered_under_18", nullable = false)
    private Long registeredUnder18;

    /** Hisobot davrida ro'yxatga olingan — kattalar (18+). Snapshot. */
    @Column(name = "registered_adult", nullable = false)
    private Long registeredAdult;

    /** Hisobot davrida ro'yxatga olingan — ayollar. Snapshot. */
    @Column(name = "registered_female", nullable = false)
    private Long registeredFemale;

    /** Hisobot davrida ro'yxatga olingan — shahar aholisi. Operator-entered (manbada yo'q). */
    @Builder.Default
    @Column(name = "registered_urban_count", nullable = false)
    private Integer registeredUrbanCount = 0;

    /** Hisobot davrida ro'yxatga olingan — qishloq aholisi. Operator-entered (manbada yo'q). */
    @Builder.Default
    @Column(name = "registered_rural_count", nullable = false)
    private Integer registeredRuralCount = 0;

    /** Ulardan — tekshiruvdan o'tdi. Operator-entered. */
    @Builder.Default
    @Column(name = "examined_count", nullable = false)
    private Integer examinedCount = 0;

    /** Ulardan — tekshirilishi kerak. Operator-entered. */
    @Builder.Default
    @Column(name = "to_be_examined_count", nullable = false)
    private Integer toBeExaminedCount = 0;

    /** Ulardan — birlamchi tashxis tasdiqlandi. Snapshot (status = APPROVED). */
    @Column(name = "primary_diagnosis_confirmed", nullable = false)
    private Long primaryDiagnosisConfirmed;

    /** Ulardan — shifoxonaga yotqizilgan. Operator-entered. */
    @Builder.Default
    @Column(name = "hospitalized_count", nullable = false)
    private Integer hospitalizedCount = 0;

    /** Davr ohirida kasallanishlar soni — operator-entered. */
    @Builder.Default
    @Column(name = "cases_at_period_end", nullable = false)
    private Integer casesAtPeriodEnd = 0;
}
