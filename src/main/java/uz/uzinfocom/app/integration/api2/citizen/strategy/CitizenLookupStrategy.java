package uz.uzinfocom.app.integration.api2.citizen.strategy;

import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;

public interface CitizenLookupStrategy {

    CitizenLookupType type();

    CitizenLookupResult lookup(CitizenLookupRequest request);
}
