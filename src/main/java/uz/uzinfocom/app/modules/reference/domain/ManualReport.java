package uz.uzinfocom.app.modules.reference.domain;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import uz.uzinfocom.app.modules.reference.domain.base.ReferenceDictionaryEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "ref_manual_report",
        indexes = {
                @Index(name = "idx_ref_manual_report_code", columnList = "code"),
                @Index(name = "idx_ref_manual_report_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ref_manual_report_code", columnNames = "code")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ManualReport extends ReferenceDictionaryEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Column(name = "name_uz")
    private String nameUz;

    @Column(name = "name_uz_cyril")
    private String nameUzCyril;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_kaa")
    private String nameKaa;

    @Builder.Default
    @Column(name = "include_in_total", nullable = false)
    private Boolean includeInTotal = true;

    /** Only cases of this gender are counted in the row; {@code null} - both. */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private ManualReportGender gender;

    /** Lower age bound, inclusive; {@code null} - none. Always set together with {@link #ageFromUnit}. */
    @Column(name = "age_from")
    private Integer ageFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_from_unit", length = 10)
    private ManualReportAgeUnit ageFromUnit;

    /** Upper age bound, exclusive; {@code null} - none. Always set together with {@link #ageToUnit}. */
    @Column(name = "age_to")
    private Integer ageTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_to_unit", length = 10)
    private ManualReportAgeUnit ageToUnit;

    /** Only cases from this notification form are counted; {@code null} - both. */
    @Enumerated(EnumType.STRING)
    @Column(name = "case_source", length = 20)
    private ManualReportCaseSource caseSource;

    /**
     * {@code true} / {@code false} - only cases whose Form 058 lab confirmation is exactly that;
     * {@code null} - no restriction. Form 058-1 has no such field, so a set value excludes it.
     */
    @Column(name = "lab_confirmation")
    private Boolean labConfirmation;

    /** Only patients with this residence type; {@code null} - no restriction. */
    @Enumerated(EnumType.STRING)
    @Column(name = "population_type", length = 30)
    private ManualReportPopulationType populationType;

    /** {@code true} - only hospitalized cases, {@code false} - only not hospitalized; {@code null} - all. */
    @Column(name = "hospitalized")
    private Boolean hospitalized;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ref_manual_report_types",
            joinColumns = @JoinColumn(name = "manual_report_id"),
            indexes = @Index(name = "idx_ref_manual_report_types_report_id", columnList = "manual_report_id")
    )
    @BatchSize(size = 200)
    @Column(name = "report_type", length = 50)
    private Set<String> reportTypes = new HashSet<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ref_manual_report_icd10_codes",
            joinColumns = @JoinColumn(name = "manual_report_id"),
            indexes = {
                    @Index(name = "idx_ref_manual_report_icd10_report_id", columnList = "manual_report_id"),
                    @Index(name = "idx_ref_manual_report_icd10_code", columnList = "icd10_code")
            }
    )
    @Column(name = "icd10_code", length = 20)
    private Set<String> icd10Codes = new HashSet<>();

    /**
     * Only cases whose Form 058 disease place ({@code DISEASE_PLACE} catalog) is one of these;
     * empty - no restriction. Form 058-1 has no such field, so a non-empty set excludes it.
     */
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ref_manual_report_disease_places",
            joinColumns = @JoinColumn(name = "manual_report_id"),
            indexes = @Index(name = "idx_ref_manual_report_disease_places_report_id", columnList = "manual_report_id")
    )
    @BatchSize(size = 200)
    @Column(name = "disease_place_code", length = 64)
    private Set<String> diseasePlaceCodes = new HashSet<>();

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}
