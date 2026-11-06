package uz.uzinfocom.app.integration.api2.citizen.application;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.strategy.CitizenLookupStrategyRegistry;
import uz.uzinfocom.app.integration.api2.citizen.validation.CitizenLookupValidator;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

@Service
@RequiredArgsConstructor
public class CitizenLookupService {

    /**
     * Dedicated audit logger: every personal-data lookup is recorded with who
     * asked, what kind of identifier and whether it succeeded - so misuse of
     * this endpoint (any authenticated user can call it) is traceable. The
     * identifier itself is masked; only its last 4 characters are kept.
     */
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT.CITIZEN_LOOKUP");

    private final CitizenLookupValidator validator;
    private final CitizenLookupStrategyRegistry strategyRegistry;
    private final CurrentUserProvider currentUserProvider;

    public CitizenLookupResult lookup(CitizenLookupRequest request) {
        CitizenLookupRequest validatedRequest = validator.validate(request);

        boolean succeeded = false;
        try {
            CitizenLookupResult result = strategyRegistry
                    .getRequired(validatedRequest.type())
                    .lookup(validatedRequest);
            succeeded = true;
            return result;
        } finally {
            AUDIT.info("Citizen lookup. userId={}, type={}, identifier={}, succeeded={}",
                    currentUserProvider.userIdOrNull(),
                    validatedRequest.type(),
                    mask(identifier(validatedRequest)),
                    succeeded);
        }
    }

    private static String identifier(CitizenLookupRequest request) {
        return switch (request.type()) {
            case NNUZB -> request.nnuzb();
            case PPN -> request.document();
            case CZ -> request.number();
        };
    }

    private static String mask(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 4
                ? "****"
                : "*".repeat(trimmed.length() - 4) + trimmed.substring(trimmed.length() - 4);
    }
}
