package uz.uzinfocom.isemid.features.report.dtos.response;

import lombok.*;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form3_1ReportResponse {
    private Long id;
    private UUID uuid;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private OrgMiniResponse createdOrg;
    private Integer influenzaLikeIllness;
    private Integer acuteRespiratoryInfection;
    private Integer acutePancreatitis;
    private Integer severeAcuteRespiratoryInfectionTotal;
    private Integer severeAcuteRespiratoryInfectionPregnant;
    private Integer deathTotal;
    private Integer deathPregnant;
    private Integer vaccinationWeekly;
    private Integer vaccinationFromStart;
}
