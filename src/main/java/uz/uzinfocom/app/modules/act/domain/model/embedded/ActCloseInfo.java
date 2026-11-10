package uz.uzinfocom.app.modules.act.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Who closed the act and when — the attached employee (the doctor) moving
 * it {@code RESULT_RECEIVED -> COMPLETED} after reviewing LIS's laboratory
 * result. Both {@code null} until then; legacy/pre-existing
 * {@code COMPLETED} acts (auto-completed by the LIS callback before this
 * review step existed) keep them {@code null} as well.
 */
@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ActCloseInfo {

    @Column(name = "closed_by_id")
    private Long closedById;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public void close(Long closedBy) {
        this.closedById = closedBy;
        this.closedAt = LocalDateTime.now();
    }
}
