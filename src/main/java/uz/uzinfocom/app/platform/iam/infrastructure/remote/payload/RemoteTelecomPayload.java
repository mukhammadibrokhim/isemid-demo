package uz.uzinfocom.app.platform.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteTelecomPayload(
        String use,
        String system,
        String value
) {

    public boolean systemEquals(String expectedSystem) {
        return RemotePayloadSupport.equalsAnyIgnoreCase(system, expectedSystem);
    }
}