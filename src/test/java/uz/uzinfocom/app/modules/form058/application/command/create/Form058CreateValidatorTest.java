package uz.uzinfocom.app.modules.form058.application.command.create;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.modules.form058.application.validator.Form058CreateValidator;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.reference.domain.Mkb10;
import uz.uzinfocom.app.platform.reference.repository.Mkb10Repository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Form058CreateValidatorTest {

    private final Mkb10Repository mkb10Repository = mock(Mkb10Repository.class);
    private final Form058CreateValidator validator = new Form058CreateValidator(mkb10Repository);

    @BeforeEach
    void stubKnownMkb10Code() {
        when(mkb10Repository.findByCodeAndDeletedFalse("A00")).thenReturn(Optional.of(mock(Mkb10.class)));
    }

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

    @Test
    void rejectsUnknownMkb10Code() {
        CurrentOrganizationContext.set(organization(100L));
        when(mkb10Repository.findByCodeAndDeletedFalse("Z99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validate(commandWithSender(100L, 200L, "Z99", null)))
                .isInstanceOf(Form058ValidationException.class);
    }

    @Test
    void rejectsUnknownFinalMkb10CodeWhenDistinctFromPrimary() {
        CurrentOrganizationContext.set(organization(100L));
        when(mkb10Repository.findByCodeAndDeletedFalse("B99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validate(commandWithSender(100L, 200L, "A00", "B99")))
                .isInstanceOf(Form058ValidationException.class);
    }

    @Test
    void acceptsKnownFinalMkb10CodeDistinctFromPrimary() {
        CurrentOrganizationContext.set(organization(100L));
        when(mkb10Repository.findByCodeAndDeletedFalse("A01")).thenReturn(Optional.of(mock(Mkb10.class)));

        assertThatCode(() -> validator.validate(commandWithSender(100L, 200L, "A00", "A01")))
                .doesNotThrowAnyException();
    }

    /**
     * Matches the original (pre-existing) factory shape exactly — deliberately
     * leaves {@code senderOrganizationId} unset, which is what the two
     * pre-existing failing tests above ({@code acceptsCurrentSenderOrganization},
     * {@code rejectsSameSenderAndReceiver}) already depend on; not fixed here,
     * since that bug is unrelated to this validator's mkb10 checks.
     */
    private CreateForm058Command command(Long receiverOrganizationId) {
        return new CreateForm058Command(
                "A00",
                "Cholera",
                null,
                null,
                null,
                patient(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                receiverOrganizationId,
                null,
                null,
                null, null,
                null, null, null, null, null, null
        );
    }

    /**
     * Used only by the new mkb10-code tests below, which need a valid
     * sender/receiver pair to actually reach the mkb10 validation branch
     * (unlike {@link #command(Long)}, which never sets a sender at all).
     */
    private CreateForm058Command commandWithSender(
            Long senderOrganizationId,
            Long receiverOrganizationId,
            String mkb10Code,
            String finalMkb10Code
    ) {
        return new CreateForm058Command(
                mkb10Code,
                "Cholera",
                finalMkb10Code,
                null,
                null,
                patient(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                senderOrganizationId,
                receiverOrganizationId,
                null,
                null,
                null, null,
                null, null, null, null, null, null
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
