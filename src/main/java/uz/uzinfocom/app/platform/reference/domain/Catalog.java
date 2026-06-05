package uz.uzinfocom.app.platform.reference.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.reference.domain.base.ReferenceDictionaryEntity;
import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

@Getter
@Setter
@Entity
@Table(
        name = "ref_catalog",
        indexes = {
                @Index(name = "idx_ref_catalog_type_deleted", columnList = "type, deleted"),
                @Index(name = "idx_ref_catalog_type_parent_code", columnList = "type, parent_code")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ref_catalog_type_code", columnNames = {"type", "code"})
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Catalog extends ReferenceDictionaryEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private CatalogType type;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "parent_code", length = 50)
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
