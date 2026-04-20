package uz.uzinfocom.isemid.features.act.models.act153;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
public class Act153 extends Act {
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private String activityTypeCode;

    @Column(columnDefinition = "TEXT")
    private String samplingDocuments;

    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;

    private String samplerFullName;
    private Integer samplerPositionId;
    private String samplerPositionUz;
    private String samplerPositionRu;
    private String participantFullName;
    private Integer participantPositionId;
    private String participantPositionUz;
    private String participantPositionRu;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "act153", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id DESC")
    @JsonManagedReference
    private Set<Act153Detail> act153Details = new HashSet<>();
}
