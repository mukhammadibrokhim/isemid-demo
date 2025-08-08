package uz.uzinfocom.isemid.features.card.dtos.responses.card175;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Card175Response extends CardResponse {

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

    private List<String> injuryParts = new ArrayList<>(); // Jarohat joylari (dropdown multi)

    private String illnessSeverityCode;  // Kasallik og‘irligi
    private String jobRelationCode;      // Kasbiy tegishlilik
    private String spreaderTypeCode;     // Yuqtiruvchi manba turi
    private String spreaderOwnerCode;    // Hayvon egasi
    private String animalObsResultCode;  // Hayvon kuzatuv natijasi
    private String animalDiagnosisCode;  // Hayvon tashxis natijasi

    private List<String> transmissionConditions = new ArrayList<>(); // Yuqtirish sharoitlari

    private List<String> mainPathogenFactors = new ArrayList<>(); // Yuqtirish omillari

    private List<String> residenceMeasures = new ArrayList<>(); // Yashash joyidagi chora-tadbirlar
}
