package uz.uzinfocom.isemid.features.act.dtos.responses.act224;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String nameOfInstitution;
    private String addressOfEpidStaff;
    private String nameOfRegulatoryActs;
    private String checkingFulfillmentOfRequirements;
    private String fullNameOfParticipant;
    private String recommendedActivities;
    private LocalDateTime executionPeriod;
}
