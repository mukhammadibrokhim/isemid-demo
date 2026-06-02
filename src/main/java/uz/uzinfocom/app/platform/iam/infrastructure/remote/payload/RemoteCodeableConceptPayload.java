package uz.uzinfocom.app.platform.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteCodeableConceptPayload(
        List<RemoteCodingPayload> coding,
        String text
) {

    public boolean hasCode(String... codes) {
        if (coding == null || coding.isEmpty()) {
            return false;
        }

        return coding.stream()
                .anyMatch(item -> item.codeEqualsAny(codes));
    }

    public Optional<RemoteCodingPayload> firstCodingBySystemContains(String systemPart) {
        if (coding == null || coding.isEmpty()) {
            return Optional.empty();
        }

        return coding.stream()
                .filter(item -> item.systemContains(systemPart))
                .findFirst();
    }
}