package uz.uzinfocom.app.platform.integrationclient.domain;

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

import java.time.Instant;

/**
 * A registered machine caller for the inbound integration API — external
 * systems (labs, hospital information systems, other registries) that submit
 * case data directly, as opposed to a human browser session. Bound to a
 * default {@code organizationId} at provisioning time; every submission's
 * {@code X-Organization-Id} header must resolve to this same organization —
 * the header is required, but a client can never spoof a different one it
 * names itself. {@link #sourceKey} is the fixed URL path segment this client
 * is known by, e.g. {@code "dmed"} for {@code POST /integration/dmed/form-058}.
 */
@Getter
@Setter
@Entity
@Table(
        name = "integration_client",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_integration_client_client_id", columnNames = "client_id"),
                @UniqueConstraint(name = "uk_integration_client_source_key", columnNames = "source_key")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationClient extends AuditableEntity {

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "client_secret_hash", nullable = false, length = 255)
    private String clientSecretHash;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "source_key", nullable = false, length = 64)
    private String sourceKey;

    @Column(nullable = false, length = 255)
    private String name;

    /** Comma-separated {@link IntegrationScope#getClaim()} values. */
    @Column(nullable = false, length = 500)
    private String scopes;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}
