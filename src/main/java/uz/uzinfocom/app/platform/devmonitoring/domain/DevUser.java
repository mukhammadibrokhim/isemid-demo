package uz.uzinfocom.app.platform.devmonitoring.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

/**
 * A local, ops-only login for the developer monitoring panel ({@code /v1/dev/**}).
 * Deliberately independent of the external SSO/DHP identity providers - this is an
 * internal tool, not something worth coordinating a new IAM role claim for. Created
 * by an SSO-authenticated admin via {@code POST /v1/admin/dev-users}, authenticated
 * itself via HTTP Basic on a separate {@code SecurityFilterChain}
 * (see {@code DevPanelSecurityConfig}).
 */
@Getter
@Setter
@Entity
@Table(
        name = "dev_user",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_user_username", columnNames = "username")
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DevUser extends AuditableEntity {

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
