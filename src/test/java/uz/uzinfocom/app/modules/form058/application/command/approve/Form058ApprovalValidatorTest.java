package uz.uzinfocom.app.modules.form058.application.command.approve;

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

class Form058ApprovalValidatorTest {

    private final Form058ApprovalValidator validator = new Form058ApprovalValidator(new AdminAccessGuard());

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void senderOrganizationCanApproveACardLinkedForm() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatCode(() -> validator.validateApprove(form(FormStatus.CARD_LINKED)))
                .doesNotThrowAnyException();
    }

    @Test
    void receiverOrganizationCannotApprove() {
        CurrentOrganizationContext.set(organization(20L));

        assertThatThrownBy(() -> validator.validateApprove(form(FormStatus.CARD_LINKED)))
                .isInstanceOf(Form058ScopeViolationException.class);
    }

    @Test
    void anAcceptedButNotYetCardLinkedFormCannotBeApproved() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validateApprove(form(FormStatus.ACCEPTED)))
                .isInstanceOf(InvalidForm058StateException.class);
    }

    @Test
    void aFreshlySentFormCannotBeApprovedWithoutACardLinkedFirst() {
        CurrentOrganizationContext.set(organization(10L));

        assertThatThrownBy(() -> validator.validateApprove(form(FormStatus.SENT)))
                .isInstanceOf(InvalidForm058StateException.class);
    }

    @Test
    void superAdminMayApproveWithoutOrganizationScope() {
        AdminAccessGuard superAdminGuard = mock(AdminAccessGuard.class);
        when(superAdminGuard.isSuperAdmin()).thenReturn(true);
        Form058ApprovalValidator superAdminValidator = new Form058ApprovalValidator(superAdminGuard);

        assertThatCode(() -> superAdminValidator.validateApprove(form(FormStatus.CARD_LINKED)))
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
