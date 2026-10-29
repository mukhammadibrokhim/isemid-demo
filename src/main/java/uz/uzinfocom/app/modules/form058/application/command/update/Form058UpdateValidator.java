package uz.uzinfocom.app.modules.form058.application.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form058UpdateValidator {

    private final AdminAccessGuard adminAccessGuard;
    private final OrganizationRepository organizationRepository;

    /**
     * A super admin is exempt from the sender-organization scope check
     * always, and — only for a {@code CANCELED} form — from
     * {@link Form058#ensureEditable()} too, so they can fix up and resubmit
     * a closed form on the sender's behalf (whether it was closed via
     * {@code cancel()} or {@code reject()}). Every other locked status
     * (APPROVED) stays off-limits even to a super admin.
     */
    public void validate(Form058 form058, UpdateForm058Command command) {
        boolean superAdmin = adminAccessGuard.isSuperAdmin();

        if (!superAdmin) {
            Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                    .map(Organization::getId)
                    .orElseThrow(Form058ScopeViolationException::new);

            if (!Objects.equals(currentOrganizationId, form058.getSenderOrganizationId())) {
                throw new Form058ScopeViolationException();
            }
        }

        Long receiverOrganizationId = command.receiverOrganizationId() == null
                ? form058.getReceiverOrganizationId()
                : command.receiverOrganizationId();

        if (Objects.equals(form058.getSenderOrganizationId(), receiverOrganizationId)) {
            throw new Form058ValidationException("error.form058.sender-receiver-same");
        }

        if (command.receiverOrganizationId() != null) {
            Organization receiver = organizationRepository.findById(command.receiverOrganizationId())
                    .orElseThrow(() -> new Form058ValidationException(
                            "error.organization.not-found", command.receiverOrganizationId()
                    ));

            if (receiver.getMedicalType() != MedicalType.SANEPID_SERVICE) {
                throw new Form058ValidationException("error.form058.receiver-not-sanepid");
            }
        }

        if (superAdmin && form058.getStatus() == FormStatus.CANCELED) {
            return;
        }

        form058.ensureEditable();
    }
}
