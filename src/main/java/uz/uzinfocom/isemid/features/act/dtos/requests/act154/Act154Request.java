package uz.uzinfocom.isemid.features.act.dtos.requests.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for Act154")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act154Request extends ActRequest {
    private String title;
    private Long serialNumber;
    private LocalDate documentSendDate;
    private Integer tin;
    private String nameOfObject;
    private String objectAddress;
    private String samplerFullName;
    private LocalDate deliveredDate;
    private String position;
    private String participantFullName;
    private String participantPosition;
    private String additionalInfo;
    private Set<Act154DetailRequest> act154Details;
}
