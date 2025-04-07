package uz.uzinfocom.isemid.models.payload.requests.act.act224;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for Act224")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqAct224 extends ReqAct {
    private Long id;
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
