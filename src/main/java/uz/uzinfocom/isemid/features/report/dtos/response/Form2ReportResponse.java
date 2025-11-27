package uz.uzinfocom.isemid.features.report.dtos.response;

import lombok.*;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form2ReportResponse {
    private Long id;
    private LocalDate createAt;
    private LocalDate updatedAt;
    private OrgMiniResponse createdOrg;
}
