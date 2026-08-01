package uz.uzinfocom.app.modules.report.form32.domain;

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
 * One "Shakl №3-2 yaratish" submission for a single reporting organization
 * and an arbitrary {@code [fromDate, toDate]} period — sanitary inspection
 * activity counts (inspected objects, identified deficiencies, officials
 * held liable, suspended/closed objects), each broken down by object type
 * (MTM/Maktab/DPM/Boshqa) plus a total. Same as {@code Form31Entry}: no
 * field here has any other source of truth in the system, every count is
 * always operator-entered.
 */
@Getter
@Setter
@Entity
@Table(
        name = "rp_form32_entry",
        indexes = {
                @Index(name = "idx_form32_entry_organization_id", columnList = "organization_id"),
                @Index(name = "idx_form32_entry_from_to", columnList = "from_date,to_date")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Form32Entry extends AuditableEntity {

    @Column(name = "organization_id", nullable = false, updatable = false)
    private Long organizationId;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    /** Tekshirilgan obyektlar — Jami. */
    @Builder.Default
    @Column(name = "inspected_total_count", nullable = false)
    private Integer inspectedTotalCount = 0;

    /** Tekshirilgan obyektlar — shu jumladan MTM. */
    @Builder.Default
    @Column(name = "inspected_mtm_count", nullable = false)
    private Integer inspectedMtmCount = 0;

    /** Tekshirilgan obyektlar — shu jumladan Maktab. */
    @Builder.Default
    @Column(name = "inspected_school_count", nullable = false)
    private Integer inspectedSchoolCount = 0;

    /** Tekshirilgan obyektlar — shu jumladan DPM. */
    @Builder.Default
    @Column(name = "inspected_dpm_count", nullable = false)
    private Integer inspectedDpmCount = 0;

    /** Tekshirilgan obyektlar — shu jumladan Boshqa. */
    @Builder.Default
    @Column(name = "inspected_other_count", nullable = false)
    private Integer inspectedOtherCount = 0;

    /** Aniqlangan kamchiliklar — Jami. */
    @Builder.Default
    @Column(name = "deficiency_total_count", nullable = false)
    private Integer deficiencyTotalCount = 0;

    /** Aniqlangan kamchiliklar — shu jumladan MTM. */
    @Builder.Default
    @Column(name = "deficiency_mtm_count", nullable = false)
    private Integer deficiencyMtmCount = 0;

    /** Aniqlangan kamchiliklar — shu jumladan Maktab. */
    @Builder.Default
    @Column(name = "deficiency_school_count", nullable = false)
    private Integer deficiencySchoolCount = 0;

    /** Aniqlangan kamchiliklar — shu jumladan DPM. */
    @Builder.Default
    @Column(name = "deficiency_dpm_count", nullable = false)
    private Integer deficiencyDpmCount = 0;

    /** Aniqlangan kamchiliklar — shu jumladan Boshqa. */
    @Builder.Default
    @Column(name = "deficiency_other_count", nullable = false)
    private Integer deficiencyOtherCount = 0;

    /** Mansabdor shaxslar — Jami. */
    @Builder.Default
    @Column(name = "official_total_count", nullable = false)
    private Integer officialTotalCount = 0;

    /** Mansabdor shaxslar — shu jumladan MTM. */
    @Builder.Default
    @Column(name = "official_mtm_count", nullable = false)
    private Integer officialMtmCount = 0;

    /** Mansabdor shaxslar — shu jumladan Maktab. */
    @Builder.Default
    @Column(name = "official_school_count", nullable = false)
    private Integer officialSchoolCount = 0;

    /** Mansabdor shaxslar — shu jumladan DPM. */
    @Builder.Default
    @Column(name = "official_dpm_count", nullable = false)
    private Integer officialDpmCount = 0;

    /** Mansabdor shaxslar — shu jumladan Boshqa. */
    @Builder.Default
    @Column(name = "official_other_count", nullable = false)
    private Integer officialOtherCount = 0;

    /** To'xtatilgan obyektlar — Jami. */
    @Builder.Default
    @Column(name = "suspended_total_count", nullable = false)
    private Integer suspendedTotalCount = 0;

    /** To'xtatilgan obyektlar — shu jumladan MTM. */
    @Builder.Default
    @Column(name = "suspended_mtm_count", nullable = false)
    private Integer suspendedMtmCount = 0;

    /** To'xtatilgan obyektlar — shu jumladan Maktab. */
    @Builder.Default
    @Column(name = "suspended_school_count", nullable = false)
    private Integer suspendedSchoolCount = 0;

    /** To'xtatilgan obyektlar — shu jumladan DPM. */
    @Builder.Default
    @Column(name = "suspended_dpm_count", nullable = false)
    private Integer suspendedDpmCount = 0;

    /** To'xtatilgan obyektlar — shu jumladan Boshqa. */
    @Builder.Default
    @Column(name = "suspended_other_count", nullable = false)
    private Integer suspendedOtherCount = 0;
}
