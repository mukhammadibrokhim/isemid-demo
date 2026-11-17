package uz.uzinfocom.app.integration.dhp.citizen.application;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupSource;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.integration.api2.citizen.exception.CitizenDataNotFoundException;
import uz.uzinfocom.app.integration.dhp.citizen.DhpCitizenTestData;
import uz.uzinfocom.app.integration.dhp.citizen.application.mapper.DhpPatientAddressMapper;
import uz.uzinfocom.app.integration.dhp.citizen.application.mapper.DhpPatientCitizenMapper;
import uz.uzinfocom.app.integration.dhp.citizen.client.DhpCitizenClient;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DhpCitizenLookupServiceTest {

    private final DhpCitizenClient client = mock(DhpCitizenClient.class);
    private final DhpCitizenLookupService service = new DhpCitizenLookupService(
            client,
            new DhpPatientCitizenMapper(DhpCitizenTestData.JSON),
            new DhpPatientAddressMapper(mock(ReferenceLookupService.class)));

    @Test
    void nnuzbIsLookedUpAsNiAndReturnsApi2ShapedDataWithAddresses() {
        when(client.populate("NI", DhpCitizenTestData.NI)).thenReturn(Optional.of(DhpCitizenTestData.patient()));

        CitizenLookupResult result = service.lookup(
                new CitizenLookupRequest(CitizenLookupType.NNUZB, DhpCitizenTestData.NI, null, null, null, null));

        assertThat(result.source()).isEqualTo(CitizenLookupSource.DHP);
        assertThat(result.upstreamStatus()).isEqualTo(200);
        assertThat(result.data().get("data").get(0).get("current_pinpp").asString()).isEqualTo(DhpCitizenTestData.NI);
        assertThat(result.addresses()).hasSize(1);
    }

    @Test
    void passportIsLookedUpAsPpn() {
        when(client.populate("PPN", "AD3131830")).thenReturn(Optional.of(DhpCitizenTestData.patient()));

        service.lookup(new CitizenLookupRequest(CitizenLookupType.PPN, null, "AD3131830", null, null, null));

        verify(client).populate("PPN", "AD3131830");
    }

    @Test
    void birthCertificateIsLookedUpAsBctWithSeriesFollowedByNumber() {
        when(client.populate("BCT", "I-TN1234567")).thenReturn(Optional.of(DhpCitizenTestData.patient()));

        service.lookup(new CitizenLookupRequest(CitizenLookupType.CZ, null, null, null, "I-TN", "1234567"));

        verify(client).populate("BCT", "I-TN1234567");
    }

    @Test
    void noPatientIsACitizenNotFound() {
        when(client.populate("NI", DhpCitizenTestData.NI)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.lookup(
                new CitizenLookupRequest(CitizenLookupType.NNUZB, DhpCitizenTestData.NI, null, null, null, null)))
                .isInstanceOf(CitizenDataNotFoundException.class);
    }
}
