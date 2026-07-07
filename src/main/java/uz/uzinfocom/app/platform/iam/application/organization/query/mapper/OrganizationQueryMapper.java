package uz.uzinfocom.app.platform.iam.application.organization.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationDetailResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationLookupResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationTableResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.projection.OrganizationTableProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.reference.application.lookup.mapper.ReferenceMappingHelper;

@Mapper(
        componentModel = "spring",
        uses = {ReferenceMappingHelper.class, OrganizationMapperHelper.class}
)
public interface OrganizationQueryMapper {
    @Mapping(target = "name", source = ".", qualifiedByName = "localizedOrganizationTableName")
    @Mapping(target = "regionName", source = "regionCode", qualifiedByName = "regionName")
    @Mapping(target = "districtName", source = "districtCode", qualifiedByName = "districtName")
    OrganizationTableResponse toTableResponse(OrganizationTableProjection organizationTableProjection);

    @Mapping(target = "name", source = "organization", qualifiedByName = "localizedOrganizationName")
    @Mapping(target = "audit", source = "audit")
    OrganizationDetailResponse toDetailedResponse(Organization organization, AuditResponse audit);

    @Mapping(target = "name", source = ".", qualifiedByName = "localizedOrganizationName")
    OrganizationLookupResponse toLookupResponse(Organization organization);
}
