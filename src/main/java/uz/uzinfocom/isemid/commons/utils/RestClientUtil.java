package uz.uzinfocom.isemid.commons.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uz.uzinfocom.isemid.commons.dtos.ApiException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
public class RestClientUtil {

    public static <T, R> T makeRequest(String url, HttpMethod method, String token, R requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();

        log.info("|Request: {} {} | Request Body: {} |  Headers: {}|", method, url, requestBody, headers);

        HttpEntity<R> entity = new HttpEntity<>(requestBody, headers);

        try {

            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

            log.info("|Response: {} | Status: {} | Response Body: {}|", response.getStatusCode(), response.getBody(), response.getBody());
            if (response.hasBody()) {
                return response.getBody();
            }
            throw new ApiException(INTERNAL_SERVER_ERROR, "Не удалось выполнить вызов REST" );

        } catch (RestClientException e) {
            log.error("Error occurred while making the request to {}: {}", url, e.getMessage());
            throw new ApiException(INTERNAL_SERVER_ERROR, "Failed to make REST call: " + e.getMessage());
        }
    }
}
