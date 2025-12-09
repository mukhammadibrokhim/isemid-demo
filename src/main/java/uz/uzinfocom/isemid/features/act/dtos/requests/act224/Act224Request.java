package uz.uzinfocom.isemid.features.act.dtos.requests.act224;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for Act224")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act224Request extends ActRequest {
    private Long id;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private String fullNameOfEpidStaff;
    private String positionOfEpidStaff;
    private String fullNameOfParticipantEpid;
    private String positionOfParticipantEpid;

    private String nameOfInstitution;
    private String addressOfInstitution;
    private String nameOfRegulatoryActs;
    private String checkingFulfillmentOfRequirements;
    private String fullNameOfParticipant;
    private List<Act224DetailRequest> details;
}
