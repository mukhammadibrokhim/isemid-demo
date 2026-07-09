package uz.uzinfocom.app.modules.card.domain.model.card205;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.Card;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * "Card 205" — animal-bite (rabies exposure) investigation card. Field
 * names are copied from the legacy {@code Card205} entity (verified against
 * the on-disk legacy source), with fixes applied:
 * <ul>
 *   <li>Every {@code java.util.Date}/{@code @Temporal(DATE)} field is now a
 *   {@link LocalDate}, matching the rest of this codebase.</li>
 *   <li>All 3 child collections are now {@code orphanRemoval = true}.
 *   Legacy had this on only one of the three ({@code infoOtherBittenAnimal})
 *   even though every child FK is {@code nullable = false} — without
 *   orphanRemoval, removing an entry from the other two collections would
 *   have made Hibernate try to null out a non-nullable FK instead of
 *   deleting the row, i.e. a guaranteed constraint violation on update.</li>
 * </ul>
 */
@Getter
@Setter
@Entity
@Table(name = "card205")
public class Card205 extends Card {

    public Card205() {
        super(CardType.CARD205);
    }

    @CatalogCode("mkb10")
    @Column(name = "mkb10_code", length = 64)
    private String mkb10Code;

    @Column(name = "mkb10_name", length = 500)
    private String mkb10Name;

    @Column(name = "epidemiological_observation_date")
    private LocalDate epidemiologicalObservationDate;

    @Column(name = "veterinary_emergency_information_date")
    private LocalDate veterinaryEmergencyInformationDate;

    @Column(name = "address_of_bite_occurrence", length = 500)
    private String addressOfBiteOccurrence;

    @Column(name = "date_of_bite_occurrence")
    private LocalDate dateOfBiteOccurrence;

    @Column(name = "name_of_treatment_preventive_institution", length = 255)
    private String nameOfTreatmentPreventiveInstitution;

    @Column(name = "date_of_treatment_preventive_institution")
    private LocalDate dateOfTreatmentPreventiveInstitution;

    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<InformationOtherBittenPeople> infoBittenPeople = new ArrayList<>();

    @Column(name = "animal_type", length = 255)
    private String animalType;

    @Column(name = "where_animal_comes_from", length = 500)
    private String whereAnimalComesFrom;

    @Column(name = "when_animal_appeared", length = 255)
    private String whenAnimalAppeared;

    @CatalogCode("card205-condition-of-animal")
    @Column(name = "condition_of_animal_code", length = 64)
    private String conditionOfAnimalCode;

    @Column(name = "age_of_animal")
    private Integer ageOfAnimal;

    @Column(name = "breed_of_animal", length = 255)
    private String breedOfAnimal;

    @Column(name = "colour_of_animal", length = 255)
    private String colourOfAnimal;

    @Column(name = "individual_signs_of_animal", length = 500)
    private String individualSignsOfAnimal;

    @Column(name = "certificate_number_of_first_vet_results")
    private Integer certificateNumberOfFirstVetResults;

    @Column(name = "issue_date_of_first_certificate")
    private LocalDate issueDateOfFirstCertificate;

    @CatalogCode("card205-animal-conservation")
    @Column(name = "animal_conservation_code", length = 64)
    private String animalConservationCode;

    @CatalogCode("card205-position-of-bitten-victim")
    @Column(name = "position_of_bitten_victim_code", length = 64)
    private String positionOfBittenVictimCode;

    @Column(name = "certificate_number_of_second_vet_results")
    private Integer certificateNumberOfSecondVetResults;

    @Column(name = "issue_date_of_secondary_certificate")
    private LocalDate issueDateOfSecondaryCertificate;

    @Column(name = "date_time_of_feather_taken", length = 255)
    private String dateTimeOfFeatherTaken;

    @Column(name = "pet_registered_vet_department", length = 255)
    private String petRegisteredVetDepartment;

    @CatalogCode("card205-dog-owner-compliance")
    @Column(name = "dog_owner_compliance_code", length = 64)
    private String dogOwnerComplianceCode;

    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<InformationOtherBittenAnimals> infoOtherBittenAnimal = new ArrayList<>();

    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<InformationAboutAnimaBittenPeople> infoAbtAnimalBittenPeople = new ArrayList<>();

    @Column(name = "additional_information", length = 1000)
    private String additionalInformation;

    @Column(name = "full_name_of_animal_owner", length = 255)
    private String fullNameofAnimalOwner;
}
