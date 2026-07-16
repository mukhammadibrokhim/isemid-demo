package uz.uzinfocom.app.modules.form058.application.command.delete;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Form058DeleteValidatorTest {

    private final Form058DeleteValidator validator = new Form058DeleteValidator(new AdminAccessGuard());

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void acceptsSentForm() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatCode(() -> validator.validate(form(FormStatus.SENT)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsApprovedForm() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validate(form(FormStatus.APPROVED)))
                .isInstanceOf(InvalidForm058StateException.class);
    }

    @Test
    void rejectsSenderOutsideCurrentOrganization() {
        CurrentOrganizationContext.set(organization(99L));

        assertThatThrownBy(() -> validator.validate(form(FormStatus.SENT)))
                .isInstanceOf(Form058ScopeViolationException.class);
    }

    private Form058 form(FormStatus status) {
        Form058 form058 = new Form058();
        form058.setStatus(status);
        form058.setSenderOrganizationId(10L);
        return form058;
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }
}
