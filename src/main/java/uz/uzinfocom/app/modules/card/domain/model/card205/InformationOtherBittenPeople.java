package uz.uzinfocom.app.modules.card.domain.model.card205;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

import java.time.LocalDateTime;

/**
 * Information about a person other than the primary patient bitten by the
 * same animal. Legacy field {@code birthDate} is a bare {@code String}
 * (not a date type) — kept as-is since this is a field-type quirk from the
 * legacy source, not one of the documented legacy mistakes to fix.
 */
@Getter
@Setter
@Entity
@Table(
        name = "card205_info_bitten_people",
        indexes = @Index(name = "idx_card205_info_bitten_people_card205_id", columnList = "card205_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class InformationOtherBittenPeople extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card205_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card205_info_bitten_people_card205"))
    private Card205 card205;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "middle_name", length = 255)
    private String middleName;

    @Column(name = "gender", length = 32)
    private String gender;

    @Column(name = "birth_date", length = 32)
    private String birthDate;

    @Column(name = "living_address", length = 500)
    private String livingAddress;

    @Embedded
    private AdministrativeAddress location;

    @Column(name = "bitten_date")
    private LocalDateTime bittenDate;
}
