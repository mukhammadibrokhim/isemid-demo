package uz.uzinfocom.app.modules.form0581.application.command.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.application.security.Form0581AccessGuard;
import uz.uzinfocom.app.modules.form0581.domain.exception.InvalidForm0581StateException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form0581CancelValidator {

    private final Form0581AccessGuard form0581AccessGuard;

    public void validate(Form0581 form0581) {
        if (!form0581.getStatus().isCancellable()) {
            throw new InvalidForm0581StateException("error.form0581.cancel-not-allowed", form0581.getStatus());
        }

        if (form0581AccessGuard.isSuperAdmin()) {
            return;
        }

        validateSenderOrganizationScope(form0581);
    }

    private void validateSenderOrganizationScope(Form0581 form0581) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form0581ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form0581.getSenderOrganizationId())) {
            throw new Form0581ScopeViolationException();
        }
    }
}
