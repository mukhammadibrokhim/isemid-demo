package uz.uzinfocom.isemid.features.card.models.card175;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.card.models.Card;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Card175 extends Card {

    private LocalDateTime epiInvestigationTime; // Epidemiologik tekshiruv vaqti

    @Temporal(TemporalType.DATE)
    private Date illnessDate; // Kasallik boshlanish sanasi

    private LocalDateTime initialDiagnosisDate; // Shoshilinch tashxis sanasi
    private LocalDateTime finalDiagnosisDate;   // Yakuniy tashxis sanasi

    @Temporal(TemporalType.DATE)
    private Date dischargeDate; // Kasalxonadan chiqish sanasi

    @Temporal(TemporalType.DATE)
    private Date vaccinationDate; // Emlash sanasi

    private String pathogenType; // Qo‘zg‘atuvchi turi (seroguruxi)

    private String originPlaceCode; // Bemor qayerdan kelgan (LIS dan)
    private String patientSourceCode; // Bemor manbai

    private String dischargeStatusCode; // Kasalxonadan chiqish holati
    private String transportTypeCode;   // Transport turi
    private String noHospitalReasonCode; // Uyda qolish sababi
    private String lateHospitalizationReasonCode; // Kech yotqizish sababi
    private String diagnosisVerifiedCode; // Tashxis laboratoriyada tasdiqlangani
    private String vaccinationInfoCode;  // Vaksinatsiya ma'lumoti
    private String lastVaccinationInfo;  // Oxirgi emlash haqida ma'lumot

    private String epiComment;           // Qisqacha epidemiologik sharh
    private String initialDiagnosisCode; // Birlamchi tashxis

    private String patientStatusCode;    // Bemor aniqlanish holati
    private String applicationPlace;     // Murojaat qilgan joy

    private String aidInfoCode;          // Antirabik yordam ma'lumoti
    private String medicineName;         // Dori nomi
    private Long medicineQty;            // Dori miqdori
    private Long medicineSeries;         // Seriyasi
    private Long vaccinationPlan;        // Jadval bo‘yicha soni
    private String clinicalForm;         // Klinik shakl

    @ElementCollection
    @CollectionTable(name = "card175_part_of_injury", joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> injuryParts = new ArrayList<>(); // Jarohat joylari (dropdown multi)

    private String illnessSeverityCode;  // Kasallik og‘irligi
    private String jobRelationCode;      // Kasbiy tegishlilik
    private String spreaderTypeCode;     // Yuqtiruvchi manba turi
    private String spreaderOwnerCode;    // Hayvon egasi
    private String animalObsResultCode;  // Hayvon kuzatuv natijasi
    private String animalDiagnosisCode;  // Hayvon tashxis natijasi

    @ElementCollection
    @CollectionTable(name = "card175_disease_transmission_condition", joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> transmissionConditions = new ArrayList<>(); // Yuqtirish sharoitlari

    @ElementCollection
    @CollectionTable(name = "card175_pathogen_main_factor", joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> mainPathogenFactors = new ArrayList<>(); // Yuqtirish omillari

    @ElementCollection
    @CollectionTable(name = "card175_taken_measures_from_residence", joinColumns = @JoinColumn(name = "card175_id"))
    @Column(name = "catalog_code")
    private List<String> residenceMeasures = new ArrayList<>(); // Yashash joyidagi chora-tadbirlar
}
