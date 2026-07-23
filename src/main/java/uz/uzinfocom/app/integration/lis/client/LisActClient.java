package uz.uzinfocom.app.integration.lis.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uz.uzinfocom.app.integration.api2.common.auth.CurrentBearerTokenProvider;
import uz.uzinfocom.app.integration.lis.client.dto.LisActPushRequest;
import uz.uzinfocom.app.integration.lis.client.dto.LisResearchCode;
import uz.uzinfocom.app.integration.lis.common.exception.LisException;
import uz.uzinfocom.app.integration.lis.common.exception.LisMalformedResponseException;
import uz.uzinfocom.app.integration.lis.common.support.LisErrorDecoder;
import uz.uzinfocom.app.integration.lis.common.support.LisUrlFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The only place that speaks HTTP to LIS.
 *
 * <p>Deliberately has no knowledge of {@code Act}, no repository, and no
 * transaction — it takes a fully-built payload and returns LIS's act id, so
 * the slow network call can be kept outside any database transaction (see
 * {@code ActLisSendService}).
 *
 * <p>Uses {@code .exchange(...)} rather than {@code .retrieve()} so status
 * and body handling stay here, in one place, and every failure leaves as a
 * typed {@link LisException} — the same choice the API2 clients make.
 */
@Slf4j
@Component
public class LisActClient {

    private static final String CREATE_ACT_OPERATION = "createAct";
    private static final String ACT_TEMPLATE_OPERATION = "actTemplateId";
    private static final String ORGANIZATION_ID_HEADER = "Organization-Id";

    private final RestClient restClient;
    private final LisUrlFactory urlFactory;
    private final LisErrorDecoder errorDecoder;
    private final CurrentBearerTokenProvider bearerTokenProvider;

    /**
     * LIS's act-template ids are static per research family (there are three)
     * and change only when LIS reconfigures itself, so they are memoized for
     * the process lifetime rather than re-fetched on every single send. A
     * restart picks up any change.
     */
    private final Map<LisResearchCode, Integer> actTemplateIdCache = new ConcurrentHashMap<>();

    public LisActClient(
            @Qualifier("lisRestClient") RestClient restClient,
            LisUrlFactory urlFactory,
            LisErrorDecoder errorDecoder,
            CurrentBearerTokenProvider bearerTokenProvider
    ) {
        this.restClient = restClient;
        this.urlFactory = urlFactory;
        this.errorDecoder = errorDecoder;
        this.bearerTokenProvider = bearerTokenProvider;
    }

    /**
     * Submits an act to a LIS laboratory.
     *
     * @return LIS's own id for the created act, used to correlate the later
     *         callback
     * @throws LisException on any transport failure, upstream error status,
     *                      or a response without an act id
     */
    public Long createAct(
            Long labId,
            Long senderActNumber,
            boolean force,
            LisActPushRequest payload,
            UUID organizationUuid
    ) {
        URI uri = urlFactory.createAct(labId, senderActNumber, force);

        log.info("Sending act to LIS. actId={}, labId={}, force={}", senderActNumber, labId, force);

        Long lisActId = execute(CREATE_ACT_OPERATION, organizationUuid, Long.class, () ->
                restClient.post()
                        .uri(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers -> applyCallerHeaders(headers, organizationUuid))
                        .body(payload)
        );

        if (lisActId == null) {
            throw new LisMalformedResponseException(
                    CREATE_ACT_OPERATION, null, "LIS returned no act id for act " + senderActNumber
            );
        }

        log.info("Act accepted by LIS. actId={}, lisActId={}", senderActNumber, lisActId);
        return lisActId;
    }

    /**
     * Resolves LIS's act-template id for a research family. Memoized — see
     * {@link #actTemplateIdCache}.
     */
    public Integer resolveActTemplateId(LisResearchCode researchCode, UUID organizationUuid) {
        Integer cached = actTemplateIdCache.get(researchCode);
        if (cached != null) {
            return cached;
        }

        URI uri = urlFactory.actTemplateId(researchCode.name());

        Integer templateId = execute(ACT_TEMPLATE_OPERATION, organizationUuid, Integer.class, () ->
                restClient.get()
                        .uri(uri)
                        .headers(headers -> applyCallerHeaders(headers, organizationUuid))
        );

        if (templateId == null) {
            throw new LisMalformedResponseException(
                    ACT_TEMPLATE_OPERATION, null, "LIS returned no template id for research code " + researchCode
            );
        }

        actTemplateIdCache.put(researchCode, templateId);
        return templateId;
    }

    /**
     * Runs the prepared request, converting every failure mode into a typed
     * {@link LisException}. Our own exceptions pass through untouched so a
     * decoded upstream error is never re-wrapped as a transport error.
     */
    private <T> T execute(
            String operation,
            UUID organizationUuid,
            Class<T> responseType,
            java.util.function.Supplier<RestClient.RequestHeadersSpec<?>> requestSupplier
    ) {
        try {
            return requestSupplier.get()
                    .exchange((request, response) -> handleResponse(operation, response, responseType));
        } catch (LisException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw errorDecoder.decodeTransport(operation, exception);
        }
    }

    private <T> T handleResponse(
            String operation,
            org.springframework.http.client.ClientHttpResponse response,
            Class<T> responseType
    ) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        String body = readBody(response);

        if (statusCode.isError()) {
            throw errorDecoder.decode(operation, statusCode, body);
        }

        if (!StringUtils.hasText(body)) {
            return null;
        }

        return parseScalar(operation, statusCode, body, responseType);
    }

    /**
     * Both LIS endpoints answer with a bare JSON number, so this parses a
     * scalar rather than running a full JSON binding — quoted values are
     * tolerated because some LIS deployments answer {@code "123"}.
     */
    private <T> T parseScalar(String operation, HttpStatusCode statusCode, String body, Class<T> responseType) {
        String trimmed = body.trim().replace("\"", "");

        try {
            if (responseType == Long.class) {
                return responseType.cast(Long.valueOf(trimmed));
            }
            if (responseType == Integer.class) {
                return responseType.cast(Integer.valueOf(trimmed));
            }
        } catch (NumberFormatException exception) {
            throw new LisMalformedResponseException(operation, statusCode.value(), trimmed);
        }

        throw new IllegalArgumentException("Unsupported LIS response type: " + responseType);
    }

    private String readBody(org.springframework.http.client.ClientHttpResponse response) throws IOException {
        byte[] bytes = response.getBody().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * LIS scopes what it returns by the calling organization and user, so the
     * caller's own selected organization and bearer token are forwarded on
     * each request rather than the app holding LIS service credentials.
     */
    private void applyCallerHeaders(HttpHeaders headers, UUID organizationUuid) {
        if (organizationUuid != null) {
            headers.set(ORGANIZATION_ID_HEADER, organizationUuid.toString());
        }
        bearerTokenProvider.currentToken().ifPresent(headers::setBearerAuth);
    }
}
