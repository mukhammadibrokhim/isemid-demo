package uz.uzinfocom.isemid.features.card.dtos.requests.card174;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.card.dtos.requests.card161.VaccinationRequest;

import java.time.LocalDate;
import java.util.List;

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
    private List<VaccinationRequest> vaccinations;
    private String vaccinationSummary;
}
