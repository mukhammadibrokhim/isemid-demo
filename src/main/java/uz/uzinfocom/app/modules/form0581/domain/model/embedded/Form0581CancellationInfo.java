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
public class Form0581CancellationInfo {

    @Column(name = "cancel_reason", length = 1000)
    private String cancelReason;

    @Column(name = "canceled_by_id")
    private Long canceledBy;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @Column(name = "not_approved_reason", length = 1000)
    private String notApprovedReason;
}
