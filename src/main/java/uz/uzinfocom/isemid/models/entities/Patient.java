package uz.uzinfocom.isemid.models.entities;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;

import java.time.LocalDate;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Patient extends AbsEntity {
    private String firstName;
    private String lastName;
    private String middleName;

    @JsonManagedReference
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Identifier> identifiers;

    private LocalDate birthDate;
    private Gender gender;
    private String stateCode;
    private String cityCode;
    private String neighborhood;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
