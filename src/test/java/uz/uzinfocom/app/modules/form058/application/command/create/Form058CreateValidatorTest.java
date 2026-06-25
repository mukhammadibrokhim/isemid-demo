package uz.uzinfocom.app.modules.form058.application.command.create;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.modules.form058.application.validator.Form058CreateValidator;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Form058CreateValidatorTest {

    private final Form058CreateValidator validator = new Form058CreateValidator();

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void acceptsCurrentSenderOrganization() {
        CurrentOrganizationContext.set(organization(100L));

        assertThatCode(() -> validator.validate(command(200L)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsSenderOutsideCurrentOrganization() {
        CurrentOrganizationContext.set(organization(300L));

        assertThatThrownBy(() -> validator.validate(command(200L)))
                .isInstanceOf(Form058ScopeViolationException.class);
    }

    @Test
    void rejectsSameSenderAndReceiver() {
        CurrentOrganizationContext.set(organization(100L));

        assertThatThrownBy(() -> validator.validate(command(100L)))
                .isInstanceOf(Form058ValidationException.class);
    }

    private CreateForm058Command command(Long receiverOrganizationId) {
        return new CreateForm058Command(
                "A00",
                "Cholera",
                patient(),
                null,
                null,
                null,
                null,
                Instant.now(),
                receiverOrganizationId,
                null,
                1L,
                "Doctor",
                "J-1",
                null,
                null,
                null,
                null, null
        );
    }

    private CreatePatientCommand patient() {
        return new CreatePatientCommand(
                "Patient",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }
}
