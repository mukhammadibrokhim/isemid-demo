package uz.uzinfocom.app.modules.form0581.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form0581IncidentInfo {

    @Column(name = "injury_date_time", nullable = false)
    private LocalDateTime injuryDateTime;

    @Column(name = "dpu_visit_date_time", nullable = false)
    private LocalDateTime dpuVisitDateTime;

    @Column(name = "injury_region_code", nullable = false, length = 64)
    private String injuryRegionCode;

    @Column(name = "injury_district_code", nullable = false, length = 64)
    private String injuryDistrictCode;

    @Column(name = "injury_address", length = 1000)
    private String injuryAddress;
}
