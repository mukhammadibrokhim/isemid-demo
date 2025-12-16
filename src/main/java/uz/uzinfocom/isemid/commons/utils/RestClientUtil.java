package uz.uzinfocom.isemid.commons.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
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

            throw new ApiException(
                    INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.api.empty_response", url)
            );

        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();

            log.error("REST call error {} - body: {}", status, responseBody);

            throw new ApiException(
                    HttpStatus.resolve(status) != null ? HttpStatus.valueOf(status) : INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.api.failed_call_with_status", status, responseBody)
            );
        } catch (RestClientException e) {
            log.error("REST call to {} failed: {}", url, e.getMessage());

            throw new ApiException(
                    INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.api.failed_call", e.getMessage())
            );
        }
    }

    public static <T, R> T makeRequest(
            String url,
            HttpMethod method,
            String token,
            R requestBody,
            HttpHeaders extraHeaders,
            Class<T> responseType,
            MessageUtil messageUtil
    ) {

        HttpHeaders headers = new HttpHeaders();

        if (token != null && !token.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        // Merge qilamiz
        if (extraHeaders != null) {
            headers.addAll(extraHeaders);
        }

        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<R> entity = new HttpEntity<>(requestBody, headers);

        log.info("➡️ Request {} {} | Body={} | Headers={}", method, url, requestBody, headers);

        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

            log.info("⬅️ Response {}: status={}, body={}", url, response.getStatusCode(), response.getBody());

            if (response.hasBody()) {
                return response.getBody();
            }

            throw new ApiException(
                    INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.api.empty_response", url)
            );

        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            String body = e.getResponseBodyAsString();

            log.error("❌ REST error {} -> {}", status, body);

            throw new ApiException(
                    HttpStatus.resolve(status) != null ? HttpStatus.valueOf(status) : INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.api.failed_call_with_status", status, body)
            );

        } catch (RestClientException e) {

            log.error("❌ REST general error: {}", e.getMessage());

            throw new ApiException(
                    INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.api.failed_call", e.getMessage())
            );
        }
    }
}