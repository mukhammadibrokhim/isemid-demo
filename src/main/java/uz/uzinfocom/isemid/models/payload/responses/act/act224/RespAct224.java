package uz.uzinfocom.isemid.models.payload.responses.act.act224;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.models.payload.responses.act.RespAct;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RespAct224 extends RespAct {
    private String title;
    private LocalDate documentSendDate;
    private String regionCode;
    private String fullNameOfEpidStaff;
    private String positionOfEpidStaff;
    private String nameOfInstitution;
    private String addressOfEpidStaff;
    private String nameOfRegulatoryActs;
    private String checkingFulfillmentOfRequirements;
    private String fullNameOfParticipant;
    private String recommendedActivities;
    private LocalDateTime executionPeriod;
}
