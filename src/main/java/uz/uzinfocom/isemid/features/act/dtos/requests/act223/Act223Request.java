package uz.uzinfocom.isemid.features.act.dtos.requests.act223;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for Act223")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act223Request extends ActRequest {
    private Long id;
    private LocalDate documentSendDate;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private String fullNameofSampler;
    private String positionOfSampler;
    private String fullNameOfParticipant;
    private String positionOfParticipant;
    private LocalDateTime sampleTakenDate;
    private String reasonOfInspectoring;
    private String loinc;
    private String additionalInfo;

    private Set<Act223DetailRequest> act223Details;
}
