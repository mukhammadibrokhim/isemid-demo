package uz.uzinfocom.app.orchestration.scope;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.orchestration.scope.jpa.OrganizationScopeOrganizationIdResolver;

/**
 * Point check "is this one organization inside the caller's resolved scope?" -
 * the single-row counterpart of {@code OrganizationScopePredicateFactory}, which
 * builds the same rules as a query predicate for list endpoints. Same rules:
 * non-SANEPID_SERVICE and {@link OrganizationScopeMode#ORGANIZATION} -> own
 * organization only; {@code ALL} -> anything; {@code REGION}/{@code DISTRICT} ->
 * the cached active organization ids of that region/district.
 */
@Component
@RequiredArgsConstructor
public class OrganizationScopeAccess {

    private final OrganizationScopeOrganizationIdResolver organizationIdResolver;

    public boolean covers(ResolvedOrganizationScope scope, Long organizationId) {
        if (scope == null || organizationId == null) {
            return false;
        }

        if (!scope.isSanepidService()) {
            return organizationId.equals(scope.organizationId());
        }

        return switch (scope.mode()) {
            case ALL -> true;
            case ORGANIZATION -> organizationId.equals(scope.organizationId());
            case REGION, DISTRICT -> organizationId.equals(scope.organizationId())
                    || organizationIdResolver
                    .resolveScopeOrganizationIds(scope.mode(), scope.regionCode(), scope.districtCode())
                    .contains(organizationId);
        };
    }
}
