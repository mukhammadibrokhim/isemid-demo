package uz.uzinfocom.app.orchestration.scope;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.iam.domain.Organization;

/**
 * Turns the caller's organization scope into the territory filter for territory-coded data.
 * {@code ALL} is unfiltered, {@code REGION} is the region, {@code DISTRICT} the district, and
 * a single {@code ORGANIZATION} narrows to its own district (or region when it has no
 * district code).
 */
@Component
@RequiredArgsConstructor
public class ScopeGeoFilterResolver {

    private final OrganizationScopeResolver organizationScopeResolver;

    public ScopeGeoFilter resolve(Organization currentOrganization) {
        return of(organizationScopeResolver.resolve(currentOrganization));
    }

    public static ScopeGeoFilter of(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case ALL -> ScopeGeoFilter.NONE;
            case REGION -> new ScopeGeoFilter(scope.regionCode(), null);
            case DISTRICT -> new ScopeGeoFilter(scope.regionCode(), scope.districtCode());
            case ORGANIZATION -> {
                if (StringUtils.hasText(scope.districtCode())) {
                    yield new ScopeGeoFilter(scope.regionCode(), scope.districtCode());
                }
                yield StringUtils.hasText(scope.regionCode())
                        ? new ScopeGeoFilter(scope.regionCode(), null)
                        : ScopeGeoFilter.NONE;
            }
        };
    }
}
