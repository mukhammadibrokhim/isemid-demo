package uz.uzinfocom.app.modules.reference.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
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

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ref_manual_report_types",
            joinColumns = @JoinColumn(name = "manual_report_id"),
            indexes = @Index(name = "idx_ref_manual_report_types_report_id", columnList = "manual_report_id")
    )
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

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}
