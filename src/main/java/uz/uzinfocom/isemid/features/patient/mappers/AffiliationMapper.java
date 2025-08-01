package uz.uzinfocom.isemid.features.patient.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogConverter;
import uz.uzinfocom.isemid.features.patient.dtos.requests.AffiliationRequest;
import uz.uzinfocom.isemid.features.patient.dtos.responses.AffiliationResponse;
import uz.uzinfocom.isemid.features.patient.models.Affiliation;

@Mapper(componentModel = "spring", uses = {CatalogConverter.class})
public interface AffiliationMapper {
    Affiliation toEntity(AffiliationRequest request);

    @Mapping(target = "state", source = "stateCode", qualifiedByName = "toCatalog")
    @Mapping(target = "city", source = "cityCode", qualifiedByName = "toCatalog")
    AffiliationResponse toResponse(Affiliation entity);
}
