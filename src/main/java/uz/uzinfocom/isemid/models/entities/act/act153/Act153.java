package uz.uzinfocom.isemid.models.entities.act.act153;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.act.Act;

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
public class Act153 extends Act {
    private LocalDate documentSendDate;
    private Integer tin;

    @Column(columnDefinition = "TEXT")
    private String sampleObjectName;

    @Column(columnDefinition = "TEXT")
    private String samplingDocuments;

    private LocalDateTime hospitalizationDate;
    private String samplerFullName;
    private String position;
    private String participantFullName;
    private String participantPosition;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "act153_detail_mapping",
            joinColumns = @JoinColumn(name = "act153_id"),
            inverseJoinColumns = @JoinColumn(name = "act153_detail_id")
    )
    @JsonIgnore
    private Set<Act153Detail> act153Details = new HashSet<>();
}
