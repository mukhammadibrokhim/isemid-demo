package uz.uzinfocom.isemid.features.patient.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "patient",
        indexes = {
                @Index(name = "idx_patient_fullname", columnList = "lastName, firstName, middleName"),
                @Index(name = "idx_patient_gender", columnList = "genderCode"),
                @Index(name = "idx_patient_created_at", columnList = "created_at"),
        }
)@EntityListeners(AuditingEntityListener.class)
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

    @JsonManagedReference
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Affiliation> affiliations;

    @JsonManagedReference
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Address> addresses;

    public String getFullName() {
        return String.format("%s %s %s", firstName, lastName, middleName);
    }
}
