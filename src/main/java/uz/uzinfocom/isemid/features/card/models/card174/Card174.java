package uz.uzinfocom.isemid.features.card.models.card174;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.card.models.Card;

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

    private String affectedAnimalTypeCode; // Kasallangan yoki o‘lgan (o‘ldirilgan) hayvonlarning turi
    private Integer affectedAnimalCount; // Kasallangan yoki o‘lgan (o‘ldirilgan) hayvonlarning soni
    private String animalOwnershipCode; // Hayvonlarni kimga tegishliligi

    private boolean isAreaExotic; // Ushbu hudud kasallik bo‘yicha ekzotik
    private boolean rodentIncrease; // Kemiruvchilar sonining ortishi
    private boolean vectorIncrease; // Tashuvchilar soni ortishi
    private boolean wildRodentsIncrease; // Kemiruvchilar soni ortdi (yovvoyi)
    private boolean synanthropicRodentsIncrease; // Kemiruvchilar soni ortdi (sinantrop)
    private boolean bloodSuckingArthropodsIncrease; // Bo‘g‘im oyoqli qon so‘ruvchilar soni ortdi
    private boolean epizootologyExistence;

    @ElementCollection
    @CollectionTable(
            name = "card174_disease_factors",
            joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> diseaseFactorCodes = new ArrayList<>();

    private String animalType; // Hayvon turi, tekshirilib o'tkazilgan namunalar turi
    private LocalDate testDate; // Tekshiruv sanasi
    private Integer testSampleCount; // Tekshirilgan namunalar soni
    private String testingMethod; // Tekshiruv usuli
    private String testResult; // Natija (serovar turi, qo‘zg‘atuvchi, titr)

    @ElementCollection
    @CollectionTable(
            name = "card174_affected_animals",
            joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> affectedAnimalCodes = new ArrayList<>();


    private Integer affectedHumans; // Kasallangan odamlar soni
    private Integer includingIndustrialConditions;// Шу жумладан саноат шароитида
    private Integer includingWhoApplied; //Shu jumladan murojaat etganlar.
    private Integer includingIdentified; //Shu jumladan aniqlanganlar.
    private Integer treatedHumans; // Tezkor yordam yoki antirabik yordam olganlar soni
    private Integer affectedInOutbreak; // O‘choqda kasallangan odamlar soni

    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    @Builder.Default
    private List<InfectionMonitoring> infectionMonitoring = new ArrayList<>();

    private String quarantineTypeCode;

    private LocalDate quarantineStartDate;
    private LocalDate quarantineEndDate;

    private String animalDisposalMethodCode;

    private LocalDate animalDisposalDate;
    private String precautionaryMeasures;
    private String strayAnimalCapture;
    private String wildAnimalCulling;

    private String deratizationCode;

    private Double deratizationArea;
    private String inspectors;
    private String isolation;
    private String meatSubmission;
    private String treatment;
    private Boolean measureTaken;

    @ElementCollection
    @CollectionTable(
            name = "card174_disinfection_factors",
            joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> disinfectionTransmissionFactorCodes = new ArrayList<>();

    private Integer disinfectedFactorAmount;//Zararsizlantirilgan omillar miqdori.
    private LocalDate disinfectionDate;//Zararsizlantirish sanasi.

    @ElementCollection
    @CollectionTable(
            name = "card174_elimination_method",
            joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> eliminationMethodCodes = new ArrayList<>();

    private String locationOfEvent;
    private String executionControlResults;

    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card174"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<OutbreakControlMeasure> outbreakControlMeasures = new ArrayList<>();

    private String additionalMeasuresInfo;

}