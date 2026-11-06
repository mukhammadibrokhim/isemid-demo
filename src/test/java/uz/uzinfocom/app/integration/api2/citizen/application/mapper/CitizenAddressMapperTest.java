package uz.uzinfocom.app.integration.api2.citizen.application.mapper;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenAddressType;
import uz.uzinfocom.app.integration.api2.citizen.web.CitizenAddressResponse;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CitizenAddressMapperTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);
    private final CitizenAddressMapper mapper = new CitizenAddressMapper(referenceLookupService);

    @Test
    void resolvesPermanentAddressBySoatoIdAndPassesThroughUnresolvedNeighborhoodName() {
        when(referenceLookupService.findRegion(eq("1726"))).thenReturn(
                new ReferenceItem("UZ-TK", "UZ", "Toshkent shahri", null, null, null, 1726, null, null));
        when(referenceLookupService.findDistrict(eq("1726294"))).thenReturn(
                new ReferenceItem("TK-294", "UZ-TK", "Chilonzor tumani", null, null, null, 1726294, null, null));
        when(referenceLookupService.getRegionName("UZ-TK")).thenReturn("Toshkent shahri");
        when(referenceLookupService.getDistrictName("TK-294")).thenReturn("Chilonzor tumani");
        // findNeighborhood("103-0105") is left unmocked -> returns null, exercising the
        // unresolved-Maxalla fallback path (raw name passed through, code stays null).

        String json = """
                {
                    "Data": {
                        "PermanentRegistration": {
                            "Address": "Катортол МФЙ, 9 мавзеси, 19-уй, 12-хонадон",
                            "Cadastre": "10:03:03:02:04:5016:0001:012",
                            "Region": {"Id": 10, "Value": "ТОШКЕНТ ШАҲРИ", "soato_id": 1726},
                            "District": {"Id": 1007, "Value": "ЧИЛОНЗОР ТУМАНИ", "soato_id": 1726294},
                            "Maxalla": {"Guid": "103-0105", "Id": 408, "Value": "КАТОРТОЛ МФЙ"},
                            "RegistrationDate": "2003-07-09T00:00:00"
                        },
                        "TemproaryRegistrations": null
                    }
                }
                """;

        List<CitizenAddressResponse> addresses = mapper.map(jsonMapper.readTree(json));

        assertThat(addresses).hasSize(1);
        CitizenAddressResponse permanent = addresses.get(0);
        assertThat(permanent.type()).isEqualTo(CitizenAddressType.PERMANENT);
        assertThat(permanent.regionCode()).isEqualTo("UZ-TK");
        assertThat(permanent.regionName()).isEqualTo("Toshkent shahri");
        assertThat(permanent.districtCode()).isEqualTo("TK-294");
        assertThat(permanent.districtName()).isEqualTo("Chilonzor tumani");
        assertThat(permanent.neighborhoodCode()).isNull();
        assertThat(permanent.neighborhoodName()).isEqualTo("КАТОРТОЛ МФЙ");
        assertThat(permanent.streetAddress()).isEqualTo("Катортол МФЙ, 9 мавзеси, 19-уй, 12-хонадон");
        assertThat(permanent.cadastre()).isEqualTo("10:03:03:02:04:5016:0001:012");
        assertThat(permanent.registrationDate()).isEqualTo("2003-07-09T00:00:00");
    }

    @Test
    void resolvesNeighborhoodByUzcadRegistryCodeGuidWhenAMatchExistsAndExposesItsTinNotItsInternalCode() {
        when(referenceLookupService.findNeighborhood(eq("103-0105"))).thenReturn(
                new ReferenceItem("TK-294003", "TK-294", "Qatortol", null, null, null, null, "202853324", "103-0105"));
        when(referenceLookupService.getNeighborhoodName("TK-294003")).thenReturn("Qatortol");

        String json = """
                {
                    "Data": {
                        "PermanentRegistration": {
                            "Maxalla": {"Guid": "103-0105", "Id": 408, "Value": "КАТОРТОЛ МФЙ"}
                        }
                    }
                }
                """;

        CitizenAddressResponse permanent = mapper.map(jsonMapper.readTree(json)).get(0);

        assertThat(permanent.neighborhoodCode()).isEqualTo("202853324");
        assertThat(permanent.neighborhoodName()).isEqualTo("Qatortol");
    }

    @Test
    void missingSoatoIdLeavesRegionAndDistrictUnresolvedWithoutCallingLookupService() {
        String json = """
                {
                    "Data": {
                        "PermanentRegistration": {
                            "Address": "some address",
                            "Region": {"Id": 10, "Value": "ТОШКЕНТ ШАҲРИ"},
                            "District": {"Id": 1007, "Value": "ЧИЛОНЗОР ТУМАНИ"}
                        }
                    }
                }
                """;

        List<CitizenAddressResponse> addresses = mapper.map(jsonMapper.readTree(json));

        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).regionCode()).isNull();
        assertThat(addresses.get(0).districtCode()).isNull();
    }

    @Test
    void nullDataReturnsEmptyList() {
        String json = "{\"AnswereId\": 0, \"AnswereMessage\": \"Not found\", \"Data\": null}";

        assertThat(mapper.map(jsonMapper.readTree(json))).isEmpty();
    }

    @Test
    void nullPayloadReturnsEmptyList() {
        assertThat(mapper.map(null)).isEmpty();
    }

    @Test
    void temporaryRegistrationsArrayIsMappedAsMultipleTemporaryEntries() {
        String json = """
                {
                    "Data": {
                        "PermanentRegistration": null,
                        "TemproaryRegistrations": [
                            {"Address": "temp address 1"},
                            {"Address": "temp address 2"}
                        ]
                    }
                }
                """;

        List<CitizenAddressResponse> addresses = mapper.map(jsonMapper.readTree(json));

        assertThat(addresses).hasSize(2);
        assertThat(addresses).allMatch(address -> address.type() == CitizenAddressType.TEMPORARY);
        assertThat(addresses).extracting(CitizenAddressResponse::streetAddress)
                .containsExactly("temp address 1", "temp address 2");
    }
}
