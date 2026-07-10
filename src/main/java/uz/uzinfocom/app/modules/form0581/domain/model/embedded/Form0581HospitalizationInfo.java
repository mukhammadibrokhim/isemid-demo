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
public class Form0581HospitalizationInfo {

    @Column(name = "hospitalized_at")
    private LocalDateTime hospitalizedAt;

    @Column(name = "hospital_organization_id")
    private Long hospitalOrganizationId;
}
