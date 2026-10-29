package uz.uzinfocom.app.integration.api2.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;

import static org.assertj.core.api.Assertions.assertThat;

class Api2LookupAccessGuardTest {

    private final Api2LookupAccessGuard guard = new Api2LookupAccessGuard(new AdminAccessGuard());

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PERMISSION_ATTACHED_CARDS_CREATE",
            "PERMISSION_ATTACHED_CARDS_UPDATE",
            "PERMISSION_ATTACHED_CARDS_MANAGE",
            "PERMISSION_ATTACH_ACT_UPDATE",
            "PERMISSION_ATTACH_ACT_MANAGE",
            "ROLE_isemid_admin",
            "ROLE_isemid_super_admin"
    })
    void cardAndActStaffAndAdminsMayLookUpBothCitizensAndLegalEntities(String authority) {
        authenticate(authority);

        assertThat(guard.canLookupCitizen()).isTrue();
        assertThat(guard.canLookupLegalEntity()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PERMISSION_FORM058_SENDER_CREATE",
            "PERMISSION_FORM058_SENDER_UPDATE",
            "PERMISSION_FORM058_SENDER_MANAGE",
            "PERMISSION_FORM058_RECEIVER_UPDATE",
            "PERMISSION_FORM058_RECEIVER_MANAGE",
            "PERMISSION_FORM0581_CREATE",
            "PERMISSION_FORM0581_UPDATE",
            "PERMISSION_FORM0581_MANAGE"
    })
    void formCreatorsAndEditorsMayLookUpCitizensButNotLegalEntities(String authority) {
        authenticate(authority);

        assertThat(guard.canLookupCitizen()).isTrue();
        assertThat(guard.canLookupLegalEntity()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PERMISSION_FORM058_SENDER_READ",
            "PERMISSION_FORM058_RECEIVER_READ",
            "PERMISSION_FORM0581_READ",
            "PERMISSION_ATTACHED_CARDS_READ",
            "PERMISSION_ATTACH_ACT_READ",
            "PERMISSION_ATTACH_ACT_ASSIGN",
            "PERMISSION_REPORTS_READ",
            "ROLE_isemid_epidemiologist"
    })
    void readOnlyAndUnrelatedGrantsMayLookUpNothing(String authority) {
        authenticate(authority);

        assertThat(guard.canLookupCitizen()).isFalse();
        assertThat(guard.canLookupLegalEntity()).isFalse();
    }

    @Test
    void deniesAnonymousAndMissingAuthentication() {
        assertThat(guard.canLookupCitizen()).isFalse();
        assertThat(guard.canLookupLegalEntity()).isFalse();

        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("PERMISSION_ATTACH_ACT_UPDATE")));
        assertThat(guard.canLookupCitizen()).isFalse();
        assertThat(guard.canLookupLegalEntity()).isFalse();
    }

    private static void authenticate(String authority) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                "user", null, AuthorityUtils.createAuthorityList(authority)));
    }
}
