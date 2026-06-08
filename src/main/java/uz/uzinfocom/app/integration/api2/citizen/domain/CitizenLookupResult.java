package uz.uzinfocom.app.integration.api2.citizen.domain;

import tools.jackson.databind.JsonNode;

public record CitizenLookupResult(
        CitizenLookupType type,
        CitizenLookupSource source,
        int upstreamStatus,
        JsonNode data
) {
}
