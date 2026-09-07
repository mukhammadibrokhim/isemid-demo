package uz.uzinfocom.app.modules.report.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Flattens a whole geography-first report tree (republic→region→district→
 * organization) into one flat row list, for reports whose own {@code
 * getRoot}/{@code getChildren} query-service methods (see {@code
 * ReportHierarchyService}) only ever return one level per call. Every
 * geography-first report's {@code ExcelExportSource} reuses this instead of
 * re-deriving the same recursive walk.
 * <p>
 * Mirrors {@link ReportHierarchyService#loadChildren}'s own scope switch
 * (ALL: region→district→organization; REGION: district→organization;
 * DISTRICT/ORGANIZATION: already leaves) instead of guessing a node's
 * geography level from its code shape. The caller supplies its own already-
 * fetched {@code rootBreakdown} (whatever {@code getRoot(...)} returned,
 * "Jami" row included) plus a {@code childrenFetcher} matching its own {@code
 * getChildren(regionCode, districtCode, ...)} signature — this class only
 * decides *when* to call it, never *how*.
 */
@Component
@RequiredArgsConstructor
public class ReportHierarchyExportFlattener {

    private final OrganizationScopeResolver organizationScopeResolver;

    /**
     * @param rootBreakdown   the report's own {@code getRoot(...)} result — regions for an
     *                        ALL-scope caller, districts for REGION scope, organizations for
     *                        DISTRICT scope, or a single row for ORGANIZATION scope, plus the
     *                        trailing "Jami" row (already {@code hasChildren = false}, so it is
     *                        never recursed into)
     * @param childrenFetcher {@code (regionCode, districtCode) -> next level}, matching the
     *                        report's own {@code getChildren} — called with a non-null
     *                        {@code regionCode} to fetch a region's districts, or a non-null
     *                        {@code districtCode} to fetch a district's organizations
     * @param codeOf          extracts a node's own code (the value to pass back into
     *                        {@code childrenFetcher})
     * @param hasChildrenOf   whether a node has a deeper level to recurse into
     */
    public <N> List<N> flattenAll(
            List<N> rootBreakdown,
            BiFunction<String, String, List<N>> childrenFetcher,
            Function<N, String> codeOf,
            Predicate<N> hasChildrenOf
    ) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());
        List<N> result = new ArrayList<>(rootBreakdown);

        switch (scope.mode()) {
            case ALL -> {
                for (N region : rootBreakdown) {
                    if (!hasChildrenOf.test(region)) {
                        continue;
                    }
                    List<N> districts = childrenFetcher.apply(codeOf.apply(region), null);
                    result.addAll(districts);
                    appendOrganizationsUnderDistricts(districts, childrenFetcher, codeOf, hasChildrenOf, result);
                }
            }
            case REGION -> appendOrganizationsUnderDistricts(rootBreakdown, childrenFetcher, codeOf, hasChildrenOf, result);
            case DISTRICT, ORGANIZATION -> {
                // rootBreakdown is already the bottom of the tree (organizations, or the
                // caller's own single-row total) — nothing more to fetch.
            }
        }

        return result;
    }

    private <N> void appendOrganizationsUnderDistricts(
            List<N> districts,
            BiFunction<String, String, List<N>> childrenFetcher,
            Function<N, String> codeOf,
            Predicate<N> hasChildrenOf,
            List<N> result
    ) {
        for (N district : districts) {
            if (!hasChildrenOf.test(district)) {
                continue;
            }
            result.addAll(childrenFetcher.apply(null, codeOf.apply(district)));
        }
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
