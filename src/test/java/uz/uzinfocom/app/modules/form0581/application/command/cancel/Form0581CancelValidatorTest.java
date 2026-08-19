package uz.uzinfocom.app.modules.form0581.application.command.cancel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.domain.exception.InvalidForm0581StateException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Form0581CancelValidatorTest {

    private final Form0581CancelValidator validator = new Form0581CancelValidator(new AdminAccessGuard());

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void senderOrganizationCanCancelASentForm() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatCode(() -> validator.validate(form(Form0581Status.SENT)))
                .doesNotThrowAnyException();
    }

    @Test
    void receiverOrganizationCanAlsoCancelASentForm() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatCode(() -> validator.validate(form(Form0581Status.SENT)))
                .doesNotThrowAnyException();
    }

    @Test
    void unrelatedOrganizationCannotCancel() {
        CurrentOrganizationContext.set(organization(99L));

        assertThatThrownBy(() -> validator.validate(form(Form0581Status.SENT)))
                .isInstanceOf(Form0581ScopeViolationException.class);
    }

    @Test
    void anAcceptedFormCanNoLongerBeCanceledByEitherSide() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validate(form(Form0581Status.ACCEPTED)))
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void aCardLinkedFormCanNoLongerBeCanceled() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatThrownBy(() -> validator.validate(form(Form0581Status.CARD_LINKED)))
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void superAdminMayCancelWithoutOrganizationScope() {
        AdminAccessGuard superAdminGuard = mock(AdminAccessGuard.class);
        when(superAdminGuard.isSuperAdmin()).thenReturn(true);
        Form0581CancelValidator superAdminValidator = new Form0581CancelValidator(superAdminGuard);

        assertThatCode(() -> superAdminValidator.validate(form(Form0581Status.SENT)))
                .doesNotThrowAnyException();
    }

    private Form0581 form(Form0581Status status) {
        Form0581 form0581 = new Form0581();
        form0581.setStatus(status);
        form0581.setSenderOrganizationId(10L);
        form0581.setReceiverOrganizationId(20L);
        return form0581;
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }
}
