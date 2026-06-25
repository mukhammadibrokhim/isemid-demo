package uz.uzinfocom.app.modules.form058.application.command.delete;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
public class Form058DeleteValidator {

    public void validate(Form058 form058) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form058.getSenderOrganizationId())) {
            throw new Form058ScopeViolationException();
        }

        if (!form058.getStatus().physicallyDeletable()) {
            throw new InvalidForm058StateException("error.form058.delete-not-allowed", form058.getStatus());
        }
    }
}
