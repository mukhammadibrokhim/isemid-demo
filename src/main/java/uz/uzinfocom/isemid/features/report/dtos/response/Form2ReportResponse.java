package uz.uzinfocom.isemid.features.report.dtos.response;

import lombok.*;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form2ReportResponse {
    private OrgMiniResponse createdOrg;
}
