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
        name = "permission",
        indexes = {
                @Index(name = "idx_permission_subject", columnList = "subject"),
                @Index(name = "idx_permission_active_deleted", columnList = "active, deleted")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 200)
    private String subject;

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

    public void softDelete() {
        this.active = false;
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.active = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    public boolean isAvailableForAuthorization() {
        return Boolean.TRUE.equals(this.active)
                && Boolean.FALSE.equals(this.deleted);
    }
}