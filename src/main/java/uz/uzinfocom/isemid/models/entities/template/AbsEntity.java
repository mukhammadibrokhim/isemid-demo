package uz.uzinfocom.isemid.models.entities.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.uzinfocom.isemid.models.entities.User;

import java.sql.Timestamp;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public abstract class AbsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @OrderBy
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Timestamp updatedAt;

    @CreatedBy
    @Column(name = "created_by_id")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by_id")
    private Long updatedBy;

    @JsonIgnore
    private UUID createdOrgId;

    @JsonIgnore
    private UUID updatedOrgId;

    @PrePersist
    public void prePersist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            User currentUser = (User) authentication.getPrincipal();
            this.createdOrgId = currentUser.getCurrentOrganization().getUuid();
            this.updatedOrgId = currentUser.getCurrentOrganization().getUuid();
        }
    }

    @PreUpdate
    public void preUpdate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            User currentUser = (User) authentication.getPrincipal();
            this.updatedOrgId = currentUser.getCurrentOrganization().getUuid();
        }
    }
}
