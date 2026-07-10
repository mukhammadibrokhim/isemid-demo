package uz.uzinfocom.app.modules.form0581.application.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ValidationException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form0581UpdateValidator {

    private final OrganizationRepository organizationRepository;

    public void validate(Form0581 form0581, UpdateForm0581Command command) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form0581ScopeViolationException::new);

        if (!Objects.equals(currentOrganizationId, form0581.getSenderOrganizationId())) {
            throw new Form0581ScopeViolationException();
        }

        Long receiverOrganizationId = command.receiverOrganizationId() == null
                ? form0581.getReceiverOrganizationId()
                : command.receiverOrganizationId();

        if (Objects.equals(form0581.getSenderOrganizationId(), receiverOrganizationId)) {
            throw new Form0581ValidationException("error.form0581.sender-receiver-same");
        }

        if (command.receiverOrganizationId() != null) {
            Organization receiver = organizationRepository.findById(command.receiverOrganizationId())
                    .orElseThrow(() -> new Form0581ValidationException(
                            "error.organization.not-found", command.receiverOrganizationId()
                    ));

            if (receiver.getMedicalType() != MedicalType.SANEPID_SERVICE) {
                throw new Form0581ValidationException("error.form0581.receiver-not-sanepid");
            }
        }

        form0581.ensureEditable();
    }
}
