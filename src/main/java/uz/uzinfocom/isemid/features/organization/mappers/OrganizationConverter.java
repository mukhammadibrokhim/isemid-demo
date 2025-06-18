package uz.uzinfocom.isemid.features.organization.mappers;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponseDto;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;

@Component
public class OrganizationConverter {
    private final OrganizationService organizationService;
    private final CatalogService catalogService;

    public OrganizationConverter(OrganizationService organizationService, CatalogService catalogService) {
        this.organizationService = organizationService;
        this.catalogService = catalogService;
    }

    @Named("toOrgMiniResponse")
    public OrgMiniResponse toOrgMiniResponse(Long organizationId) {
        Organization organization = organizationService.findById(organizationId).orElse(null);
        if (organization == null) {
            return null;
        }
        Catalog catalog = catalogService.findByCode(organization.getLevelCode());
        return OrgMiniResponse.builder()
                .id(organization.getId())
                .uuid(organization.getUuid())
                .name(organization.getName())
                .level(catalog != null ? CatalogResponseDto.builder()
                        .id(catalog.getId())
                        .code(catalog.getCode())
                        .parentCode(catalog.getParentCode())
                        .nameUz(catalog.getNameUz())
                        .nameRu(catalog.getNameRu())
                        .nameUzCyril(catalog.getNameUzCyril())
                        .nameKaa(catalog.getNameKaa())
                        .build() : null)
                .build();
    }
}
