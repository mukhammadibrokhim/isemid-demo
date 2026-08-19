package uz.uzinfocom.app.modules.form0581.application.command.approve;

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

class Form0581ApprovalValidatorTest {

    private final Form0581ApprovalValidator validator = new Form0581ApprovalValidator(new AdminAccessGuard());

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void senderOrganizationCanApproveACardLinkedForm() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatCode(() -> validator.validateApprove(form(Form0581Status.CARD_LINKED)))
                .doesNotThrowAnyException();
    }

    @Test
    void receiverOrganizationCannotApprove() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatThrownBy(() -> validator.validateApprove(form(Form0581Status.CARD_LINKED)))
                .isInstanceOf(Form0581ScopeViolationException.class);
    }

    @Test
    void anAcceptedButNotYetCardLinkedFormCannotBeApproved() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validateApprove(form(Form0581Status.ACCEPTED)))
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void aFreshlySentFormCannotBeApprovedWithoutACardLinkedFirst() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validateApprove(form(Form0581Status.SENT)))
                .isInstanceOf(InvalidForm0581StateException.class);
    }

    @Test
    void superAdminMayApproveWithoutOrganizationScope() {
        AdminAccessGuard superAdminGuard = mock(AdminAccessGuard.class);
        when(superAdminGuard.isSuperAdmin()).thenReturn(true);
        Form0581ApprovalValidator superAdminValidator = new Form0581ApprovalValidator(superAdminGuard);

        assertThatCode(() -> superAdminValidator.validateApprove(form(Form0581Status.CARD_LINKED)))
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
