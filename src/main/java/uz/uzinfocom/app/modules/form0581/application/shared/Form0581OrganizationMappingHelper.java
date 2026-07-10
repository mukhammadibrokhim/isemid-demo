package uz.uzinfocom.app.modules.form0581.application.shared;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Form0581OrganizationMappingHelper {

    private final Form0581OrganizationIdResolver organizationIdResolver;
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
}
