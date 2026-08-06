package uz.uzinfocom.app.modules.form0581.application.command.approve;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.domain.exception.InvalidForm0581StateException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

/**
 * The sender's final approval decision — only reachable once a card has
 * been linked to the form ({@link uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status#isApprovable()}).
 * The receiver's earlier accept/reject decision on a freshly {@code SENT}
 * form is a separate step owned by the receiver organization; see
 * {@code Form0581AcceptValidator}. Approval itself belongs to the sender —
 * the institution that originally reported the case — not the receiving
 * SANEPID_SERVICE organization.
 */
@Component
@RequiredArgsConstructor
public class Form0581ApprovalValidator {

    private final AdminAccessGuard form0581AccessGuard;

    public void validateApprove(Form0581 form0581) {
        if (!form0581.getStatus().isApprovable()) {
            throw new InvalidForm0581StateException("error.form0581.approve-not-allowed", form0581.getStatus());
        }

        if (form0581AccessGuard.isSuperAdmin()) {
            return;
        }

        validateSenderOrganizationScope(form0581);
    }

    private void validateSenderOrganizationScope(Form0581 form0581) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form0581ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form0581.getSenderOrganizationId())) {
            throw new Form0581ScopeViolationException();
        }
    }
}
