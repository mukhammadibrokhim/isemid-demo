package uz.uzinfocom.isemid.features.act.dtos.requests.act153;

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
@Schema(description = "Request object for ACT153 type Act")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act153Request extends ActRequest {
    private Long id;
    private LocalDate documentSendDate;
    private Integer tin;
    private String loinc;
    private String sampleObjectName;
    private String samplingDocuments;
    private LocalDateTime hospitalizationDate;
    private String samplerFullName;
    private String position;
    private String participantFullName;
    private String participantPosition;
    private String additionalInfo;
    private Set<Act153DetailRequest> act153Details;
}
