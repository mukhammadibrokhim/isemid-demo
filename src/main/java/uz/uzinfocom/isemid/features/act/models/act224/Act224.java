package uz.uzinfocom.isemid.features.act.models.act224;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act224 extends Act {
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
