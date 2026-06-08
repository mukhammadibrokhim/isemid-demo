package uz.uzinfocom.app.integration.api2.citizen.api;

import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.citizen.application.mapper.CitizenResponseMapper;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupSource;

public record CitizenLookupResponse(
        boolean success,
        String message,
        String source,
        int status,
        String result,
        String comments,
        JsonNode data
) {

    public CitizenLookupResponse(
            boolean success,
            String message,
            CitizenLookupSource source,
            int status,
            JsonNode payload
    ) {
        this(
                success,
                message,
                source == null ? null : source.name(),
                status,
                CitizenResponseMapper.result(payload),
                CitizenResponseMapper.comments(payload),
                CitizenResponseMapper.data(payload)
        );
    }
}
