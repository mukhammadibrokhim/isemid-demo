package uz.uzinfocom.app.modules.form058.application.shared;

import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.orchestration.scope.FormAccessScopeResolver;

import java.util.HashSet;
import java.util.Set;

/**
 * Which organizations currently have {@code /affiliated} visibility into a
 * patient (WORKPLACE/EDUCATIONAL affiliations only, same set {@code
 * FormAccessScopeResolver}/{@code Form058Specification} use) — reads only
 * the already-loaded {@code patient.getAffiliations()} collection, no query.
 * Callers are responsible for excluding sender/receiver from the result
 * where that applies (e.g. before attaching it to a routing/notification
 * event) — this returns the raw affiliated set.
 */
public final class Form058AffiliatedOrganizationsResolver {

    private Form058AffiliatedOrganizationsResolver() {
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
