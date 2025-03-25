package uz.uzinfocom.isemid.models.payload.responses.act.act223;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.models.payload.responses.act.RespAct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RespAct223 extends RespAct {
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
    private Set<RespAct223Detail> act223Details;
}
