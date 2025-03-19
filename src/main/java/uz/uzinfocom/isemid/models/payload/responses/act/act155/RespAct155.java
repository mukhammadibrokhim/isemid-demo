package uz.uzinfocom.isemid.models.payload.responses.act.act155;

import lombok.*;
import uz.uzinfocom.isemid.models.payload.responses.act.RespAct;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RespAct155 extends RespAct {
    private String title;
    private LocalDate documentSendDate;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private Date selectedDate;
    private String samplerFullName;
    private String samplerPosition;
    private String objectRepresentativeFullName;
    private String objectRepresentativePosition;
    private String additionalInfo;
    private Set<Resp155Detail> resp155Details;
}
