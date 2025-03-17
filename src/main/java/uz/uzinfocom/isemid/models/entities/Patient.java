package uz.uzinfocom.isemid.models.entities;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
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

    @ManyToOne
    @JoinColumn(name = "Gender", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog gender;

    private String phoneNumber;
    private String kinshipDegree; // Example: "Father", "Mother", "Sibling", etc.
    private String kinshipFullName;

    @ManyToOne
    @JoinColumn(name = "residential_status", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog residentialStatus;

    @ManyToOne
    @JoinColumn(name = "marital_status", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog maritalStatus;

    @ManyToOne
    @JoinColumn(name = "population_type", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog populationType;

    @ManyToOne
    @JoinColumn(name = "category", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog category;

    @ManyToOne
    @JoinColumn(name = "profession", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog profession;

    private String workPlace;

    @ManyToOne
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog workPlaceState;

    @ManyToOne
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog workPlaceCity;

    @ManyToOne
    @JoinColumn(name = "work_place_org_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"children", "serviceArea", "city", "state", "country", "level","children","users"})
    private Organization workPlaceOrg;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Address> addresses;
}
