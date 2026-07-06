package uz.uzinfocom.app.platform.persistence.audit;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.persistence.entity.OrganizationScopedEntity;

import java.util.UUID;

public class OrganizationAuditListener {

    @PrePersist
    public void onPrePersist(OrganizationScopedEntity entity) {
        UUID organizationUuid = resolveRequiredOrganizationUuid();
        entity.initializeOrganizationAudit(organizationUuid);
    }

    @PreUpdate
    public void onPreUpdate(OrganizationScopedEntity entity) {
        UUID organizationUuid = resolveRequiredOrganizationUuid();

        entity.updateOrganizationAudit(organizationUuid);
    }

    private UUID resolveRequiredOrganizationUuid() {
        Organization organization = CurrentAuditContext.currentOrganization();

        if (organization == null) {
            throw new IllegalStateException(
                    "Current organization is missing from audit context"
            );
        }

        UUID organizationUuid = organization.getUuid();

        if (organizationUuid == null) {
            throw new IllegalStateException(
                    "Current organization UUID is missing"
            );
        }

        return organizationUuid;
    }
}