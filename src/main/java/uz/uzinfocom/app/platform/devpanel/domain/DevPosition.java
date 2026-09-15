package uz.uzinfocom.app.platform.devpanel.domain;

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
 * A dev-panel-only lookup of positions/departments ("lavozim/bo'lim"), assignable
 * to a {@link DevUser} as descriptive profile info - not tied to the org-facing
 * {@code modules.reference} dictionaries (those are multi-language, business-data
 * lookups; this is a single-language internal-ops one). Managed at
 * {@code /v1/dev/ref/positions} - reads open to any dev-panel account, writes
 * gated the same way as everything else under {@code /v1/dev/**} (see
 * {@code DevPositionController}).
 */
@Getter
@Setter
@Entity
@Table(
        name = "dev_position",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_position_name", columnNames = "name")
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DevPosition extends AuditableEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
