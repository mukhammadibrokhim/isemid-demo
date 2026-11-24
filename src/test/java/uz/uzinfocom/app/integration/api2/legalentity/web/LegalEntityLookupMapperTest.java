package uz.uzinfocom.app.integration.api2.legalentity.web;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class LegalEntityLookupMapperTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final LegalEntityLookupMapper mapper = new LegalEntityLookupMapper();

    @Test
    void keepsOnlyNameAndAddressesAndIgnoresNestedDirectors() {
        var data = mapper.map(json.readTree("""
                {"status":200,"data":{
                  "tin":"123456789","full_name":"ACME LLC","shortName":"ACME",
                  "legalAddress":"Toshkent, Chilonzor","factAddress":"Toshkent, Yunusobod",
                  "directors":[{"name":"Ali Valiyev","pinfl":"1"}],
                  "director":{"name":"Ali Valiyev"}
                }}
                """));

        assertThat(data.tin()).isEqualTo("123456789");
        assertThat(data.name()).isEqualTo("ACME LLC");
        assertThat(data.shortName()).isEqualTo("ACME");
        assertThat(data.legalAddress()).isEqualTo("Toshkent, Chilonzor");
        assertThat(data.actualAddress()).isEqualTo("Toshkent, Yunusobod");
    }

    @Test
    void readsFirstElementOfDataArrayAndJoinsObjectAddress() {
        var data = mapper.map(json.readTree("""
                {"data":[{"STIR":"1","Name":"X","Address":{"region":"Toshkent","street":"Navoiy"}}]}
                """));

        assertThat(data.tin()).isEqualTo("1");
        assertThat(data.name()).isEqualTo("X");
        assertThat(data.legalAddress()).isEqualTo("Toshkent, Navoiy");
    }

    @Test
    void returnsEmptyDataWhenNothingMatches() {
        assertThat(mapper.map(json.readTree("{\"data\":{\"foo\":\"bar\"}}")).isEmpty()).isTrue();
        assertThat(mapper.map(null).isEmpty()).isTrue();
    }
}
