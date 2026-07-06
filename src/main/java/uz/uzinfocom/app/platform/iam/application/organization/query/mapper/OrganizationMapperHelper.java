package uz.uzinfocom.app.platform.iam.application.organization.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.shared.cache.OrganizationCacheConfig;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationMapperHelper {

    private final OrganizationRepository organizationRepository;

    @Named("toOrgMiniResponse")
    public OrganizationShortResponse toOrgMiniResponse(Long id) {
        if (id == null) {
            return null;
        }
        return organizationRepository.findById(id).map(this::toResponse).orElse(null);
    }

    @Named("toOrgMiniResponse")
    public OrganizationShortResponse toOrgMiniResponse(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return organizationRepository.findByUuid(uuid).map(this::toResponse).orElse(null);
    }

    @Named("uuidToLongId")
    public Long uuidToLongId(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return organizationRepository.findByUuid(uuid)
                .map(Organization::getId)
                .orElse(null);
    }

    public OrganizationShortResponse toResponse(Organization organization) {
        if (organization == null) {
            return null;
        }
        return new OrganizationShortResponse(
                organization.getId(),
                organization.getUuid(),
                organization.getName()
        );
    }

    @Named("nullableOrganizationUuidToId")
    @Cacheable(
            cacheNames = OrganizationCacheConfig.ORGANIZATION_ID_BY_UUID,
            key = "#organizationUuid",
            condition = "#organizationUuid != null",
            sync = true,
            cacheManager = "securityCacheManager"
    )
    public Long nullableOrganizationUuidToId(UUID organizationUuid) {
        if (organizationUuid == null) {
            return null;
        }

        return organizationRepository.findActiveIdByUuid(organizationUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active organization was not found by UUID: "
                                + organizationUuid
                ));
    }
}
