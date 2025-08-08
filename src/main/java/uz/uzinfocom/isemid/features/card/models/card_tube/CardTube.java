package uz.uzinfocom.isemid.features.card.models.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.card.models.Card;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CardTube extends Card {

    // Dispanser nazorati bilan bog‘liq maydonlar
    private LocalDate dispensaryStartDate;      // Dispanser ro'yxatga olingan sana
    private String dispensaryId;                // Dispanser ID raqami

    // MKB-10 bilan bog‘liq ma'lumotlar
    private String mkb10Code;                   // MKB-10 kodi
    private String mkb10Name;                   // MKB-10 kasallik nomi

    // MB (Mycobacterium) aniqlanishi
    private LocalDate firstMBDate;              // MB birinchi aniqlangan sana
    private String mbDetectionMethod;           // Aniqlash usuli
    private LocalDate mbRegDate;                // MB kasalligi ro‘yxatga olingan sana

    private String homeStayReasonCode;          // Uyda qolish sababi (kod)
    private LocalDate dischargeDate;            // Chiqarilgan sana

    // Emlash haqida ma'lumot
    private String vaccineName;                 // Vaksina/preparat nomi
    private String vaccineSeries;               // Seriya raqami
    private LocalDateTime vaccineDate;          // Emlash vaqti
    private int doseVolume;                     // Doza hajmi
    private boolean isScheduled;                // Rejali emlashmi yoki yo‘q

    // MB oldidan olingan ko‘krak rentgen natijalari
    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("cardTube")
    @OrderBy("id DESC")
    @Builder.Default
    private List<XRay> chestXrays = new ArrayList<>();

    // Oldingi sil (TB) tarixi
    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("cardTube")
    @OrderBy("id DESC")
    @Builder.Default
    private List<TBHistory> tbHistory = new ArrayList<>();

    // Dispanser guruhiga oid ma'lumot
    private String dispensaryGroup;
    private String dgMkb10Code;
    private String dgMkb10Name;

    // So‘nggi 2 yildagi tibbiy ko‘rik sanalari
    @ElementCollection
    @CollectionTable(name = "card_tube_checkup_dates", joinColumns = @JoinColumn(name = "card_tube_id"))
    @Column(name = "checkup_date")
    private List<LocalDate> checkupDates = new ArrayList<>();

    // Qayta davolanish
    private LocalDate retreatmentStartDate;
    private LocalDate retreatmentEndDate;
    private String dismissalDate;

    // Xabarnomalar
    private LocalDate workplaceNotificationDate;  // Ish joyiga yuborilgan sana
    private String notifiedTo;                    // Kimga yuborilgan
    private LocalDate clinicNotificationDate;     // Poliklinikaga yuborilgan sana

    // Oziqlanish turlari (dropdown multiselect)
    @ElementCollection
    @CollectionTable(name = "card_tube_nutrition_type", joinColumns = @JoinColumn(name = "card_tube_id"))
    @Column(name = "code")
    private List<String> nutritionCodes = new ArrayList<>();

    // Ijtimoiy-iqtisodiy holatlar
    private String workConditionCode;             // Mehnat sharoiti
    private String incomeLevelCode;               // Oila byudjeti darajasi
    private String harmfulHabitCode;              // Zarur odatlar (masalan, chekish)

    // Yuqtirish manbalari
    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("cardTube")
    @OrderBy("id DESC")
    @Builder.Default
    private List<InfectionSource> infectionSources = new ArrayList<>();

    // Uy-joy sharoitlari
    private String housingConditionCode;
    private Integer rooms;
    private Integer floors;
    private Boolean hasElevator;

    private Integer totalContacts;               // Umumiy aloqalar soni
    private Integer householdContacts;           // Uy ichidagi aloqa
    private Integer adults;
    private Integer teenagers;
    private Integer childrenUnder14;
    private Integer pregnantWomen;
    private Integer foodOrChildWorkers;          // Oziq-ovqat va bolalar bilan ishlovchilar

    // Uy o'lchamlari va aloqadorlar
    private Integer familyRooms;
    private Integer roomArea;
    private Integer totalArea;
    private Integer isolatedRoomArea;
    private Integer roommates;
    private Integer childRoommates;

    // Uy sanitariya holati
    private String hygieneAssessmentCode;
    private String heatingCode;
    private String sewerageCode;
    private Boolean hasVentilation;
    private String renovationNeededCode;
    private String habitabilityCode;
    private LocalDate improvementDate;
    private String housingDiff;

    // Sil kasalligi gigiyena qoidalariga rioya
    private Boolean followsCoughEtiquette;       // Yo‘taldan himoyalanishga rioya qiladi
    private Boolean hasSpittoon;                 // Qopqoqli idishi bor
    private Integer spittoonCount;
    private Boolean spittoonAtWork;
    private Boolean spittoonAtHome;
    private Boolean spittoonInPublic;

    private String sputumDisposalCode;           // So‘lakni yo‘q qilish usuli

    // Qarindosh va dezinfektsiya
    private String fullName;                     // Qarindosh to‘liq F.I.Sh
    private String relationCode;                 // Qarindoshlik darajasi

    private String getsDisinfectantCode;         // Dezinfeksiyalovchi vosita oladimi
    private Integer disinfectantAmount;          // Oylik dozasi
    private String disinfectantProvider;         // Kim beradi

    // Tashrif intervallari
    private Integer visitIntervalValue;          // Masalan: 2
    private String visitIntervalUnit;            // Masalan: "weeks"
    private Integer ftbVisitIntervalValue;       // FTB tashrifi: 2
    private String ftbVisitIntervalUnit;         // FTB birlik: "weeks"

    // Aloqalarni monitoring qilish
    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("cardTube")
    @OrderBy("id DESC")
    @Builder.Default
    private List<ContactMonitoring> contactMonitoring = new ArrayList<>();

    // Sog‘ayish rejasi
    private String recoveryPlanCode;
    private LocalDate startDate;
    private LocalDate endDate;
}
