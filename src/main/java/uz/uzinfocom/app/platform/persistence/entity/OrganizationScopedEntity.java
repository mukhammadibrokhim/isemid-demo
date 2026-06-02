package uz.uzinfocom.app.platform.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.persistence.audit.CurrentAuditContext;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OrganizationScopedEntity extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_org_uuid",
            referencedColumnName = "uuid",
            updatable = false
    )
    private Organization createdOrg;

    @Column(name = "created_org_uuid", insertable = false, updatable = false)
    private UUID createdOrgUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "updated_org_uuid",
            referencedColumnName = "uuid"
    )
    private Organization updatedOrg;

    @Column(name = "updated_org_uuid", insertable = false, updatable = false)
    private UUID updatedOrgUuid;

    @PreUpdate
    protected void preUpdateOrganizationAudit() {
        Organization currentOrganization = CurrentAuditContext.currentOrganization();

        if (currentOrganization != null) {
            updatedOrg = currentOrganization;
        }
    }

}