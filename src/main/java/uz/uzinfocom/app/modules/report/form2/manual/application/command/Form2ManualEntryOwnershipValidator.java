package uz.uzinfocom.app.modules.report.form2.manual.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.report.form2.manual.domain.Form2ManualEntry;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.Objects;

/**
 * Shared update/delete ownership rule: only the organization that created a
 * Shakl №2 entry may edit or remove it — every other organization is
 * refused, even ones that could otherwise view the entry in the scoped
 * table listing. {@code isemid_admin}/{@code isemid_super_admin} bypass the
 * check entirely, mirroring {@code Form058DeleteValidator}'s pattern.
 */
@Component
@RequiredArgsConstructor
public class Form2ManualEntryOwnershipValidator {

    private final AdminAccessGuard adminAccessGuard;

    public void validate(Form2ManualEntry entry) {
        if (adminAccessGuard.isAdmin()) {
            return;
        }

        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));

        if (!Objects.equals(currentOrganizationId, entry.getOrganizationId())) {
            throw new ScopeViolationException("report.form2_manual_entry.not_owner");
        }
    }
}
