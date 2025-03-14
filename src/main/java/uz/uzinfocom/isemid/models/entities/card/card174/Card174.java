package uz.uzinfocom.isemid.models.entities.card.card174;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Card174 extends Card {
    private Integer serialDocNumber; //Hujjat tartib raqami.
    private String mkb10Code;
    private String mkb10Name;
    private String pathogenType; // Qo‘zg‘atuvchisining turi (serogruppa)
    private LocalDate dataObtainedDate; // Hayvonlar kasalligi yoki kasallikka shubha tug‘ilganda olingan ma’lumot sanasi
    private LocalDate reportToVeterinaryDepartmentDate; // Tuman Vetenariya bo‘limiga ma’lumot berilgan sana
    private String animalPrimaryDiagnosis; // Hayvondagi birlamchi tashxisi (dastlabki)
    private String humanPrimaryDiagnosis; // Odamdagi birlamchi tashxisi (dastlabki) //MKB 10 spravchnik
    private LocalDate investigationDate; // Epizootologik-epidemiologik tekshiruv o‘tkazilgan sana
    private LocalDate lastDiseaseYear; // Hayvonlar va odamlar orasida kasallik qayd etilgan so‘ngi yil
    private LocalDate currentAnimalInfectionDate; // Hayvon(lar)ning hozirgi kasallangan sanasi
    private String outbreakLocalization; // O‘chog‘ni lokalizatsiyasi (xo‘jalik, aholi punkti, korxona sex)
    private String animalOwner; // Hayvon egasi, Familiya, Ismi Sharifi
    private String ownerAddress; // Manzilgohi

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "affected_animal_type_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog affectedAnimalType; // Kasallangan yoki o‘lgan (o‘ldirilgan) hayvonlarning turi
    private Integer affectedAnimalCount; // Kasallangan yoki o‘lgan (o‘ldirilgan) hayvonlarning soni
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "animal_ownership_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog animalOwnership; // Hayvonlarni kimga tegishliligi
    private boolean isAreaExotic; // Ushbu hudud kasallik bo‘yicha ekzotik
    private boolean rodentIncrease; // Kemiruvchilar sonining ortishi
    private boolean vectorIncrease; // Tashuvchilar soni ortishi
    private boolean wildRodentsIncrease; // Kemiruvchilar soni ortdi (yovvoyi)
    private boolean synanthropicRodentsIncrease; // Kemiruvchilar soni ortdi (sinantrop)
    private boolean bloodSuckingArthropodsIncrease; // Bo‘g‘im oyoqli qon so‘ruvchilar soni ortdi
    private boolean epizootologyExistence;
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "card174_disease_factors",
            joinColumns = @JoinColumn(name = "card174_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private List<Catalog> diseaseFactors = new ArrayList<>(); // Bu kasallikning kelib chiqishiga sabab bo‘lgan faktorlar
    private String animalType; // Hayvon turi, tekshirilib o'tkazilgan namunalar turi
    private LocalDate testDate; // Tekshiruv sanasi
    private Integer testSampleCount; // Tekshirilgan namunalar soni
    private String testingMethod; // Tekshiruv usuli
    private String testResult; // Natija (serovar turi, qo‘zg‘atuvchi, titr)
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "card174_affected_animals",
            joinColumns = @JoinColumn(name = "card174_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private List<Catalog> affectedAnimals = new ArrayList<>(); // Kasallangan hayvonlar soni
    private Integer affectedHumans; // Kasallangan odamlar soni
    private Integer includingIndustrialConditions;// Шу жумладан саноат шароитида
    private Integer includingWhoApplied; //Shu jumladan murojaat etganlar.
    private Integer includingIdentified; //Shu jumladan aniqlanganlar.
    private Integer treatedHumans; // Tezkor yordam yoki antirabik yordam olganlar soni
    private Integer affectedInOutbreak; // O‘choqda kasallangan odamlar soni
    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card174"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<InfectionMonitoring> infectionMonitoring = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "quarantine_type_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog quarantineType;
    private LocalDate quarantineStartDate;
    private LocalDate quarantineEndDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "quarantine_disposal_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog animalDisposalMethod;
    private LocalDate animalDisposalDate;
    private String precautionaryMeasures;
    private String strayAnimalCapture;
    private String wildAnimalCulling;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "deratization_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog deratization;
    private Double deratizationArea;
    private String inspectors;
    private String isolation;
    private String meatSubmission;
    private String treatment;
    private Boolean measureTaken;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "card174_disinfection_factors",
            joinColumns = @JoinColumn(name = "card174_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private List<Catalog> disinfectionTransmissionFactors = new ArrayList<>();//Infeksiyani uzatish omillarini zararsizlantirish.
    private Integer disinfectedFactorAmount;//Zararsizlantirilgan omillar miqdori.
    private LocalDate disinfectionDate;//Zararsizlantirish sanasi.
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "card174_elimination_method",
            joinColumns = @JoinColumn(name = "card174_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private List<Catalog> eliminationMethod = new ArrayList<>();//Yo‘qotish usuli.
    private String locationOfEvent;
    private String executionControlResults;
    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card174"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<OutbreakControlMeasure> outbreakControlMeasures = new ArrayList<>();
    private String additionalMeasuresInfo;

}