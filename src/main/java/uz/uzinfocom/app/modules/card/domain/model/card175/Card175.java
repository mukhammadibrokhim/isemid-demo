package uz.uzinfocom.app.modules.card.domain.model.card175;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.Card;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * "Card 175" — rabies/animal-bite exposure investigation card. Field
 * names are copied from the legacy {@code Card175} entity (verified against
 * the on-disk legacy source). Legacy fix: the 3 date-only fields used
 * {@code java.util.Date} + {@code @Temporal(DATE)} — every other entity in
 * this codebase uses {@code java.time}, so these are now {@link LocalDate}.
 * No child entities exist for this type in the legacy repo — this entity is
 * intentionally flat.
 */
@Getter
@Setter
@Entity
@Table(name = "card175")
public class Card175 extends Card {

    public Card175() {
        super(CardType.CARD175);
    }

    @Column(name = "time_of_epidemiological_investigation")
    private LocalDateTime timeOfEpidemiologicalInvestigation;

    @Column(name = "date_of_illness")
    private LocalDate dateOfIllness;

    @Column(name = "date_of_diagnosis_of_emergency_day")
    private LocalDateTime dateOfDiagnosisOfEmergencyDay;

    @Column(name = "date_of_final_diagnosis")
    private LocalDateTime dateOfFinalDiagnosis;

    @Column(name = "date_of_discharge_from_hospital")
    private LocalDate dateOfDischargeFromHospital;

    @Column(name = "date_of_vaccination")
    private LocalDate dateOfVaccination;

    @Column(name = "pathogen_type", length = 255)
    private String pathogenType;

    @CatalogCode("card175-where-patient-come")
    @Column(name = "where_patient_come_code", length = 64)
    private String wherePatientComeCode;

    @CatalogCode("card175-patient-come")
    @Column(name = "patient_come_code", length = 64)
    private String patientComeCode;

    @CatalogCode("card175-hospital-discharge-status")
    @Column(name = "hospital_discharge_status_code", length = 64)
    private String hospitalDischargeStatusCode;

    @CatalogCode("card175-transport-type")
    @Column(name = "transport_type_code", length = 64)
    private String transportTypeCode;

    @CatalogCode("card175-reason-of-leaving-home")
    @Column(name = "reason_of_leaving_home_code", length = 64)
    private String reasonOfLeavingHomeCode;

    @CatalogCode("card175-reason-of-late-hospitalization")
    @Column(name = "reason_of_late_hospitalization_code", length = 64)
    private String reasonOfLateHospitalizationCode;

    @CatalogCode("card175-diagnosis-confirmed")
    @Column(name = "diagnosis_confirmed_code", length = 64)
    private String diagnosisConfirmedCode;

    @CatalogCode("card175-information-about-vaccination")
    @Column(name = "information_about_vaccination_code", length = 64)
    private String informationAboutVaccinationCode;

    @Column(name = "information_about_last_vaccination", length = 500)
    private String informationAboutLastVaccination;

    @Column(name = "brief_epidemiological_comment", length = 1000)
    private String briefEpidemiologicalComment;

    @CatalogCode("card175-initial-diagnosis")
    @Column(name = "initial_diagnosis_code", length = 64)
    private String initialDiagnosisCode;

    @CatalogCode("card175-patient-identified")
    @Column(name = "patient_identified_code", length = 64)
    private String patientIdentifiedCode;

    @Column(name = "place_of_application", length = 500)
    private String placeOfApplication;

    @CatalogCode("card175-prevention-and-aid")
    @Column(name = "prevention_and_aid_code", length = 64)
    private String preventionAndAidCode;

    @Column(name = "name_of_medicine", length = 255)
    private String nameOfMedicine;

    @Column(name = "quantity_of_medicine")
    private Long quantityOfMedicine;

    @Column(name = "serial_number")
    private Long serialNumber;

    @Column(name = "vaccination_count")
    private Long vaccinationCount;

    @Column(name = "clinical_form", length = 255)
    private String clinicalForm;

    @ElementCollection
    @CollectionTable(name = "card175_part_of_injury", joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> partOfInjury = new ArrayList<>();

    @CatalogCode("card175-severity-of-illness")
    @Column(name = "severity_of_illness_code", length = 64)
    private String severityOfIllnessCode;

    @CatalogCode("card175-relevance-of-disease-to-profession")
    @Column(name = "relevance_of_disease_to_profession_code", length = 64)
    private String relevanceOfDiseaseToProfessionCode;

    @CatalogCode("card175-disease-spreader-type")
    @Column(name = "disease_spreader_type_code", length = 64)
    private String diseaseSpreaderTypeCode;

    @CatalogCode("card175-owner-of-disease-spreader")
    @Column(name = "owner_of_disease_spreader_code", length = 64)
    private String ownerOfDiseaseSpreaderCode;

    @CatalogCode("card175-observation-result-of-animals")
    @Column(name = "observation_result_of_animals_code", length = 64)
    private String observationResultOfAnimalsCode;

    @CatalogCode("card175-checking-diagnosis")
    @Column(name = "checking_diagnosis_code", length = 64)
    private String checkingDiagnosisCode;

    @ElementCollection
    @CollectionTable(name = "card175_disease_transmission_condition", joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> diseaseTransmissionConditionCode = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "card175_pathogen_main_factor", joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> pathogenMainFactor = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "card175_taken_measures_from_residence", joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> takenMeasuresFromResidence = new ArrayList<>();
}
