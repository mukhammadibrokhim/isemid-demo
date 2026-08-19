package uz.uzinfocom.app.modules.form0581.application.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.domain.exception.InvalidForm0581StateException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form0581DeleteValidator {

    private final AdminAccessGuard securityAccessGuard;

    /**
     * {@code CANCELED} is normally not {@link Form0581Status#physicallyDeletable()}
     * — it is a locked dead end for both organizations, whether it got
     * there via {@code cancel()} by the sender or the receiver — but a
     * super admin may still clean one up, same as they may {@code reopen}
     * it. Every other status keeps the regular deletability rule even for a
     * super admin (an approved medical record stays protected).
     */
    public void validate(Form0581 form0581) {
        boolean superAdmin = securityAccessGuard.isSuperAdmin();

        if (!(superAdmin && form0581.getStatus() == Form0581Status.CANCELED)) {
            validateDeleteState(form0581);
        }

        if (superAdmin) {
            return;
        }

        validateSenderOrganizationScope(form0581);
    }

    private void validateSenderOrganizationScope(Form0581 form0581) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form0581ScopeViolationException::new);

        if (!Objects.equals(
                currentOrganizationId,
                form0581.getSenderOrganizationId()
        )) {
            throw new Form0581ScopeViolationException();
        }
    }

    private void validateDeleteState(Form0581 form0581) {
        if (!form0581.getStatus().physicallyDeletable()) {
            throw new InvalidForm0581StateException(
                    "error.form0581.delete-not-allowed",
                    form0581.getStatus()
            );
        }
    }
}
