package uz.uzinfocom.isemid.features.act.models.act223;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.features.act.models.Act;

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

    private String samplerFullName;
    private Integer samplerPositionId;
    private String samplerPositionUz;
    private String samplerPositionRu;
    private String participantFullName;

    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;

    private String reasonOfInspectoring;
    private String reasonInspectoringLoinc;

    private Integer participantPositionId;
    private String participantPositionUz;
    private String participantPositionRu;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "act223", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Act223Detail> act223Details = new HashSet<>();
}
