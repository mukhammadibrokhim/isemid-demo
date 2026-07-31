package uz.uzinfocom.app.modules.report.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationGeoProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationNameProjection;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.reference.domain.District;
import uz.uzinfocom.app.platform.reference.domain.Region;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scope-aware "walk one level of the republic→region→district→organization
 * hierarchy and aggregate counts" engine, shared by every report under
 * {@code modules.report}. Promoted out of "Form 1"'s {@code
 * Form1ReportQueryService} once a second and third report needed the
 * identical mechanics against different source data — see that class'
 * original Javadoc, which reserved this exact promotion.
 * <p>
 * This service owns geography and scope; it knows nothing about how a
 * report counts cases — that is supplied per call via {@link
 * ReportCountSource}, so adding report #4, #5, ... never requires touching
 * this class (Strategy + Dependency Inversion).
 */
@Service
@RequiredArgsConstructor
public class ReportHierarchyService {

    private final OrganizationScopeResolver organizationScopeResolver;
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver;
    private final OrganizationRepository organizationRepository;
    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;
    private final ReferenceLookupService referenceLookupService;
    private final OrganizationNameResolver organizationNameResolver;
    private final MessageResolver messageResolver;

    public <C> ReportHierarchyNode<C> loadRoot(
            Organization currentOrganization,
            ReportCountSource<C> countSource,
            ReportDateRange range,
            String diagnosisCode
    ) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);
        List<Long> organizationIds = rootOrganizationIds(scope);

        return new ReportHierarchyNode<>(
                rootCode(scope),
                rootName(scope, currentOrganization),
                scope.mode() != OrganizationScopeMode.ORGANIZATION,
                countSource.total(organizationIds, range, diagnosisCode)
        );
    }

    /**
     * The "first screen" every report's {@code root} endpoint now returns:
     * the caller's whole access scope broken down one level (regions for a
     * REPUBLICAN/ALL-scope caller, districts for REGION scope,
     * organizations for DISTRICT scope — exactly {@link #loadChildren}
     * with no {@code regionCode}/{@code districtCode}), plus a trailing
     * "Jami" (grand total) row summing the whole scope, so the frontend can
     * render a complete table from one call instead of needing a follow-up
     * {@link #loadChildren} just to see anything. {@link #loadChildren}
     * itself is unchanged and still exists for drilling deeper — {@code
     * children(regionCode=...)} for a region's districts, {@code
     * children(districtCode=...)} for a district's organizations.
     * <p>
     * The "Jami" row is folded from the breakdown's own counts ({@code
     * countSource.merge}), never from a second {@link #loadRoot} database
     * round-trip — for a republic-wide caller {@code loadRoot}'s total query
     * re-scans essentially the same rows the breakdown query already
     * touched (the "in scope" org-id list is ~95% of all organizations at
     * that level, so it barely narrows anything), doubling an already
     * multi-second query for no benefit: summing 14-1000 already-aggregated
     * per-node rows in memory is arithmetically identical and free.
     * <p>
     * An ORGANIZATION-scope caller (a single non-SANEPID organization, or a
     * SANEPID branch with no level configured) has no breakdown to show —
     * {@link #loadChildren} returns an empty list for that scope — so this
     * falls back to the single row {@link #loadRoot} already produces for
     * that case (the caller's own totals, a cheap single-organization
     * query), matching what the caller could see before this method
     * existed.
     */
    public <C> List<ReportHierarchyNode<C>> loadRootBreakdown(
            Organization currentOrganization,
            ReportCountSource<C> countSource,
            ReportDateRange range,
            String diagnosisCode
    ) {
        List<ReportHierarchyNode<C>> breakdown =
                loadChildren(currentOrganization, null, null, countSource, range, diagnosisCode);

        if (breakdown.isEmpty()) {
            return List.of(loadRoot(currentOrganization, countSource, range, diagnosisCode));
        }

        C total = breakdown.stream()
                .map(ReportHierarchyNode::counts)
                .reduce(countSource.empty(), countSource::merge);

        List<ReportHierarchyNode<C>> result = new ArrayList<>(breakdown);
        result.add(new ReportHierarchyNode<>("TOTAL", messageResolver.resolve("report.scope.total"), false, total));
        return result;
    }

    public <C> List<ReportHierarchyNode<C>> loadChildren(
            Organization currentOrganization,
            String regionCode,
            String districtCode,
            ReportCountSource<C> countSource,
            ReportDateRange range,
            String diagnosisCode
    ) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);

        if (scope.mode() == OrganizationScopeMode.ORGANIZATION) {
            return List.of();
        }

        if (StringUtils.hasText(districtCode)) {
            return buildOrganizationBreakdown(
                    requireInScopeDistrict(scope, districtCode), countSource, range, diagnosisCode
            );
        }

        if (StringUtils.hasText(regionCode)) {
            return buildDistrictBreakdown(
                    requireInScopeRegion(scope, regionCode), countSource, range, diagnosisCode
            );
        }

        return switch (scope.mode()) {
            case ALL -> buildRegionBreakdown(countSource, range, diagnosisCode);
            case REGION -> buildDistrictBreakdown(scope.regionCode(), countSource, range, diagnosisCode);
            case DISTRICT -> buildOrganizationBreakdown(scope.districtCode(), countSource, range, diagnosisCode);
            case ORGANIZATION -> List.of();
        };
    }

    /**
     * Resolves an explicit {@code regionCode}/{@code districtCode} (or, with
     * both omitted, the caller's whole access scope) into a single node
     * identity plus the flat list of organization ids under it — for a
     * report that needs one aggregate over a node's whole sub-tree rather
     * than a one-level-deeper breakdown (e.g. an age-structure drill-down
     * triggered from a specific row of {@link #loadRootBreakdown}/{@link
     * #loadChildren}). Reuses the exact same scope validation {@link
     * #loadChildren} applies to {@code regionCode}/{@code districtCode}, so
     * a request outside the caller's access scope is rejected the same way.
     */
    public ResolvedReportNode resolveNode(
            Organization currentOrganization, String regionCode, String districtCode
    ) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);

        if (StringUtils.hasText(districtCode)) {
            String validDistrict = requireInScopeDistrict(scope, districtCode);
            return new ResolvedReportNode(
                    validDistrict,
                    referenceLookupService.getDistrictName(validDistrict),
                    organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                            OrganizationScopeMode.DISTRICT, null, validDistrict
                    )
            );
        }

        if (StringUtils.hasText(regionCode)) {
            String validRegion = requireInScopeRegion(scope, regionCode);
            return new ResolvedReportNode(
                    validRegion,
                    referenceLookupService.getRegionName(validRegion),
                    organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                            OrganizationScopeMode.REGION, validRegion, null
                    )
            );
        }

        return new ResolvedReportNode(
                rootCode(scope), rootName(scope, currentOrganization), rootOrganizationIds(scope)
        );
    }

    private String requireInScopeRegion(ResolvedOrganizationScope scope, String regionCode) {
        return switch (scope.mode()) {
            case ALL -> regionCode;
            case REGION -> {
                if (!scope.regionCode().equalsIgnoreCase(regionCode)) {
                    throw new ScopeViolationException("organization.scope_violation");
                }
                yield regionCode;
            }
            case DISTRICT, ORGANIZATION -> throw new ScopeViolationException("organization.scope_violation");
        };
    }

    private String requireInScopeDistrict(ResolvedOrganizationScope scope, String districtCode) {
        return switch (scope.mode()) {
            case ALL -> districtCode;
            case REGION -> {
                District district = districtRepository.findByCodeAndDeletedFalse(districtCode)
                        .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
                if (!scope.regionCode().equalsIgnoreCase(district.getParentCode())) {
                    throw new ScopeViolationException("organization.scope_violation");
                }
                yield districtCode;
            }
            case DISTRICT -> {
                if (!scope.districtCode().equalsIgnoreCase(districtCode)) {
                    throw new ScopeViolationException("organization.scope_violation");
                }
                yield districtCode;
            }
            case ORGANIZATION -> throw new ScopeViolationException("organization.scope_violation");
        };
    }

    private List<Long> rootOrganizationIds(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case ALL -> organizationRepository.findActiveIdAndRegionCode().stream()
                    .map(OrganizationGeoProjection::id)
                    .toList();
            case REGION -> organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                    OrganizationScopeMode.REGION, scope.regionCode(), null
            );
            case DISTRICT -> organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                    OrganizationScopeMode.DISTRICT, null, scope.districtCode()
            );
            case ORGANIZATION -> List.of(scope.organizationId());
        };
    }

    private String rootCode(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case ALL -> "UZ";
            case REGION -> scope.regionCode();
            case DISTRICT -> scope.districtCode();
            case ORGANIZATION -> String.valueOf(scope.organizationId());
        };
    }

    private String rootName(ResolvedOrganizationScope scope, Organization currentOrganization) {
        return switch (scope.mode()) {
            case ALL -> messageResolver.resolve("report.scope.republic");
            case REGION -> referenceLookupService.getRegionName(scope.regionCode());
            case DISTRICT -> referenceLookupService.getDistrictName(scope.districtCode());
            case ORGANIZATION -> organizationNameResolver.resolve(currentOrganization);
        };
    }

    /**
     * DISTRICT-scope callers (and an explicit districtCode request) have no
     * sub-district geography left, so this lists the district's own
     * organizations instead.
     */
    private <C> List<ReportHierarchyNode<C>> buildOrganizationBreakdown(
            String districtCode, ReportCountSource<C> countSource, ReportDateRange range, String diagnosisCode
    ) {
        List<OrganizationNameProjection> organizations = organizationRepository.findActiveByDistrictCode(districtCode);
        List<Long> organizationIds = organizations.stream().map(OrganizationNameProjection::id).toList();

        Map<Long, C> countsByOrgId = countSource.groupedByOrganization(organizationIds, range, diagnosisCode);

        return organizations.stream()
                .map(org -> new ReportHierarchyNode<>(
                        String.valueOf(org.id()),
                        organizationNameResolver.resolve(new OrganizationLocalizedName(
                                org.name(), org.nameUz(), org.nameUzCyril(), org.nameRu(), org.nameKaa()
                        )),
                        false,
                        countsByOrgId.getOrDefault(org.id(), countSource.empty())
                ))
                .toList();
    }

    private <C> List<ReportHierarchyNode<C>> buildDistrictBreakdown(
            String regionCode, ReportCountSource<C> countSource, ReportDateRange range, String diagnosisCode
    ) {
        List<District> districts = districtRepository.findAllByParentCodeAndDeletedFalseOrderByNameUzAsc(regionCode);
        List<OrganizationGeoProjection> organizations =
                organizationRepository.findActiveIdAndDistrictCodeByRegionCode(regionCode);

        Map<String, C> countsByDistrict = aggregateByGeoCode(organizations, countSource, range, diagnosisCode);

        return districts.stream()
                .map(district -> new ReportHierarchyNode<>(
                        district.getCode(),
                        referenceLookupService.getDistrictName(district.getCode()),
                        true,
                        countsByDistrict.getOrDefault(district.getCode(), countSource.empty())
                ))
                .toList();
    }

    private <C> List<ReportHierarchyNode<C>> buildRegionBreakdown(
            ReportCountSource<C> countSource, ReportDateRange range, String diagnosisCode
    ) {
        List<Region> regions = regionRepository.findAllByDeletedFalseOrderByNameUzAsc();
        List<OrganizationGeoProjection> organizations = organizationRepository.findActiveIdAndRegionCode();

        Map<String, C> countsByRegion = aggregateByGeoCode(organizations, countSource, range, diagnosisCode);

        return regions.stream()
                .map(region -> new ReportHierarchyNode<>(
                        region.getCode(),
                        referenceLookupService.getRegionName(region.getCode()),
                        true,
                        countsByRegion.getOrDefault(region.getCode(), countSource.empty())
                ))
                .toList();
    }

    /**
     * Small, bounded in-memory merge (at most a few hundred organizations
     * per region, at most ~1000 countrywide) — not a full-table per-row
     * merge, just bucketing an already org-grouped, already-small count
     * list by geo code.
     */
    private <C> Map<String, C> aggregateByGeoCode(
            List<OrganizationGeoProjection> organizations,
            ReportCountSource<C> countSource,
            ReportDateRange range,
            String diagnosisCode
    ) {
        Map<Long, String> geoCodeByOrgId = new HashMap<>();
        for (OrganizationGeoProjection organization : organizations) {
            geoCodeByOrgId.put(organization.id(), organization.code());
        }

        List<Long> organizationIds = organizations.stream().map(OrganizationGeoProjection::id).toList();

        Map<String, C> countsByGeoCode = new HashMap<>();
        countSource.groupedByOrganization(organizationIds, range, diagnosisCode).forEach((organizationId, counts) -> {
            String geoCode = geoCodeByOrgId.get(organizationId);
            if (geoCode != null) {
                countsByGeoCode.merge(geoCode, counts, countSource::merge);
            }
        });

        return countsByGeoCode;
    }
}
