package uz.uzinfocom.isemid.models.payload.responses.act.act153;

import lombok.*;
import uz.uzinfocom.isemid.models.payload.responses.act.RespAct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RespAct153 extends RespAct {
    private LocalDate documentSendDate;
    private Integer tin;
    private String sampleObjectName;
    private String samplingDocuments;
    private LocalDateTime hospitalizationDate;
    private String samplerFullName;
    private String position;
    private String participantFullName;
    private String participantPosition;
    private String additionalInfo;
    private Set<RespAct153Detail> act153Details;
}
