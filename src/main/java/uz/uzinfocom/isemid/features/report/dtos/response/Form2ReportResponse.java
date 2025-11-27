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
    private Integer registeredLastYear;

    private Integer registeredThisYear;

    private Integer casesInSchoolsAndDdu;

    private Integer disinfectionEventsCount;

    private Integer inspectedObjectsCount;

    private Integer closedObjectsCount;

    private Integer dduCount;

    private Integer schoolCount;

    private Integer finesCount;

    private Integer sentToProsecutorCount;
}
