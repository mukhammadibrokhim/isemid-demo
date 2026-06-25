package uz.uzinfocom.app.modules.form058.application.validator;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Command;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
public class Form058CreateValidator {

    public void validate(CreateForm058Command command) {
        if (command == null) {
            throw new Form058ValidationException("validation.form058.required");
        }
        if (command.patient() == null) {
            throw new Form058ValidationException("validation.form058.patient.required");
        }
        if (Objects.equals(command.senderOrganizationId(), command.receiverOrganizationId())) {
            throw new Form058ValidationException("error.form058.sender-receiver-same");
        }

        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, command.senderOrganizationId())) {
            throw new Form058ScopeViolationException();
        }
    }
}
