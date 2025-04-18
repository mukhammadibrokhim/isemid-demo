package uz.uzinfocom.isemid.commons.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.models.User;

import java.sql.Timestamp;
import java.util.UUID;

@Slf4j
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_org_uuid", referencedColumnName = "uuid")
    private Organization createdOrg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_org_uuid", referencedColumnName = "uuid")
    private Organization updatedOrg;

    @Column(name = "created_org_uuid", insertable = false, updatable = false)
    private UUID createdOrgUuid;

    @Column(name = "updated_org_uuid", insertable = false, updatable = false)
    private UUID updatedOrgUuid;

    @PrePersist
    public void prePersist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            User currentUser = (User) authentication.getPrincipal();
            this.createdOrgUuid = currentUser.getCurrentOrganization().getUuid();
            this.createdOrg = currentUser.getCurrentOrganization();
            this.updatedOrgUuid = currentUser.getCurrentOrganization().getUuid();
            this.updatedOrg = currentUser.getCurrentOrganization();
        }
    }

    @PreUpdate
    public void preUpdate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser.getCurrentOrganization() != null) {
                this.updatedOrgUuid = currentUser.getCurrentOrganization().getUuid();
                this.updatedOrg = currentUser.getCurrentOrganization();
            } else {
                log.error("Warning: Organization is null for entity update.");
            }
        }
    }
}
