package uz.uzinfocom.isemid.features.report.model;

import jakarta.persistence.Entity;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Form2Report extends AbsEntity {
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
