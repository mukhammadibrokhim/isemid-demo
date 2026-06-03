package uz.uzinfocom.app.platform.iam.infrastructure.remote.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteAddressPayload(
        String use,
        String type,
        String country,
        String state,
        String city,
        String district,
        String text,
        JsonNode line
) {

    public List<String> lines() {
        return RemotePayloadSupport.textList(line);
    }

    public String lineText() {
        if (StringUtils.hasText(text)) {
            return text;
        }

        List<String> lines = lines();

        if (lines.isEmpty()) {
            return null;
        }

        return String.join(", ", lines);
    }
}