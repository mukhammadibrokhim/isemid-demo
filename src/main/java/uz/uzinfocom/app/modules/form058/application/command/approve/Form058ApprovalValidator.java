package uz.uzinfocom.app.modules.form058.application.command.approve;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

/**
 * The sender's final approval decision — only reachable once a card has
 * been linked to the form ({@link uz.uzinfocom.app.modules.form058.domain.enums.FormStatus#isApprovable()}).
 * The receiver's earlier accept/reject decision on a freshly {@code SENT}
 * form is a separate step owned by the receiver organization; see
 * {@code Form058AcceptValidator}. Approval itself belongs to the sender —
 * the institution that originally diagnosed and reported the case — not the
 * receiving SANEPID_SERVICE organization.
 */
@Component
@RequiredArgsConstructor
public class Form058ApprovalValidator {

    private final AdminAccessGuard form058AccessGuard;

    public void validateApprove(Form058 form058) {
        if (!form058.getStatus().isApprovable()) {
            throw new InvalidForm058StateException("error.form058.approve-not-allowed", form058.getStatus());
        }

        if (form058AccessGuard.isSuperAdmin()) {
            return;
        }

        validateSenderOrganizationScope(form058);
    }

    private void validateSenderOrganizationScope(Form058 form058) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form058.getSenderOrganizationId())) {
            throw new Form058ScopeViolationException();
        }
    }
}
