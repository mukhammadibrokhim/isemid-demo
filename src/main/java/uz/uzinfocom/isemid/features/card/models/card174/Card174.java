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
    private String mkbCode; // MKB-10 kodi
    private String mkbName; // MKB-10 nomi
    private String pathogen; // Qo‘zg‘atuvchi turi (serogruppa)
    private LocalDate dataDate; // Ma’lumot olingan sana (hayvon kasalligida)
    private LocalDate reportedSanepidDate; //Tuman Sanitariya Epidemiologiya Qo‘mitasiga ma’lumot berilagn sana.
    private LocalDate reportedVetDate; // Tuman veterinariyaga xabar berilgan sana
    private LocalDate reportedHumanVetDate;
    private String animalDiag; // Hayvonlardagi birlamchi tashxis
    //humanDiag Mkb10 bilan
    private String humanDiag; // Odamlardagi birlamchi tashxis (MKB bilan)
    private LocalDate investigationDate; // Tekshiruv sanasi
    private LocalDate lastCaseYear; // So‘nggi kasallik aniqlangan yil
    private LocalDate currentAnimalInfectionDate; // Hozirgi hayvon infeksiya sanasi
    private String outbreakLocation; // O‘chog‘ joyi (xo‘jalik, korxona, aholi punkti)
    private String ownerName; // Hayvon egasining to‘liq FISH
    private String ownerAddr; // Hayvon egasining manzili

    private String affectedAnimalCode; // Kasallangan hayvon turi kodi
    private Integer affectedAnimalCount; // Kasallangan hayvonlar soni
    private String ownershipCode; // Hayvon tegishliligi

    private boolean isExoticArea; // Hudud ekzotikmi
    private boolean rodentsIncreased; // Kemiruvchilar ortganmi
    private boolean vectorsIncreased; // Tashuvchilar ortganmi
    private boolean wildRodents; // Yovvoyi kemiruvchilar ortgan
    private boolean synanthropicRodents; // Sinantrop kemiruvchilar ortgan
    private boolean arthropodsIncreased; // Qon so‘ruvchi bo‘g‘imoyoqlilar ortgan
    private boolean epizootologyExists; // Epizootologik holat mavjudmi

    @ElementCollection
    @CollectionTable(name = "card174_disease_factors", joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> diseaseFactors = new ArrayList<>(); // Kasallik manbalari

    @Builder.Default
    @JsonIgnoreProperties({"card174"})
    @OrderBy("id DESC")
    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExternalSampleTest> externalSampleTests = new ArrayList<>(); //bu tashqi muhitdan olingan labaratoriya namunalari tekshiruvi boladi

    @ElementCollection
    @CollectionTable(name = "card174_affected_animals", joinColumns = @JoinColumn(name = "card174_id"))
    @Column(name = "catalog_code")
    private List<String> affectedAnimals = new ArrayList<>(); // Ta’sirlangan hayvonlar ro‘yxati

    private Integer infectedHumans; // Kasallangan odamlar soni
    private Integer atIndustry; // Shu jumladan sanoat sharoitida
    private Integer reportedCases; // Shu jumladan murojaat qilganlar
    private Integer confirmedCases; // Shu jumladan aniqlanganlar
    private Integer treatedHumans; // Antirabik yordam olganlar soni
    private Integer infectedAtOutbreak; // O‘chog‘da kasallanganlar soni

    @OrderBy("id DESC")
    @Builder.Default
    @JsonIgnoreProperties({"card174"})
    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InfectionMonitoring> monitoring = new ArrayList<>(); // Infeksiya nazorati

    private String quarantineCode; // Karantin turi kodi
    private LocalDate quarantineStart; // Karantin boshlanish sanasi
    private LocalDate quarantineEnd; // Karantin tugash sanasi

    private String disposalMethodCode; // Hayvonni yo‘q qilish usuli kodi
    private LocalDate disposalDate; // Hayvonni yo‘q qilish sanasi

    @Builder.Default
    @JsonIgnoreProperties({"card174"})
    @OrderBy("id DESC")
    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreventiveMeasure> preventiveMeasures = new ArrayList<>();

    private String vaccinationByEpizootic; // Ehtiyotdan emlanishi keraklar (epizootik ko‘rsatmaga asosan)
    private String strayCapture;       // Daydi hayvonlarni tutish
    private String wildCulling;        // Yovvoyi hayvonlarni yo‘qotish

    private String deratCode; // Deratizatsiya kodi
    private Double deratArea; // Deratizatsiya qamrovi (m²)
    private String inspectors; // Tekshiruvchilar FISH
    private String isolation; // Izolyatsiya choralari
    private String meatSubmission; // Go‘sht topshirilganligi
    private String treatment; // Davolash choralari
    private Boolean actionTaken; // Choralar ko‘rildimi

    @Builder.Default
    @JsonIgnoreProperties({"card174"})
    @OrderBy("id DESC")
    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DisinfectionInfo> disinfectionInfos = new ArrayList<>();


    @OrderBy("id DESC")
    @Builder.Default
    @JsonIgnoreProperties({"card174"})
    @OneToMany(mappedBy = "card174", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutbreakControlMeasure> controlMeasures = new ArrayList<>(); // O‘choqdagi chora-tadbirlar

    private String additionalInfo; // Qo‘shimcha ma’lumot
}
