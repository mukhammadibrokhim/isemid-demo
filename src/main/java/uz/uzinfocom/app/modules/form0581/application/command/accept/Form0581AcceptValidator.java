package uz.uzinfocom.app.modules.form0581.application.command.accept;

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
 * The receiver's decision to accept an incoming ({@code SENT}) form — only
 * the receiver organization may make it, and only while the decision is
 * still open. Rejecting an incoming form is not a separate decision here:
 * it is handled by the shared {@code cancel} endpoint/{@code Form0581CancelValidator},
 * available to both organizations while the form is still {@code SENT}.
 */
@Component
@RequiredArgsConstructor
public class Form0581AcceptValidator {

    private final AdminAccessGuard form0581AccessGuard;

    public void validateAccept(Form0581 form0581) {
        if (!form0581.getStatus().isAcceptanceDecisionPending()) {
            throw new InvalidForm0581StateException("error.form0581.accept-not-allowed", form0581.getStatus());
        }

        if (form0581AccessGuard.isSuperAdmin()) {
            return;
        }

        validateReceiverOrganizationScope(form0581);
    }

    private void validateReceiverOrganizationScope(Form0581 form0581) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form0581ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form0581.getReceiverOrganizationId())) {
            throw new Form0581ScopeViolationException();
        }
    }
}
