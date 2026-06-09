package uz.uzinfocom.app.integration.api2.citizen.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupSource;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.exception.Api2MalformedResponseException;
import uz.uzinfocom.app.integration.api2.common.properties.Api2Properties;
import uz.uzinfocom.app.integration.api2.common.support.Api2ErrorDecoder;
import uz.uzinfocom.app.integration.api2.common.support.Api2ResponseBody;
import uz.uzinfocom.app.integration.api2.common.support.Api2ResponseBodyReader;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

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
    private final Api2ResponseBodyReader responseBodyReader;

    public CitizenApi2Client(
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
            throw errorDecoder.decodeTransport(operation, exception);
        }
    }

    private CitizenLookupResult handleResponse(
            String operation,
            CitizenLookupType type,
            ClientHttpResponse response
    ) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        Api2ResponseBody body = responseBodyReader.read(response);

        if (statusCode.isError()) {
            throw errorDecoder.decodeCitizen(operation, statusCode, body);
        }

        if (!body.hasJson()) {
            throw new Api2MalformedResponseException(
                    operation,
                    malformedResponse(statusCode, body)
            );
        }

        if (errorDecoder.isFailurePayload(body)) {
            throw errorDecoder.decodeCitizenPayloadFailure(operation, statusCode, body);
        }

        return new CitizenLookupResult(
                type,
                CitizenLookupSource.CP,
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
