package uz.uzinfocom.app.platform.iam.application.organization.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.projection.OrganizationTableProjection;
import uz.uzinfocom.app.platform.iam.application.shared.cache.OrganizationCacheConfig;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationMapperHelper {

    private final OrganizationRepository organizationRepository;
    private final OrganizationNameResolver organizationNameResolver;

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
                organizationNameResolver.resolve(organization)
        );
    }

    @Named("localizedOrganizationName")
    public String localizedOrganizationName(Organization organization) {
        return organizationNameResolver.resolve(organization);
    }

    @Named("localizedOrganizationTableName")
    public String localizedOrganizationTableName(OrganizationTableProjection projection) {
        if (projection == null) {
            return null;
        }

        return organizationNameResolver.resolve(new OrganizationLocalizedName(
                projection.getName(),
                projection.getNameUz(),
                projection.getNameUzCyril(),
                projection.getNameRu(),
                projection.getNameKaa()
        ));
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
