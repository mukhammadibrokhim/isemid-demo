package uz.uzinfocom.isemid.features.card.dtos.requests.card174;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfectionMonitoringRequest {
    private Long id;
    private String lastName; // Familiya
    private String firstName; // Ismi
    private String middleName; // Sharifi
    private String genderCode; // Jins kodi (M/F)
    private LocalDate birthDate; // Tug‘ilgan sana
    private String address; // Yashash manzili
    private String profession; // Kasbi yoki ish joyi
    private LocalDate applyDate; // Murojaat qilingan sana
    private LocalDate confirmDate; // Kasallik tasdiqlangan sana
    private String possibleLocation; // Yuqtirgan joy (taxminiy)
    private String possibleFactor; // Yuqtirish omili (taxminiy)
    private LocalDate possibleDate; // Yuqtirilgan sana (taxminiy)
    private LocalDate contactInfectionDate; // Muloqatda bo‘lganlarning kasallanish sanasi

    // Quyidagilar integratsiyaga tayyor fieldlar, hozircha izohda qoldirildi

    // private LocalDate previousInfectionDate; // Ilgari infeksiya bo‘lgan sana (emlangan)
    // private String emergencyProphylaxis; // Zudlik bilan oldini olish
    // private String antibioticSeries; // Antibiotik davolash seriyasi
    // private String antibioticScheme; // Davolash sxemasi (Emlash.uz orqali)

    // private String provizorName; // 058 dan olinadi
    // private LocalDate provizorRecordDate; // Provizor qayd sanasi
    // private LocalDate discussionDate; // Muhokama sanasi

    // private LocalDate labTestDate; // Laborator test sanasi (LIS bilan integratsiya)
    // private String labTestResult; // Laborator test natijasi
}
