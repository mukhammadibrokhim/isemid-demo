package uz.uzinfocom.app.modules.card.domain.model.card174;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
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
 * "Card 174" — zoonotic/animal-borne disease investigation card. Field
 * names/types are copied from the legacy {@code Card174} entity (verified
 * against the on-disk legacy source), with the fixes listed in the plan
 * document applied — in particular every legacy primitive {@code boolean}
 * flag below is now a nullable {@code Boolean}.
 */
@Getter
@Setter
@Entity
@Table(name = "card174")
public class Card174 extends Card {

    public Card174() {
        super(CardType.CARD174);
    }

    @Column(name = "serial_doc_number")
    private Integer serialDocNumber;

    @CatalogCode("mkb10")
    @Column(name = "mkb10_code", length = 64)
    private String mkb10Code;

    @Column(name = "mkb10_name", length = 500)
    private String mkb10Name;

    @Column(name = "pathogen_type", length = 255)
    private String pathogenType;

    @Column(name = "data_obtained_date")
    private LocalDate dataObtainedDate;

    @Column(name = "report_to_veterinary_department_date")
    private LocalDate reportToVeterinaryDepartmentDate;

    @Column(name = "animal_primary_diagnosis", length = 500)
    private String animalPrimaryDiagnosis;

    @Column(name = "human_primary_diagnosis", length = 500)
    private String humanPrimaryDiagnosis;

    @Column(name = "investigation_date")
    private LocalDate investigationDate;

    @Column(name = "last_disease_year")
    private LocalDate lastDiseaseYear;

    @Column(name = "current_animal_infection_date")
    private LocalDate currentAnimalInfectionDate;

    @Column(name = "outbreak_localization", length = 500)
    private String outbreakLocalization;

    @Column(name = "animal_owner", length = 255)
    private String animalOwner;

    @Column(name = "owner_address", length = 500)
    private String ownerAddress;

    @CatalogCode("card174-affected-animal-type")
    @Column(name = "affected_animal_type_code", length = 64)
    private String affectedAnimalTypeCode;

    @Column(name = "affected_animal_count")
    private Integer affectedAnimalCount;

    @CatalogCode("card174-animal-ownership")
    @Column(name = "animal_ownership_code", length = 64)
    private String animalOwnershipCode;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "is_area_exotic")
    private Boolean isAreaExotic;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "rodent_increase")
    private Boolean rodentIncrease;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "vector_increase")
    private Boolean vectorIncrease;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "wild_rodents_increase")
    private Boolean wildRodentsIncrease;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "synanthropic_rodents_increase")
    private Boolean synanthropicRodentsIncrease;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "blood_sucking_arthropods_increase")
    private Boolean bloodSuckingArthropodsIncrease;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "epizootology_existence")
    private Boolean epizootologyExistence;

    @ElementCollection
    @CollectionTable(name = "card174_disease_factors", joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> diseaseFactorCodes = new ArrayList<>();

    @Column(name = "animal_type", length = 255)
    private String animalType;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "test_sample_count")
    private Integer testSampleCount;

    @Column(name = "testing_method", length = 255)
    private String testingMethod;

    @Column(name = "test_result", length = 500)
    private String testResult;

    @ElementCollection
    @CollectionTable(name = "card174_affected_animals", joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> affectedAnimalCodes = new ArrayList<>();

    @Column(name = "affected_humans")
    private Integer affectedHumans;

    @Column(name = "including_industrial_conditions")
    private Integer includingIndustrialConditions;

    @Column(name = "including_who_applied")
    private Integer includingWhoApplied;

    @Column(name = "including_identified")
    private Integer includingIdentified;

    @Column(name = "treated_humans")
    private Integer treatedHumans;

    @Column(name = "affected_in_outbreak")
    private Integer affectedInOutbreak;

    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<InfectionMonitoring> infectionMonitoring = new ArrayList<>();

    @CatalogCode("card174-quarantine-type")
    @Column(name = "quarantine_type_code", length = 64)
    private String quarantineTypeCode;

    @Column(name = "quarantine_start_date")
    private LocalDate quarantineStartDate;

    @Column(name = "quarantine_end_date")
    private LocalDate quarantineEndDate;

    @CatalogCode("card174-animal-disposal-method")
    @Column(name = "animal_disposal_method_code", length = 64)
    private String animalDisposalMethodCode;

    @Column(name = "animal_disposal_date")
    private LocalDate animalDisposalDate;

    @Column(name = "precautionary_measures", length = 1000)
    private String precautionaryMeasures;

    @Column(name = "stray_animal_capture", length = 500)
    private String strayAnimalCapture;

    @Column(name = "wild_animal_culling", length = 500)
    private String wildAnimalCulling;

    @CatalogCode("card174-deratization")
    @Column(name = "deratization_code", length = 64)
    private String deratizationCode;

    @Column(name = "deratization_area")
    private Double deratizationArea;

    @Column(name = "inspectors", length = 500)
    private String inspectors;

    @Column(name = "isolation", length = 500)
    private String isolation;

    @Column(name = "meat_submission", length = 500)
    private String meatSubmission;

    @Column(name = "treatment", length = 500)
    private String treatment;

    @Column(name = "measure_taken")
    private Boolean measureTaken;

    @ElementCollection
    @CollectionTable(name = "card174_disinfection_factors", joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> disinfectionTransmissionFactorCodes = new ArrayList<>();

    @Column(name = "disinfected_factor_amount")
    private Integer disinfectedFactorAmount;

    @Column(name = "disinfection_date")
    private LocalDate disinfectionDate;

    @ElementCollection
    @CollectionTable(name = "card174_elimination_method", joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> eliminationMethodCodes = new ArrayList<>();

    @Column(name = "location_of_event", length = 500)
    private String locationOfEvent;

    @Column(name = "execution_control_results", length = 1000)
    private String executionControlResults;

    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<OutbreakControlMeasure> outbreakControlMeasures = new ArrayList<>();

    @Column(name = "additional_measures_info", length = 1000)
    private String additionalMeasuresInfo;
}
