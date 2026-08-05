package uz.uzinfocom.isemid.features.patient.mappers;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapperHelper;
import uz.uzinfocom.isemid.features.form058.mappers.Form058MapperHelper;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapper;
import uz.uzinfocom.isemid.features.patient.dtos.requests.PatientRequest;
import uz.uzinfocom.isemid.features.patient.dtos.responses.PatientResponse;
import uz.uzinfocom.isemid.features.patient.models.Patient;

@Mapper(componentModel = "spring", uses = {
        Form058MapperHelper.class,
        CatalogMapperHelper.class,
        IdentifierMapper.class,
        AddressMapper.class,
        OrganizationMapper.class,
        AffiliationMapper.class
})
public interface PatientMapper {
    @Mapping(target = "genderCode", source = "gender")
    @Mapping(target = "residentialStatusCode", source = "residentialStatus")
    @Mapping(target = "maritalStatusCode", source = "maritalStatus")
    @Mapping(target = "populationTypeCode", source = "populationType")
    @Mapping(source = "firstName", target = "firstName", qualifiedByName = "toUpperCase")
    @Mapping(source = "lastName", target = "lastName", qualifiedByName = "toUpperCase")
    @Mapping(source = "middleName", target = "middleName", qualifiedByName = "toUpperCase")
    Patient toEntity(PatientRequest request, @Context String source);

    @Mapping(target = "residentialStatus", source = "residentialStatusCode", qualifiedByName = "toCatalog")
    @Mapping(target = "maritalStatus", source = "maritalStatusCode", qualifiedByName = "toCatalog")
    @Mapping(target = "populationType", source = "populationTypeCode", qualifiedByName = "toCatalog")
    @Mapping(target = "category", source = "categoryCode", qualifiedByName = "toCatalog")
    @Mapping(target = "profession", source = "professionCode", qualifiedByName = "toCatalog")
    @Mapping(target = "identifiers", source = "identifiers")
    @Mapping(target = "gender", source = "genderCode", qualifiedByName = "toCatalog")
    @Mapping(target = "addresses", source = "addresses")
    PatientResponse toResponse(Patient patient);
}
