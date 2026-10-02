package uz.uzinfocom.app.modules.form129.application.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form129.application.command.create.CreateForm129Command;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ScopeViolationException;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ValidationException;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form129CreateValidator {

    private final OrganizationRepository organizationRepository;

    public void validate(CreateForm129Command command) {
        if (command == null) {
            throw new Form129ValidationException("validation.form129.required");
        }
        if (command.patient() == null) {
            throw new Form129ValidationException("validation.form129.patient.required");
        }
        if (Objects.equals(command.senderOrganizationId(), command.receiverOrganizationId())) {
            throw new Form129ValidationException("error.form129.sender-receiver-same");
        }

        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form129ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, command.senderOrganizationId())) {
            throw new Form129ScopeViolationException();
        }

        Organization receiver = organizationRepository.findById(command.receiverOrganizationId())
                .orElseThrow(() -> new Form129ValidationException(
                        "error.organization.not-found", command.receiverOrganizationId()
                ));

        if (receiver.getMedicalType() != MedicalType.SANEPID_SERVICE) {
            throw new Form129ValidationException("error.form129.receiver-not-sanepid");
        }
    }
}
