package uz.uzinfocom.app.modules.form058.application.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Command;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.reference.repository.Icd10Repository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.validation.ReferenceCodeValidation;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form058CreateValidator {

    private final Icd10Repository icd10Repository;
    private final OrganizationRepository organizationRepository;

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

        Organization receiver = organizationRepository.findById(command.receiverOrganizationId())
                .orElseThrow(() -> new Form058ValidationException(
                        "error.organization.not-found", command.receiverOrganizationId()
                ));

        if (receiver.getMedicalType() != MedicalType.SANEPID_SERVICE) {
            throw new Form058ValidationException("error.form058.receiver-not-sanepid");
        }

        validateIcd10Code(command.icd10Code());
        String finalIcd10Code = command.resolvedFinalIcd10Code();
        if (!Objects.equals(command.icd10Code(), finalIcd10Code)) {
            validateIcd10Code(finalIcd10Code);
        }
    }

    private void validateIcd10Code(String icd10Code) {
        ReferenceCodeValidation.requireExists(
                icd10Code,
                code -> icd10Repository.findByCodeAndDeletedFalse(code).isPresent(),
                () -> new Form058ValidationException("error.form058.icd10-not-found", icd10Code)
        );
    }
}
