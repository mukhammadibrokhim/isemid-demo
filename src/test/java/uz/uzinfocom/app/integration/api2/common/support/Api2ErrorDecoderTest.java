package uz.uzinfocom.app.integration.api2.common.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

class Api2ErrorDecoderTest {

    private static final String OPERATION = "TEST_OPERATION";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final Api2ErrorDecoder decoder = new Api2ErrorDecoder();

    @ParameterizedTest
    @CsvSource({
            "400, BAD_REQUEST, API2_UPSTREAM_BAD_REQUEST",
            "401, BAD_GATEWAY, API2_UPSTREAM_UNAUTHORIZED",
            "403, BAD_GATEWAY, API2_UPSTREAM_FORBIDDEN",
            "404, NOT_FOUND, CITIZEN_NOT_FOUND",
            "408, GATEWAY_TIMEOUT, API2_TIMEOUT",
            "429, SERVICE_UNAVAILABLE, API2_RATE_LIMITED",
            "500, BAD_GATEWAY, API2_BAD_GATEWAY",
            "501, BAD_GATEWAY, API2_BAD_GATEWAY",
            "502, BAD_GATEWAY, API2_BAD_GATEWAY",
            "503, BAD_GATEWAY, API2_BAD_GATEWAY",
            "504, GATEWAY_TIMEOUT, API2_TIMEOUT"
    })
    void mapsCitizenHttpStatuses(int upstreamStatus, HttpStatus downstreamStatus, String errorCode) {
        Api2Exception exception = decoder.decodeCitizen(
                OPERATION,
                HttpStatusCode.valueOf(upstreamStatus),
                jsonBody("{\"code\":\"UPSTREAM\",\"message\":\"upstream message\",\"detail\":\"safe detail\"}")
        );

        assertThat(exception.getResponseStatus()).isEqualTo(downstreamStatus);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getUpstreamStatus()).isEqualTo(upstreamStatus);
    }

    @ParameterizedTest
    @CsvSource({
            "400, BAD_REQUEST, API2_UPSTREAM_BAD_REQUEST",
            "401, BAD_GATEWAY, API2_UPSTREAM_UNAUTHORIZED",
            "403, BAD_GATEWAY, API2_UPSTREAM_FORBIDDEN",
            "404, NOT_FOUND, LEGAL_ENTITY_NOT_FOUND",
            "408, GATEWAY_TIMEOUT, API2_TIMEOUT",
            "429, SERVICE_UNAVAILABLE, API2_RATE_LIMITED",
            "500, BAD_GATEWAY, API2_BAD_GATEWAY",
            "502, BAD_GATEWAY, API2_BAD_GATEWAY",
            "504, GATEWAY_TIMEOUT, API2_TIMEOUT"
    })
    void mapsLegalEntityHttpStatuses(int upstreamStatus, HttpStatus downstreamStatus, String errorCode) {
        Api2Exception exception = decoder.decodeLegalEntity(
                OPERATION,
                HttpStatusCode.valueOf(upstreamStatus),
                jsonBody("{\"code\":\"UPSTREAM\",\"message\":\"upstream message\",\"detail\":\"safe detail\"}")
        );

        assertThat(exception.getResponseStatus()).isEqualTo(downstreamStatus);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getUpstreamStatus()).isEqualTo(upstreamStatus);
    }

    @Test
    void preservesSafeMetadataForValidJsonBadRequest() {
        Api2Exception exception = decoder.decodeCitizen(
                OPERATION,
                HttpStatusCode.valueOf(400),
                jsonBody("{\"code\":\"BAD_INPUT\",\"message\":\"Invalid request\",\"detail\":\"field is invalid\"}")
        );

        assertThat(exception.getResponseStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getUpstreamCode()).isEqualTo("BAD_INPUT");
        assertThat(exception.getUpstreamMessage()).isEqualTo("Invalid request");
        assertThat(exception.getUpstreamDetail()).isEqualTo("field is invalid");
    }

    @Test
    void mapsPlainTextBadRequestWithoutTreatingItAsOnlyMalformedResponse() {
        Api2Exception exception = decoder.decodeCitizen(
                OPERATION,
                HttpStatusCode.valueOf(400),
                textBody("incorrect")
        );

        assertThat(exception.getResponseStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getErrorCode()).isEqualTo("API2_UPSTREAM_BAD_REQUEST");
        assertThat(exception.getUpstreamDetail()).isEqualTo("incorrect");
    }

    @Test
    void stripsAuthenticationDetailsFromUnauthorizedResponse() {
        Api2Exception exception = decoder.decodeCitizen(
                OPERATION,
                HttpStatusCode.valueOf(401),
                jsonBody("{\"code\":\"TOKEN_REJECTED\",\"message\":\"Bearer secret-token rejected\",\"detail\":\"token=secret\"}")
        );

        assertThat(exception.getResponseStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(exception.getErrorCode()).isEqualTo("API2_UPSTREAM_UNAUTHORIZED");
        assertThat(exception.getUpstreamStatus()).isEqualTo(401);
        assertThat(exception.getUpstreamCode()).isNull();
        assertThat(exception.getUpstreamMessage()).isNull();
        assertThat(exception.getUpstreamDetail()).isNull();
    }

    @Test
    void preservesParsedFieldValidationErrorsForUnprocessableEntity() {
        Api2Exception exception = decoder.decodeLegalEntity(
                OPERATION,
                HttpStatusCode.valueOf(422),
                jsonBody("{\"errors\":[{\"field\":\"tin\",\"message\":\"bad tin\"}]}")
        );

        assertThat(exception.getResponseStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getFieldErrors())
                .extracting("field", "message")
                .containsExactly(org.assertj.core.groups.Tuple.tuple("tin", "validation.invalid_value"));
    }

    @Test
    void mapsExplicitFailurePayloadThroughDecoder() {
        Api2Exception exception = decoder.decodeCitizenPayloadFailure(
                OPERATION,
                HttpStatusCode.valueOf(200),
                jsonBody("{\"status\":false,\"result\":\"incorrect\",\"comments\":\"bad request\"}")
        );

        assertThat(exception.getResponseStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getErrorCode()).isEqualTo("API2_UPSTREAM_BAD_REQUEST");
    }

    @Test
    void mapsTransportTimeoutSeparatelyFromConnectionFailure() {
        Api2Exception timeout = decoder.decodeTransport(
                OPERATION,
                new ResourceAccessException("Read timed out", new SocketTimeoutException("Read timed out"))
        );
        Api2Exception unavailable = decoder.decodeTransport(
                OPERATION,
                new ResourceAccessException("Connection refused", new ConnectException("Connection refused"))
        );

        assertThat(timeout.getResponseStatus()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(timeout.getErrorCode()).isEqualTo("API2_TIMEOUT");
        assertThat(unavailable.getResponseStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(unavailable.getErrorCode()).isEqualTo("API2_UNAVAILABLE");
    }

    @Test
    void sanitizesSensitiveRawBodyBeforePublicErrorFields() {
        Api2Exception exception = decoder.decodeCitizen(
                OPERATION,
                HttpStatusCode.valueOf(400),
                textBody("incorrect token=secret nnuzb=12345678901234 tin=123456789")
        );

        assertThat(exception.getUpstreamDetail())
                .contains("incorrect")
                .contains("token=****")
                .doesNotContain("secret")
                .doesNotContain("12345678901234")
                .doesNotContain("123456789");
    }

    private Api2ResponseBody jsonBody(String rawJson) {
        try {
            JsonNode json = jsonMapper.readTree(rawJson);
            return new Api2ResponseBody(json, null, MediaType.APPLICATION_JSON, false, false);
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    private Api2ResponseBody textBody(String rawText) {
        return new Api2ResponseBody(
                null,
                Api2ResponseBodySanitizer.sanitize(rawText, MediaType.TEXT_PLAIN),
                MediaType.TEXT_PLAIN,
                false,
                false
        );
    }
}
