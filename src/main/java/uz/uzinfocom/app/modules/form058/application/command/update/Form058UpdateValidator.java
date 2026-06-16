package uz.uzinfocom.app.modules.form058.application.command.update;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
public class Form058UpdateValidator {

    public void validate(Form058 form058, UpdateForm058Command command) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form058.getSenderOrganizationId())) {
            throw new Form058ScopeViolationException();
        }

        Long receiverOrganizationId = command.receiverOrganizationId() == null
                ? form058.getReceiverOrganizationId()
                : command.receiverOrganizationId();

        if (Objects.equals(form058.getSenderOrganizationId(), receiverOrganizationId)) {
            throw new Form058ValidationException("error.form058.sender-receiver-same");
        }

        form058.ensureEditable();
    }
}
