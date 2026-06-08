package uz.uzinfocom.app.integration.api2.citizen.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupSource;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.exception.Api2MalformedResponseException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2UnavailableException;
import uz.uzinfocom.app.integration.api2.common.properties.Api2Properties;
import uz.uzinfocom.app.integration.api2.common.support.Api2ErrorDecoder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.function.Function;

@Component
public class CitizenApi2Client {

    private static final String NNUZB_OPERATION = "CITIZEN_NNUZB_LOOKUP";
    private static final String PASSPORT_OPERATION = "CITIZEN_PPN_LOOKUP";
    private static final String CHILD_OPERATION = "CITIZEN_CZ_LOOKUP";

    private final RestClient restClient;
    private final Api2Properties properties;
    private final Api2ErrorDecoder errorDecoder;
    private final JsonMapper jsonMapper;

    public CitizenApi2Client(
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

    public CitizenLookupResult lookupByNnuzb(String nnuzb, LocalDate birthDate) {
        return execute(
                NNUZB_OPERATION,
                CitizenLookupType.NNUZB,
                uriBuilder -> uriBuilder
                        .path(properties.endpoints().citizen())
                        .queryParam("birth_date", birthDate)
                        .queryParam("nnuzb", nnuzb)
                        .build()
        );
    }

    public CitizenLookupResult lookupByPassport(String document, LocalDate birthDate) {
        return execute(
                PASSPORT_OPERATION,
                CitizenLookupType.PPN,
                uriBuilder -> uriBuilder
                        .path(properties.endpoints().citizenPassport())
                        .queryParam("document", document)
                        .queryParam("birth_date", birthDate)
                        .build()
        );
    }

    public CitizenLookupResult lookupChild(String series, String number) {
        return execute(
                CHILD_OPERATION,
                CitizenLookupType.CZ,
                uriBuilder -> uriBuilder
                        .path(properties.endpoints().child())
                        .queryParam("series", series)
                        .queryParam("number", number)
                        .build()
        );
    }

    private CitizenLookupResult execute(
            String operation,
            CitizenLookupType type,
            Function<UriBuilder, URI> uri
    ) {
        try {
            return restClient.get()
                    .uri(uri)
                    .exchange((request, response) -> handleResponse(operation, type, response));
        } catch (Api2Exception exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new Api2UnavailableException(operation, exception);
        }
    }

    private CitizenLookupResult handleResponse(
            String operation,
            CitizenLookupType type,
            ClientHttpResponse response
    ) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        JsonNode body = readBody(operation, response);

        if (statusCode.isError()) {
            throw errorDecoder.decodeCitizen(operation, statusCode, body);
        }

        return new CitizenLookupResult(
                type,
                CitizenLookupSource.CP,
                statusCode.value(),
                body
        );
    }

    private JsonNode readBody(String operation, ClientHttpResponse response) throws IOException {
        try {
            JsonNode body = jsonMapper.readTree(response.getBody());
            return body == null ? jsonMapper.createObjectNode() : body;
        } catch (IOException exception) {
            throw new Api2MalformedResponseException(operation, exception);
        }
    }
}
