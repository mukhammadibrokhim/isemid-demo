package uz.uzinfocom.app.platform.settings.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

/**
 * A runtime-editable, DB-backed configuration value managed through the
 * admin API. Deliberately not wired to any existing hardcoded config
 * (e.g. {@code Form058SourceResolver.ALLOWED_SOURCES}) yet — this module
 * only provides the CRUD store itself; rewiring specific behaviors to read
 * from it is separate, targeted follow-up work.
 */
@Getter
@Setter
@Entity
@Table(
        name = "system_settings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_system_settings_key", columnNames = "setting_key")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting extends AuditableEntity {

    @Column(name = "setting_key", nullable = false, length = 200)
    private String settingKey;

    @Column(name = "setting_value", length = 2000)
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    private SystemSettingValueType valueType;

    @Column(length = 1000)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}
