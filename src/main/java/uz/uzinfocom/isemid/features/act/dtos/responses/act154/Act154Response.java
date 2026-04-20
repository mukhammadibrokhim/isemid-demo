package uz.uzinfocom.isemid.features.act.dtos.responses.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;

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
    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;
    private Integer samplerPositionId;
    private String samplerPositionUz;
    private String samplerPositionRu;
    private String participantFullName;
    private Integer participantPositionId;
    private String participantPositionUz;
    private String participantPositionRu;
    private String additionalInfo;
    private Set<Act154DetailResponse> act154Details;
}
