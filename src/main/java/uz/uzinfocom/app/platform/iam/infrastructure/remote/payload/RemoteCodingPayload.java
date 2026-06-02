package uz.uzinfocom.app.platform.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteCodingPayload(
        String system,
        String code,
        String display
) {

    public boolean codeEqualsAny(String... codes) {
        return RemotePayloadSupport.equalsAnyIgnoreCase(code, codes);
    }

    public boolean systemContains(String systemPart) {
        return RemotePayloadSupport.containsIgnoreCase(system, systemPart);
    }
}