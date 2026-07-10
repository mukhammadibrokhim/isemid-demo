package uz.uzinfocom.app.modules.form0581.domain.model;

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
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581Address;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

/**
 * A person other than the primary victim who was also injured in the same
 * incident (not by the animal itself — see {@link Form0581#getOtherPeopleInjured()}).
 * Repeatable (0..N), gated by that boolean flag.
 */
@Getter
@Setter
@Entity
@Table(
        name = "form058_1_other_injured_person",
        indexes = @Index(name = "idx_form0581_other_injured_person_form0581_id", columnList = "form0581_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Form0581OtherInjuredPerson extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "form0581_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_form0581_other_injured_person_form0581")
    )
    private Form0581 form0581;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "middle_name", length = 255)
    private String middleName;

    @Embedded
    private Form0581Address address;
}
