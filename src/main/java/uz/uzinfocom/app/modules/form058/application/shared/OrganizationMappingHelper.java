package uz.uzinfocom.app.modules.form058.application.shared;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationMappingHelper {

    private final OrganizationIdResolver organizationIdResolver;

    @Named("activeOrganizationId")
    public Long activeOrganizationId(UUID uuid) {
        return organizationIdResolver.resolveActiveId(uuid);
    }
}
