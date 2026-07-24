package uz.uzinfocom.app.modules.act.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Embeddable
public class ActDeleteInfo {

    @Getter
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by_id")
    private Long deletedBy;

    @Column(name = "delete_reason", length = 1000)
    private String deleteReason;

    public void softDelete(Long deletedBy, String reason) {
        if (this.deleted) {
            return;
        }

        this.deleted = true;
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
        this.deleteReason = reason;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.deleteReason = null;
    }

}
