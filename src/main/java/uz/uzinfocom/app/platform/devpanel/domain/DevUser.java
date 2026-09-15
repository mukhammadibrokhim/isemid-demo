package uz.uzinfocom.app.platform.devpanel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * /v1/dev/dev-users} and revoke is gated to {@link DevUserRole#SUPER_ADMIN}
 * accounts (see {@code DevUserController}, {@code DevUserPrincipal}). Not even
 * an {@code isemid_super_admin} can create or revoke a dev-panel account.
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

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * Contact/profile detail - not used for authentication or notifications,
     * purely descriptive for the account list. Nullable: existing accounts
     * predate this field, and the seeded bootstrap account has none.
     */
    @Column(length = 150)
    private String email;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(length = 32)
    private String phone;

    /**
     * Optional position/department, from the dev-panel-only {@link DevPosition}
     * lookup ({@code /v1/dev/ref/positions}) - not the org-facing reference
     * dictionaries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private DevPosition position;

    /**
     * Access tier for this account - drives the granted authorities in
     * {@code DevUserPrincipal} and, in turn, the {@code @PreAuthorize} checks
     * across the {@code /v1/dev/**} controllers. See {@link DevUserRole}.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevUserRole role = DevUserRole.USER;

    /**
     * When true, {@code DevPasswordChangeGuardFilter} blocks every
     * {@code /v1/dev/**} request from this account except its own
     * {@code PATCH /v1/dev/dev-users/me/password} call. Defaults to true for
     * every newly-provisioned account (see {@code DevUserCommandService.create})
     * - cleared only by {@code DevUserCommandService.changeOwnPassword}, once
     * the account's real owner has set their own password.
     */
    @Builder.Default
    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = true;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public boolean isSuperAdmin() {
        return role == DevUserRole.SUPER_ADMIN;
    }

    public boolean isMustChangePassword() {
        return Boolean.TRUE.equals(mustChangePassword);
    }
}
