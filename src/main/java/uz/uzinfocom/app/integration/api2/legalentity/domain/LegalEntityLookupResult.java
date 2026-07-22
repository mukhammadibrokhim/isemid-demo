package uz.uzinfocom.app.integration.api2.legalentity.domain;

import tools.jackson.databind.JsonNode;

public record LegalEntityLookupResult(
        LegalEntityLookupSource source,
        int upstreamStatus,
        JsonNode data
) {
}
