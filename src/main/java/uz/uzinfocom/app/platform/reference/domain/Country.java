package uz.uzinfocom.app.platform.reference.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.reference.domain.base.ReferenceDictionaryEntity;

@Getter
@Setter
@Entity
@Table(
        name = "ref_country",
        indexes = {
                @Index(name = "idx_ref_country_code", columnList = "code"),
                @Index(name = "idx_ref_country_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ref_country_code", columnNames = "code")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Country extends ReferenceDictionaryEntity {

    @Column(nullable = false, length = 3)
    private String code;

    @Column(name = "alpha2_code", length = 2)
    private String alpha2Code;

    @Column(name = "iso_3166_2_code", length = 32)
    private String iso3166Part2Code;

    @Column(name = "citizenship_code", length = 32)
    private String citizenshipCode;

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
