package uz.uzinfocom.app.modules.iam.application.organization.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationShortResponse;
import uz.uzinfocom.app.modules.iam.application.organization.query.projection.OrganizationTableProjection;
import uz.uzinfocom.app.modules.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;

@Component
@RequiredArgsConstructor
public class OrganizationMapperHelper {

    private final OrganizationNameResolver organizationNameResolver;

    @Named("toOrgMiniResponse")
    public OrganizationShortResponse toOrgMiniResponse(Organization organization) {
        return toResponse(organization);
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

}
