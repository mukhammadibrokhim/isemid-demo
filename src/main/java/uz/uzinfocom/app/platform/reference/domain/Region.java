package uz.uzinfocom.app.platform.reference.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.reference.domain.base.ReferenceDictionaryEntity;

@Getter
@Setter
@Entity
@Table(
        name = "ref_region",
        indexes = {
                @Index(name = "idx_ref_region_code", columnList = "code"),
                @Index(name = "idx_ref_region_parent_code", columnList = "parent_code"),
                @Index(name = "idx_ref_region_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ref_region_code", columnNames = "code")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Region extends ReferenceDictionaryEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "parent_code", nullable = false, length = 50)
    private String parentCode;

    @Column(name = "soato_id", nullable = false)
    private Integer soatoId;

    @Column(name = "legacy_soato_id", nullable = false)
    private Integer legacySoatoId;

    @Column(name = "name_uz")
    private String nameUz;

    @Column(name = "name_uz_cyril")
    private String nameUzCyril;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_kaa")
    private String nameKaa;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}
