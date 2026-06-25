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
public class Form058ClinicalInfo {

    @Column(name = "lab_confirmation")
    private Boolean labConfirmation;

    @Column(name = "hospital_place_id")
    private Long hospitalPlaceId;
}