package uz.uzinfocom.isemid.features.act.dtos.responses.act153;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    private String samplerFullName;
    private String position;
    private LocalDate deliveredDate;
    private String participantFullName;
    private String participantPosition;
    private String additionalInfo;
    @Builder.Default
    private Set<Act153DetailResponse> act153Details = new HashSet<>();
}
