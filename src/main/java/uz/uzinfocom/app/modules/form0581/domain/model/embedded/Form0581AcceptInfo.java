package uz.uzinfocom.app.modules.form0581.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form0581AcceptInfo {

    @Column(name = "accepted_by_id")
    private Long acceptedBy;

    @Column(name = "accepted_at")
    private Instant acceptedAt;
}
