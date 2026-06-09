package uz.uzinfocom.app.integration.api2.common.support;

import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;

public record Api2ResponseBody(
        JsonNode json,
        String safeRawBody,
        MediaType contentType,
        boolean empty,
        boolean bodyReadFailed
) {

    public boolean hasJson() {
        return json != null;
    }
}
