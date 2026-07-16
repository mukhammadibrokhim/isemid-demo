package uz.uzinfocom.app.modules.form058.application.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Command;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.reference.repository.Mkb10Repository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form058CreateValidator {

    private final Mkb10Repository mkb10Repository;

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

        validateMkb10Code(command.mkb10Code());
        String finalMkb10Code = command.resolvedFinalMkb10Code();
        if (!Objects.equals(command.mkb10Code(), finalMkb10Code)) {
            validateMkb10Code(finalMkb10Code);
        }
    }

    /**
     * Blank/missing codes are left to bean-validation ({@code @NotBlank}) on
     * the request DTO — this only rejects a code that was actually supplied
     * but does not exist in the ICD-10 reference catalog.
     */
    private void validateMkb10Code(String mkb10Code) {
        if (mkb10Code == null) {
            return;
        }

        if (mkb10Repository.findByCodeAndDeletedFalse(mkb10Code).isEmpty()) {
            throw new Form058ValidationException("error.form058.mkb10-not-found", mkb10Code);
        }
    }
}
