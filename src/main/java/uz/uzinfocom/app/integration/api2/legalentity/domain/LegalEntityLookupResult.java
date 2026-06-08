package uz.uzinfocom.app.integration.api2.legalentity.domain;

import tools.jackson.databind.JsonNode;

public record LegalEntityLookupResult(
        String source,
        int upstreamStatus,
        JsonNode data
) {
}
