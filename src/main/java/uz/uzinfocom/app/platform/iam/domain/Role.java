package uz.uzinfocom.app.platform.iam.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "role",
        indexes = {
                @Index(name = "idx_role_name", columnList = "name"),
                @Index(name = "idx_role_active_deleted", columnList = "active, deleted")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "description_uz", length = 1000)
    private String descriptionUz;

    @Column(name = "description_ru", length = 1000)
    private String descriptionRu;

    @Column(name = "description_uz_cyril", length = 1000)
    private String descriptionUzCyril;

    @Column(name = "description_kaa", length = 1000)
    private String descriptionKaa;

    @Builder.Default
    @OneToMany(
            mappedBy = "role",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<RolePermission> rolePermissions = new LinkedHashSet<>();

    public void replacePermissions(Set<RolePermission> newPermissions) {
        this.rolePermissions.clear();
        newPermissions.forEach(this::addPermission);
    }

    public void addPermission(RolePermission rolePermission) {
        rolePermission.setRole(this);
        this.rolePermissions.add(rolePermission);
    }

    public boolean isAvailableForAuthorization() {
        return Boolean.TRUE.equals(this.active)
                && Boolean.FALSE.equals(this.deleted);
    }
}