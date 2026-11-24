package uz.uzinfocom.app.modules.reference.application.geosync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.EgovNeighborhoodRow;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.EgovSoatoRow;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.EgovTableResponse;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the cs.egov.uz partner API: {@code GET /apiPartner/Table/Get?accessToken=&name=&limit=&offset=&lang=1}.
 * The API takes the token as a query parameter, so the request URL is a secret - it is never
 * logged and a failure is re-thrown without the underlying exception (whose message embeds it).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EgovClassifierClient {

    private static final String TABLE_PATH = "/apiPartner/Table/Get";
    private static final int MAX_PAGES = 50;
    private static final String USER_AGENT = "isemid-platform/geo-sync";

    private static final ParameterizedTypeReference<EgovTableResponse<EgovNeighborhoodRow>> NEIGHBORHOOD_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<EgovTableResponse<EgovSoatoRow>> SOATO_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final EgovClassifierProperties properties;

    public List<EgovNeighborhoodRow> fetchNeighborhoods() {
        return fetchAll(properties.getNeighborhoodClassifier(), NEIGHBORHOOD_TYPE);
    }

    public List<EgovSoatoRow> fetchSoato() {
        return fetchAll(properties.getSoatoClassifier(), SOATO_TYPE);
    }

    private <T> List<T> fetchAll(String classifier, ParameterizedTypeReference<EgovTableResponse<T>> type) {
        String token = properties.getAccessToken();
        if (token == null || token.isBlank()) {
            throw new GeoSyncException(ErrorCode.INTERNAL_ERROR, "reference.geo_sync.token_missing");
        }

        List<T> all = new ArrayList<>();
        int offset = 0;
        for (int page = 0; page < MAX_PAGES; page++) {
            EgovTableResponse.Result<T> result = fetchPage(classifier, token.trim(), offset, type);
            List<T> rows = result.rows();
            all.addAll(rows);
            offset += rows.size();

            int total = result.count() == null ? all.size() : result.count();
            if (rows.isEmpty() || all.size() >= total) {
                break;
            }
        }

        if (all.isEmpty()) {
            throw new GeoSyncException("reference.geo_sync.empty_response");
        }
        log.info("Fetched {} rows of egov classifier {}", all.size(), classifier);
        return all;
    }

    private <T> EgovTableResponse.Result<T> fetchPage(
            String classifier, String token, int offset, ParameterizedTypeReference<EgovTableResponse<T>> type
    ) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(TABLE_PATH)
                .queryParam("accessToken", token)
                .queryParam("name", classifier)
                .queryParam("limit", properties.getPageLimit())
                .queryParam("offset", offset)
                .queryParam("lang", 1)
                .encode()
                .build()
                .toUri();

        EgovTableResponse<T> body;
        try {
            body = restClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    // The registry's nginx answers 403 to the default Java client User-Agent.
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException exception) {
            log.warn("egov classifier {} request failed: HTTP {}", classifier, exception.getStatusCode().value());
            throw new GeoSyncException("reference.geo_sync.fetch_failed");
        } catch (RuntimeException exception) {
            log.warn("egov classifier {} request failed: {}", classifier, exception.getClass().getSimpleName());
            throw new GeoSyncException("reference.geo_sync.fetch_failed");
        }

        if (body == null || body.result() == null) {
            throw new GeoSyncException("reference.geo_sync.empty_response");
        }
        if (body.statusCode() != null && body.statusCode() != 200) {
            log.warn("egov classifier {} rejected: statusCode={}", classifier, body.statusCode());
            throw new GeoSyncException("reference.geo_sync.invalid_response");
        }
        return body.result();
    }
}
