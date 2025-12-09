package uz.uzinfocom.isemid.features.act.dtos.responses.act223;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Request object for Act223")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act223Response extends ActResponse {
    private Long id;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private String fullNameofSampler;
    private String positionOfSampler;
    private String fullNameOfParticipant;
    private String positionOfParticipant;
    private LocalDateTime sampleTakenDate;
    private String reasonOfInspectoring;
    private String reasonInspectoringLoinc;
    private String additionalInfo;
    private Set<Act223DetailResponse> act223Details;
}
