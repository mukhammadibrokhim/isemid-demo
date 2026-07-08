package uz.uzinfocom.app.platform.reference.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.ExternallyIdentifiedEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * ICD-10 (MKB-10) classifier node, bulk-imported from an external source (WHO
 * ICD-10 hierarchy). Ids are assigned by that source, not by this application,
 * so parent/child cross-references survive re-import runs — see
 * {@link ExternallyIdentifiedEntity}.
 */
@Getter
@Setter
@Entity
@Table(
        name = "ref_mkb10",
        indexes = {
                @Index(name = "idx_ref_mkb10_parent_id", columnList = "parent_id"),
                @Index(name = "idx_ref_mkb10_code", columnList = "code"),
                @Index(name = "idx_ref_mkb10_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ref_mkb10_code", columnNames = "code")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Mkb10 extends ExternallyIdentifiedEntity {

    @Column(name = "secondary_id")
    private Long secondaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Mkb10 parent;

    @Column(name = "parent_id", insertable = false, updatable = false)
    private Long parentId;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Mkb10> children = new ArrayList<>();

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false)
    private int level;

    @Column(name = "is_last_level", nullable = false)
    private boolean lastLevel;

    @Column(name = "name_uz")
    private String nameUz;

    @Column(name = "name_uz_cyril")
    private String nameUzCyril;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_kaa")
    private String nameKaa;

    @Column(length = 2000)
    private String comment;

    @Builder.Default
    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit = 1;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}
