package uz.uzinfocom.isemid.features.act.dtos.responses.act153;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Schema(description = "Request object for ACT153 type Act")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act153Response extends ActResponse {
    private Long id;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private String activityTypeCode;

    private String samplingDocuments;
    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;

    private String samplerFullName;
    private Integer samplerPositionId;
    private String samplerPositionUz;
    private String samplerPositionRu;
    private String participantFullName;
    private Integer participantPositionId;
    private String participantPositionUz;
    private String participantPositionRu;
    private String additionalInfo;
    @Builder.Default
    private Set<Act153DetailResponse> act153Details = new HashSet<>();
}
