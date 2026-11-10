package uz.uzinfocom.app.modules.report.analytic.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReport;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.Objects;

/**
 * Shared update/delete ownership rule: only the user (within the organization)
 * who created an analytic report may edit or remove it — every other user or
 * organization is refused, even ones that could otherwise view it in the scoped
 * table listing. {@code isemid_admin}/{@code isemid_super_admin} bypass the check
 * entirely — mirrors {@code Form2ManualEntryOwnershipValidator}.
 */
@Component
@RequiredArgsConstructor
public class AnalyticReportOwnershipValidator {

    private final AdminAccessGuard adminAccessGuard;
    private final CurrentUserProvider currentUserProvider;

    public void validate(AnalyticReport entry) {
        if (adminAccessGuard.isAdmin()) {
            return;
        }

        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));

        if (!Objects.equals(currentOrganizationId, entry.getOrganizationId())) {
            throw new ScopeViolationException("report.analytic_report.not_owner");
        }

        // Rows predating per-user ownership have no creator and stay organization-owned.
        if (entry.getCreatedBy() != null
                && !Objects.equals(entry.getCreatedBy(), currentUserProvider.userIdOrNull())) {
            throw new ScopeViolationException("report.analytic_report.not_owner");
        }
    }
}
