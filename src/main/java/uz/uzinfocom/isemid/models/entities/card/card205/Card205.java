package uz.uzinfocom.isemid.models.entities.card.card205;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Card205 extends Card {

    private String mkb10Code;
    private String mkb10Name; //Jarohat joyi MKB 10 spravchnikdan

    @Temporal(value = TemporalType.DATE)
    private Date epidemiologicalObservationDate; //Epidemiologik surishtiruv o‘tkazilgan sana.

    @Temporal(value = TemporalType.DATE)
    private Date veterinaryEmergencyInformationDate; //Hududiy veterinariya xizmatiga shoshilinch xabar bergan sana.

    private String addressOfBiteOccurrence;//Tishlanish sodir bo‘lgan manzili.

    @Temporal(value = TemporalType.DATE) //Tishlanish sodir bo‘lgan sana.
    private Date dateOfBiteOccurrence;
    private String nameOfTreatmentPreventiveInstitution; //Jabrlanganning birinchi bor tibbiy yordamga murojaat qilgan DPM nomi.

    @Temporal(value = TemporalType.DATE)
    private Date dateOfTreatmentPreventiveInstitution; //Jabrlanganning birinchi bor tibbiy yordam uchun DPMga murojaat qilgan sana.

    //Ушбу ҳайвон томонидан тишланган бошқа шахслар тўғрисида маълумот
    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"card205"})
    @Builder.Default
    private List<InformationOtherBittenPeople> infoBittenPeople = new ArrayList<>();
    private String animalType;

    private String whereAnimalComesFrom;
    private String whenAnimalAppeared;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog conditionOfAnimal;
    private Integer ageOfAnimal;
    private String breedOfAnimal;
    private String colourOfAnimal;
    private String individualSignsOfAnimal;

    private Integer certificateNumberOfFirstVetResults;

    @Temporal(value = TemporalType.DATE)
    private Date issueDateOfFirstCertificate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog animalConservation;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog positionOfBittenVictim;
    private Integer certificateNumberOfSecondVetResults;
    private Date issueDateOfSecondaryCertificate;
    private String dateTimeOfFeatherTaken;
    private String petRegisteredVetDepartment;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog dogOwnerCompliance;


    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card205"})
    @Builder.Default
    private List<InformationOtherBittenAnimals> infoOtherBittenAnimal = new ArrayList<>();

    @OneToMany(mappedBy = "card205", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"card205"})
    @Builder.Default
    private List<InformationAboutAnimaBittenPeople> infoAbtAnimalBittenPeople = new ArrayList<>();

    private String additionalInformation;

    private String fullNameofAnimalOwner;
    // DMED, Emlash.uz integratsiya


}
