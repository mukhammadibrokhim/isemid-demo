package uz.uzinfocom.app.modules.form0581.application.shared;

import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.orchestration.scope.FormAccessScopeResolver;

import java.util.HashSet;
import java.util.Set;

/**
 * Form0581 counterpart of {@code Form058AffiliatedOrganizationsResolver} —
 * see its javadoc.
 */
public final class Form0581AffiliatedOrganizationsResolver {

    private Form0581AffiliatedOrganizationsResolver() {
    }

    public static Set<Long> resolve(Patient patient) {
        if (patient == null) {
            return new HashSet<>();
        }
        Set<Long> organizationIds = new HashSet<>();
        for (PatientAffiliation affiliation : patient.getAffiliations()) {
            if (FormAccessScopeResolver.AFFILIATION_TYPES.contains(affiliation.getType())
                    && affiliation.getOrganizationId() != null) {
                organizationIds.add(affiliation.getOrganizationId());
            }
        }
        return organizationIds;
    }
}
