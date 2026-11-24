package uz.uzinfocom.app.modules.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;

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

    public String typeCode() {
        if (type == null || type.coding() == null) {
            return null;
        }

        return type.coding().stream()
                .map(RemoteCodingPayload::code)
                .filter(code -> code != null && !code.isBlank())
                .findFirst()
                .orElse(null);
    }

    public boolean hasTypeCode(String... codes) {
        return type != null && type.hasCode(codes);
    }

    public boolean systemContains(String systemPart) {
        return RemotePayloadSupport.containsIgnoreCase(system, systemPart);
    }
}