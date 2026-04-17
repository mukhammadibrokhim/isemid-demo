package uz.uzinfocom.isemid.features.act.models.act153;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "delivered_date")
    private LocalDate deliveredDate; // laboratoriyaga yetkazilgan sana

    private String samplerFullName;
    private Integer positionId;
    private String position;
    private String participantFullName;
    private Integer participantPositionId;
    private String participantPosition;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "act153", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id DESC")
    @JsonManagedReference
    private List<Act153Detail> act153Details = new ArrayList<>();
}
