package uz.uzinfocom.app.modules.form058.application.command.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

/**
 * Cancel is shared between both organizations on a form: while it is still
 * {@code SENT}, the sender may withdraw it and the receiver may reject it —
 * both close the form into {@code CANCELED} the same way, via the same
 * endpoint. Once the receiver has accepted the form, cancellation is closed
 * off to both sides (see {@link uz.uzinfocom.app.modules.form058.domain.enums.FormStatus#isCancellable()});
 * only a super admin can still act on it from there.
 */
@Component
@RequiredArgsConstructor
public class Form058CancelValidator {

    private final AdminAccessGuard form058AccessGuard;

    public void validate(Form058 form058) {
        if (!form058.getStatus().isCancellable()) {
            throw new InvalidForm058StateException("error.form058.cancel-not-allowed", form058.getStatus());
        }

        if (form058AccessGuard.isSuperAdmin()) {
            return;
        }

        validateSenderOrReceiverOrganizationScope(form058);
    }

    private void validateSenderOrReceiverOrganizationScope(Form058 form058) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);

        boolean isSender = Objects.equals(currentOrganizationId, form058.getSenderOrganizationId());
        boolean isReceiver = Objects.equals(currentOrganizationId, form058.getReceiverOrganizationId());

        if (!isSender && !isReceiver) {
            throw new Form058ScopeViolationException();
        }
    }
}
