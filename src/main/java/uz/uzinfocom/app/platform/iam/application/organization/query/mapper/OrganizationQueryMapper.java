package uz.uzinfocom.app.platform.iam.application.organization.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationDetailResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationTableResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.projection.OrganizationTableProjection;
import uz.uzinfocom.app.platform.iam.domain.Organization;

@Mapper(componentModel = "spring")
public interface OrganizationQueryMapper {
    OrganizationTableResponse toTableResponse(OrganizationTableProjection organizationTableProjection);

    OrganizationDetailResponse toDetailedResponse(Organization organization);
}
