package uz.uzinfocom.isemid.features.card.dtos.requests.card175;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD175 type card")
public class Card175Request extends CardRequest {
    private String pathogenType; // Qo‘zg‘atuvchi turi (seroguruxi)
    private String originPlaceCode; // Bemor qayerdan kelgan (LIS dan)
    private String patientSourceCode; // Bemor manbai
    private String possibleInfectionPlace; // Qisqacha epidimiologik anamnez (yuqish mumkin bo‘lgan joy).
    private String primaryDiagnosis; //Birlemshi diagnoz.
    private String patientDetectedCode;    // Bemor aniqlanish holati
    private LocalDateTime epiCheckTime; // Epidemiologik tekshiruv vaqti
    private LocalDate illnessDate; // Kasallangan sana
    private String applicationPlace;     // Murojaat qilgan joy
    private LocalDate urgentDiagnosisDate; // Shoshilinch tashxis sanasi
    private LocalDate finalDiagnosisDate; // Yakuniy tashxis sanasi
    private LocalDate dischargeDate; // Kasalxonadan Chiqish sanasi
    private String dischargeStatusCode; // Kasalxonadan Chiqish holati
    private String transportType; // Kasalxonaga olib borilgan transport vositasi
    private String noHomeStayReason; // Uyda qoldirilmaslik sababi
    private String lateHospitalizationReason; // Kech yotqizilish sabablari
    private String diagnosisLabConfirmed; // Diagnoz va laboratoriya tasdiqlangan
    private String vaccinationInfo; // Vaksinatsiya ma'lumotlari
    private String emergencyProphylaxisInfo; // Tezkor profilaktika yoki antirabik yordam ma'lumotlari
    private LocalDate vaccinationDate; // Emlash sanasi
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
