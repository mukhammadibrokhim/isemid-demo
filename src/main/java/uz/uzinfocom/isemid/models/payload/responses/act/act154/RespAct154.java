package uz.uzinfocom.isemid.models.payload.responses.act.act154;

import lombok.*;
import uz.uzinfocom.isemid.models.payload.responses.act.RespAct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RespAct154 extends RespAct {
    private String title;
    private Long serialNumber;
    private LocalDate documentSendDate;
    private Integer tin;
    private String nameOfObject;
    private String objectAddress;
    private LocalDateTime hospitalizationDate;
    private String samplerFullName;
    private String position;
    private String additionalInfo;
    private Set<RespAct154Detail> act154SampleDetailsList;
}
