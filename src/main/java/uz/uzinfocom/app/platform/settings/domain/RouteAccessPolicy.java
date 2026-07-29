package uz.uzinfocom.app.platform.settings.domain;

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
 * A single runtime-editable route-access rule, replacing the three static
 * lists that used to live in {@code SecurityRouteCatalog} (deleted) —
 * {@code OPEN_PATTERNS}, {@code POLICY_RULES}, and {@code NO_ORG_HEADER_PATTERNS}
 * unified into one ordered table. {@link #displayOrder} preserves today's
 * first-match-wins precedence (the old {@code LinkedHashMap} iteration
 * order); a request path with no matching row falls back to the same
 * fail-closed default as before (see {@code RequestPolicy.defaultProtectedRoute()}).
 *
 * <p>Read on every request by {@code RouteAccessPolicyResolver} (short-TTL
 * cached) and consumed by both {@code DynamicRouteAuthorizationManager}
 * (the raw Spring Security gate) and {@code RequestPolicyResolver} (the
 * org-header/role-validation policy) — the same source of truth for both
 * layers, closing the gap where they could previously disagree.
 */
@Getter
@Setter
@Entity
@Table(
        name = "security_route_policy",
        uniqueConstraints = @UniqueConstraint(name = "uk_security_route_policy_pattern", columnNames = "pattern")
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RouteAccessPolicy extends AuditableEntity {

    @Column(nullable = false, length = 200)
    private String pattern;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @Column(nullable = false)
    private Boolean open = false;

    @Builder.Default
    @Column(name = "organization_header_required", nullable = false)
    private Boolean organizationHeaderRequired = true;

    @Builder.Default
    @Column(name = "role_validation_required", nullable = false)
    private Boolean roleValidationRequired = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String description;

    public boolean isOpen() {
        return Boolean.TRUE.equals(open);
    }

    public boolean isOrganizationHeaderRequired() {
        return Boolean.TRUE.equals(organizationHeaderRequired);
    }

    public boolean isRoleValidationRequired() {
        return Boolean.TRUE.equals(roleValidationRequired);
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
