package uz.uzinfocom.isemid.features.card.dtos.requests.card174;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.models.card174.PreventiveMeasure;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD174 type card")
public class Card174Request extends CardRequest {
    private Long id;
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

    private List<String> diseaseFactors = new ArrayList<>(); // Kasallik manbalari
    private List<ExternalSampleTestRequest> externalSampleTests = new ArrayList<>();
    private List<String> affectedAnimals = new ArrayList<>(); // Ta’sirlangan hayvonlar ro‘yxati

    private Integer infectedHumans; // Kasallangan odamlar soni
    private Integer atIndustry; // Shu jumladan sanoat sharoitida
    private Integer reportedCases; // Shu jumladan murojaat qilganlar
    private Integer confirmedCases; // Shu jumladan aniqlanganlar
    private Integer treatedHumans; // Antirabik yordam olganlar soni
    private Integer infectedAtOutbreak; // O‘chog‘da kasallanganlar soni
    private List<InfectionMonitoringRequest> monitoring = new ArrayList<>(); // Infeksiya nazorati
    private String quarantineCode; // Karantin turi kodi
    private LocalDate quarantineStart; // Karantin boshlanish sanasi
    private LocalDate quarantineEnd; // Karantin tugash sanasi
    private String disposalMethodCode; // Hayvonni yo‘q qilish usuli kodi
    private LocalDate disposalDate; // Hayvonni yo‘q qilish sanasi

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
    private List<String> disinfectionFactors = new ArrayList<>(); // Dezinfeksiya ob'ektlari
    private Integer disinfectedCount; // Zararsizlantirilgan omillar miqdori
    private LocalDate disinfectionDate; // Zararsizlantirish sanasi
    private List<String> eliminationMethods = new ArrayList<>(); // Yo‘q qilish usullari
    private String eventLocation; // Hodisa joyi
    private String controlResults; // Chora nazorati natijasi
    private List<OutbreakControlMeasureRequest> controlMeasures = new ArrayList<>(); // O‘choqdagi chora-tadbirlar
    private String additionalInfo; // Qo‘shimcha ma’lumot
}