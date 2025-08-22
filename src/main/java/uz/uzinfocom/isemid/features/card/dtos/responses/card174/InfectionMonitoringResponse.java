package uz.uzinfocom.isemid.features.card.dtos.responses.card174;

import lombok.*;
import uz.uzinfocom.isemid.features.card.dtos.responses.card161.VaccinationResponse;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfectionMonitoringResponse {
    private Integer seqNo; // Tartib raqami
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
    private List<VaccinationResponse> vaccinations;
    private String vaccinationSummary;
}

