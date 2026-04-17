package uz.uzinfocom.isemid.features.act.dtos.requests.act153;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for ACT153 type Act")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act153Request extends ActRequest {
    private Long id;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private String activityTypeCode;
    private String samplingDocuments;
    private LocalDate deliveredDate;
    private String samplerFullName;
    private Integer positionId;
    private String position;
    private String participantFullName;
    private Integer participantPositionId;
    private String participantPosition;
    private String additionalInfo;
    private Set<Act153DetailRequest> act153Details;
}
