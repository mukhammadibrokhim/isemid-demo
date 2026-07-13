package uz.uzinfocom.isemid.features.form0581.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Location where the injury occurred (DE.58-61) - modeled as a single cohesive value object.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class InjuryLocation {

    @Column(name = "injury_state_code")
    private String stateCode;

    @Column(name = "injury_city_code")
    private String cityCode;

    @Column(name = "injury_address")
    private String address;
}
