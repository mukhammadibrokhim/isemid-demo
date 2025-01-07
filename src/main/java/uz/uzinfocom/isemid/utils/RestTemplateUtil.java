package uz.uzinfocom.isemid.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class RestTemplateUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);

    public static <T, R> T makeRequest(String url, HttpMethod method, String token, R requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();

        logger.info("|Request: {} {} | Request Body: {} |  Headers: {}|", method, url, requestBody, headers);

        HttpEntity<R> entity = new HttpEntity<>(requestBody, headers);

        try {

            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

            logger.info("|Response: {} | Status: {} | Response Body: {}|", response.getStatusCode(), response.getBody(), response.getBody());

            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Error occurred while making the request to {}: {}", url, e.getMessage());
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to make REST call: " + e.getMessage());
        }
    }

}
