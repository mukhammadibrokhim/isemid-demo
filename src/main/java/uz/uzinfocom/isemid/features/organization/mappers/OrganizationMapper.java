package uz.uzinfocom.isemid.features.organization.mappers;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapperHelper;
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.dtos.responses.MisOrganizationResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgResponse;
import uz.uzinfocom.isemid.features.organization.models.Organization;

@Mapper(
        componentModel = "spring",
        uses = {
                CatalogMapperHelper.class,
                UuidConverter.class,
                OrganizationMapperHelper.class
        },
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "id", qualifiedByName = "toUuid")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "tin", source = ".", qualifiedByName = "extractTin")
    @Mapping(target = "email", source = ".", qualifiedByName = "extractEmail")
    @Mapping(target = "phone", source = ".", qualifiedByName = "extractPhone")
    @Mapping(target = "serviceTypes", source = ".", qualifiedByName = "extractServiceTypes")
    @Mapping(target = "medicalType", source = ".", qualifiedByName = "extractMedicalType")
    @Mapping(target = "levelType", source = ".", qualifiedByName = "extractLevelType")
    @Mapping(target = "countryCode", source = ".", qualifiedByName = "extractCountryCode")
    @Mapping(target = "stateCode", source = ".", qualifiedByName = "extractStateCode")
    @Mapping(target = "cityCode", source = ".", qualifiedByName = "extractCityCode")
    @Mapping(target = "line", source = ".", qualifiedByName = "extractLine")
    @Mapping(target = "serviceAreaCode", source = ".", qualifiedByName = "extractServiceArea")
    @Mapping(target = "district", source = ".", qualifiedByName = "extractDistrict")
    Organization toOrganization(MisOrganizationResponse misOrganizationResponse);

    @Mapping(target = "country", source = "countryCode", qualifiedByName = "toCatalog")
    @Mapping(target = "state", source = "stateCode", qualifiedByName = "toCatalog")
    @Mapping(target = "city", source = "cityCode", qualifiedByName = "toCatalog")
    @Mapping(target = "level", source = "levelType", qualifiedByName = "toCatalog")
    @Mapping(target = "children", source = "children", qualifiedByName = "mapChildren")
    OrgResponse toResponse(Organization organization);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "parentId", source = "parentId")
    @Mapping(target = "children", source = "children", qualifiedByName = "mapChildren")
    OrgMiniResponse toOrgMiniResponse(Organization organization);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(OrgRequest request, @MappingTarget Organization org);
}
