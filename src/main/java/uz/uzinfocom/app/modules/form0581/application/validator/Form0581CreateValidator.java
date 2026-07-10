package uz.uzinfocom.app.modules.form0581.application.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Command;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ValidationException;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form0581CreateValidator {

    private final OrganizationRepository organizationRepository;

    public void validate(CreateForm0581Command command) {
        if (command == null) {
            throw new Form0581ValidationException("validation.form0581.required");
        }
        if (command.patient() == null) {
            throw new Form0581ValidationException("validation.form0581.patient.required");
        }
        if (Objects.equals(command.senderOrganizationId(), command.receiverOrganizationId())) {
            throw new Form0581ValidationException("error.form0581.sender-receiver-same");
        }

        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form0581ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, command.senderOrganizationId())) {
            throw new Form0581ScopeViolationException();
        }

        Organization receiver = organizationRepository.findById(command.receiverOrganizationId())
                .orElseThrow(() -> new Form0581ValidationException(
                        "error.organization.not-found", command.receiverOrganizationId()
                ));

        if (receiver.getMedicalType() != MedicalType.SANEPID_SERVICE) {
            throw new Form0581ValidationException("error.form0581.receiver-not-sanepid");
        }
    }
}
