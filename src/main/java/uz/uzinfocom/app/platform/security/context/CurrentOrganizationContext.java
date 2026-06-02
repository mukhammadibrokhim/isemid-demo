package uz.uzinfocom.app.platform.security.context;

import uz.uzinfocom.app.platform.iam.domain.Organization;

import java.util.Optional;
import java.util.UUID;

public final class CurrentOrganizationContext {

    private static final ThreadLocal<Organization> ORGANIZATION = new ThreadLocal<>();

    private CurrentOrganizationContext() {
    }

    public static void set(Organization organization) {
        ORGANIZATION.set(organization);
    }

    public static Optional<Organization> getOptional() {
        return Optional.ofNullable(ORGANIZATION.get());
    }

    public static Organization require() {
        return getOptional()
                .orElseThrow(() -> new IllegalStateException("Current organization is not selected"));
    }

    public static Optional<UUID> getOrganizationUuidOptional() {
        return getOptional().map(Organization::getUuid);
    }

    public static UUID getRequiredOrganizationUuid() {
        return require().getUuid();
    }

    public static void clear() {
        ORGANIZATION.remove();
    }
}