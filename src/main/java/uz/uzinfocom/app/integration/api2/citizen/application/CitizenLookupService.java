package uz.uzinfocom.app.integration.api2.citizen.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.strategy.CitizenLookupStrategyRegistry;
import uz.uzinfocom.app.integration.api2.citizen.validation.CitizenLookupValidator;

@Service
@RequiredArgsConstructor
public class CitizenLookupService {

    private final CitizenLookupValidator validator;
    private final CitizenLookupStrategyRegistry strategyRegistry;

    public CitizenLookupResult lookup(CitizenLookupRequest request) {
        CitizenLookupRequest validatedRequest = validator.validate(request);

        return strategyRegistry
                .getRequired(validatedRequest.type())
                .lookup(validatedRequest);
    }
}
