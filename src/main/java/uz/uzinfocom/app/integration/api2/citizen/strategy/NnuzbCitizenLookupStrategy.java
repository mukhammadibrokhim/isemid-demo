package uz.uzinfocom.app.integration.api2.citizen.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.api2.citizen.client.CitizenApi2Client;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;

@Component
@RequiredArgsConstructor
public class NnuzbCitizenLookupStrategy implements CitizenLookupStrategy {

    private final CitizenApi2Client citizenApi2Client;

    @Override
    public CitizenLookupType type() {
        return CitizenLookupType.NNUZB;
    }

    @Override
    public CitizenLookupResult lookup(CitizenLookupRequest request) {
        return citizenApi2Client.lookupByNnuzb(request.nnuzb(), request.birthDate());
    }
}
