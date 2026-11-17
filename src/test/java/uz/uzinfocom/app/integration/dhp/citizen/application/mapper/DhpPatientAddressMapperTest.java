package uz.uzinfocom.app.integration.dhp.citizen.application.mapper;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenAddressType;
import uz.uzinfocom.app.integration.api2.citizen.web.CitizenAddressResponse;
import uz.uzinfocom.app.integration.dhp.citizen.DhpCitizenTestData;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DhpPatientAddressMapperTest {

    private final ReferenceLookupService lookup = mock(ReferenceLookupService.class);
    private final DhpPatientAddressMapper mapper = new DhpPatientAddressMapper(lookup);

    @Test
    void mapsTheHomeAddressWithSoatoResolvedRegionAndDistrict() {
        when(lookup.findRegion("1703")).thenReturn(
                new ReferenceItem("UZ-AN", "UZ", "Andijon viloyati", null, null, null, 1703, null, null));
        when(lookup.findDistrict("1703203")).thenReturn(
                new ReferenceItem("AN-203", "UZ-AN", "Andijon tumani", null, null, null, 1703203, null, null));
        when(lookup.getRegionName("UZ-AN")).thenReturn("Andijon viloyati");
        when(lookup.getDistrictName("AN-203")).thenReturn("Andijon tumani");

        List<CitizenAddressResponse> addresses = mapper.map(DhpCitizenTestData.patient());

        assertThat(addresses).hasSize(1);
        CitizenAddressResponse address = addresses.get(0);
        assertThat(address.type()).isEqualTo(CitizenAddressType.PERMANENT);
        assertThat(address.regionCode()).isEqualTo("UZ-AN");
        assertThat(address.regionName()).isEqualTo("Andijon viloyati");
        assertThat(address.districtCode()).isEqualTo("AN-203");
        assertThat(address.districtName()).isEqualTo("Andijon tumani");
        assertThat(address.streetAddress()).isEqualTo("Бешпахлавон МФЙ, Шодлик кучаси, 8-уй");
        assertThat(address.neighborhoodCode()).isNull();
        assertThat(address.cadastre()).isNull();
    }

    @Test
    void unresolvedCodesStillReturnTheAddressLine() {
        List<CitizenAddressResponse> addresses = mapper.map(DhpCitizenTestData.patient());

        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).regionCode()).isNull();
        assertThat(addresses.get(0).districtCode()).isNull();
        assertThat(addresses.get(0).streetAddress()).contains("Шодлик кучаси");
    }

    @Test
    void usesTheJoinedLineWhenThereIsNoText() {
        var patient = DhpCitizenTestData.JSON.readTree("""
                {"resourceType":"Patient","address":[{"use":"temp","line":["A","B"]}]}
                """);

        List<CitizenAddressResponse> addresses = mapper.map(patient);

        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).type()).isEqualTo(CitizenAddressType.TEMPORARY);
        assertThat(addresses.get(0).streetAddress()).isEqualTo("A, B");
    }

    @Test
    void oldAndWorkAddressesAreSkippedAndNoAddressMeansEmptyList() {
        var patient = DhpCitizenTestData.JSON.readTree("""
                {"resourceType":"Patient","address":[{"use":"old","text":"x"},{"use":"work","text":"y"}]}
                """);

        assertThat(mapper.map(patient)).isEmpty();
        assertThat(mapper.map(DhpCitizenTestData.JSON.readTree("{\"resourceType\":\"Patient\"}"))).isEmpty();
    }
}
