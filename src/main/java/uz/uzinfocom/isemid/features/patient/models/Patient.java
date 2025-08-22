package uz.uzinfocom.isemid.features.patient.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

import java.time.LocalDate;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    private String genderCode;
    private String phoneNumber;
    private String kinshipDegree; // Example: "Father", "Mother", "Sibling", etc.
    private String kinshipFullName;
    private String residentialStatusCode;
    private String maritalStatusCode;
    private String populationTypeCode;
    private String categoryCode;
    private String professionCode;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Affiliation> affiliations;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Address> addresses;
}
