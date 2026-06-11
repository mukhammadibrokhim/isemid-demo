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
public class ReportScopeResolverV2 {

    private static final String UZ_TK = "UZ-TK";

    private final CatalogService catalogService;
    private final MessageUtil messageUtil;

    public ReportScopeResolverV2(CatalogService catalogService, MessageUtil messageUtil) {
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
    public ReportScopeV2 resolve(Organization org, String parentCode) {
        String specification;
        String effectiveParentCode;
        int hierarchyLevelNumber;

        // Drill-down: depth is decided by the catalog type of parentCode.
        if (hasText(parentCode)) {
            CatalogType parentType = lookupType(parentCode);
            specification = switch (parentType) {
                case state ->{
                    hierarchyLevelNumber = ReportDimension.REGION.level();
                    yield OrganizationLevelSpecification.DISTRICT_WISE_SPECIFICATION;
                }

                case city ->{
                    hierarchyLevelNumber = ReportDimension.DISTRICT.level();
                    yield OrganizationLevelSpecification.NEIGHBORHOOD_WISE_SPECIFICATION;
                }

                case neighborhood ->{
                    hierarchyLevelNumber = ReportDimension.NEIGHBOURHOOD.level();
                    yield OrganizationLevelSpecification.OWN_ORGANIZATION_SPECIFICATION;
                }

                default ->
                    throw new ApiException(
                        NOT_FOUND,
                        messageUtil.get("error.catalog.notfound", parentCode)
                    );
            };

            effectiveParentCode = parentCode;

        } else {
            LevelType level = org.getLevelType();
            boolean uzTk = UZ_TK.equals(org.getStateCode());
            switch (level) {
                case REPUBLICAN -> {
                    specification =
                        OrganizationLevelSpecification.REGION_WISE_SPECIFICATION;
                    effectiveParentCode = null;
                }

                case REGIONAL -> {
                    specification =
                        OrganizationLevelSpecification.DISTRICT_WISE_SPECIFICATION;
                    effectiveParentCode = org.getStateCode();
                }

                case URBAN -> {
                    if (uzTk) {
                        specification =
                            OrganizationLevelSpecification.DISTRICT_WISE_SPECIFICATION;
                        effectiveParentCode = org.getStateCode();
                    } else {
                        specification =
                            OrganizationLevelSpecification.OWN_ORGANIZATION_SPECIFICATION;
                        effectiveParentCode = org.getCityCode();
                    }
                }

                case AREA,
                     DISTRICT,
                     INTERDISTRICT,
                     NOT_DEFINED -> {
                    specification =
                        OrganizationLevelSpecification.OWN_ORGANIZATION_SPECIFICATION;
                    effectiveParentCode = org.getCityCode();
                }

                default ->
                    throw new IllegalStateException(
                        "Unsupported level type: " + level
                    );
            }

            hierarchyLevelNumber= level.numericForm();
        }
        return new ReportScopeV2(
            specification,
            effectiveParentCode,
            org.getId(),
            hierarchyLevelNumber
        );


    }

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
