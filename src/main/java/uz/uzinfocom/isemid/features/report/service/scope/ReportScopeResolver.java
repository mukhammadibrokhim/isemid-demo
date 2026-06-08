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

    private static final String UZ_TK = "UZ-TK";

    private final CatalogService catalogService;
    private final MessageUtil messageUtil;

    public ReportScopeResolver(CatalogService catalogService, MessageUtil messageUtil) {
        this.catalogService = catalogService;
        this.messageUtil = messageUtil;
    }

    public ReportScope resolve(Organization org, String parentCode) {
        LevelType level = org.getLevelType();
        boolean uzTk = UZ_TK.equals(org.getStateCode());

        ReportDimension dimension;
        String dimensionParentCode;

        if (hasText(parentCode)) {
            // Drill-down: depth is decided by the catalog type of parentCode.
            CatalogType parentType = lookupType(parentCode);
            dimension = switch (parentType) {
                case state -> ReportDimension.DISTRICT;       // state -> its districts
                case city -> ReportDimension.NEIGHBOURHOOD;   // city  -> its neighbourhoods
                default -> throw new ApiException(NOT_FOUND,
                        messageUtil.get("error.catalog.notfound", parentCode));
            };
            dimensionParentCode = parentCode;
        } else {
            // Default view for the organization's own level.
            switch (level) {
                case REPUBLICAN -> {
                    dimension = ReportDimension.REGION;
                    dimensionParentCode = null;
                }
                case REGIONAL -> {
                    dimension = ReportDimension.DISTRICT;
                    dimensionParentCode = org.getStateCode();
                }
                case URBAN -> {
                    if (uzTk) {
                        dimension = ReportDimension.DISTRICT;
                        dimensionParentCode = org.getStateCode();
                    } else {
                        dimension = ReportDimension.NEIGHBOURHOOD;
                        dimensionParentCode = org.getCityCode();
                    }
                }
                default /* AREA, DISTRICT, INTERDISTRICT, NOT_DEFINED */ -> {
                    dimension = ReportDimension.NEIGHBOURHOOD;
                    dimensionParentCode = org.getCityCode();
                }
            }
        }

        boolean sanepid = org.getMedicalType() == MedicalType.SANEPID_SERVICE;
        return new ReportScope(
                dimension,
                dimensionParentCode,
                levelSql(level, uzTk),
                sanepid,
                org.getStateCode(),
                org.getCityCode(),
                org.getId());
    }

    /**
     * Classifies {@code parentCode} via the cached catalog. This reads a handful of cached
     * rows for labelling only — it does not load report data.
     */
    private CatalogType lookupType(String parentCode) {
        return catalogService.findAllByParentCode(parentCode).stream()
                .findFirst()
                .map(Catalog::getType)
                .map(CatalogType::valueOf)
                .orElseThrow(() -> new ApiException(NOT_FOUND,
                        messageUtil.get("error.catalog.notfound", parentCode)));
    }

    /**
     * Renders the sanepid visibility level fragment. Output contains column references and
     * bind-parameter names only ({@code :orgStateCode}, {@code :orgCityCode}) — no user input —
     * so it is safe to interpolate into the SQL string.
     */
    private String levelSql(LevelType level, boolean uzTk) {
        return switch (level) {
            case REPUBLICAN -> "TRUE";
            case REGIONAL -> "co.state_code = :orgStateCode";
            case URBAN -> uzTk
                    ? "co.state_code = :orgStateCode"
                    : "co.city_code = :orgCityCode AND co.level_type = 'URBAN'";
            case AREA -> "co.city_code = :orgCityCode AND co.level_type = 'AREA'";
            case DISTRICT, INTERDISTRICT, NOT_DEFINED ->
                    "co.city_code = :orgCityCode AND co.level_type IN ('DISTRICT','INTERDISTRICT','NOT_DEFINED')";
        };
    }
}
