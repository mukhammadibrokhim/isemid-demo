package uz.uzinfocom.app.modules.form129.application.command.accept;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ScopeViolationException;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.modules.form129.domain.exception.InvalidForm129StateException;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Form129AcceptValidatorTest {

    private final Form129AcceptValidator validator = new Form129AcceptValidator(new AdminAccessGuard());

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void receiverOrganizationCanAcceptASentForm() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatCode(() -> validator.validateAccept(form(Form129Status.SENT)))
                .doesNotThrowAnyException();
    }

    @Test
    void senderOrganizationCannotAccept() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validateAccept(form(Form129Status.SENT)))
                .isInstanceOf(Form129ScopeViolationException.class);
    }

    @Test
    void anAlreadyAcceptedFormCannotBeAcceptedAgain() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatThrownBy(() -> validator.validateAccept(form(Form129Status.ACCEPTED)))
                .isInstanceOf(InvalidForm129StateException.class);
    }

    @Test
    void superAdminMayAcceptWithoutOrganizationScope() {
        AdminAccessGuard superAdminGuard = mock(AdminAccessGuard.class);
        when(superAdminGuard.isSuperAdmin()).thenReturn(true);
        Form129AcceptValidator superAdminValidator = new Form129AcceptValidator(superAdminGuard);

        assertThatCode(() -> superAdminValidator.validateAccept(form(Form129Status.SENT)))
                .doesNotThrowAnyException();
    }

    private Form129 form(Form129Status status) {
        Form129 form129 = new Form129();
        form129.setStatus(status);
        form129.setSenderOrganizationId(10L);
        form129.setReceiverOrganizationId(20L);
        return form129;
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }
}
