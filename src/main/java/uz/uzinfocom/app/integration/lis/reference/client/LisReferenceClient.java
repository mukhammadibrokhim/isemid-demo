package uz.uzinfocom.app.integration.lis.reference.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import uz.uzinfocom.app.integration.lis.common.exception.LisMalformedResponseException;
import uz.uzinfocom.app.integration.lis.common.support.LisErrorDecoder;
import uz.uzinfocom.app.integration.lis.common.support.LisUrlFactory;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisCategoryResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisConditionResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisItemTypeResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisOrganizationResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisPaginationRequest;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisProfessionResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisReferenceListEnvelope;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisReferencePage;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisReferencePageEnvelope;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisResearchTypeResponse;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

/**
 * Speaks to LIS's public, read-only dictionary endpoints (organizations,
 * departments, conditions, professions, research types, categories, item
 * types) — the "spravochnik" lookups behind the dropdowns on an act, kept
 * separate from {@link uz.uzinfocom.app.integration.lis.client.LisActClient}
 * (act submission/result) even though both share the same base URL, API key
 * and circuit breaker, because these are proxied+cached for the frontend
 * rather than driving a domain workflow — see {@code LisReferenceQueryService}
 * for the caching layer and {@code docs/act-lis-frontend-guide.md} for why
 * this backend proxies these instead of the frontend calling LIS directly.
 *
 * <p>Same failure handling as {@code LisActClient}: every failure becomes a
 * typed {@link uz.uzinfocom.app.integration.lis.common.exception.LisException},
 * which already carries a localized, user-facing message via the app-wide
 * {@code GlobalExceptionHandler} — no separate error-message plumbing needed
 * here.
 */
@Slf4j
@Component
public class LisReferenceClient {

    private static final String ORGANIZATIONS_OPERATION = "organizations";
    private static final String DEPARTMENTS_OPERATION = "departments";
    private static final String CONDITIONS_OPERATION = "conditions";
    private static final String PROFESSIONS_OPERATION = "professions";
    private static final String RESEARCH_TYPES_OPERATION = "researchTypes";
    private static final String CATEGORIES_OPERATION = "categories";
    private static final String ITEM_TYPES_OPERATION = "itemTypes";

    private final RestClient restClient;
    private final LisUrlFactory urlFactory;
    private final LisErrorDecoder errorDecoder;
    private final DynamicCircuitBreakerLookup circuitBreakerLookup;
    private final ObjectMapper objectMapper;

    public LisReferenceClient(
            @Qualifier("lisRestClient") RestClient restClient,
            LisUrlFactory urlFactory,
            LisErrorDecoder errorDecoder,
            DynamicCircuitBreakerLookup circuitBreakerLookup,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.urlFactory = urlFactory;
        this.errorDecoder = errorDecoder;
        this.circuitBreakerLookup = circuitBreakerLookup;
        this.objectMapper = objectMapper;
    }

    public List<LisOrganizationResponse> organizations(String name) {
        JavaType envelopeType = listEnvelopeType(LisOrganizationResponse.class);
        LisReferenceListEnvelope<LisOrganizationResponse> envelope =
                get(ORGANIZATIONS_OPERATION, urlFactory.sesorgs(name), envelopeType);
        return nullToEmpty(envelope.data());
    }

    public List<LisOrganizationResponse> departments(Long organizationId) {
        JavaType envelopeType = listEnvelopeType(LisOrganizationResponse.class);
        LisReferenceListEnvelope<LisOrganizationResponse> envelope =
                get(DEPARTMENTS_OPERATION, urlFactory.departments(organizationId), envelopeType);
        return nullToEmpty(envelope.data());
    }

    public List<LisConditionResponse> conditions() {
        JavaType envelopeType = listEnvelopeType(LisConditionResponse.class);
        LisReferenceListEnvelope<LisConditionResponse> envelope =
                get(CONDITIONS_OPERATION, urlFactory.conditions(), envelopeType);
        return nullToEmpty(envelope.data());
    }

    public LisReferencePage<LisProfessionResponse> professions(String search, int page, int limit) {
        return postPage(
                PROFESSIONS_OPERATION, urlFactory.professions(), search, page, limit, LisProfessionResponse.class
        );
    }

    public LisReferencePage<LisResearchTypeResponse> researchTypes(String search, int page, int limit) {
        return postPage(
                RESEARCH_TYPES_OPERATION, urlFactory.researchTypes(), search, page, limit, LisResearchTypeResponse.class
        );
    }

    public LisReferencePage<LisCategoryResponse> categories(String search, int page, int limit) {
        return postPage(
                CATEGORIES_OPERATION, urlFactory.categories(), search, page, limit, LisCategoryResponse.class
        );
    }

    public LisReferencePage<LisItemTypeResponse> itemTypes(String search, int page, int limit) {
        return postPage(
                ITEM_TYPES_OPERATION, urlFactory.itemTypes(), search, page, limit, LisItemTypeResponse.class
        );
    }

    private <T> LisReferencePage<T> postPage(
            String operation, URI uri, String search, int page, int limit, Class<T> elementType
    ) {
        LisPaginationRequest body = LisPaginationRequest.of(search, page, limit);
        JavaType envelopeType = pageEnvelopeType(elementType);
        LisReferencePageEnvelope<T> envelope = post(operation, uri, body, envelopeType);

        LisReferencePage<T> page1 = envelope.data();
        return page1 == null ? new LisReferencePage<>(List.of(), 0L) : page1;
    }

    private <T> T get(String operation, URI uri, JavaType responseType) {
        return execute(operation, responseType, () -> restClient.get().uri(uri));
    }

    private <T> T post(String operation, URI uri, Object body, JavaType responseType) {
        return execute(operation, responseType, () -> restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }

    private <T> T execute(
            String operation,
            JavaType responseType,
            Supplier<RestClient.RequestHeadersSpec<?>> requestSupplier
    ) {
        try {
            return circuitBreakerLookup.forName(CircuitBreakerNames.LIS).executeSupplier(() -> requestSupplier.get()
                    .exchange((request, response) -> handleResponse(operation, response, responseType)));
        } catch (CallNotPermittedException exception) {
            throw errorDecoder.decodeCircuitBreakerOpen(operation, exception);
        } catch (RestClientException exception) {
            throw errorDecoder.decodeTransport(operation, exception);
        }
    }

    private <T> T handleResponse(String operation, ClientHttpResponse response, JavaType responseType)
            throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        String body = readBody(response);

        if (statusCode.isError()) {
            throw errorDecoder.decode(operation, statusCode, body);
        }

        if (!StringUtils.hasText(body)) {
            throw new LisMalformedResponseException(operation, statusCode.value(), "Empty response body");
        }

        try {
            return objectMapper.readValue(body, responseType);
        } catch (RuntimeException exception) {
            throw new LisMalformedResponseException(operation, statusCode.value(), exception.getMessage());
        }
    }

    private String readBody(ClientHttpResponse response) throws IOException {
        byte[] bytes = response.getBody().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private JavaType listEnvelopeType(Class<?> elementType) {
        return objectMapper.getTypeFactory()
                .constructParametricType(LisReferenceListEnvelope.class, elementType);
    }

    private JavaType pageEnvelopeType(Class<?> elementType) {
        JavaType pageType = objectMapper.getTypeFactory().constructParametricType(LisReferencePage.class, elementType);
        return objectMapper.getTypeFactory().constructParametricType(LisReferencePageEnvelope.class, pageType);
    }

    private <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
