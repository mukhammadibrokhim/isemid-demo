package uz.uzinfocom.app.modules.patient.web.request;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DMED's own payloads use {@code stateCode}/{@code cityCode} for what our
 * request DTOs call {@code regionCode}/{@code districtCode} (address) and
 * {@code regionCode}/{@code cityCode} (affiliation). {@code @JsonAlias} lets
 * both naming conventions deserialize into the same field, so the shared
 * frontend contract (regionCode/districtCode) keeps working unchanged while
 * DMED's stateCode/cityCode payloads also bind correctly.
 */
class PatientAddressDmedAliasTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void addressAcceptsDmedsStateCodeAndCityCodeNaming() {
        String json = """
                {
                  "type": "PERMANENT",
                  "stateCode": "UZ-TK",
                  "cityCode": "TK-283"
                }
                """;

        CreatePatientAddressRequest address = jsonMapper.readValue(json, CreatePatientAddressRequest.class);

        assertThat(address.type()).isEqualTo(AddressType.PERMANENT);
        assertThat(address.regionCode()).isEqualTo("UZ-TK");
        assertThat(address.districtCode()).isEqualTo("TK-283");
    }

    @Test
    void addressStillAcceptsTheCanonicalRegionCodeAndDistrictCodeNaming() {
        String json = """
                {
                  "type": "PERMANENT",
                  "regionCode": "UZ-TK",
                  "districtCode": "TK-283"
                }
                """;

        CreatePatientAddressRequest address = jsonMapper.readValue(json, CreatePatientAddressRequest.class);

        assertThat(address.regionCode()).isEqualTo("UZ-TK");
        assertThat(address.districtCode()).isEqualTo("TK-283");
    }

    @Test
    void affiliationAcceptsDmedsStateCodeNaming() {
        String json = """
                {
                  "stateCode": "UZ-TK",
                  "cityCode": "TK-283"
                }
                """;

        CreatePatientAffiliationRequest affiliation =
                jsonMapper.readValue(json, CreatePatientAffiliationRequest.class);

        assertThat(affiliation.regionCode()).isEqualTo("UZ-TK");
        assertThat(affiliation.cityCode()).isEqualTo("TK-283");
    }
}
