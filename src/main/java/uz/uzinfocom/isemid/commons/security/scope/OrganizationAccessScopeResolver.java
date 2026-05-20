package uz.uzinfocom.isemid.commons.security.scope;

import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;

/**
 * Resolves the effective access scope for the current organization.
 *
 * <p>This class contains the business rules that define who can see whose data.
 * It is the single source of truth for organization hierarchy visibility.
 *
 * <p>Current rules:
 * <ul>
 *     <li>Republican Sanepid organization can see everything</li>
 *     <li>Regional Sanepid organization can see all non-republican organizations in its state</li>
 *     <li>Tashkent city head Sanepid organization is stored as {@code URBAN}, but behaves like a
 *         regional head and can see all non-republican organizations in {@code UZ-TK}</li>
 *     <li>All other Sanepid organizations can see only their own records</li>
 *     <li>All non-Sanepid organizations can see only their own records</li>
 * </ul>
 *
 * <p>This approach is based on real organization data, where:
 * <ul>
 *     <li>ordinary region heads are typically {@code REGIONAL}</li>
 *     <li>Tashkent city head is a special case stored as {@code URBAN}</li>
 *     <li>many district-level units are stored as {@code AREA}</li>
 * </ul>
 */
@Component
public class OrganizationAccessScopeResolver {

    /**
     * Classifier code of Tashkent city state.
     */
    private static final String TASHKENT_CITY_STATE_CODE = "UZ-TK";

    /**
     * Resolves the access scope for the current organization.
     *
     * @param currentOrganization currently selected organization of the authenticated user
     * @return resolved scope used later in JPA specifications
     */
    public ResolvedAccessScope resolve(Organization currentOrganization) {
        // No organization -> no access.
        if (currentOrganization == null) {
            return ResolvedAccessScope.none();
        }

        // Non-Sanepid organizations must never inherit hierarchical access.
        if (!isSanepidService(currentOrganization)) {
            return ResolvedAccessScope.directOrganization(currentOrganization.getUuid());
        }

        LevelType levelType = currentOrganization.getLevelType();

        // Invalid or incomplete organization metadata falls back to the safest possible access.
        if (levelType == null) {
            return ResolvedAccessScope.directOrganization(currentOrganization.getUuid());
        }

        // Republican Sanepid has full access.
        if (levelType == LevelType.REPUBLICAN) {
            return ResolvedAccessScope.all();
        }

        // Tashkent city head is a special case:
        // although stored as URBAN, it acts like a top state-level head.
        if (isTashkentCityHead(currentOrganization)) {
            return ResolvedAccessScope.stateNonRepublican(currentOrganization.getStateCode());
        }

        // Ordinary regional Sanepid heads see all lower-level organizations inside their state.
        if (levelType == LevelType.REGIONAL) {
            return ResolvedAccessScope.stateNonRepublican(currentOrganization.getStateCode());
        }

        // All remaining organizations are restricted to themselves:
        // AREA, URBAN (ordinary city), DISTRICT, INTERDISTRICT, NOT_DEFINED, etc.
        return ResolvedAccessScope.directOrganization(currentOrganization.getUuid());
    }

    /**
     * Returns {@code true} when the organization belongs to Sanepid hierarchy.
     */
    private boolean isSanepidService(Organization organization) {
        return organization.getMedicalType() == MedicalType.SANEPID_SERVICE;
    }

    /**
     * Detects the special Tashkent city head node.
     *
     * <p>Important:
     * this is not every {@code URBAN} organization inside Tashkent.
     * This condition is intentionally narrow and is used only for the top Tashkent city head.
     *
     * <p>If later the data model becomes more explicit (for example, a dedicated head flag or
     * parent-child structure is introduced), this method should be simplified to use that source
     * of truth instead of inferred classification.
     */
    private boolean isTashkentCityHead(Organization organization) {
        return isSanepidService(organization)
                && organization.getLevelType() == LevelType.URBAN
                && TASHKENT_CITY_STATE_CODE.equals(organization.getStateCode());
    }
}