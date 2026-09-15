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

    /**
     * Sentinel node code {@code ReportHierarchyService#loadRootBreakdown} gives its trailing
     * grand-total row. Visited (see {@link NodeVisitor}) with {@code (null, null)} — "the
     * caller's whole scope" — rather than being mistaken for a real region/district literally
     * named "TOTAL".
     */
    private static final String TOTAL_NODE_CODE = "TOTAL";

    private final OrganizationScopeResolver organizationScopeResolver;

    /**
     * Callback for {@link #flattenAll(List, BiFunction, Function, Predicate, NodeVisitor)},
     * invoked once per region- or district-level node with the {@code (regionCode,
     * districtCode)} tuple identifying that node's own subtree — the same shape a node-scoped
     * drill-down (e.g. an age/category breakdown) expects from {@code
     * ReportHierarchyService#resolveNode}.
     */
    @FunctionalInterface
    public interface NodeVisitor<N> {
        void visit(N node, String regionCode, String districtCode);
    }

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
        return flattenAll(rootBreakdown, childrenFetcher, codeOf, hasChildrenOf, null);
    }

    /**
     * Like {@link #flattenAll(List, BiFunction, Function, Predicate)}, but also calls {@code
     * nodeVisitor} for every region- and district-level node the walk passes through (never for
     * organization-level leaves — the deepest level this system models, and the "Jami" row,
     * visited with a {@code (null, null)} whole-scope tuple instead of being treated as a real
     * region/district). Lets a report attach one extra per-node dataset (e.g. an age-group
     * breakdown) to its export in the same walk, instead of a second separate tree walk.
     */
    public <N> List<N> flattenAll(
            List<N> rootBreakdown,
            BiFunction<String, String, List<N>> childrenFetcher,
            Function<N, String> codeOf,
            Predicate<N> hasChildrenOf,
            NodeVisitor<N> nodeVisitor
    ) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());
        List<N> result = new ArrayList<>();

        switch (scope.mode()) {
            case ALL -> appendRegions(rootBreakdown, childrenFetcher, codeOf, hasChildrenOf, result, nodeVisitor);
            case REGION -> appendDistricts(rootBreakdown, childrenFetcher, codeOf, hasChildrenOf, result, nodeVisitor);
            case DISTRICT, ORGANIZATION -> result.addAll(rootBreakdown);
            // rootBreakdown is already the bottom of the tree (organizations, or the
            // caller's own single-row total) — nothing more to fetch, and no node-scoped
            // breakdown to visit (organizations have none).
        }

        return result;
    }

    /**
     * Depth-first, pre-order: each region row is followed immediately by its own districts
     * (each in turn followed immediately by its own organizations), not by every other
     * region's rows first — so a spreadsheet reader always finds a parent directly above its
     * children, e.g. "Andijon viloyati" directly above its districts rather than after every
     * region in the scope.
     */
    private <N> void appendRegions(
            List<N> regions,
            BiFunction<String, String, List<N>> childrenFetcher,
            Function<N, String> codeOf,
            Predicate<N> hasChildrenOf,
            List<N> result,
            NodeVisitor<N> nodeVisitor
    ) {
        for (N region : regions) {
            result.add(region);
            String code = codeOf.apply(region);
            visit(nodeVisitor, region, code, code, null);
            if (!hasChildrenOf.test(region)) {
                continue;
            }
            List<N> districts = childrenFetcher.apply(code, null);
            appendDistricts(districts, childrenFetcher, codeOf, hasChildrenOf, result, nodeVisitor);
        }
    }

    private <N> void appendDistricts(
            List<N> districts,
            BiFunction<String, String, List<N>> childrenFetcher,
            Function<N, String> codeOf,
            Predicate<N> hasChildrenOf,
            List<N> result,
            NodeVisitor<N> nodeVisitor
    ) {
        for (N district : districts) {
            result.add(district);
            String code = codeOf.apply(district);
            visit(nodeVisitor, district, code, null, code);
            if (!hasChildrenOf.test(district)) {
                continue;
            }
            result.addAll(childrenFetcher.apply(null, code));
            // Organizations added here are leaves (the deepest level this system models) and
            // are never passed to nodeVisitor — there is no deeper node-scoped breakdown for
            // a single organization via this walk.
        }
    }

    private <N> void visit(NodeVisitor<N> nodeVisitor, N node, String code, String regionCode, String districtCode) {
        if (nodeVisitor == null) {
            return;
        }
        if (TOTAL_NODE_CODE.equals(code)) {
            nodeVisitor.visit(node, null, null);
        } else {
            nodeVisitor.visit(node, regionCode, districtCode);
        }
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
