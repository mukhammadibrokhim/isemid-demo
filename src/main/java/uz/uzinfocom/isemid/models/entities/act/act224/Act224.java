package uz.uzinfocom.isemid.models.entities.act.act224;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code", nullable = false)
    private Catalog region;
    @Column(name = "region_code", insertable = false, updatable = false)
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
