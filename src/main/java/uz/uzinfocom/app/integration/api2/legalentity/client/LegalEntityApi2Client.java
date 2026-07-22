package uz.uzinfocom.app.integration.api2.legalentity.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2MalformedResponseException;
import uz.uzinfocom.app.integration.api2.common.properties.Api2Properties;
import uz.uzinfocom.app.integration.api2.common.support.Api2ErrorDecoder;
import uz.uzinfocom.app.integration.api2.common.support.Api2ResponseBody;
import uz.uzinfocom.app.integration.api2.common.support.Api2ResponseBodyReader;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;
import uz.uzinfocom.app.integration.api2.legalentity.domain.LegalEntityLookupResult;
import uz.uzinfocom.app.integration.api2.legalentity.domain.LegalEntityLookupSource;

import java.io.IOException;

@Component
public class LegalEntityApi2Client {

    private static final String OPERATION = "LEGAL_ENTITY_TIN_LOOKUP";
    private static final LegalEntityLookupSource SOURCE = LegalEntityLookupSource.SOLIQ;

    private final RestClient restClient;
    private final Api2Properties properties;
    private final Api2ErrorDecoder errorDecoder;
    private final Api2ResponseBodyReader responseBodyReader;

    public LegalEntityApi2Client(
            @Qualifier("api2RestClient") RestClient restClient,
            Api2Properties properties,
            Api2ErrorDecoder errorDecoder,
            Api2ResponseBodyReader responseBodyReader
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.errorDecoder = errorDecoder;
        this.responseBodyReader = responseBodyReader;
    }

    public LegalEntityLookupResult lookupByTin(String tin) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(properties.endpoints().legalEntity())
                            .queryParam("tin", tin)
                            .build())
                    .exchange((request, response) -> handleResponse(response));
        } catch (RestClientException exception) {
            throw errorDecoder.decodeTransport(OPERATION, exception);
        }
    }

    private LegalEntityLookupResult handleResponse(ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        Api2ResponseBody body = responseBodyReader.read(response);

        if (statusCode.isError()) {
            throw errorDecoder.decodeLegalEntity(OPERATION, statusCode, body);
        }

        if (!body.hasJson()) {
            throw new Api2MalformedResponseException(
                    OPERATION,
                    malformedResponse(statusCode, body)
            );
        }

        if (errorDecoder.isFailurePayload(body)) {
            throw errorDecoder.decodeLegalEntityPayloadFailure(OPERATION, statusCode, body);
        }

        return new LegalEntityLookupResult(
                SOURCE,
                statusCode.value(),
                body.json()
        );
    }

    private Api2UpstreamError malformedResponse(HttpStatusCode statusCode, Api2ResponseBody body) {
        String detail = body == null || body.empty()
                ? "Upstream response body is empty."
                : body.safeRawBody();

        return new Api2UpstreamError(statusCode.value(), null, null, detail);
    }
}
