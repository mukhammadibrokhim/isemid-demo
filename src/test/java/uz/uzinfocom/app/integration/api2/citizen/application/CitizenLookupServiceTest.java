package uz.uzinfocom.app.integration.api2.citizen.application;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupSource;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.integration.api2.citizen.exception.CitizenLookupValidationException;
import uz.uzinfocom.app.integration.api2.citizen.strategy.CitizenLookupStrategy;
import uz.uzinfocom.app.integration.api2.citizen.strategy.CitizenLookupStrategyRegistry;
import uz.uzinfocom.app.integration.api2.citizen.validation.CitizenLookupValidator;
import uz.uzinfocom.app.integration.dhp.citizen.application.DhpCitizenLookupService;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The login provider decides the data source: DHP callers use DHP, everyone else API2. */
class CitizenLookupServiceTest {

    private static final String NNUZB = "32501852930042";

    private final CitizenLookupStrategyRegistry registry = mock(CitizenLookupStrategyRegistry.class);
    private final CitizenLookupStrategy api2Strategy = mock(CitizenLookupStrategy.class);
    private final DhpCitizenLookupService dhp = mock(DhpCitizenLookupService.class);
    private final CurrentUserProvider currentUser = mock(CurrentUserProvider.class);
    private final CitizenLookupService service =
            new CitizenLookupService(new CitizenLookupValidator(), registry, dhp, currentUser);

    private final CitizenLookupResult api2Result =
            new CitizenLookupResult(CitizenLookupType.NNUZB, CitizenLookupSource.CP, 200, null);
    private final CitizenLookupResult dhpResult =
            new CitizenLookupResult(CitizenLookupType.NNUZB, CitizenLookupSource.DHP, 200, null, java.util.List.of());

    @Test
    void ssoCallersStillGoThroughApi2AndNeedABirthDate() {
        when(currentUser.currentProviderKeyOrNull()).thenReturn("sso");
        when(registry.getRequired(CitizenLookupType.NNUZB)).thenReturn(api2Strategy);
        when(api2Strategy.lookup(any())).thenReturn(api2Result);

        CitizenLookupResult result = service.lookup(request(LocalDate.of(1985, 1, 25)));

        assertThat(result.source()).isEqualTo(CitizenLookupSource.CP);
        verify(dhp, never()).lookup(any());

        assertThatThrownBy(() -> service.lookup(request(null)))
                .isInstanceOf(CitizenLookupValidationException.class);
    }

    @Test
    void dhpCallersGoThroughDhpWithoutABirthDate() {
        when(currentUser.currentProviderKeyOrNull()).thenReturn("dhp");
        when(dhp.lookup(any())).thenReturn(dhpResult);

        CitizenLookupResult result = service.lookup(request(null));

        assertThat(result.source()).isEqualTo(CitizenLookupSource.DHP);
        verify(registry, never()).getRequired(any());
    }

    @Test
    void dhpCallersStillGetTheNnuzbFormatValidated() {
        when(currentUser.currentProviderKeyOrNull()).thenReturn("dhp");

        assertThatThrownBy(() -> service.lookup(
                new CitizenLookupRequest(CitizenLookupType.NNUZB, "123", null, null, null, null)))
                .isInstanceOf(CitizenLookupValidationException.class);
        verify(dhp, never()).lookup(any());
    }

    @Test
    void unknownProviderFallsBackToApi2() {
        when(currentUser.currentProviderKeyOrNull()).thenReturn(null);
        when(registry.getRequired(CitizenLookupType.NNUZB)).thenReturn(api2Strategy);
        when(api2Strategy.lookup(any())).thenReturn(api2Result);

        assertThat(service.lookup(request(LocalDate.of(1985, 1, 25))).source()).isEqualTo(CitizenLookupSource.CP);
    }

    private static CitizenLookupRequest request(LocalDate birthDate) {
        return new CitizenLookupRequest(CitizenLookupType.NNUZB, NNUZB, null, birthDate, null, null);
    }
}
