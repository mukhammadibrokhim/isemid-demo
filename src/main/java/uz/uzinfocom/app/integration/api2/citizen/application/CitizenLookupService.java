package uz.uzinfocom.app.integration.api2.citizen.application;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.strategy.CitizenLookupStrategyRegistry;
import uz.uzinfocom.app.integration.api2.citizen.validation.CitizenLookupValidator;
import uz.uzinfocom.app.integration.dhp.citizen.application.DhpCitizenLookupService;
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

    /** Login provider key of callers whose citizen data comes from DHP instead of API2. */
    static final String DHP_PROVIDER_KEY = "dhp";

    private final CitizenLookupValidator validator;
    private final CitizenLookupStrategyRegistry strategyRegistry;
    private final DhpCitizenLookupService dhpCitizenLookupService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Callers who signed in through DHP are served from DHP's {@code Patient/$populate}
     * (same response shape, no birth date needed); everyone else - SSO and any other
     * provider - keeps using API2.
     */
    public CitizenLookupResult lookup(CitizenLookupRequest request) {
        boolean viaDhp = DHP_PROVIDER_KEY.equals(currentUserProvider.currentProviderKeyOrNull());
        CitizenLookupRequest validatedRequest = validator.validate(request, !viaDhp);

        boolean succeeded = false;
        try {
            CitizenLookupResult result = viaDhp
                    ? dhpCitizenLookupService.lookup(validatedRequest)
                    : strategyRegistry.getRequired(validatedRequest.type()).lookup(validatedRequest);
            succeeded = true;
            return result;
        } finally {
            AUDIT.info("Citizen lookup. userId={}, provider={}, type={}, identifier={}, succeeded={}",
                    currentUserProvider.userIdOrNull(),
                    viaDhp ? DHP_PROVIDER_KEY : "api2",
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
