package uz.uzinfocom.app.modules.report.form31.domain;

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
 * One "Shakl №3-1 yaratish" submission for a single reporting organization
 * and an arbitrary {@code [fromDate, toDate]} period — weekly ILI/SARI
 * (influenza-like illness / severe acute respiratory infection) sentinel
 * surveillance counts plus flu vaccination coverage. Unlike {@code
 * Form2ManualEntry}, none of these counts have any other source of truth
 * in the system (no linked {@code ManualReport}/MKB-10 tagging) — every
 * field here is always operator-entered.
 */
@Getter
@Setter
@Entity
@Table(
        name = "form31_entry",
        indexes = {
                @Index(name = "idx_form31_entry_organization_id", columnList = "organization_id"),
                @Index(name = "idx_form31_entry_from_to", columnList = "from_date,to_date")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Form31Entry extends AuditableEntity {

    @Column(name = "organization_id", nullable = false, updatable = false)
    private Long organizationId;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    /** Grippga o'xshash kasallik (ILI — influenza-like illness) case count. */
    @Builder.Default
    @Column(name = "ili_cases_count", nullable = false)
    private Integer iliCasesCount = 0;

    /** O'RI (acute respiratory infection) case count. */
    @Builder.Default
    @Column(name = "ari_cases_count", nullable = false)
    private Integer ariCasesCount = 0;

    /** O'tkir zotiljam (acute pneumonia) case count. */
    @Builder.Default
    @Column(name = "pneumonia_cases_count", nullable = false)
    private Integer pneumoniaCasesCount = 0;

    /** OO'RI (severe acute respiratory infection) — Jami (total). */
    @Builder.Default
    @Column(name = "sari_total_count", nullable = false)
    private Integer sariTotalCount = 0;

    /** OO'RI — Ulardan homiladorlar (of which, pregnant women). */
    @Builder.Default
    @Column(name = "sari_pregnant_count", nullable = false)
    private Integer sariPregnantCount = 0;

    /** O'lganlar (deaths) — Jami (total). */
    @Builder.Default
    @Column(name = "death_total_count", nullable = false)
    private Integer deathTotalCount = 0;

    /** O'lganlar — Ulardan homiladorlar (of which, pregnant women). */
    @Builder.Default
    @Column(name = "death_pregnant_count", nullable = false)
    private Integer deathPregnantCount = 0;

    /** Vaktsinatsiya grippga qarshi — Haftalik kasallanish (weekly count). */
    @Builder.Default
    @Column(name = "weekly_vaccination_count", nullable = false)
    private Integer weeklyVaccinationCount = 0;

    /** Vaktsinatsiya grippga qarshi — Mavsum boshidan (since season start). */
    @Builder.Default
    @Column(name = "season_vaccination_count", nullable = false)
    private Integer seasonVaccinationCount = 0;
}
