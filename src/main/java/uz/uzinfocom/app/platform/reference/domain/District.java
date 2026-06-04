package uz.uzinfocom.app.platform.reference.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Table(
        name = "ref_district",
        indexes = {
                @Index(name = "idx_ref_district_code", columnList = "code"),
                @Index(name = "idx_ref_district_parent_code", columnList = "parent_code"),
                @Index(name = "idx_ref_district_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ref_district_code", columnNames = "code")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class District extends ReferenceDictionaryEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "parent_code", nullable = false, length = 50)
    private String parentCode;

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
