package uz.uzinfocom.app.integration.api2.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.auth.AuthorityNames;

import java.util.HashSet;
import java.util.Set;

/**
 * Who may pull personal/organization data from API2. No dedicated permission
 * subject - access follows the roles whose work actually needs the lookup,
 * via their existing role-permission grants:
 * <ul>
 *     <li><b>Citizen</b> (PINFL/passport/birth certificate) - creating or editing a
 *     Form 058 ({@code FORM058_SENDER}/{@code FORM058_RECEIVER}) or Form 058-1
 *     ({@code FORM0581}) notification, where the patient block is filled from it;
 *     plus everyone allowed a legal-entity lookup below (cards and acts carry
 *     person identifiers too).</li>
 *     <li><b>Legal entity</b> (TIN) - filling an epi investigation card
 *     ({@code ATTACHED_CARDS} create/update) or an act ({@code ATTACH_ACT} update).</li>
 * </ul>
 * {@code MANAGE} is accepted alongside each action because the role editor
 * collapses a full grant down to the single {@code MANAGE} row. Admins
 * ({@link AdminAccessGuard#isAdmin()}) always pass.
 */
@Component("api2LookupAccessGuard")
@RequiredArgsConstructor
public class Api2LookupAccessGuard {

    private static final String CREATE = "CREATE";
    private static final String UPDATE = "UPDATE";
    private static final String MANAGE = "MANAGE";

    private static final Set<String> CARD_AND_ACT_AUTHORITIES = Set.of(
            AuthorityNames.permission("ATTACHED_CARDS", CREATE),
            AuthorityNames.permission("ATTACHED_CARDS", UPDATE),
            AuthorityNames.permission("ATTACHED_CARDS", MANAGE),
            AuthorityNames.permission("ATTACH_ACT", UPDATE),
            AuthorityNames.permission("ATTACH_ACT", MANAGE)
    );

    private static final Set<String> CITIZEN_AUTHORITIES = union(CARD_AND_ACT_AUTHORITIES, Set.of(
            AuthorityNames.permission("FORM058_SENDER", CREATE),
            AuthorityNames.permission("FORM058_SENDER", UPDATE),
            AuthorityNames.permission("FORM058_SENDER", MANAGE),
            AuthorityNames.permission("FORM058_RECEIVER", UPDATE),
            AuthorityNames.permission("FORM058_RECEIVER", MANAGE),
            AuthorityNames.permission("FORM0581", CREATE),
            AuthorityNames.permission("FORM0581", UPDATE),
            AuthorityNames.permission("FORM0581", MANAGE)
    ));

    private final AdminAccessGuard adminAccessGuard;

    public boolean canLookupCitizen() {
        return hasAnyOrIsAdmin(CITIZEN_AUTHORITIES);
    }

    public boolean canLookupLegalEntity() {
        return hasAnyOrIsAdmin(CARD_AND_ACT_AUTHORITIES);
    }

    private boolean hasAnyOrIsAdmin(Set<String> allowedAuthorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        if (adminAccessGuard.isAdmin()) {
            return true;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(allowedAuthorities::contains);
    }

    private static Set<String> union(Set<String> first, Set<String> second) {
        Set<String> union = new HashSet<>(first);
        union.addAll(second);
        return Set.copyOf(union);
    }
}
