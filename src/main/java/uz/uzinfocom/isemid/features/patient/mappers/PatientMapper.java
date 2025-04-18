package uz.uzinfocom.isemid.features.patient.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogConverter;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapper;
import uz.uzinfocom.isemid.features.patient.dtos.responses.PatientResponse;
import uz.uzinfocom.isemid.features.patient.models.Patient;

@Mapper(componentModel = "spring", uses = {
        CatalogConverter.class, IdentifierMapper.class, AddressMapper.class,
        OrganizationMapper.class
})
public interface PatientMapper {
    @Mapping(target = "residentialStatus", source = "residentialStatusCode", qualifiedByName = "toCatalog")
    @Mapping(target = "maritalStatus", source = "maritalStatusCode", qualifiedByName = "toCatalog")
    @Mapping(target = "populationType", source = "populationTypeCode", qualifiedByName = "toCatalog")
    @Mapping(target = "category", source = "categoryCode", qualifiedByName = "toCatalog")
    @Mapping(target = "profession", source = "professionCode", qualifiedByName = "toCatalog")
    @Mapping(target = "workPlaceState", source = "workPlaceStateCode", qualifiedByName = "toCatalog")
    @Mapping(target = "workPlaceCity", source = "workPlaceCityCode", qualifiedByName = "toCatalog")
    @Mapping(target = "identifiers", source = "identifiers")
    @Mapping(target = "gender", source = "genderCode", qualifiedByName = "toCatalog")
    @Mapping(target = "addresses", source = "addresses")
//    @Mapping(target = "workPlaceOrg", source = "workPlaceOrg")
    PatientResponse toPatientResponse(Patient patient);
}
