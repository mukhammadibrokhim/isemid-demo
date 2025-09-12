package uz.uzinfocom.isemid.commons.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uz.uzinfocom.isemid.commons.configs.UnsafeHttpClientFactory;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
public class RestClientUtil {
    private static final RestTemplate restTemplate = UnsafeHttpClientFactory.create();

    public static <T, R> T makeRequest(String url, HttpMethod method, String token, R requestBody,
                                       Class<T> responseType, MessageUtil messageUtil) {
        HttpHeaders headers = new HttpHeaders();

        if (token != null && !token.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, token);
        }

        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<R> entity = new HttpEntity<>(requestBody, headers);

        log.debug("Request: {} {} | Body: {} | Headers: {}", method, url, requestBody, headers);

        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

            log.debug("Response from {}: status={}, body={}", url, response.getStatusCode(), response.getBody());

            if (response.hasBody()) {
                return response.getBody();
            }

            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.api.empty_response", url));

        } catch (RestClientException e) {
            log.error("REST call to {} failed: {}", url, e.getMessage(), e);
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.api.failed_call", e.getMessage()));
        }
    }
}