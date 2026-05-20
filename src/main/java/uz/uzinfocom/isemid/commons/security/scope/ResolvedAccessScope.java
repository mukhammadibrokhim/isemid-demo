package uz.uzinfocom.isemid.commons.security.scope;

import java.util.UUID;

/**
 * Encapsulates the resolved data-access scope for the current organization.
 *
 * <p>This object is produced by {@link OrganizationAccessScopeResolver} and then consumed by
 * {@link GlobalAccessScope} to build JPA predicates.
 *
 * <p>The goal of this record is to separate:
 * <ul>
 *     <li>business rules of access resolution</li>
 *     <li>technical query building logic</li>
 * </ul>
 *
 * <p>Supported access strategies:
 * <ul>
 *     <li>{@code ALL} - full access without organization filtering</li>
 *     <li>{@code STATE_NON_REPUBLICAN} - access to all organizations of the same state,
 *         excluding republican-level organizations</li>
 *     <li>{@code DIRECT_ORGANIZATION} - access only to the current organization</li>
 *     <li>{@code NONE} - no access</li>
 * </ul>
 *
 * <p>This design keeps service-layer code clean and prevents duplication of organization
 * visibility rules across specifications.
 */
public record ResolvedAccessScope(
        Mode mode,
        UUID organizationUuid,
        String stateCode
) {

    /**
     * Supported scope resolution modes.
     */
    public enum Mode {

        /**
         * Full access to all records.
         * Typically used for republican-level Sanepid organizations.
         */
        ALL,

        /**
         * Access to organizations within the same state, excluding republican-level nodes.
         * Typically used for regional heads and the special Tashkent city head case.
         */
        STATE_NON_REPUBLICAN,

        /**
         * Access only to records directly related to a single organization UUID.
         * Used for ordinary district/city/local organizations and all non-Sanepid organizations.
         */
        DIRECT_ORGANIZATION,

        /**
         * Explicitly denies access.
         */
        NONE
    }

    /**
     * Creates a scope with unrestricted access.
     */
    public static ResolvedAccessScope all() {
        return new ResolvedAccessScope(Mode.ALL, null, null);
    }

    /**
     * Creates a scope with no access.
     */
    public static ResolvedAccessScope none() {
        return new ResolvedAccessScope(Mode.NONE, null, null);
    }

    /**
     * Creates a scope restricted to a single organization.
     *
     * @param organizationUuid UUID of the organization that should be visible
     */
    public static ResolvedAccessScope directOrganization(UUID organizationUuid) {
        return new ResolvedAccessScope(Mode.DIRECT_ORGANIZATION, organizationUuid, null);
    }

    /**
     * Creates a scope for all non-republican organizations inside the given state.
     *
     * @param stateCode state classifier code, for example {@code UZ-AN}, {@code UZ-SA}, {@code UZ-TK}
     */
    public static ResolvedAccessScope stateNonRepublican(String stateCode) {
        return new ResolvedAccessScope(Mode.STATE_NON_REPUBLICAN, null, stateCode);
    }

    /**
     * Defensive validation used before query predicate building.
     *
     * <p>If the scope does not contain enough data for its mode, it is treated as empty and should
     * not be used to build permissive predicates.
     */
    public boolean isEmpty() {
        return mode == null
                || (mode == Mode.DIRECT_ORGANIZATION && organizationUuid == null)
                || (mode == Mode.STATE_NON_REPUBLICAN && isBlank(stateCode));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}