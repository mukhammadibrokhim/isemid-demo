package uz.uzinfocom.app.platform.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.platform.i18n.MessageResolver;

public final class RestClientUtil {

    private RestClientUtil() {
    }

    public static <T> T makeRequest(
            String url,
            HttpMethod method,
            String token,
            Object body,
            HttpHeaders headers,
            Class<T> responseType,
            MessageResolver messages
    ) {
        RestClient.RequestBodySpec request = RestClient.create()
                .method(method)
                .uri(url)
                .headers(target -> {
                    if (headers != null) {
                        target.addAll(headers);
                    }
                    if (token != null && !token.isBlank()) {
                        target.setBearerAuth(token);
                    }
                });

        if (body == null) {
            return request.retrieve().body(responseType);
        }

        return request.body(body).retrieve().body(responseType);
    }
}
