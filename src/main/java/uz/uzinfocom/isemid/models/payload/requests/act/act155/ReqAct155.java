package uz.uzinfocom.isemid.models.payload.requests.act.act155;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for Act155")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqAct155 extends ReqAct {
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
    private Set<ReqAct155Detail> act155Details;
}
