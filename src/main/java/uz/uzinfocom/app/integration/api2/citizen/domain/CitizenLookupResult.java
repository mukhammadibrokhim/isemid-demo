package uz.uzinfocom.app.integration.api2.citizen.domain;

import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.citizen.web.CitizenAddressResponse;

import java.util.List;

/**
 * @param addresses addresses the source already supplied (DHP carries them in the Patient itself);
 *                  {@code null} means the source has none and the caller may enrich via
 *                  {@code v3/citizenAddress}
 */
public record CitizenLookupResult(
        CitizenLookupType type,
        CitizenLookupSource source,
        int upstreamStatus,
        JsonNode data,
        List<CitizenAddressResponse> addresses
) {

    public CitizenLookupResult(
            CitizenLookupType type,
            CitizenLookupSource source,
            int upstreamStatus,
            JsonNode data
    ) {
        this(type, source, upstreamStatus, data, null);
    }
}
