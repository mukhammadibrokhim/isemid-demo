package uz.uzinfocom.isemid.features.report.service.scope;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;

import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.report.specification.OrganizationLevelSpecification;

/**
 * Pure hierarchy/visibility resolution for the location report.
 *
 * <p>Translates the requesting organization plus an optional drill-down {@code parentCode}
 * into a {@link ReportScope} the repository turns directly into SQL. Performs no data
 * loading other than a cached catalog lookup to classify {@code parentCode}, and never
 * traverses the organization or location hierarchy in Java.</p>
 *
 * <p>Mirrors the visibility matrix previously expressed by
 * {@code SpecificationUtils.buildLevelPredicateInternal} /
 * {@code addSanepidAccessPredicate}.</p>
 */
@Component
public class ReportScopeResolver {

    private static final String TASHKENT_CITY_STATE_CODE = "UZ-TK";

    private final CatalogService catalogService;
    private final MessageUtil messageUtil;

    public ReportScopeResolver(CatalogService catalogService, MessageUtil messageUtil) {
        this.catalogService = catalogService;
        this.messageUtil = messageUtil;
    }

    /**
     1. org levelType is REPUBLICAN
     if parentCode is not null
     then according to reportDimensionQuery
     else
     REGION_WISE_SPECIFICATION

     2. org levelType is REGION
     if parentCode is not null (org.getStateCode/org.getCityCode)
     then according to reportDimensionQuery (city, neighborhood)
     else
     DISTRICT_WISE_SPECIFICATION

     3. org levelType is in (URBAN,AREA,DISTRICT,INTERDISTRICT,NOT_DEFINED)
     if levelType is URBAN and org.stateCode=UZ-TK
     then according to reportDimensionQuery (city, neighborhood)
     else
     OWN_ORGANIZATION_SPECIFICATION
     */
    /**
     * Resolves the {@link ReportScope} for the given organization.
     *
     * <ul>
     *   <li><b>Drill-down</b> (parentCode present): the catalog type of
     *       parentCode determines the next level of granularity
     *       (state -> districts, city -> neighborhoods, neighborhood -> own org).</li>
     *   <li><b>Top-level</b> (parentCode absent): the organization's
     *       {@link LevelType} determines its default reporting scope.</li>
     * </ul>
     */
    public ReportScope resolve(Organization org, String parentCode) {
        LevelResolution resolution = hasText(parentCode)
            ? resolveDrillDown(parentCode)
            : resolveFromOrganizationLevel(org);

        boolean hasSanepidPermission = org.getMedicalType() == MedicalType.SANEPID_SERVICE;

        return new ReportScope(
            resolution.specification(),
            resolution.effectiveParentCode(),
            org.getId(),
            resolution.hierarchyLevelNumber(),
            hasSanepidPermission
        );
    }

    /**
     * Classifies {@code parentCode} itself (the catalog row whose {@code code = parentCode})
     * to decide the next drill-down level.
     */
    private LevelResolution resolveDrillDown(String parentCode) {
        CatalogType parentType = lookupType(parentCode);
        return switch (parentType) {
            case state -> new LevelResolution(
                OrganizationLevelSpecification.DISTRICT_WISE_SPECIFICATION,
                parentCode,
                ReportDimension.REGION.level());

            case city -> new LevelResolution(
                OrganizationLevelSpecification.NEIGHBORHOOD_WISE_SPECIFICATION,
                parentCode,
                ReportDimension.DISTRICT.level());

            case neighborhood -> new LevelResolution(
                OrganizationLevelSpecification.OWN_ORGANIZATION_SPECIFICATION,
                parentCode,
                ReportDimension.NEIGHBOURHOOD.level());

            default -> throw new ApiException(
                NOT_FOUND,
                messageUtil.get("error.catalog.notfound", parentCode)
            );
        };
    }

    private LevelResolution resolveFromOrganizationLevel(Organization org) {
        LevelType level = org.getLevelType();
        int hierarchyLevelNumber = level.numericForm();

        return switch (level) {
            case REPUBLICAN -> new LevelResolution(
                OrganizationLevelSpecification.REGION_WISE_SPECIFICATION,
                null,
                hierarchyLevelNumber);

            case REGIONAL -> new LevelResolution(
                OrganizationLevelSpecification.DISTRICT_WISE_SPECIFICATION,
                org.getStateCode(),
                hierarchyLevelNumber);

            case URBAN -> isTashkentCity(org)
                ? new LevelResolution(
                OrganizationLevelSpecification.DISTRICT_WISE_SPECIFICATION,
                org.getStateCode(),
                hierarchyLevelNumber)
                : new LevelResolution(
                    OrganizationLevelSpecification.OWN_ORGANIZATION_SPECIFICATION,
                    org.getCityCode(),
                    hierarchyLevelNumber);

            case AREA, DISTRICT, INTERDISTRICT, NOT_DEFINED -> new LevelResolution(
                OrganizationLevelSpecification.OWN_ORGANIZATION_SPECIFICATION,
                org.getCityCode(),
                hierarchyLevelNumber);

            default -> throw new IllegalStateException("Unsupported level type: " + level);
        };
    }

    private boolean isTashkentCity(Organization org) {
        return TASHKENT_CITY_STATE_CODE.equals(org.getStateCode());
    }

    /**
     * The three values that vary together depending on drill-down depth or
     * organization level: the report specification, the parent code to scope
     * subsequent queries by, and the numeric hierarchy level.
     */
    private record LevelResolution(
        String specification,
        String effectiveParentCode,
        int hierarchyLevelNumber
    ) {}

    /**
     * Classifies {@code parentCode} itself (the catalog row whose {@code code = parentCode}),
     * so the drill-down switch can pick the next granularity down. This is a single cached
     * lookup for labelling only — it does not load report data.
     */
    private CatalogType lookupType(String parentCode) {
        Catalog catalog = catalogService.findByCode(parentCode);
        if (catalog == null) {
            throw new ApiException(NOT_FOUND, messageUtil.get("error.catalog.notfound", parentCode));
        }
        return CatalogType.valueOf(catalog.getType());
    }
}
