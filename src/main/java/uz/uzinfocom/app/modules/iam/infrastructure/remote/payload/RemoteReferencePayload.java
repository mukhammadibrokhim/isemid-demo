package uz.uzinfocom.app.modules.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteReferencePayload(
        String reference,
        String display
) {
}