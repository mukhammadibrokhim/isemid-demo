package uz.uzinfocom.app.modules.form058.application.command.approve;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.security.Form058AccessGuard;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

/**
 * Shared validation for both approve and not-approve: both are the
 * receiver's decision on a form it received, so they share the same
 * scope check and the same "decision still open" status check.
 */
@Component
@RequiredArgsConstructor
public class Form058ApprovalValidator {

    private final Form058AccessGuard form058AccessGuard;

    public void validateApprove(Form058 form058) {
        validate(form058, "error.form058.approve-not-allowed");
    }

    public void validateNotApprove(Form058 form058) {
        validate(form058, "error.form058.not-approve-not-allowed");
    }

    private void validate(Form058 form058, String stateErrorMessageCode) {
        if (!form058.getStatus().isApprovalDecisionPending()) {
            throw new InvalidForm058StateException(stateErrorMessageCode, form058.getStatus());
        }

        if (form058AccessGuard.isSuperAdmin()) {
            return;
        }

        validateReceiverOrganizationScope(form058);
    }

    private void validateReceiverOrganizationScope(Form058 form058) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form058.getReceiverOrganizationId())) {
            throw new Form058ScopeViolationException();
        }
    }
}
