package uz.uzinfocom.app.modules.form0581.application.command.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.domain.exception.InvalidForm0581StateException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

/**
 * Cancellation is only possible while the form is still {@code SENT} — see
 * {@link uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status#isCancellable()}.
 * In that window either party may act: the sender withdraws it, or the
 * receiver declines the incoming "NEW" notification.
 */
@Component
@RequiredArgsConstructor
public class Form0581CancelValidator {

    private final AdminAccessGuard form0581AccessGuard;

    public void validate(Form0581 form0581) {
        if (!form0581.getStatus().isCancellable()) {
            throw new InvalidForm0581StateException("error.form0581.cancel-not-allowed", form0581.getStatus());
        }

        if (form0581AccessGuard.isSuperAdmin()) {
            return;
        }

        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form0581ScopeViolationException::new);

        boolean isSender = Objects.equals(currentOrganizationId, form0581.getSenderOrganizationId());
        boolean isReceiver = Objects.equals(currentOrganizationId, form0581.getReceiverOrganizationId());

        if (!isSender && !isReceiver) {
            throw new Form0581ScopeViolationException();
        }
    }
}
