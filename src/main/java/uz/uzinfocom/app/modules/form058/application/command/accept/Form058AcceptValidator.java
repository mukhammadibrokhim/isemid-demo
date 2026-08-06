package uz.uzinfocom.app.modules.form058.application.command.accept;

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
 * The receiver's decision to accept an incoming ({@code SENT}) form — only
 * the receiver organization may make it, and only while the decision is
 * still open. Rejecting an incoming form is not a separate decision here:
 * it is handled by the shared {@code cancel} endpoint/{@code Form058CancelValidator},
 * available to both organizations while the form is still {@code SENT}.
 */
@Component
@RequiredArgsConstructor
public class Form058AcceptValidator {

    private final AdminAccessGuard form058AccessGuard;

    public void validateAccept(Form058 form058) {
        if (!form058.getStatus().isAcceptanceDecisionPending()) {
            throw new InvalidForm058StateException("error.form058.accept-not-allowed", form058.getStatus());
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
