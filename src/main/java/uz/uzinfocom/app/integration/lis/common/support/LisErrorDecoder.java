package uz.uzinfocom.app.integration.lis.common.support;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import uz.uzinfocom.app.integration.lis.common.exception.LisAuthenticationException;
import uz.uzinfocom.app.integration.lis.common.exception.LisBadRequestException;
import uz.uzinfocom.app.integration.lis.common.exception.LisException;
import uz.uzinfocom.app.integration.lis.common.exception.LisTimeoutException;
import uz.uzinfocom.app.integration.lis.common.exception.LisUnavailableException;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * Turns a LIS HTTP status or a transport failure into the matching
 * {@link LisException}, the same job {@code Api2ErrorDecoder} does for API2.
 *
 * <p>Kept deliberately smaller than API2's: LIS's create-act endpoint returns
 * a bare act id rather than an envelope, so there is no "HTTP 200 that is
 * actually an error" payload to unwrap.
 */
@Component
public class LisErrorDecoder {

    public LisException decode(String operation, HttpStatusCode statusCode, String responseBody) {
        int status = statusCode.value();
        String safeBody = LisResponseSanitizer.sanitize(responseBody);

        return switch (status) {
            case 400, 404, 409, 422 -> new LisBadRequestException(operation, status, safeBody);
            case 401, 403 -> new LisAuthenticationException(operation, status);
            case 408, 504 -> new LisTimeoutException(operation, status);
            default -> new LisUnavailableException(operation, status, safeBody);
        };
    }

    /**
     * Transport-level failure — nothing ever reached LIS, or the connection
     * broke before a status was read. The original exception is preserved as
     * the cause (via {@code initCause}) purely for log/stack-trace purposes;
     * it never reaches a client response.
     */
    public LisException decodeTransport(String operation, RestClientException exception) {
        LisException decoded = containsTimeout(exception)
                ? new LisTimeoutException(operation, null)
                : new LisUnavailableException(operation, null, exception.getMessage());
        decoded.initCause(exception);
        return decoded;
    }

    private boolean containsTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof TimeoutException
                    || current instanceof org.apache.hc.core5.http.ConnectionClosedException) {
                return true;
            }
            if (current instanceof ResourceAccessException
                    && current.getMessage() != null
                    && current.getMessage().toLowerCase().contains("timed out")) {
                return true;
            }
            current = current.getCause() == current ? null : current.getCause();
        }
        return false;
    }
}
