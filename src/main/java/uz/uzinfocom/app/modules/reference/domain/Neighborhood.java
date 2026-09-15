package uz.uzinfocom.app.modules.reference.domain;

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
import uz.uzinfocom.app.modules.reference.domain.base.ReferenceDictionaryEntity;

@Getter
@Setter
@Entity
@Table(
        name = "ref_neighborhood",
        indexes = {
                @Index(name = "idx_ref_neighborhood_code", columnList = "code"),
                @Index(name = "idx_ref_neighborhood_parent_code", columnList = "parent_code"),
                @Index(name = "idx_ref_neighborhood_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ref_neighborhood_code", columnNames = "code")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Neighborhood extends ReferenceDictionaryEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "parent_code", nullable = false, length = 50)
    private String parentCode;

    @Column(name = "soato_id", nullable = false)
    private Integer soatoId;

    @Column(name = "parent_soato_id", nullable = false)
    private Integer parentSoatoId;

    @Column(name = "tin", length = 20)
    private String tin;

    /**
     * The uzcad source registry's own neighborhood code (e.g. "103-0105" or "23010008") -
     * distinct from {@code soato_id} (that registry's SOATO id) and from {@code code} (this
     * app's own internal code). Matches the {@code Guid} api2's v3/citizenAddress sends for a
     * Maxalla, which carries neither soato_id nor tin - see CitizenAddressMapper.
     */
    @Column(name = "uzcad_registry_code", length = 20)
    private String uzcadRegistryCode;

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
