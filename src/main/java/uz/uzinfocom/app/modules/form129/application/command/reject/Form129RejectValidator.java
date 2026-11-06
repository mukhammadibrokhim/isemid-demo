package uz.uzinfocom.app.modules.form129.application.command.reject;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ScopeViolationException;
import uz.uzinfocom.app.modules.form129.domain.exception.InvalidForm129StateException;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

/**
 * Rejection is only possible while the form is still {@code SENT} — see
 * {@link uz.uzinfocom.app.modules.form129.domain.enums.Form129Status#isDecisionPending()}.
 * Unlike Form0581's shared cancel (sender withdraw OR receiver reject), only
 * the receiving SES organization may reject a Form129 — there is no
 * sender-withdraw path.
 */
@Component
@RequiredArgsConstructor
public class Form129RejectValidator {

    private final AdminAccessGuard form129AccessGuard;

    public void validate(Form129 form129) {
        if (!form129.getStatus().isDecisionPending()) {
            throw new InvalidForm129StateException("error.form129.reject-not-allowed", form129.getStatus());
        }

        if (form129AccessGuard.isSuperAdmin()) {
            return;
        }

        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form129ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form129.getReceiverOrganizationId())) {
            throw new Form129ScopeViolationException();
        }
    }
}
