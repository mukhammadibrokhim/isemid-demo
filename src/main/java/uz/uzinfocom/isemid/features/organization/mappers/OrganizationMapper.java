package uz.uzinfocom.isemid.features.organization.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogConverter;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgChildResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrganizationResponse;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.enums.LevelStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = {CatalogConverter.class, UuidConverter.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "id", qualifiedByName = "toUuid")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "tin", expression = "java(extractTin(organizationResponse))")
    @Mapping(target = "email", expression = "java(extractEmail(organizationResponse))")
    @Mapping(target = "phone", expression = "java(extractPhone(organizationResponse))")
    @Mapping(target = "type", expression = "java(extractType(organizationResponse))")
    @Mapping(target = "levelCode", expression = "java(extractLevel(organizationResponse))")
    @Mapping(target = "countryCode", expression = "java(extractCountryCode(organizationResponse))")
    @Mapping(target = "stateCode", expression = "java(extractStateCode(organizationResponse))")
    @Mapping(target = "cityCode", expression = "java(extractCityCode(organizationResponse))")
    @Mapping(target = "state", expression = "java(catalogConverter.toCatalogEntity(extractStateCode(organizationResponse)))")
    @Mapping(target = "city", expression = "java(catalogConverter.toCatalogEntity(extractCityCode(organizationResponse)))")
    @Mapping(target = "district", expression = "java(extractDistrict(organizationResponse))")
    @Mapping(target = "line", expression = "java(extractLine(organizationResponse))")
    @Mapping(target = "serviceAreaCode", expression = "java(extractServiceArea(organizationResponse))")
    Organization toOrganization(OrganizationResponse organizationResponse);

    @Mapping(target = "country", source = "countryCode", qualifiedByName = "toCatalog")
    @Mapping(target = "state", source = "stateCode", qualifiedByName = "toCatalog")
    @Mapping(target = "city", source = "cityCode", qualifiedByName = "toCatalog")
    @Mapping(target = "level", source = "levelCode", qualifiedByName = "toCatalog")
    @Mapping(target = "children", source = "children", qualifiedByName = "mapChildren")
    OrgResponse toResponse(Organization organization);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "level", source = "levelCode", qualifiedByName = "toCatalog")
    @Mapping(target = "parentId", source = "parentId")
    @Mapping(target = "children", source = "children", qualifiedByName = "mapChildren")
    OrgMiniResponse toOrgMiniResponse(Organization organization);

    //ADDITIONAL
    @Named("mapChildren")
    default List<OrgChildResponse> mapChildren(List<Organization> children) {
        if (children == null) {
            return null;
        }
        return children.stream().map(org ->
                OrgChildResponse.builder()
                        .id(org.getId())
                        .uuid(org.getUuid())
                        .name(org.getName())
                        .parentId(org.getParentId())
                        .children(mapChildren(org.getChildren()))
                        .build()
        ).collect(Collectors.toList());
    }
    default String extractEmail(OrganizationResponse organizationResponse) {
        return organizationResponse.getTelecom().stream()
                .filter(t -> "email".equalsIgnoreCase(t.getSystem()))
                .map(OrganizationResponse.Telecom::getValue)
                .findFirst().orElse(null);
    }

    default String extractPhone(OrganizationResponse organizationResponse) {
        return organizationResponse.getTelecom().stream()
                .filter(t -> "phone".equalsIgnoreCase(t.getSystem()))
                .map(OrganizationResponse.Telecom::getValue)
                .findFirst().orElse(null);
    }

    default String extractType(OrganizationResponse organizationResponse) {
        return organizationResponse.getType().stream()
                .flatMap(t -> t.getCoding().stream())
                .map(OrganizationResponse.Coding::getCode)
                .findFirst().orElse(null);
    }

    default String extractCountryCode(OrganizationResponse organizationResponse) {
        return organizationResponse.getAddress().stream()
                .map(OrganizationResponse.Address::getCountry)
                .findFirst().orElse(null);
    }

    default String extractStateCode(OrganizationResponse organizationResponse) {
        return organizationResponse.getAddress().stream()
                .map(OrganizationResponse.Address::getState)
                .findFirst().orElse(null);
    }

    default String extractCityCode(OrganizationResponse organizationResponse) {
        return organizationResponse.getAddress().stream()
                .map(OrganizationResponse.Address::getCity)
                .findFirst().orElse(null);
    }

    default String extractDistrict(OrganizationResponse organizationResponse) {
        return organizationResponse.getAddress().stream()
                .map(OrganizationResponse.Address::getDistrict)
                .findFirst().orElse(null);
    }

    default String extractLine(OrganizationResponse organizationResponse) {
        return organizationResponse.getAddress().stream()
                .map(OrganizationResponse.Address::getLine)
                .findFirst().orElse(null);
    }

    default String extractServiceArea(OrganizationResponse organizationResponse) {
        if (organizationResponse.getServiceArea() != null && !organizationResponse.getServiceArea().isEmpty()) {
        return organizationResponse.getServiceArea().stream()
                .map(OrganizationResponse.ServiceArea::getState)
                .findFirst().orElse(null);
        }
        return null;
    }

    default String extractTin(OrganizationResponse organizationResponse) {
        if (organizationResponse.getIdentifier() == null || organizationResponse.getIdentifier().isEmpty()) {
            return null;
        }

        return organizationResponse.getIdentifier().stream()
                .filter(identifier -> identifier.getType() != null &&
                        identifier.getType().getCoding().stream()
                                .anyMatch(coding -> "TAX".equalsIgnoreCase(coding.getCode())))
                .map(OrganizationResponse.Identifier::getValue)
                .findFirst()
                .orElse(null);
    }

    default String extractLevel(OrganizationResponse response) {
        if (response.getType() == null || response.getType().isEmpty()) {
            return null;
        }
        Optional<OrganizationResponse.Coding> codingOpt = response.getType().stream()
                .flatMap(type -> type.getCoding().stream())
                .filter(coding -> "http://fhir.ssv.uz/ValueSet/organization-type-level".equals(coding.getSystem()))
                .findFirst();

        return codingOpt.map(coding -> switch (coding.getCode()) {
            case "1" -> LevelStatus.republic_level.name();
            case "2" -> LevelStatus.region_level.name();
            case "3", "4" -> LevelStatus.district_level.name();
            default -> null;
        }).orElse(null);
    }
}
