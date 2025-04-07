package uz.uzinfocom.isemid.models.entities.act.act223;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.act156.Act156GroupDetail;

import java.time.LocalDate;
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
    private String title;
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
    private String reasonInspectoringLoinc;
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "act223_detail_mapping",
            joinColumns = @JoinColumn(name = "act223_id"),
            inverseJoinColumns = @JoinColumn(name = "act223_detail_id")
    )
    @JsonIgnore
    private Set<Act223Detail> act223Details = new HashSet<>();
}
