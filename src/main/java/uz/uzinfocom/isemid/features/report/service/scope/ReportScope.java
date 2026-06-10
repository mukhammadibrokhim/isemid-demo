package uz.uzinfocom.isemid.features.report.service.scope;

/**
 * Fully resolved inputs for a single location-report query.
 *
 * <p>Produced by {@link ReportScopeResolver} from the requesting organization and the
 * optional {@code parentCode}. Carries everything the SQL needs:</p>
 * <ul>
 *   <li>which {@link ReportDimension} (and therefore catalog type + group column) to use;</li>
 *   <li>the catalog parent that bounds the dimension rows ({@code null} = whole type);</li>
 *   <li>the pre-rendered visibility level fragment (column references only — never user input);</li>
 *   <li>the organization scope parameters bound to that fragment.</li>
 * </ul>
 */
public record ReportScope(
        ReportDimension dimension,
        String dimensionParentCode,
        String visibilityLevelSql,
        boolean isSanepid,
        String orgStateCode,
        String orgCityCode,
        Long currentOrgId
) {}
