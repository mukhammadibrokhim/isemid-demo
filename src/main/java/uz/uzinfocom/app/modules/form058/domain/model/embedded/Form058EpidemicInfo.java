package uz.uzinfocom.app.modules.form058.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form058EpidemicInfo {

    @Column(name = "disease_place_code", length = 64)
    private String diseasePlaceCode;

    @Column(name = "disease_place", length = 512)
    private String diseasePlace;

    @Column(name = "disease_cause", length = 2000)
    private String diseaseCause;

    @Column(name = "epidemic_measures", length = 2000)
    private String epidemicMeasures;
}