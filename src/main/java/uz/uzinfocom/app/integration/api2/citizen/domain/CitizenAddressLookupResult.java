package uz.uzinfocom.app.integration.api2.citizen.domain;

import tools.jackson.databind.JsonNode;

public record CitizenAddressLookupResult(
        int upstreamStatus,
        JsonNode data
) {
}
