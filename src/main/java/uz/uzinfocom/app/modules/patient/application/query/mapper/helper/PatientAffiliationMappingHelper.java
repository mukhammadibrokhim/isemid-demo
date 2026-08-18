package uz.uzinfocom.app.modules.patient.application.query.mapper.helper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationIdResolver;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;

/**
 * Resolves an affiliation's display name from the organization registry
 * (locale-aware) whenever it references a registered organizationId, since
 * the stored organizationName can go stale as the organization's own name
 * changes. Falls back to the stored organizationName only when there is no
 * organizationId - the case of an organization not registered here, which
 * integration callers (DMED and others) may still submit via
 * organizationUuid/organizationName (see IntegrationPatientAffiliationRequest).
 */
@Component
@RequiredArgsConstructor
public class PatientAffiliationMappingHelper {

    private final OrganizationIdResolver organizationIdResolver;
    private final OrganizationNameResolver organizationNameResolver;

    @Named("resolveAffiliationOrganizationName")
    public String resolveAffiliationOrganizationName(PatientAffiliation affiliation) {
        if (affiliation == null) {
            return null;
        }

        Long organizationId = affiliation.getOrganizationId();
        if (organizationId == null) {
            return affiliation.getOrganizationName();
        }

        return organizationNameResolver.resolve(organizationIdResolver.resolveActiveNameFields(organizationId));
    }
}
