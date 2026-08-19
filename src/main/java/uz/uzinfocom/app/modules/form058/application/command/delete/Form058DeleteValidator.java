package uz.uzinfocom.app.modules.form058.application.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form058DeleteValidator {

    private final AdminAccessGuard securityAccessGuard;

    /**
     * {@code CANCELED} is normally not {@link FormStatus#physicallyDeletable()}
     * — it is a locked dead end for both organizations, whether it got there
     * via {@code cancel()} or {@code reject()} — but a super admin may still
     * clean one up, same as they may {@code reopen} it. Every other status
     * keeps the regular deletability rule even for a super admin (an
     * approved medical record stays protected).
     */
    public void validate(Form058 form058) {
        boolean superAdmin = securityAccessGuard.isSuperAdmin();

        if (!(superAdmin && form058.getStatus() == FormStatus.CANCELED)) {
            validateDeleteState(form058);
        }

        if (superAdmin) {
            return;
        }

        validateSenderOrganizationScope(form058);
    }

    private void validateSenderOrganizationScope(Form058 form058) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);

        if (!Objects.equals(
                currentOrganizationId,
                form058.getSenderOrganizationId()
        )) {
            throw new Form058ScopeViolationException();
        }
    }

    private void validateDeleteState(Form058 form058) {
        if (!form058.getStatus().physicallyDeletable()) {
            throw new InvalidForm058StateException(
                    "error.form058.delete-not-allowed",
                    form058.getStatus()
            );
        }
    }
}