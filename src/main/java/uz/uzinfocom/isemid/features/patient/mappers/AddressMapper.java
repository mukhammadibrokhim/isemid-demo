package uz.uzinfocom.isemid.features.patient.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogConverter;
import uz.uzinfocom.isemid.features.patient.dtos.responses.AddressResponse;
import uz.uzinfocom.isemid.features.patient.models.Address;

@Mapper(componentModel = "spring", uses = {CatalogConverter.class})
public interface AddressMapper {

    @Mapping(target = "state", source = "stateCode", qualifiedByName = "toCatalog")
    @Mapping(target = "city", source = "cityCode", qualifiedByName = "toCatalog")
    @Mapping(target = "neighborhood", source = "neighborhoodCode", qualifiedByName = "toCatalog")
    AddressResponse toAddressResponse(Address address);
}
