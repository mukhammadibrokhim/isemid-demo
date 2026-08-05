package uz.uzinfocom.isemid.features.patient.mappers;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapperHelper;
import uz.uzinfocom.isemid.features.patient.dtos.requests.AddressRequest;
import uz.uzinfocom.isemid.features.patient.dtos.responses.AddressResponse;
import uz.uzinfocom.isemid.features.patient.models.Address;

@Mapper(componentModel = "spring", uses = {CatalogMapperHelper.class})
public interface AddressMapper {

    @Mapping(target = "neighborhoodCode", source = "neighborhood", qualifiedByName = "toNeighborhoodCode")
    Address toEntity(AddressRequest request,@Context String source);

    @Mapping(target = "state", source = "stateCode", qualifiedByName = "toCatalog")
    @Mapping(target = "city", source = "cityCode", qualifiedByName = "toCatalog")
    @Mapping(target = "neighborhood", source = "neighborhoodCode", qualifiedByName = "toCatalog")
    AddressResponse toAddressResponse(Address address);
}
