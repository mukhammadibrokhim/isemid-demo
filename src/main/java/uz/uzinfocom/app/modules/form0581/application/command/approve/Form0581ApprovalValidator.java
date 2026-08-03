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
 * Shared validation for both approve and not-approve: unlike the sibling
 * {@code Form058}, both are the sender's final decision on a form it sent
 * (made after the receiver has reviewed it and linked its cards), so they
 * share the same scope check and the same "decision still open" status
 * check.
 */
@Component
@RequiredArgsConstructor
public class Form0581ApprovalValidator {

    private final AdminAccessGuard form0581AccessGuard;

    public void validateApprove(Form0581 form0581) {
        validate(form0581, "error.form0581.approve-not-allowed");
    }

    public void validateNotApprove(Form0581 form0581) {
        validate(form0581, "error.form0581.not-approve-not-allowed");
    }

    private void validate(Form0581 form0581, String stateErrorMessageCode) {
        if (!form0581.getStatus().isApprovalDecisionPending()) {
            throw new InvalidForm0581StateException(stateErrorMessageCode, form0581.getStatus());
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
