package uz.uzinfocom.app.modules.form129.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form129CancellationInfo {

    @Column(name = "cancel_reason", length = 1000)
    private String cancelReason;

    @Column(name = "canceled_by")
    private Long canceledBy;

    @Column(name = "canceled_at")
    private Instant canceledAt;
}
