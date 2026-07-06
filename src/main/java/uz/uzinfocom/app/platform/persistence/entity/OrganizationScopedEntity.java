package uz.uzinfocom.app.platform.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.persistence.audit.OrganizationAuditListener;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(OrganizationAuditListener.class)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OrganizationScopedEntity extends UuidAuditableEntity {

    @Setter(AccessLevel.NONE)
    @Column(name = "created_org_uuid", nullable = false, updatable = false)
    private UUID createdOrgUuid;

    @Setter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_org_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private Organization createdOrg;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_org_uuid")
    private UUID updatedOrgUuid;

    @Setter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_org_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private Organization updatedOrg;

    public final void initializeOrganizationAudit(UUID organizationUuid) {
        if (organizationUuid == null) {
            throw new IllegalArgumentException(
                    "Organization UUID must not be null"
            );
        }

        if (createdOrgUuid == null) {
            createdOrgUuid = organizationUuid;
        }

        if (updatedOrgUuid == null) {
            updatedOrgUuid = organizationUuid;
        }
    }

    public final void updateOrganizationAudit(UUID organizationUuid) {
        if (organizationUuid == null) {
            throw new IllegalArgumentException(
                    "Organization UUID must not be null"
            );
        }

        updatedOrgUuid = organizationUuid;
    }
}