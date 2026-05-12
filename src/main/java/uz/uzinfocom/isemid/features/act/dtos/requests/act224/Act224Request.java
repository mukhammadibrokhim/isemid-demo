package uz.uzinfocom.isemid.features.act.dtos.requests.act224;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;

import java.util.List;
import uz.uzinfocom.isemid.features.act.models.common.Institution;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for Act224")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act224Request extends ActRequest {
    private Long id;
    private Institution institution;
    private String activityTypeCode;

    private String fullNameOfEpidStaff;
    private String positionOfEpidStaff;
    private String fullNameOfParticipantEpid;
    private String positionOfParticipantEpid;

    private String nameOfInstitution;
    private String addressOfInstitution;
    private String nameOfRegulatoryActs;
    private String checkingFulfillmentOfRequirements;
    private String fullNameOfParticipant;
    private String additionalInfo;
    private List<Act224DetailRequest> details;
}
