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
 * internal tool, not something worth coordinating a new IAM role claim for.
 * Authenticated via HTTP Basic on a separate {@code SecurityFilterChain}
 * (see {@code DevPanelSecurityConfig}).
 *
 * <p>Management of these accounts (create/list/revoke) is intentionally kept
 * OUT of the SSO-admin surface entirely - it lives at {@code POST/GET/PATCH
 * /v1/dev/dev-users} and is gated to accounts where {@link #isRoot()} is true
 * (see {@code DevUserController}, {@code DevUserPrincipal}). Not even an
 * {@code isemid_super_admin} can create or revoke a dev-panel account.
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

    /**
     * Grants {@code ROLE_DEV_ROOT} (see {@code DevUserPrincipal}) - the only
     * accounts allowed to manage other {@code DevUser} rows.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean root = false;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public boolean isRoot() {
        return Boolean.TRUE.equals(root);
    }
}
