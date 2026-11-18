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
 *   <li>Authority constants (e.g. {@link #REPORTS_READ}) name the authority string.</li>
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

    // --- REPORTS ---------------------------------------------------------------------------
    public static final String REPORTS_READ = "PERMISSION_REPORTS_READ";
    public static final String REPORTS_CREATE = "PERMISSION_REPORTS_CREATE";
    public static final String REPORTS_UPDATE = "PERMISSION_REPORTS_UPDATE";
    public static final String REPORTS_DELETE = "PERMISSION_REPORTS_DELETE";

    // --- ATTACH_ACT ------------------------------------------------------------------------
    public static final String ATTACH_ACT_READ = "PERMISSION_ATTACH_ACT_READ";
    public static final String ATTACH_ACT_UPDATE = "PERMISSION_ATTACH_ACT_UPDATE";
    public static final String ATTACH_ACT_DELETE = "PERMISSION_ATTACH_ACT_DELETE";
    public static final String ATTACH_ACT_ASSIGN = "PERMISSION_ATTACH_ACT_ASSIGN";
    public static final String ATTACH_ACT_VIEW_ALL = "PERMISSION_ATTACH_ACT_VIEW_ALL";

    // --- ATTACHED_CARDS --------------------------------------------------------------------
    public static final String ATTACHED_CARDS_VIEW_ALL = "PERMISSION_ATTACHED_CARDS_VIEW_ALL";

    // --- SPEECH_TO_TEXT --------------------------------------------------------------------
    public static final String SPEECH_TO_TEXT_CREATE = "PERMISSION_SPEECH_TO_TEXT_CREATE";

    // --- @PreAuthorize expressions ---------------------------------------------------------
    // Plain literal concatenation (no method call) so these stay compile-time constants.
    private static final String AUTHENTICATED_AND = "isAuthenticated() and hasAuthority('";
    private static final String AUTHORITY_END = "')";

    public static final String HAS_REPORTS_READ = AUTHENTICATED_AND + REPORTS_READ + AUTHORITY_END;
    public static final String HAS_REPORTS_CREATE = AUTHENTICATED_AND + REPORTS_CREATE + AUTHORITY_END;
    public static final String HAS_REPORTS_UPDATE = AUTHENTICATED_AND + REPORTS_UPDATE + AUTHORITY_END;
    public static final String HAS_REPORTS_DELETE = AUTHENTICATED_AND + REPORTS_DELETE + AUTHORITY_END;

    public static final String HAS_ATTACH_ACT_READ = AUTHENTICATED_AND + ATTACH_ACT_READ + AUTHORITY_END;
    public static final String HAS_ATTACH_ACT_UPDATE = AUTHENTICATED_AND + ATTACH_ACT_UPDATE + AUTHORITY_END;
    public static final String HAS_ATTACH_ACT_DELETE = AUTHENTICATED_AND + ATTACH_ACT_DELETE + AUTHORITY_END;
    public static final String HAS_ATTACH_ACT_ASSIGN = AUTHENTICATED_AND + ATTACH_ACT_ASSIGN + AUTHORITY_END;
    public static final String HAS_ATTACH_ACT_VIEW_ALL = AUTHENTICATED_AND + ATTACH_ACT_VIEW_ALL + AUTHORITY_END;

    public static final String HAS_ATTACHED_CARDS_VIEW_ALL = AUTHENTICATED_AND + ATTACHED_CARDS_VIEW_ALL + AUTHORITY_END;

    public static final String HAS_SPEECH_TO_TEXT_CREATE = AUTHENTICATED_AND + SPEECH_TO_TEXT_CREATE + AUTHORITY_END;

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
