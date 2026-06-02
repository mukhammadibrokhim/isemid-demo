package uz.uzinfocom.app.platform.iam.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "user_organization_roles",
        indexes = {
                @Index(name = "idx_user_org_role_user_id", columnList = "user_id"),
                @Index(name = "idx_user_org_role_org_id", columnList = "organization_id"),
                @Index(name = "idx_user_org_role_role_id", columnList = "role_id"),
                @Index(name = "idx_user_org_role_active_deleted", columnList = "active, deleted_at")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationRole extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isActiveAssignment() {
        return Boolean.TRUE.equals(active) && deletedAt == null;
    }

    public void deactivate() {
        this.active = false;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.active = true;
        this.deletedAt = null;
    }
}
