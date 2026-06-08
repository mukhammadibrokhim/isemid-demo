package uz.uzinfocom.app.integration.api2.legalentity.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.exception.Api2MalformedResponseException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2UnavailableException;
import uz.uzinfocom.app.integration.api2.common.properties.Api2Properties;
import uz.uzinfocom.app.integration.api2.common.support.Api2ErrorDecoder;
import uz.uzinfocom.app.integration.api2.legalentity.domain.LegalEntityLookupResult;

import java.io.IOException;

@Component
public class LegalEntityApi2Client {

    private static final String OPERATION = "LEGAL_ENTITY_TIN_LOOKUP";
    private static final String SOURCE = "SOLIQ";

    private final RestClient restClient;
    private final Api2Properties properties;
    private final Api2ErrorDecoder errorDecoder;
    private final JsonMapper jsonMapper;

    public LegalEntityApi2Client(
            @Qualifier("api2RestClient") RestClient restClient,
            Api2Properties properties,
            Api2ErrorDecoder errorDecoder,
            JsonMapper jsonMapper
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.errorDecoder = errorDecoder;
        this.jsonMapper = jsonMapper;
    }

    public LegalEntityLookupResult lookupByTin(String tin) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(properties.endpoints().legalEntity())
                            .queryParam("tin", tin)
                            .build())
                    .exchange((request, response) -> handleResponse(response));
        } catch (Api2Exception exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new Api2UnavailableException(OPERATION, exception);
        }
    }

    private LegalEntityLookupResult handleResponse(ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        JsonNode body = readBody(response);

        if (statusCode.isError()) {
            throw errorDecoder.decodeLegalEntity(OPERATION, statusCode, body);
        }

        return new LegalEntityLookupResult(
                SOURCE,
                statusCode.value(),
                body
        );
    }

    private JsonNode readBody(ClientHttpResponse response) throws IOException {
        try {
            JsonNode body = jsonMapper.readTree(response.getBody());
            return body == null ? jsonMapper.createObjectNode() : body;
        } catch (IOException exception) {
            throw new Api2MalformedResponseException(OPERATION, exception);
        }
    }
}
