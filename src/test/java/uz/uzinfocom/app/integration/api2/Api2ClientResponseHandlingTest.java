package uz.uzinfocom.app.integration.api2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.api2.citizen.client.CitizenApi2Client;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.exception.Api2MalformedResponseException;
import uz.uzinfocom.app.integration.api2.common.properties.Api2Properties;
import uz.uzinfocom.app.integration.api2.common.support.Api2ErrorDecoder;
import uz.uzinfocom.app.integration.api2.common.support.Api2ResponseBodyReader;
import uz.uzinfocom.app.integration.api2.legalentity.client.LegalEntityApi2Client;
import uz.uzinfocom.app.integration.api2.legalentity.domain.LegalEntityLookupResult;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class Api2ClientResponseHandlingTest {

    private static final String BASE_URL = "https://api2.example";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final Api2Properties properties = new Api2Properties(
            BASE_URL,
            Duration.ofMillis(100),
            Duration.ofMillis(100),
            new Api2Properties.Endpoints(
                    "/v3/Child",
                    "/v3/Citizen",
                    "/v3/CitizenPassport",
                    "/v3/LegalEntity"
            )
    );
    private final Api2ErrorDecoder decoder = new Api2ErrorDecoder();
    private final Api2ResponseBodyReader bodyReader = new Api2ResponseBodyReader(jsonMapper);

    private MockRestServiceServer server;
    private CitizenApi2Client citizenClient;
    private LegalEntityApi2Client legalEntityClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        citizenClient = new CitizenApi2Client(restClient, properties, decoder, bodyReader);
        legalEntityClient = new LegalEntityApi2Client(restClient, properties, decoder, bodyReader);
    }

    @Test
    void citizenValidJsonSuccessReturnsResult() {
        server.expect(once(), requestTo(BASE_URL + "/v3/Citizen?birth_date=2020-01-01&nnuzb=12345678901234"))
                .andRespond(withSuccess(
                        "{\"status\":200,\"result\":\"OK\",\"comments\":\"done\",\"data\":[]}",
                        MediaType.APPLICATION_JSON
                ));

        CitizenLookupResult result = citizenClient.lookupByNnuzb(
                "12345678901234",
                LocalDate.of(2020, 1, 1)
        );

        assertThat(result.upstreamStatus()).isEqualTo(200);
        assertThat(result.data().get("result").asText()).isEqualTo("OK");
        server.verify();
    }

    @Test
    void legalEntityValidJsonSuccessReturnsResult() {
        server.expect(once(), requestTo(BASE_URL + "/v3/LegalEntity?tin=123456789"))
                .andRespond(withSuccess(
                        "{\"status\":200,\"data\":{\"tin\":\"123456789\"}}",
                        MediaType.APPLICATION_JSON
                ));

        LegalEntityLookupResult result = legalEntityClient.lookupByTin("123456789");

        assertThat(result.upstreamStatus()).isEqualTo(200);
        assertThat(result.data().get("data").get("tin").asText()).isEqualTo("123456789");
        server.verify();
    }

    @Test
    void citizenPlainTextBadRequestIsStatusSpecificBadRequest() {
        server.expect(once(), requestTo(BASE_URL + "/v3/Citizen?birth_date=2020-01-01&nnuzb=12345678901234"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("incorrect"));

        assertThatThrownBy(() -> citizenClient.lookupByNnuzb("12345678901234", LocalDate.of(2020, 1, 1)))
                .isInstanceOf(Api2Exception.class)
                .hasFieldOrPropertyWithValue("responseStatus", HttpStatus.BAD_REQUEST)
                .hasFieldOrPropertyWithValue("errorCode", "API2_UPSTREAM_BAD_REQUEST")
                .hasFieldOrPropertyWithValue("upstreamDetail", "incorrect");

        server.verify();
    }

    @Test
    void legalEntityPlainTextBadRequestIsStatusSpecificBadRequest() {
        server.expect(once(), requestTo(BASE_URL + "/v3/LegalEntity?tin=123456789"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("incorrect"));

        assertThatThrownBy(() -> legalEntityClient.lookupByTin("123456789"))
                .isInstanceOf(Api2Exception.class)
                .hasFieldOrPropertyWithValue("responseStatus", HttpStatus.BAD_REQUEST)
                .hasFieldOrPropertyWithValue("errorCode", "API2_UPSTREAM_BAD_REQUEST")
                .hasFieldOrPropertyWithValue("upstreamDetail", "incorrect");

        server.verify();
    }

    @Test
    void citizenSuccessfulMalformedJsonIsRejected() {
        server.expect(once(), requestTo(BASE_URL + "/v3/Citizen?birth_date=2020-01-01&nnuzb=12345678901234"))
                .andRespond(withSuccess("incorrect", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> citizenClient.lookupByNnuzb("12345678901234", LocalDate.of(2020, 1, 1)))
                .isInstanceOf(Api2MalformedResponseException.class)
                .hasFieldOrPropertyWithValue("responseStatus", HttpStatus.BAD_GATEWAY)
                .hasFieldOrPropertyWithValue("errorCode", "API2_MALFORMED_RESPONSE");

        server.verify();
    }

    @Test
    void legalEntitySuccessfulEmptyBodyIsRejected() {
        server.expect(once(), requestTo(BASE_URL + "/v3/LegalEntity?tin=123456789"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> legalEntityClient.lookupByTin("123456789"))
                .isInstanceOf(Api2MalformedResponseException.class)
                .hasFieldOrPropertyWithValue("responseStatus", HttpStatus.BAD_GATEWAY)
                .hasFieldOrPropertyWithValue("errorCode", "API2_MALFORMED_RESPONSE");

        server.verify();
    }

    @Test
    void successfulHttpPayloadWithExplicitFailureDoesNotReturnSuccess() {
        server.expect(once(), requestTo(BASE_URL + "/v3/LegalEntity?tin=123456789"))
                .andRespond(withSuccess(
                        "{\"status\":false,\"result\":\"incorrect\",\"comments\":\"bad request\"}",
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(() -> legalEntityClient.lookupByTin("123456789"))
                .isInstanceOf(Api2Exception.class)
                .hasFieldOrPropertyWithValue("responseStatus", HttpStatus.BAD_REQUEST)
                .hasFieldOrPropertyWithValue("errorCode", "API2_UPSTREAM_BAD_REQUEST");

        server.verify();
    }
}
