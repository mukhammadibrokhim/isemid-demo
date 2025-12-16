package uz.uzinfocom.isemid.features.report.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "form_report_2")
public class FormReport2 extends AbsEntity {
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
