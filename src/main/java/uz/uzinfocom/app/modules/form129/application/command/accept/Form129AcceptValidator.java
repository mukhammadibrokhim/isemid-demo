package uz.uzinfocom.app.modules.form129.application.command.accept;

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
 * The receiver's decision to accept an incoming ({@code SENT}) form — only
 * the receiver organization may make it, and only while the decision is
 * still open. Unlike Form0581, Form129 has no sender-withdraw path: reject
 * ({@code Form129RejectValidator}) is also receiver-only.
 */
@Component
@RequiredArgsConstructor
public class Form129AcceptValidator {

    private final AdminAccessGuard form129AccessGuard;

    public void validateAccept(Form129 form129) {
        if (!form129.getStatus().isDecisionPending()) {
            throw new InvalidForm129StateException("error.form129.accept-not-allowed", form129.getStatus());
        }

        if (form129AccessGuard.isSuperAdmin()) {
            return;
        }

        validateReceiverOrganizationScope(form129);
    }

    private void validateReceiverOrganizationScope(Form129 form129) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form129ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form129.getReceiverOrganizationId())) {
            throw new Form129ScopeViolationException();
        }
    }
}
