package uz.uzinfocom.isemid.features.patient.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapperHelper;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.patient.dtos.requests.AffiliationRequest;
import uz.uzinfocom.isemid.features.patient.dtos.responses.AffiliationResponse;
import uz.uzinfocom.isemid.features.patient.models.Affiliation;

@Mapper(componentModel = "spring", uses = {CatalogMapperHelper.class, OrganizationMapperHelper.class})
public interface AffiliationMapper {
    Affiliation toEntity(AffiliationRequest request);

    @Mapping(target = "state", source = "stateCode", qualifiedByName = "toCatalog")
    @Mapping(target = "city", source = "cityCode", qualifiedByName = "toCatalog")
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "toOrgMiniResponse")
    AffiliationResponse toResponse(Affiliation entity);
}
