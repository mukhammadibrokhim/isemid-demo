package uz.uzinfocom.app.modules.reference.application.geosync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.EgovNeighborhoodRow;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.EgovSoatoRow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EgovClassifierClientTest {

    private static final String TOKEN = "test-token-123";

    private EgovClassifierProperties properties;
    private MockRestServiceServer server;
    private EgovClassifierClient client;

    @BeforeEach
    void setUp() {
        properties = new EgovClassifierProperties();
        properties.setAccessToken(TOKEN);
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new EgovClassifierClient(builder.build(), properties);
    }

    @Test
    void parsesNeighborhoodRowsAndSendsTheDocumentedQuery() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://cs.egov.uz/apiPartner/Table/Get")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("accessToken", TOKEN))
                .andExpect(queryParam("name", "035-3-004"))
                .andExpect(queryParam("offset", "0"))
                .andRespond(withSuccess("""
                        {"result":{"count":2,"data":[
                          {"tin":"207140195","region_soato":"1735","district_soato":"1735204",
                           "name_uz_latin":"Bog'","name_uz":"Боғ","name_ru":"Бог","name_en":"Bog",
                           "uzcad_code":"1735204017","code":"23010008","extra":"ignored"},
                          {"tin":"207140211","region_soato":"1735","district_soato":"1735204",
                           "name_uz_latin":"Bo‘z solma","name_uz":"Бўз солма","name_ru":"Боз солма","name_en":"Boz solma",
                           "uzcad_code":"1735204018","code":"23010012"}]},
                         "statusCode":200,"error":null}
                        """, MediaType.APPLICATION_JSON));

        List<EgovNeighborhoodRow> rows = client.fetchNeighborhoods();

        assertThat(rows).hasSize(2);
        EgovNeighborhoodRow first = rows.getFirst();
        assertThat(first.tin()).isEqualTo("207140195");
        assertThat(first.districtSoato()).isEqualTo("1735204");
        assertThat(first.nameUzLatin()).isEqualTo("Bog'");
        assertThat(first.nameUzCyril()).isEqualTo("Боғ");
        assertThat(first.nameRu()).isEqualTo("Бог");
        assertThat(first.uzcadCode()).isEqualTo("1735204017");
        assertThat(first.code()).isEqualTo("23010008");
        server.verify();
    }

    @Test
    void parsesSoatoRows() {
        server.expect(queryParam("name", "035-1-002"))
                .andRespond(withSuccess("""
                        {"result":{"count":2,"data":[
                          {"RegionID":"1735","RegionNane":"Республика Каракалпакстан"},
                          {"RegionID":"1735204","RegionNane":"Амударьинский район"}]},
                         "statusCode":200,"error":null}
                        """, MediaType.APPLICATION_JSON));

        List<EgovSoatoRow> rows = client.fetchSoato();

        assertThat(rows).extracting(EgovSoatoRow::id).containsExactly("1735", "1735204");
        assertThat(rows.get(1).nameRu()).isEqualTo("Амударьинский район");
    }

    @Test
    void requestsFollowingPagesUntilEveryRowIsRead() {
        server.expect(queryParam("offset", "0"))
                .andRespond(withSuccess("""
                        {"result":{"count":2,"data":[{"RegionID":"1735","RegionNane":"A"}]},"statusCode":200}
                        """, MediaType.APPLICATION_JSON));
        server.expect(queryParam("offset", "1"))
                .andRespond(withSuccess("""
                        {"result":{"count":2,"data":[{"RegionID":"1735204","RegionNane":"B"}]},"statusCode":200}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.fetchSoato()).hasSize(2);
        server.verify();
    }

    @Test
    void failsWithoutLeakingTheTokenWhenTheServerErrors() {
        server.expect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.fetchSoato())
                .isInstanceOf(GeoSyncException.class)
                .hasMessageNotContaining(TOKEN)
                .hasNoCause();
    }

    @Test
    void rejectsAnEnvelopeWithANonOkStatusCode() {
        server.expect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"result":{"count":0,"data":[]},"statusCode":401,"error":"bad token"}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchSoato()).isInstanceOf(GeoSyncException.class);
    }

    @Test
    void failsBeforeAnyRequestWhenTheTokenIsNotConfigured() {
        properties.setAccessToken(" ");

        assertThatThrownBy(() -> client.fetchSoato()).isInstanceOf(GeoSyncException.class);
        server.verify();
    }
}
