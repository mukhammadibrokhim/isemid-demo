package uz.uzinfocom.isemid.features.form0581.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "fm0581_bitten_person",
        indexes = {
                @Index(name = "idx_fm0581_bitten_person_form", columnList = "form0581_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Form0581BittenPerson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form0581_id", nullable = false)
    @JsonBackReference("form0581-bittenPersons")
    private Form0581 form0581;

    private String lastName;
    private String firstName;
    private String middleName;

    private String stateCode;
    private String cityCode;
    private String neighborhoodCode;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
