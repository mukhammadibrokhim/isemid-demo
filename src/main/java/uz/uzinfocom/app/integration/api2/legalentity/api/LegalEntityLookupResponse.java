package uz.uzinfocom.app.integration.api2.legalentity.api;

import tools.jackson.databind.JsonNode;

public record LegalEntityLookupResponse(
        boolean success,
        String message,
        String source,
        int status,
        JsonNode data
) {
}
