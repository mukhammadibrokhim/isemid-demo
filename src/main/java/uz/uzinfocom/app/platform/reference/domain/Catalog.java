package uz.uzinfocom.app.platform.reference.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.reference.domain.base.ReferenceDictionaryEntity;

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

    @Column(nullable = false, length = 100)
    private String type;

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

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}
