package uz.uzinfocom.app.modules.form129.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Soft-delete marker for {@link uz.uzinfocom.app.modules.form129.domain.model.Form129}.
 * Mirrors {@code Form058DeleteInfo}/{@code ActDeleteInfo}: the row stays in the
 * table, hidden from every listing and lookup via {@code deleted = false}
 * predicates in {@code Form129Specification}. Only an admin / super admin may
 * flip it — see {@code DeleteForm129Service}.
 */
@Getter
@Setter
@Embeddable
public class Form129DeleteInfo {

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
