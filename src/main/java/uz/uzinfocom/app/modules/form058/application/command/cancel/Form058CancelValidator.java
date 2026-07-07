package uz.uzinfocom.app.modules.form058.application.command.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.security.Form058AccessGuard;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form058CancelValidator {

    private final Form058AccessGuard form058AccessGuard;

    public void validate(Form058 form058) {
        if (!form058.getStatus().isCancellable()) {
            throw new InvalidForm058StateException("error.form058.cancel-not-allowed", form058.getStatus());
        }

        if (form058AccessGuard.isSuperAdmin()) {
            return;
        }

        validateSenderOrganizationScope(form058);
    }

    private void validateSenderOrganizationScope(Form058 form058) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form058.getSenderOrganizationId())) {
            throw new Form058ScopeViolationException();
        }
    }
}
