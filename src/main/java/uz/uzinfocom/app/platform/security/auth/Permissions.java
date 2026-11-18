package uz.uzinfocom.app.platform.security.auth;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Single catalog of the {@code PERMISSION_<SUBJECT>_<ACTION>} authorities that controllers
 * check with {@code @PreAuthorize}.
 *
 * <ul>
 *   <li>Authority constants (e.g. {@link #REPORT_READ}) name the authority string.</li>
 *   <li>{@code HAS_*} constants are ready-made SpEL expressions for {@code @PreAuthorize}.</li>
 * </ul>
 *
 * Never write an authority literal inside an annotation: add it here, so the name the code
 * expects lives in one place. {@code PermissionCatalogValidator} checks at startup that every
 * authority in {@link #required()} can actually be granted from the {@code permission} /
 * {@code action} tables, and {@code PermissionsTest} keeps the constants consistent.
 *
 * <p>A role holding {@code MANAGE} on a subject is expanded to every action in SQL
 * ({@code RoleRepository#findPermissionAuthorityNamesByRoleIds}), so guards only ever name the
 * concrete action.
 */
public final class Permissions {

    private Permissions() {
    }

    // Subject/action names follow docs/permission-naming-mapping.md: singular resource subject +
    // verb action. REPORT_ENTRY_* is "REPORT_ENTRY" + READ/CREATE/UPDATE/DELETE.

    // --- REPORT: statistical forms, forecast, air-health, map, air quality ----------------
    public static final String REPORT_READ = "PERMISSION_REPORT_READ";
    public static final String REPORT_EXPORT = "PERMISSION_REPORT_EXPORT";

    // --- REPORT_ENTRY: manually entered reports (form 2 manual, 31, 32, 7) ----------------
    public static final String REPORT_ENTRY_READ = "PERMISSION_REPORT_ENTRY_READ";
    public static final String REPORT_ENTRY_CREATE = "PERMISSION_REPORT_ENTRY_CREATE";
    public static final String REPORT_ENTRY_UPDATE = "PERMISSION_REPORT_ENTRY_UPDATE";
    public static final String REPORT_ENTRY_DELETE = "PERMISSION_REPORT_ENTRY_DELETE";

    // --- ANALYTIC_REPORT: the caller's own report builder ---------------------------------
    public static final String ANALYTIC_REPORT_READ = "PERMISSION_ANALYTIC_REPORT_READ";
    public static final String ANALYTIC_REPORT_CREATE = "PERMISSION_ANALYTIC_REPORT_CREATE";
    public static final String ANALYTIC_REPORT_UPDATE = "PERMISSION_ANALYTIC_REPORT_UPDATE";
    public static final String ANALYTIC_REPORT_DELETE = "PERMISSION_ANALYTIC_REPORT_DELETE";
    public static final String ANALYTIC_REPORT_EXPORT = "PERMISSION_ANALYTIC_REPORT_EXPORT";

    // --- ACT -------------------------------------------------------------------------------
    public static final String ACT_READ = "PERMISSION_ACT_READ";
    public static final String ACT_UPDATE = "PERMISSION_ACT_UPDATE";
    public static final String ACT_DELETE = "PERMISSION_ACT_DELETE";
    public static final String ACT_ASSIGN = "PERMISSION_ACT_ASSIGN";
    public static final String ACT_VIEW_ALL = "PERMISSION_ACT_VIEW_ALL";

    // --- CARD ------------------------------------------------------------------------------
    public static final String CARD_VIEW_ALL = "PERMISSION_CARD_VIEW_ALL";
    public static final String FORM058_CREATE = "PERMISSION_FORM058_CREATE";
    public static final String FORM058_UPDATE = "PERMISSION_FORM058_UPDATE";
    public static final String FORM058_DELETE = "PERMISSION_FORM058_DELETE";
    public static final String FORM058_ACCEPT = "PERMISSION_FORM058_ACCEPT";
    public static final String FORM058_APPROVE = "PERMISSION_FORM058_APPROVE";
    public static final String FORM058_CANCEL = "PERMISSION_FORM058_CANCEL";
    public static final String FORM058_REJECT = "PERMISSION_FORM058_REJECT";
    public static final String FORM058_READ_SENT = "PERMISSION_FORM058_READ_SENT";
    public static final String FORM058_READ_RECEIVED = "PERMISSION_FORM058_READ_RECEIVED";
    public static final String FORM0581_CREATE = "PERMISSION_FORM0581_CREATE";
    public static final String FORM0581_UPDATE = "PERMISSION_FORM0581_UPDATE";
    public static final String FORM0581_DELETE = "PERMISSION_FORM0581_DELETE";
    public static final String FORM0581_ACCEPT = "PERMISSION_FORM0581_ACCEPT";
    public static final String FORM0581_APPROVE = "PERMISSION_FORM0581_APPROVE";
    public static final String FORM0581_CANCEL = "PERMISSION_FORM0581_CANCEL";
    public static final String FORM0581_REJECT = "PERMISSION_FORM0581_REJECT";
    public static final String FORM0581_READ_SENT = "PERMISSION_FORM0581_READ_SENT";
    public static final String FORM0581_READ_RECEIVED = "PERMISSION_FORM0581_READ_RECEIVED";
    public static final String FORM129_CREATE = "PERMISSION_FORM129_CREATE";
    public static final String FORM129_ACCEPT = "PERMISSION_FORM129_ACCEPT";
    public static final String FORM129_REJECT = "PERMISSION_FORM129_REJECT";
    public static final String FORM129_READ_SENT = "PERMISSION_FORM129_READ_SENT";
    public static final String FORM129_READ_RECEIVED = "PERMISSION_FORM129_READ_RECEIVED";
    public static final String CARD_READ = "PERMISSION_CARD_READ";
    public static final String CARD_UPDATE = "PERMISSION_CARD_UPDATE";
    public static final String CARD_DELETE = "PERMISSION_CARD_DELETE";
    public static final String CARD_ACCEPT = "PERMISSION_CARD_ACCEPT";
    public static final String CARD_REJECT = "PERMISSION_CARD_REJECT";
    public static final String CARD_COMPLETE = "PERMISSION_CARD_COMPLETE";
    public static final String CARD_APPROVE = "PERMISSION_CARD_APPROVE";
    public static final String CARD_RETURN = "PERMISSION_CARD_RETURN";
    public static final String CARD_ASSIGN = "PERMISSION_CARD_ASSIGN";

    // --- SPEECH_TO_TEXT --------------------------------------------------------------------
    public static final String SPEECH_TO_TEXT_CREATE = "PERMISSION_SPEECH_TO_TEXT_CREATE";

    // --- @PreAuthorize expressions ---------------------------------------------------------
    // Plain literal concatenation (no method call) so these stay compile-time constants.
    private static final String AUTHENTICATED_AND = "isAuthenticated() and hasAuthority('";
    private static final String AUTHORITY_END = "')";

    public static final String HAS_REPORT_READ = AUTHENTICATED_AND + REPORT_READ + AUTHORITY_END;
    public static final String HAS_REPORT_EXPORT = AUTHENTICATED_AND + REPORT_EXPORT + AUTHORITY_END;
    public static final String HAS_REPORT_ENTRY_READ = AUTHENTICATED_AND + REPORT_ENTRY_READ + AUTHORITY_END;
    public static final String HAS_REPORT_ENTRY_CREATE = AUTHENTICATED_AND + REPORT_ENTRY_CREATE + AUTHORITY_END;
    public static final String HAS_REPORT_ENTRY_UPDATE = AUTHENTICATED_AND + REPORT_ENTRY_UPDATE + AUTHORITY_END;
    public static final String HAS_REPORT_ENTRY_DELETE = AUTHENTICATED_AND + REPORT_ENTRY_DELETE + AUTHORITY_END;
    public static final String HAS_ANALYTIC_REPORT_READ = AUTHENTICATED_AND + ANALYTIC_REPORT_READ + AUTHORITY_END;
    public static final String HAS_ANALYTIC_REPORT_CREATE = AUTHENTICATED_AND + ANALYTIC_REPORT_CREATE + AUTHORITY_END;
    public static final String HAS_ANALYTIC_REPORT_UPDATE = AUTHENTICATED_AND + ANALYTIC_REPORT_UPDATE + AUTHORITY_END;
    public static final String HAS_ANALYTIC_REPORT_DELETE = AUTHENTICATED_AND + ANALYTIC_REPORT_DELETE + AUTHORITY_END;
    public static final String HAS_ANALYTIC_REPORT_EXPORT = AUTHENTICATED_AND + ANALYTIC_REPORT_EXPORT + AUTHORITY_END;
    public static final String HAS_ACT_READ = AUTHENTICATED_AND + ACT_READ + AUTHORITY_END;
    public static final String HAS_ACT_UPDATE = AUTHENTICATED_AND + ACT_UPDATE + AUTHORITY_END;
    public static final String HAS_ACT_DELETE = AUTHENTICATED_AND + ACT_DELETE + AUTHORITY_END;
    public static final String HAS_ACT_ASSIGN = AUTHENTICATED_AND + ACT_ASSIGN + AUTHORITY_END;
    public static final String HAS_ACT_VIEW_ALL = AUTHENTICATED_AND + ACT_VIEW_ALL + AUTHORITY_END;
    public static final String HAS_CARD_VIEW_ALL = AUTHENTICATED_AND + CARD_VIEW_ALL + AUTHORITY_END;
    public static final String HAS_FORM058_CREATE = AUTHENTICATED_AND + FORM058_CREATE + AUTHORITY_END;
    public static final String HAS_FORM058_UPDATE = AUTHENTICATED_AND + FORM058_UPDATE + AUTHORITY_END;
    public static final String HAS_FORM058_DELETE = AUTHENTICATED_AND + FORM058_DELETE + AUTHORITY_END;
    public static final String HAS_FORM058_ACCEPT = AUTHENTICATED_AND + FORM058_ACCEPT + AUTHORITY_END;
    public static final String HAS_FORM058_APPROVE = AUTHENTICATED_AND + FORM058_APPROVE + AUTHORITY_END;
    public static final String HAS_FORM058_CANCEL = AUTHENTICATED_AND + FORM058_CANCEL + AUTHORITY_END;
    public static final String HAS_FORM058_REJECT = AUTHENTICATED_AND + FORM058_REJECT + AUTHORITY_END;
    public static final String HAS_FORM058_READ_SENT = AUTHENTICATED_AND + FORM058_READ_SENT + AUTHORITY_END;
    public static final String HAS_FORM058_READ_RECEIVED = AUTHENTICATED_AND + FORM058_READ_RECEIVED + AUTHORITY_END;
    public static final String HAS_FORM0581_CREATE = AUTHENTICATED_AND + FORM0581_CREATE + AUTHORITY_END;
    public static final String HAS_FORM0581_UPDATE = AUTHENTICATED_AND + FORM0581_UPDATE + AUTHORITY_END;
    public static final String HAS_FORM0581_DELETE = AUTHENTICATED_AND + FORM0581_DELETE + AUTHORITY_END;
    public static final String HAS_FORM0581_ACCEPT = AUTHENTICATED_AND + FORM0581_ACCEPT + AUTHORITY_END;
    public static final String HAS_FORM0581_APPROVE = AUTHENTICATED_AND + FORM0581_APPROVE + AUTHORITY_END;
    public static final String HAS_FORM0581_CANCEL = AUTHENTICATED_AND + FORM0581_CANCEL + AUTHORITY_END;
    public static final String HAS_FORM0581_REJECT = AUTHENTICATED_AND + FORM0581_REJECT + AUTHORITY_END;
    public static final String HAS_FORM0581_READ_SENT = AUTHENTICATED_AND + FORM0581_READ_SENT + AUTHORITY_END;
    public static final String HAS_FORM0581_READ_RECEIVED = AUTHENTICATED_AND + FORM0581_READ_RECEIVED + AUTHORITY_END;
    public static final String HAS_FORM129_CREATE = AUTHENTICATED_AND + FORM129_CREATE + AUTHORITY_END;
    public static final String HAS_FORM129_ACCEPT = AUTHENTICATED_AND + FORM129_ACCEPT + AUTHORITY_END;
    public static final String HAS_FORM129_REJECT = AUTHENTICATED_AND + FORM129_REJECT + AUTHORITY_END;
    public static final String HAS_FORM129_READ_SENT = AUTHENTICATED_AND + FORM129_READ_SENT + AUTHORITY_END;
    public static final String HAS_FORM129_READ_RECEIVED = AUTHENTICATED_AND + FORM129_READ_RECEIVED + AUTHORITY_END;
    public static final String HAS_CARD_READ = AUTHENTICATED_AND + CARD_READ + AUTHORITY_END;
    public static final String HAS_CARD_UPDATE = AUTHENTICATED_AND + CARD_UPDATE + AUTHORITY_END;
    public static final String HAS_CARD_DELETE = AUTHENTICATED_AND + CARD_DELETE + AUTHORITY_END;
    public static final String HAS_CARD_ACCEPT = AUTHENTICATED_AND + CARD_ACCEPT + AUTHORITY_END;
    public static final String HAS_CARD_REJECT = AUTHENTICATED_AND + CARD_REJECT + AUTHORITY_END;
    public static final String HAS_CARD_COMPLETE = AUTHENTICATED_AND + CARD_COMPLETE + AUTHORITY_END;
    public static final String HAS_CARD_APPROVE = AUTHENTICATED_AND + CARD_APPROVE + AUTHORITY_END;
    public static final String HAS_CARD_RETURN = AUTHENTICATED_AND + CARD_RETURN + AUTHORITY_END;
    public static final String HAS_CARD_ASSIGN = AUTHENTICATED_AND + CARD_ASSIGN + AUTHORITY_END;
    public static final String HAS_SPEECH_TO_TEXT_CREATE = AUTHENTICATED_AND + SPEECH_TO_TEXT_CREATE + AUTHORITY_END;

    /** List/export of FORM058: READ_SENT for OUTGOING, READ_RECEIVED for INCOMING, both for ALL. */
    public static final String GUARD_FORM058_LIST = "@formPermissionGuard.canList('FORM058', #filter.direction().name())";
    /** Single-form read / stats of FORM058: either side's read permission. */
    public static final String GUARD_FORM058_READ_ANY = "@formPermissionGuard.canReadAny('FORM058')";
    /** List/export of FORM0581: READ_SENT for OUTGOING, READ_RECEIVED for INCOMING, both for ALL. */
    public static final String GUARD_FORM0581_LIST = "@formPermissionGuard.canList('FORM0581', #filter.direction().name())";
    /** Single-form read / stats of FORM0581: either side's read permission. */
    public static final String GUARD_FORM0581_READ_ANY = "@formPermissionGuard.canReadAny('FORM0581')";
    /** List/export of FORM129: READ_SENT for OUTGOING, READ_RECEIVED for INCOMING, both for ALL. */
    public static final String GUARD_FORM129_LIST = "@formPermissionGuard.canList('FORM129', #filter.direction().name())";
    /** Single-form read / stats of FORM129: either side's read permission. */
    public static final String GUARD_FORM129_READ_ANY = "@formPermissionGuard.canReadAny('FORM129')";
    /** Cancel endpoint of FORM058: CANCEL (sender) or REJECT (receiver); the side is checked in the validator. */
    public static final String GUARD_FORM058_CANCEL_OR_REJECT = "@formPermissionGuard.canCancelOrReject('FORM058')";
    /** Cancel endpoint of FORM0581: CANCEL (sender) or REJECT (receiver); the side is checked in the validator. */
    public static final String GUARD_FORM0581_CANCEL_OR_REJECT = "@formPermissionGuard.canCancelOrReject('FORM0581')";

    /**
     * Every authority constant declared above (values starting with {@code PERMISSION_}),
     * collected by reflection so the list can never drift from the constants.
     */
    public static Set<String> required() {
        return Arrays.stream(Permissions.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()) && f.getType() == String.class)
                .map(Permissions::value)
                .filter(v -> v.startsWith("PERMISSION_"))
                .collect(java.util.stream.Collectors.toCollection(TreeSet::new));
    }

    private static String value(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
