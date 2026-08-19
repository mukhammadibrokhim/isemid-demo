package uz.uzinfocom.app.modules.form058.application.command.cancel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Form058CancelValidatorTest {

    private final Form058CancelValidator validator = new Form058CancelValidator(new AdminAccessGuard());

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void senderOrganizationCanCancelASentForm() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatCode(() -> validator.validate(form(FormStatus.SENT)))
                .doesNotThrowAnyException();
    }

    @Test
    void receiverOrganizationCanAlsoCancelASentForm() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatCode(() -> validator.validate(form(FormStatus.SENT)))
                .doesNotThrowAnyException();
    }

    @Test
    void unrelatedOrganizationCannotCancel() {
        CurrentOrganizationContext.set(organization(99L));

        assertThatThrownBy(() -> validator.validate(form(FormStatus.SENT)))
                .isInstanceOf(Form058ScopeViolationException.class);
    }

    @Test
    void anAcceptedFormCanNoLongerBeCanceledByEitherSide() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validate(form(FormStatus.ACCEPTED)))
                .isInstanceOf(InvalidForm058StateException.class);
    }

    @Test
    void aCardLinkedFormCanNoLongerBeCanceled() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatThrownBy(() -> validator.validate(form(FormStatus.CARD_LINKED)))
                .isInstanceOf(InvalidForm058StateException.class);
    }

    @Test
    void superAdminMayCancelWithoutOrganizationScope() {
        AdminAccessGuard superAdminGuard = mock(AdminAccessGuard.class);
        when(superAdminGuard.isSuperAdmin()).thenReturn(true);
        Form058CancelValidator superAdminValidator = new Form058CancelValidator(superAdminGuard);

        assertThatCode(() -> superAdminValidator.validate(form(FormStatus.SENT)))
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
