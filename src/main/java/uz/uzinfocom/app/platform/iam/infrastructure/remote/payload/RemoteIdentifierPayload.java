package uz.uzinfocom.app.platform.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteIdentifierPayload(
        String use,
        String system,
        RemoteCodeableConceptPayload type,
        JsonNode value
) {

    public String valueAsString() {
        return RemotePayloadSupport.text(value);
    }

    public boolean hasTypeCode(String... codes) {
        return type != null && type.hasCode(codes);
    }

    public boolean systemContains(String systemPart) {
        return RemotePayloadSupport.containsIgnoreCase(system, systemPart);
    }
}