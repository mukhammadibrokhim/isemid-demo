package uz.uzinfocom.isemid.features.form0581.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "fm0581_animal_owner")
@EntityListeners(AuditingEntityListener.class)
public class Form0581AnimalOwner extends BaseEntity {
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
