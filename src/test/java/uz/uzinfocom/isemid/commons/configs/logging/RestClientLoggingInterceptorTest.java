package uz.uzinfocom.isemid.commons.configs.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

class RestClientLoggingInterceptorTest {

    private final RestClientLoggingInterceptor interceptor =
            new RestClientLoggingInterceptor(new ObjectMapper());

    @Test
    void intercept_stillAllowsDownstreamToReadResponseBodyAfterLogging() throws IOException {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("https://test-lis.sanepid.uz/api/v1/publicapi/act-code"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        byte[] payload = "3".getBytes(StandardCharsets.UTF_8);
        ClientHttpResponse rawResponse = new SingleDrainClientHttpResponse(payload);
        ClientHttpRequestExecution execution = (req, body) -> rawResponse;

        ClientHttpResponse result = interceptor.intercept(request, new byte[0], execution);

        // Simulates what Spring's RestClient message converter does AFTER the interceptor
        // has already logged (and thereby drained) the response body.
        String bodyReadByMessageConverter =
                StreamUtils.copyToString(result.getBody(), StandardCharsets.UTF_8);

        assertThat(bodyReadByMessageConverter).isEqualTo("3");
    }

    /**
     * Mimics Apache HttpClient5's EofSensorInputStream/ChunkedInputStream: once the stream is
     * read to EOF, the underlying connection is released back to the pool and any further read
     * on that same stream instance fails with "Stream already closed".
     */
    private static final class SingleDrainClientHttpResponse implements ClientHttpResponse {
        private final InputStream body;

        SingleDrainClientHttpResponse(byte[] payload) {
            this.body = new AutoClosingOnEofInputStream(new ByteArrayInputStream(payload));
        }

        @Override
        public InputStream getBody() {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }

        @Override
        public HttpStatusCode getStatusCode() {
            return HttpStatusCode.valueOf(200);
        }

        @Override
        public String getStatusText() {
            return "OK";
        }

        @Override
        public void close() {
            // no-op
        }
    }

    private static final class AutoClosingOnEofInputStream extends FilterInputStream {
        private boolean drained = false;

        AutoClosingOnEofInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            assertNotDrained();
            int result = super.read();
            if (result == -1) {
                drained = true;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            assertNotDrained();
            int result = super.read(b, off, len);
            if (result == -1) {
                drained = true;
            }
            return result;
        }

        private void assertNotDrained() throws IOException {
            if (drained) {
                throw new IOException("Stream already closed");
            }
        }
    }
}
