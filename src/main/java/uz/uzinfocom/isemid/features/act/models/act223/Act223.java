package uz.uzinfocom.isemid.features.act.models.act223;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act223 extends Act {
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private String activityTypeCode;

    private String fullNameofSampler;
    private String positionOfSampler;
    private String fullNameOfParticipant;
    private String positionOfParticipant;
    private LocalDateTime sampleTakenDate;
    private String reasonOfInspectoring;
    private String reasonInspectoringLoinc;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "act223", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Act223Detail> act223Details = new HashSet<>();
}
