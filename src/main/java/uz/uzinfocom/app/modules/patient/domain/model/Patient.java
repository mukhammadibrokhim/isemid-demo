package uz.uzinfocom.app.modules.patient.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patient")
public class Patient extends AbsEntity {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "age_years")
    private Integer ageYears;

    @Column(name = "age_months")
    private Integer ageMonths;

    @Column(name = "gender_code")
    private String genderCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "kinship_degree")
    private String kinshipDegree;

    @Column(name = "kinship_full_name")
    private String kinshipFullName;

    @Column(name = "residential_status_code")
    private String residentialStatusCode;

    @Column(name = "marital_status_code")
    private String maritalStatusCode;

    @Column(name = "population_type_code")
    private String populationTypeCode;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "profession_code")
    private String professionCode;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    @OneToMany(
            mappedBy = "patient",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<PatientIdentifier> identifiers = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @Builder.Default
    @OneToMany(
            mappedBy = "patient",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<PatientAddress> addresses = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @Builder.Default
    @OneToMany(
            mappedBy = "patient",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<PatientAffiliation> affiliations = new ArrayList<>();

    public void addIdentifier(PatientIdentifier identifier) {
        if (identifier == null) {
            return;
        }

        identifiers.add(identifier);
        identifier.setPatient(this);
    }

    public void addAddress(PatientAddress address) {
        if (address == null) {
            return;
        }

        addresses.add(address);
        address.setPatient(this);
    }

    public PatientAddress getPermanentAddress() {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        return addresses.stream()
                .filter(address -> address.getType() == AddressType.PERMANENT)
                .findFirst()
                .orElse(null);
    }

    public void addAffiliation(PatientAffiliation affiliation) {
        if (affiliation == null) {
            return;
        }

        affiliations.add(affiliation);
        affiliation.setPatient(this);
    }

    public void removeIdentifier(PatientIdentifier identifier) {
        if (identifier != null && identifiers.remove(identifier)) {
            identifier.setPatient(null);
        }
    }

    public void removeAddress(PatientAddress address) {
        if (address != null && addresses.remove(address)) {
            address.setPatient(null);
        }
    }

    public void removeAffiliation(PatientAffiliation affiliation) {
        if (affiliation != null && affiliations.remove(affiliation)) {
            affiliation.setPatient(null);
        }
    }
}