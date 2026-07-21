package uz.uzinfocom.app.platform.integrationclient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Single source of truth for the scope literals granted to an
 * {@link IntegrationClient} and checked by every inbound-integration
 * controller's {@code @PreAuthorize}. Adding a new inbound integration form
 * type later is one new constant here.
 */
@Getter
@RequiredArgsConstructor
public enum IntegrationScope {
    FORM058_SUBMIT("form058:submit"),
    FORM0581_SUBMIT("form0581:submit");

    private final String claim;

    public static Optional<IntegrationScope> fromClaim(String claim) {
        return Arrays.stream(values())
                .filter(scope -> scope.claim.equals(claim))
                .findFirst();
    }
}
