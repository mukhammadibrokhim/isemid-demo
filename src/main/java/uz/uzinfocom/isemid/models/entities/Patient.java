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
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

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
    private Integer ageYears;
    private Integer ageMonths;
    private Gender gender;
    private String phoneNumber;
    private String kinshipDegree; // Example: "Father", "Mother", "Sibling", etc.
    private String kinshipFullName;

    @Enumerated(EnumType.STRING)
    private ResidentialStatus residentialStatus;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    private PopulationType populationType;

    @ManyToOne
    @JoinColumn(name = "category", referencedColumnName = "code")
    private Catalog category;

    @ManyToOne
    @JoinColumn(name = "profession", referencedColumnName = "code")
    private Catalog profession;

    private String workPlace;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Address> addresses;
}
