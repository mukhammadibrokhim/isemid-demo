package uz.uzinfocom.isemid.features.act.dtos.responses.act224;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Request object for Act224")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act224Response extends ActResponse {
    private Long id;
    private String title;
    private LocalDate documentSendDate;
    private String regionCode;
    private String fullNameOfEpidStaff;
    private String positionOfEpidStaff;
    private String fullNameOfParticipantEpid;
    private String positionOfParticipantEpid;

    private String nameOfInstitution;
    private String addressOfInstitution;
    private String nameOfRegulatoryActs;
    private String checkingFulfillmentOfRequirements;
    private String fullNameOfParticipant;
    private String recommendedActivities;
    private LocalDateTime executionPeriod;
    private List<RecommendedActionResponse> recommendedActions;
}
