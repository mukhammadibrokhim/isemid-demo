package uz.uzinfocom.isemid.commons.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uz.uzinfocom.isemid.commons.configs.http.UnsafeHttpClientFactory;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.exceptions.handler.ErrorDecoder;

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

            throw decodeHttpError(status, responseBody, null, messageUtil);
        } catch (RestClientException e) {
            log.error("REST call to {} failed: {}", url, e.getMessage());

            throw new ApiException(
                    INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.api.failed_call", e.getMessage())
            );
        }
    }

    public static <T, R> T makeRequest(String url, HttpMethod method, String token, R requestBody,
                                       Class<T> responseType, MessageUtil messageUtil,
                                       @Nullable ErrorDecoder errorDecoder) {
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

            throw decodeHttpError(status, responseBody, errorDecoder, messageUtil);
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

            if (responseType == Void.class) {
                return null;
            }

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

            throw decodeHttpError(status, body, null, messageUtil);

        } catch (RestClientException e) {

            log.error("❌ REST general error: {}", e.getMessage());

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
            MessageUtil messageUtil,
            @Nullable ErrorDecoder errorDecoder
    ) {
        HttpHeaders headers = new HttpHeaders();

        if (token != null && !token.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

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

            if (responseType == Void.class) {
                return null;
            }

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

            throw decodeHttpError(status, body, errorDecoder, messageUtil);

        } catch (RestClientException e) {

            log.error("❌ REST general error: {}", e.getMessage());

            throw new ApiException(
                    INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.api.failed_call", e.getMessage())
            );
        }
    }

    private static ApiException decodeHttpError(int status, String body,
                                                @Nullable ErrorDecoder errorDecoder,
                                                MessageUtil messageUtil) {
        HttpStatus httpStatus = HttpStatus.resolve(status) != null ? HttpStatus.valueOf(status) : INTERNAL_SERVER_ERROR;
        if (errorDecoder != null) {
            return errorDecoder.decode(httpStatus, body);
        }
        return new ApiException(httpStatus, messageUtil.get("error.api.failed_call_with_status", status, body));
    }
}