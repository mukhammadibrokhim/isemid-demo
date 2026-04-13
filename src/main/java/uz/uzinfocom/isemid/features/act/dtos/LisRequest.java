package uz.uzinfocom.isemid.features.act.dtos;

import lombok.*;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.Priority;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.Purpose;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LisRequest {
    private Long actId;
    private Long lisLabId;

    Integer actTemplateId;
    Priority priority;
    String tin;
    String organizationName;
    String organizationAddress;
    String organizationLegalAddress;
    Purpose purpose;
}
