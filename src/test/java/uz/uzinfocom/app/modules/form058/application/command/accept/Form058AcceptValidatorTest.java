package uz.uzinfocom.app.modules.form058.application.command.accept;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Form058AcceptValidatorTest {

    private final Form058AcceptValidator validator = new Form058AcceptValidator(new AdminAccessGuard());

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void receiverOrganizationCanAcceptASentForm() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatCode(() -> validator.validateAccept(form(FormStatus.SENT)))
                .doesNotThrowAnyException();
    }

    @Test
    void senderOrganizationCannotAccept() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validateAccept(form(FormStatus.SENT)))
                .isInstanceOf(Form058ScopeViolationException.class);
    }

    @Test
    void anAlreadyAcceptedFormCannotBeAcceptedAgain() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatThrownBy(() -> validator.validateAccept(form(FormStatus.ACCEPTED)))
                .isInstanceOf(InvalidForm058StateException.class);
    }

    @Test
    void superAdminMayAcceptWithoutOrganizationScope() {
        AdminAccessGuard superAdminGuard = mock(AdminAccessGuard.class);
        when(superAdminGuard.isSuperAdmin()).thenReturn(true);
        Form058AcceptValidator superAdminValidator = new Form058AcceptValidator(superAdminGuard);

        assertThatCode(() -> superAdminValidator.validateAccept(form(FormStatus.SENT)))
                .doesNotThrowAnyException();
    }

    private Form058 form(FormStatus status) {
        Form058 form058 = new Form058();
        form058.setStatus(status);
        form058.setSenderOrganizationId(10L);
        form058.setReceiverOrganizationId(20L);
        return form058;
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }
}
