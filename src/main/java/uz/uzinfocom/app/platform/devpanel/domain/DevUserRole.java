package uz.uzinfocom.app.platform.devpanel.domain;

/**
 * Access tier for a {@link DevUser} account, enforced via {@code @PreAuthorize}
 * on the {@code /v1/dev/**} controllers (see {@code DevUserPrincipal} for the
 * granted-authority mapping).
 *
 * <ul>
 *     <li>{@link #SUPER_ADMIN} - full CRUD everywhere, including delete, and
 *     the only tier allowed to manage other {@code DevUser} accounts.</li>
 *     <li>{@link #ADMIN} - full CRUD except delete (and except revoking a
 *     dev-panel account, which is irreversible via the API).</li>
 *     <li>{@link #USER} - read-only.</li>
 * </ul>
 */
public enum DevUserRole {
    SUPER_ADMIN,
    ADMIN,
    USER
}
