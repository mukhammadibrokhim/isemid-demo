package uz.uzinfocom.app.modules.iam.application.shared.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.application.shared.exception.OrganizationResolutionException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationMappingHelper {

    private final OrganizationIdResolver organizationIdResolver;
    private final OrganizationNameResolver organizationNameResolver;

    @Named("activeOrganizationId")
    public Long activeOrganizationId(UUID uuid) {
        return organizationIdResolver.resolveActiveId(uuid);
    }

    @Named("nullableActiveOrganizationId")
    public Long nullableActiveOrganizationId(UUID uuid) {
        return uuid == null ? null : organizationIdResolver.resolveActiveId(uuid);
    }

    @Named("activeOrganizationNameById")
    public String activeOrganizationNameById(Long id) {
        if (id == null) {
            return null;
        }

        return organizationNameResolver.resolve(organizationIdResolver.resolveActiveNameFields(id));
    }

    /**
     * Read-side variant for display-only name columns: an id that no longer
     * resolves to an active organization (deactivated, or a legacy sentinel
     * like 0) yields {@code null} instead of failing the whole response.
     */
    public String activeOrganizationNameByIdOrNull(Long id) {
        if (id == null) {
            return null;
        }

        try {
            return organizationNameResolver.resolve(organizationIdResolver.resolveActiveNameFields(id));
        } catch (OrganizationResolutionException e) {
            return null;
        }
    }
}
