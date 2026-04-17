package uz.uzinfocom.isemid.features.act.dtos.responses.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Request object for Act154")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act154Response extends ActResponse {
    private Long id;
    private String title;
    private Long serialNumber;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private String activityTypeCode;

    private String samplerFullName;
    private LocalDate deliveredDate;
    private Integer positionId;
    private String position;
    private String participantFullName;
    private Integer participantPositionId;
    private String participantPosition;
    private String additionalInfo;
    private Set<Act154DetailResponse> act154Details;
}
