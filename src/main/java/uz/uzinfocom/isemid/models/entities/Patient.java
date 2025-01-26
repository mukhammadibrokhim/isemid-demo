package uz.uzinfocom.isemid.models.entities;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.entities.enums.MaritalStatus;
import uz.uzinfocom.isemid.models.entities.enums.PopulationType;
import uz.uzinfocom.isemid.models.entities.enums.ResidentialStatus;
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
    @OrderBy("id DESC")
    private List<Identifier> identifiers;

    private LocalDate birthDate;
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private ResidentialStatus residentialStatus;
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    private PopulationType populationType;
    private String workPlace;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Address> addresses;
}
