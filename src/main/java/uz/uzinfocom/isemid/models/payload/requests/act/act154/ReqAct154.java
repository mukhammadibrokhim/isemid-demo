package uz.uzinfocom.isemid.models.payload.requests.act.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for Act154")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqAct154 extends ReqAct {
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
    private String actClassificatorCode;
    private List<ReqAct154Details> act154SampleDetailsList;
}
