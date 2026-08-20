package uz.uzinfocom.app.modules.iam.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "role_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_permissions_role_permission",
                        columnNames = {"role_id", "permission_id"}
                )
        },
        indexes = {
                @Index(name = "idx_role_permissions_role_id", columnList = "role_id"),
                @Index(name = "idx_role_permissions_permission_id", columnList = "permission_id")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permission_actions",
            joinColumns = @JoinColumn(name = "role_permission_id"),
            inverseJoinColumns = @JoinColumn(name = "action_id")
    )
    private Set<Action> actions = new LinkedHashSet<>();
}
