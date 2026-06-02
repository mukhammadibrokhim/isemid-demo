package uz.uzinfocom.app.platform.security.principal;

import java.util.UUID;

public record PrincipalOrganization(
        Long id,
        UUID uuid,
        String name
) {
}