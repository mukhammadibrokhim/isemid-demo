package uz.uzinfocom.app.modules.form0581.application.command.delete;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.application.security.Form0581AccessGuard;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.domain.exception.InvalidForm0581StateException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Form0581DeleteValidatorTest {

    private final Form0581DeleteValidator validator = new Form0581DeleteValidator(new Form0581AccessGuard());

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void acceptsSentForm() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatCode(() -> validator.validate(form(Form0581Status.SENT)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsApprovedForm() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validate(form(Form0581Status.APPROVED)))
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void rejectsSenderOutsideCurrentOrganization() {
        CurrentOrganizationContext.set(organization(99L));

        assertThatThrownBy(() -> validator.validate(form(Form0581Status.SENT)))
                .isInstanceOf(Form0581ScopeViolationException.class);
    }

    private Form0581 form(Form0581Status status) {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(status);
        form0581.setSenderOrganizationId(10L);
        return form0581;
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }
}
