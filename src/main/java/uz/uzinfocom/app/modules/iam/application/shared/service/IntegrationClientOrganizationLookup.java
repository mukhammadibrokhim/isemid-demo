package uz.uzinfocom.app.modules.iam.application.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.integrationclient.application.OrganizationLookup;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IntegrationClientOrganizationLookup implements OrganizationLookup {

    private final OrganizationIdResolver organizationIdResolver;
    private final OrganizationMappingHelper organizationMappingHelper;

    @Override
    public Long resolveActiveId(UUID organizationUuid) {
        return organizationIdResolver.resolveActiveId(organizationUuid);
    }

    @Override
    public String activeOrganizationNameById(Long organizationId) {
        return organizationMappingHelper.activeOrganizationNameById(organizationId);
    }
}
